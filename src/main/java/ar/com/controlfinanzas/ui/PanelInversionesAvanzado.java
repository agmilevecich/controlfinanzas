package ar.com.controlfinanzas.ui;

import java.awt.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import ar.com.controlfinanzas.dao.InversionDAO;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;

public class PanelInversionesAvanzado extends JPanel {

	private JTable tabla;
	private DefaultTableModel tablaModel;
	private InversionDAO inversionDAO;

	private JComboBox<TipoInversion> cbTipo;
	private JComboBox<Moneda> cbMoneda;

	private JTextField txtDescripcion, txtCapital, txtRendimiento, txtCantidad, txtPrecioUnitario, txtCryptoTipo,
			txtBroker;
	private DatePicker dpFechaInicio, dpFechaVencimiento;

	private PanelVencimientos panelVencimientos;
	private PanelResumenFinanciero panelResumen;

	public PanelInversionesAvanzado(PanelVencimientos panelVencimientos, PanelResumenFinanciero panelResumen) {
		this.panelVencimientos = panelVencimientos;
		this.panelResumen = panelResumen;
		inversionDAO = new InversionDAO();

		inicializarPanel();
		cargarInversiones();
	}

	private void inicializarPanel() {
		setLayout(new BorderLayout());

		JPanel panelForm = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Tipo
		gbc.gridx = 0;
		gbc.gridy = 0;
		panelForm.add(new JLabel("Tipo:"), gbc);
		cbTipo = new JComboBox<>(TipoInversion.values());
		gbc.gridx = 1;
		panelForm.add(cbTipo, gbc);

		// Moneda
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Moneda:"), gbc);
		cbMoneda = new JComboBox<>(Moneda.values());
		gbc.gridx = 1;
		panelForm.add(cbMoneda, gbc);

		// Descripción
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Descripción:"), gbc);
		gbc.gridx = 1;
		txtDescripcion = new JTextField(15);
		panelForm.add(txtDescripcion, gbc);

		// Capital
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Capital inicial:"), gbc);
		gbc.gridx = 1;
		txtCapital = new JTextField(10);
		panelForm.add(txtCapital, gbc);

		// Rendimiento
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Rendimiento (%):"), gbc);
		gbc.gridx = 1;
		txtRendimiento = new JTextField(5);
		panelForm.add(txtRendimiento, gbc);

		// Fecha inicio
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Fecha inicio:"), gbc);
		gbc.gridx = 1;
		DatePickerSettings startSettings = new DatePickerSettings();
		startSettings.setFirstDayOfWeek(DayOfWeek.SUNDAY);
		dpFechaInicio = new DatePicker(startSettings);
		panelForm.add(dpFechaInicio, gbc);

		// Fecha vencimiento
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Fecha vencimiento:"), gbc);
		gbc.gridx = 1;
		DatePickerSettings endSettings = new DatePickerSettings();
		endSettings.setFirstDayOfWeek(DayOfWeek.SUNDAY);
		dpFechaVencimiento = new DatePicker(endSettings);
		panelForm.add(dpFechaVencimiento, gbc);

		// Campos dinámicos adicionales
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Cantidad:"), gbc);
		gbc.gridx = 1;
		txtCantidad = new JTextField(10);
		panelForm.add(txtCantidad, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Precio unitario:"), gbc);
		gbc.gridx = 1;
		txtPrecioUnitario = new JTextField(10);
		panelForm.add(txtPrecioUnitario, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Tipo Cripto:"), gbc);
		gbc.gridx = 1;
		txtCryptoTipo = new JTextField(10);
		panelForm.add(txtCryptoTipo, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Broker/Exchange:"), gbc);
		gbc.gridx = 1;
		txtBroker = new JTextField(15);
		panelForm.add(txtBroker, gbc);

		// Botón agregar
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		JButton btnAgregar = new JButton("Agregar inversión");
		panelForm.add(btnAgregar, gbc);

		btnAgregar.addActionListener(e -> agregarInversion());

		add(panelForm, BorderLayout.NORTH);

		// Tabla
		tablaModel = new DefaultTableModel(new Object[] { "Tipo", "Moneda", "Descripción", "Capital", "Rendimiento",
				"Fecha Inicio", "Fecha Vencimiento", "Cantidad", "Precio", "Cripto", "Broker" }, 0);
		tabla = new JTable(tablaModel);
		add(new JScrollPane(tabla), BorderLayout.CENTER);

		// Ajuste dinámico de campos según tipo
		cbTipo.addActionListener(e -> ajustarCamposSegunTipo());
		ajustarCamposSegunTipo(); // inicial
	}

	private void ajustarCamposSegunTipo() {
        TipoInversion tipo = (TipoInversion) cbTipo.getSelectedItem();
        // Ocultar todo primero
        txtCantidad.setEnabled(false);
        txtPrecioUnitario.setEnabled(false);
        txtCryptoTipo.setEnabled(false);
        txtBroker.setEnabled(false);

        switch(tipo) {
            case PLAZO_FIJO_UVA -> txtCantidad.setEnabled(true); // cantidad UVA
            case ACCION, FONDO_COMUN_INVERSION -> { txtCantidad.setEnabled(true); txtPrecioUnitario.setEnabled(true); txtBroker.setEnabled(true); }
            case CRIPTOMONEDA -> { txtCantidad.setEnabled(true); txtCryptoTipo.setEnabled(true); txtBroker.setEnabled(true); }
            default -> { /* PF tradicional no necesita campos extra */ }
        }
    }

	private void agregarInversion() {
		try {
			String descripcion = txtDescripcion.getText().trim();
			String capitalStr = txtCapital.getText().trim();
			String rendimientoStr = txtRendimiento.getText().trim();
			String cantidadStr = txtCantidad.getText().trim();
			String precioStr = txtPrecioUnitario.getText().trim();
			String cryptoTipo = txtCryptoTipo.getText().trim();
			String broker = txtBroker.getText().trim();

			TipoInversion tipo = (TipoInversion) cbTipo.getSelectedItem();
			Moneda moneda = (Moneda) cbMoneda.getSelectedItem();
			LocalDate fechaInicio = dpFechaInicio.getDate();
			LocalDate fechaVencimiento = dpFechaVencimiento.getDate();

			if (descripcion.isEmpty() || capitalStr.isEmpty() || rendimientoStr.isEmpty() || fechaInicio == null
					|| fechaVencimiento == null) {
				JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			BigDecimal capital = new BigDecimal(capitalStr);
			BigDecimal rendimiento = new BigDecimal(rendimientoStr);
			BigDecimal cantidad = cantidadStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cantidadStr);
			BigDecimal precio = precioStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(precioStr);

			Inversion inv = new Inversion(tipo, moneda, descripcion, capital, rendimiento, fechaInicio,
					fechaVencimiento);
			inv.setCantidad(cantidad);
			inv.setPrecioUnitario(precio);
			inv.setCryptoTipo(cryptoTipo);
			inv.setBroker(broker);

			inversionDAO.guardarInversion(inv);

			cargarInversiones();

			// Limpiar campos
			txtDescripcion.setText("");
			txtCapital.setText("");
			txtRendimiento.setText("");
			txtCantidad.setText("");
			txtPrecioUnitario.setText("");
			txtCryptoTipo.setText("");
			txtBroker.setText("");
			dpFechaInicio.clear();
			dpFechaVencimiento.clear();

			// Actualizar paneles
			if (panelVencimientos != null) {
				panelVencimientos.actualizarInversiones(inversionDAO.listarInversiones());
			}
			if (panelResumen != null) {
				panelResumen.actualizarResumen();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar inversión", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void cargarInversiones() {
		try {
			tablaModel.setRowCount(0);
			List<Inversion> inversiones = inversionDAO.listarInversiones();
			for (Inversion inv : inversiones) {
				tablaModel.addRow(
						new Object[] { inv.getTipo(), inv.getMoneda(), inv.getDescripcion(), inv.getCapitalInicial(),
								inv.getRendimientoEsperado(), inv.getFechaInicio(), inv.getFechaVencimiento(),
								inv.getCantidad(), inv.getPrecioUnitario(), inv.getCryptoTipo(), inv.getBroker() });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
