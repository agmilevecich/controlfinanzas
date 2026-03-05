package ar.com.controlfinanzas.repository;

import ar.com.controlfinanzas.model.Movimiento;
import jakarta.persistence.EntityManager;

public class MovimientoRepository {

	public void guardar(EntityManager em, Movimiento movimiento) {
		em.persist(movimiento);
	}
}