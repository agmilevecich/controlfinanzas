package ar.com.controlfinanzas.service;

import java.util.List;

import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.repository.TarjetaCreditoRepository;
import jakarta.persistence.EntityManager;

public class TarjetaCreditoService {

	private final TarjetaCreditoRepository repo;
	private final EntityManager em;

	public TarjetaCreditoService(EntityManager em) {
		this.em = em;
		this.repo = new TarjetaCreditoRepository();
	}

	public TarjetaCredito guardar(TarjetaCredito tarjeta) {
		em.getTransaction().begin();
		repo.guardar(em, tarjeta);
		em.getTransaction().commit();
		return tarjeta;
	}

	public List<TarjetaCredito> getTarjetasUsuario(Integer usuarioId) {
		return repo.buscarPorUsuario(em, usuarioId);
	}

	public TarjetaCredito buscarPorId(Long id) {
		return repo.buscarPorId(em, id);
	}
}