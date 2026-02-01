package ar.com.controlfinanzas.charts;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.controlfinanzas.model.Inversion;

public class GeneradorVencimientosPorPeriodo {

	public List<ItemVencimiento> generar(List<Inversion> inversiones) {

		Map<YearMonth, Integer> conteo = new HashMap<>();

		for (Inversion inv : inversiones) {
			if (inv.tieneVencimiento()) {
				YearMonth periodo = YearMonth.from(inv.getFechaVencimiento());

				conteo.merge(periodo, 1, Integer::sum);
			}
		}

		return conteo.entrySet().stream().map(e -> new ItemVencimiento(e.getKey(), e.getValue())).toList();
	}
}
