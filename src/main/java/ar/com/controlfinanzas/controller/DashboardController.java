package ar.com.controlfinanzas.controller;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.InversionService;

public class DashboardController {

	private final GastoService gastoService;
	@SuppressWarnings("unused")
	private final InversionService inversionService;

	private List<Gasto> gastos;
	private List<Inversion> inversiones;

	private BigDecimal totalGastos = BigDecimal.ZERO;
	private BigDecimal capitalTotalInvertido = BigDecimal.ZERO;
	private BigDecimal patrimonioNeto = BigDecimal.ZERO;
	private Usuario usuario;

	public DashboardController(GastoService gastoService, InversionService inversionService, Usuario usuario) {
		this.gastoService = gastoService;
		this.inversionService = inversionService;
		this.usuario = usuario;
	}

	public void refrescarDatos() throws Exception {

		gastos = gastoService.listarPorUsuario(usuario.getUsuarioID());
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
