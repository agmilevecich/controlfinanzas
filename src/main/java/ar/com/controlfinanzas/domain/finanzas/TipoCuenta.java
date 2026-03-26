package ar.com.controlfinanzas.domain.finanzas;

public enum TipoCuenta {
	CAJA_AHORRO("Caja de Ahorros"), CUENTA_CORRIENTE("Cuenta Corriente"), BILLETERA_VIRTUAL("Billetera Virtual"),
	EFECTIVO("Efectivo");

	private String descripcion;

	private TipoCuenta(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return descripcion;
	}
}