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
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.MovimientoService;

public class PanelGastos extends JPanel {

	private JTextField txtDescripcion;
	private JTextField txtMonto;
	private JComboBox<CategoriaGasto> cbCategoria;
	private JTable tableGastos;
	private DefaultTableModel tableModel;

	private JPanel panelGraficos;

	private final GastoService gastoService;
	private PanelResumenFinanciero panelResumen;

	private List<Gasto> gastosCache;
	private PanelResumenGastos panelResumenGastos;

	private PanelBotones botones = new PanelBotones();
	private JSplitPane split;
	private Usuario usuario;
	private CuentaService cuentaService;
	private MovimientoService movimientoService;
	private JComboBox<Cuenta> cbCuenta;
	private JComboBox<FormaPago> cbFormaPago;
	private DefaultComboBoxModel<Cuenta> modelCuenta = new DefaultComboBoxModel<Cuenta>();

	private Runnable actualizaGastos;

	public PanelGastos(GastoService gastoService, CuentaService cuentaServce, MovimientoService movimientoService,
			PanelResumenFinanciero panelResumen, PanelResumenGastos panelResumenGastos, Usuario usuario) {
		this.gastoService = gastoService;
		this.cuentaService = cuentaServce;
		this.movimientoService = movimientoService;
		this.panelResumen = panelResumen;
		this.panelResumenGastos = panelResumenGastos;
		this.usuario = usuario;

		inicializarPanel();
		cargarGastos();
		actualizarGraficos();
		actualizarCuenta();
	}

	private void actualizarCuenta() {
		modelCuenta.removeAllElements();
		for (Cuenta c : cuentaService.getCuentasUsuario(usuario)) {
			modelCuenta.addElement(c);
		}
	}

	public void refrescarCuentas() {
		actualizarCuenta();
	}

	private void inicializarPanel() {
		this.setLayout(new BorderLayout());

		JPanel panelFormulario = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		cbCuenta = new JComboBox<>(modelCuenta);
		cbCuenta.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel lblCuenta = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);

				if (value instanceof Cuenta) {
					Cuenta cuenta = (Cuenta) value;
					lblCuenta.setText(cuenta.getNombre() + " " + cuenta.getMoneda());
				}

				return lblCuenta;
			}

		});
		cbFormaPago = new JComboBox<>(FormaPago.values());

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		txtDescripcion = new JTextField(15);
		txtMonto = new JTextField(8);
		cbCategoria = new JComboBox<>(CategoriaGasto.values());

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
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);

		this.add(split, BorderLayout.CENTER);

		JButton[] boton = botones.getBotones();
		boton[0].addActionListener(e -> agregarGasto());
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

			Gasto gasto = new Gasto();
			gasto.setFecha(LocalDate.now());
			gasto.setDescripcion(descripcion);
			gasto.setMonto(monto);
			gasto.setCategoria(categoria);
			gasto.setCuenta(cuenta);
			gasto.setFormapago(formaPago);
			gasto.setUsuario(usuario);

			gastoService.guardar(gasto);

			Movimiento mov = new Movimiento(LocalDate.now(), gasto.getDescripcion(), gasto.getMonto(),
					TipoMovimiento.GASTO);
			movimientoService.registrarMovimiento(cuenta, mov);
			cuenta.getMovimientos().add(mov);
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

	public void setActualizaGastos(Runnable actualizaGastos) {
		this.actualizaGastos = actualizaGastos;
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
		chartPanel.setPreferredSize(new Dimension(400, 300)); // <-- tamaño controlado
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
		chartPanel.setPreferredSize(new Dimension(400, 300)); // <-- tamaño controlado
		chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
		panelGraficos.add(chartPanel);
	}

}