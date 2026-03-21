package ar.com.controlfinanzas.alerts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.EstadoInversion;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.MovimientoService;

public class AlertaManager {

	private final ServicioAlertas servicioAlertas;

	// 🔧 CONFIGURACIÓN SIMPLE (después lo hacemos configurable)
	private static final BigDecimal SALDO_CRITICO = new BigDecimal("1000");
	private static final BigDecimal SALDO_BAJO = new BigDecimal("5000");

	public AlertaManager() {
		this.servicioAlertas = new ServicioAlertas(new ConfiguracionAlertas());
	}

	// ===============================
	// ALERTAS DE INVERSIONES
	// ===============================
	public List<Alerta> generarAlertasInversiones(List<Inversion> inversiones) {

		List<Alerta> alertas = new ArrayList<>();

		if (inversiones == null || inversiones.isEmpty()) {
			return alertas;
		}

		for (Inversion inv : inversiones) {

			ResultadoAlertaInversion resultado = servicioAlertas.evaluar(inv);

			if (resultado.getEstado() == EstadoInversion.VENCIDA) {

				alertas.add(new Alerta("Inversión vencida", inv.getDescripcion() + " ya venció", LocalDate.now(),
						Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.CRITICA));

			} else if (resultado.getEstado() == EstadoInversion.PROXIMA_A_VENCER) {

				long dias = resultado.getDiasRestantes();

				Alerta.Nivel nivel = dias <= 1 ? Alerta.Nivel.HOY
						: (dias <= 7 ? Alerta.Nivel.CRITICA : Alerta.Nivel.PROXIMA);

				alertas.add(new Alerta("Inversión por vencer", inv.getDescripcion() + " vence en " + dias + " días",
						LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, nivel));
			}
		}

		return alertas;
	}

	// ===============================
	// 🆕 ALERTAS DE CUENTAS
	// ===============================
	public List<Alerta> generarAlertasCuentas(List<Cuenta> cuentas) {

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

				alertas.add(new Alerta("Saldo crítico", cuenta.getNombre() + " tiene saldo crítico: $" + saldo,
						LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.CRITICA));

			}
			// 🟠 BAJO
			else if (saldo.compareTo(SALDO_BAJO) <= 0) {

				alertas.add(new Alerta("Saldo bajo", cuenta.getNombre() + " tiene saldo bajo: $" + saldo,
						LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.PROXIMA));
			}
		}

		return alertas;
	}

	public List<Alerta> generarAlertaGastoMensual(MovimientoService movimientoService, Usuario usuario) {

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

			alertas.add(new Alerta("Gasto elevado", "Estás gastando " + porcentaje + "% más que el mes pasado",
					LocalDate.now(), Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.PROXIMA));
		}

		return alertas;
	}

	// ===============================
	// 🧠 MÉTODO CENTRAL (TODO JUNTO)
	// ===============================
	public List<Alerta> generarTodas(List<Inversion> inversiones, List<Cuenta> cuentas,
			MovimientoService movimientoService, Usuario usuario) {

		List<Alerta> alertas = new ArrayList<>();

		alertas.addAll(generarAlertasInversiones(inversiones));
		alertas.addAll(generarAlertasCuentas(cuentas));
		alertas.addAll(generarAlertaGastoMensual(movimientoService, usuario));

		return alertas;
	}
}