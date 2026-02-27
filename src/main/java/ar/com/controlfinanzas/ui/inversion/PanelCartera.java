package ar.com.controlfinanzas.ui.inversion;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.model.Posicion;

public class PanelCartera extends JPanel {

	private DefaultTableModel modelo;
	private JTable tabla;

	public PanelCartera() {

		setLayout(new BorderLayout());

		modelo = new DefaultTableModel(new Object[] { "Activo", "Invertido", "Valor actual", "PnL", "Ingreso mensual" },
				0);

		tabla = new JTable(modelo);

		add(new JScrollPane(tabla), BorderLayout.CENTER);
	}

	public void refrescar(List<Posicion> posiciones) {

		modelo.setRowCount(0);

		for (Posicion p : posiciones) {

			BigDecimal invertido = p.getCapitalInvertido();
			BigDecimal actual = p.getValorActual();
			BigDecimal pnl = p.getPnL();
			BigDecimal ingreso = p.getIngresoMensual();

			modelo.addRow(new Object[] { p.getClave(), invertido, actual, pnl, ingreso });
		}
	}
}