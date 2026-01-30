package ar.com.controlfinanzas.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResultadoProyeccion {

	private LocalDate fecha;
	private BigDecimal capitalProyectado;

	public ResultadoProyeccion(LocalDate fecha, BigDecimal capitalProyectado) {
		this.fecha = fecha;
		this.capitalProyectado = capitalProyectado;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public BigDecimal getCapitalProyectado() {
		return capitalProyectado;
	}
}
