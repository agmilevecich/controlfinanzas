package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

import javax.swing.JButton;
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
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.repository.InversionRepository;
import ar.com.controlfinanzas.service.PosicionService;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelInversionesAvanzado extends JPanel {

	private JTable tabla;
	private DefaultTableModel tablaModel;
	private final InversionController inversionController;

	private JComboBox<TipoInversion> cbTipoInversion;
	private JComboBox<Moneda> cbMoneda;

	private JTextField txtDescripcion;
	private JTextField txtCapital;
	private JTextField txtRendimiento;
	private JTextField txtCantidad;
	private JTextField txtPrecioUnitario;
	private JTextField txtCryptoTipo;
	private JTextField txtBroker;

	private DatePicker dpFechaInicio;
	private DatePicker dpFechaVencimiento;
	private JComboBox cbTipoActivo;

	private PanelBotones botones = new PanelBotones();
	private PanelDistribucion panelDistribucion;
	private JSplitPane split;
	private PanelPosiciones panelPosiciones;

	public PanelInversionesAvanzado(InversionController inversionController) {
		this.inversionController = inversionController;
		panelDistribucion = new PanelDistribucion(inversionController);

		panelPosiciones = new PanelPosiciones(new PosicionService(new InversionRepository()));
		panelPosiciones.refrescar(MainApp.getUsuarioActivo().getUsuarioID());

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
		panelForm.add(new JLabel("Tipo Activo: "), gbc);
		cbTipoActivo = new JComboBox<>(TipoActivo.values());
		cbTipoActivo.addActionListener(e -> filtrarTipoInversion());
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

		JButton[] boton = botones.getBotones();
		boton[0].addActionListener(e -> agregarInversion());

		add(panelForm, BorderLayout.NORTH);

		tablaModel = new DefaultTableModel(
				new Object[] { "ID", "Tipo Activo", "Tipo Inversión", "Moneda", "Descripción", "Capital", "Rendimiento",
						"Inicio", "Vencimiento", "Cantidad", "Precio", "Cripto", "Broker" },
				0);

		tabla = new JTable(tablaModel);
		tabla.removeColumn(tabla.getColumnModel().getColumn(0)); // ocultamos ID

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tabla), panelDistribucion);
		split.setResizeWeight(0.6);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);
		add(split, BorderLayout.CENTER);
		add(panelPosiciones, BorderLayout.SOUTH);
	}

	private void filtrarTipoInversion() {

		TipoActivo activoSeleccionado = (TipoActivo) cbTipoActivo.getSelectedItem();

		cbTipoInversion.removeAllItems();

		if (activoSeleccionado == null) {
			return;
		}

		for (TipoInversion tipo : TipoInversion.values()) {
			if (tipo.permite(activoSeleccionado)) {
				cbTipoInversion.addItem(tipo);
			}
		}
	}

	private void agregarInversion() {
		try {

			if (txtDescripcion.getText().trim().isEmpty() || txtCapital.getText().trim().isEmpty()
					|| txtRendimiento.getText().trim().isEmpty() || dpFechaInicio.getDate() == null
					|| dpFechaVencimiento.getDate() == null) {

				JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			Inversion inv = new Inversion((TipoActivo) cbTipoActivo.getSelectedItem(),
					(TipoInversion) cbTipoInversion.getSelectedItem(), (Moneda) cbMoneda.getSelectedItem(),
					txtDescripcion.getText().trim(), NumeroUtils.parse(txtCapital.getText()),
					NumeroUtils.parse(txtRendimiento.getText()), dpFechaInicio.getDate(), dpFechaVencimiento.getDate());

			inv.setCantidad(
					txtCantidad.getText().isEmpty() ? BigDecimal.ZERO : NumeroUtils.parse(txtCantidad.getText()));

			inv.setPrecioUnitario(txtPrecioUnitario.getText().isEmpty() ? BigDecimal.ZERO
					: NumeroUtils.parse(txtPrecioUnitario.getText()));

			inv.setCryptoTipo(txtCryptoTipo.getText().trim().toUpperCase());
			inv.setBroker(txtBroker.getText().trim());
			inv.setUsuario(MainApp.getUsuarioActivo());

			inversionController.agregarInversion(inv);
			panelPosiciones.refrescar(MainApp.getUsuarioActivo().getUsuarioID());
			limpiarCampos();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar inversión", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void cargarInversiones() {

		tablaModel.setRowCount(0);

		List<Inversion> inversiones = inversionController.getInversiones();

		for (Inversion inv : inversiones) {
			tablaModel.addRow(new Object[] { inv.getId(), inv.getTipoActivo(), inv.getTipoInversion(), inv.getMoneda(),
					inv.getDescripcion(), inv.getCapitalInicial(), inv.getRendimientoEsperado(), inv.getFechaInicio(),
					inv.getFechaVencimiento(), inv.getCantidad(), inv.getPrecioUnitario(), inv.getCryptoTipo(),
					inv.getBroker() });
		}
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
}
