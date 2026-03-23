package ar.com.controlfinanzas.alerts.generator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.alerts.ResultadoAlertaInversion;
import ar.com.controlfinanzas.alerts.ServicioAlertas;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.EstadoInversion;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class AlertasInversiones implements GeneradorAlertas {

	private List<Inversion> inversiones;
	private ServicioAlertas servicioAlertas;
	private DashboardFrame dashboardFrame;

	public AlertasInversiones(List<Inversion> inversiones, ServicioAlertas servicioAlertas,
			DashboardFrame dashboardFrame) {
		this.inversiones = inversiones;
		this.servicioAlertas = servicioAlertas;
		this.dashboardFrame = dashboardFrame;
	}

	@Override
	public List<Alerta> generar() {

		List<Alerta> alertas = new ArrayList<>();

		if (inversiones == null || inversiones.isEmpty()) {
			return alertas;
		}

		for (Inversion inv : inversiones) {

			ResultadoAlertaInversion resultado = servicioAlertas.evaluar(inv);

			if (resultado.getEstado() == EstadoInversion.VENCIDA) {

				Alerta alerta = new Alerta("Inversión vencida", inv.getDescripcion() + " ya venció", LocalDate.now(),
						Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.CRITICA);
				alerta.setAccion(() -> dashboardFrame.irAInversiones());
				alertas.add(alerta);

			} else if (resultado.getEstado() == EstadoInversion.PROXIMA_A_VENCER) {

				long dias = resultado.getDiasRestantes();

				Alerta.Nivel nivel = dias <= 1 ? Alerta.Nivel.HOY
						: (dias <= 7 ? Alerta.Nivel.CRITICA : Alerta.Nivel.PROXIMA);
				Alerta alerta = new Alerta("Inversión por vencer", inv.getDescripcion() + " vence en " + dias + " días",
						LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, nivel);
				alerta.setAccion(() -> dashboardFrame.irAInversiones());
				alertas.add(alerta);
			}
		}

		return alertas;

	}

}
