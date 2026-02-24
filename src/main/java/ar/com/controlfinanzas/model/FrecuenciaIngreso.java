package ar.com.controlfinanzas.model;

public enum FrecuenciaIngreso {

	DIARIO(365), SEMANAL(52), MENSUAL(12), TRIMESTRAL(4), ANUAL(1), AL_VENCIMIENTO(0);

	private final int periodosPorAnio;

	FrecuenciaIngreso(int periodosPorAnio) {
		this.periodosPorAnio = periodosPorAnio;
	}

	public int periodosPorAnio() {
		return periodosPorAnio;
	}
}