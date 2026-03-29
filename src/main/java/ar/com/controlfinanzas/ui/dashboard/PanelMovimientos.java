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

import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.TipoMovimiento;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
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

			modeloMovimientos.addElement(m.getFecha() + " | " + ((m.getTipo() == TipoMovimiento.GASTO
					&& (m.getCategoria() == CategoriaGasto.AJUSTE || m.getCategoria() == CategoriaGasto.TRANSFERENCIA)
					|| (m.getTipo() == TipoMovimiento.INGRESO && (m.getCategoria() == CategoriaGasto.AJUSTE
							|| m.getCategoria() == CategoriaGasto.TRANSFERENCIA)))
									? m.getCategoria().toString().toUpperCase()
									: m.getTipo())
					+ " | " + m.getDescripcion() + " | " + NumeroUtils.formatearMonedaARS(m.getMonto()));
		}

		lblSaldo.setText("Saldo: " + NumeroUtils.formatearMonedaARS(cuentaSeleccionada.getSaldo()));
	}

	private void crearMovimientoDialog() {

		if (cuentaSeleccionada == null) {
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

		// MONTO
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(new JLabel("Monto:"), gbc);

		JTextField txtMonto = new JTextField(10);

		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(txtMonto, gbc);

		// TIPO
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Tipo:"), gbc);

		JComboBox<TipoMovimiento> comboTipo = new JComboBox<>(
				new TipoMovimiento[] { TipoMovimiento.INGRESO, TipoMovimiento.GASTO });

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(comboTipo, gbc);

		// DESCRIPCIÓN
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Descripción:"), gbc);

		JTextField txtDescripcion = new JTextField(15);

		gbc.gridx = 1;
		panel.add(txtDescripcion, gbc);

		dialog.add(panel, BorderLayout.CENTER);

		// BOTONES
		JPanel panelBotones = new JPanel();

		JButton btnAceptar = new JButton("Aceptar");
		JButton btnCancelar = new JButton("Cancelar");

		panelBotones.add(btnAceptar);
		panelBotones.add(btnCancelar);

		dialog.add(panelBotones, BorderLayout.SOUTH);

		// ACCIÓN ACEPTAR
		btnAceptar.addActionListener(e -> {

			try {
				BigDecimal monto = NumeroUtils.parse(txtMonto.getText());

				if (monto.compareTo(BigDecimal.ZERO) <= 0) {
					JOptionPane.showMessageDialog(dialog, "El monto debe ser mayor a 0");
					return;
				}

				String descripcion = txtDescripcion.getText();

				if (descripcion == null || descripcion.trim().isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "Ingrese una descripción");
					return;
				}

				TipoMovimiento tipo = (TipoMovimiento) comboTipo.getSelectedItem();

				Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, tipo);
				mov.setCuenta(cuentaSeleccionada);

				movimientoService.registrarMovimiento(mov);

				if (actualizarPanelCuentasCallback != null) {
					actualizarPanelCuentasCallback.run();
				}

				cargarMovimientos();
				dialog.dispose();

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(dialog, "Monto inválido");
			}
		});

		btnCancelar.addActionListener(e -> dialog.dispose());

		dialog.setVisible(true);
	}

	public void abrirNuevoMovimiento() {
		btnAgregar.doClick();
	}
}