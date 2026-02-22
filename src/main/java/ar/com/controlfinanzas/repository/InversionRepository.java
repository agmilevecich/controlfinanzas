package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class InversionRepository {

	public Inversion guardar(Inversion inversion) {
		EntityManager em = JPAUtil.getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(inversion);
			em.getTransaction().commit();
			return inversion;
		} catch (Exception e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	public List<Inversion> listarPorUsuario(Integer usuarioId) {
		EntityManager em = JPAUtil.getEntityManager();

		try {
			return em.createQuery("SELECT i FROM Inversion i WHERE i.usuario.usuarioID = :usuarioId", Inversion.class)
					.setParameter("usuarioId", usuarioId).getResultList();

		} finally {
			em.close();
		}
	}

	public void eliminar(Long id) {
		EntityManager em = JPAUtil.getEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();
			Inversion inversion = em.find(Inversion.class, id);
			if (inversion != null) {
				em.remove(inversion);
			}
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

}
