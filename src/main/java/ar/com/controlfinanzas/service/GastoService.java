package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.repository.GastoRepository;

public class GastoService {

	private final GastoRepository repository;

	public GastoService(GastoRepository repository) {
		this.repository = repository;
	}

	public List<Gasto> listarPorUsuario(Integer usuarioId) {
		return repository.listarPorUsuario(usuarioId);
	}

	public BigDecimal calcularTotalGastos() throws Exception {
		return repository.obtenerTotalPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());
	}

	public void guardar(Gasto gasto) throws Exception {
		repository.guardar(gasto);
	}

	public void eliminar(Integer id) {
		repository.eliminar(id);
	}
}
