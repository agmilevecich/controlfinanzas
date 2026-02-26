package ar.com.controlfinanzas.service.market;

import java.math.BigDecimal;

public interface ServicioCotizaciones {

    BigDecimal obtenerPrecio(String ticker);

    BigDecimal obtenerIndice(String nombreIndice);

}