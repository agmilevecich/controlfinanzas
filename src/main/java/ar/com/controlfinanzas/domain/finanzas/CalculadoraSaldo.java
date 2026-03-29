package ar.com.controlfinanzas.domain.finanzas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.model.Movimiento;

public class CalculadoraSaldo {

	public BigDecimal calcularSaldo(Cuenta cuenta, List<Movimiento> movimientos) {

		BigDecimal saldo = BigDecimal.ZERO;

		for (Movimiento movimiento : movimientos) {

			if (!cuenta.equals(movimiento.getCuenta())) {
				continue;
			}

			switch (movimiento.getTipo()) {

			case INGRESO:
				saldo = saldo.add(movimiento.getMonto());
				break;

			case GASTO:
				saldo = saldo.subtract(movimiento.getMonto());
				break;
			}
		}

		return saldo;
	}
}