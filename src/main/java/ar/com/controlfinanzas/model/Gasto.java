package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "gastos")
public class Gasto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private LocalDate fecha;
	private String descripcion;
	private BigDecimal monto;
	private String categoria;

	public Gasto() {
	}

	public Gasto(int id, LocalDate fecha, String descripcion, BigDecimal monto, String categoria) {
		this.id = id;
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.categoria = categoria;
	}

	public Gasto(LocalDate fecha, String descripcion, BigDecimal monto, String categoria) {
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.categoria = categoria;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
}
