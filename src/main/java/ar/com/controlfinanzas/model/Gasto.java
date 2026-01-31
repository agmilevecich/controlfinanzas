package ar.com.controlfinanzas.model;

import java.time.LocalDate;

public class Gasto {

	private int id;
	private LocalDate fecha;
	private String descripcion;
	private double monto;
	private String categoria;

	public Gasto() {
	}

	public Gasto(int id, LocalDate fecha, String descripcion, double monto, String categoria) {
		this.id = id;
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.categoria = categoria;
	}

	public Gasto(LocalDate fecha, String descripcion, double monto, String categoria) {
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

	public double getMonto() {
		return monto;
	}

	public void setMonto(double monto) {
		this.monto = monto;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
}
