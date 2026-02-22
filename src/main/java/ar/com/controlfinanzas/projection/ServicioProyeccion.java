package ar.com.controlfinanzas.projection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class ServicioProyeccion {

	private final CalculadoraProyeccion calculadora;

	public ServicioProyeccion(CalculadoraProyeccion calculadora) {
		this.calculadora = calculadora;
	}

	public List<ResultadoProyeccion> proyectarInversion(Inversion inversion, List<LocalDate> fechas) {

		List<ResultadoProyeccion> resultados = new ArrayList<>();

		for (LocalDate fecha : fechas) {
			resultados.add(new ResultadoProyeccion(fecha, calculadora.proyectar(inversion, fecha)));
		}
		return resultados;
	}
}
