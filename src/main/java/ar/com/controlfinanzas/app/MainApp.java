package ar.com.controlfinanzas.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ar.com.controlfinanzas.persistence.JPAUtil;
import ar.com.controlfinanzas.service.UsuarioService;
import ar.com.controlfinanzas.ui.LoginFrame;

public class MainApp {

	public static void main(String[] args) {
		// Crear el EntityManager desde JPAUtil
		var em = JPAUtil.getEntityManager();
		var usuarioService = new UsuarioService(em);

		try {

			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				System.err.println(info.getName());

			}

			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Abrir LoginFrame
		SwingUtilities.invokeLater(() -> {

			LoginFrame login = new LoginFrame(usuarioService, em);
			login.setVisible(true);
		});

		// Nota: podés cerrar JPA al salir de la app si querés
		Runtime.getRuntime().addShutdownHook(new Thread(() -> JPAUtil.close()));
	}
}