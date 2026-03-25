package ar.com.controlfinanzas.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import ar.com.controlfinanzas.util.SwingUtils;

public class TransferenciaDialog extends JDialog {

	private JLabel lblSaldo;
	private JTextField txtMonto;
	private JTextField txtDescripcion;
	private JComboBox<Cuenta> comboOrigen;

	private Cuenta destino;
	private MovimientoService movimientoService;
	private CuentaService cuentaService;
	private Runnable onSuccess;
	private JButton btnTransferir;
	private JButton btnCancelar;
	private JButton btnUsarSaldo;

	public TransferenciaDialog(JFrame parent, Cuenta destino, CuentaService cuentaService,
			MovimientoService movimientoService, Runnable onSuccess) {

		super(parent, "Transferir fondos", true);
		this.destino = destino;
		this.movimientoService = movimientoService;
		this.cuentaService = cuentaService;
		this.onSuccess = onSuccess;
		this.lblSaldo = new JLabel();

		setSize(400, 260);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());

		JPanel panelForm = new JPanel(new GridBagLayout());
		panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// ===============================
		// BASE CONSTRAINTS
		// ===============================
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);

		// ===============================
		// MONTO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Monto:"), gbc);

		txtMonto = new JTextField(10);
		SwingUtils.configurarCampoDecimal(txtMonto);
		txtMonto.setHorizontalAlignment(JTextField.RIGHT);
		txtMonto.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				validarMonto();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				validarMonto();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				validarMonto();
			}
		});

		txtMonto.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				formatearMonto();
			}
		});

		// 🔥 contenedor que NO se estira
		JPanel panelMonto = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelMonto.add(txtMonto);

		btnUsarSaldo = new JButton("Usar Saldo");

		btnUsarSaldo.addActionListener(e -> {
			Cuenta origen = (Cuenta) comboOrigen.getSelectedItem();

			if (origen != null) {
				BigDecimal saldo = origen.getSaldo();
				txtMonto.setText(NumeroUtils.redondearMoneda(saldo).toString()); // sin formato por ahora
			}
		});
		panelMonto.add(btnUsarSaldo);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL; // el panel se estira, no el campo
		gbc.anchor = GridBagConstraints.WEST;

		panelForm.add(panelMonto, gbc);

		// ===============================
		// DESCRIPCIÓN
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Descripción:"), gbc);

		txtDescripcion = new JTextField(10);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(txtDescripcion, gbc);

		// ===============================
		// ORIGEN
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Desde cuenta:"), gbc);

		comboOrigen = new JComboBox<>();
		comboOrigen.addActionListener(e -> {
			actualizarSaldoLabel();
		});
		cargarCuentasOrigen();

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(comboOrigen, gbc);

		gbc.gridx = 1;
		gbc.gridy = 3; // 👈 ojo, corrés lo que sigue
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;

		lblSaldo = new JLabel("Saldo: -");
		lblSaldo.setForeground(Color.GRAY);
		lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.PLAIN, 11));
		lblSaldo.setVerticalTextPosition(JLabel.TOP);

		panelForm.add(lblSaldo, gbc);

		// ===============================
		// DESTINO (solo label)
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Hacia cuenta:"), gbc);

		JLabel lblDestino = new JLabel(destino.getNombre());
		lblDestino.setFont(lblDestino.getFont().deriveFont(java.awt.Font.BOLD));

		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(lblDestino, gbc);

		add(panelForm, BorderLayout.CENTER);

		// ===============================
		// BOTONES
		// ===============================
		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		btnTransferir = new JButton("Transferir");
		btnTransferir.setEnabled(false);
		btnCancelar = new JButton("Cancelar");

		panelBotones.add(btnCancelar);
		panelBotones.add(btnTransferir);

		add(panelBotones, BorderLayout.SOUTH);

		// ===============================
		// ACCIONES
		// ===============================
		btnTransferir.addActionListener(e -> ejecutarTransferencia());
		btnCancelar.addActionListener(e -> dispose());

		// foco inicial
		SwingUtilities.invokeLater(() -> txtMonto.requestFocus());
		actualizarSaldoLabel();
	}

	private void formatearMonto() {
		BigDecimal monto = obtenerMontoValido();

		if (monto != null) {
			BigDecimal formateado = NumeroUtils.redondearMoneda(monto);
			txtMonto.setText(formateado.toString());
		}
	}

	private void cargarCuentasOrigen() {

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(destino.getUsuario());

		cuentas.removeIf(c -> c.equals(destino) || !c.getMoneda().equals(destino.getMoneda()));

		for (Cuenta c : cuentas) {
			comboOrigen.addItem(c);
		}
	}

	private void ejecutarTransferencia() {

		String montoStr = txtMonto.getText();
		String descripcion = txtDescripcion.getText();

		if (montoStr.isEmpty() || descripcion.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Completar todos los campos");
			return;
		}

		BigDecimal monto;
		try {
			monto = NumeroUtils.parse(montoStr);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Monto inválido");
			return;
		}

		Cuenta origen = (Cuenta) comboOrigen.getSelectedItem();

		if (origen == null) {
			return;
		}

		if (monto.compareTo(origen.getSaldo()) > 0) {
			JOptionPane.showMessageDialog(this,
					"Saldo insuficiente: " + NumeroUtils.formatearMonedaARS(origen.getSaldo()));
			return;
		}

		// ORIGEN
		Movimiento movOrigen = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.TRANSFERENCIA);
		movOrigen.setDescripcion(descripcion + " -> " + destino.getNombre());
		movOrigen.setCuenta(origen);
		movimientoService.registrarMovimiento(movOrigen);

		// DESTINO
		Movimiento movDestino = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
		movDestino.setDescripcion(descripcion + " <- " + origen.getNombre());
		movDestino.setCuenta(destino);
		movimientoService.registrarMovimiento(movDestino);

		if (onSuccess != null) {
			onSuccess.run();
		}

		dispose();
	}

	private BigDecimal obtenerMontoValido() {
		try {
			String texto = txtMonto.getText().trim();

			if (texto.isEmpty()) {
				return null;
			}

			BigDecimal monto = NumeroUtils.parse(texto);

			// 👇 CLAVE: validar después del parse
			if (monto.compareTo(BigDecimal.ZERO) <= 0) {
				return null;
			}

			return monto;

		} catch (Exception e) {
			return null;
		}
	}

	private void validarMonto() {
		BigDecimal monto = obtenerMontoValido();
		Cuenta origen = (Cuenta) comboOrigen.getSelectedItem();

		boolean valido = false;

		if (monto != null && origen != null) {
			BigDecimal saldo = origen.getSaldo();

			if (saldo != null && monto.compareTo(saldo) <= 0) {
				valido = true;
			}
		}

		btnTransferir.setEnabled(valido);

		if (valido) {
			txtMonto.setForeground(Color.BLACK);
		} else {
			txtMonto.setForeground(Color.RED);
		}
	}

	private void actualizarSaldoLabel() {
		Cuenta origen = (Cuenta) comboOrigen.getSelectedItem();

		if (origen != null) {
			lblSaldo.setText("Saldo: " + NumeroUtils.formatearMonedaARS(origen.getSaldo()));
		} else {
			lblSaldo.setText("Saldo: -");
		}
	}

}