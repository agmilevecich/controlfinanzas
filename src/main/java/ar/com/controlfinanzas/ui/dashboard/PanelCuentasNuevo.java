package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TipoMovimiento;
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

	private JButton btnIngresarSaldo;
	private JButton btnCrear;
	private JButton btnAjusteSaldo;
	private JButton btnEliminar;
	private JButton btnTransferir;

	private Consumer<Cuenta> cuentaSeleccionadaListener;
	private Runnable actualizarCuentas;
	private JButton btnIrAGastos;
	private Runnable irAGastos;
	private Consumer<Cuenta> onRegistroGastos;

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
		GridBagConstraints gbcListasCuentas = new GridBagConstraints();
		gbcListasCuentas.gridx = 0;
		gbcListasCuentas.gridy = 0;
		gbcListasCuentas.fill = GridBagConstraints.VERTICAL;
		gbcListasCuentas.weighty = 1.0;
		contenedor.add(new JScrollPane(listaCuentas), gbcListasCuentas);

		add(contenedor, BorderLayout.CENTER);

		btnIngresarSaldo = new JButton("Ingresar Saldo");
		btnCrear = new JButton("Crear cuenta");
		btnAjusteSaldo = new JButton("Ajuste Saldo");
		btnEliminar = new JButton("Eliminar");
		btnTransferir = new JButton("Transferir");
		btnIrAGastos = new JButton("Registrar Gastos");

		JPanel panelBotones = new JPanel(new GridBagLayout());
		GridBagConstraints gbcBotones = new GridBagConstraints();
		gbcBotones.insets = new Insets(5, 5, 5, 5);

		gbcBotones.gridx = 0;
		gbcBotones.gridy = 0;
		gbcBotones.fill = GridBagConstraints.HORIZONTAL;
		panelBotones.add(btnIngresarSaldo, gbcBotones);

		gbcBotones.gridx = 1;
		gbcBotones.gridy = 0;
		gbcBotones.fill = GridBagConstraints.HORIZONTAL;
		panelBotones.add(btnAjusteSaldo, gbcBotones);

		gbcBotones.gridx = 0;
		gbcBotones.gridy = 1;
		gbcBotones.fill = GridBagConstraints.HORIZONTAL;
		panelBotones.add(btnEliminar, gbcBotones);

		gbcBotones.gridx = 1;
		gbcBotones.gridy = 1;
		gbcBotones.fill = GridBagConstraints.HORIZONTAL;
		panelBotones.add(btnTransferir, gbcBotones);

		gbcBotones.gridx = 0;
		gbcBotones.gridy = 2;
		gbcBotones.fill = GridBagConstraints.HORIZONTAL;
		panelBotones.add(btnCrear, gbcBotones);

		gbcBotones.gridx = 1;
		gbcBotones.gridy = 2;
		gbcBotones.fill = GridBagConstraints.HORIZONTAL;
		panelBotones.add(btnIrAGastos, gbcBotones);
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

		btnIngresarSaldo.addActionListener(e -> registrarIngreso());
		btnCrear.addActionListener(e -> crearCuenta());
		btnAjusteSaldo.addActionListener(e -> {
			ajusteSaldo(movimientoService);
		});
		btnEliminar.addActionListener(e -> eliminarCuenta());
		btnTransferir.addActionListener(e -> transferir());

		btnIrAGastos.addActionListener(e -> {

			Cuenta cuentaSeleccionada = listaCuentas.getSelectedValue();

			if (cuentaSeleccionada == null) {
				JOptionPane.showMessageDialog(this, "Seleccione una cuenta");
				return;
			}

			if (cuentaSeleccionada != null) {
				onRegistroGastos.accept(cuentaSeleccionada);
			}
		});

		cargarCuentas();
	}

	// =========================
	// MÉTODOS
	// =========================

	private void registrarIngreso() {

		Cuenta cuenta = listaCuentas.getSelectedValue();

		if (cuenta == null) {
			JOptionPane.showMessageDialog(this, "Seleccione una cuenta");
			return;
		}

		String inputMonto = JOptionPane.showInputDialog("Monto a ingresar:");

		if (inputMonto == null || inputMonto.trim().isEmpty()) {
			return;
		}

		try {
			BigDecimal monto = NumeroUtils.parse(inputMonto);

			if (monto.compareTo(BigDecimal.ZERO) <= 0) {
				JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero");
				return;
			}

			String descripcion = JOptionPane.showInputDialog("Descripción:");

			if (descripcion == null || descripcion.trim().isEmpty()) {
				descripcion = "Ingreso manual";
			}

			Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
			mov.setCuenta(cuenta);
			movimientoService.registrarMovimiento(mov);

			cargarCuentas();
			listaCuentas.setSelectedValue(cuenta, true);
			if (actualizarCuentas != null) {
				actualizarCuentas.run();
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Monto inválido");
		}
	}

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

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(SesionUsuario.getUsuarioActual());

		cuentas.removeIf(c -> c.equals(cuentaSeleccionada) || !c.getMoneda().equals(cuentaSeleccionada.getMoneda()));

		if (cuentas.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No hay otras cuentas en la misma moneda para transferir");
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

	public void setIrAGastos(Runnable irAGastos) {
		this.irAGastos = irAGastos;
	}

	public void setOnRegistroGastos(Consumer<Cuenta> listener) {
		this.onRegistroGastos = listener;
	}
}