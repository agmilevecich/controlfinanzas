package ar.com.controlfinanzas.app;

import java.util.List;

import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.UsuarioService;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class MainApp {

	private static Usuario usuarioActivo;

	public static void main(String[] args) {

//		MainApp.class.getClassLoader().getResourceAsStream("logging.properties");
//		System.setProperty("java.util.logging.config.file", "logging.properties");

		UsuarioService usuarioService = new UsuarioService();
		List<Usuario> usuarios = usuarioService.listarUsuarios();
		if (usuarios.isEmpty()) {
			Usuario nuevo = new Usuario("Usuario Principal");
			usuarioService.guardarUsuario(nuevo);
			usuarioActivo = nuevo;
		} else {
			usuarioActivo = usuarios.get(0);
		}

		SwingUtilities.invokeLater(() -> {

			try {
				new DashboardFrame().setVisible(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public static Usuario getUsuarioActivo() {
		return usuarioActivo;
	}
}
