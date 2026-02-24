package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.com.controlfinanzas.model.Posicion;
import ar.com.controlfinanzas.service.PosicionService;

public class PanelPosiciones extends JPanel {

	private JTable tabla;
	private DefaultTableModel model;

	private PosicionService service;

	public PanelPosiciones(PosicionService service) {

		this.service = service;

		setLayout(new BorderLayout());

		model = new DefaultTableModel(new Object[] { "Activo", "Tipo", "Cantidad", "Capital", "Precio Promedio" }, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		tabla = new JTable(model);

		add(new JScrollPane(tabla), BorderLayout.CENTER);
	}

	public void refrescar(Integer usuarioId) {

		model.setRowCount(0);

		List<Posicion> posiciones = service.calcular(usuarioId);

		for (Posicion p : posiciones) {

			model.addRow(new Object[] { p.getClave(), p.getTipoActivo(), format(p.getCantidadTotal()),
					format(p.getCapitalTotal()), format(p.getPrecioPromedio()) });
		}
	}

	private String format(BigDecimal v) {
		if (v == null) {
			return "0";
		}
		return v.stripTrailingZeros().toPlainString();
	}
}