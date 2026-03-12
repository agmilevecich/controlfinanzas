package ar.com.controlfinanzas.model;

import java.math.BigDecimal;

public class ResumenCompra {

	private String compraId;
	private String descripcion;

	private BigDecimal totalCompra;
	private BigDecimal montoCuota;

	private int totalCuotas;
	private int cuotasPagadas;
	private int cuotasPendientes;

	public ResumenCompra() {
	}

	public String getCompraId() {
		return compraId;
	}

	public void setCompraId(String compraId) {
		this.compraId = compraId;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getTotalCompra() {
		return totalCompra;
	}

	public void setTotalCompra(BigDecimal totalCompra) {
		this.totalCompra = totalCompra;
	}

	public BigDecimal getMontoCuota() {
		return montoCuota;
	}

	public void setMontoCuota(BigDecimal montoCuota) {
		this.montoCuota = montoCuota;
	}

	public int getTotalCuotas() {
		return totalCuotas;
	}

	public void setTotalCuotas(int totalCuotas) {
		this.totalCuotas = totalCuotas;
	}

	public int getCuotasPagadas() {
		return cuotasPagadas;
	}

	public void setCuotasPagadas(int cuotasPagadas) {
		this.cuotasPagadas = cuotasPagadas;
	}

	public int getCuotasPendientes() {
		return cuotasPendientes;
	}

	public void setCuotasPendientes(int cuotasPendientes) {
		this.cuotasPendientes = cuotasPendientes;
	}
}
