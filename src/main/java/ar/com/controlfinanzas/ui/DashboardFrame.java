package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ar.com.controlfinanzas.controller.InversionController;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.service.AlertaService;
import ar.com.controlfinanzas.service.InversionService;

public class DashboardFrame extends JFrame {

	private List<Inversion> inversiones;

	// Paneles que necesitan refresco
	private PanelAlertas panelAlertas;
	private PanelResumenFinanciero panelResumen;
	private PanelVencimientos panelVencimientos;

	// Servicios
	private final AlertaService alertaService;
	private final InversionService inversionService;
	private final InversionController inversionController;

	public DashboardFrame() {

		this.alertaService = new AlertaService();
		this.inversionService = new InversionService();
		this.inversionController = new InversionController(inversionService);

		setTitle("Control Finanzas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// ===============================
		// Paneles
		// ===============================
		panelResumen = new PanelResumenFinanciero();
		PanelGastos panelGastos = new PanelGastos(panelResumen);
		panelAlertas = new PanelAlertas();

		PanelInversionesAvanzado panelInversiones = new PanelInversionesAvanzado(inversionController);

		panelVencimientos = new PanelVencimientos(List.of());

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

		// ===============================
		// Carga inicial
		// ===============================
		refrescarEstadoFinanciero();
	}

	// ==================================================
	// MÉTODO CENTRAL (orquestador real)
	// ==================================================
	public void refrescarEstadoFinanciero() {
		cargarInversiones();
		actualizarAlertas();
		actualizarVencimientos();
		actualizarResumen();
	}

	private void actualizarResumen() {
		panelResumen.actualizarResumen();
	}

	private void cargarInversiones() {
		try {
			inversiones = inversionService.obtenerTodas();
		} catch (Exception e) {
			e.printStackTrace();
			inversiones = List.of();
		}
	}

	private void actualizarAlertas() {
		panelAlertas.actualizarAlertas(alertaService.generarAlertasInversiones(inversiones));
	}

	private void actualizarVencimientos() {
		panelVencimientos.actualizarInversiones(inversiones);
	}

	// ==================================================
	// API PARA OTROS PANELES
	// ==================================================
	public List<Inversion> getInversiones() {
		return inversiones;
	}

	public void onInversionesActualizadas() {
		refrescarEstadoFinanciero();
	}
}
