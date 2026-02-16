package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.InversionService;

public class PanelResumenFinanciero extends JPanel {

	private final InversionService inversionService;
	private final GastoService gastoService;

	private JLabel lblTotalGastos;
	private JLabel lblTotalInversiones;
	private JLabel lblSaldoNeto;

	private JPanel panelGraficos;

	public PanelResumenFinanciero(InversionService inversionService, GastoService gastoService) {

		this.inversionService = inversionService;
		this.gastoService = gastoService;

		inicializarPanel();
		actualizarResumen();
	}

	private void inicializarPanel() {

		this.setLayout(new BorderLayout());

		JPanel panelNumerico = new JPanel(new GridLayout(1, 3, 10, 10));

		lblTotalInversiones = new JLabel("Total Inversiones: $0");
		lblTotalGastos = new JLabel("Total Gastos: $0");
		lblSaldoNeto = new JLabel("Saldo Neto: $0");

		panelNumerico.add(lblTotalInversiones);
		panelNumerico.add(lblTotalGastos);
		panelNumerico.add(lblSaldoNeto);

		panelGraficos = new JPanel(new GridLayout(1, 2, 10, 10));

		this.add(panelNumerico, BorderLayout.NORTH);
		this.add(panelGraficos, BorderLayout.CENTER);
	}

	public void actualizarResumen() {

		panelGraficos.removeAll();

		try {

			// Obtener datos UNA sola vez
			List<Inversion> inversiones = inversionService.obtenerTodas();
			List<Gasto> gastos = gastoService.listarPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());

			// Totales
			BigDecimal totalGastos = gastoService.calcularTotalGastos();
			BigDecimal totalInversiones = BigDecimal.ZERO;

			for (Inversion inv : inversiones) {
				totalInversiones = totalInversiones.add(inv.getCapitalInicial());
			}

			lblTotalGastos.setText("Total Gastos: $" + totalGastos.setScale(2, RoundingMode.HALF_UP));

			lblTotalInversiones.setText("Total Inversiones: $" + totalInversiones.setScale(2, RoundingMode.HALF_UP));

			lblSaldoNeto.setText(
					"Saldo Neto: $" + totalInversiones.subtract(totalGastos).setScale(2, RoundingMode.HALF_UP));

			// ============================
			// Gráfico Inversiones por Tipo
			// ============================

			DefaultCategoryDataset datasetInv = new DefaultCategoryDataset();
			Map<TipoInversion, BigDecimal> sumaPorTipo = new HashMap<>();

			for (Inversion inv : inversiones) {
				sumaPorTipo.put(inv.getTipo(),
						sumaPorTipo.getOrDefault(inv.getTipo(), BigDecimal.ZERO).add(inv.getCapitalInicial()));
			}

			for (Map.Entry<TipoInversion, BigDecimal> entry : sumaPorTipo.entrySet()) {
				datasetInv.addValue(entry.getValue(), "Inversiones", entry.getKey().name());
			}

			JFreeChart chartInv = ChartFactory.createBarChart("Inversiones por Tipo", "Tipo", "Monto", datasetInv);

			panelGraficos.add(new ChartPanel(chartInv));

			// ============================
			// Gráfico Gastos por Categoría
			// ============================

			DefaultPieDataset datasetGastos = new DefaultPieDataset();
			Map<String, BigDecimal> sumaPorCategoria = new HashMap<>();

			for (Gasto g : gastos) {
				sumaPorCategoria.put(g.getCategoria(),
						sumaPorCategoria.getOrDefault(g.getCategoria(), BigDecimal.ZERO).add(g.getMonto()));
			}

			for (Map.Entry<String, BigDecimal> entry : sumaPorCategoria.entrySet()) {
				datasetGastos.setValue(entry.getKey(), entry.getValue());
			}

			JFreeChart chartGastos = ChartFactory.createPieChart("Gastos por Categoría", datasetGastos, true, true,
					false);

			panelGraficos.add(new ChartPanel(chartGastos));

		} catch (Exception e) {
			e.printStackTrace();
		}

		panelGraficos.revalidate();
		panelGraficos.repaint();
	}
}
