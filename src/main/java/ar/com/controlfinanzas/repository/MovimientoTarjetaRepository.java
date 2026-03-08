package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.MovimientoTarjeta;
import jakarta.persistence.EntityManager;

public class MovimientoTarjetaRepository {

	private EntityManager em;

	public MovimientoTarjetaRepository(EntityManager em) {
		this.em = em;
	}

	public void guardar(MovimientoTarjeta movimiento) {

		em.getTransaction().begin();
		em.persist(movimiento);
		em.getTransaction().commit();
	}

	public List<MovimientoTarjeta> findByTarjetaId(Long tarjetaId) {

		return em.createQuery("SELECT m FROM MovimientoTarjeta m WHERE m.tarjeta.id = :tarjetaId",
				MovimientoTarjeta.class).setParameter("tarjetaId", tarjetaId).getResultList();
	}
}