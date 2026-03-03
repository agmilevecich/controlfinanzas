package ar.com.controlfinanzas.ui;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.UsuarioService;

public class LoginFrame extends JFrame {

	private JTextField tfNombre;
	private JButton btnLogin;
	private UsuarioService usuarioService;

	public LoginFrame(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;

		setTitle("Login");
		setSize(300, 150);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());

		tfNombre = new JTextField(15);
		btnLogin = new JButton("Entrar");

		btnLogin.addActionListener(e -> login());

		add(new JLabel("Nombre de usuario:"));
		add(tfNombre);
		add(btnLogin);
	}

	private void login() {
		String nombre = tfNombre.getText().trim();
		if (nombre.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Ingrese un nombre");
			return;
		}

		// Busca o crea usuario
		Usuario usuario = usuarioService.buscarOCrearUsuario(nombre);

		// Abre dashboard
		DashboardFrame dashboard = new DashboardFrame(usuario, usuarioService);
		dashboard.setVisible(true);

		this.dispose(); // cierra login
	}
}
