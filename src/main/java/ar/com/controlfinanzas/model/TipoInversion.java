package ar.com.controlfinanzas.model;

import java.util.Set;

public enum TipoInversion {

	// Bancarias
	PLAZO_FIJO_TRADICIONAL(TipoActivo.EFECTIVO), PLAZO_FIJO_UVA(TipoActivo.EFECTIVO),

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
}