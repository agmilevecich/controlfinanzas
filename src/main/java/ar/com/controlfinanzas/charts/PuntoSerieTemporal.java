package ar.com.controlfinanzas.charts;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PuntoSerieTemporal {

    private LocalDate fecha;
    private BigDecimal valor;

    public PuntoSerieTemporal(LocalDate fecha, BigDecimal valor) {
        this.fecha = fecha;
        this.valor = valor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
