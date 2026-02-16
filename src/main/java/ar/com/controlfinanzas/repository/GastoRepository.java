package ar.com.controlfinanzas.repository;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.persistence.JPAUtil;
import jakarta.persistence.EntityManager;

public class GastoRepository {

	public List<Gasto> listarPorUsuario(Integer usuarioId) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			return em.createQuery("SELECT g FROM Gasto g WHERE g.usuario.usuarioID = :usuarioId", Gasto.class)
					.setParameter("usuarioId", usuarioId).getResultList();

		} finally {
			em.close();
		}
	}

	public BigDecimal obtenerTotalPorUsuario(Integer usuarioId) {

		EntityManager em = JPAUtil.getEntityManager();

		try {
			return em
					.createQuery("SELECT COALESCE(SUM(g.monto), 0) " + "FROM Gasto g "
							+ "WHERE g.usuario.usuarioID = :usuarioId", BigDecimal.class)
					.setParameter("usuarioId", usuarioId).getSingleResult();

		} finally {
			em.close();
		}
	}

	public void guardar(Gasto gasto) {
		EntityManager em = JPAUtil.getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(gasto);
			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;

		} finally {
			em.close();
		}
	}

	public void eliminar(Integer id) {
		EntityManager em = JPAUtil.getEntityManager();
		try {
			em.getTransaction().begin();

			Gasto gasto = em.find(Gasto.class, id);
			if (gasto != null) {
				em.remove(gasto);
			}

			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

}
