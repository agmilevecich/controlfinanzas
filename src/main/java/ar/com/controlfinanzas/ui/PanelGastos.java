package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
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
import ar.com.controlfinanzas.model.Gasto;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;

public class PanelGastos extends JPanel {

	private JTextField txtDescripcion;
	private JTextField txtMonto;

	private JComboBox<CategoriaGasto> cbCategoria;
	private JComboBox<Cuenta> cbCuenta;
	private JComboBox<FormaPago> cbFormaPago;
	private JComboBox<TarjetaCredito> cbTarjetaCredito;

	private JTable tableGastos;
	private DefaultTableModel tableModel;

	private JPanel panelGraficos;
	private JSplitPane split;

	private final GastoService gastoService;
	private final CuentaService cuentaService;
	private final MovimientoService movimientoService;

	private Usuario usuario;

	private List<Gasto> gastosCache;

	private PanelResumenFinanciero panelResumen;
	private PanelResumenGastos panelResumenGastos;

	private PanelBotones botones = new PanelBotones();

	private DefaultComboBoxModel<Cuenta> modelCuenta = new DefaultComboBoxModel<>();
	private DefaultComboBoxModel<TarjetaCredito> modelTarjeta = new DefaultComboBoxModel<>();

	private Runnable actualizaGastos;
	private TarjetaCreditoService tarjetaCreditoService;

	public PanelGastos(GastoService gastoService, CuentaService cuentaService, MovimientoService movimientoService,
			TarjetaCreditoService tarjetaCreditoService, PanelResumenFinanciero panelResumen,
			PanelResumenGastos panelResumenGastos, Usuario usuario) {

		this.gastoService = gastoService;
		this.cuentaService = cuentaService;
		this.movimientoService = movimientoService;
		this.tarjetaCreditoService = tarjetaCreditoService;
		this.panelResumen = panelResumen;
		this.panelResumenGastos = panelResumenGastos;
		this.usuario = usuario;

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

		cbCategoria = new JComboBox<>(CategoriaGasto.values());

		cbCuenta = new JComboBox<>(modelCuenta);
		cbCuenta.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof Cuenta c) {
					lbl.setText(c.getNombre() + " " + c.getMoneda());
				}

				return lbl;
			}
		});

		cbCuenta.revalidate();
		cbCuenta.repaint();

		cbFormaPago = new JComboBox<>(FormaPago.values());
		cbFormaPago.revalidate();
		cbFormaPago.repaint();

		cbTarjetaCredito = new JComboBox<>(modelTarjeta);
		cbCuenta.revalidate();
		cbCuenta.repaint();
		cbTarjetaCredito.setEnabled(false);
		cbTarjetaCredito.revalidate();
		cbTarjetaCredito.repaint();

		cbFormaPago.addActionListener(e -> {
			FormaPago forma = (FormaPago) cbFormaPago.getSelectedItem();
			cbTarjetaCredito.setEnabled(forma == FormaPago.CREDITO);
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
		panelFormulario.add(new JLabel("Forma de Pago:"), gbc);

		gbc.gridx = 1;
		panelFormulario.add(cbFormaPago, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		panelFormulario.add(new JLabel("Tarjeta Crédito:"), gbc);

		gbc.gridx = 1;
		panelFormulario.add(cbTarjetaCredito, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		panelFormulario.add(botones, gbc);

		tableModel = new DefaultTableModel(new String[] { "ID", "Fecha", "Descripción", "Monto", "Categoría" }, 0) {
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

		JButton[] boton = botones.getBotones();
		boton[0].addActionListener(e -> agregarGasto());
	}

	private void actualizarCuenta() {

		modelCuenta.removeAllElements();

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

	public void refrescar() {
		cargarGastos();
		actualizarGraficos();
		actualizarCuenta();
	}

	private void agregarGasto() {

		try {

			String descripcion = txtDescripcion.getText().trim();
			String montoStr = txtMonto.getText().trim();

			CategoriaGasto categoria = (CategoriaGasto) cbCategoria.getSelectedItem();
			Cuenta cuenta = (Cuenta) cbCuenta.getSelectedItem();
			FormaPago formaPago = (FormaPago) cbFormaPago.getSelectedItem();

			if (descripcion.isEmpty() || montoStr.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Complete todos los campos");
				return;
			}

			BigDecimal monto = new BigDecimal(montoStr);

			if (formaPago != FormaPago.CREDITO) {

				Gasto gasto = new Gasto();
				gasto.setFecha(LocalDate.now());
				gasto.setDescripcion(descripcion);
				gasto.setMonto(monto);
				gasto.setCategoria(categoria);
				gasto.setCuenta(cuenta);
				gasto.setFormapago(formaPago);
				gasto.setUsuario(usuario);

				gastoService.guardar(gasto);

				Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.GASTO);

				movimientoService.registrarMovimiento(cuenta, mov);

			} else {

				TarjetaCredito tarjeta = (TarjetaCredito) cbTarjetaCredito.getSelectedItem();

				if (tarjeta == null) {
					JOptionPane.showMessageDialog(this, "Seleccione una tarjeta");
					return;
				}

				Movimiento mov = new Movimiento(LocalDate.now(), descripcion, monto, TipoMovimiento.GASTO);
				mov.setFormaPago(formaPago);
				mov.setTarjeta(tarjeta);
				mov.setPendiente(true);

				movimientoService.registrarMovimiento(cuenta, mov);
			}

			limpiarFormulario();
			cargarGastos();
			actualizarGraficos();

			if (panelResumen != null) {
				panelResumen.actualizarResumen();
			}

			if (actualizaGastos != null) {
				actualizaGastos.run();
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar gasto");
		}
	}

	private void limpiarFormulario() {

		txtDescripcion.setText("");
		txtMonto.setText("");
		cbCategoria.setSelectedIndex(0);
	}

	public void cargarGastos() {

		tableModel.setRowCount(0);

		try {

			gastosCache = gastoService.listarPorUsuario(usuario.getUsuarioID());

			for (Gasto g : gastosCache) {

				tableModel.addRow(new Object[] { g.getId(), g.getFecha(), g.getDescripcion(),
						g.getMonto().setScale(2, RoundingMode.HALF_UP), g.getCategoria() });
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void actualizarGraficos() {

		panelGraficos.removeAll();

		actualizarGraficoPie();
		actualizarGraficoBarras();

		panelGraficos.revalidate();
		panelGraficos.repaint();

		if (panelResumenGastos != null) {
			panelResumenGastos.refrescar(usuario.getUsuarioID());
		}
	}

	private void actualizarGraficoPie() {

		if (gastosCache == null || gastosCache.isEmpty()) {
			return;
		}

		DefaultPieDataset dataset = new DefaultPieDataset();

		Map<CategoriaGasto, BigDecimal> totales = new HashMap<>();

		for (Gasto g : gastosCache) {

			totales.put(g.getCategoria(), totales.getOrDefault(g.getCategoria(), BigDecimal.ZERO).add(g.getMonto()));
		}

		for (Map.Entry<CategoriaGasto, BigDecimal> e : totales.entrySet()) {
			dataset.setValue(e.getKey(), e.getValue());
		}

		JFreeChart chart = ChartFactory.createPieChart("Gastos por Categoría", dataset, true, true, false);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(400, 300));
		chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

		panelGraficos.add(chartPanel);
	}

	private void actualizarGraficoBarras() {

		if (gastosCache == null || gastosCache.isEmpty()) {
			return;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Map<Integer, BigDecimal> totales = new HashMap<>();

		for (Gasto g : gastosCache) {

			int mes = g.getFecha().getMonthValue();

			totales.put(mes, totales.getOrDefault(mes, BigDecimal.ZERO).add(g.getMonto()));
		}

		for (Map.Entry<Integer, BigDecimal> e : totales.entrySet()) {

			String nombreMes = java.time.Month.of(e.getKey()).getDisplayName(TextStyle.SHORT, Locale.getDefault());

			dataset.addValue(e.getValue(), "Gastos", nombreMes);
		}

		JFreeChart chart = ChartFactory.createBarChart("Gastos Mensuales", "Mes", "Monto", dataset);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(400, 300));
		chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

		panelGraficos.add(chartPanel);
	}

	public void setActualizaGastos(Runnable actualizaGastos) {
		this.actualizaGastos = actualizaGastos;
	}

}
