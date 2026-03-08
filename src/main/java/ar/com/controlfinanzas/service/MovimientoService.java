package ar.com.controlfinanzas.service;

import java.util.List;

import javax.swing.JOptionPane;

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

		try {

			movimiento.validar();

			movimiento.setCuenta(cuenta); // SIEMPRE se asigna

			em.getTransaction().begin();

			em.persist(movimiento);

			if (movimiento.getFormaPago() != FormaPago.CREDITO) {
				cuenta.getMovimientos().add(movimiento);
			} else {
				movimiento.setPendiente(true); // deuda de tarjeta
			}

			em.getTransaction().commit();

		} catch (Exception e) {

			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}

			JOptionPane.showMessageDialog(null, e.getMessage());
		}

		return movimiento;
	}

	public List<Movimiento> getMovimientosCuenta(Cuenta cuenta) {

		TypedQuery<Movimiento> query = em.createQuery(
				"SELECT m FROM Movimiento m WHERE m.cuenta = :cuenta ORDER BY m.fecha DESC", Movimiento.class);

		query.setParameter("cuenta", cuenta);

		return query.getResultList();
	}

}