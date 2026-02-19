package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
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
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.service.GastoService;

public class PanelResumenGastos extends JPanel {

	private final GastoService gastoService;

	private JTable tabla;
	private DefaultTableModel model;
	private JPanel panelGrafico;

	private DefaultPieDataset datasetGastos;
	private ChartPanel chartPanelGastos;

	public PanelResumenGastos(GastoService gastoService) {

		this.gastoService = gastoService;

		setLayout(new BorderLayout());

		model = new DefaultTableModel(new Object[] { "Categoría", "Total" }, 0);
		tabla = new JTable(model);

		add(new JScrollPane(tabla), BorderLayout.CENTER);

		panelGrafico = new JPanel(new BorderLayout());
		panelGrafico.setPreferredSize(new Dimension(0, 300));

		datasetGastos = new DefaultPieDataset();

		JFreeChart chart = ChartFactory.createPieChart("Gastos del mes por categoría", datasetGastos, true, true,
				false);

		chartPanelGastos = new ChartPanel(chart);
		panelGrafico.add(chartPanelGastos, BorderLayout.CENTER);

		add(panelGrafico, BorderLayout.SOUTH);

		actualizar();
	}

	public void actualizar() {

		model.setRowCount(0);
		datasetGastos.clear();

		try {

			Integer usuarioId = MainApp.getUsuarioActivo().getUsuarioID();

			YearMonth mesActual = YearMonth.now();

			LocalDate inicio = mesActual.atDay(1);
			LocalDate fin = mesActual.atEndOfMonth();

			List<Gasto> gastos = gastoService.listarPorUsuarioYPeriodo(usuarioId, inicio, fin);

			// 👉 SOLO categorías que existen en BD
			Map<CategoriaGasto, BigDecimal> totales = new EnumMap<>(CategoriaGasto.class);

			for (Gasto g : gastos) {

				if (g.getCategoria() == null || g.getMonto() == null) {
					continue;
				}

				totales.put(g.getCategoria(),
						totales.getOrDefault(g.getCategoria(), BigDecimal.ZERO).add(g.getMonto()));
			}

			// ===== TABLA + GRÁFICO =====
			for (Map.Entry<CategoriaGasto, BigDecimal> entry : totales.entrySet()) {

				model.addRow(new Object[] { entry.getKey().name(), entry.getValue() });

				datasetGastos.setValue(entry.getKey().name(), entry.getValue());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		revalidate();
		repaint();
	}
}
