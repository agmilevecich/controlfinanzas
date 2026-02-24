package ar.com.controlfinanzas.projection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Cuenta;

public class CalculadoraProyeccionSimple implements CalculadoraProyeccion {

	@Override
	public BigDecimal proyectar(Inversion inversion, LocalDate fechaObjetivo) {

		if (fechaObjetivo.isBefore(inversion.getFechaInicio())) {
			return BigDecimal.ZERO;
		}

		long diasTotales = ChronoUnit.DAYS.between(inversion.getFechaInicio(),
				inversion.getFechaVencimiento() != null ? inversion.getFechaVencimiento() : fechaObjetivo);

		long diasTranscurridos = ChronoUnit.DAYS.between(inversion.getFechaInicio(), fechaObjetivo);

		if (diasTotales <= 0) {
			return inversion.getCapitalInicial();
		}

		BigDecimal proporcion = BigDecimal.valueOf(diasTranscurridos).divide(BigDecimal.valueOf(diasTotales), 6,
				RoundingMode.HALF_UP);

		BigDecimal rendimiento = inversion.getCapitalInicial().multiply(inversion.getTasaAnual()).multiply(proporcion);

		return inversion.getCapitalInicial().add(rendimiento);
	}

	@Override
	public BigDecimal proyectar(Cuenta cuenta, LocalDate fechaObjetivo) {
		if (!cuenta.estaActiva(fechaObjetivo)) {
			return BigDecimal.ZERO;
		}

		long dias = java.time.temporal.ChronoUnit.DAYS.between(cuenta.getFechaInicio(), fechaObjetivo);
		double capitalFinal = cuenta.getCapitalInicial().doubleValue() * Math.pow(1 + cuenta.getInteresDiario(), dias);
		return BigDecimal.valueOf(capitalFinal);
	}

}
