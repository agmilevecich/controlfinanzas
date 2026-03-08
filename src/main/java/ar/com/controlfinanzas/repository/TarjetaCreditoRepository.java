package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.TarjetaCredito;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class TarjetaCreditoRepository {

	public void guardar(EntityManager em, TarjetaCredito tarjeta) {
		em.persist(tarjeta);
	}

	public List<TarjetaCredito> buscarPorUsuario(EntityManager em, Integer usuarioId) {

		TypedQuery<TarjetaCredito> query = em.createQuery(
				"SELECT t FROM TarjetaCredito t WHERE t.cuenta.usuario.id = :usuarioId", TarjetaCredito.class);

		query.setParameter("usuarioId", usuarioId);

		return query.getResultList();
	}

	public TarjetaCredito buscarPorId(EntityManager em, Long id) {
		return em.find(TarjetaCredito.class, id);
	}
}