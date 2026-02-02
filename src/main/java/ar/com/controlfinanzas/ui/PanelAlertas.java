package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.model.Alerta;

public class PanelAlertas extends JPanel {

	private JTable tabla;
	private DefaultTableModel modelo;

	public PanelAlertas(List<Alerta> alertas) {

		setLayout(new BorderLayout());

		modelo = new DefaultTableModel(new Object[] { "Tipo", "Descripción", "Fecha" }, 0);

		tabla = new JTable(modelo);
		add(new JScrollPane(tabla), BorderLayout.CENTER);

		cargarAlertas(alertas);
	}

	public PanelAlertas() {
		setLayout(new BorderLayout());

		modelo = new DefaultTableModel(new Object[] { "Tipo", "Descripción", "Fecha" }, 0);

		tabla = new JTable(modelo);
		add(new JScrollPane(tabla), BorderLayout.CENTER);
	}

	private void cargarAlertas(List<Alerta> alertas) {
		modelo.setRowCount(0);

		for (Alerta alerta : alertas) {
			modelo.addRow(new Object[] { alerta.getTipo(), alerta.getDescripcion(), alerta.getFechaEvento() });
		}
	}

	public void actualizarAlertas(List<Alerta> alertas) {
		modelo.setRowCount(0);
		for (Alerta alerta : alertas) {
			modelo.addRow(new Object[] { alerta.getTipo(), alerta.getDescripcion(), alerta.getFechaEvento() });
		}
	}
}
