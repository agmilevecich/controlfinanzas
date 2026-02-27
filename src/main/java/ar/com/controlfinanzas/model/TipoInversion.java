package ar.com.controlfinanzas.model;

import java.util.Set;

public enum TipoInversion {

	// Bancarias
	PLAZO_FIJO_TRADICIONAL(TipoActivo.EFECTIVO), PLAZO_FIJO_UVA(TipoActivo.UVA),

	// Mercado
	COMPRA_DIRECTA(TipoActivo.ACCION, TipoActivo.BONO, TipoActivo.CRIPTO), OBLIGACION_NEGOCIABLE(TipoActivo.BONO),
	FONDO_COMUN(TipoActivo.FONDO),

	// Cripto
	STAKING(TipoActivo.CRIPTO), LENDING(TipoActivo.CRIPTO);

	private final Set<TipoActivo> activosPermitidos;

	TipoInversion(TipoActivo... activos) {
		this.activosPermitidos = Set.of(activos);
	}

	public boolean permite(TipoActivo activo) {
		return activosPermitidos.contains(activo);
	}

	public Set<TipoActivo> getActivosPermitidos() {
		return activosPermitidos;
	}

	public FrecuenciaIngreso frecuenciaSugerida() {
		switch (this) {

		case LENDING:
			return FrecuenciaIngreso.MENSUAL;

		case STAKING:
			return FrecuenciaIngreso.DIARIO;

		case PLAZO_FIJO_TRADICIONAL, PLAZO_FIJO_UVA:
			return FrecuenciaIngreso.AL_VENCIMIENTO;

		case OBLIGACION_NEGOCIABLE:
			return FrecuenciaIngreso.TRIMESTRAL; // típico cupon

		case COMPRA_DIRECTA, FONDO_COMUN:
			return FrecuenciaIngreso.ANUAL;

		default:
			return FrecuenciaIngreso.MENSUAL;
		}
	}

	public boolean usaCantidadPrecio() {
		return activosPermitidos.stream().anyMatch(activo -> activo != TipoActivo.EFECTIVO);
	}

}