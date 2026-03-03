package ar.com.controlfinanzas.app;

import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.persistence.JPAUtil;
import ar.com.controlfinanzas.service.UsuarioService;
import ar.com.controlfinanzas.ui.LoginFrame;

public class MainApp {

	public static void main(String[] args) {
		// Crear el EntityManager desde JPAUtil
		var em = JPAUtil.getEntityManager();
		var usuarioService = new UsuarioService(em);

		// Abrir LoginFrame
		SwingUtilities.invokeLater(() -> {
			LoginFrame login = new LoginFrame(usuarioService);
			login.setVisible(true);
		});

		// Nota: podés cerrar JPA al salir de la app si querés
		Runtime.getRuntime().addShutdownHook(new Thread(() -> JPAUtil.close()));
	}
}