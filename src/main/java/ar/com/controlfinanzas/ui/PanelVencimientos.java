package ar.com.controlfinanzas.ui;

import java.time.LocalDate;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import ar.com.controlfinanzas.model.Inversion;

/**
 * Panel que muestra inversiones próximas a vencer en barras
 */
public class PanelVencimientos extends JPanel {

	public PanelVencimientos(List<Inversion> inversiones, LocalDate hoy) {

		int dias30 = 0, dias15 = 0, dias7 = 0;

		for (Inversion inv : inversiones) {
			if (!inv.tieneVencimiento()) {
				continue;
			}

			long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, inv.getFechaVencimiento());

			if (diasRestantes <= 7 && diasRestantes >= 0) {
				dias7++;
			} else if (diasRestantes <= 15) {
				dias15++;
			} else if (diasRestantes <= 30) {
				dias30++;
			}
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(dias30, "Vencimientos próximos", "≤30 días");
		dataset.addValue(dias15, "Vencimientos próximos", "≤15 días");
		dataset.addValue(dias7, "Vencimientos próximos", "≤7 días");

		JFreeChart chart = ChartFactory.createBarChart("Vencimientos de Inversiones", "Período",
				"Cantidad de inversiones", dataset);

		add(new ChartPanel(chart));
	}
}
