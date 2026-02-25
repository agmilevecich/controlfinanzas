package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.valuacion.ValuadorInversion;

public class Posicion {

	private String clave;
	private TipoActivo tipoActivo;

	private BigDecimal cantidadTotal = BigDecimal.ZERO;
	private BigDecimal capitalTotal = BigDecimal.ZERO;

	private final List<Inversion> inversiones = new ArrayList<>();

	public Posicion(String clave, TipoActivo tipoActivo) {
		this.clave = clave;
		this.tipoActivo = tipoActivo;
	}

	// Agrega una inversión y actualiza totales usando el valuador correcto
	public void agregar(Inversion inv) {
		ValuadorInversion valuador = inv.getValuador();

		BigDecimal cantidad = inv.getCantidad() != null ? inv.getCantidad() : BigDecimal.ZERO;
		BigDecimal capital = valuador.calcularCapitalActual(inv);

		cantidadTotal = cantidadTotal.add(cantidad);
		capitalTotal = capitalTotal.add(capital);

		inversiones.add(inv);
	}

	public BigDecimal getPrecioPromedio() {
		if (cantidadTotal.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return capitalTotal.divide(cantidadTotal, 8, RoundingMode.HALF_UP);
	}

	// Getters
	public String getClave() {
		return clave;
	}

	public TipoActivo getTipoActivo() {
		return tipoActivo;
	}

	public BigDecimal getCantidadTotal() {
		return cantidadTotal;
	}

	public BigDecimal getCapitalTotal() {
		return capitalTotal;
	}

	// Getter de inversiones para PanelIngresoMensuales y PanelVencimiento
	public List<Inversion> getInversiones() {
		return inversiones;
	}
}