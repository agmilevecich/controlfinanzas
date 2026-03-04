package ar.com.controlfinanzas.service;

import java.math.BigDecimal;

import ar.com.controlfinanzas.model.Moneda;

public class CurrencyService {

	// Convierte cualquier moneda a la moneda base (ARS)
	public BigDecimal convertirAMonedaBase(BigDecimal monto, Moneda moneda) {
		if (monto == null) {
			return BigDecimal.ZERO;
		}

		switch (moneda) {
		case USD:
			return monto.multiply(new BigDecimal("1400")); // ejemplo, tasa fija
		case EUR:
			return monto.multiply(new BigDecimal("380")); // ejemplo
		case ARS:
		default:
			return monto;
		}
	}
}