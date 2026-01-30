package ar.com.controlfinanzas.charts;

import java.time.YearMonth;

public class ItemVencimiento {

	private YearMonth periodo;
	private int cantidad;

	public ItemVencimiento(YearMonth periodo, int cantidad) {
		this.periodo = periodo;
		this.cantidad = cantidad;
	}

	public YearMonth getPeriodo() {
		return periodo;
	}

	public int getCantidad() {
		return cantidad;
	}
}
