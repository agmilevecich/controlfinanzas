package ar.com.controlfinanzas.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Inversion;

public class AlertaService {

	private static final int DIAS_AVISO = 7;

	public List<Alerta> generarAlertasInversiones(List<Inversion> inversiones) {

		List<Alerta> alertas = new ArrayList<>();
		LocalDate hoy = LocalDate.now();

		for (Inversion inv : inversiones) {

			if (inv.getFechaVencimiento() == null) {
				continue;
			}

			long dias = ChronoUnit.DAYS.between(hoy, inv.getFechaVencimiento());

			if (dias >= 0 && dias <= DIAS_AVISO) {
				alertas.add(new Alerta("Vencimiento próximo", inv.getDescripcion() + " vence en " + dias + " días",
						inv.getFechaVencimiento(), Alerta.TipoAlerta.VENCIMIENTO));
			}
		}

		return alertas;
	}
}
