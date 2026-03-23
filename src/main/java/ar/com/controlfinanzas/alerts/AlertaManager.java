package ar.com.controlfinanzas.alerts;

import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.alerts.generator.AlertaCuentas;
import ar.com.controlfinanzas.alerts.generator.AlertaGastoMensual;
import ar.com.controlfinanzas.alerts.generator.AlertaIngresoFaltante;
import ar.com.controlfinanzas.alerts.generator.AlertaMargenFinanciero;
import ar.com.controlfinanzas.alerts.generator.AlertaTarjetas;
import ar.com.controlfinanzas.alerts.generator.AlertasInversiones;
import ar.com.controlfinanzas.alerts.generator.GeneradorAlertas;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class AlertaManager {

	private final ServicioAlertas servicioAlertas;

	private DashboardFrame dashboardFrame;

	public AlertaManager(DashboardFrame dashboardFrame) {
		this.servicioAlertas = new ServicioAlertas(new ConfiguracionAlertas());
		this.dashboardFrame = dashboardFrame;
	}

	// ===============================
	// 🧠 MÉTODO CENTRAL (TODO JUNTO)
	// ===============================
	public List<Alerta> generarTodas(List<Inversion> inversiones, List<Cuenta> cuentas, List<TarjetaCredito> tarjetas,
			MovimientoService movimientoService, TarjetaCreditoService tarjetaCreditoService, Usuario usuario) {

		List<Alerta> alertas = new ArrayList<>();

		List<GeneradorAlertas> generadores = List.of(
				new AlertasInversiones(inversiones, servicioAlertas, dashboardFrame),
				new AlertaCuentas(cuentas, dashboardFrame),
				new AlertaGastoMensual(movimientoService, usuario, dashboardFrame),
				new AlertaTarjetas(tarjetas, tarjetaCreditoService, dashboardFrame),
				new AlertaIngresoFaltante(movimientoService, usuario, dashboardFrame), new AlertaMargenFinanciero(
						movimientoService, tarjetaCreditoService, tarjetas, usuario, dashboardFrame));

		for (GeneradorAlertas g : generadores) {
			alertas.addAll(g.generar());
		}

		return alertas;
	}
}