package ar.com.controlfinanzas.alerts.generator;

import java.util.List;

import ar.com.controlfinanzas.model.Alerta;

public interface GeneradorAlertas {
	List<Alerta> generar();
}