package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.service.market.ServicioCotizaciones;
import ar.com.controlfinanzas.service.market.ServicioCotizacionesMock;

public class ValuadorMercado implements ValuadorInversion {

	private final ServicioCotizaciones cotizaciones = new ServicioCotizacionesMock();

	@Override
	public BigDecimal calcularCapitalActual(Inversion inv) {

		if (inv.getCantidad() == null) {
			return BigDecimal.ZERO;
		}
		if (inv.getTicker() == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal precio = cotizaciones.obtenerPrecio(inv.getTicker());

		return inv.getCantidad().multiply(precio);
	}

	@Override
	public BigDecimal calcularIngresoMensual(Inversion inv) {
		return BigDecimal.ZERO; // mercado puro no paga flujo automático
	}

	@Override
	public BigDecimal calcularValorAlVencimiento(Inversion inv) {
		return calcularCapitalActual(inv);
	}
}
