package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.Gasto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class GastoRepository {

	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("controlFinanzasPU");

	public List<Gasto> listarTodos() {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery("SELECT g FROM Gasto g", Gasto.class).getResultList();
		} finally {
			em.close();
		}
	}

	public void guardar(Gasto gasto) {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(gasto);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public void eliminar(Long id) {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Gasto gasto = em.find(Gasto.class, id);
			if (gasto != null) {
				em.remove(gasto);
			}
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}
}
