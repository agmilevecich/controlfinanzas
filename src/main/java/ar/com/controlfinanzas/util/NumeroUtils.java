package ar.com.controlfinanzas.util;

import java.math.BigDecimal;

public class NumeroUtils {

	public static BigDecimal parse(String texto) {
		if (texto == null || texto.isBlank()) {
			return BigDecimal.ZERO;
		}

		texto = texto.trim().replace(",", ".");

		return new BigDecimal(texto);
	}
}