package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class MovimientoService {

	private EntityManager em;

	public MovimientoService(EntityManager em) {
		this.em = em;
	}

	public Movimiento registrarMovimiento(Cuenta cuenta, Movimiento movimiento) {
		movimiento.setCuenta(cuenta);

		em.getTransaction().begin();
		em.persist(movimiento);
		em.getTransaction().commit();

		return movimiento;
	}

	public List<Movimiento> getMovimientosCuenta(Cuenta cuenta) {
		TypedQuery<Movimiento> query = em.createQuery(
				"SELECT m FROM Movimiento m WHERE m.cuenta = :cuenta ORDER BY m.fecha DESC", Movimiento.class);
		query.setParameter("cuenta", cuenta);
		return query.getResultList();
	}

	// ================================
	// Método que faltaba
	// ================================
	public BigDecimal calcularSaldo(Cuenta cuenta) {
		List<Movimiento> movimientos = getMovimientosCuenta(cuenta);
		BigDecimal saldo = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {
			switch (m.getTipo()) {
			case INGRESO -> saldo = saldo.add(m.getMonto());
			case GASTO, TRANSFERENCIA -> saldo = saldo.subtract(m.getMonto());
			}
		}

		return saldo;
	}

}