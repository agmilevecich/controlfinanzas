package ar.com.controlfinanzas.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;
import ar.com.controlfinanzas.projection.CalculadoraProyeccionSimple;
import ar.com.controlfinanzas.ui.DashboardFrame;
import ar.com.controlfinanzas.ui.PanelDistribucion;
import ar.com.controlfinanzas.ui.PanelEvolucionPatrimonio;
import ar.com.controlfinanzas.ui.PanelVencimientos;

public class MainApp {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {

			// -----------------------------
			// 1️⃣ Crear inversiones de prueba
			// -----------------------------
			Inversion inv1 = new Inversion(TipoInversion.FONDO_COMUN_INVERSION, Moneda.ARS, "Fondo Común 1",
					new BigDecimal("100000"), new BigDecimal("0.05"), LocalDate.now().minusDays(60), null);

			Inversion inv2 = new Inversion(TipoInversion.BONO, Moneda.ARS, "Obligación Negociable",
					new BigDecimal("50000"), new BigDecimal("0.06"), LocalDate.now().minusDays(30),
					LocalDate.now().plusDays(180));

			// Inversiones para test de vencimientos
			Inversion inv3 = new Inversion(TipoInversion.FONDO_COMUN_INVERSION, Moneda.ARS,
					"Fondo Próximo a Vencer (5 días)", new BigDecimal("30000"), new BigDecimal("0.03"),
					LocalDate.now().minusDays(10), LocalDate.now().plusDays(5));

			Inversion inv4 = new Inversion(TipoInversion.BONO, Moneda.ARS, "Bono Próximo a Vencer (12 días)",
					new BigDecimal("20000"), new BigDecimal("0.04"), LocalDate.now().minusDays(20),
					LocalDate.now().plusDays(12));

			Inversion inv5 = new Inversion(TipoInversion.OBLIGACION_NEGOCIABLE, Moneda.ARS,
					"ON Próxima a Vencer (25 días)", new BigDecimal("40000"), new BigDecimal("0.05"),
					LocalDate.now().minusDays(15), LocalDate.now().plusDays(25));

			List<Inversion> inversiones = Arrays.asList(inv1, inv2, inv3, inv4, inv5);

			// -----------------------------
			// 2️⃣ Crear cuentas/billeteras
			// -----------------------------
			Cuenta cuenta1 = new Cuenta("Billetera X", new BigDecimal("20000"), 0.001, LocalDate.now().minusDays(15));
			Cuenta cuenta2 = new Cuenta("Cuenta Banco", new BigDecimal("50000"), 0.0005, LocalDate.now().minusDays(30));

			List<Cuenta> cuentas = Arrays.asList(cuenta1, cuenta2);

			// -----------------------------
			// 3️⃣ Calculadora de proyecciones
			// -----------------------------
			CalculadoraProyeccionSimple calculadora = new CalculadoraProyeccionSimple();

			// -----------------------------
			// 4️⃣ Panel de evolución del patrimonio
			// -----------------------------
			PanelEvolucionPatrimonio panelPatrimonio = new PanelEvolucionPatrimonio(inversiones, cuentas, calculadora,
					LocalDate.now().minusDays(90), LocalDate.now());

			// -----------------------------
			// 5️⃣ Dashboard
			// -----------------------------
			DashboardFrame dashboard = new DashboardFrame();

			// Pestaña Patrimonio
			dashboard.getTabbedPane().setComponentAt(0, panelPatrimonio);

			// Pestaña Distribución
			PanelDistribucion panelDistribucion = new PanelDistribucion(inversiones);
			dashboard.getTabbedPane().setComponentAt(1, panelDistribucion);

			// Pestaña Vencimientos
			PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones, LocalDate.now());
			dashboard.getTabbedPane().setComponentAt(2, panelVencimientos);

			dashboard.setVisible(true);
		});
	}
}
