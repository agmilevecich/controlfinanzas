package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.model.Posicion;

public class ResumenService {

	public BigDecimal calcularPatrimonio(List<Posicion> posiciones) {
		return posiciones.stream().map(Posicion::getValorActual).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularTotalInvertido(List<Posicion> posiciones) {
		return posiciones.stream().map(Posicion::getCapitalInvertido).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularPnLTotal(List<Posicion> posiciones) {
		return posiciones.stream().map(Posicion::getPnL).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularIngresoMensual(List<Posicion> posiciones) {
		return posiciones.stream().map(p -> p.getIngresoMensual()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
