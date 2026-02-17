package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.model.Ingreso;
import ar.com.controlfinanzas.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class IngresoRepository {

	public void guardar(Ingreso ingreso) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			em.getTransaction().begin();
			em.persist(ingreso);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public List<Ingreso> listarPorUsuario(Integer usuarioId) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			TypedQuery<Ingreso> query = em.createQuery("SELECT i FROM Ingreso i WHERE i.usuario.id = :usuarioId",
					Ingreso.class);
			query.setParameter("usuarioId", usuarioId);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
}
