package ar.com.controlfinanzas.service;

import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class UsuarioService {

	private EntityManager em;

	public UsuarioService(EntityManager em) {
		this.em = em;
	}

	public Usuario buscarOCrearUsuario(String nombre) {
		TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class);
		query.setParameter("nombre", nombre);

		Usuario usuario;
		try {
			usuario = query.getSingleResult();
		} catch (Exception e) {
			usuario = new Usuario(nombre);
			em.getTransaction().begin();
			em.persist(usuario);
			em.getTransaction().commit();
		}
		return usuario;
	}
}