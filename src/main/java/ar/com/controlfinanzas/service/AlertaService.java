package ar.com.controlfinanzas.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;

public class AlertaService {

	private static final int DIAS_AVISO = 7;

	public List<Alerta> generarAlertasInversiones(List<Inversion> inversiones) {

		AlertaInversionService clasificador = new AlertaInversionService();
		List<Inversion> proximas = clasificador.proximasAVencer(inversiones);

		List<Alerta> alertas = new ArrayList<>();
		LocalDate hoy = LocalDate.now();

		for (Inversion inv : proximas) {

			if (inv.getFechaVencimiento() == null) {
				continue;
			}

			long dias = ChronoUnit.DAYS.between(hoy, inv.getFechaVencimiento());

			// Fuera de rango → no genera alerta
			if (dias < 0 || dias > DIAS_AVISO) {
				continue;
			}

			Alerta.Nivel nivel;
			String titulo;

			if (dias == 0) {
				nivel = Alerta.Nivel.HOY;
				titulo = "Vence hoy";
			} else if (dias <= 2) {
				nivel = Alerta.Nivel.CRITICA;
				titulo = "Vencimiento crítico";
			} else {
				nivel = Alerta.Nivel.PROXIMA;
				titulo = "Vencimiento próximo";
			}

			String mensaje = inv.getDescripcion() + " vence en " + dias + " días";

			alertas.add(new Alerta(titulo, mensaje, inv.getFechaVencimiento(), Alerta.TipoAlerta.VENCIMIENTO, nivel));
		}

		return alertas;
	}
}
