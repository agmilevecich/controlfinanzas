package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.dao.GastoDAO;
import ar.com.controlfinanzas.model.Gasto;

public class PanelGastos extends JPanel {

	private JTextField txtDescripcion;
	private JTextField txtMonto;
	private JComboBox<String> cbCategoria;
	private JTable tableGastos;
	private DefaultTableModel tableModel;
	private GastoDAO gastoDAO = new GastoDAO();

	private JPanel panelGraficos; // contendrá los dos gráficos
	private PanelResumenFinanciero panelResumen;

	public PanelGastos() {
		inicializarPanel();
		cargarGastos();
		actualizarGraficos();
	}

	public PanelGastos(PanelResumenFinanciero panelResumen) {
		this();
		this.panelResumen = panelResumen;
	}

	private void inicializarPanel() {
		this.setLayout(new BorderLayout());

		// --- Formulario y tabla en panelTabla ---
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
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tableGastos = new JTable(tableModel);
		JScrollPane scrollTabla = new JScrollPane(tableGastos);

		JPanel panelTabla = new JPanel(new BorderLayout());
		panelTabla.add(panelFormulario, BorderLayout.NORTH);
		panelTabla.add(scrollTabla, BorderLayout.CENTER);

		// --- Panel de gráficos ---
		panelGraficos = new JPanel(new GridLayout(2, 1, 5, 5));

		// --- Split pane para tabla y gráficos ---
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTabla, panelGraficos);
		splitPane.setResizeWeight(0.5); // 50%-50% al redimensionar
		splitPane.setContinuousLayout(true); // actualiza mientras movés el divider
		splitPane.setOneTouchExpandable(true); // colapsar paneles con un click
		panelTabla.setMinimumSize(new Dimension(400, 300));
		panelGraficos.setMinimumSize(new Dimension(400, 300));
		splitPane.setDividerLocation(0.5); // divider inicial en la mitad
		this.add(splitPane, BorderLayout.CENTER);

		// Acción botón agregar
		btnAgregar.addActionListener(e -> agregarGasto());
	}

	private void agregarGasto() {
		String descripcion = txtDescripcion.getText().trim();
		String montoStr = txtMonto.getText().trim();
		String categoria = (String) cbCategoria.getSelectedItem();

		if (descripcion.isEmpty() || montoStr.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		double monto;
		try {
			monto = Double.parseDouble(montoStr);
			if (monto <= 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Monto inválido", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Gasto gasto = new Gasto(LocalDate.now(), descripcion, monto, categoria);

		try {
			gastoDAO.guardarGasto(gasto);
			limpiarFormulario();
			cargarGastos();
			actualizarGraficos();
			if (panelResumen != null) {
				panelResumen.actualizarResumen();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar gasto", "Error", JOptionPane.ERROR_MESSAGE);
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
			List<Gasto> gastos = gastoDAO.listarGastos();
			for (Gasto g : gastos) {
				BigDecimal bdMonto = new BigDecimal(g.getMonto()).setScale(2, RoundingMode.HALF_UP);
				tableModel.addRow(new Object[] { g.getId(), g.getFecha(), g.getDescripcion(), bdMonto.doubleValue(),
						g.getCategoria() });
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
			List<Gasto> gastos = gastoDAO.listarGastos();
			DefaultPieDataset dataset = new DefaultPieDataset();

			Map<String, Double> totalPorCategoria = new HashMap<>();
			for (Gasto g : gastos) {
				double sumaAnterior = totalPorCategoria.getOrDefault(g.getCategoria(), 0.0);
				double sumaNueva = sumaAnterior + g.getMonto();
				BigDecimal bd = new BigDecimal(sumaNueva).setScale(2, RoundingMode.HALF_UP);
				totalPorCategoria.put(g.getCategoria(), bd.doubleValue());
			}

			for (Map.Entry<String, Double> entry : totalPorCategoria.entrySet()) {
				dataset.setValue(entry.getKey(), entry.getValue());
			}

			JFreeChart chart = ChartFactory.createPieChart("Gastos por Categoría", dataset, true, true, false);

			PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator("{0}: {1}");
			PiePlot piePlot = (PiePlot) chart.getPlot();
			piePlot.setLabelGenerator(labelGenerator);

			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setPreferredSize(new Dimension(400, 300));
			panelGraficos.add(chartPanel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void actualizarGraficoBarras() {
		try {
			List<Gasto> gastos = gastoDAO.listarGastos();
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			Map<Integer, Double> totalPorMes = new HashMap<>();

			for (Gasto g : gastos) {
				int mes = g.getFecha().getMonthValue();
				double sumaAnterior = totalPorMes.getOrDefault(mes, 0.0);
				double sumaNueva = sumaAnterior + g.getMonto();
				BigDecimal bd = new BigDecimal(sumaNueva).setScale(2, RoundingMode.HALF_UP);
				totalPorMes.put(mes, bd.doubleValue());
			}

			for (Map.Entry<Integer, Double> entry : totalPorMes.entrySet()) {
				String nombreMes = java.time.Month.of(entry.getKey()).getDisplayName(TextStyle.SHORT,
						Locale.getDefault());
				dataset.addValue(entry.getValue(), "Gastos", nombreMes);
			}

			JFreeChart chart = ChartFactory.createBarChart("Gastos Mensuales", "Mes", "Monto", dataset);

			CategoryPlot plot = chart.getCategoryPlot();
			BarRenderer renderer = (BarRenderer) plot.getRenderer();
			renderer.setSeriesPaint(0, new Color(79, 129, 189));

			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setPreferredSize(new Dimension(400, 300));
			panelGraficos.add(chartPanel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
