package ar.com.controlfinanzas.ui;

import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.UsuarioService;
import jakarta.persistence.EntityManager;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField tfNombre;
	private JButton btnLogin;
	private UsuarioService usuarioService;
	private CuentaService cuentaService;
	private MovimientoService movimientoService;

	private EntityManager em;

	public LoginFrame(UsuarioService usuarioService, EntityManager em) {
		this.em = em;
		this.usuarioService = usuarioService;
		this.cuentaService = new CuentaService(em);
		this.movimientoService = new MovimientoService(em);
		this.setTitle("Login");
		setSize(300, 150);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());

		tfNombre = new JTextField(15);
		tfNombre.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					login();
				}

			}
		});
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

		SesionUsuario.setUsuarioActual(usuario);

		// Abre dashboard
		DashboardFrame dashboard = new DashboardFrame(cuentaService, movimientoService, em);
		dashboard.setVisible(true);

		this.dispose(); // cierra login
	}
}
