package ar.com.controlfinanzas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.controlfinanzas.alerts.AlertaVencimiento;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.EstadoInversion;

public class AlertaInversionService {

	private int diasUmbral = 7; // configurable después

	public List<Inversion> proximasAVencer(List<Inversion> inversiones) {
		LocalDate hoy = LocalDate.now();

		return inversiones.stream().filter(inv -> {
			AlertaVencimiento alerta = new AlertaVencimiento(inv, hoy);
			return alerta.getEstado() == EstadoInversion.PROXIMA_A_VENCER;
		}).collect(Collectors.toList());
	}

	public List<Inversion> vencidas(List<Inversion> inversiones) {
		LocalDate hoy = LocalDate.now();

		return inversiones.stream().filter(inv -> {
			AlertaVencimiento alerta = new AlertaVencimiento(inv, hoy);
			return alerta.getEstado() == EstadoInversion.VENCIDA;
		}).collect(Collectors.toList());
	}

	public List<Inversion> vigentes(List<Inversion> inversiones) {
		LocalDate hoy = LocalDate.now();

		return inversiones.stream().filter(inv -> {
			AlertaVencimiento alerta = new AlertaVencimiento(inv, hoy);
			return alerta.getEstado() == EstadoInversion.VIGENTE;
		}).collect(Collectors.toList());
	}
}
