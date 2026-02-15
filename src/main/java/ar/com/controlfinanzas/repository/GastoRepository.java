package ar.com.controlfinanzas.repository;

import java.util.List;

import ar.com.controlfinanzas.dao.GastoDAO;
import ar.com.controlfinanzas.model.Gasto;

public class GastoRepository {

	private final GastoDAO gastoDAO = new GastoDAO();

	public List<Gasto> listarTodos() throws Exception {
		return gastoDAO.listarGastos();
	}

	public void guardar(Gasto gasto) throws Exception {
		gastoDAO.guardarGasto(gasto);
	}

	public void eliminar(Long id) {
		gastoDAO.eliminar(id);
	}
}
