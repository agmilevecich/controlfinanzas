package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.YearMonth;
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

	public void guardar(Gasto gasto) throws Exception {
		repository.guardar(gasto);
	}

	public void eliminar(Integer id) {
		repository.eliminar(id);
	}

	public BigDecimal calcularTotalGastos() {
		List<Gasto> gastos = repository.listarPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());

		return gastos.stream().map(g -> g.getMonto() != null ? g.getMonto() : BigDecimal.ZERO).reduce(BigDecimal.ZERO,
				BigDecimal::add);
	}

	public BigDecimal calcularTotalHistorico(Integer usuarioId) {
		return repository.listarPorUsuario(usuarioId).stream().map(g -> g.getMonto()).reduce(BigDecimal.ZERO,
				BigDecimal::add);
	}

	public BigDecimal calcularTotalPorMes(Integer usuarioId, YearMonth mes) {
		return repository.listarPorUsuario(usuarioId).stream().filter(g -> YearMonth.from(g.getFecha()).equals(mes))
				.map(g -> g.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
