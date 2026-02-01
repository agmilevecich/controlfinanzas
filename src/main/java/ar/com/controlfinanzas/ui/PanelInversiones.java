package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import ar.com.controlfinanzas.dao.InversionDAO;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;

public class PanelInversiones extends JPanel {

	private JTable tabla;
	private DefaultTableModel tablaModel;
	private InversionDAO inversionDAO;

	private JComboBox<TipoInversion> cbTipo;
	private JComboBox<Moneda> cbMoneda;
	private DatePicker dpFechaInicio;
	private DatePicker dpFechaVencimiento;

	private PanelVencimientos panelVencimientos;
	private PanelResumenFinanciero panelResumen;

	public PanelInversiones(PanelVencimientos panelVencimientos, PanelResumenFinanciero panelResumen) {
		this.panelVencimientos = panelVencimientos;
		this.panelResumen = panelResumen;
		inversionDAO = new InversionDAO();

		inicializarPanel();
		cargarInversiones();
	}

	private void inicializarPanel() {
		setLayout(new BorderLayout());

		// Panel de formulario
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
		javax.swing.JTextField txtDescripcion = new javax.swing.JTextField(15);
		panelForm.add(txtDescripcion, gbc);

		// Capital inicial
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Capital inicial:"), gbc);
		gbc.gridx = 1;
		javax.swing.JTextField txtCapital = new javax.swing.JTextField(10);
		panelForm.add(txtCapital, gbc);

		// Rendimiento esperado
		gbc.gridx = 0;
		gbc.gridy++;
		panelForm.add(new JLabel("Rendimiento (%):"), gbc);
		gbc.gridx = 1;
		javax.swing.JTextField txtRendimiento = new javax.swing.JTextField(5);
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

		// Botón agregar
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		JButton btnAgregar = new JButton("Agregar inversión");
		panelForm.add(btnAgregar, gbc);

		btnAgregar.addActionListener(e -> {
			try {
				String descripcion = txtDescripcion.getText().trim();
				String capitalStr = txtCapital.getText().trim();
				String rendimientoStr = txtRendimiento.getText().trim();
				TipoInversion tipo = (TipoInversion) cbTipo.getSelectedItem();
				Moneda moneda = (Moneda) cbMoneda.getSelectedItem();
				LocalDate fechaInicio = dpFechaInicio.getDate();
				LocalDate fechaVencimiento = dpFechaVencimiento.getDate();

				if (descripcion.isEmpty() || capitalStr.isEmpty() || rendimientoStr.isEmpty() || fechaInicio == null
						|| fechaVencimiento == null) {
					JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				BigDecimal capital = new BigDecimal(capitalStr);
				BigDecimal rendimiento = new BigDecimal(rendimientoStr);

				Inversion inv = new Inversion(tipo, moneda, descripcion, capital, rendimiento, fechaInicio,
						fechaVencimiento);
				inversionDAO.guardarInversion(inv);

				cargarInversiones();
				txtDescripcion.setText("");
				txtCapital.setText("");
				txtRendimiento.setText("");
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
		});

		add(panelForm, BorderLayout.NORTH);

		// Tabla
		tablaModel = new DefaultTableModel(
				new Object[] { "Tipo", "Moneda", "Descripción", "Capital", "Rendimiento", "Inicio", "Vencimiento" }, 0);
		tabla = new JTable(tablaModel);
		add(new JScrollPane(tabla), BorderLayout.CENTER);
	}

	private void cargarInversiones() {
		try {
			tablaModel.setRowCount(0); // limpiar
			List<Inversion> inversiones = inversionDAO.listarInversiones();
			for (Inversion inv : inversiones) {
				tablaModel.addRow(
						new Object[] { inv.getTipo(), inv.getMoneda(), inv.getDescripcion(), inv.getCapitalInicial(),
								inv.getRendimientoEsperado(), inv.getFechaInicio(), inv.getFechaVencimiento() });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
