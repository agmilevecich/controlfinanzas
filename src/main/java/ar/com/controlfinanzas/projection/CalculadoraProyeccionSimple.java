package ar.com.controlfinanzas.projection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import ar.com.controlfinanzas.model.Inversion;

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

		BigDecimal rendimiento = inversion.getCapitalInicial().multiply(inversion.getRendimientoEsperado())
				.multiply(proporcion);

		return inversion.getCapitalInicial().add(rendimiento);
	}
}
