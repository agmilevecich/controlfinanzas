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
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import jakarta.persistence.EntityManager;

public class PanelMovimientos extends JPanel {

	private Cuenta cuentaSeleccionada;
	private MovimientoService movimientoService;
	private DefaultListModel<String> modeloMovimientos;
	private JList<String> listaMovimientos;
	private JLabel lblSaldo;

	// Callback para actualizar PanelCuentas
	private Runnable actualizarPanelCuentasCallback;
	private EntityManager em;
	private CuentaService cuentaService;

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

		if (tipo == TipoMovimiento.TRANSFERENCIA) {
			// Validamos que la cuenta origen tenga saldo suficiente
			BigDecimal saldoOrigen = cuentaSeleccionada.getSaldo();
			if (monto.compareTo(saldoOrigen) > 0) {
				JOptionPane.showMessageDialog(this, "No puede transferir más de lo que tiene en la cuenta: "
						+ NumeroUtils.formatearMonedaARS(saldoOrigen));
				return;
			}

			// Filtramos cuentas destino con misma moneda
			List<Cuenta> cuentasDestino = cuentaService.getCuentasUsuario(cuentaSeleccionada.getUsuario());
			cuentasDestino.removeIf(
					c -> c.equals(cuentaSeleccionada) || !c.getMoneda().equals(cuentaSeleccionada.getMoneda()));

			if (cuentasDestino.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"No hay otra cuenta disponible con la misma moneda para transferir.");
				return;
			}

			// Pedimos la cuenta destino
			Cuenta destino = (Cuenta) JOptionPane.showInputDialog(this, "Seleccione cuenta destino:", "Cuenta Destino",
					JOptionPane.QUESTION_MESSAGE, null, cuentasDestino.toArray(), cuentasDestino.get(0));
			if (destino == null) {
				return;
			}

			// Movimiento en cuenta origen (negativo)
			Movimiento movOrigen = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.TRANSFERENCIA);
			movOrigen.setDescripcion(descripcion + " -> " + destino.getNombre());
			movOrigen.setCuenta(cuentaSeleccionada);
			Movimiento movimientoActualizadoOrigen = movimientoService.registrarMovimiento(cuentaSeleccionada,
					movOrigen);

			// Movimiento en cuenta destino (positivo)
			Movimiento movDestino = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
			movDestino.setDescripcion(descripcion + " <- " + cuentaSeleccionada.getNombre());
			movDestino.setCuenta(destino);
			Movimiento movimientoActualizadoDestino = movimientoService.registrarMovimiento(destino, movDestino);

			// Refrescar paneles
			if (actualizarPanelCuentasCallback != null) {
				actualizarPanelCuentasCallback.run();
			}
			cargarMovimientos();

		} else {
			// Movimiento normal
			Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, tipo);
			mov.setDescripcion(descripcion);
			mov.setCuenta(cuentaSeleccionada);

			Movimiento movimientoActualizado = movimientoService.registrarMovimiento(cuentaSeleccionada, mov);

			// Refrescar paneles
			if (actualizarPanelCuentasCallback != null) {
				actualizarPanelCuentasCallback.run();
			}
			cargarMovimientos();
		}
	}

	private Cuenta elegirCuentaDestino() {
		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(cuentaSeleccionada.getUsuario());

		// Filtrar cuentas que tengan la misma moneda que la cuenta origen
		cuentas.removeIf(c -> c.equals(cuentaSeleccionada) || !c.getMoneda().equals(cuentaSeleccionada.getMoneda()));

		if (cuentas.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No hay otra cuenta disponible con la misma moneda para transferir.");
			return null;
		}

		return (Cuenta) JOptionPane.showInputDialog(this, "Seleccione cuenta destino:", "Cuenta Destino",
				JOptionPane.QUESTION_MESSAGE, null, cuentas.toArray(), cuentas.get(0));
	}
}