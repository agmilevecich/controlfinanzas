package ar.com.controlfinanzas.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.ui.PanelVencimientos;

public class MainApp {

	public static void main(String[] args) {

		// Creamos algunas inversiones de prueba
		List<Inversion> inversiones = new ArrayList<>();

		inversiones
				.add(new Inversion(TipoInversion.FONDO_COMUN_INVERSION, Moneda.ARS, "Fondo A", new BigDecimal("100000"),
						new BigDecimal("5.0"), LocalDate.now().minusMonths(1), LocalDate.now().plusDays(10)));

		inversiones.add(
				new Inversion(TipoInversion.OBLIGACION_NEGOCIABLE, Moneda.ARS, "OBL River", new BigDecimal("200000"),
						new BigDecimal("6.0"), LocalDate.now().minusMonths(2), LocalDate.now().plusDays(3)));

		inversiones.add(new Inversion(TipoInversion.PLAZO_FIJO_TRADICIONAL, Moneda.ARS, "Plazo Fijo Banco X",
				new BigDecimal("50000"), new BigDecimal("4.5"), LocalDate.now().minusMonths(6),
				LocalDate.now().minusDays(1) // ya
												// vencida
		));

		// Creamos la ventana principal
		JFrame frame = new JFrame("Control Finanzas - Vencimientos");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(900, 500);

		// Creamos el panel de vencimientos con la lista de inversiones
		PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones);
		frame.add(panelVencimientos);

		frame.setLocationRelativeTo(null); // centra la ventana
		frame.setVisible(true);
	}
}
