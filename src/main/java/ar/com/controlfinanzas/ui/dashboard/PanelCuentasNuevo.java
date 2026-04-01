package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.ui.dialog.CuentaDialog;
import ar.com.controlfinanzas.ui.dialog.TransferenciaDialog;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelCuentasNuevo extends JPanel {

	private CuentaService cuentaService;
	private MovimientoService movimientoService;
	private BancoService bancoService;

	private JList<Cuenta> listaCuentas;
	private DefaultListModel<Cuenta> modelo;

	private JButton btnCrear;
	private JButton btnAjusteSaldo;
	private JButton btnEliminar;
	private JButton btnTransferir;

	private Consumer<Cuenta> cuentaSeleccionadaListener;
	private Runnable actualizarCuentas;

	public PanelCuentasNuevo(CuentaService cuentaService, MovimientoService movimientoService,
			BancoService bancoService) {
		this.cuentaService = cuentaService;
		this.movimientoService = movimientoService;
		this.bancoService = bancoService;

		setLayout(new BorderLayout());

		modelo = new DefaultListModel<>();
		listaCuentas = new JList<>(modelo);
		listaCuentas.setPreferredSize(new Dimension(270, 0));
		JPanel contenedor = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1.0;
		contenedor.add(new JScrollPane(listaCuentas), gbc);

		add(contenedor, BorderLayout.CENTER);

		JPanel panelBotones = new JPanel();

		btnCrear = new JButton("Crear cuenta");
		btnAjusteSaldo = new JButton("Ajuste Saldo");
		btnEliminar = new JButton("Eliminar");
		btnTransferir = new JButton("Transferir");

		panelBotones.add(btnCrear);
		panelBotones.add(btnAjusteSaldo);
		panelBotones.add(btnEliminar);
		panelBotones.add(btnTransferir);

		add(panelBotones, BorderLayout.SOUTH);

		// =========================
		// EVENTOS
		// =========================

		listaCuentas.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				Cuenta seleccionada = listaCuentas.getSelectedValue();
				if (cuentaSeleccionadaListener != null) {
					cuentaSeleccionadaListener.accept(seleccionada);
				}
			}
		});

		btnCrear.addActionListener(e -> crearCuenta());
		btnAjusteSaldo.addActionListener(e -> {
			ajusteSaldo(movimientoService);
		});
		btnEliminar.addActionListener(e -> eliminarCuenta());
		btnTransferir.addActionListener(e -> transferir());

		cargarCuentas();
	}

	// =========================
	// MÉTODOS
	// =========================

	private void ajusteSaldo(MovimientoService movimientoService) {
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
	}

	public void cargarCuentas() {
		modelo.clear();

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(SesionUsuario.getUsuarioActual());

		for (Cuenta c : cuentas) {
			modelo.addElement(c);
		}
	}

	private void crearCuenta() {

		new CuentaDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, // 👈 null = crear nueva cuenta
				cuentaService, bancoService, () -> {
					cargarCuentas();

					if (actualizarCuentas != null) {
						actualizarCuentas.run();
					}
				}).setVisible(true);
	}

	private void eliminarCuenta() {

		try {

			Cuenta cuenta = listaCuentas.getSelectedValue();

			if (cuenta == null) {
				JOptionPane.showMessageDialog(this, "Seleccioná una cuenta");
				return;
			}

			int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar cuenta?", "Confirmar",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				cuentaService.eliminar(cuenta.getId());
				cargarCuentas();

				if (actualizarCuentas != null) {
					actualizarCuentas.run();
				}
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			return;
		}

	}

	private void transferir() {

		Cuenta cuentaSeleccionada = listaCuentas.getSelectedValue();

		if (cuentaSeleccionada == null) {
			JOptionPane.showMessageDialog(this, "Seleccione una cuenta");
			return;
		}

		new TransferenciaDialog((JFrame) SwingUtilities.getWindowAncestor(this), cuentaSeleccionada, cuentaService,
				movimientoService, () -> {
					cargarCuentas();

					if (actualizarCuentas != null) {
						actualizarCuentas.run();
					}
				}).setVisible(true);
	}

	// =========================
	// LISTENERS
	// =========================

	public void setCuentaSeleccionadaListener(Consumer<Cuenta> listener) {
		this.cuentaSeleccionadaListener = listener;
	}

	public void setActualizarCuentas(Runnable callback) {
		this.actualizarCuentas = callback;
	}

	public void seleccionarCuenta(Cuenta cuenta) {
		listaCuentas.setSelectedValue(cuenta, true);
	}

	public JButton getBotonTransferir() {
		return btnTransferir;
	}
}