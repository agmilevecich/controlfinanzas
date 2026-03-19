package ar.com.controlfinanzas.controller;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.InversionService;
import ar.com.controlfinanzas.service.MovimientoService;

public class DashboardController {

	private final MovimientoService movimientoService;

	@SuppressWarnings("unused")
	private final InversionService inversionService;

	private List<Movimiento> movimientos;
	private List<Inversion> inversiones;

	private BigDecimal totalGastos = BigDecimal.ZERO;
	private BigDecimal capitalTotalInvertido = BigDecimal.ZERO;
	private BigDecimal patrimonioNeto = BigDecimal.ZERO;
	private Usuario usuario;

	public DashboardController(MovimientoService movimientoService, InversionService inversionService) {
		this.movimientoService = movimientoService;
		this.inversionService = inversionService;
		this.usuario = usuario;
	}

	public void refrescarDatos() throws Exception {

		movimientos = movimientoService.listarPorUsuario();
//		inversiones = inversionService.listarPorUsuario(usuarioId);

		totalGastos = movimientoService.calcularTotalGastos();
//		capitalTotalInvertido = inversionService.calcularCapitalTotal(inversiones);

		patrimonioNeto = capitalTotalInvertido.subtract(totalGastos);
	}

	public List<Movimiento> getMovimientos() {
		return movimientos;
	}

	public List<Inversion> getInversiones() {
		return inversiones;
	}

	public BigDecimal getTotalGastos() {
		return totalGastos;
	}

	public BigDecimal getCapitalTotalInvertido() {
		return capitalTotalInvertido;
	}

	public BigDecimal getPatrimonioNeto() {
		return patrimonioNeto;
	}
}
