package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class ValuadorPrecio implements ValuadorInversion {
	@Override
	public BigDecimal calcularCapitalActual(Inversion inv) {
		return inv.getCantidad().multiply(inv.getPrecioUnitario());
	}

	@Override
	public BigDecimal calcularIngresoMensual(Inversion inv) {
		return BigDecimal.ZERO; // por defecto acciones/cripto no generan ingreso mensual
	}

	@Override
	public BigDecimal calcularValorAlVencimiento(Inversion inv) {
		return calcularCapitalActual(inv); // valor actual = valor al vencimiento
	}
}