package ar.com.controlfinanzas.alerts.generator;

import java.util.List;

import ar.com.controlfinanzas.alerts.AlertaManager;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Cuenta;

public class AlertasCuentas implements GeneradorAlertas {

    private final AlertaManager manager;
    private final List<Cuenta> cuentas;

    public AlertasCuentas(AlertaManager manager, List<Cuenta> cuentas) {
        this.manager = manager;
        this.cuentas = cuentas;
    }

    @Override
    public List<Alerta> generar() {
        return manager.generarAlertasCuentas(cuentas);
    }
}