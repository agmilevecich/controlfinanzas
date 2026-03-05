package ar.com.controlfinanzas.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumeroUtils {

	private static final int SCALE_MONEDA = 2;

	public static BigDecimal parse(String texto) {
		if (texto == null || texto.isBlank()) {
			return BigDecimal.ZERO;
		}

		texto = texto.trim().replace(",", ".");
		return new BigDecimal(texto);
	}

	public static BigDecimal redondearMoneda(BigDecimal valor) {
		if (valor == null) {
			return BigDecimal.ZERO;
		}
		return valor.setScale(SCALE_MONEDA, RoundingMode.HALF_UP);
	}

	public static String formatearMoneda(BigDecimal valor) {
		if (valor == null) {
			return "0.00";
		}
		return redondearMoneda(valor).toString();
	}

	public static String formatearMonedaARS(BigDecimal valor) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("es", "AR"));
		DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
		return df.format(redondearMoneda(valor));
	}
}