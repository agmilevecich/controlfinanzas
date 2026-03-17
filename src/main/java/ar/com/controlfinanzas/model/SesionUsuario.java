package ar.com.controlfinanzas.model;

public class SesionUsuario {

	private static Usuario usuarioActual;

	public static Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public static void setUsuarioActual(Usuario usuario) {
		usuarioActual = usuario;
	}
}