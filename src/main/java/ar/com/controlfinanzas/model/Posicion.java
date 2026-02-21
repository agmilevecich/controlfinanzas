package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.TipoActivo;

public class Posicion {

    private String clave;
    private TipoActivo tipoActivo;

    private BigDecimal cantidadTotal = BigDecimal.ZERO;
    private BigDecimal capitalTotal = BigDecimal.ZERO;

    public Posicion(String clave, TipoActivo tipoActivo) {
        this.clave = clave;
        this.tipoActivo = tipoActivo;
    }

    public void agregar(Inversion inv) {

        BigDecimal cantidad = inv.getCantidad() != null
                ? inv.getCantidad()
                : BigDecimal.ZERO;

        BigDecimal capital = inv.getCapitalTotalCalculado() != null
                ? inv.getCapitalTotalCalculado()
                : BigDecimal.ZERO;

        cantidadTotal = cantidadTotal.add(cantidad);
        capitalTotal = capitalTotal.add(capital);
    }

    public BigDecimal getPrecioPromedio() {
        if (cantidadTotal.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return capitalTotal.divide(cantidadTotal, 8, RoundingMode.HALF_UP);
    }

    public String getClave() {
        return clave;
    }

    public TipoActivo getTipoActivo() {
        return tipoActivo;
    }

    public BigDecimal getCantidadTotal() {
        return cantidadTotal;
    }

    public BigDecimal getCapitalTotal() {
        return capitalTotal;
    }
}