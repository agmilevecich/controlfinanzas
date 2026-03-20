package ar.com.controlfinanzas.util;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import ar.com.controlfinanzas.model.CategoriaGasto;

public class ChartUtils {

	// 🎨 PALETA GLOBAL APP
	public static final Color COLOR_GASTOS = new Color(231, 76, 60); // rojo
	public static final Color COLOR_INGRESOS = new Color(46, 204, 113); // verde
	public static final Color COLOR_INVERSIONES = new Color(155, 89, 182); // violeta

	// 🎨 COLORES POR CATEGORÍA
	public static Map<CategoriaGasto, Color> coloresCategorias() {
		Map<CategoriaGasto, Color> colores = new EnumMap<>(CategoriaGasto.class);

		colores.put(CategoriaGasto.SUPERMERCADO, new Color(52, 152, 219));
		colores.put(CategoriaGasto.SERVICIOS, new Color(241, 196, 15));
		colores.put(CategoriaGasto.ALQUILER, new Color(231, 76, 60));
		colores.put(CategoriaGasto.TRANSPORTE, new Color(46, 204, 113));
		colores.put(CategoriaGasto.SALUD, new Color(155, 89, 182));
		colores.put(CategoriaGasto.EDUCACION, new Color(26, 188, 156));
		colores.put(CategoriaGasto.ENTRETENIMIENTO, new Color(230, 126, 34));
		colores.put(CategoriaGasto.IMPUESTOS, new Color(127, 140, 141));
		colores.put(CategoriaGasto.OTROS, new Color(149, 165, 166));

		return colores;
	}

	// 🚀 MÉTODO PRO (automático)
	public static void aplicarEstiloBasico(JFreeChart chart) {

		chart.setBackgroundPaint(Color.WHITE);

		Plot plot = chart.getPlot();

		// =========================
		// 🎯 PIE CHART
		// =========================
		if (plot instanceof PiePlot piePlot) {

			piePlot.setBackgroundPaint(Color.WHITE);
			piePlot.setOutlineVisible(false);

			Map<CategoriaGasto, Color> colores = coloresCategorias();

			for (Map.Entry<CategoriaGasto, Color> entry : colores.entrySet()) {
				piePlot.setSectionPaint(entry.getKey(), entry.getValue());
			}
		}

		// =========================
		// 🎯 BAR CHART
		// =========================
		if (plot instanceof CategoryPlot categoryPlot) {

			categoryPlot.setBackgroundPaint(Color.WHITE);
			categoryPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);

			var renderer = categoryPlot.getRenderer();

			if (renderer instanceof BarRenderer barRenderer) {
				barRenderer.setSeriesPaint(0, COLOR_GASTOS);
			}

			if (renderer instanceof LineAndShapeRenderer lineRenderer) {
				lineRenderer.setSeriesPaint(0, COLOR_INGRESOS); // saldo en verde
				lineRenderer.setDefaultShapesVisible(true);
			}
		}
	}
}