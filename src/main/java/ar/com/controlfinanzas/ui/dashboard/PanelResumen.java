package ar.com.controlfinanzas.ui.dashboard;

import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanelResumen extends JPanel {

	private JLabel lblPatrimonio = new JLabel();
	private JLabel lblInvertido = new JLabel();
	private JLabel lblPnL = new JLabel();
	private JLabel lblIngreso = new JLabel();

	public PanelResumen() {

		setLayout(new GridLayout(4, 1, 10, 10));

		add(lblPatrimonio);
		add(lblInvertido);
		add(lblPnL);
		add(lblIngreso);
	}

	public void refrescar(BigDecimal patrimonio, BigDecimal invertido, BigDecimal pnl, BigDecimal ingresoMensual) {

		lblPatrimonio.setText("Patrimonio total: " + format(patrimonio));
		lblInvertido.setText("Total invertido: " + format(invertido));
		lblPnL.setText("Ganancia / Pérdida: " + format(pnl));
		lblIngreso.setText("Ingreso mensual estimado: " + format(ingresoMensual));
	}

	private String format(BigDecimal v) {
		return v.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
	}
}