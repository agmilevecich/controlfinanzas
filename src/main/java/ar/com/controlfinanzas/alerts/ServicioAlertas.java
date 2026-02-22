package ar.com.controlfinanzas.alerts;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.EstadoInversion;
import ar.com.controlfinanzas.util.FechaUtils;

public class ServicioAlertas {

	private final ConfiguracionAlertas config;

	public ServicioAlertas(ConfiguracionAlertas config) {
		this.config = config;
	}

	public ResultadoAlertaInversion evaluar(Inversion inversion) {

		if (!inversion.tieneVencimiento()) {
			return new ResultadoAlertaInversion(inversion, EstadoInversion.VIGENTE, Long.MAX_VALUE);
		}

		long diasRestantes = FechaUtils.diasHasta(inversion.getFechaVencimiento());

		if (diasRestantes < 0) {
			return new ResultadoAlertaInversion(inversion, EstadoInversion.VENCIDA, diasRestantes);
		}

		if (diasRestantes <= config.getDiasAlerta3()) {
			return new ResultadoAlertaInversion(inversion, EstadoInversion.PROXIMA_A_VENCER, diasRestantes);
		}

		if (diasRestantes <= config.getDiasAlerta1()) {
			return new ResultadoAlertaInversion(inversion, EstadoInversion.VIGENTE, diasRestantes);
		}

		return new ResultadoAlertaInversion(inversion, EstadoInversion.VIGENTE, diasRestantes);
	}
}
