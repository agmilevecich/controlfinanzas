package ar.com.controlfinanzas.alerts.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class AlertaIngresoFaltante implements GeneradorAlertas {

	private MovimientoService movimientoService;
	private Usuario usuario;
	private DashboardFrame dashboardFrame;

	public AlertaIngresoFaltante(MovimientoService movimientoService, Usuario usuario, DashboardFrame dashboardFrame) {
		this.movimientoService = movimientoService;
		this.usuario = usuario;
		this.dashboardFrame = dashboardFrame;
	}

	@Override
	public List<Alerta> generar() {

		List<Alerta> alertas = new ArrayList<>();

		BigDecimal ingresosMesActual = movimientoService.calcularIngresosMesActual(usuario.getUsuarioID());
		BigDecimal ingresosMesAnterior = movimientoService.calcularIngresosMesAnterior(usuario.getUsuarioID());

		if (ingresosMesActual == null || ingresosMesAnterior == null) {
			return alertas;
		}

		// 🚫 evitar división por cero
		if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) <= 0) {
			return alertas;
		}

		// 👉 si el mes pasado no hubo ingresos relevantes, no alertar
		if (ingresosMesAnterior.compareTo(new BigDecimal("50000")) < 0) {
			return alertas;
		}

		// 👉 si este mes no hay ingresos en absoluto
		if (ingresosMesActual.compareTo(BigDecimal.ZERO) == 0) {

			Alerta alerta = new Alerta("Ingresos faltantes", "Aún no registraste ingresos este mes", LocalDate.now(),
					Alerta.TipoAlerta.INGRESO, Alerta.Nivel.CRITICA);
			alerta.setAccion(() -> dashboardFrame.irAMovimientos());
			alertas.add(alerta);

			return alertas;
		}

		// 👉 cálculo seguro

		// 🚫 evitar división por cero
		BigDecimal porcentaje = ingresosMesActual.divide(ingresosMesAnterior, 2, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));

		if (porcentaje.compareTo(BigDecimal.valueOf(50)) < 0) {

			Alerta alerta = new Alerta("Ingresos bajos",
					"Este mes ingresaste solo el " + porcentaje + "% respecto al mes pasado", LocalDate.now(),
					Alerta.TipoAlerta.INGRESO, Alerta.Nivel.CRITICA);
			alerta.setAccion(() -> dashboardFrame.irAMovimientos());
			alertas.add(alerta);
		}

		return alertas;

	}

}
