package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.persistence.JPAUtil;
import jakarta.persistence.EntityManager;

public class UsuarioRepository {

	public void guardar(Usuario usuario) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			em.getTransaction().begin();

			if (usuario.getUsuarioID() == null) {
				em.persist(usuario);
			} else {
				em.merge(usuario);
			}

			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	public Usuario buscarPorId(Integer id) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			return em.find(Usuario.class, id);
		} finally {
			em.close();
		}
	}

	public List<Usuario> listarTodos() {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
		} finally {
			em.close();
		}
	}

	public void eliminar(Usuario usuario) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			em.getTransaction().begin();

			Usuario managed = em.merge(usuario);
			em.remove(managed);

			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}
}
