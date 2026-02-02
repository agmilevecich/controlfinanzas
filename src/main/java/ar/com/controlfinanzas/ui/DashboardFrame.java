package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ar.com.controlfinanzas.dao.InversionDAO;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.service.AlertaService;

public class DashboardFrame extends JFrame {

	public DashboardFrame() {

		setTitle("Control Finanzas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// ===============================
		// Carga inicial de inversiones
		// ===============================
		List<Inversion> inversiones;

		try {
			InversionDAO inversionDAO = new InversionDAO();
			inversiones = inversionDAO.listarInversiones();
		} catch (Exception e) {
			e.printStackTrace();
			inversiones = List.of();
		}

		// ===============================
		// Paneles
		// ===============================
		PanelResumenFinanciero panelResumen = new PanelResumenFinanciero();
		PanelGastos panelGastos = new PanelGastos(panelResumen);

		PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones);

		PanelAlertas panelAlertas = new PanelAlertas();
		AlertaService alertaService = new AlertaService();
		panelAlertas.actualizarAlertas(alertaService.generarAlertasInversiones(inversiones));

		PanelInversionesAvanzado panelInversiones = new PanelInversionesAvanzado(panelVencimientos, panelResumen,
				panelAlertas);

		// ===============================
		// Tabs
		// ===============================
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Resumen", panelResumen);
		tabs.addTab("Gastos", panelGastos);
		tabs.addTab("Inversiones", panelInversiones);
		tabs.addTab("Vencimientos", panelVencimientos);
		tabs.addTab("Alertas", panelAlertas);

		add(tabs, BorderLayout.CENTER);
	}
}
