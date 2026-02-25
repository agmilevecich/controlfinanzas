package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public interface ValuadorInversion {
	BigDecimal calcularCapitalActual(Inversion inv);

	BigDecimal calcularIngresoMensual(Inversion inv);

	BigDecimal calcularValorAlVencimiento(Inversion inv);
}