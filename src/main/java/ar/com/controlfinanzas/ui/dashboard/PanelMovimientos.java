package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.ui.dialog.TransferenciaDialog;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelMovimientos extends JPanel {

	private Cuenta cuentaSeleccionada;
	private MovimientoService movimientoService;
	private DefaultListModel<String> modeloMovimientos;
	private JList<String> listaMovimientos;
	private JLabel lblSaldo;

	private Runnable actualizarPanelCuentasCallback;
	private CuentaService cuentaService;
	private JButton btnAgregar;

	// 🔥 NUEVO: cuenta destino preseleccionada
	private Cuenta cuentaDestinoPreseleccionada = null;;

	public PanelMovimientos(Cuenta cuenta, CuentaService cuentaService, MovimientoService movimientoService) {
		this.cuentaSeleccionada = cuenta;
		this.movimientoService = movimientoService;
		this.cuentaService = cuentaService;

		setLayout(new BorderLayout());

		lblSaldo = new JLabel("Saldo: 0");
		add(lblSaldo, BorderLayout.NORTH);

		modeloMovimientos = new DefaultListModel<>();
		listaMovimientos = new JList<>(modeloMovimientos);
		add(new JScrollPane(listaMovimientos), BorderLayout.CENTER);

		btnAgregar = new JButton("Agregar Movimiento");
		btnAgregar.addActionListener(e -> crearMovimientoDialog());
		add(btnAgregar, BorderLayout.SOUTH);

		cargarMovimientos();
	}

	public void setActualizarPanelCuentasCallback(Runnable callback) {
		this.actualizarPanelCuentasCallback = callback;
	}

	public void actualizarCuenta(Cuenta cuenta) {
		this.cuentaSeleccionada = cuenta;
		cargarMovimientos();
	}

	public void cargarMovimientos() {
		modeloMovimientos.clear();

		if (cuentaSeleccionada == null) {
			lblSaldo.setText("Saldo: 0");
			return;
		}

		List<Movimiento> movimientos = movimientoService.getMovimientosCuenta(cuentaSeleccionada);

		for (Movimiento m : movimientos) {
			modeloMovimientos.addElement(m.getFecha() + " | " + m.getTipo() + " | " + m.getDescripcion() + " | "
					+ NumeroUtils.formatearMonedaARS(m.getMonto()) + " | ");
		}

		lblSaldo.setText("Saldo: " + NumeroUtils.formatearMonedaARS(cuentaSeleccionada.getSaldo()));
	}

	private void crearMovimientoDialog() {

		if (cuentaSeleccionada == null && cuentaDestinoPreseleccionada == null) {
			JOptionPane.showMessageDialog(this, "Seleccione una cuenta primero");
			return;
		}

		JDialog dialog = new JDialog((JFrame) null, "Nuevo Movimiento", true);
		dialog.setSize(350, 250);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout());

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		// ===============================
		// MONTO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(new JLabel("Monto:"), gbc);

		JTextField txtMonto = new JTextField(10); // 🔥 tamaño controlado

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(txtMonto, gbc);

		// ===============================
		// TIPO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(new JLabel("Tipo:"), gbc);

		JComboBox<TipoMovimiento> comboTipo = new JComboBox<>(
				new TipoMovimiento[] { TipoMovimiento.INGRESO, TipoMovimiento.TRANSFERENCIA });

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(comboTipo, gbc);

		// ===============================
		// DESCRIPCIÓN
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(new JLabel("Descripción:"), gbc);

		JTextField txtDescripcion = new JTextField(15);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(txtDescripcion, gbc);

		dialog.add(panel, BorderLayout.CENTER);

		// ===============================
		// BOTONES
		// ===============================
		JPanel panelBotones = new JPanel();

		JButton btnAceptar = new JButton("Aceptar");
		JButton btnCancelar = new JButton("Cancelar");

		panelBotones.add(btnAceptar);
		panelBotones.add(btnCancelar);

		dialog.add(panelBotones, BorderLayout.SOUTH);

		// ===============================
		// ACCIONES
		// ===============================
		btnAceptar.addActionListener(e -> {

			String montoStr = txtMonto.getText();

			if (montoStr.isEmpty()) {
				JOptionPane.showMessageDialog(dialog, "Ingrese un monto");
				return;
			}

			BigDecimal monto;
			try {
				monto = NumeroUtils.parse(montoStr);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(dialog, "Monto inválido");
				return;
			}

			TipoMovimiento tipo = (TipoMovimiento) comboTipo.getSelectedItem();
			String descripcion = txtDescripcion.getText();

			if (descripcion == null || descripcion.trim().isEmpty()) {
				JOptionPane.showMessageDialog(dialog, "Ingrese una descripción");
				return;
			}

			// ===============================
			// TRANSFERENCIA
			// ===============================
			if (tipo == TipoMovimiento.TRANSFERENCIA) {

				if (cuentaSeleccionada == null) {
					JOptionPane.showMessageDialog(dialog, "No hay cuenta origen seleccionada");
					return;
				}

				BigDecimal saldoOrigen = cuentaSeleccionada.getSaldo();

				if (monto.compareTo(saldoOrigen) > 0) {
					JOptionPane.showMessageDialog(dialog, "Saldo insuficiente");
					return;
				}

				List<Cuenta> cuentasDestino = cuentaService.getCuentasUsuario(cuentaSeleccionada.getUsuario());

				cuentasDestino.removeIf(
						c -> c.equals(cuentaSeleccionada) || !c.getMoneda().equals(cuentaSeleccionada.getMoneda()));

				if (cuentasDestino.isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "No hay cuentas destino disponibles");
					return;
				}

				Cuenta destino;

				if (cuentaDestinoPreseleccionada != null) {
					destino = cuentaDestinoPreseleccionada;
					cuentaDestinoPreseleccionada = null;
				} else {
					destino = (Cuenta) JOptionPane.showInputDialog(dialog, "Seleccione cuenta destino", "Destino",
							JOptionPane.QUESTION_MESSAGE, null, cuentasDestino.toArray(), cuentasDestino.get(0));
				}

				if (destino == null) {
					return;
				}

				Movimiento movOrigen = new Movimiento(LocalDate.now(), descripcion, monto,
						TipoMovimiento.TRANSFERENCIA);
				movOrigen.setDescripcion(descripcion + " -> " + destino.getNombre());
				movOrigen.setCuenta(cuentaSeleccionada);
				movimientoService.registrarMovimiento(movOrigen);

				Movimiento movDestino = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
				movDestino.setDescripcion(descripcion + " <- " + cuentaSeleccionada.getNombre());
				movDestino.setCuenta(destino);
				movimientoService.registrarMovimiento(movDestino);

			} else {

				Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, tipo);
				mov.setCuenta(cuentaSeleccionada);
				movimientoService.registrarMovimiento(mov);
			}

			if (actualizarPanelCuentasCallback != null) {
				actualizarPanelCuentasCallback.run();
			}

			cargarMovimientos();

			dialog.dispose();
		});

		btnCancelar.addActionListener(e -> dialog.dispose());

		dialog.setVisible(true);
	}

	private BigDecimal pedirMonto() {
		String montoStr = JOptionPane.showInputDialog(this, "Monto:");
		if (montoStr == null || montoStr.isEmpty()) {
			return null;
		}

		try {
			return NumeroUtils.parse(montoStr);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Monto inválido");
			return null;
		}
	}

	private TipoMovimiento pedirTipoMovimiento() {
		return (TipoMovimiento) JOptionPane.showInputDialog(this, "Tipo de movimiento:", "Tipo Movimiento",
				JOptionPane.QUESTION_MESSAGE, null,
				new TipoMovimiento[] { TipoMovimiento.INGRESO, TipoMovimiento.TRANSFERENCIA }, TipoMovimiento.INGRESO);
	}

	private String pedirDescripcion() {
		String desc = JOptionPane.showInputDialog(this, "Descripción:");
		if (desc == null || desc.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "La descripción no puede estar en blanco");
			return null;
		}
		return desc;
	}

	private void procesarTransferencia(BigDecimal monto, String descripcion) {

		Cuenta origen;
		Cuenta destino;

		if (cuentaDestinoPreseleccionada != null) {
			destino = cuentaDestinoPreseleccionada;

			JOptionPane.showMessageDialog(this,
					"Seleccioná una cuenta desde donde transferir fondos a " + destino.getNombre());

			origen = elegirCuentaOrigen(destino);
			if (origen == null) {
				return;
			}

			cuentaDestinoPreseleccionada = null;

		} else {
			origen = cuentaSeleccionada;
			destino = elegirCuentaDestino(origen);
			if (destino == null) {
				return;
			}
		}

		if (monto.compareTo(origen.getSaldo()) > 0) {
			JOptionPane.showMessageDialog(this,
					"Saldo insuficiente: " + NumeroUtils.formatearMonedaARS(origen.getSaldo()));
			return;
		}

		Movimiento movOrigen = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.TRANSFERENCIA);
		movOrigen.setDescripcion(descripcion + " -> " + destino.getNombre());
		movOrigen.setCuenta(origen);
		movimientoService.registrarMovimiento(movOrigen);

		Movimiento movDestino = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
		movDestino.setDescripcion(descripcion + " <- " + origen.getNombre());
		movDestino.setCuenta(destino);
		movimientoService.registrarMovimiento(movDestino);
	}

	private void procesarIngreso(BigDecimal monto, String descripcion) {

		Cuenta cuenta = (cuentaDestinoPreseleccionada != null) ? cuentaDestinoPreseleccionada : cuentaSeleccionada;

		Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
		mov.setCuenta(cuenta);

		movimientoService.registrarMovimiento(mov);

		cuentaDestinoPreseleccionada = null;
	}

	private Cuenta elegirCuentaOrigen(Cuenta destino) {

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(destino.getUsuario());

		cuentas.removeIf(c -> c.equals(destino) || !c.getMoneda().equals(destino.getMoneda()));

		if (cuentas.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No hay cuentas disponibles");
			return null;
		}

		return (Cuenta) JOptionPane.showInputDialog(this, "Seleccione cuenta origen:", "Cuenta Origen",
				JOptionPane.QUESTION_MESSAGE, null, cuentas.toArray(), cuentas.get(0));
	}

	private Cuenta elegirCuentaDestino(Cuenta origen) {

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(origen.getUsuario());

		cuentas.removeIf(c -> c.equals(origen) || !c.getMoneda().equals(origen.getMoneda()));

		if (cuentas.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No hay cuentas disponibles");
			return null;
		}

		return (Cuenta) JOptionPane.showInputDialog(this, "Seleccione cuenta destino:", "Cuenta Destino",
				JOptionPane.QUESTION_MESSAGE, null, cuentas.toArray(), cuentas.get(0));
	}

	// 🔥 NUEVO MÉTODO

	public void abrirTransferenciaConDestino(Cuenta destino) {

		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

		TransferenciaDialog dialog = new TransferenciaDialog(frame, destino, cuentaService, movimientoService, () -> {
			if (actualizarPanelCuentasCallback != null) {
				actualizarPanelCuentasCallback.run();
			}
			cargarMovimientos();
		});

		dialog.setVisible(true);
	}

	private void crearTransferenciaDirecta() {

		if (cuentaDestinoPreseleccionada == null) {
			return;
		}

		Cuenta destino = cuentaDestinoPreseleccionada;

		BigDecimal monto = pedirMonto();
		if (monto == null) {
			return;
		}

		String descripcion = pedirDescripcion();
		if (descripcion == null) {
			return;
		}

		JOptionPane.showMessageDialog(this, "Seleccioná una cuenta desde donde transferir a " + destino.getNombre());

		Cuenta origen = elegirCuentaOrigen(destino);
		if (origen == null) {
			return;
		}

		if (monto.compareTo(origen.getSaldo()) > 0) {
			JOptionPane.showMessageDialog(this,
					"Saldo insuficiente: " + NumeroUtils.formatearMonedaARS(origen.getSaldo()));
			return;
		}

		// Movimiento origen
		Movimiento movOrigen = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.TRANSFERENCIA);
		movOrigen.setDescripcion(descripcion + " -> " + destino.getNombre());
		movOrigen.setCuenta(origen);
		movimientoService.registrarMovimiento(movOrigen);

		// Movimiento destino
		Movimiento movDestino = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
		movDestino.setDescripcion(descripcion + " <- " + origen.getNombre());
		movDestino.setCuenta(destino);
		movimientoService.registrarMovimiento(movDestino);

		cuentaDestinoPreseleccionada = null;

		if (actualizarPanelCuentasCallback != null) {
			actualizarPanelCuentasCallback.run();
		}

		cargarMovimientos();
	}

	public void abrirNuevoMovimiento() {
		btnAgregar.doClick();
	}
}