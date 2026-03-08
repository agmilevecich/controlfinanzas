package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.EntityManager;

public class UsuarioRepository {

	private EntityManager em;

	public UsuarioRepository(EntityManager em) {
		this.em = em;
	}

	public void guardar(Usuario usuario) {

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
		}
	}

	public Usuario buscarPorId(Integer id) {
		return em.find(Usuario.class, id);

	}

	public List<Usuario> listarTodos() {
		return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();

	}

	public void eliminar(Usuario usuario) {

		try {
			em.getTransaction().begin();

			Usuario managed = em.merge(usuario);
			em.remove(managed);

			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}
	}
}
