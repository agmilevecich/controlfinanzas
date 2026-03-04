package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.ui.PanelBotones;

public class PanelCuentas extends JPanel {

	private Usuario usuario;
	private CuentaService cuentaService;
	private MovimientoService movimientoService;
	private JList<Cuenta> listaCuentas;
	private DefaultListModel<Cuenta> modeloCuentas;
	private Consumer<Cuenta> cuentaSeleccionadaListener;
	private PanelBotones botones = new PanelBotones();

	public PanelCuentas(Usuario usuario, CuentaService cuentaService, MovimientoService movimientoService) {
		this.usuario = usuario;
		this.cuentaService = cuentaService;
		this.movimientoService = movimientoService;

		setLayout(new BorderLayout());

		modeloCuentas = new DefaultListModel<>();
		listaCuentas = new JList<>(modeloCuentas);
		listaCuentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(listaCuentas), BorderLayout.CENTER);

		// Listener para seleccionar cuenta
		listaCuentas.addListSelectionListener(e -> {
			int index = listaCuentas.getSelectedIndex();
			if (index >= 0 && cuentaSeleccionadaListener != null) {
				Cuenta cuenta = modeloCuentas.getElementAt(index);
				cuentaSeleccionadaListener.accept(cuenta);
			}
		});

		// Botón para crear nueva cuenta
		botones.getBotones()[0].setText("Crear Cuenta");
		botones.getBotones()[0].addActionListener(e -> crearCuentaDialog());
		add(botones, BorderLayout.SOUTH);

		cargarCuentas();
	}

	private void crearCuentaDialog() {
		String nombre = JOptionPane.showInputDialog(this, "Nombre de la cuenta:");
		if (nombre == null || nombre.isEmpty()) {
			return;
		}

		TipoCuenta tipo = (TipoCuenta) JOptionPane.showInputDialog(this, "Seleccione tipo de cuenta:", "Tipo Cuenta",
				JOptionPane.QUESTION_MESSAGE, null, TipoCuenta.values(), TipoCuenta.CAJA_AHORRO);
		if (tipo == null) {
			return;
		}

		Moneda moneda = (Moneda) JOptionPane.showInputDialog(this, "Seleccione moneda:", "Moneda",
				JOptionPane.QUESTION_MESSAGE, null, Moneda.values(), Moneda.ARS);
		if (moneda == null) {
			return;
		}

		double interesDiario = 0.001; // valor por defecto
		LocalDate fechaInicio = LocalDate.now();

		// Crear la cuenta con capital inicial = 0
		cuentaService.crearCuenta(usuario, nombre, tipo, moneda, interesDiario, fechaInicio);

		cargarCuentas();
	}

	public void cargarCuentas() {
		modeloCuentas.clear();
		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(usuario);

		for (Cuenta c : cuentas) {
			// calcular saldo actual con movimientoService
			c.getSaldo();
			modeloCuentas.addElement(c); // toString() de Cuenta mostrará nombre + moneda + saldo
		}

		if (!cuentas.isEmpty()) {
			listaCuentas.setSelectedIndex(0);
		}
	}

	public void setCuentaSeleccionadaListener(Consumer<Cuenta> listener) {
		this.cuentaSeleccionadaListener = listener;
	}
}