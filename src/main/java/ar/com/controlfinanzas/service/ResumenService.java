package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.Posicion;

public class ResumenService {

	private final CurrencyService currencyService;

	public ResumenService() {
		currencyService = new CurrencyService();
	}

	// Patrimonio total: suma de saldos de cuentas + valor actual de posiciones
	public BigDecimal calcularPatrimonio(List<Cuenta> cuentas, List<Posicion> posiciones) {
		BigDecimal total = BigDecimal.ZERO;

		if (cuentas != null) {
			for (Cuenta c : cuentas) {
				BigDecimal saldoEnBase = currencyService.convertirAMonedaBase(
						c.getSaldo() != null ? c.getSaldo() : BigDecimal.ZERO, c.getMoneda());
				total = total.add(saldoEnBase);
			}
		}

		if (posiciones != null) {
			for (Posicion p : posiciones) {
				// asumimos que todas las posiciones ya están en ARS o podés agregar moneda si
				// aplica
				total = total.add(p.getValorActual() != null ? p.getValorActual() : BigDecimal.ZERO);
			}
		}

		return total;
	}

	// Total invertido: suma de capital invertido en las posiciones
	public BigDecimal calcularTotalInvertido(List<Posicion> posiciones) {
		BigDecimal total = BigDecimal.ZERO;

		if (posiciones != null) {
			for (Posicion p : posiciones) {
				total = total.add(p.getCapitalInvertido() != null ? p.getCapitalInvertido() : BigDecimal.ZERO);
			}
		}

		return total;
	}

	// PnL total: suma de PnL de todas las posiciones
	public BigDecimal calcularPnLTotal(List<Posicion> posiciones) {
		BigDecimal pnl = BigDecimal.ZERO;

		if (posiciones != null) {
			for (Posicion p : posiciones) {
				pnl = pnl.add(p.getPnL() != null ? p.getPnL() : BigDecimal.ZERO);
			}
		}

		return pnl;
	}

	// Ingreso mensual: suma de ingresos mensuales de todas las cuentas
	public BigDecimal calcularIngresoMensual(List<Cuenta> cuentas, MovimientoService movimientoService) {
		BigDecimal ingreso = BigDecimal.ZERO;

		if (cuentas != null && movimientoService != null) {
			for (Cuenta c : cuentas) {
				List<Movimiento> movimientos = movimientoService.getMovimientosCuenta(c);
				if (movimientos != null) {
					for (Movimiento m : movimientos) {
						if (m.getTipo() != null && m.getTipo().name().equals("INGRESO")) {
							ingreso = ingreso.add(m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO);
						}
					}
				}
			}
		}

		return ingreso;
	}
}