package ar.com.controlfinanzas.alerts.generator;

import java.util.List;

import ar.com.controlfinanzas.alerts.AlertaManager;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.MovimientoService;

public class AlertasGastos implements GeneradorAlertas {

    private final AlertaManager manager;
    private final MovimientoService movimientoService;
    private final Usuario usuario;

    public AlertasGastos(AlertaManager manager, MovimientoService movimientoService, Usuario usuario) {
        this.manager = manager;
        this.movimientoService = movimientoService;
        this.usuario = usuario;
    }

    @Override
    public List<Alerta> generar() {
        return manager.generarAlertaGastoMensual(movimientoService, usuario);
    }
}