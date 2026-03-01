package ar.com.controlfinanzas.model;

import ar.com.controlfinanzas.valuacion.EstrategiaValuacion;

public enum TipoInversion {

	ACCION(TipoActivo.ACCION, EstrategiaValuacion.MERCADO, FrecuenciaIngreso.NINGUNA),
	CRIPTO(TipoActivo.CRIPTO, EstrategiaValuacion.MERCADO, FrecuenciaIngreso.NINGUNA),
	FONDO_COMUN(TipoActivo.FONDO, EstrategiaValuacion.MERCADO, FrecuenciaIngreso.ANUAL),
	PLAZO_FIJO_TRADICIONAL(TipoActivo.EFECTIVO, EstrategiaValuacion.MONTO, FrecuenciaIngreso.MENSUAL),
	PLAZO_FIJO_UVA(TipoActivo.EFECTIVO, EstrategiaValuacion.INDEXADO, FrecuenciaIngreso.AL_VENCIMIENTO);

	private final TipoActivo tipoActivo;
	private final EstrategiaValuacion estrategia;
	private final FrecuenciaIngreso frecuenciaSugerida;

	TipoInversion(TipoActivo tipoActivo, EstrategiaValuacion estrategia, FrecuenciaIngreso frecuenciaSugerida) {
		this.tipoActivo = tipoActivo;
		this.estrategia = estrategia;
		this.frecuenciaSugerida = frecuenciaSugerida;
	}

	public EstrategiaValuacion getEstrategia() {
		return estrategia;
	}

	public boolean permite(TipoActivo activo) {
		return this.tipoActivo == activo;
	}

	public FrecuenciaIngreso frecuenciaSugerida() {
		return frecuenciaSugerida;
	}
}