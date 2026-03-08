package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import jakarta.persistence.EntityManager;

public class PanelTarjetaCredito extends JPanel {

	private JTextField txtNombre;
	private JTextField txtLimite;
	private JSpinner txtDiaCierre;
	private JSpinner txtDiaVencimiento;

	private JComboBox<Cuenta> comboCuentas;

	private JButton btnGuardar;

	private TarjetaCreditoService tarjetaService;
	private CuentaService cuentaService;

	private Usuario usuarioActual;

	private Runnable actualizar;
	private EntityManager em;

	public PanelTarjetaCredito(Usuario usuario, EntityManager em) {

		this.usuarioActual = usuario;
		this.em = em;

		tarjetaService = new TarjetaCreditoService(em);
		cuentaService = new CuentaService(em);

		setLayout(new BorderLayout());

		inicializarComponentes();
		cargarCuentas();
	}

	private void inicializarComponentes() {

		JPanel formulario = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 2, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		formulario.add(new JLabel("Nombre Tarjeta:"), gbc);
		gbc.gridx = 1;
		txtNombre = new JTextField(15);
		formulario.add(txtNombre, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Límite:"), gbc);
		txtLimite = new JTextField();
		gbc.gridx = 1;
		formulario.add(txtLimite, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Día de Cierre:"), gbc);
		txtDiaCierre = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
		gbc.gridx = 1;
		formulario.add(txtDiaCierre, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Día de Vencimiento:"), gbc);
		txtDiaVencimiento = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
		gbc.gridx = 1;
		formulario.add(txtDiaVencimiento, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Cuenta asociada:"), gbc);
		gbc.gridx = 1;
		comboCuentas = new JComboBox<>();
		comboCuentas.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof Cuenta c) {
					lbl.setText(
							c.getNombre() + " " + c.getMoneda() + " " + NumeroUtils.formatearMonedaARS(c.getSaldo()));
				}

				return lbl;
			}
		});

		comboCuentas.revalidate();
		comboCuentas.repaint();
		formulario.add(comboCuentas, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		btnGuardar = new JButton("Guardar Tarjeta");
		formulario.add(btnGuardar, gbc);

		add(formulario, BorderLayout.NORTH);

		btnGuardar.addActionListener(e -> guardarTarjeta());
	}

	private void cargarCuentas() {

		List<Cuenta> cuentas = cuentaService.getCuentasUsuario(usuarioActual);

		comboCuentas.removeAllItems();

		for (Cuenta cuenta : cuentas) {
			comboCuentas.addItem(cuenta);
		}
	}

	public void actualizarCuentas() {
		cargarCuentas();
	}

	public void setActualizarTarjeta(Runnable actualizar) {
		this.actualizar = actualizar;
	}

	private void guardarTarjeta() {

		try {

			String nombre = txtNombre.getText();
			if (txtNombre.getText().isBlank()) {
				JOptionPane.showMessageDialog(this, "Debe ingresar un nombre de tarjeta");
				return;
			}

			BigDecimal limite = NumeroUtils.parse(txtLimite.getText());

			int diaCierre = (int) txtDiaCierre.getValue();

			int diaVencimiento = (int) txtDiaVencimiento.getValue();

			Cuenta cuentaSeleccionada = (Cuenta) comboCuentas.getSelectedItem();

			if (cuentaSeleccionada == null) {
				JOptionPane.showMessageDialog(this, "Debe seleccionar una cuenta");
				return;
			}

			TarjetaCredito tarjeta = new TarjetaCredito();

			tarjeta.setNombre(nombre);
			tarjeta.setLimite(limite);
			tarjeta.setDiaCierre(diaCierre);
			tarjeta.setDiaVencimiento(diaVencimiento);
			tarjeta.setCuenta(cuentaSeleccionada);
			tarjeta.setUsuario(usuarioActual);

			tarjetaService.guardar(tarjeta);

			if (actualizar != null) {
				actualizar.run();
			}

			JOptionPane.showMessageDialog(this, "Tarjeta guardada correctamente");

			limpiarFormulario();

		} catch (Exception ex) {

			JOptionPane.showMessageDialog(this, "Error al guardar tarjeta");
			ex.printStackTrace();
		}
	}

	private void limpiarFormulario() {

		txtNombre.setText("");
		txtLimite.setText("");
		txtDiaCierre.setValue(1);
		txtDiaVencimiento.setValue(1);
	}
}