package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.ui.dashboard.PanelResumenTarjeta;
import ar.com.controlfinanzas.ui.render.ComboRendererGenerico;
import ar.com.controlfinanzas.util.NumeroUtils;

public class PanelGastos extends JPanel {

	private JTextField txtDescripcion;
	private JTextField txtMonto;
	private JTextField txtCuotas;
	private JTextField txtInteres;

	private JLabel lblTotalFinanciado;
	private JLabel lblValorCuota;

	private JComboBox<CategoriaGasto> cbCategoria;
	private JComboBox<Cuenta> cbCuenta;
	private JComboBox<FormaPago> cbFormaPago;
	private JComboBox<TarjetaCredito> cbTarjetaCredito;

	private JButton btnSimular;

	private JTable tableGastos;
	private DefaultTableModel tableModel;

	private JPanel panelGraficos;
	private JSplitPane split;

	private final GastoService gastoService;
	private final CuentaService cuentaService;
	private final MovimientoService movimientoService;

	private Usuario usuario;

	private List<Movimiento> movimientosCache;

	private PanelBotones botones = new PanelBotones();
	private DefaultComboBoxModel<Cuenta> modelCuenta = new DefaultComboBoxModel<>();
	private DefaultComboBoxModel<TarjetaCredito> modelTarjeta = new DefaultComboBoxModel<>();

	private TarjetaCreditoService tarjetaCreditoService;
	private PanelResumenTarjeta panelResumenTarjeta;
	private Runnable actualizaGastos;

	private JLabel lblTarjeta;
	private JLabel lblCuotas;
	private JLabel lblInteres;

	public PanelGastos(GastoService gastoService, CuentaService cuentaService, MovimientoService movimientoService,
			TarjetaCreditoService tarjetaCreditoService, PanelResumenTarjeta panelResumenTarjeta) {

		this.gastoService = gastoService;
		this.cuentaService = cuentaService;
		this.movimientoService = movimientoService;
		this.tarjetaCreditoService = tarjetaCreditoService;
		this.panelResumenTarjeta = panelResumenTarjeta;
		this.usuario = SesionUsuario.getUsuarioActual();

		inicializarPanel();
		cargarGastos();
		actualizarGraficos();
		actualizarCuenta();
		actualizarTarjetaCredito();
	}

	private void inicializarPanel() {

		setLayout(new BorderLayout());

		JPanel panelFormulario = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		txtDescripcion = new JTextField(15);
		txtMonto = new JTextField(8);
		txtCuotas = new JTextField(5);
		txtInteres = new JTextField(5);

		lblTarjeta = new JLabel("Tarjeta Crédito:");
		lblCuotas = new JLabel("Cuotas:");
		lblInteres = new JLabel("Interés %:");

		lblTotalFinanciado = new JLabel("Total: -");
		lblValorCuota = new JLabel("Cuota: -");

		btnSimular = new JButton("Simular");

		cbCategoria = new JComboBox<>(CategoriaGasto.values());
		cbCategoria.insertItemAt(null, 0);
		cbCategoria.setRenderer(new ComboRendererGenerico<>("Seleccione una categoría", CategoriaGasto::toString));
		cbCategoria.setSelectedIndex(0);

		cbCuenta = new JComboBox<>(modelCuenta);
		cbCuenta.setRenderer(new ComboRendererGenerico<>("Seleccione una cuenta", Cuenta::getNombre));

		cbFormaPago = new JComboBox<>(FormaPago.values());
		cbFormaPago.insertItemAt(null, 0);
		cbFormaPago.setRenderer(new ComboRendererGenerico<>("Seleccione una forma de pago", FormaPago::toString));
		cbFormaPago.setSelectedIndex(0);

		cbTarjetaCredito = new JComboBox<>(modelTarjeta);

		cbFormaPago.addActionListener(e -> {
			FormaPago forma = (FormaPago) cbFormaPago.getSelectedItem();
			boolean esCredito = forma == FormaPago.CREDITO;
			mostrarCamposCredito(esCredito);
		});

		gbc.gridx = 0;
		gbc.gridy = 0;
		panelFormulario.add(new JLabel("Descripción:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(txtDescripcion, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panelFormulario.add(new JLabel("Monto:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(txtMonto, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panelFormulario.add(new JLabel("Categoría:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(cbCategoria, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		panelFormulario.add(new JLabel("Cuenta:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(cbCuenta, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		panelFormulario.add(new JLabel("Forma Pago:"), gbc);
		gbc.gridx = 1;
		panelFormulario.add(cbFormaPago, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		panelFormulario.add(lblTarjeta, gbc);
		gbc.gridx = 1;
		panelFormulario.add(cbTarjetaCredito, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		panelFormulario.add(lblCuotas, gbc);
		gbc.gridx = 1;
		panelFormulario.add(txtCuotas, gbc);

		gbc.gridx = 0;
		gbc.gridy = 7;
		panelFormulario.add(lblInteres, gbc);
		gbc.gridx = 1;
		panelFormulario.add(txtInteres, gbc);

		gbc.gridx = 0;
		gbc.gridy = 8;
		panelFormulario.add(btnSimular, gbc);

		gbc.gridx = 1;
		panelFormulario.add(lblTotalFinanciado, gbc);

		gbc.gridx = 1;
		gbc.gridy = 9;
		panelFormulario.add(lblValorCuota, gbc);

		gbc.gridx = 0;
		gbc.gridy = 10;
		gbc.gridwidth = 2;
		panelFormulario.add(botones, gbc);

		tableModel = new DefaultTableModel(
				new String[] { "ID", "Fecha", "Descripción", "Monto", "Categoría", "Cuenta/Tarjeta" }, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		tableGastos = new JTable(tableModel);
		tableGastos.removeColumn(tableGastos.getColumnModel().getColumn(0));

		JPanel panelTabla = new JPanel(new BorderLayout());
		panelTabla.add(panelFormulario, BorderLayout.NORTH);
		panelTabla.add(new JScrollPane(tableGastos), BorderLayout.CENTER);

		panelGraficos = new JPanel();
		panelGraficos.setLayout(new BoxLayout(panelGraficos, BoxLayout.Y_AXIS));
		JScrollPane scrollGrafico = new JScrollPane(panelGraficos);

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTabla, scrollGrafico);
		split.setResizeWeight(0.5);
		add(split, BorderLayout.CENTER);

		btnSimular.addActionListener(e -> simularCuotas());
		JButton[] boton = botones.getBotones();
		boton[0].addActionListener(e -> agregarGasto());
		mostrarCamposCredito(false);
	}

	private void simularCuotas() {
		try {
			BigDecimal monto = NumeroUtils.parse(txtMonto.getText());
			int cuotas = txtCuotas.getText().isBlank() ? 1 : Integer.parseInt(txtCuotas.getText());
			BigDecimal interes = txtInteres.getText().isBlank() ? BigDecimal.ZERO
					: new BigDecimal(txtInteres.getText()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

			BigDecimal total = monto.add(monto.multiply(interes));
			BigDecimal cuota = total.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);

			lblTotalFinanciado.setText("Total: $" + total);
			lblValorCuota.setText("Cuota: $" + cuota);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Ingrese monto, cuotas e interés válidos");
		}
	}

	private void agregarGasto() {

		try {
			String descripcion = txtDescripcion.getText().trim();
			BigDecimal monto = NumeroUtils.parse(txtMonto.getText());
			CategoriaGasto categoria = (CategoriaGasto) cbCategoria.getSelectedItem();
			if (categoria == null) {
				JOptionPane.showMessageDialog(this, "Seleccione una categoría");
				return;
			}

			FormaPago formaPago = (FormaPago) cbFormaPago.getSelectedItem();
			if (formaPago == null) {
				JOptionPane.showMessageDialog(this, "Seleccione una forma de pago");
				return;
			}

			Cuenta cuenta = (Cuenta) cbCuenta.getSelectedItem();
			if (formaPago != FormaPago.CREDITO && cuenta == null) {
				JOptionPane.showMessageDialog(this, "Debe seleccionar una cuenta");
				return;
			}
			if (formaPago != FormaPago.CREDITO) {

				Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.GASTO);
				mov.setUsuario(usuario);
				mov.setCuenta(cuenta);
				mov.setFormaPago(formaPago);
				mov.setCategoria(categoria);
				movimientoService.registrarMovimiento(mov);
			} else {
				TarjetaCredito tarjeta = (TarjetaCredito) cbTarjetaCredito.getSelectedItem();
				int cuotas = txtCuotas.getText().isBlank() ? 1 : Integer.parseInt(txtCuotas.getText());
				BigDecimal interes = txtInteres.getText().isBlank() ? BigDecimal.ZERO
						: new BigDecimal(txtInteres.getText());

				movimientoService.registrarCompraCuotas(tarjeta, descripcion, monto, cuotas, interes, categoria);
				JOptionPane.showMessageDialog(this, "Compra realizada con tarjeta realizada correctamente");
			}

			if (actualizaGastos != null) {
				actualizaGastos.run();
			}

			limpiarFormulario();
			cargarGastos();
			actualizarGraficos();

			if (panelResumenTarjeta != null) {
				panelResumenTarjeta.refrescar();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar gasto");
		}
	}

	private void limpiarFormulario() {
		txtDescripcion.setText("");
		txtMonto.setText("");
		txtCuotas.setText("");
		txtInteres.setText("");
		cbCategoria.setSelectedIndex(0);
		cbCuenta.setSelectedIndex(0);
		cbFormaPago.setSelectedIndex(0);
	}

	public void cargarGastos() {

		movimientosCache = movimientoService.listarPorUsuario();

		for (Movimiento m : movimientosCache) {

			String cuentaNombre = "-";

			if (m.getFormaPago() == FormaPago.CREDITO) {
				cuentaNombre = m.getTarjeta() != null ? m.getTarjeta().toString() : "Tarjeta";
			} else {
				cuentaNombre = m.getCuenta() != null ? m.getCuenta().getNombre() : "-";
			}

			tableModel.addRow(new Object[] { m.getId(), m.getFecha(), m.getDescripcion(), m.getMonto(),
					m.getCategoria(), cuentaNombre });
		}

	}

	public void actualizarCuenta() {
		modelCuenta.removeAllElements();
		modelCuenta.addElement(null);
		for (Cuenta c : cuentaService.getCuentasUsuario(usuario)) {
			modelCuenta.addElement(c);
		}
	}

	public void actualizarTarjetaCredito() {
		modelTarjeta.removeAllElements();
		for (TarjetaCredito t : tarjetaCreditoService.getTarjetasUsuario(usuario.getUsuarioID())) {
			modelTarjeta.addElement(t);
		}
	}

	private void actualizarGraficos() {
		panelGraficos.removeAll();
		actualizarGraficoPie();
		actualizarGraficoBarras();
		panelGraficos.revalidate();
		panelGraficos.repaint();
	}

	private void actualizarGraficoPie() {
		if (movimientosCache == null || movimientosCache.isEmpty()) {
			return;
		}

		DefaultPieDataset dataset = new DefaultPieDataset();
		Map<CategoriaGasto, BigDecimal> totales = new HashMap<>();
		for (Movimiento m : movimientosCache) {
			totales.put(m.getCategoria(), totales.getOrDefault(m.getCategoria(), BigDecimal.ZERO).add(m.getMonto()));
		}
		for (Map.Entry<CategoriaGasto, BigDecimal> e : totales.entrySet()) {
			dataset.setValue(e.getKey(), e.getValue());
		}
		JFreeChart chart = ChartFactory.createPieChart("Gastos por Categoría", dataset, true, true, false);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(400, 300));
		panelGraficos.add(chartPanel);
	}

	private void actualizarGraficoBarras() {
		if (movimientosCache == null || movimientosCache.isEmpty()) {
			return;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Map<Integer, BigDecimal> totales = new HashMap<>();
		for (Movimiento m : movimientosCache) {
			int mes = m.getFecha().getMonthValue();
			totales.put(mes, totales.getOrDefault(mes, BigDecimal.ZERO).add(m.getMonto()));
		}
		for (Map.Entry<Integer, BigDecimal> e : totales.entrySet()) {
			String nombreMes = java.time.Month.of(e.getKey()).getDisplayName(TextStyle.SHORT, Locale.getDefault());
			dataset.addValue(e.getValue(), "Gastos", nombreMes);
		}
		JFreeChart chart = ChartFactory.createBarChart("Gastos Mensuales", "Mes", "Monto", dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(400, 300));
		panelGraficos.add(chartPanel);
	}

	public void setActualizaGastos(Runnable actualizaGastos) {
		this.actualizaGastos = actualizaGastos;
	}

	public void refrescar() {
		cargarGastos();
		actualizarGraficos();
		actualizarCuenta();
		actualizarTarjetaCredito();
	}

	private void mostrarCamposCredito(boolean visible) {
		lblTarjeta.setVisible(visible);
		cbTarjetaCredito.setVisible(visible);
		lblCuotas.setVisible(visible);
		txtCuotas.setVisible(visible);
		lblInteres.setVisible(visible);
		txtInteres.setVisible(visible);
		btnSimular.setVisible(visible);
		lblTotalFinanciado.setVisible(visible);
		lblValorCuota.setVisible(visible);
	}
}