package ar.com.controlfinanzas.controller;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.InversionService;

public class DashboardController {

	private final GastoService gastoService;
	private final InversionService inversionService;

	private List<Gasto> gastos;
	private List<Inversion> inversiones;

	private BigDecimal totalGastos = BigDecimal.ZERO;
	private BigDecimal capitalTotalInvertido = BigDecimal.ZERO;
	private BigDecimal patrimonioNeto = BigDecimal.ZERO;

	public DashboardController(GastoService gastoService, InversionService inversionService) {
		this.gastoService = gastoService;
		this.inversionService = inversionService;
	}

	public void refrescarDatos() throws Exception {

		Integer usuarioId = MainApp.getUsuarioActivo().getUsuarioID();

		gastos = gastoService.listarPorUsuario(usuarioId);
//		inversiones = inversionService.listarPorUsuario(usuarioId);

		totalGastos = gastoService.calcularTotalGastos();
//		capitalTotalInvertido = inversionService.calcularCapitalTotal(inversiones);

		patrimonioNeto = capitalTotalInvertido.subtract(totalGastos);
	}

	public List<Gasto> getGastos() {
		return gastos;
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
