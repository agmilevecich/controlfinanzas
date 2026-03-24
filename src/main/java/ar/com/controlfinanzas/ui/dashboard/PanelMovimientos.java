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

		BigDecimal monto = pedirMonto();
		if (monto == null) {
			return;
		}

		TipoMovimiento tipo = pedirTipoMovimiento();
		if (tipo == null) {
			return;
		}

		String descripcion = pedirDescripcion();
		if (descripcion == null) {
			return;
		}

		if (tipo == TipoMovimiento.TRANSFERENCIA) {
			procesarTransferencia(monto, descripcion);
		} else {
			procesarIngreso(monto, descripcion);
		}

		if (actualizarPanelCuentasCallback != null) {
			actualizarPanelCuentasCallback.run();
		}

		cargarMovimientos();
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
	public void abrirNuevoMovimientoConDestino(Cuenta cuentaDestino) {
		this.cuentaDestinoPreseleccionada = cuentaDestino;
		btnAgregar.doClick();
	}

	public void abrirNuevoMovimiento() {
		btnAgregar.doClick();
	}
}