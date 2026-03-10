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

	private Integer numeroCuota;
	private Integer totalCuotas;
	private String compraId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cuenta_id", nullable = false)
	private Cuenta cuenta;

	// NUEVO: tarjeta usada (solo si formaPago = CREDITO)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tarjeta_id")
	private TarjetaCredito tarjeta;

	// NUEVO: indica si la deuda de la tarjeta ya fue pagada
	@Column(nullable = false)
	private boolean pendiente = false;

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

	public Integer getNumeroCuota() {
		return numeroCuota;
	}

	public void setNumeroCuota(Integer numeroCuota) {
		this.numeroCuota = numeroCuota;
	}

	public Integer getTotalCuotas() {
		return totalCuotas;
	}

	public void setTotalCuotas(Integer totalCuotas) {
		this.totalCuotas = totalCuotas;
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
		this.monto = monto;
	}

	public void validar() {

		if (formaPago == FormaPago.CREDITO && tarjeta == null) {
			throw new IllegalStateException("Los movimientos con tarjeta de crédito deben tener una tarjeta asociada");
		}

		if (formaPago != FormaPago.CREDITO && tarjeta != null) {
			throw new IllegalStateException("Solo los movimientos con forma de pago CREDITO pueden tener tarjeta");
		}
	}
}