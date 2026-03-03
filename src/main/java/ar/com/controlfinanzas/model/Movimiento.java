package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
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
@Table(name = "movimientos")
public class Movimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal monto;
	private String descripcion;
	private LocalDate fecha;

	@Enumerated(EnumType.STRING)
	private TipoMovimiento tipo; // INGRESO o EGRESO

	@ManyToOne
	@JoinColumn(name = "cuenta_id", nullable = false)
	private Cuenta cuenta;

	public Movimiento() {
	}

	public Movimiento(BigDecimal monto, String descripcion, LocalDate fecha, TipoMovimiento tipo, Cuenta cuenta) {
		this.monto = monto;
		this.descripcion = descripcion;
		this.fecha = fecha;
		this.tipo = tipo;
		this.cuenta = cuenta;
	}

	// Getters y setters
	public Long getId() {
		return id;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public TipoMovimiento getTipo() {
		return tipo;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public void setTipo(TipoMovimiento tipo) {
		this.tipo = tipo;
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}
}