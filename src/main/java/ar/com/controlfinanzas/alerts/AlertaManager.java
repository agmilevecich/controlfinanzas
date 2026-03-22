package ar.com.controlfinanzas.alerts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.alerts.generator.GeneradorAlertas;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.EstadoInversion;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;

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

	// ===============================
	// 🆕 ALERTAS DE GASTOS MENSUALES
	// ===============================

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
	// 🆕 ALERTAS DE TARJETAS
	// ===============================

	public List<Alerta> generarAlertasTarjetas(List<TarjetaCredito> tarjetas, TarjetaCreditoService tarjetaService) {

		List<Alerta> alertas = new ArrayList<>();

		if (tarjetas == null || tarjetas.isEmpty()) {
			return alertas;
		}

		LocalDate hoy = LocalDate.now();

		for (TarjetaCredito tarjeta : tarjetas) {

			BigDecimal deuda = tarjetaService.calcularDeudaTotal(tarjeta);

			// 🚫 SI NO HAY DEUDA → NO ALERTAR
			if (deuda == null || deuda.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			LocalDate vencimiento = tarjetaService.calcularProximoVencimiento(tarjeta);

			long dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, vencimiento);

			BigDecimal minimo = tarjetaService.calcularPagoMinimo(tarjeta);

			if (dias == 0) {

				alertas.add(new Alerta("Tarjeta vence hoy",
						tarjeta.getNombre() + " vence hoy | Deuda: $" + deuda + " | Mínimo: $" + minimo, hoy,
						Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.HOY));

			} else if (dias <= 3) {

				alertas.add(
						new Alerta(
								"Tarjeta por vencer", tarjeta.getNombre() + " vence en " + dias + " días | Deuda: $"
										+ deuda + " | Mínimo: $" + minimo,
								hoy, Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.CRITICA));

			} else if (dias <= 7) {

				alertas.add(new Alerta(
						"Tarjeta próxima a vencer", tarjeta.getNombre() + " vence en " + dias + " días | Deuda: $"
								+ deuda + " | Mínimo: $" + minimo,
						hoy, Alerta.TipoAlerta.VENCIMIENTO, Alerta.Nivel.PROXIMA));
			}
		}

		return alertas;
	}

	// ===============================
	// 🧠 INGRESOS FALTANTE
	// ===============================
	public List<Alerta> generarAlertaIngresosFaltantes(MovimientoService movimientoService, Usuario usuario) {

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

			alertas.add(new Alerta("Ingresos faltantes", "Aún no registraste ingresos este mes", LocalDate.now(),
					Alerta.TipoAlerta.INGRESO, Alerta.Nivel.CRITICA));

			return alertas;
		}

		// 👉 cálculo seguro

		// 🚫 evitar división por cero
		BigDecimal porcentaje = ingresosMesActual.divide(ingresosMesAnterior, 2, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));

		if (porcentaje.compareTo(BigDecimal.valueOf(50)) < 0) {

			alertas.add(new Alerta("Ingresos bajos",
					"Este mes ingresaste solo el " + porcentaje + "% respecto al mes pasado", LocalDate.now(),
					Alerta.TipoAlerta.INGRESO, Alerta.Nivel.CRITICA));
		}

		return alertas;
	}

	// ===============================
	// 🧠 ALERTA MARGEN FALTANTE
	// ===============================
	public List<Alerta> generarAlertaMargenFinanciero(MovimientoService movimientoService,
			TarjetaCreditoService tarjetaService, List<TarjetaCredito> tarjetas, Usuario usuario) {

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
				alertas.add(new Alerta("Sin ingresos", "Tenés gastos o deudas este mes pero no registraste ingresos",
						LocalDate.now(), Alerta.TipoAlerta.FINANZAS, Alerta.Nivel.CRITICA));
			}

			return alertas;
		}

		// 🔴 margen negativo
		if (margen.compareTo(BigDecimal.ZERO) < 0) {

			alertas.add(
					new Alerta("Margen negativo", "Estás gastando más de lo que ingresás. Diferencia: $" + margen.abs(),
							LocalDate.now(), Alerta.TipoAlerta.FINANZAS, Alerta.Nivel.CRITICA));

			return alertas;
		}

		// ⚠️ margen bajo
		BigDecimal porcentaje = margen.divide(ingresos, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

		if (porcentaje.compareTo(BigDecimal.valueOf(20)) < 0) {

			alertas.add(new Alerta("Margen bajo", "Te queda solo un " + porcentaje + "% de margen este mes",
					LocalDate.now(), Alerta.TipoAlerta.FINANZAS, Alerta.Nivel.PROXIMA));
		}

		return alertas;
	}

	// ===============================
	// 🧠 MÉTODO CENTRAL (TODO JUNTO)
	// ===============================
	public List<Alerta> generarTodas(List<Inversion> inversiones, List<Cuenta> cuentas, List<TarjetaCredito> tarjetas,
			MovimientoService movimientoService, TarjetaCreditoService tarjetaCreditoService, Usuario usuario) {

		List<Alerta> alertas = new ArrayList<>();

		List<GeneradorAlertas> generadores = List.of(() -> generarAlertasInversiones(inversiones),
				() -> generarAlertasCuentas(cuentas), () -> generarAlertaGastoMensual(movimientoService, usuario),
				() -> generarAlertasTarjetas(tarjetas, tarjetaCreditoService),
				() -> generarAlertaIngresosFaltantes(movimientoService, usuario),
				() -> generarAlertaMargenFinanciero(movimientoService, tarjetaCreditoService, tarjetas, usuario));

		for (GeneradorAlertas g : generadores) {
			alertas.addAll(g.generar());
		}

		return alertas;
	}
}