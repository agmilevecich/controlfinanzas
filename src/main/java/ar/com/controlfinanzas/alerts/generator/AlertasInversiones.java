package ar.com.controlfinanzas.alerts.generator;

import java.util.List;

import ar.com.controlfinanzas.alerts.AlertaManager;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;

public class AlertasInversiones implements GeneradorAlertas {

    private final AlertaManager manager;
    private final List<Inversion> inversiones;

    public AlertasInversiones(AlertaManager manager, List<Inversion> inversiones) {
        this.manager = manager;
        this.inversiones = inversiones;
    }

    @Override
    public List<Alerta> generar() {
        return manager.generarAlertasInversiones(inversiones);
    }
}