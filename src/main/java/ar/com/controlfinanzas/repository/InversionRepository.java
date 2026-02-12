package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.Inversion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class InversionRepository {

	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("controlFinanzasPU");

	public Inversion guardar(Inversion inversion) {
		EntityManager em = emf.createEntityManager();
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

	public List<Inversion> listar() {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery("SELECT i FROM Inversion i", Inversion.class).getResultList();
		} finally {
			em.close();
		}
	}
}
