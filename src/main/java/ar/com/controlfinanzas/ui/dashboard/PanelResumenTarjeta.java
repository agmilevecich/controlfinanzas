package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import jakarta.persistence.EntityManager;

public class PanelResumenTarjeta extends JPanel {

	private JComboBox<TarjetaCredito> comboTarjetas;

	private JLabel lblDeudaCiclo;
	private JLabel lblDeuda;
	private JLabel lblDisponible;
	private JLabel lblAFavor;

	private JTable tablaMovimientos;
	private DefaultTableModel modeloTabla;

	private JButton btnPagar;

	private TarjetaCreditoService tarjetaService;

	private Usuario usuario;
	private Runnable actualizar;

	private CuentaService cuentaService;

	public PanelResumenTarjeta(CuentaService cuentaService, EntityManager em) {

		this.usuario = usuario = SesionUsuario.getUsuarioActual();
		tarjetaService = new TarjetaCreditoService(em);
		this.cuentaService = cuentaService;

		setLayout(new BorderLayout());

		inicializarComponentes();
		cargarTarjetas();
	}

	private void inicializarComponentes() {

		JPanel panelSuperior = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panelSuperior.add(new JLabel("Tarjeta:"), gbc);

		gbc.gridx = 1;
		comboTarjetas = new JComboBox<>();
		panelSuperior.add(comboTarjetas, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelSuperior.add(new JLabel("Deuda del mes:"), gbc);
		gbc.gridx = 1;
		lblDeudaCiclo = new JLabel("$0");
		lblDeudaCiclo.setHorizontalAlignment(SwingConstants.RIGHT);
		panelSuperior.add(lblDeudaCiclo, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelSuperior.add(new JLabel("Deuda actual:"), gbc);

		gbc.gridx = 1;
		lblDeuda = new JLabel("$0");
		lblDeuda.setHorizontalAlignment(SwingConstants.RIGHT);
		panelSuperior.add(lblDeuda, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelSuperior.add(new JLabel("Límite disponible:"), gbc);

		gbc.gridx = 1;
		lblDisponible = new JLabel("$0");
		lblDisponible.setHorizontalAlignment(SwingConstants.RIGHT);
		panelSuperior.add(lblDisponible, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelSuperior.add(new JLabel("Saldo a Favor:"), gbc);

		gbc.gridx = 1;
		lblAFavor = new JLabel("$0");
		lblAFavor.setHorizontalAlignment(SwingConstants.RIGHT);
		panelSuperior.add(lblAFavor, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;

		btnPagar = new JButton("Pagar tarjeta");
		panelSuperior.add(btnPagar, gbc);

		add(panelSuperior, BorderLayout.NORTH);

		modeloTabla = new DefaultTableModel(new String[] { "Fecha", "Descripción", "Monto", "Estado" }, 0);

		tablaMovimientos = new JTable(modeloTabla);

		add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);

		comboTarjetas.addActionListener(e -> cargarMovimientos());

		btnPagar.addActionListener(e -> pagarTarjeta());
	}

	private void cargarTarjetas() {

		List<TarjetaCredito> tarjetas = tarjetaService.getTarjetasUsuario(usuario.getUsuarioID());

		DefaultComboBoxModel<TarjetaCredito> model = new DefaultComboBoxModel<>();

		for (TarjetaCredito t : tarjetas) {
			model.addElement(t);
		}

		comboTarjetas.setModel(model);

		if (model.getSize() > 0) {
			comboTarjetas.setSelectedIndex(0);
		}

		cargarMovimientos();
	}

	private void cargarMovimientos() {

		TarjetaCredito tarjeta = (TarjetaCredito) comboTarjetas.getSelectedItem();

		if (tarjeta == null) {
			return;
		}

		List<Movimiento> movimientos = tarjetaService.getMovimientosCiclo(tarjeta);
		List<Movimiento> todos = tarjetaService.getMovimientosTarjeta(tarjeta);

		modeloTabla.setRowCount(0);

		for (Movimiento m : movimientos) {

			modeloTabla.addRow(new Object[] { m.getFecha(), m.getDescripcion(),
					NumeroUtils.formatearMonedaARS(m.getMonto()), m.isPendiente() ? "Pendiente" : "Pagado" });
		}

		BigDecimal deudaCiclo = tarjetaService.obtenerResumenTarjeta(tarjeta).getDeudaMes();
		BigDecimal deudaTotal = tarjetaService.obtenerResumenTarjeta(tarjeta).getDeudaTotal();
		BigDecimal disponible = tarjetaService.obtenerResumenTarjeta(tarjeta).getLimiteDisponible();
		BigDecimal saldoFavor = tarjetaService.obtenerResumenTarjeta(tarjeta).getSaldoFavor();

		lblDeuda.setText(NumeroUtils.formatearMonedaARS(deudaTotal));
		lblDisponible.setText(NumeroUtils.formatearMonedaARS(disponible));
		lblDeudaCiclo.setText(NumeroUtils.formatearMonedaARS(deudaCiclo));
		lblAFavor.setText(NumeroUtils.formatearMonedaARS(saldoFavor));
	}

	private void pagarTarjeta() {

		TarjetaCredito tarjeta = (TarjetaCredito) comboTarjetas.getSelectedItem();

		if (tarjeta == null) {
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "¿Desea pagar la tarjeta?", "Confirmar pago",
				JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(usuario);

		Cuenta cuenta = (Cuenta) JOptionPane.showInputDialog(this, "Seleccione una cuenta:", "Cuenta",
				JOptionPane.QUESTION_MESSAGE, null, cuentas.toArray(), cuentas.get(0));

		if (cuenta == null) {
			return;
		}

		try {

			// elegir tipo de pago
			String[] opciones = { "Pago mínimo", "Pago parcial", "Pago total" };

			String opcion = (String) JOptionPane.showInputDialog(this, "Seleccione tipo de pago:", "Pago de tarjeta",
					JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[2]);

			if (opcion == null) {
				return;
			}

			BigDecimal montoPago;

			if (opcion.equals("Pago total")) {

				montoPago = tarjetaService.calcularDeudaCiclo(tarjeta);

			} else if (opcion.equals("Pago mínimo")) {

				montoPago = tarjetaService.calcularPagoMinimo(tarjeta);

			} else {

				String monto = JOptionPane.showInputDialog(this, "Ingrese el monto a pagar:");

				if (monto == null || monto.isEmpty()) {
					return;
				}

				montoPago = new BigDecimal(monto);
			}

			tarjetaService.pagarTarjeta(tarjeta, cuenta, montoPago);

			if (actualizar != null) {
				actualizar.run();
			}

			JOptionPane.showMessageDialog(this, "Tarjeta pagada correctamente");

			cargarMovimientos();

		} catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage());
		}
	}

	public void setActualizar(Runnable actualizar) {
		this.actualizar = actualizar;
	}

	public void refrescar() {
		cargarTarjetas();
	}
}
