package ar.com.controlfinanzas.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import ar.com.controlfinanzas.model.Inversion;

public interface CalculadoraProyeccion {

	BigDecimal proyectar(Inversion inversion, LocalDate fechaObjetivo);
}
