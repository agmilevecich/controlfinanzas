package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.Usuario;
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

	public BigDecimal calcularTotalGastosHistoricos(Usuario usuario) {
		return repository.obtenerTotalPorUsuario(usuario.getUsuarioID());
	}

	public BigDecimal calcularTotalPorCategoria(Integer usuarioId, CategoriaGasto categoria) {

		List<Gasto> gastos = repository.listarPorUsuario(usuarioId);

		BigDecimal total = BigDecimal.ZERO;

		for (Gasto g : gastos) {

			if (g.getCategoria() == categoria && g.getMonto() != null) {
				total = total.add(g.getMonto());
			}
		}

		return total;
	}

	public BigDecimal calcularTotalPorCategoriaYMes(Integer usuarioId, CategoriaGasto categoria, YearMonth mes) {

		List<Gasto> gastos = repository.listarPorUsuario(usuarioId);

		BigDecimal total = BigDecimal.ZERO;

		for (Gasto g : gastos) {

			if (g.getCategoria() == categoria && g.getFecha() != null && YearMonth.from(g.getFecha()).equals(mes)
					&& g.getMonto() != null) {

				total = total.add(g.getMonto());
			}
		}

		return total;
	}

	public LinkedHashMap<CategoriaGasto, BigDecimal> rankingCategoriasPorMes(Integer usuarioId, YearMonth mes) {

		List<Gasto> gastos = repository.listarPorUsuario(usuarioId);

		Map<CategoriaGasto, BigDecimal> acumulado = new HashMap<CategoriaGasto, BigDecimal>();

		for (Gasto g : gastos) {
			if (g.getFecha() != null && YearMonth.from(g.getFecha()).equals(mes) && g.getMonto() != null) {
				acumulado.merge(g.getCategoria(), g.getMonto(), BigDecimal::add);
			}
		}

		return acumulado.entrySet().stream().sorted(Map.Entry.<CategoriaGasto, BigDecimal>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
	}

	public Map<CategoriaGasto, BigDecimal> obtenerTotalesPorPeriodo(Integer usuarioId, YearMonth periodo) {
		LocalDate inicio = periodo.atDay(1);
		LocalDate fin = periodo.atEndOfMonth();

		List<Gasto> gastos = repository.listarPorUsuarioYPeriodo(usuarioId, inicio, fin);

		Map<CategoriaGasto, BigDecimal> totales = new HashMap<>();

		for (Gasto g : gastos) {
			totales.merge(g.getCategoria(), g.getMonto(), BigDecimal::add);
		}

		return totales;

	}

	public BigDecimal obtenerTotalDelMes(Integer usuarioId, YearMonth periodo) {
		LocalDate inicio = periodo.atDay(1);
		LocalDate fin = periodo.atEndOfMonth();

		List<Gasto> gastos = repository.listarPorUsuarioYPeriodo(usuarioId, inicio, fin);

		return gastos.stream().map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

	}

}
