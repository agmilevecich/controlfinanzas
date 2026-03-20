package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.util.ChartUtils;

public class PanelResumenGastos extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final MovimientoService movimientoService;

	private JTable tabla;
	private DefaultTableModel model;
	private JPanel panelGrafico;

	private DefaultPieDataset<String> datasetGastos;
	private ChartPanel chartPanelGastos;

	// 🔴 datos en memoria (clave)
	private Map<CategoriaGasto, BigDecimal> totales = new EnumMap<>(CategoriaGasto.class);

	public PanelResumenGastos(MovimientoService movimientoService) {
		this.movimientoService = movimientoService;

		setLayout(new BorderLayout());

		model = new DefaultTableModel(new Object[] { "Categoría", "Total" }, 0);
		tabla = new JTable(model);
		add(new JScrollPane(tabla), BorderLayout.CENTER);

		panelGrafico = new JPanel(new BorderLayout());
		panelGrafico.setPreferredSize(new Dimension(0, 300));

		datasetGastos = new DefaultPieDataset();

		JFreeChart chart = ChartFactory.createPieChart("Gastos por Categoría", datasetGastos, true, true, false);
		ChartUtils.aplicarEstiloBasico(chart);

		PiePlot plot = (PiePlot) chart.getPlot();
		Map<CategoriaGasto, Color> colores = ChartUtils.coloresCategorias();
		for (Map.Entry<CategoriaGasto, Color> entry : colores.entrySet()) {
			plot.setSectionPaint(entry.getKey().name(), entry.getValue());
		}

		chartPanelGastos = new ChartPanel(chart);
		chartPanelGastos.setPreferredSize(new Dimension(600, 300));
		panelGrafico.add(chartPanelGastos, BorderLayout.CENTER);

		add(panelGrafico, BorderLayout.SOUTH);

		refrescar(SesionUsuario.getUsuarioActual().getUsuarioID());
	}

	// =========================
	// CARGA DATOS (BD)
	// =========================
	private void cargarDatos(Integer usuarioId) {

		totales.clear();

		YearMonth mesActual = YearMonth.now();
		LocalDate inicio = mesActual.atDay(1);
		LocalDate fin = mesActual.atEndOfMonth();

		List<Movimiento> movimientos = movimientoService.listarPorUsuarioYPeriodo(inicio, fin);

		for (Movimiento m : movimientos) {

			if (m.getCategoria() == null || m.getMonto() == null) {
				continue;
			}

			totales.put(m.getCategoria(), totales.getOrDefault(m.getCategoria(), BigDecimal.ZERO).add(m.getMonto()));
		}
	}

	// =========================
	// PINTA UI
	// =========================
	private void actualizarGraficos() {

		// TABLA
		model.setRowCount(0);

		for (var entry : totales.entrySet()) {
			model.addRow(new Object[] { entry.getKey().name(), entry.getValue() });
		}

		// GRÁFICO
		datasetGastos.clear();

		for (var entry : totales.entrySet()) {
			datasetGastos.setValue(entry.getKey().name(), entry.getValue());
		}
	}

	// =========================
	// MÉTODO PÚBLICO
	// =========================
	public void refrescar(Integer usuarioId) {
		cargarDatos(usuarioId);
		actualizarGraficos();
		revalidate();
		repaint();
	}
}