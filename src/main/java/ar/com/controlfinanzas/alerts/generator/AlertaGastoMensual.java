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

public class AlertaGastoMensual implements GeneradorAlertas {

	private MovimientoService movimientoService;
	private Usuario usuario;
	private DashboardFrame dashboardFrame;

	public AlertaGastoMensual(MovimientoService movimientoService, Usuario usuario, DashboardFrame dashboardFrame) {
		this.movimientoService = movimientoService;
		this.usuario = usuario;
		this.dashboardFrame = dashboardFrame;
	}

	@Override
	public List<Alerta> generar() {

		List<Alerta> alertas = new ArrayList<>();

		BigDecimal gastoMesActual = movimientoService.calcularTotalMesActual(usuario.getUsuarioID());
		BigDecimal gastoMesAnterior = movimientoService.calcularTotalMesAnterior(usuario.getUsuarioID());

		if (gastoMesActual == null || gastoMesAnterior == null) {
			return alertas;
		}

		if (gastoMesAnterior.compareTo(BigDecimal.ZERO) == 0) {
			return alertas;
		}

		BigDecimal diferencia = gastoMesActual.subtract(gastoMesAnterior);

		BigDecimal porcentaje = diferencia.divide(gastoMesAnterior, 2, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));

		if (porcentaje.compareTo(BigDecimal.valueOf(30)) > 0) {

			Alerta alerta = new Alerta("Gasto elevado", "Estás gastando " + porcentaje + "% más que el mes pasado",
					LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.PROXIMA);
			alerta.setAccion(() -> dashboardFrame.irAMovimientos());
			alertas.add(alerta);
		}

		return alertas;

	}

}
