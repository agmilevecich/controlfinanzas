package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ar.com.controlfinanzas.controller.InversionController;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.repository.GastoRepository;
import ar.com.controlfinanzas.repository.InversionRepositoryJPA;
import ar.com.controlfinanzas.repository.interfaces.InversionRepository;
import ar.com.controlfinanzas.service.AlertaService;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.IngresoService;
import ar.com.controlfinanzas.service.InversionService;
import ar.com.controlfinanzas.ui.inversion.PanelVencimiento;

public class DashboardFrame extends JFrame {

	private List<Inversion> inversiones;

	// Paneles que necesitan refresco
	private PanelAlertas panelAlertas;
	private PanelResumenFinanciero panelResumen;
	private PanelVencimientosGraficos panelVencimientosGraficos;

	// Servicios
	private final AlertaService alertaService;
	private final InversionRepository inversionRepository;
	private final InversionService inversionService;
	private final InversionController inversionController;

	private final GastoRepository gastoRepository;
	private final GastoService gastoService;
	private final PanelResumenGastos panelResumenGastos;

	private final IngresoService ingresoService;

	private PanelVencimiento panelVencimiento;

	public DashboardFrame() {

		this.alertaService = new AlertaService();
		this.inversionRepository = new InversionRepositoryJPA();
		this.inversionService = new InversionService(inversionRepository);
		this.inversionController = new InversionController(inversionService);
		this.gastoRepository = new GastoRepository();
		this.gastoService = new GastoService(gastoRepository);
		this.panelResumenGastos = new PanelResumenGastos(gastoService);
		this.ingresoService = new IngresoService();
		panelResumen = new PanelResumenFinanciero(inversionService, gastoService, ingresoService);

		setTitle("Control Finanzas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// ===============================
		// Paneles
		// ===============================

		PanelGastos panelGastos = new PanelGastos(gastoService, panelResumen, panelResumenGastos);
		panelAlertas = new PanelAlertas();
		panelVencimiento = new PanelVencimiento();
		panelVencimientosGraficos = new PanelVencimientosGraficos(List.of());

		PanelInversionesAvanzado panelInversiones = new PanelInversionesAvanzado(inversionController, panelVencimiento);

		inversionController.addListener(() -> {
			onInversionesActualizadas();
		});

		// ===============================
		// Tabs
		// ===============================
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Resumen", panelResumen);
		tabs.addTab("Resumen Gastos", panelResumenGastos);
		tabs.addTab("Gastos", panelGastos);
		tabs.addTab("Inversiones", panelInversiones);
		tabs.addTab("Vencimientos", panelVencimientosGraficos);
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
		panelVencimiento.refrescar(inversiones);
		panelVencimientosGraficos.actualizarInversiones(inversiones);
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
