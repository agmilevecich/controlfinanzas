package ar.com.controlfinanzas.domain.inversion.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class InversionFinancieraService {

	public BigDecimal calcularIngresoMensualTotal(List<Inversion> inversiones) {

		if (inversiones == null || inversiones.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = BigDecimal.ZERO;

		for (Inversion inv : inversiones) {
			if (inv.esGeneradoraDeIngresos()) {
				total = total.add(inv.calcularIngresoMensual());
			}
		}

		return total;
	}

	public BigDecimal calcularCapitalTotal(List<Inversion> inversiones) {

		if (inversiones == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = BigDecimal.ZERO;

		for (Inversion inv : inversiones) {
			total = total.add(inv.getCapitalTotalCalculado());
		}

		return total;
	}

	public BigDecimal calcularCapitalProyectadoTotal(List<Inversion> inversiones) {

		if (inversiones == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = BigDecimal.ZERO;

		for (Inversion inv : inversiones) {
			total = total.add(inv.calcularCapitalProyectado());
		}

		return total;
	}
}