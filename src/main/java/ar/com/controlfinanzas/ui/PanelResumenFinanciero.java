package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.IngresoService;
import ar.com.controlfinanzas.service.InversionService;

public class PanelResumenFinanciero extends JPanel {

	private final InversionService inversionService;
	private final GastoService gastoService;
	private final IngresoService ingresoService;

	private JLabel lblTotalInversiones;
	private JLabel lblTotalGastos;
	private JLabel lblSaldoNeto;
	private JLabel lblGastosMes;

	private JPanel panelGraficos;

	public PanelResumenFinanciero(InversionService inversionService, GastoService gastoService,
			IngresoService ingresoService) {

		this.inversionService = inversionService;
		this.gastoService = gastoService;
		this.ingresoService = ingresoService;

		setLayout(new BorderLayout());

		inicializarComponentes();
	}

	private void inicializarComponentes() {

		JPanel panelMetricas = new JPanel(new GridLayout(4, 1));

		lblTotalInversiones = new JLabel("Total Inversiones: $0.00", SwingConstants.CENTER);
		lblTotalGastos = new JLabel("Gastos Históricos: $0.00", SwingConstants.CENTER);
		lblSaldoNeto = new JLabel("Patrimonio Neto: $0.00", SwingConstants.CENTER);
		lblGastosMes = new JLabel("Gastos Mes Actual: $0.00", SwingConstants.CENTER);

		panelMetricas.add(lblTotalInversiones);
		panelMetricas.add(lblTotalGastos);
		panelMetricas.add(lblSaldoNeto);
		panelMetricas.add(lblGastosMes);

		add(panelMetricas, BorderLayout.NORTH);

		panelGraficos = new JPanel(new GridLayout(1, 2));
		add(panelGraficos, BorderLayout.CENTER);
	}

	public void actualizarResumen() {

		panelGraficos.removeAll();

		try {

			Integer usuarioId = MainApp.getUsuarioActivo().getUsuarioID();
			YearMonth mesActual = YearMonth.now();

			// ============================
			// MÉTRICAS FINANCIERAS
			// ============================

			BigDecimal totalInversiones = inversionService.calcularCapitalTotal(usuarioId);
			BigDecimal totalGastosHistorico = gastoService.calcularTotalHistorico(usuarioId);
			BigDecimal gastosMes = gastoService.calcularTotalPorMes(usuarioId, mesActual);

			if (totalInversiones == null) {
				totalInversiones = BigDecimal.ZERO;
			}

			if (totalGastosHistorico == null) {
				totalGastosHistorico = BigDecimal.ZERO;
			}

			if (gastosMes == null) {
				gastosMes = BigDecimal.ZERO;
			}

			// Por ahora el patrimonio es el total invertido
			BigDecimal patrimonioNeto = totalInversiones;

			BigDecimal totalIngresosHistorico = ingresoService.calcularTotalHistorico(usuarioId);
			BigDecimal ingresosMes = ingresoService.calcularTotalPorMes(usuarioId, mesActual);

			BigDecimal resultadoAcumulado = totalIngresosHistorico.subtract(totalGastosHistorico);
			BigDecimal resultadoMes = ingresosMes.subtract(gastosMes);
			BigDecimal totalSupermercado = gastoService.calcularTotalPorCategoria(usuarioId,
					CategoriaGasto.SUPERMERCADO);
			BigDecimal supermercadoMes = gastoService.calcularTotalesPorCategoriaYMes(usuarioId,
					CategoriaGasto.SUPERMERCADO, mesActual);

			LinkedHashMap<CategoriaGasto, BigDecimal> ranking = gastoService.rankingCategoriasPorMes(usuarioId,
					mesActual);

			lblTotalInversiones.setText("Total Inversiones: $" + totalInversiones.setScale(2, RoundingMode.HALF_UP));

			lblTotalGastos.setText("Gastos Históricos: $" + totalGastosHistorico.setScale(2, RoundingMode.HALF_UP));

			lblSaldoNeto.setText("Activos Invertidos: $" + patrimonioNeto.setScale(2, RoundingMode.HALF_UP));

			lblGastosMes.setText("Gastos " + mesActual + ": $" + gastosMes.setScale(2, RoundingMode.HALF_UP));

			// ============================
			// GRÁFICO INVERSIONES POR TIPO
			// ============================

			List<Inversion> inversiones = inversionService.obtenerTodas();

			DefaultCategoryDataset datasetInv = new DefaultCategoryDataset();
			Map<TipoInversion, BigDecimal> sumaPorTipo = new HashMap<>();

			for (Inversion inv : inversiones) {

				BigDecimal capital = inv.getCapitalTotalCalculado() != null ? inv.getCapitalTotalCalculado()
						: BigDecimal.ZERO;

				sumaPorTipo.put(inv.getTipoInversion(),
						sumaPorTipo.getOrDefault(inv.getTipoInversion(), BigDecimal.ZERO).add(capital));
			}

			for (Map.Entry<TipoInversion, BigDecimal> entry : sumaPorTipo.entrySet()) {
				datasetInv.addValue(entry.getValue(), "Inversiones", entry.getKey().name());
			}

			JFreeChart chartInv = ChartFactory.createBarChart("Inversiones por Tipo", "Tipo", "Monto", datasetInv);

			panelGraficos.add(new ChartPanel(chartInv));

			// ============================
			// GRÁFICO GASTOS POR CATEGORÍA (MES ACTUAL)
			// ============================

			List<Gasto> gastos = gastoService.listarPorUsuario(usuarioId);

			DefaultPieDataset datasetGastos = new DefaultPieDataset();
			Map<CategoriaGasto, BigDecimal> sumaPorCategoria = new HashMap<>();

			for (Gasto g : gastos) {

				if (g.getFecha() != null && YearMonth.from(g.getFecha()).equals(mesActual)) {

					BigDecimal monto = g.getMonto() != null ? g.getMonto() : BigDecimal.ZERO;

					sumaPorCategoria.put(g.getCategoria(),
							sumaPorCategoria.getOrDefault(g.getCategoria(), BigDecimal.ZERO).add(monto));
				}
			}

			for (Map.Entry<CategoriaGasto, BigDecimal> entry : sumaPorCategoria.entrySet()) {
				datasetGastos.setValue(entry.getKey(), entry.getValue());
			}

			JFreeChart chartGastos = ChartFactory.createPieChart("Gastos por Categoría (" + mesActual + ")",
					datasetGastos, true, true, false);

			panelGraficos.add(new ChartPanel(chartGastos));

		} catch (Exception e) {
			e.printStackTrace();
		}

		panelGraficos.revalidate();
		panelGraficos.repaint();
	}
}
