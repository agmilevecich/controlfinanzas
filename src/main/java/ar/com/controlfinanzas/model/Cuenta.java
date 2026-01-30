package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cuenta {

	private String nombre;
	private BigDecimal capitalInicial;
	private double interesDiario; // porcentaje diario, ej: 0.001 = 0.1%
	private LocalDate fechaInicio;

	public Cuenta(String nombre, BigDecimal capitalInicial, double interesDiario, LocalDate fechaInicio) {
		this.nombre = nombre;
		this.capitalInicial = capitalInicial;
		this.interesDiario = interesDiario;
		this.fechaInicio = fechaInicio;
	}

	public String getNombre() {
		return nombre;
	}

	public BigDecimal getCapitalInicial() {
		return capitalInicial;
	}

	public double getInteresDiario() {
		return interesDiario;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public boolean estaActiva(LocalDate fecha) {
		return !fecha.isBefore(fechaInicio);
	}
}
