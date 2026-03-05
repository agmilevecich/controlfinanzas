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

	@Column(precision = 19, scale = 4)
	private BigDecimal monto;

	@Enumerated(EnumType.STRING)
	private TipoMovimiento tipo;

	@Enumerated(EnumType.STRING)
	private FormaPago formaPago;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cuenta_id", nullable = false)
	private Cuenta cuenta;

	protected Movimiento() {
	}

	public Movimiento(LocalDate fecha, BigDecimal monto, TipoMovimiento tipo) {
		this.fecha = fecha;
		this.monto = monto;
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getFecha() {
		return fecha;
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

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}

	public FormaPago getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(FormaPago formaPago) {
		this.formaPago = formaPago;
	}

	public void setDescripcion(String string) {

	}

}