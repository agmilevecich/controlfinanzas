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

	public Movimiento(Cuenta cuentaOrigen, Cuenta cuentaDestino, BigDecimal monto, Moneda moneda, TipoMovimiento tipo,
			LocalDate fecha, FormaPago formaPago, String descripcion) {

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