package ar.com.controlfinanzas.domain.finanzas;

import java.util.Objects;
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

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Cuenta other = (Cuenta) obj;
		return Objects.equals(id, other.id);
	}

}