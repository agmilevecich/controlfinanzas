package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.service.market.ServicioCotizaciones;
import ar.com.controlfinanzas.service.market.ServicioCotizacionesMock;

public class ValuadorMercado implements ValuadorInversion {

	private final ServicioCotizaciones cotizaciones = new ServicioCotizacionesMock();

	@Override
	public BigDecimal calcularCapitalActual(Inversion inv) {

		if (inv.getCantidad() == null || inv.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		// 1️⃣ Si tiene ticker → intentar precio de mercado
		if (inv.getTicker() != null && !inv.getTicker().isBlank()) {

			BigDecimal precioMercado = cotizaciones.obtenerPrecio(inv.getTicker());

			if (precioMercado != null && precioMercado.compareTo(BigDecimal.ZERO) > 0) {
				return inv.getCantidad().multiply(precioMercado);
			}
		}

		// 2️⃣ Fallback → usar precio de compra
		if (inv.getPrecioCompra() != null && inv.getPrecioCompra().compareTo(BigDecimal.ZERO) > 0) {
			return inv.getCantidad().multiply(inv.getPrecioCompra());
		}

		return BigDecimal.ZERO;
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