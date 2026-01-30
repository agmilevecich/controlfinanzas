package ar.com.controlfinanzas.charts;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.projection.CalculadoraProyeccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GeneradorEvolucionPatrimonio {

    private final CalculadoraProyeccion calculadora;

    public GeneradorEvolucionPatrimonio(CalculadoraProyeccion calculadora) {
        this.calculadora = calculadora;
    }

    public List<PuntoSerieTemporal> generar(
            List<Inversion> inversiones,
            List<LocalDate> fechas) {

        List<PuntoSerieTemporal> serie = new ArrayList<>();

        for (LocalDate fecha : fechas) {
            BigDecimal total = BigDecimal.ZERO;

            for (Inversion inv : inversiones) {
                total = total.add(
                        calculadora.proyectar(inv, fecha)
                );
            }

            serie.add(new PuntoSerieTemporal(fecha, total));
        }
        return serie;
    }
}
