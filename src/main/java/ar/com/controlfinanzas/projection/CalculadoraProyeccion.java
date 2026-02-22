package ar.com.controlfinanzas.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Cuenta;

public interface CalculadoraProyeccion {
	BigDecimal proyectar(Inversion inversion, LocalDate fechaObjetivo);

	BigDecimal proyectar(Cuenta cuenta, LocalDate fechaObjetivo); // <-- agregado
}
