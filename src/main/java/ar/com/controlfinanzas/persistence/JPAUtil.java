package ar.com.controlfinanzas.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

	private static final String PERSISTENCE_UNIT = "controlFinanzasPU";

	private static final EntityManagerFactory entityManagerFactory = Persistence
			.createEntityManagerFactory(PERSISTENCE_UNIT);

	private JPAUtil() {
	}

	public static EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

	public static void close() {
		if (entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
	}
}
