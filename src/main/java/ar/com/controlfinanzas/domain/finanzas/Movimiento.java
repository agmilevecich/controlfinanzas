package ar.com.controlfinanzas.domain.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Moneda;

public class Movimiento {

	private UUID id;
	private Cuenta cuentaOrigen;
	private Cuenta cuentaDestino;
	private BigDecimal monto;
	private Moneda moneda;
	private TipoMovimiento tipo;
	private LocalDate fecha;
	private FormaPago formaPago;
	private String descripcion;

	private Movimiento(Cuenta cuentaOrigen, Cuenta cuentaDestino, BigDecimal monto, Moneda moneda, TipoMovimiento tipo,
			LocalDate fecha, FormaPago formaPago, String descripcion) {

		if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El monto debe ser mayor a cero");
		}

		if (tipo == null) {
			throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
		}

		if (tipo == TipoMovimiento.INGRESO) {
			if (cuentaDestino == null) {
				throw new IllegalArgumentException("Un ingreso debe tener cuenta destino");
			}
			if (!cuentaDestino.getMoneda().equals(moneda)) {
				throw new IllegalArgumentException("La moneda del movimiento no coincide con la cuenta destino");
			}
		}

		if (tipo == TipoMovimiento.GASTO) {
			if (cuentaOrigen == null) {
				throw new IllegalArgumentException("Un gasto debe tener cuenta origen");
			}
			if (!cuentaOrigen.getMoneda().equals(moneda)) {
				throw new IllegalArgumentException("La moneda del movimiento no coincide con la cuenta origen");
			}
		}

		if (tipo == TipoMovimiento.TRANSFERENCIA) {
			if (cuentaOrigen == null || cuentaDestino == null) {
				throw new IllegalArgumentException("Una transferencia debe tener cuenta origen y destino");
			}
			if (!cuentaOrigen.getMoneda().equals(cuentaDestino.getMoneda())) {
				throw new IllegalArgumentException("No se permiten transferencias entre cuentas de distinta moneda");
			}
			if (!cuentaOrigen.getMoneda().equals(moneda)) {
				throw new IllegalArgumentException("La moneda del movimiento no coincide con las cuentas");
			}
		}

		this.id = UUID.randomUUID();
		this.cuentaOrigen = cuentaOrigen;
		this.cuentaDestino = cuentaDestino;
		this.monto = monto;
		this.moneda = moneda;
		this.tipo = tipo;
		this.fecha = fecha;
		this.formaPago = formaPago;
		this.descripcion = descripcion;
	}

	public static Movimiento crearIngreso(Cuenta destino, BigDecimal monto, FormaPago formaPago, String descripcion) {

		return new Movimiento(null, destino, monto, destino.getMoneda(), TipoMovimiento.INGRESO, LocalDate.now(),
				formaPago, descripcion);
	}

	public static Movimiento crearGasto(Cuenta origen, BigDecimal monto, FormaPago formaPago, String descripcion) {

		return new Movimiento(origen, null, monto, origen.getMoneda(), TipoMovimiento.GASTO, LocalDate.now(), formaPago,
				descripcion);
	}

	public static Movimiento crearTransferencia(Cuenta origen, Cuenta destino, BigDecimal monto, FormaPago formaPago,
			String descripcion) {

		return new Movimiento(origen, destino, monto, origen.getMoneda(), TipoMovimiento.TRANSFERENCIA, LocalDate.now(),
				formaPago, descripcion);
	}

	public UUID getId() {
		return id;
	}

	public Cuenta getCuentaOrigen() {
		return cuentaOrigen;
	}

	public Cuenta getCuentaDestino() {
		return cuentaDestino;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public TipoMovimiento getTipo() {
		return tipo;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public FormaPago getFormaPago() {
		return formaPago;
	}

	public String getDescripcion() {
		return descripcion;
	}
}