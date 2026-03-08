package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import ar.com.controlfinanzas.model.CategoriaIngreso;
import ar.com.controlfinanzas.model.Ingreso;
import ar.com.controlfinanzas.repository.IngresoRepository;
import jakarta.persistence.EntityManager;

public class IngresoService {

	private final IngresoRepository repository;

	public IngresoService(EntityManager em) {
		repository = new IngresoRepository(em);
	}

	public BigDecimal calcularTotalHistorico(Integer usuarioId) {

		List<Ingreso> ingresos = repository.listarPorUsuario(usuarioId);

		BigDecimal total = BigDecimal.ZERO;

		for (Ingreso i : ingresos) {
			if (i.getMonto() != null) {
				total = total.add(i.getMonto());
			}
		}

		return total;
	}

	public BigDecimal calcularTotalPorMes(Integer usuarioId, YearMonth mes) {

		List<Ingreso> ingresos = repository.listarPorUsuario(usuarioId);

		BigDecimal total = BigDecimal.ZERO;

		for (Ingreso i : ingresos) {
			if (i.getFecha() != null && YearMonth.from(i.getFecha()).equals(mes) && i.getMonto() != null) {

				total = total.add(i.getMonto());
			}
		}

		return total;
	}

	public BigDecimal calcularTotalPorCategoria(Integer usuarioId, CategoriaIngreso categoria) {

		List<Ingreso> ingresos = repository.listarPorUsuario(usuarioId);

		BigDecimal total = BigDecimal.ZERO;

		for (Ingreso i : ingresos) {

			if (i.getCategoria() == categoria && i.getMonto() != null) {
				total = total.add(i.getMonto());
			}
		}

		return total;
	}

}
