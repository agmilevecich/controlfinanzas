package ar.com.controlfinanzas.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FechaUtils {

    private FechaUtils() {}

    public static long diasHasta(LocalDate fecha) {
        return ChronoUnit.DAYS.between(LocalDate.now(), fecha);
    }

    public static boolean estaVencida(LocalDate fechaVencimiento) {
        return fechaVencimiento.isBefore(LocalDate.now());
    }
}
