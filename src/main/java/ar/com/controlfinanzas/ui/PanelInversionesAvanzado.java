package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.controller.InversionController;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.service.AlertaService;
import ar.com.controlfinanzas.service.PosicionService;
import ar.com.controlfinanzas.ui.inversion.PanelIngresosMensuales;
import ar.com.controlfinanzas.ui.inversion.PanelVencimientos;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelInversionesAvanzado extends JPanel {

	private JTable tabla;
	private DefaultTableModel tablaModel;
	private final InversionController inversionController;

	private JComboBox<TipoInversion> cbTipoInversion;
	private JComboBox<Moneda> cbMoneda;
	private JComboBox<TipoActivo> cbTipoActivo;

	private JTextField txtDescripcion;
	private JTextField txtCapital;
	private JTextField txtRendimiento;
	private JTextField txtCantidad;
	private JTextField txtPrecioUnitario;
	private JTextField txtCryptoTipo;
	private JTextField txtBroker;

	private DatePicker dpFechaInicio;
	private DatePicker dpFechaVencimiento;

	private PanelBotones botones = new PanelBotones();
	private PanelDistribucion panelDistribucion;
	private PanelPosiciones panelPosiciones;
	private PanelIngresosMensuales panelIngresoMensuales;
	private PanelVencimientos panelVencimientos;

	public PanelInversionesAvanzado(InversionController inversionController) {
		this.inversionController = inversionController;

		panelDistribucion = new PanelDistribucion(inversionController);
		panelPosiciones = new PanelPosiciones(new PosicionService());
		panelPosiciones.refrescar(MainApp.getUsuarioActivo().getUsuarioID());
		panelIngresoMensuales = new PanelIngresosMensuales();
		panelVencimientos = new PanelVencimientos();
		inicializarPanel();

		inversionController.addListener(() -> cargarInversiones());
		cargarInversiones();
	}

	private void inicializarPanel() {

		setLayout(new BorderLayout());

		JPanel panelForm = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panelForm.add(new JLabel("Tipo Activo:"), gbc);
		cbTipoActivo = new JComboBox<>(TipoActivo.values());
		cbTipoActivo.addActionListener(e -> {
			filtrarTipoInversion();
		});
		gbc.gridx = 1;
		panelForm.add(cbTipoActivo, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Tipo Inversión:"), gbc);
		cbTipoInversion = new JComboBox<>();
		gbc.gridx = 1;
		panelForm.add(cbTipoInversion, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Moneda:"), gbc);
		cbMoneda = new JComboBox<>(Moneda.values());
		gbc.gridx = 1;
		panelForm.add(cbMoneda, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Descripción:"), gbc);
		gbc.gridx = 1;
		txtDescripcion = new JTextField(15);
		panelForm.add(txtDescripcion, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Capital inicial:"), gbc);
		gbc.gridx = 1;
		txtCapital = new JTextField(10);
		panelForm.add(txtCapital, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Rendimiento (%):"), gbc);
		gbc.gridx = 1;
		txtRendimiento = new JTextField(5);
		panelForm.add(txtRendimiento, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		panelForm.add(new JLabel("Fecha inicio:"), gbc);
		gbc.gridx = 3;
		DatePickerSettings startSettings = new DatePickerSettings();
		startSettings.setFirstDayOfWeek(DayOfWeek.SUNDAY);
		dpFechaInicio = new DatePicker(startSettings);
		panelForm.add(dpFechaInicio, gbc);

		gbc.gridx = 2;
		gbc.gridy++;
		panelForm.add(new JLabel("Fecha vencimiento:"), gbc);
		gbc.gridx = 3;
		DatePickerSettings endSettings = new DatePickerSettings();
		endSettings.setFirstDayOfWeek(DayOfWeek.SUNDAY);
		dpFechaVencimiento = new DatePicker(endSettings);
		panelForm.add(dpFechaVencimiento, gbc);

		gbc.gridx = 2;
		gbc.gridy++;
		panelForm.add(new JLabel("Cantidad:"), gbc);
		gbc.gridx = 3;
		txtCantidad = new JTextField(10);
		panelForm.add(txtCantidad, gbc);

		gbc.gridx = 2;
		gbc.gridy++;
		panelForm.add(new JLabel("Precio unitario:"), gbc);
		gbc.gridx = 3;
		txtPrecioUnitario = new JTextField(10);
		panelForm.add(txtPrecioUnitario, gbc);

		gbc.gridx = 2;
		gbc.gridy++;
		panelForm.add(new JLabel("Tipo Cripto:"), gbc);
		gbc.gridx = 3;
		txtCryptoTipo = new JTextField(10);
		panelForm.add(txtCryptoTipo, gbc);

		gbc.gridx = 2;
		gbc.gridy++;
		panelForm.add(new JLabel("Broker / Exchange:"), gbc);
		gbc.gridx = 3;
		txtBroker = new JTextField(15);
		panelForm.add(txtBroker, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 4;
		panelForm.add(botones, gbc);

		botones.getBotones()[0].addActionListener(e -> agregarInversion());

		add(panelForm, BorderLayout.NORTH);

		tablaModel = new DefaultTableModel(
				new Object[] { "ID", "Tipo Activo", "Tipo Inversión", "Moneda", "Descripción", "Capital", "Rendimiento",
						"Inicio", "Vencimiento", "Cantidad", "Precio", "Cripto", "Broker" },
				0);

		tabla = new JTable(tablaModel);
		tabla.removeColumn(tabla.getColumnModel().getColumn(0));

		JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tabla),
				panelDistribucion);
		splitHorizontal.setResizeWeight(0.6);
		splitHorizontal.setContinuousLayout(true);
		splitHorizontal.setOneTouchExpandable(true);

		JPanel panelSur = new JPanel(new GridLayout(1, 3));
		panelSur.add(panelPosiciones);
		panelSur.add(panelIngresoMensuales);
		panelSur.add(panelVencimientos);

		JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitHorizontal, panelSur);
		splitVertical.setResizeWeight(0.7);
		splitVertical.setContinuousLayout(true);
		splitVertical.setOneTouchExpandable(true);

		add(splitVertical, BorderLayout.CENTER);
	}

	private void filtrarTipoInversion() {
		TipoActivo activo = (TipoActivo) cbTipoActivo.getSelectedItem();
		cbTipoInversion.removeAllItems();
		if (activo == null) {
			return;
		}

		for (TipoInversion t : TipoInversion.values()) {
			if (t.permite(activo)) {
				cbTipoInversion.addItem(t);
			}
		}
	}

	private void agregarInversion() {

		try {

			if (!validarDatos()) {
				return;
			}

			TipoActivo tipoActivo = (TipoActivo) cbTipoActivo.getSelectedItem();

			BigDecimal capital = txtCapital.getText().isEmpty() ? BigDecimal.ZERO
					: NumeroUtils.parse(txtCapital.getText());

			Inversion inv = new Inversion(tipoActivo, (TipoInversion) cbTipoInversion.getSelectedItem(),
					(Moneda) cbMoneda.getSelectedItem(), txtDescripcion.getText().trim(), capital,
					NumeroUtils.parse(txtRendimiento.getText()), dpFechaInicio.getDate(), dpFechaVencimiento.getDate());

			if (!usaMonto(tipoActivo)) {
				inv.setCantidad(NumeroUtils.parse(txtCantidad.getText()));
				inv.setPrecioUnitario(NumeroUtils.parse(txtPrecioUnitario.getText()));
			}

			inv.setCryptoTipo(txtCryptoTipo.getText().trim().toUpperCase());
			inv.setBroker(txtBroker.getText().trim());
			inv.setUsuario(MainApp.getUsuarioActivo());

			inversionController.agregarInversion(inv);

			panelPosiciones.refrescar(MainApp.getUsuarioActivo().getUsuarioID());
			limpiarCampos();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar inversión");
		}
	}

	private void cargarInversiones() {
		tablaModel.setRowCount(0);
		List<Inversion> inversiones = inversionController.getInversiones();

		for (Inversion inv : inversiones) {
			tablaModel.addRow(new Object[] { inv.getId(), inv.getTipoActivo(), inv.getTipoInversion(), inv.getMoneda(),
					inv.getDescripcion(), inv.getCapitalInicial(), inv.getTasaAnual(), inv.getFechaInicio(),
					inv.getFechaVencimiento(), inv.getCantidad(), inv.getPrecioUnitario(), inv.getCryptoTipo(),
					inv.getBroker() });
		}

		panelIngresoMensuales.refrescar(inversiones);
		AlertaService alertaService = new AlertaService();

		List<Alerta> alertas = alertaService.generarAlertasInversiones(inversionController.getInversiones());
		panelVencimientos.refrescar(inversiones.stream().filter(i -> i.getFechaVencimiento() != null).toList());
	}

	private void limpiarCampos() {
		txtDescripcion.setText("");
		txtCapital.setText("");
		txtRendimiento.setText("");
		txtCantidad.setText("");
		txtPrecioUnitario.setText("");
		txtCryptoTipo.setText("");
		txtBroker.setText("");
		dpFechaInicio.clear();
		dpFechaVencimiento.clear();
	}

	private boolean usaMonto(TipoActivo tipo) {
		return tipo == TipoActivo.EFECTIVO || tipo == TipoActivo.DEUDA_CORTO_PLAZO;
	}

	private boolean validarDatos() {

		TipoActivo tipo = (TipoActivo) cbTipoActivo.getSelectedItem();

		if (tipo == null) {
			JOptionPane.showMessageDialog(this, "Seleccione tipo activo");
			return false;
		}

		if (txtDescripcion.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Ingrese descripción");
			return false;
		}

		if (txtRendimiento.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Ingrese rendimiento");
			return false;
		}

		if (dpFechaInicio.getDate() == null) {
			JOptionPane.showMessageDialog(this, "Ingrese fecha inicio");
			return false;
		}

		boolean monto = usaMonto(tipo);

		if (monto) {
			if (txtCapital.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Ingrese capital inicial");
				return false;
			}
		} else {
			if (txtCantidad.getText().trim().isEmpty() || txtPrecioUnitario.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Ingrese cantidad y precio");
				return false;
			}
		}

		return true;
	}

}