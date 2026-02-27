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
	private BigDecimal capitalInvertido = BigDecimal.ZERO;
	private BigDecimal valorActual = BigDecimal.ZERO;
	private BigDecimal capitalTotal = BigDecimal.ZERO;

	private final List<Inversion> inversiones = new ArrayList<>();

	public Posicion(String clave, TipoActivo tipoActivo) {
		this.clave = clave;
		this.tipoActivo = tipoActivo;
	}

	// ===============================
	// AGREGA INVERSION
	// ===============================
	public void agregar(Inversion inv) {

		ValuadorInversion valuador = inv.getValuador();

		BigDecimal invertido = inv.getCapitalTotalCalculado();
		BigDecimal actual = valuador.calcularCapitalActual(inv);

		capitalInvertido = capitalInvertido.add(invertido);
		capitalTotal = capitalTotal.add(invertido);
		valorActual = valorActual.add(actual);

		BigDecimal cantidad = inv.getCantidad() != null ? inv.getCantidad() : BigDecimal.ZERO;
		cantidadTotal = cantidadTotal.add(cantidad);

		inversiones.add(inv);
	}

	// ===============================
	// CALCULOS
	// ===============================

	public BigDecimal getPrecioPromedio() {
		if (cantidadTotal.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return capitalInvertido.divide(cantidadTotal, 8, RoundingMode.HALF_UP);
	}

	public BigDecimal getPnL() {
		return valorActual.subtract(capitalInvertido);
	}

	public BigDecimal getPnLPorcentaje() {

		if (capitalInvertido.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		return getPnL().divide(capitalInvertido, 8, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
	}

	public BigDecimal getValorActual() {
		return valorActual;
	}

	public BigDecimal getCapitalInvertido() {
		return capitalInvertido;
	}

	public BigDecimal getIngresoMensual() {

		BigDecimal total = BigDecimal.ZERO;

		for (Inversion inv : inversiones) {

			ValuadorInversion valuador = inv.getValuador();
			BigDecimal ingreso = valuador.calcularIngresoMensual(inv);

			if (ingreso != null) {
				total = total.add(ingreso);
			}
		}

		return total;
	}

	// ===============================
	// GETTERS
	// ===============================

	public String getClave() {
		return clave;
	}

	public TipoActivo getTipoActivo() {
		return tipoActivo;
	}

	public BigDecimal getCantidadTotal() {
		return cantidadTotal;
	}

	public List<Inversion> getInversiones() {
		return inversiones;
	}

	public BigDecimal getCapitalTotal() {
		return capitalTotal;
	}
}