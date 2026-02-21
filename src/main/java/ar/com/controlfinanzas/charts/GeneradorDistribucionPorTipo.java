package ar.com.controlfinanzas.charts;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.TipoInversion;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorDistribucionPorTipo {

    public List<ItemDistribucion> generar(List<Inversion> inversiones) {

        Map<TipoInversion, BigDecimal> acumulado = new HashMap<>();

        for (Inversion inv : inversiones) {
            acumulado.merge(
                    inv.getTipoInversion(),
                    inv.getCapitalInicial(),
                    BigDecimal::add
            );
        }

        return acumulado.entrySet().stream()
                .map(e ->
                        new ItemDistribucion(
                                e.getKey().name(),
                                e.getValue()
                        )
                )
                .toList();
    }
}
