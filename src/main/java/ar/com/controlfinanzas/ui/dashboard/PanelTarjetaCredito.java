package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.SesionUsuario;
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
	private JButton btnEliminar;
	private JButton btnIrAgastos;

	private DefaultListModel<TarjetaCredito> modeloTarjetas;
	private JList<TarjetaCredito> listaTarjetas;

	private TarjetaCreditoService tarjetaService;
	private BancoService bancoService;

	private Usuario usuarioActual;

	private Runnable actualizar;
	private Consumer<TarjetaCredito> onRegistrarGasto;

	public PanelTarjetaCredito(EntityManager em) {

		this.usuarioActual = SesionUsuario.getUsuarioActual();
		this.tarjetaService = new TarjetaCreditoService(em);
		this.bancoService = new BancoService(em);

		setLayout(new BorderLayout());

		inicializarComponentes();
		cargarBancos();
		cargarTarjetas();
	}

	private void inicializarComponentes() {

		JPanel formulario = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 2, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// =========================
		// CAMPOS
		// =========================

		gbc.gridx = 0;
		gbc.gridy = 0;
		formulario.add(new JLabel("Nombre Tarjeta:"), gbc);

		gbc.gridx = 1;
		txtNombre = new JTextField(15);
		formulario.add(txtNombre, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Límite:"), gbc);

		gbc.gridx = 1;
		txtLimite = new JTextField();
		formulario.add(txtLimite, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Banco:"), gbc);

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

		formulario.add(comboBanco, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Día de Cierre:"), gbc);

		gbc.gridx = 1;
		txtDiaCierre = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
		formulario.add(txtDiaCierre, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		formulario.add(new JLabel("Día de Vencimiento:"), gbc);

		gbc.gridx = 1;
		txtDiaVencimiento = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
		formulario.add(txtDiaVencimiento, gbc);

		// =========================
		// BOTONES
		// =========================

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;

		btnGuardar = new JButton("Guardar Tarjeta");
		formulario.add(btnGuardar, gbc);

		gbc.gridy++;
		btnEliminar = new JButton("Eliminar Tarjeta");
		formulario.add(btnEliminar, gbc);

		add(formulario, BorderLayout.NORTH);

		gbc.gridy++;
		btnIrAgastos = new JButton("Registrar Gasto");
		formulario.add(btnIrAgastos, gbc);
		// =========================
		// LISTA
		// =========================

		modeloTarjetas = new DefaultListModel<>();
		listaTarjetas = new JList<>(modeloTarjetas);

		listaTarjetas.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof TarjetaCredito t) {
					lbl.setText(t.getNombre() + " - " + t.getBanco().getNombre());
				}

				return lbl;
			}
		});

		JPanel panelLista = new JPanel(new BorderLayout());
		panelLista.add(new JLabel("Tarjetas"), BorderLayout.NORTH);
		panelLista.add(new JScrollPane(listaTarjetas), BorderLayout.CENTER);

		add(panelLista, BorderLayout.CENTER);

		// =========================
		// EVENTOS
		// =========================

		btnGuardar.addActionListener(e -> guardarTarjeta());
		btnEliminar.addActionListener(e -> eliminarTarjeta());
		btnIrAgastos.addActionListener(e -> irAgastos());
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

	public void cargarTarjetas() {
		modeloTarjetas.clear();

		List<TarjetaCredito> tarjetas = tarjetaService.getTarjetasUsuario(usuarioActual.getUsuarioID());

		for (TarjetaCredito t : tarjetas) {
			modeloTarjetas.addElement(t);
		}
	}

	public void setActualizarTarjeta(Runnable actualizar) {
		this.actualizar = actualizar;
	}

	private void guardarTarjeta() {

		try {

			String nombre = txtNombre.getText();

			if (nombre.isBlank()) {
				JOptionPane.showMessageDialog(this, "Debe ingresar un nombre de tarjeta");
				return;
			}

			BigDecimal limite = NumeroUtils.parse(txtLimite.getText());

			int diaCierre = (int) txtDiaCierre.getValue();
			int diaVencimiento = (int) txtDiaVencimiento.getValue();

			Banco bancoSeleccionado = (Banco) comboBanco.getSelectedItem();

			if (bancoSeleccionado == null) {
				JOptionPane.showMessageDialog(this, "Debe seleccionar un banco");
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

			cargarTarjetas();

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

	private void eliminarTarjeta() {

		TarjetaCredito t = listaTarjetas.getSelectedValue();

		if (t == null) {
			JOptionPane.showMessageDialog(this, "Seleccioná una tarjeta");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar tarjeta?");

		if (confirm == JOptionPane.YES_OPTION) {
//			tarjetaService.eliminar(t.getId());

			cargarTarjetas();

			if (actualizar != null) {
				actualizar.run();
			}
		}
	}

	private void irAgastos() {

		TarjetaCredito tarjeta = listaTarjetas.getSelectedValue();

		if (tarjeta == null) {
			JOptionPane.showMessageDialog(this, "Seleccioná una tarjeta");
			return;
		}

		if (onRegistrarGasto != null) {
			onRegistrarGasto.accept(tarjeta);
		}

	}

	public void setOnRegistrarGasto(Consumer<TarjetaCredito> onRegistrarGasto) {
		this.onRegistrarGasto = onRegistrarGasto;
	}

	private void limpiarFormulario() {
		txtNombre.setText("");
		txtLimite.setText("");
		txtDiaCierre.setValue(1);
		txtDiaVencimiento.setValue(1);
	}
}