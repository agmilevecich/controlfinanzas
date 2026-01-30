package ar.com.controlfinanzas.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.ui.PanelVencimientos;

public class MainApp {

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
			inversiones.add(new Inversion(TipoInversion.PLAZO_FIJO_TRADICIONAL, Moneda.ARS, "Plazo Fijo Banco X",
					new BigDecimal("50000"), new BigDecimal("4.5"), LocalDate.now().minusMonths(6),
					LocalDate.now().minusDays(1) // ya vencida
			));

			// Creamos la ventana y el panel
			JFrame frame = new JFrame("Control Finanzas - Vencimientos Dinámico");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(900, 500);

			PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones);
			frame.add(panelVencimientos);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			// Simulamos agregar una nueva inversión después de 5 segundos
			new Thread(() -> {
				try {
					Thread.sleep(5000); // espera 5 segundos
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// Agregamos una inversión nueva
				inversiones.add(new Inversion(TipoInversion.ACCION, Moneda.ARS, "Acción Nueva", new BigDecimal("75000"),
						new BigDecimal("7.0"), LocalDate.now(), LocalDate.now().plusDays(20)));

				// Actualizamos el panel dinámicamente en el hilo de Swing
				SwingUtilities.invokeLater(() -> {
					panelVencimientos.actualizarInversiones(inversiones);
					System.out.println("Se agregó 'Acción Nueva' y el panel se actualizó.");
				});
			}).start();

		});
	}
}
