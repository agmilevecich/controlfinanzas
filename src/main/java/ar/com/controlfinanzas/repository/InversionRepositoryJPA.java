package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.repository.interfaces.InversionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class InversionRepositoryJPA implements InversionRepository {

	private EntityManager em;

	public InversionRepositoryJPA(EntityManager em) {
		this.em = em;
	}

	@Override
	public Inversion guardar(Inversion inversion) {
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
		}
	}

	@Override
	public List<Inversion> listarPorUsuario(Integer usuarioId) {
		return em.createQuery("SELECT i FROM Inversion i WHERE i.usuario.usuarioID = :usuarioId", Inversion.class)
				.setParameter("usuarioId", usuarioId).getResultList();
	}

	@Override
	public void eliminar(Long id) {
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
		}
	}

}
