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
				"Tipo Movimiento", JOptionPane.QUESTION_MESSAGE, null,
				new TipoMovimiento[] { TipoMovimiento.INGRESO, TipoMovimiento.TRANSFERENCIA }, TipoMovimiento.INGRESO);

		if (tipo == null) {
			return;
		}

		String descripcion = JOptionPane.showInputDialog(this, "Descripción:");
		if (descripcion == null || descripcion.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "La descripción no puede estar en blanco");
			return;
		}

		// ===============================
		// 🔁 TRANSFERENCIA
		// ===============================
		if (tipo == TipoMovimiento.TRANSFERENCIA) {

			Cuenta origen;
			Cuenta destino;

			// 🔥 CASO: viene desde alerta (ya hay destino)
			if (cuentaDestinoPreseleccionada != null) {

				destino = cuentaDestinoPreseleccionada;

				// Elegir cuenta origen
				List<Cuenta> cuentasOrigen = cuentaService.getCuentasUsuario(destino.getUsuario());

				cuentasOrigen.removeIf(c -> c.equals(destino) || !c.getMoneda().equals(destino.getMoneda()));

				if (cuentasOrigen.isEmpty()) {
					JOptionPane.showMessageDialog(this, "No hay cuentas disponibles para transferir.");
					return;
				}

				JOptionPane.showMessageDialog(this,
						"Seleccioná una cuenta desde donde transferir fondos a " + destino.getNombre());

				origen = (Cuenta) JOptionPane.showInputDialog(this, "Seleccione cuenta origen:", "Cuenta Origen",
						JOptionPane.QUESTION_MESSAGE, null, cuentasOrigen.toArray(), cuentasOrigen.get(0));

				if (origen == null) {
					return;
				}

				cuentaDestinoPreseleccionada = null; // limpiar

			} else {

				// flujo normal
				origen = cuentaSeleccionada;

				List<Cuenta> cuentasDestino = cuentaService.getCuentasUsuario(origen.getUsuario());

				cuentasDestino.removeIf(c -> c.equals(origen) || !c.getMoneda().equals(origen.getMoneda()));

				if (cuentasDestino.isEmpty()) {
					JOptionPane.showMessageDialog(this,
							"No hay otra cuenta disponible con la misma moneda para transferir.");
					return;
				}

				destino = (Cuenta) JOptionPane.showInputDialog(this, "Seleccione cuenta destino:", "Cuenta Destino",
						JOptionPane.QUESTION_MESSAGE, null, cuentasDestino.toArray(), cuentasDestino.get(0));

				if (destino == null) {
					return;
				}
			}

			// 💰 Validación saldo
			BigDecimal saldoOrigen = origen.getSaldo();
			if (monto.compareTo(saldoOrigen) > 0) {
				JOptionPane.showMessageDialog(this, "No puede transferir más de lo que tiene en la cuenta: "
						+ NumeroUtils.formatearMonedaARS(saldoOrigen));
				return;
			}

			// 🔁 Movimiento origen
			Movimiento movOrigen = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.TRANSFERENCIA);
			movOrigen.setDescripcion(descripcion + " -> " + destino.getNombre());
			movOrigen.setCuenta(origen);
			movimientoService.registrarMovimiento(movOrigen);

			// 🔁 Movimiento destino
			Movimiento movDestino = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.INGRESO);
			movDestino.setDescripcion(descripcion + " <- " + origen.getNombre());
			movDestino.setCuenta(destino);
			movimientoService.registrarMovimiento(movDestino);

		} else {
			// ===============================
			// 💰 INGRESO NORMAL
			// ===============================

			Cuenta cuenta = (cuentaDestinoPreseleccionada != null) ? cuentaDestinoPreseleccionada : cuentaSeleccionada;

			Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, tipo);
			mov.setDescripcion(descripcion);
			mov.setCuenta(cuenta);

			movimientoService.registrarMovimiento(mov);

			cuentaDestinoPreseleccionada = null;
		}

		// 🔄 refrescar UI
		if (actualizarPanelCuentasCallback != null) {
			actualizarPanelCuentasCallback.run();
		}

		cargarMovimientos();
	}

	// 🔥 NUEVO MÉTODO
	public void abrirNuevoMovimientoConDestino(Cuenta cuentaDestino) {
		this.cuentaDestinoPreseleccionada = cuentaDestino;
		btnAgregar.doClick();
	}

	public void abrirNuevoMovimiento() {
		btnAgregar.doClick();
	}
}