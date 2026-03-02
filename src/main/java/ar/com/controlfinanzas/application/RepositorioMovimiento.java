package ar.com.controlfinanzas.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ar.com.controlfinanzas.domain.finanzas.Movimiento;

public class RepositorioMovimiento {

	private List<Movimiento> movimientos = new ArrayList<>();

	public void guardar(Movimiento movimiento) {
		movimientos.add(movimiento);
	}

	public List<Movimiento> listarTodos() {
		return new ArrayList<>(movimientos);
	}

	public List<Movimiento> buscarPorCuenta(UUID cuentaId) {
		return movimientos.stream()
				.filter(m -> (m.getCuentaOrigen() != null && m.getCuentaOrigen().getId().equals(cuentaId))
						|| (m.getCuentaDestino() != null && m.getCuentaDestino().getId().equals(cuentaId)))
				.collect(Collectors.toList());
	}
}