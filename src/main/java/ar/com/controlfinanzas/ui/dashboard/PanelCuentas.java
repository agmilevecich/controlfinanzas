package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.math.BigDecimal;
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
import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.ui.PanelBotones;

public class PanelCuentas extends JPanel {

	private Usuario usuario;
	private CuentaService cuentaService;
	private MovimientoService movimientoService;
	private BancoService bancoService;

	private JList<Cuenta> listaCuentas;
	private DefaultListModel<Cuenta> modeloCuentas;

	private Consumer<Cuenta> cuentaSeleccionadaListener;

	private PanelBotones botones = new PanelBotones();

	private Runnable actualizarCuentas;

	private boolean cuentaNueva;

	public PanelCuentas(Usuario usuario, CuentaService cuentaService, MovimientoService movimientoService,
			BancoService bancoService) {

		this.usuario = usuario;
		this.cuentaService = cuentaService;
		this.movimientoService = movimientoService;
		this.bancoService = bancoService;

		setLayout(new BorderLayout());

		modeloCuentas = new DefaultListModel<>();
		listaCuentas = new JList<>(modeloCuentas);
		listaCuentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(listaCuentas), BorderLayout.CENTER);

		// Listener selección de cuenta
		listaCuentas.addListSelectionListener(e -> {
			int index = listaCuentas.getSelectedIndex();

			if (index >= 0 && cuentaSeleccionadaListener != null) {
				Cuenta cuenta = modeloCuentas.getElementAt(index);
				cuentaSeleccionadaListener.accept(cuenta);
			}
		});

		// Botón crear cuenta
		botones.getBotones()[0].setText("Crear Cuenta");
		botones.getBotones()[0].addActionListener(e -> crearCuentaDialog());

		add(botones, BorderLayout.SOUTH);

		cargarCuentas();
	}

	private void crearCuentaDialog() {

		List<Banco> bancos = bancoService.getBancosUsuario(usuario);

		if (bancos.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Primero debe crear un banco.", "Sin bancos",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Banco banco = (Banco) JOptionPane.showInputDialog(this, "Seleccione banco:", "Banco",
				JOptionPane.QUESTION_MESSAGE, null, bancos.toArray(), bancos.get(0));

		if (banco == null) {
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

		double interesDiario = 0.001;
		LocalDate fechaInicio = LocalDate.now();

		cuentaService.crearCuenta(usuario, banco.getNombre() + " " + tipo + " " + moneda, // nombre automático
				banco, "Capital inicial", tipo, moneda, BigDecimal.ZERO, interesDiario, fechaInicio);

		cuentaNueva = true;
		cargarCuentas();
		cuentaNueva = false;

		if (actualizarCuentas != null) {
			actualizarCuentas.run();
		}
	}

	public void setActualizarCuentas(Runnable actualizarCuentas) {
		this.actualizarCuentas = actualizarCuentas;
	}

	public void cargarCuentas() {

		Cuenta seleccionadaAntes = listaCuentas.getSelectedValue();

		modeloCuentas.clear();

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(usuario);

		for (Cuenta c : cuentas) {
			modeloCuentas.addElement(c);
		}

		if (cuentaNueva) {

			listaCuentas.setSelectedIndex(listaCuentas.getModel().getSize() - 1);

		} else if (seleccionadaAntes != null && cuentas.contains(seleccionadaAntes)) {

			listaCuentas.setSelectedValue(seleccionadaAntes, true);

		} else if (!cuentas.isEmpty()) {

			listaCuentas.setSelectedIndex(0);
		}
	}

	public void setCuentaSeleccionadaListener(Consumer<Cuenta> listener) {
		this.cuentaSeleccionadaListener = listener;
	}

}
