package ar.com.controlfinanzas.valuacion;

import java.math.BigDecimal;
import java.time.LocalDate;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class ValuadorIndexado implements ValuadorInversion {

	@Override
	public BigDecimal calcularCapitalActual(Inversion inv) {

		BigDecimal cotizacion = obtenerCotizacion(inv);

		BigDecimal cantidad = inv.getCantidad() != null ? inv.getCantidad() : BigDecimal.ZERO;
		BigDecimal capital = inv.getCapitalInicial() != null ? inv.getCapitalInicial() : BigDecimal.ZERO;

		// ✅ Si hay cantidad real → valuación por cantidad
		if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
			return cantidad.multiply(cotizacion);
		}

		// ✅ Si no → usar capital inicial
		return capital.multiply(cotizacion);
	}

	@Override
	public BigDecimal calcularIngresoMensual(Inversion inv) {
		// Si es indexado, normalmente no hay pago mensual, solo al vencimiento
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal calcularValorAlVencimiento(Inversion inv) {

		BigDecimal cotizacionVto = obtenerCotizacion(inv.getFechaVencimiento());

		BigDecimal cantidad = inv.getCantidad() != null ? inv.getCantidad() : BigDecimal.ZERO;
		BigDecimal capital = inv.getCapitalInicial() != null ? inv.getCapitalInicial() : BigDecimal.ZERO;

		if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
			return cantidad.multiply(cotizacionVto);
		}

		return capital.multiply(cotizacionVto);
	}

	// Aquí podés implementar scraping o API para UVA, cripto, etc.
	private BigDecimal obtenerCotizacion(Inversion inv) {
		// Por ahora devuelvo 1 para pruebas
		return BigDecimal.ONE;
	}

	private BigDecimal obtenerCotizacion(LocalDate fecha) {
		// Por ahora devuelvo 1 para pruebas
		return BigDecimal.ONE;
	}
}