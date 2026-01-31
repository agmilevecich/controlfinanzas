package ar.com.controlfinanzas.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.ui.PanelGastos;
import ar.com.controlfinanzas.ui.PanelVencimientos;

public class MainAppTabbed {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {

			// 1️⃣ Lista de inversiones de ejemplo
			List<Inversion> inversiones = new ArrayList<>();
			inversiones.add(
					new Inversion(TipoInversion.FONDO_COMUN_INVERSION, Moneda.ARS, "Fondo A", new BigDecimal("100000"),
							new BigDecimal("5.0"), LocalDate.now().minusMonths(1), LocalDate.now().plusDays(10)));
			inversiones.add(new Inversion(TipoInversion.OBLIGACION_NEGOCIABLE, Moneda.ARS, "OBL River",
					new BigDecimal("200000"), new BigDecimal("6.0"), LocalDate.now().minusMonths(2),
					LocalDate.now().plusDays(3)));
			inversiones.add(new Inversion(TipoInversion.PLAZO_FIJO_TRADICIONAL, Moneda.ARS, "Plazo Fijo Banco X",
					new BigDecimal("50000"), new BigDecimal("4.5"), LocalDate.now().minusMonths(6),
					LocalDate.now().minusDays(1)));

			// 2️⃣ Crear ventana principal
			JFrame frame = new JFrame("Control Finanzas");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(900, 500);
			frame.setLocationRelativeTo(null);

			// 3️⃣ Crear pestañas
			JTabbedPane tabbedPane = new JTabbedPane();

			PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones);
			PanelGastos panelGastos = new PanelGastos();

			tabbedPane.addTab("Vencimientos", panelVencimientos);
			tabbedPane.addTab("Gastos", panelGastos);

			frame.add(tabbedPane);
			frame.setVisible(true);

			// 4️⃣ Timer para refrescar inversiones cada segundo
			Timer timer = new Timer(1000, e -> panelVencimientos.actualizarInversiones(inversiones));
			timer.start();
		});
	}
}
