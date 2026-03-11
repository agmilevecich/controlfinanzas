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

import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.util.NumeroUtils;
import jakarta.persistence.EntityManager;

public class PanelTarjetaCredito extends JPanel {

	private JTextField txtNombre;
	private JTextField txtLimite;
	private JSpinner txtDiaCierre;
	private JSpinner txtDiaVencimiento;

	private JComboBox<Banco> comboBanco;

	private JButton btnGuardar;

	private TarjetaCreditoService tarjetaService;
	private BancoService bancoService;

	private Usuario usuarioActual;

	private Runnable actualizar;

	public PanelTarjetaCredito(Usuario usuario, EntityManager em) {

		this.usuarioActual = usuario;
		tarjetaService = new TarjetaCreditoService(em);
		bancoService = new BancoService(em);

		setLayout(new BorderLayout());

		inicializarComponentes();
		cargarBancos();
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
		comboBanco = new JComboBox<>();
		comboBanco.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof Banco b) {
					lbl.setText(b.getNombre());
				}

				return lbl;
			}
		});

		comboBanco.revalidate();
		comboBanco.repaint();
		formulario.add(comboBanco, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		btnGuardar = new JButton("Guardar Tarjeta");
		formulario.add(btnGuardar, gbc);

		add(formulario, BorderLayout.NORTH);

		btnGuardar.addActionListener(e -> guardarTarjeta());
	}

	private void cargarBancos() {

		List<Banco> bancos = bancoService.getBancosUsuario(usuarioActual);

		comboBanco.removeAllItems();

		for (Banco banco : bancos) {
			comboBanco.addItem(banco);
		}
	}

	public void actualizarBancos() {
		cargarBancos();
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

			Banco bancoSeleccionado = (Banco) comboBanco.getSelectedItem();

			if (bancoSeleccionado == null) {
				JOptionPane.showMessageDialog(this, "Debe seleccionar una cuenta");
				return;
			}

			TarjetaCredito tarjeta = new TarjetaCredito();

			tarjeta.setNombre(nombre);
			tarjeta.setLimite(limite);
			tarjeta.setDiaCierre(diaCierre);
			tarjeta.setDiaVencimiento(diaVencimiento);
			tarjeta.setBanco(bancoSeleccionado);
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