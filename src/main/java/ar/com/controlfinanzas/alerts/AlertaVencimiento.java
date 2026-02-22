package ar.com.controlfinanzas.alerts;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.EstadoInversion;

public class AlertaVencimiento {

	private Inversion inversion;
	private int diasRestantes;
	private EstadoInversion estado;

	public AlertaVencimiento(Inversion inversion, LocalDate fechaHoy) {
		this.inversion = inversion;
		calcularDiasRestantes(fechaHoy);
		determinarEstado();
	}

	private void calcularDiasRestantes(LocalDate fechaHoy) {
		if (inversion.getFechaVencimiento() != null) {
			diasRestantes = (int) ChronoUnit.DAYS.between(fechaHoy, inversion.getFechaVencimiento());
		} else {
			diasRestantes = -1; // sin vencimiento
		}
	}

	private void determinarEstado() {
		if (inversion.getFechaVencimiento() == null) {
			estado = EstadoInversion.VIGENTE;
		} else if (diasRestantes < 0) {
			estado = EstadoInversion.VENCIDA;
		} else if (diasRestantes <= 7) {
			estado = EstadoInversion.PROXIMA_A_VENCER;
		} else if (diasRestantes <= 30) {
			estado = EstadoInversion.PROXIMA_A_VENCER;
		} else {
			estado = EstadoInversion.VIGENTE;
		}
	}

	// Getters
	public Inversion getInversion() {
		return inversion;
	}

	public int getDiasRestantes() {
		return diasRestantes;
	}

	public EstadoInversion getEstado() {
		return estado;
	}

	@Override
	public String toString() {
		return inversion.getDescripcion() + " - días restantes: " + diasRestantes + " - estado: " + estado;
	}
}
