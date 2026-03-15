package ar.com.controlfinanzas.model;

import java.math.BigDecimal;

public class ResumenTarjeta {

	private BigDecimal deudaMes;
	private BigDecimal deudaTotal;
	private BigDecimal limiteDisponible;
	private BigDecimal saldoFavor;

	public BigDecimal getDeudaMes() {
		return deudaMes;
	}

	public void setDeudaMes(BigDecimal deudaMes) {
		this.deudaMes = deudaMes;
	}

	public BigDecimal getDeudaTotal() {
		return deudaTotal;
	}

	public void setDeudaTotal(BigDecimal deudaTotal) {
		this.deudaTotal = deudaTotal;
	}

	public BigDecimal getLimiteDisponible() {
		return limiteDisponible;
	}

	public void setLimiteDisponible(BigDecimal limiteDisponible) {
		this.limiteDisponible = limiteDisponible;
	}

	public BigDecimal getSaldoFavor() {
		return saldoFavor;
	}

	public void setSaldoFavor(BigDecimal saldoFavor) {
		this.saldoFavor = saldoFavor;
	}
}
