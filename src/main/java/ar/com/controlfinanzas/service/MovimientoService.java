package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
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
		cuenta.getMovimientos().add(movimiento);

		return movimiento;
	}

	public List<Movimiento> getMovimientosCuenta(Cuenta cuenta) {

		TypedQuery<Movimiento> query = em.createQuery(
				"SELECT m FROM Movimiento m WHERE m.cuenta = :cuenta ORDER BY m.fecha DESC", Movimiento.class);

		query.setParameter("cuenta", cuenta);

		return query.getResultList();
	}

	public BigDecimal calcularDeudaTarjeta(Long usuarioId) {

		TypedQuery<Movimiento> query = em
				.createQuery("SELECT m FROM Movimiento m WHERE m.cuenta.usuario.id = :usuarioId", Movimiento.class);

		query.setParameter("usuarioId", usuarioId);

		List<Movimiento> movimientos = query.getResultList();

		BigDecimal deuda = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			if (m.getFormaPago() == FormaPago.CREDITO && m.getTipo() == TipoMovimiento.GASTO) {

				deuda = deuda.add(m.getMonto());
			}
		}

		return deuda;
	}
}