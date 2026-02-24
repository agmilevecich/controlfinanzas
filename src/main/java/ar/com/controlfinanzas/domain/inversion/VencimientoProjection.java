package ar.com.controlfinanzas.domain.inversion;

public class VencimientoProjection {

    private String descripcion;
    private java.time.LocalDate fecha;
    private java.math.BigDecimal capital;
    private java.math.BigDecimal interes;
    private java.math.BigDecimal total;

    public VencimientoProjection(String descripcion,
                                 java.time.LocalDate fecha,
                                 java.math.BigDecimal capital,
                                 java.math.BigDecimal interes,
                                 java.math.BigDecimal total) {
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.capital = capital;
        this.interes = interes;
        this.total = total;
    }

    public String getDescripcion() { return descripcion; }
    public java.time.LocalDate getFecha() { return fecha; }
    public java.math.BigDecimal getCapital() { return capital; }
    public java.math.BigDecimal getInteres() { return interes; }
    public java.math.BigDecimal getTotal() { return total; }
}