package ar.com.controlfinanzas.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.ui.PanelGastos;
import ar.com.controlfinanzas.ui.PanelResumenFinanciero;
import ar.com.controlfinanzas.ui.PanelVencimientos;

public class MainDashboard {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {

			// Lista inicial de inversiones
			List<Inversion> inversiones = new ArrayList<>();
			inversiones.add(
					new Inversion(TipoInversion.FONDO_COMUN_INVERSION, Moneda.ARS, "Fondo A", new BigDecimal("100000"),
							new BigDecimal("5.0"), LocalDate.now().minusMonths(1), LocalDate.now().plusDays(10)));
			inversiones.add(new Inversion(TipoInversion.OBLIGACION_NEGOCIABLE, Moneda.ARS, "OBL River",
					new BigDecimal("200000"), new BigDecimal("6.0"), LocalDate.now().minusMonths(2),
					LocalDate.now().plusDays(3)));

			// Crear frame principal
			JFrame frame = new JFrame("Control Finanzas - Dashboard");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(1000, 600);
			frame.setLocationRelativeTo(null);

			// Crear panel resumen primero (para pasarlo a PanelGastos)
			PanelResumenFinanciero panelResumen = new PanelResumenFinanciero();

			// Crear paneles
			PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones);
			PanelGastos panelGastos = new PanelGastos(panelResumen); // referencia al resumen

			// Crear pestañas
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Vencimientos", panelVencimientos);
			tabbedPane.addTab("Gastos", panelGastos);
			tabbedPane.addTab("Resumen", panelResumen);

			frame.add(tabbedPane);
			frame.setVisible(true);

			// Actualización dinámica ejemplo: agregar inversión luego de 5s
			new Thread(() -> {
				try {
					Thread.sleep(5000);
					inversiones.add(
							new Inversion(TipoInversion.ACCION, Moneda.ARS, "Acción Nueva", new BigDecimal("75000"),
									new BigDecimal("7.0"), LocalDate.now(), LocalDate.now().plusDays(20)));

					SwingUtilities.invokeLater(() -> {
						panelVencimientos.actualizarInversiones(inversiones);
						panelResumen.actualizarResumen();
						System.out.println("Se agregó 'Acción Nueva' y se actualizaron paneles.");
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		});
	}
}
