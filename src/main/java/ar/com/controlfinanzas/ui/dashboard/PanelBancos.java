package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.ui.PanelBotones;

public class PanelBancos extends JPanel {

	private Usuario usuario;
	private BancoService bancoService;

	private DefaultListModel<Banco> modeloBancos;
	private JList<Banco> listaBancos;

	private PanelBotones botones = new PanelBotones();

	public PanelBancos(BancoService bancoService) {

		this.usuario = usuario = SesionUsuario.getUsuarioActual();
		this.bancoService = bancoService;

		setLayout(new BorderLayout());

		modeloBancos = new DefaultListModel<>();
		listaBancos = new JList<>(modeloBancos);
		listaBancos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(listaBancos), BorderLayout.CENTER);

		botones.getBotones()[0].setText("Crear Banco");
		botones.getBotones()[0].addActionListener(e -> crearBanco());

//		botones.getBotones()[1].setText("Eliminar Banco");
//		botones.getBotones()[1].addActionListener(e -> eliminarBanco());

		add(botones, BorderLayout.SOUTH);

		cargarBancos();
	}

	private void crearBanco() {

		String nombre = JOptionPane.showInputDialog(this, "Nombre del banco:");

		if (nombre == null || nombre.trim().isEmpty()) {
			return;
		}

		bancoService.crearBanco(usuario, nombre);

		cargarBancos();
	}

	private void eliminarBanco() {

		Banco banco = listaBancos.getSelectedValue();

		if (banco == null) {
			JOptionPane.showMessageDialog(this, "Seleccione un banco.");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar banco?", "Confirmar", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {

			bancoService.eliminarBanco(banco);

			cargarBancos();
		}
	}

	private void cargarBancos() {

		modeloBancos.clear();

		List<Banco> bancos = bancoService.getBancosUsuario(usuario);

		for (Banco b : bancos) {
			modeloBancos.addElement(b);
		}
	}

}
