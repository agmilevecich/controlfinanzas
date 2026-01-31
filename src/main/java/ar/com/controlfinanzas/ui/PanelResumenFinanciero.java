package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.dao.GastoDAO;
import ar.com.controlfinanzas.dao.InversionDAO;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.TipoInversion;

public class PanelResumenFinanciero extends JPanel {

	private GastoDAO gastoDAO = new GastoDAO();
	private InversionDAO inversionDAO = new InversionDAO();

	private JLabel lblTotalGastos;
	private JLabel lblTotalInversiones;
	private JLabel lblSaldoNeto;

	private JPanel panelGraficos;

	public PanelResumenFinanciero() {
		inicializarPanel();
		actualizarResumen();
	}

	private void inicializarPanel() {
		this.setLayout(new BorderLayout());

		// --- Panel resumen numérico ---
		JPanel panelNumerico = new JPanel(new GridLayout(1, 3, 10, 10));
		lblTotalInversiones = new JLabel("Total Inversiones: $0");
		lblTotalGastos = new JLabel("Total Gastos: $0");
		lblSaldoNeto = new JLabel("Saldo Neto: $0");
		panelNumerico.add(lblTotalInversiones);
		panelNumerico.add(lblTotalGastos);
		panelNumerico.add(lblSaldoNeto);

		// --- Panel gráficos ---
		panelGraficos = new JPanel(new GridLayout(1, 2, 10, 10));

		this.add(panelNumerico, BorderLayout.NORTH);
		this.add(panelGraficos, BorderLayout.CENTER);
	}

	public void actualizarResumen() {
		panelGraficos.removeAll();

		// --- Totales ---
		BigDecimal totalGastos = BigDecimal.ZERO;
		BigDecimal totalInversiones = BigDecimal.ZERO;

		try {
			List<Gasto> gastos = gastoDAO.listarGastos();
			for (Gasto g : gastos) {
				totalGastos = totalGastos.add(BigDecimal.valueOf(g.getMonto()));
			}

			List<Inversion> inversiones = inversionDAO.listarInversiones();
			for (Inversion inv : inversiones) {
				totalInversiones = totalInversiones.add(inv.getCapitalInicial());
			}

			lblTotalGastos.setText("Total Gastos: $" + totalGastos.setScale(2, BigDecimal.ROUND_HALF_UP));
			lblTotalInversiones
					.setText("Total Inversiones: $" + totalInversiones.setScale(2, BigDecimal.ROUND_HALF_UP));
			lblSaldoNeto.setText(
					"Saldo Neto: $" + totalInversiones.subtract(totalGastos).setScale(2, BigDecimal.ROUND_HALF_UP));

		} catch (Exception e) {
			e.printStackTrace();
		}

		// --- Gráfico de barras de inversiones por tipo ---
		DefaultCategoryDataset datasetInv = new DefaultCategoryDataset();
		try {
			List<Inversion> inversiones = inversionDAO.listarInversiones();
			Map<TipoInversion, BigDecimal> sumaPorTipo = new HashMap<>();
			for (Inversion inv : inversiones) {
				sumaPorTipo.put(inv.getTipo(),
						sumaPorTipo.getOrDefault(inv.getTipo(), BigDecimal.ZERO).add(inv.getCapitalInicial()));
			}
			for (Map.Entry<TipoInversion, BigDecimal> entry : sumaPorTipo.entrySet()) {
				datasetInv.addValue(entry.getValue(), "Inversiones", entry.getKey().name());
			}

			JFreeChart chartInv = ChartFactory.createBarChart("Inversiones por Tipo", "Tipo", "Monto", datasetInv);

			ChartPanel panelChartInv = new ChartPanel(chartInv);
			panelGraficos.add(panelChartInv);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// --- Gráfico de pastel de gastos por categoría ---
		DefaultPieDataset datasetGastos = new DefaultPieDataset();
		try {
			List<Gasto> gastos = gastoDAO.listarGastos();
			Map<String, BigDecimal> sumaPorCategoria = new HashMap<>();
			for (Gasto g : gastos) {
				sumaPorCategoria.put(g.getCategoria(), sumaPorCategoria.getOrDefault(g.getCategoria(), BigDecimal.ZERO)
						.add(BigDecimal.valueOf(g.getMonto())));
			}
			for (Map.Entry<String, BigDecimal> entry : sumaPorCategoria.entrySet()) {
				datasetGastos.setValue(entry.getKey(), entry.getValue());
			}

			JFreeChart chartGastos = ChartFactory.createPieChart("Gastos por Categoría", datasetGastos, true, true,
					false);

			ChartPanel panelChartGastos = new ChartPanel(chartGastos);
			panelGraficos.add(panelChartGastos);

		} catch (Exception e) {
			e.printStackTrace();
		}

		panelGraficos.revalidate();
		panelGraficos.repaint();
	}
}
