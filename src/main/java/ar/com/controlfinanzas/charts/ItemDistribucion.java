package ar.com.controlfinanzas.charts;

import java.math.BigDecimal;

public class ItemDistribucion {

	private String categoria;
	private BigDecimal valor;

	public ItemDistribucion(String categoria, BigDecimal valor) {
		this.categoria = categoria;
		this.valor = valor;
	}

	public String getCategoria() {
		return categoria;
	}

	public BigDecimal getValor() {
		return valor;
	}
}
