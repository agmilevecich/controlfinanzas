package ar.com.controlfinanzas.domain.finanzas;

import java.math.BigDecimal;
import java.util.List;

public class CalculadoraSaldo {

	public BigDecimal calcularSaldo(Cuenta cuenta, List<Movimiento> movimientos) {

		BigDecimal saldo = BigDecimal.ZERO;

		for (Movimiento movimiento : movimientos) {

			switch (movimiento.getTipo()) {

			case INGRESO:
				if (cuenta.equals(movimiento.getCuentaDestino())) {
					saldo = saldo.add(movimiento.getMonto());
				}
				break;

			case GASTO:
				if (cuenta.equals(movimiento.getCuentaOrigen())) {
					saldo = saldo.subtract(movimiento.getMonto());
				}
				break;

			case TRANSFERENCIA:
				if (cuenta.equals(movimiento.getCuentaOrigen())) {
					saldo = saldo.subtract(movimiento.getMonto());
				}
				if (cuenta.equals(movimiento.getCuentaDestino())) {
					saldo = saldo.add(movimiento.getMonto());
				}
				break;
			}
		}

		return saldo;
	}
}