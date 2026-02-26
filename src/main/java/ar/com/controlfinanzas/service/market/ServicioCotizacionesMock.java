package ar.com.controlfinanzas.service.market;

import java.math.BigDecimal;

public class ServicioCotizacionesMock implements ServicioCotizaciones {

    @Override
    public BigDecimal obtenerPrecio(String ticker) {

        if (ticker == null) return BigDecimal.ONE;

        return switch (ticker) {
            case "BTC" -> new BigDecimal("60000");
            case "FCI_SANTANDER_AHORRO" -> new BigDecimal("120.5");
            default -> BigDecimal.ONE;
        };
    }

    @Override
    public BigDecimal obtenerIndice(String nombreIndice) {

        if (nombreIndice == null) return BigDecimal.ONE;

        return switch (nombreIndice) {
            case "UVA" -> new BigDecimal("1.35");
            default -> BigDecimal.ONE;
        };
    }
}