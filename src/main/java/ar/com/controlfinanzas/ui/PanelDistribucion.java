package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.controller.InversionController;
import ar.com.controlfinanzas.domain.inversion.Inversion;

/**
 * Panel que muestra la distribución de las inversiones por tipo de activo
 */
public class PanelDistribucion extends JPanel {

	private final InversionController inversionController;
	private ChartPanel chartPanel;

	public PanelDistribucion(InversionController inversionController) {
		this.inversionController = inversionController;

		setLayout(new BorderLayout());

		// Escucha cambios (cuando agregás inversión)
		inversionController.addListener(this::refrescar);

		// Carga inicial
		refrescar();
	}

	private void refrescar() {

		List<Inversion> inversiones = inversionController.getInversiones();

		// Agrupar capital por tipo de activo
		Map<String, BigDecimal> capitalPorActivo = inversiones.stream()
				.collect(Collectors.groupingBy(inv -> inv.getTipoActivo().name(), Collectors.mapping(inv -> {
					BigDecimal capital = inv.getCapitalTotalCalculado();
					return capital != null ? capital : BigDecimal.ZERO;
				}, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

		// Dataset
		DefaultPieDataset dataset = new DefaultPieDataset();
		capitalPorActivo.forEach((tipo, capital) -> dataset.setValue(tipo, capital.doubleValue()));

		// Chart
		JFreeChart chart = ChartFactory.createPieChart("Distribución por Tipo de Activo", dataset, true, true, false);

		// Reemplazar gráfico anterior
		if (chartPanel != null) {
			remove(chartPanel);
		}

		chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);

		revalidate();
		repaint();
	}
}