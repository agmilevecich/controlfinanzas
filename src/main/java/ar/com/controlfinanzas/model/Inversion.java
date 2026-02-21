package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	private String descripcion;

	@Column(precision = 19, scale = 4)
	private BigDecimal capitalInicial;

	@Column(precision = 19, scale = 4)
	private BigDecimal rendimientoEsperado;

	private LocalDate fechaInicio;
	private LocalDate fechaVencimiento;

	// Campos opcionales / avanzados
	@Column(precision = 19, scale = 4)
	private BigDecimal cantidad;

	@Column(precision = 19, scale = 4)
	private BigDecimal precioUnitario;

	private String broker;
	private String cryptoTipo;

	@ManyToOne(optional = false)
	@JoinColumn(name = "UsuarioID", nullable = false)
	private Usuario usuario;

	/*
	 * ====================== CONSTRUCTORES ======================
	 */

	public Inversion() {
	}

	public Inversion(TipoActivo tipoActivo, TipoInversion tipoInversion, Moneda moneda, String descripcion,
			BigDecimal capitalInicial, BigDecimal rendimientoEsperado, LocalDate fechaInicio,
			LocalDate fechaVencimiento) {

		this.tipoActivo = tipoActivo;
		this.tipoInversion = tipoInversion;
		this.moneda = moneda;
		this.descripcion = descripcion;
		this.capitalInicial = capitalInicial;
		this.rendimientoEsperado = rendimientoEsperado;
		this.fechaInicio = fechaInicio;
		this.fechaVencimiento = fechaVencimiento;

		this.cantidad = BigDecimal.ZERO;
		this.precioUnitario = BigDecimal.ZERO;
	}

	/*
	 * ====================== MÉTODOS DE NEGOCIO ======================
	 */

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
		return capitalInicial;
	}

	/*
	 * ====================== GETTERS & SETTERS ======================
	 */

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
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
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

}
