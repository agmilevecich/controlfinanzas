package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.TarjetaCredito;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class TarjetaCreditoRepository {

	public void guardar(EntityManager em, TarjetaCredito tarjeta) {
		em.persist(tarjeta);
	}

	public List<TarjetaCredito> buscarPorUsuario(EntityManager em, Integer cuenusuarioId) {
		TypedQuery<TarjetaCredito> query = em.createQuery(
				"SELECT DISTINCT u FROM Usuario JOIN FETCH u.cuentas JOIN FETCH c.tarjetas WHERE u.id = :usuarioId",
				TarjetaCredito.class);
		query.setParameter("usuarioId", cuenusuarioId);
		return query.getResultList();
	}

	public TarjetaCredito buscarPorId(EntityManager em, Long id) {
		return em.find(TarjetaCredito.class, id);
	}
}