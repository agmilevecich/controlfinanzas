package ar.com.controlfinanzas.ui.inversion;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class PanelIngresosMensuales extends JPanel {

	private JTable tabla;
	private DefaultTableModel model;
	private JLabel lblTotal;

	public PanelIngresosMensuales() {

		setLayout(new BorderLayout());

		model = new DefaultTableModel(new Object[] { "Inversión", "Ingreso mensual estimado" }, 0);

		tabla = new JTable(model);

		lblTotal = new JLabel("Total mensual: 0");

		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(lblTotal, BorderLayout.SOUTH);
	}

	public void refrescar(List<Inversion> inversiones) {

		model.setRowCount(0);

		BigDecimal total = BigDecimal.ZERO;

		for (Inversion inv : inversiones) {

			BigDecimal ingreso = inv.calcularIngresoMensualEstimado();

			model.addRow(new Object[] { inv.getDescripcion(), ingreso });

			if (ingreso != null) {
				total = total.add(ingreso);
			}
		}

		lblTotal.setText("Total mensual: " + total);
	}
}