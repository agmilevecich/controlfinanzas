package ar.com.controlfinanzas.domain.finanzas;

import java.util.UUID;

import ar.com.controlfinanzas.model.Moneda;

public class Cuenta {

	private UUID id;
	private String nombre;
	private TipoCuenta tipo;
	private Moneda moneda;
	private boolean activa;

	public Cuenta(String nombre, TipoCuenta tipo, Moneda moneda) {
		this.id = UUID.randomUUID();
		this.nombre = nombre;
		this.tipo = tipo;
		this.moneda = moneda;
		this.activa = true;
	}

	public UUID getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public TipoCuenta getTipo() {
		return tipo;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public boolean isActiva() {
		return activa;
	}

	public void desactivar() {
		this.activa = false;
	}
}