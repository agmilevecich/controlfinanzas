package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimientos")
public class Movimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate fecha;

	@Column(name = "descripcion", length = 50, nullable = false)
	private String descripcion;

	@Column(precision = 19, scale = 4)
	private BigDecimal monto;

	@Enumerated(EnumType.STRING)
	private TipoMovimiento tipo;

	@Enumerated(EnumType.STRING)
	private FormaPago formaPago;

	private String compraId;
	private int numeroCuotas;
	private int totalCuotas;
	private int cuotasPendientes;
	private BigDecimal interes;
	private String periodo;

	private BigDecimal montoPagado = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	private CategoriaGasto categoria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cuenta_id", nullable = true)
	private Cuenta cuenta;

	// NUEVO: tarjeta usada (solo si formaPago = CREDITO)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tarjeta_id")
	private TarjetaCredito tarjeta;

	// NUEVO: indica si la deuda de la tarjeta ya fue pagada
	@Column(nullable = false)
	private boolean pendiente = false;

	@ManyToOne
	private Usuario usuario;

	public Movimiento() {
	}

	public Movimiento(LocalDate fecha, String descripcion, BigDecimal monto, TipoMovimiento tipo) {
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.tipo = tipo;
	}

	// getters

	public Long getId() {
		return id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public TipoMovimiento getTipo() {
		return tipo;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public FormaPago getFormaPago() {
		return formaPago;
	}

	public TarjetaCredito getTarjeta() {
		return tarjeta;
	}

	public boolean isPendiente() {
		return pendiente;
	}

	// setters

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}

	public void setFormaPago(FormaPago formaPago) {
		this.formaPago = formaPago;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setTarjeta(TarjetaCredito tarjeta) {
		this.tarjeta = tarjeta;
	}

	public void setPendiente(boolean pendiente) {
		this.pendiente = pendiente;
	}

	public int getCuotasPendientes() {
		return cuotasPendientes;
	}

	public void setCuotasPendientes(int cuotasPendientes) {
		this.cuotasPendientes = cuotasPendientes;
	}

	public BigDecimal getInteres() {
		return interes;
	}

	public void setInteres(BigDecimal interes) {
		this.interes = interes;
	}

	public String getCompraId() {
		return compraId;
	}

	public void setCompraId(String compraId) {
		this.compraId = compraId;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = (monto != null) ? monto : BigDecimal.ZERO;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTipo(TipoMovimiento tipo) {
		this.tipo = tipo;
	}

	public int getNumeroCuotas() {
		return numeroCuotas;
	}

	public void setNumeroCuotas(int numeroCuotas) {
		this.numeroCuotas = numeroCuotas;
	}

	public int getTotalCuotas() {
		return totalCuotas;
	}

	public void setTotalCuotas(int totalCuotas) {
		this.totalCuotas = totalCuotas;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public BigDecimal getMontoPagado() {
		return montoPagado == null ? BigDecimal.ZERO : montoPagado;
	}

	public void setMontoPagado(BigDecimal montoPagado) {
		this.montoPagado = (montoPagado != null) ? montoPagado : BigDecimal.ZERO;
	}

	public CategoriaGasto getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaGasto categoria) {
		this.categoria = categoria;
	}

	public BigDecimal getRestante() {
		return monto.subtract(montoPagado);
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void validar() {

		// ✔ Regla de categoría SOLO para gastos
		if (tipo == TipoMovimiento.GASTO && categoria == null) {
			throw new IllegalStateException("Los gastos deben tener categoría");
		}

		if (formaPago == FormaPago.CREDITO) {
			// Movimientos de tarjeta
			if (tarjeta == null) {
				throw new IllegalStateException(
						"Los movimientos con tarjeta de crédito deben tener una tarjeta asociada");
			}
			if (cuenta != null) {
				throw new IllegalStateException("Las compras con tarjeta no deben estar asociadas a una cuenta");
			}
		} else {
			// Movimientos de cuenta normales
			if (tarjeta != null) {
				throw new IllegalStateException("Solo los movimientos con forma de pago CREDITO pueden tener tarjeta");
			}
			if (cuenta == null) {
				throw new IllegalStateException("Los movimientos que no son de tarjeta deben tener una cuenta");
			}
		}

	}
}