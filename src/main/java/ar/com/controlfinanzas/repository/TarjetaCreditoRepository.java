package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.TarjetaCredito;
import jakarta.persistence.EntityManager;

public class TarjetaCreditoRepository {

	public void guardar(EntityManager em, TarjetaCredito tarjeta) {
		em.persist(tarjeta);
	}

	public List<TarjetaCredito> buscarPorUsuario(EntityManager em, Integer usuarioId) {

		return em.createQuery("SELECT t FROM TarjetaCredito t WHERE t.banco.usuario.id = :usuarioId",
				TarjetaCredito.class).setParameter("usuarioId", usuarioId).getResultList();
	}

	public TarjetaCredito buscarPorId(EntityManager em, Long id) {
		return em.find(TarjetaCredito.class, id);
	}
}