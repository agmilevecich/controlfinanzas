package ar.com.controlfinanzas.alerts.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class AlertaCuentas implements GeneradorAlertas {

	private List<Cuenta> cuentas;
	private DashboardFrame dashboardFrame;
	private static final BigDecimal SALDO_CRITICO = new BigDecimal("1000");
	private static final BigDecimal SALDO_BAJO = new BigDecimal("5000");

	public AlertaCuentas(List<Cuenta> cuentas, DashboardFrame dashboardFrame) {
		this.cuentas = cuentas;
		this.dashboardFrame = dashboardFrame;
	}

	@Override
	public List<Alerta> generar() {

		List<Alerta> alertas = new ArrayList<>();

		if (cuentas == null || cuentas.isEmpty()) {
			return alertas;
		}

		for (Cuenta cuenta : cuentas) {

			BigDecimal saldo = cuenta.getSaldo();

			if (saldo == null) {
				continue;
			}

			// 🔴 CRÍTICO
			if (saldo.compareTo(SALDO_CRITICO) <= 0) {

				Alerta alerta = new Alerta("Saldo crítico", cuenta.getNombre() + " tiene saldo crítico: $" + saldo,
						LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.CRITICA);
				alerta.setAccion(() -> dashboardFrame.irACuentas(cuenta));
				alertas.add(alerta);

			}
			// 🟠 BAJO
			else if (saldo.compareTo(SALDO_BAJO) <= 0) {
				Alerta alerta = new Alerta("Saldo bajo", cuenta.getNombre() + " tiene saldo bajo: $" + saldo,
						LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.PROXIMA);
				alerta.setAccion(() -> dashboardFrame.irACuentas(cuenta));
				alertas.add(alerta);
			}
		}

		return alertas;

	}

}
