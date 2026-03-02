package ar.com.controlfinanzas.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import ar.com.controlfinanzas.domain.finanzas.CalculadoraSaldo;
import ar.com.controlfinanzas.domain.finanzas.Cuenta;
import ar.com.controlfinanzas.domain.finanzas.Movimiento;
import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import ar.com.controlfinanzas.model.Moneda;

public class ServicioFinanciero {

	private RepositorioCuenta repositorioCuenta;
	private RepositorioMovimiento repositorioMovimiento;
	private CalculadoraSaldo calculadoraSaldo;

	public ServicioFinanciero(RepositorioCuenta repositorioCuenta, RepositorioMovimiento repositorioMovimiento) {
		this.repositorioCuenta = repositorioCuenta;
		this.repositorioMovimiento = repositorioMovimiento;
		this.calculadoraSaldo = new CalculadoraSaldo();
	}

	public Cuenta crearCuenta(String nombre, TipoCuenta tipo, Moneda moneda) {
		Cuenta cuenta = new Cuenta(nombre, tipo, moneda);
		repositorioCuenta.guardar(cuenta);
		return cuenta;
	}

	public void registrarMovimiento(Movimiento movimiento) {
		repositorioMovimiento.guardar(movimiento);
	}

	public BigDecimal obtenerSaldo(UUID cuentaId) {
		Cuenta cuenta = repositorioCuenta.buscarPorId(cuentaId)
				.orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

		List<Movimiento> movimientos = repositorioMovimiento.buscarPorCuenta(cuentaId);

		return calculadoraSaldo.calcularSaldo(cuenta, movimientos);
	}
}