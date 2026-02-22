package ar.com.controlfinanzas.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.projection.CalculadoraProyeccion;

/**
 * Panel que muestra la evolución diaria del patrimonio total. Combina
 * inversiones y cuentas/billeteras.
 */
public class PanelEvolucionPatrimonio extends JPanel {

	public PanelEvolucionPatrimonio(List<Inversion> inversiones, List<Cuenta> cuentas,
			CalculadoraProyeccion calculadora, LocalDate fechaInicio, LocalDate fechaFin) {

		// Generamos las fechas diarias entre inicio y fin
		List<LocalDate> fechas = generarFechasDiarias(fechaInicio, fechaFin);

		// Creamos la serie de tiempo para el gráfico
		TimeSeries serie = new TimeSeries("Patrimonio Total");

		for (LocalDate fecha : fechas) {

			// Proyectamos inversiones
			BigDecimal totalInversiones = inversiones.stream().map(inv -> calculadora.proyectar(inv, fecha))
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			// Proyectamos cuentas/billeteras
			BigDecimal totalCuentas = cuentas.stream().map(c -> calculadora.proyectar(c, fecha)).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			BigDecimal total = totalInversiones.add(totalCuentas);

			// Agregamos al TimeSeries (JFreeChart necesita double)
			serie.add(new Day(java.util.Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant())),
					total.doubleValue());
		}

		// Creamos el dataset y el gráfico
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(serie);

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Evolución del Patrimonio", "Fecha",
				"Capital (ARS / USD según moneda)", dataset, true, // legend
				true, // tooltips
				false // URLs
		);

		// Layout y agregado del chart panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new ChartPanel(chart));
	}

	/**
	 * Genera una lista de fechas diarias entre inicio y fin
	 */
	private List<LocalDate> generarFechasDiarias(LocalDate inicio, LocalDate fin) {
		List<LocalDate> fechas = new ArrayList<>();
		LocalDate actual = inicio;
		while (!actual.isAfter(fin)) {
			fechas.add(actual);
			actual = actual.plusDays(1);
		}
		return fechas;
	}

}
