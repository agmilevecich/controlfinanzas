package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.service.GastoService;

public class PanelGastos extends JPanel {

	private JTextField txtDescripcion;
	private JTextField txtMonto;
	private JComboBox<String> cbCategoria;
	private JTable tableGastos;
	private DefaultTableModel tableModel;

	private JPanel panelGraficos;

	private final GastoService gastoService;
	private PanelResumenFinanciero panelResumen;

	public PanelGastos(GastoService gastoService, PanelResumenFinanciero panelResumen) {
		this.gastoService = gastoService;
		this.panelResumen = panelResumen;
		inicializarPanel();
		cargarGastos();
		actualizarGraficos();
	}

	private void inicializarPanel() {

		this.setLayout(new BorderLayout());

		JPanel panelFormulario = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		txtDescripcion = new JTextField(15);
		txtMonto = new JTextField(8);
		cbCategoria = new JComboBox<>(new String[] { "Alimentación", "Transporte", "Hogar", "Ocio", "Otros" });
		JButton btnAgregar = new JButton("Agregar gasto");

		gbc.gridx = 0;
		gbc.gridy = 0;
		panelFormulario.add(new JLabel("Descripción:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(txtDescripcion, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panelFormulario.add(new JLabel("Monto:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(txtMonto, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panelFormulario.add(new JLabel("Categoría:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(cbCategoria, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		panelFormulario.add(btnAgregar, gbc);

		tableModel = new DefaultTableModel(new String[] { "ID", "Fecha", "Descripción", "Monto", "Categoría" }, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		tableGastos = new JTable(tableModel);

		JPanel panelTabla = new JPanel(new BorderLayout());
		panelTabla.add(panelFormulario, BorderLayout.NORTH);
		panelTabla.add(new JScrollPane(tableGastos), BorderLayout.CENTER);

		panelGraficos = new JPanel(new GridLayout(2, 1, 5, 5));

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTabla, panelGraficos);

		split.setResizeWeight(0.5);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);

		this.add(split, BorderLayout.CENTER);

		btnAgregar.addActionListener(e -> agregarGasto());
	}

	private void agregarGasto() {

		try {
			String descripcion = txtDescripcion.getText().trim();
			String montoStr = txtMonto.getText().trim();
			String categoria = (String) cbCategoria.getSelectedItem();

			if (descripcion.isEmpty() || montoStr.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Complete todos los campos");
				return;
			}

			BigDecimal monto = new BigDecimal(montoStr);

			Gasto gasto = new Gasto();
			gasto.setFecha(LocalDate.now());
			gasto.setDescripcion(descripcion);
			gasto.setMonto(monto);
			gasto.setCategoria(categoria);
			gasto.setUsuario(MainApp.getUsuarioActivo());

			gastoService.guardar(gasto);

			limpiarFormulario();
			cargarGastos();
			actualizarGraficos();

			if (panelResumen != null) {
				panelResumen.actualizarResumen();
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar gasto");
		}
	}

	private void limpiarFormulario() {
		txtDescripcion.setText("");
		txtMonto.setText("");
		cbCategoria.setSelectedIndex(0);
	}

	private void cargarGastos() {
		tableModel.setRowCount(0);
		try {
			List<Gasto> gastos = gastoService.listarPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());
			for (Gasto g : gastos) {
				tableModel.addRow(new Object[] { g.getId(), g.getFecha(), g.getDescripcion(),
						g.getMonto().setScale(2, RoundingMode.HALF_UP), g.getCategoria() });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void actualizarGraficos() {
		panelGraficos.removeAll();
		actualizarGraficoPie();
		actualizarGraficoBarras();
		panelGraficos.revalidate();
		panelGraficos.repaint();
	}

	private void actualizarGraficoPie() {
		try {
			List<Gasto> gastos = gastoService.listarPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());
			DefaultPieDataset dataset = new DefaultPieDataset();

			Map<String, BigDecimal> totales = new HashMap<>();

			for (Gasto g : gastos) {
				totales.put(g.getCategoria(),
						totales.getOrDefault(g.getCategoria(), BigDecimal.ZERO).add(g.getMonto()));
			}

			for (Map.Entry<String, BigDecimal> e : totales.entrySet()) {
				dataset.setValue(e.getKey(), e.getValue());
			}

			JFreeChart chart = ChartFactory.createPieChart("Gastos por Categoría", dataset, true, true, false);

			panelGraficos.add(new ChartPanel(chart));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void actualizarGraficoBarras() {
		try {
			List<Gasto> gastos = gastoService.listarPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			Map<Integer, BigDecimal> totales = new HashMap<>();

			for (Gasto g : gastos) {
				int mes = g.getFecha().getMonthValue();
				totales.put(mes, totales.getOrDefault(mes, BigDecimal.ZERO).add(g.getMonto()));
			}

			for (Map.Entry<Integer, BigDecimal> e : totales.entrySet()) {
				String nombreMes = java.time.Month.of(e.getKey()).getDisplayName(TextStyle.SHORT, Locale.getDefault());

				dataset.addValue(e.getValue(), "Gastos", nombreMes);
			}

			JFreeChart chart = ChartFactory.createBarChart("Gastos Mensuales", "Mes", "Monto", dataset);

			panelGraficos.add(new ChartPanel(chart));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
