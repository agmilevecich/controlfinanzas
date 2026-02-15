package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.repository.GastoRepository;

public class GastoService {

	private final GastoRepository repository;

	public GastoService(GastoRepository repository) {
		this.repository = repository;
	}

	public List<Gasto> obtenerTodos() throws Exception {
		return repository.listarTodos();
	}

	public BigDecimal calcularTotalGastos() throws Exception {
		BigDecimal total = BigDecimal.ZERO;

		for (Gasto g : repository.listarTodos()) {
			total = total.add(g.getMonto());
		}

		return total;
	}

	public void guardar(Gasto gasto) throws Exception {
		repository.guardar(gasto);
	}

	public void eliminar(Long id) {
		repository.eliminar(id);
	}
}
