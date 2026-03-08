package ar.com.controlfinanzas.service;

import java.util.List;

import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class BancoService {

	private EntityManager em;

	public BancoService(EntityManager em) {
		this.em = em;
	}

	public Banco crearBanco(Usuario usuario, String nombre) {

		Banco banco = new Banco();
		banco.setNombre(nombre);
		banco.setUsuario(usuario);

		em.getTransaction().begin();
		em.persist(banco);
		em.getTransaction().commit();

		return banco;
	}

	public List<Banco> getBancosUsuario(Usuario usuario) {

		TypedQuery<Banco> query = em.createQuery("SELECT b FROM Banco b WHERE b.usuario = :usuario ORDER BY b.nombre",
				Banco.class);

		query.setParameter("usuario", usuario);

		return query.getResultList();
	}

	public void eliminarBanco(Banco banco) {

		em.getTransaction().begin();

		Banco bancoDb = em.merge(banco);
		em.remove(bancoDb);

		em.getTransaction().commit();
	}

}
