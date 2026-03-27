package ar.com.controlfinanzas.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.util.NumeroUtils;
import ar.com.controlfinanzas.util.SwingUtils;

public class CuentaDialog extends JDialog {

	private JTextField txtNombre;
	private JTextField txtSaldoInicial;
	private JComboBox<TipoCuenta> comboTipo;
	private JComboBox<Moneda> comboMoneda;
	private JComboBox<Banco> comboBanco;

	private JButton btnGuardar;
	private JButton btnCancelar;

	private CuentaService cuentaService;
	private BancoService bancoService;
	private Runnable onSuccess;

	public CuentaDialog(JFrame parent, CuentaService cuentaService, BancoService bancoService, Runnable onSuccess) {

		this.onSuccess = onSuccess;
		this.cuentaService = cuentaService;
		this.bancoService = bancoService;

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
		// BANCO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Banco:"), gbc);

		comboBanco = new JComboBox<>();
		cargarBancos();

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(comboBanco, gbc);

		// ===============================
		// TIPO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelForm.add(new JLabel("Tipo:"), gbc);

		comboTipo = new JComboBox<>(TipoCuenta.values());

		gbc.gridx = 1;
		gbc.gridy = 3;
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
		btnGuardar.addActionListener(e -> guardarCuenta());
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

	private void guardarCuenta() {

		try {
			String nombre = txtNombre.getText().trim();

			TipoCuenta tipo = (TipoCuenta) comboTipo.getSelectedItem();
			Moneda moneda = (Moneda) comboMoneda.getSelectedItem();

			BigDecimal saldoInicial = NumeroUtils.parse(txtSaldoInicial.getText());

			// 👇 si no tenés banco todavía, podés pasar null o uno por defecto
			Banco banco = (Banco) comboBanco.getSelectedItem();

			cuentaService.crearCuenta(SesionUsuario.getUsuarioActual(), // ⚠️ o el usuario actual
					nombre, banco, "Saldo inicial", tipo, moneda, saldoInicial, 0.0, // interesDiario (por ahora)
					java.time.LocalDate.now());

			if (onSuccess != null) {
				onSuccess.run();
			}

			dispose();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	private DocumentListener SimpleListenner() {
		return new javax.swing.event.DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				validarFormulario();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				validarFormulario();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
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

	private void cargarBancos() {

		List<Banco> bancos = bancoService.getBancosUsuario(SesionUsuario.getUsuarioActual()); // o como lo tengas

		for (Banco b : bancos) {
			comboBanco.addItem(b);
		}
	}
}
