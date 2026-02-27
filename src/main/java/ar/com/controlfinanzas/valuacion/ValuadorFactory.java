package ar.com.controlfinanzas.valuacion;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.model.TipoInversion;

public class ValuadorFactory {

	public static ValuadorInversion crear(Inversion inv) {

		// Mercado (precio dinámico)
		if (inv.getTipoActivo() == TipoActivo.ACCION || inv.getTipoActivo() == TipoActivo.CRIPTO
				|| inv.getTipoActivo() == TipoActivo.FONDO) {

			return new ValuadorMercado();
		}

		// Indexado (UVA)
		if (inv.getTipoInversion() == TipoInversion.PLAZO_FIJO_UVA) {
			return new ValuadorIndexado();
		}

		// Default → monto / tasa
		return new ValuadorMonto();
	}
}