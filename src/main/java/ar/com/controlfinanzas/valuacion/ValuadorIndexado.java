package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.service.market.ServicioCotizaciones;
import ar.com.controlfinanzas.service.market.ServicioCotizacionesMock;

public class ValuadorIndexado implements ValuadorInversion {

	private final ServicioCotizaciones cotizaciones = new ServicioCotizacionesMock();

	@Override
	public BigDecimal calcularCapitalActual(Inversion inv) {

		String ticker = inv.getTicker();

		BigDecimal indice = cotizaciones.obtenerIndice(ticker);

		if (inv.getCapitalInicial() != null && inv.getCapitalInicial().compareTo(BigDecimal.ZERO) > 0) {
			return inv.getCapitalInicial().multiply(indice);
		}

		if (inv.getCantidad() != null && inv.getPrecioUnitario() != null) {
			return inv.getCantidad().multiply(inv.getPrecioUnitario()).multiply(indice);
		}

		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal calcularIngresoMensual(Inversion inv) {
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal calcularValorAlVencimiento(Inversion inv) {

		BigDecimal indice = cotizaciones.obtenerIndice("UVA");

		if (inv.getCapitalInicial() != null) {
			return inv.getCapitalInicial().multiply(indice);
		}

		return BigDecimal.ZERO;
	}
}