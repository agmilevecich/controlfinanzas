package ar.com.controlfinanzas.domain.inversion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.model.FrecuenciaIngreso;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "inversion")
public class Inversion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TipoActivo tipoActivo;

	@Enumerated(EnumType.STRING)
	private TipoInversion tipoInversion;

	@Enumerated(EnumType.STRING)
	private Moneda moneda;

	@Enumerated(EnumType.STRING)
	private FrecuenciaIngreso frecuenciaIngreso;

	private String descripcion;

	@Column(precision = 19, scale = 4)
	private BigDecimal capitalInicial;

	@Column(precision = 19, scale = 4)
	private BigDecimal rendimientoEsperado;

	private LocalDate fechaInicio;
	private LocalDate fechaVencimiento;

	@Column(precision = 19, scale = 4)
	private BigDecimal cantidad;

	@Column(precision = 19, scale = 4)
	private BigDecimal precioUnitario;

	@Column(precision = 7, scale = 4)
	private BigDecimal tasaAnual;

	private Integer plazoDias;

	private String broker;
	private String cryptoTipo;

	@ManyToOne(optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@OneToMany(mappedBy = "inversion", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FlujoIngreso> flujos = new ArrayList<>();

	public Inversion() {
	}

	public Inversion(TipoActivo tipoActivo, TipoInversion tipoInversion, Moneda moneda, String descripcion,
			BigDecimal capitalInicial, BigDecimal rendimientoEsperado, LocalDate fechaInicio,
			LocalDate fechaVencimiento) {

		this.tipoActivo = tipoActivo;
		this.tipoInversion = tipoInversion;
		this.moneda = moneda;
		this.setFrecuenciaIngreso(getTipoInversion().frecuenciaSugerida());
		this.descripcion = descripcion;
		this.capitalInicial = capitalInicial;
		this.rendimientoEsperado = rendimientoEsperado;
		this.fechaInicio = fechaInicio;
		this.fechaVencimiento = fechaVencimiento;

		this.cantidad = BigDecimal.ZERO;
		this.precioUnitario = BigDecimal.ZERO;
	}

	/* ================= NEGOCIO ================= */

	public boolean tieneVencimiento() {
		return fechaVencimiento != null;
	}

	public String getNombre() {
		return descripcion;
	}

	public BigDecimal getCapitalTotalCalculado() {
		if (cantidad != null && precioUnitario != null && cantidad.compareTo(BigDecimal.ZERO) > 0
				&& precioUnitario.compareTo(BigDecimal.ZERO) > 0) {
			return cantidad.multiply(precioUnitario);
		}
		return capitalInicial != null ? capitalInicial : BigDecimal.ZERO;
	}

	public boolean tieneTasa() {
		return tasaAnual != null && tasaAnual.compareTo(BigDecimal.ZERO) > 0;
	}

	public BigDecimal calcularCapitalProyectado() {
		if (!tieneTasa()) {
			return getCapitalTotalCalculado();
		}

		BigDecimal capital = getCapitalTotalCalculado();

		BigDecimal interes = capital.multiply(tasaAnual).divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);

		if (plazoDias != null && plazoDias > 0) {
			interes = interes.multiply(new BigDecimal(plazoDias)).divide(new BigDecimal("365"), 8,
					RoundingMode.HALF_UP);
		}

		return capital.add(interes);
	}

	public BigDecimal calcularIngresoMensual() {
		if (!tieneTasa()) {
			return BigDecimal.ZERO;
		}

		BigDecimal capital = getCapitalTotalCalculado();

		return capital.multiply(tasaAnual).divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
				.divide(new BigDecimal("12"), 8, RoundingMode.HALF_UP);
	}

	public boolean esGeneradoraDeIngresos() {
		return tieneTasa();
	}

	public void agregarFlujo(FlujoIngreso flujo) {
		flujo.setInversion(this);
		this.flujos.add(flujo);
	}

	public BigDecimal calcularIngresoMensualEstimado() {

		if (tasaAnual == null || frecuenciaIngreso == null) {
			return BigDecimal.ZERO;
		}

		if (frecuenciaIngreso == FrecuenciaIngreso.AL_VENCIMIENTO) {
			return BigDecimal.ZERO;
		}

		BigDecimal capital = getCapitalTotalCalculado();

		if (capital == null || capital.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		int periodos = frecuenciaIngreso.periodosPorAnio();

		if (periodos == 0) {
			return BigDecimal.ZERO;
		}

		// tasa periodo = tasa anual / periodos
		BigDecimal tasaPeriodo = tasaAnual.divide(BigDecimal.valueOf(periodos), 8, java.math.RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(100), 8, java.math.RoundingMode.HALF_UP);

		BigDecimal ingresoPeriodo = capital.multiply(tasaPeriodo);

		// convertir a mensual
		BigDecimal ingresoMensual = ingresoPeriodo.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(periodos),
				8, java.math.RoundingMode.HALF_UP);

		return ingresoMensual;
	}

	public BigDecimal calcularGananciaAlVencimiento() {

		if (tasaAnual == null || getCapitalTotalCalculado() == null) {
			return BigDecimal.ZERO;
		}

		if (frecuenciaIngreso != FrecuenciaIngreso.AL_VENCIMIENTO) {
			return BigDecimal.ZERO;
		}

		if (fechaInicio == null || fechaVencimiento == null) {
			return BigDecimal.ZERO;
		}

		long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaVencimiento);

		if (dias <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal capital = getCapitalTotalCalculado();

		BigDecimal tasa = tasaAnual.divide(BigDecimal.valueOf(100), 8, java.math.RoundingMode.HALF_UP);

		BigDecimal interes = capital.multiply(tasa).multiply(BigDecimal.valueOf(dias)).divide(BigDecimal.valueOf(365),
				8, java.math.RoundingMode.HALF_UP);

		return interes;
	}

	public BigDecimal calcularInteresAlVencimiento() {

		if (tasaAnual == null || fechaInicio == null || fechaVencimiento == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal capital = getCapitalTotalCalculado();

		if (capital == null || capital.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaVencimiento);

		if (dias <= 0) {
			return BigDecimal.ZERO;
		}

		// tasa diaria = tasa anual / 365
		BigDecimal tasaDiaria = tasaAnual.divide(BigDecimal.valueOf(365), 10, java.math.RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP);

		BigDecimal interes = capital.multiply(tasaDiaria).multiply(BigDecimal.valueOf(dias));

		return interes;
	}

	public BigDecimal calcularCapitalFinalEstimado() {
		return getCapitalTotalCalculado().add(calcularInteresAlVencimiento());
	}

	/* ================= GETTERS ================= */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoActivo getTipoActivo() {
		return tipoActivo;
	}

	public void setTipoActivo(TipoActivo tipoActivo) {
		this.tipoActivo = tipoActivo;
	}

	public TipoInversion getTipoInversion() {
		return tipoInversion;
	}

	public void setTipoInversion(TipoInversion tipoInversion) {
		this.tipoInversion = tipoInversion;
		if (tipoInversion != null) {
			this.frecuenciaIngreso = tipoInversion.frecuenciaSugerida();
		}
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public FrecuenciaIngreso getFrecuenciaIngreso() {
		return frecuenciaIngreso;
	}

	public void setFrecuenciaIngreso(FrecuenciaIngreso frecuenciaIngreso) {
		this.frecuenciaIngreso = frecuenciaIngreso;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getCapitalInicial() {
		return capitalInicial;
	}

	public void setCapitalInicial(BigDecimal capitalInicial) {
		this.capitalInicial = capitalInicial;
	}

	public BigDecimal getRendimientoEsperado() {
		return rendimientoEsperado;
	}

	public void setRendimientoEsperado(BigDecimal rendimientoEsperado) {
		this.rendimientoEsperado = rendimientoEsperado;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(LocalDate fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
	}

	public BigDecimal getTasaAnual() {
		return tasaAnual;
	}

	public void setTasaAnual(BigDecimal tasaAnual) {
		this.tasaAnual = tasaAnual;
	}

	public Integer getPlazoDias() {
		return plazoDias;
	}

	public void setPlazoDias(Integer plazoDias) {
		this.plazoDias = plazoDias;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public String getCryptoTipo() {
		return cryptoTipo;
	}

	public void setCryptoTipo(String cryptoTipo) {
		this.cryptoTipo = cryptoTipo;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<FlujoIngreso> getFlujos() {
		return flujos;
	}

	public void setFlujos(List<FlujoIngreso> flujos) {
		this.flujos = flujos;
	}

}