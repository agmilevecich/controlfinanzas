package ar.com.controlfinanzas.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.model.Inversion;

/**
 * Panel que muestra la distribución de las inversiones por tipo
 */
public class PanelDistribucion extends JPanel {

	public PanelDistribucion(List<Inversion> inversiones) {

		// Sumamos el capital por tipo de inversión
		Map<String, BigDecimal> capitalPorTipo = inversiones.stream()
				.collect(Collectors.groupingBy(inv -> inv.getTipo().name(), Collectors
						.mapping(Inversion::getCapitalInicial, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

		// Dataset de JFreeChart
		DefaultPieDataset dataset = new DefaultPieDataset();
		capitalPorTipo.forEach((tipo, capital) -> dataset.setValue(tipo, capital.doubleValue()));

		// Gráfico de torta
		JFreeChart chart = ChartFactory.createPieChart("Distribución por Tipo de Inversión", dataset, true, // legend
				true, // tooltips
				false // URLs
		);

		add(new ChartPanel(chart));
	}
}
