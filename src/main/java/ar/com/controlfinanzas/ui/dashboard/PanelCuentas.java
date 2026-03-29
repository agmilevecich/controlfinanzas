package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.ui.PanelBotones;
import ar.com.controlfinanzas.ui.dialog.CuentaDialog;
import ar.com.controlfinanzas.ui.dialog.TransferenciaDialog;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelCuentas extends JPanel {

	private Usuario usuario;
	private CuentaService cuentaService;
	private BancoService bancoService;

	private JList<Cuenta> listaCuentas;
	private DefaultListModel<Cuenta> modeloCuentas;

	private Consumer<Cuenta> cuentaSeleccionadaListener;

	private PanelBotones botones = new PanelBotones();

	private Runnable actualizarCuentas;

	private boolean cuentaNueva;

	public PanelCuentas(CuentaService cuentaService, MovimientoService movimientoService, BancoService bancoService) {

		this.usuario = SesionUsuario.getUsuarioActual();
		this.cuentaService = cuentaService;
		this.bancoService = bancoService;

		setLayout(new BorderLayout());

		modeloCuentas = new DefaultListModel<>();
		listaCuentas = new JList<>(modeloCuentas);
		listaCuentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(listaCuentas), BorderLayout.CENTER);

		// Listener selección de cuenta
		listaCuentas.addListSelectionListener(e -> {
			int index = listaCuentas.getSelectedIndex();

			if (index >= 0 && cuentaSeleccionadaListener != null) {
				Cuenta cuenta = modeloCuentas.getElementAt(index);
				cuentaSeleccionadaListener.accept(cuenta);
			}
		});

		listaCuentas.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					Cuenta seleccionada = listaCuentas.getSelectedValue();

					if (seleccionada != null) {
						abrirDialogoEditarCuenta(seleccionada);
					}
				}
			}
		});

		// Botón crear cuenta
		botones.getBotones()[0].setText("Crear Cuenta");
		botones.getBotones()[0].addActionListener(e -> crearCuentaDialog());
		botones.getBotones()[1].setText("Ajustar Saldo");
		botones.getBotones()[1].addActionListener(e -> {

			Cuenta cuenta = listaCuentas.getSelectedValue();

			if (cuenta == null) {
				return;
			}

			String input = JOptionPane.showInputDialog(this, "Nuevo saldo:");

			if (input == null || input.isBlank()) {
				return;
			}

			try {
				BigDecimal nuevoSaldo = NumeroUtils.parse(input);

				movimientoService.ajustarSaldo(cuenta, nuevoSaldo);

				cargarCuentas();

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Monto inválido");
			}
		});

		botones.getBotones()[2].setText("Transferir");

		botones.getBotones()[2].addActionListener(e -> {

			Cuenta cuentaSeleccionada = getCuentaSeleccionada();

			if (cuentaSeleccionada == null) {
				JOptionPane.showMessageDialog(this, "Seleccione una cuenta destino");
				return;
			}

			// 🔥 VALIDACIÓN CLAVE
			List<Cuenta> cuentas = cuentaService.getCuentasUsuario(usuario);

			cuentas.removeIf(
					c -> c.equals(cuentaSeleccionada) || !c.getMoneda().equals(cuentaSeleccionada.getMoneda()));

			if (cuentas.isEmpty()) {
				JOptionPane.showMessageDialog(this, "No hay otras cuentas en la misma moneda para transferir");
				return;
			}

			// 👇 recién acá abrís el dialog
			JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

			new TransferenciaDialog(frame, cuentaSeleccionada, cuentaService, movimientoService, () -> {
				cargarCuentas();
			}).setVisible(true);

		});
		add(botones, BorderLayout.SOUTH);

		cargarCuentas();
	}

	private Cuenta getCuentaSeleccionada() {
		return listaCuentas.getSelectedValue();
	}

	private void abrirDialogoEditarCuenta(Cuenta cuenta) {

		CuentaDialog dialog = new CuentaDialog((JFrame) SwingUtilities.getWindowAncestor(this), cuenta, cuentaService,
				bancoService, this::cargarCuentas);

		dialog.setVisible(true);
	}

	private void crearCuentaDialog() {

		List<Banco> bancos = bancoService.getBancosUsuario(usuario);

		if (bancos.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Primero debe crear un banco.", "Sin bancos",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		CuentaDialog dialog = new CuentaDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, cuentaService,
				bancoService, () -> {
					cuentaNueva = true;
					cargarCuentas();
					cuentaNueva = false;

					if (actualizarCuentas != null) {
						actualizarCuentas.run();
					}
				});

		dialog.setVisible(true);
	}

	public void setActualizarCuentas(Runnable actualizarCuentas) {
		this.actualizarCuentas = actualizarCuentas;
	}

	public void cargarCuentas() {

		Cuenta seleccionadaAntes = listaCuentas.getSelectedValue();

		modeloCuentas.clear();

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(usuario);

		for (Cuenta c : cuentas) {
			modeloCuentas.addElement(c);
		}

		if (cuentaNueva) {

			listaCuentas.setSelectedIndex(listaCuentas.getModel().getSize() - 1);

		} else if (seleccionadaAntes != null && cuentas.contains(seleccionadaAntes)) {

			listaCuentas.setSelectedValue(seleccionadaAntes, true);

		} else if (!cuentas.isEmpty()) {

			listaCuentas.setSelectedIndex(0);
		}
	}

	public void setCuentaSeleccionadaListener(Consumer<Cuenta> listener) {
		this.cuentaSeleccionadaListener = listener;
	}

	public void seleccionarCuenta(Cuenta cuenta) {
		listaCuentas.setSelectedValue(cuenta, true);
	}

	public JButton getBotonTransferir() {
		return botones.getBotones()[2];
	}
}
