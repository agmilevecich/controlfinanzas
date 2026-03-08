package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.Ingreso;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class IngresoRepository {

	private EntityManager em;

	public IngresoRepository(EntityManager em) {
		this.em = em;
	}

	public void guardar(Ingreso ingreso) {
		em.getTransaction().begin();
		em.persist(ingreso);
		em.getTransaction().commit();
	}

	public List<Ingreso> listarPorUsuario(Integer usuarioId) {
		TypedQuery<Ingreso> query = em.createQuery("SELECT i FROM Ingreso i WHERE i.usuario.id = :usuarioId",
				Ingreso.class);
		query.setParameter("usuarioId", usuarioId);
		return query.getResultList();
	}
}
