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
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import jakarta.persistence.EntityManager;

public class PanelResumenTarjeta extends JPanel {

	private JComboBox<TarjetaCredito> comboTarjetas;

	private JLabel lblDeuda;
	private JLabel lblDisponible;

	private JTable tablaMovimientos;
	private DefaultTableModel modeloTabla;

	private JButton btnPagar;

	private TarjetaCreditoService tarjetaService;

	private Usuario usuario;
	private EntityManager em;

	public PanelResumenTarjeta(Usuario usuario, EntityManager em) {

		this.usuario = usuario;
		this.em = em;

		tarjetaService = new TarjetaCreditoService(em);

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
		panelSuperior.add(new JLabel("Deuda actual:"), gbc);

		gbc.gridx = 1;
		lblDeuda = new JLabel("$0");
		panelSuperior.add(lblDeuda, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		panelSuperior.add(new JLabel("Límite disponible:"), gbc);

		gbc.gridx = 1;
		lblDisponible = new JLabel("$0");
		panelSuperior.add(lblDisponible, gbc);

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

		modeloTabla.setRowCount(0);

		BigDecimal deuda = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			if (m.isPendiente()) {
				deuda = deuda.add(m.getMonto());
			}

			modeloTabla.addRow(new Object[] { m.getFecha(), m.getDescripcion(),
					NumeroUtils.formatearMonedaARS(m.getMonto()), m.isPendiente() ? "Pendiente" : "Pagado" });
		}

		lblDeuda.setText(NumeroUtils.formatearMonedaARS(deuda));

		BigDecimal disponible = tarjeta.getLimite().subtract(deuda);

		lblDisponible.setText(NumeroUtils.formatearMonedaARS(disponible));
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

		try {

			tarjetaService.pagarTarjeta(tarjeta);

			JOptionPane.showMessageDialog(this, "Tarjeta pagada correctamente");

			cargarMovimientos();

		} catch (Exception ex) {

			ex.printStackTrace();

			JOptionPane.showMessageDialog(this, "Error al pagar tarjeta");
		}
	}

	public void refrescar() {
		cargarTarjetas();
	}
}
