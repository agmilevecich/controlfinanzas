package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ar.com.controlfinanzas.controller.InversionController;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Posicion;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.repository.GastoRepository;
import ar.com.controlfinanzas.repository.InversionRepositoryJPA;
import ar.com.controlfinanzas.repository.interfaces.InversionRepository;
import ar.com.controlfinanzas.service.AlertaService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.GastoService;
import ar.com.controlfinanzas.service.IngresoService;
import ar.com.controlfinanzas.service.InversionService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.PosicionService;
import ar.com.controlfinanzas.service.ResumenService;
import ar.com.controlfinanzas.service.UsuarioService;
import ar.com.controlfinanzas.ui.dashboard.PanelCuentas;
import ar.com.controlfinanzas.ui.dashboard.PanelMovimientos;
import ar.com.controlfinanzas.ui.dashboard.PanelResumen;
import ar.com.controlfinanzas.ui.inversion.PanelCartera;
import ar.com.controlfinanzas.ui.inversion.PanelVencimiento;

public class DashboardFrame extends JFrame {

	private List<Inversion> inversiones;
	private List<Posicion> posiciones;
	private List<Cuenta> cuentas;

	private PanelCartera panelCartera;
	private PanelResumen panelResumenKPIs;

	private PanelAlertas panelAlertas;
	private PanelResumenFinanciero panelResumen;
	private PanelVencimientosGraficos panelVencimientosGraficos;

	private final AlertaService alertaService;
	private final InversionRepository inversionRepository;
	private final InversionService inversionService;
	private final InversionController inversionController;

	private final GastoRepository gastoRepository;
	private final GastoService gastoService;
	private final PanelResumenGastos panelResumenGastos;

	private final IngresoService ingresoService;

	private PanelVencimiento panelVencimiento;

	private Usuario usuario;
	private UsuarioService usuarioService;

	private CuentaService cuentaService;
	private MovimientoService movimientoService;

	private PanelCuentas panelCuentas;
	private PanelMovimientos panelMovimientos;

	public DashboardFrame(Usuario usuario, UsuarioService usuarioService, CuentaService cuentaService,
			MovimientoService movimientoService) {

		this.usuario = usuario;
		this.usuarioService = usuarioService;
		this.cuentaService = cuentaService;

		this.alertaService = new AlertaService();
		this.inversionRepository = new InversionRepositoryJPA();
		this.inversionService = new InversionService(inversionRepository, usuario);
		this.inversionController = new InversionController(inversionService);
		this.gastoRepository = new GastoRepository();
		this.gastoService = new GastoService(gastoRepository, usuario);
		this.panelResumenGastos = new PanelResumenGastos(gastoService, usuario);
		this.ingresoService = new IngresoService();

		panelResumen = new PanelResumenFinanciero(inversionService, gastoService, ingresoService, usuario);

		setTitle("Control Finanzas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 700);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// ===============================
		// Paneles
		// ===============================
		PanelGastos panelGastos = new PanelGastos(gastoService, cuentaService, movimientoService, panelResumen,
				panelResumenGastos, usuario);
		panelAlertas = new PanelAlertas();
		panelVencimiento = new PanelVencimiento();
		panelVencimientosGraficos = new PanelVencimientosGraficos(List.of());

		PanelInversionesAvanzado panelInversiones = new PanelInversionesAvanzado(inversionController, panelVencimiento,
				usuario);

		// NUEVOS: cuentas y movimientos
		panelCuentas = new PanelCuentas(usuario, cuentaService, movimientoService);
		panelMovimientos = new PanelMovimientos(null, movimientoService);

		// Sincronizamos selección de cuenta
		panelCuentas.setCuentaSeleccionadaListener(cuenta -> panelMovimientos.actualizarCuenta(cuenta));

		// Callback para actualizar PanelCuentas al agregar movimiento
		panelMovimientos.setActualizarPanelCuentasCallback(() -> panelCuentas.cargarCuentas());

		panelCartera = new PanelCartera();
		panelResumenKPIs = new PanelResumen();

		inversionController.addListener(this::onInversionesActualizadas);

		// ===============================
		// Tabs
		// ===============================
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Resumen", panelResumen);
		tabs.addTab("Resumen Gastos", panelResumenGastos);
		tabs.addTab("Gastos", panelGastos);
		tabs.addTab("Inversiones", panelInversiones);
		tabs.addTab("Cartera", panelCartera);
		tabs.addTab("Cuentas", panelCuentas);
		tabs.addTab("Movimientos", panelMovimientos);
		tabs.addTab("KPIs", panelResumenKPIs);
		tabs.addTab("Vencimientos", panelVencimientosGraficos);
		tabs.addTab("Alertas", panelAlertas);

		add(tabs, BorderLayout.CENTER);

		refrescarEstadoFinanciero();
	}

	// ==================================================
	// MÉTODO CENTRAL
	// ==================================================
	public void refrescarEstadoFinanciero() {
		cargarInversiones();
		cargarPosiciones();
		cargarCuentas(); // carga cuentas y actualiza saldo
		actualizarAlertas();
		actualizarVencimientos();
		actualizarResumen();
		actualizarKPIs();
	}

	private void cargarInversiones() {
		try {
			inversiones = inversionService.obtenerTodas();
		} catch (Exception e) {
			e.printStackTrace();
			inversiones = List.of();
		}
	}

	private void cargarPosiciones() {
		PosicionService posicionService = new PosicionService(new InversionRepositoryJPA());
		posiciones = posicionService.obtenerPosiciones(usuario.getUsuarioID());
		panelCartera.refrescar(posiciones);
	}

	private void cargarCuentas() {
		cuentas = cuentaService.getCuentasUsuario(usuario);
		for (Cuenta c : cuentas) {
			c.getSaldo();
		}
		panelCuentas.cargarCuentas();
	}

	private void actualizarResumen() {
		panelResumen.actualizarResumen();
	}

	private void actualizarKPIs() {
		if (posiciones == null || cuentas == null) {
			return;
		}

		ResumenService resumenService = new ResumenService();
		BigDecimal patrimonio = resumenService.calcularPatrimonio(cuentas, posiciones);
		BigDecimal invertido = resumenService.calcularTotalInvertido(posiciones);
		BigDecimal pnl = resumenService.calcularPnLTotal(posiciones);
		BigDecimal ingreso = resumenService.calcularIngresoMensual(cuentas, movimientoService);

		panelResumenKPIs.refrescar(patrimonio, invertido, pnl, ingreso);
	}

	private void actualizarAlertas() {
		panelAlertas.actualizarAlertas(alertaService.generarAlertasInversiones(inversiones));
	}

	private void actualizarVencimientos() {
		panelVencimiento.refrescar(inversiones);
		panelVencimientosGraficos.actualizarInversiones(inversiones);
	}

	public List<Inversion> getInversiones() {
		return inversiones;
	}

	public void onInversionesActualizadas() {
		refrescarEstadoFinanciero();
	}
}