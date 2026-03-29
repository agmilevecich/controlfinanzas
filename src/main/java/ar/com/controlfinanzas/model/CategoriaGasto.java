package ar.com.controlfinanzas.model;

public enum CategoriaGasto {

	SUPERMERCADO("Supermercado"), SERVICIOS("Servicios"), ALQUILER("Alquiler"), TRANSPORTE("Transporte"),
	SALUD("Salud"), EDUCACION("Educación"), ENTRETENIMIENTO("Entretenimiento"), IMPUESTOS("Impuestos"), OTROS("Otros"),
	AJUSTE("Ajuste"), TRANSFERENCIA("Transferencia");

	private final String descripcion;

	CategoriaGasto(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return descripcion;
	}
}
