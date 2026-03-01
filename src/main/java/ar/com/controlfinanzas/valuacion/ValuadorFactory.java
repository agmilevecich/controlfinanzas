package ar.com.controlfinanzas.valuacion;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class ValuadorFactory {

	public static ValuadorInversion crear(Inversion inv) {

		EstrategiaValuacion estrategia = inv.getTipoInversion().getEstrategia();

		switch (estrategia) {
		case MERCADO:
			return new ValuadorMercado();
		case INDEXADO:
			return new ValuadorIndexado();
		case MONTO:
			return new ValuadorMonto();
		default:
			throw new IllegalArgumentException("Estrategia no soportada");
		}
	}
}