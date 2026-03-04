package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class ValuadorMonto implements ValuadorInversion {
	@Override
	public BigDecimal calcularCapitalActual(Inversion inv) {
		return inv.getSaldo(); // simple, puede sumar intereses si aplica
	}

	@Override
	public BigDecimal calcularIngresoMensual(Inversion inv) {
		// interés mensual aproximado
		BigDecimal tasaMensual = inv.getTasaAnual().divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP);
		return inv.getSaldo().multiply(tasaMensual);
	}

	@Override
	public BigDecimal calcularValorAlVencimiento(Inversion inv) {
		return inv.calcularInteresAlVencimiento().add(inv.getSaldo());
	}
}