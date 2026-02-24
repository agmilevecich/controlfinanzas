package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Posicion;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.repository.InversionRepository;

public class PosicionService {

	private String activo;
	private TipoActivo tipo;
	private BigDecimal cantidad;
	private BigDecimal capital;
	private BigDecimal precioPromedio;

	private final InversionRepository inversionRepository;

	public PosicionService() {
		inversionRepository = new InversionRepository();
	}

	public String getActivo() {
		return activo;
	}

	public void setActivo(String activo) {
		this.activo = activo;
	}

	public TipoActivo getTipo() {
		return tipo;
	}

	public void setTipo(TipoActivo tipo) {
		this.tipo = tipo;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getCapital() {
		return capital;
	}

	public void setCapital(BigDecimal capital) {
		this.capital = capital;
	}

	public BigDecimal getPrecioPromedio() {
		return precioPromedio;
	}

	public void setPrecioPromedio(BigDecimal precioPromedio) {
		this.precioPromedio = precioPromedio;
	}

	public List<Posicion> calcular(Integer usuarioId) {

		List<Inversion> inversiones = inversionRepository.listarPorUsuario(usuarioId);

		Map<String, Posicion> mapa = new LinkedHashMap<>();

		for (Inversion inv : inversiones) {

			String clave = inv.getDescripcion();
			TipoActivo tipo = inv.getTipoActivo();

			Posicion pos = mapa.computeIfAbsent(clave, k -> new Posicion(clave, tipo));

			BigDecimal cantidad = inv.getCantidad() != null ? inv.getCantidad() : BigDecimal.ZERO;
			BigDecimal capital = inv.getCapitalTotalCalculado();

			pos.agregar(inv);
		}

		return new ArrayList<>(mapa.values());
	}
}