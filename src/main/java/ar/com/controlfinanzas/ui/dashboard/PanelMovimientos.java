package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelMovimientos extends JPanel {

	private Cuenta cuentaSeleccionada;
	private MovimientoService movimientoService;
	private DefaultListModel<String> modeloMovimientos;
	private JList<String> listaMovimientos;
	private JLabel lblSaldo;

	// Callback para actualizar PanelCuentas
	private Runnable actualizarPanelCuentasCallback;

	public PanelMovimientos(Cuenta cuenta, MovimientoService movimientoService) {
		this.cuentaSeleccionada = cuenta;
		this.movimientoService = movimientoService;

		setLayout(new BorderLayout());

		lblSaldo = new JLabel("Saldo: 0");
		add(lblSaldo, BorderLayout.NORTH);

		modeloMovimientos = new DefaultListModel<>();
		listaMovimientos = new JList<>(modeloMovimientos);
		add(new JScrollPane(listaMovimientos), BorderLayout.CENTER);

		JButton btnAgregar = new JButton("Agregar Movimiento");
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

	private void cargarMovimientos() {
		modeloMovimientos.clear();

		if (cuentaSeleccionada == null) {
			lblSaldo.setText("Saldo: 0");
			return;
		}

		List<Movimiento> movimientos = movimientoService.getMovimientosCuenta(cuentaSeleccionada);
		BigDecimal saldo = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {
			modeloMovimientos.addElement(m.getFecha() + " | " + m.getTipo() + " | " + m.getMonto() + " | ");

			switch (m.getTipo()) {
			case INGRESO -> saldo = saldo.add(m.getMonto());
			case GASTO, TRANSFERENCIA -> saldo = saldo.subtract(m.getMonto());
			}
		}
		lblSaldo.setText("Saldo: " + saldo);
	}

	private void crearMovimientoDialog() {
		if (cuentaSeleccionada == null) {
			JOptionPane.showMessageDialog(this, "Seleccione una cuenta primero");
			return;
		}

		String montoStr = JOptionPane.showInputDialog(this, "Monto:");
		if (montoStr == null || montoStr.isEmpty()) {
			return;
		}

		BigDecimal monto;
		try {
			monto = NumeroUtils.parse(montoStr);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Monto inválido");
			return;
		}

		TipoMovimiento tipo = (TipoMovimiento) JOptionPane.showInputDialog(this, "Tipo de movimiento:",
				"Tipo Movimiento", JOptionPane.QUESTION_MESSAGE, null, TipoMovimiento.values(), TipoMovimiento.INGRESO);
		if (tipo == null) {
			return;
		}

		String descripcion = JOptionPane.showInputDialog(this, "Descripción:");

		Movimiento mov = new Movimiento(LocalDate.now(), monto, tipo);
		mov.setCuenta(cuentaSeleccionada);

		movimientoService.registrarMovimiento(cuentaSeleccionada, mov);

		cargarMovimientos();

		// Actualizar PanelCuentas para reflejar saldo nuevo
		if (actualizarPanelCuentasCallback != null) {
			actualizarPanelCuentasCallback.run();
		}
	}
}