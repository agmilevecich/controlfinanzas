package ar.com.controlfinanzas.alerts.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class AlertaMargenFinanciero implements GeneradorAlertas {

	private MovimientoService movimientoService;
	private List<TarjetaCredito> tarjetas;
	private Usuario usuario;
	private TarjetaCreditoService tarjetaService;
	private DashboardFrame dashboardFrame;

	public AlertaMargenFinanciero(MovimientoService movimientoService, TarjetaCreditoService tarjetaService,
			List<TarjetaCredito> tarjetas, Usuario usuario, DashboardFrame dashboarFrame) {
		this.movimientoService = movimientoService;
		this.tarjetaService = tarjetaService;
		this.tarjetas = tarjetas;
		this.usuario = usuario;
		this.dashboardFrame = dashboarFrame;
	}

	@Override
	public List<Alerta> generar() {

		List<Alerta> alertas = new ArrayList<>();

		BigDecimal ingresos = movimientoService.calcularIngresosMesActual(usuario.getUsuarioID());
		BigDecimal gastos = movimientoService.calcularTotalMesActual(usuario.getUsuarioID());

		if (ingresos == null) {
			ingresos = BigDecimal.ZERO;
		}
		if (gastos == null) {
			gastos = BigDecimal.ZERO;
		}

		// 💳 deuda total de tarjetas
		BigDecimal deudaTarjetas = BigDecimal.ZERO;

		if (tarjetas != null) {
			for (TarjetaCredito t : tarjetas) {
				BigDecimal deuda = tarjetaService.calcularDeudaTotal(t);
				if (deuda != null && deuda.compareTo(BigDecimal.ZERO) > 0) {
					deudaTarjetas = deudaTarjetas.add(deuda);
				}
			}
		}

		BigDecimal margen = ingresos.subtract(gastos).subtract(deudaTarjetas);

		// 🚫 CASO CLAVE: evitar división por cero
		if (ingresos.compareTo(BigDecimal.ZERO) <= 0) {

			if (gastos.compareTo(BigDecimal.ZERO) > 0 || deudaTarjetas.compareTo(BigDecimal.ZERO) > 0) {
				Alerta alerta = new Alerta("Sin ingresos",
						"Tenés gastos o deudas este mes pero no registraste ingresos", LocalDate.now(),
						Alerta.TipoAlerta.FINANZAS, Alerta.Nivel.CRITICA);
				alerta.setAccion(() -> dashboardFrame.irAResumen());
				alertas.add(alerta);
			}

			return alertas;
		}

		// 🔴 margen negativo
		if (margen.compareTo(BigDecimal.ZERO) < 0) {
			Alerta alerta = new Alerta("Margen negativo",
					"Estás gastando más de lo que ingresás. Diferencia: $" + margen.abs(), LocalDate.now(),
					Alerta.TipoAlerta.FINANZAS, Alerta.Nivel.CRITICA);
			alerta.setAccion(() -> dashboardFrame.irAResumen());
			alertas.add(alerta);

			return alertas;
		}

		// ⚠️ margen bajo
		BigDecimal porcentaje = margen.divide(ingresos, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

		if (porcentaje.compareTo(BigDecimal.valueOf(20)) < 0) {
			Alerta alerta = new Alerta("Margen bajo", "Te queda solo un " + porcentaje + "% de margen este mes",
					LocalDate.now(), Alerta.TipoAlerta.FINANZAS, Alerta.Nivel.PROXIMA);
			alerta.setAccion(() -> dashboardFrame.irAResumen());
			alertas.add(alerta);
		}

		return alertas;

	}

}
