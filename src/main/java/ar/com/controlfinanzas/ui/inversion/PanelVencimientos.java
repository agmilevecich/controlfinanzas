package ar.com.controlfinanzas.ui.inversion;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class PanelVencimientos extends JPanel {

	private JTable tabla;
	private DefaultTableModel model;

	public PanelVencimientos() {

		setLayout(new BorderLayout());

		model = new DefaultTableModel(new Object[] { "Inversión", "Fecha venc.", "Capital", "Interés", "Total" }, 0);

		tabla = new JTable(model);

		add(new JScrollPane(tabla), BorderLayout.CENTER);
	}

	public void refrescar(List<Inversion> inversiones) {

		model.setRowCount(0);

		inversiones.stream().filter(i -> i.getFechaVencimiento() != null)
				.sorted((a, b) -> a.getFechaVencimiento().compareTo(b.getFechaVencimiento())).forEach(i -> {

					BigDecimal capital = safe(i.getCapitalTotalCalculado());
					BigDecimal interes = safe(i.calcularInteresAlVencimiento());
					BigDecimal total = capital.add(interes);

					model.addRow(new Object[] { i.getDescripcion(), i.getFechaVencimiento(), capital, interes, total });
				});
	}

	private BigDecimal safe(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}
}