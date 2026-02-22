package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import ar.com.controlfinanzas.alerts.AlertaVencimiento;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.EstadoInversion;

/**
 * Panel que muestra gráficamente los días restantes hasta el vencimiento de
 * cada inversión. Las barras se colorean según el estado de la inversión:
 * VIGENTE (verde), PROXIMA_A_VENCER (naranja), VENCIDA (rojo)
 * 
 * Ahora incluye método actualizarInversiones() para refrescar dinámicamente el
 * gráfico sin errores.
 */
public class PanelVencimientos extends JPanel {

	private List<Inversion> inversiones;
	private DefaultCategoryDataset dataset;
	private ChartPanel chartPanel;

	public PanelVencimientos(List<Inversion> inversiones) {
		this.inversiones = inversiones;
		inicializarPanel();
	}

	private void inicializarPanel() {
		dataset = crearDataset();

		JFreeChart chart = ChartFactory.createBarChart("Vencimientos de Inversiones", "Inversión", "Días restantes",
				dataset, PlotOrientation.VERTICAL, false, true, false);

		// Configuramos renderer inicial
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setRenderer(crearRenderer());

		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(800, 400));

		this.setLayout(new BorderLayout());
		this.add(chartPanel, BorderLayout.CENTER);
	}

	/**
	 * Crea el dataset con los días restantes de cada inversión
	 */
	private DefaultCategoryDataset crearDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		LocalDate hoy = LocalDate.now();

		for (Inversion inv : inversiones) {
			AlertaVencimiento alerta = new AlertaVencimiento(inv, hoy);
			int diasRestantes = alerta.getDiasRestantes();
			dataset.addValue(diasRestantes, "Días hasta vencimiento", inv.getNombre());
		}

		return dataset;
	}

	/**
	 * Crea un renderer que colorea las barras según el estado de la inversión
	 */
	private BarRenderer crearRenderer() {

		BarRenderer renderer = new BarRenderer() {

			@Override
			public Paint getItemPaint(int row, int column) {

				String nombreInversion = (String) dataset.getColumnKey(column);

				Inversion inv = inversiones.stream().filter(i -> i.getNombre().equals(nombreInversion)).findFirst()
						.orElse(null);

				if (inv != null) {
					AlertaVencimiento alerta = new AlertaVencimiento(inv, LocalDate.now());
					return getColorForEstado(alerta.getEstado());
				}

				return super.getItemPaint(row, column);
			}
		};

		// 👇 TOOLTIP DINÁMICO
		renderer.setDefaultToolTipGenerator((dataset, row, column) -> {

			String nombreInversion = (String) dataset.getColumnKey(column);

			Inversion inv = inversiones.stream().filter(i -> i.getNombre().equals(nombreInversion)).findFirst()
					.orElse(null);

			return inv != null ? generarTooltip(inv) : "";
		});

		return renderer;
	}

	/**
	 * Retorna el color asociado a un estado de inversión
	 */
	private Color getColorForEstado(EstadoInversion estado) {
		switch (estado) {
		case VIGENTE:
			return Color.GREEN;
		case PROXIMA_A_VENCER:
			return Color.ORANGE;
		case VENCIDA:
			return Color.RED;
		default:
			return Color.GRAY;
		}
	}

	/**
	 * Actualiza el panel con una nueva lista de inversiones y refresca el gráfico
	 * de manera segura, recreando el renderer para evitar errores de índice.
	 */
	public void actualizarInversiones(List<Inversion> nuevasInversiones) {
		this.inversiones = nuevasInversiones;

		// Creamos un nuevo dataset
		dataset = crearDataset();

		// Actualizamos el plot y renderer
		CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
		plot.setDataset(dataset);
		plot.setRenderer(crearRenderer());

		// Forzamos repaint
		this.repaint();
	}

	private String generarTooltip(Inversion inv) {

		AlertaVencimiento alerta = new AlertaVencimiento(inv, LocalDate.now());

		return "<html>" + "<b>" + inv.getNombre() + "</b><br>" + "Tipo: " + inv.getTipoInversion() + "<br>" + "Capital: "
				+ inv.getCapitalInicial() + "<br>" + "Vencimiento: " + inv.getFechaVencimiento() + "<br>"
				+ "Días restantes: " + alerta.getDiasRestantes() + "<br>" + "Estado: " + alerta.getEstado() + "</html>";
	}

}
