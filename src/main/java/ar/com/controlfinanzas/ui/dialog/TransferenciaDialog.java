package ar.com.controlfinanzas.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import ar.com.controlfinanzas.util.SwingUtils;

public class TransferenciaDialog extends JDialog {

	private JLabel lblSaldo = new JLabel();
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

		setSize(400, 300);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());
		setResizable(false);

		JPanel panelForm = new JPanel(new GridBagLayout());
		panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

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
		SwingUtils.configurarCampoMoneda(txtMonto);
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

		JPanel panelMonto = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelMonto.add(txtMonto);

		btnUsarSaldo = new JButton("Usar Saldo");
		btnUsarSaldo.addActionListener(e -> {
			Cuenta origen = (Cuenta) comboOrigen.getSelectedItem();
			if (origen != null) {
				txtMonto.setText(NumeroUtils.redondearMoneda(origen.getSaldo()).toString());
			}
		});

		panelMonto.add(btnUsarSaldo);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(panelMonto, gbc);

		// ===============================
		// DESCRIPCIÓN
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		panelForm.add(new JLabel("Descripción:"), gbc);

		txtDescripcion = new JTextField(15);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(txtDescripcion, gbc);

		// ===============================
		// ORIGEN
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 2;
		panelForm.add(new JLabel("Desde cuenta:"), gbc);

		comboOrigen = new JComboBox<>();
		comboOrigen.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof Cuenta c) {
					lbl.setText(c.getNombre());
				}

				return lbl;
			}

		});
		comboOrigen.addActionListener(e -> actualizarSaldoLabel());
		cargarCuentasOrigen();

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelForm.add(comboOrigen, gbc);

		// SALDO
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;

		lblSaldo = new JLabel("Saldo: -");
		lblSaldo.setForeground(Color.GRAY);
		lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.PLAIN, 11));

		panelForm.add(lblSaldo, gbc);

		// ===============================
		// DESTINO
		// ===============================
		gbc.gridx = 0;
		gbc.gridy = 4;
		panelForm.add(new JLabel("Hacia cuenta:"), gbc);

		JLabel lblDestino = new JLabel(destino.getNombre());
		lblDestino.setFont(lblDestino.getFont().deriveFont(Font.BOLD));

		gbc.gridx = 1;
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

		SwingUtilities.invokeLater(() -> txtMonto.requestFocus());
		actualizarSaldoLabel();
	}

	private void ejecutarTransferencia() {

		try {
			BigDecimal monto = NumeroUtils.parse(txtMonto.getText());

			String descripcion = txtDescripcion.getText();

			if (descripcion == null || descripcion.trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Ingrese una descripción");
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

			// 🔥 ACÁ ESTÁ LA CLAVE
			movimientoService.transferir(origen, destino, monto, descripcion, origen.getUsuario());

			if (onSuccess != null) {
				onSuccess.run();
			}

			dispose();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	private void cargarCuentasOrigen() {
		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(destino.getUsuario());

		cuentas.removeIf(c -> c.equals(destino) || !c.getMoneda().equals(destino.getMoneda()));

		for (Cuenta c : cuentas) {
			comboOrigen.addItem(c);
		}
	}

	private void validarMonto() {

		try {
			BigDecimal monto = NumeroUtils.parse(txtMonto.getText());
			Cuenta origen = (Cuenta) comboOrigen.getSelectedItem();

			boolean valido = monto.compareTo(BigDecimal.ZERO) > 0 && origen != null
					&& monto.compareTo(origen.getSaldo()) <= 0;

			btnTransferir.setEnabled(valido);
			txtMonto.setForeground(valido ? Color.BLACK : Color.RED);

		} catch (Exception e) {
			btnTransferir.setEnabled(false);
			txtMonto.setForeground(Color.RED);
		}
	}

	private void formatearMonto() {
		try {
			BigDecimal monto = NumeroUtils.parse(txtMonto.getText());
			txtMonto.setText(NumeroUtils.redondearMoneda(monto).toString());
		} catch (Exception ignored) {
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