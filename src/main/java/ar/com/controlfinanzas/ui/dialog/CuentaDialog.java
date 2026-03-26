package ar.com.controlfinanzas.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.util.NumeroUtils;
import ar.com.controlfinanzas.util.SwingUtils;

public class CuentaDialog extends JDialog {

	private JTextField txtNombre;
	private JTextField txtSaldoInicial;
	private JComboBox<TipoCuenta> comboTipo;
	private JComboBox<Moneda> comboMoneda;

	private JButton btnGuardar;
	private JButton btnCancelar;

	public CuentaDialog(JFrame parent) {
		super(parent, "Nueva Cuenta", true);

		setSize(400, 300);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());

		JPanel panelForm = new JPanel(new GridBagLayout());
		panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);

		// ===============================
		// NOMBRE
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Nombre:"), gbc);

		txtNombre = new JTextField(15);
		txtNombre.getDocument().addDocumentListener(SimpleListenner());

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(txtNombre, gbc);

		// ===============================
		// TIPO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Tipo:"), gbc);

		comboTipo = new JComboBox<>(TipoCuenta.values());

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(comboTipo, gbc);

		// ===============================
		// SALDO INICIAL
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Saldo inicial:"), gbc);

		txtSaldoInicial = new JTextField(10);
		txtSaldoInicial.setHorizontalAlignment(JTextField.RIGHT);
		txtSaldoInicial.getDocument().addDocumentListener(SimpleListenner());
		SwingUtils.configurarCampoNumerico(txtSaldoInicial);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(txtSaldoInicial, gbc);

		// ===============================
		// MONEDA
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Moneda:"), gbc);

		comboMoneda = new JComboBox<>(Moneda.values());

		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(comboMoneda, gbc);

		add(panelForm, BorderLayout.CENTER);

		// ===============================
		// BOTONES
		// ===============================
		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		btnGuardar = new JButton("Guardar");
		btnGuardar.setEnabled(false);
		btnCancelar = new JButton("Cancelar");

		panelBotones.add(btnCancelar);
		panelBotones.add(btnGuardar);

		add(panelBotones, BorderLayout.SOUTH);

		// ===============================
		// ACCIONES
		// ===============================
		btnCancelar.addActionListener(e -> dispose());

		// (guardar todavía no hace nada)
		btnGuardar.addActionListener(e -> dispose());
	}

	private DocumentListener SimpleListenner() {
		return new javax.swing.event.DocumentListener() {
			@Override
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				validarFormulario();
			}

			@Override
			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				validarFormulario();
			}

			@Override
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				validarFormulario();
			}
		};
	}

	private void validarFormulario() {

		boolean valido = true;

		// 👇 validar nombre
		String nombre = txtNombre.getText().trim();
		if (nombre.isEmpty()) {
			valido = false;
		}

		// 👇 validar saldo
		try {
			String texto = txtSaldoInicial.getText().trim();

			if (texto.isEmpty()) {
				valido = false;
			} else {
				BigDecimal saldo = NumeroUtils.parse(texto);

				if (saldo.compareTo(BigDecimal.ZERO) < 0) {
					valido = false;
				}
			}

		} catch (Exception e) {
			valido = false;
		}

		btnGuardar.setEnabled(valido);
	}
}
