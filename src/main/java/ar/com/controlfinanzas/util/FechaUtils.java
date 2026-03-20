package ar.com.controlfinanzas.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FechaUtils {

	private FechaUtils() {
	}

	public static long diasHasta(LocalDate fecha) {
		return ChronoUnit.DAYS.between(LocalDate.now(), fecha);
	}

	public static boolean estaVencida(LocalDate fechaVencimiento) {
		return fechaVencimiento.isBefore(LocalDate.now());
	}

	public static String obtenerNombreMes(int mes) {
		return java.time.Month.of(mes).getDisplayName(java.time.format.TextStyle.SHORT,
				java.util.Locale.forLanguageTag("es"));
	}

	public static String formatearMesAnio(int mes, int anio) {
		return obtenerNombreMes(mes) + " " + anio;
	}
}
