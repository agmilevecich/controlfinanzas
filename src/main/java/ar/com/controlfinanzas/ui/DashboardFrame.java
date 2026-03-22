package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ar.com.controlfinanzas.alerts.AlertaManager;
import ar.com.controlfinanzas.controller.InversionController;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Posicion;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.repository.InversionRepositoryJPA;
import ar.com.controlfinanzas.repository.interfaces.InversionRepository;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.IngresoService;
import ar.com.controlfinanzas.service.InversionService;
import ar.com.controlfinanzas.service.MovimientoService;
import ar.com.controlfinanzas.service.PosicionService;
import ar.com.controlfinanzas.service.ResumenService;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.ui.dashboard.PanelBancos;
import ar.com.controlfinanzas.ui.dashboard.PanelCuentas;
import ar.com.controlfinanzas.ui.dashboard.PanelMovimientos;
import ar.com.controlfinanzas.ui.dashboard.PanelResumen;
import ar.com.controlfinanzas.ui.dashboard.PanelResumenTarjeta;
import ar.com.controlfinanzas.ui.dashboard.PanelTarjetaCredito;
import ar.com.controlfinanzas.ui.inversion.PanelCartera;
import ar.com.controlfinanzas.ui.inversion.PanelVencimiento;
import jakarta.persistence.EntityManager;

public class DashboardFrame extends JFrame {

	private List<Inversion> inversiones;
	private List<Posicion> posiciones;
	private List<Cuenta> cuentas;

	private PanelCartera panelCartera;
	private PanelResumen panelResumenKPIs;

	private PanelAlertas panelAlertas;
	private PanelResumenFinanciero panelResumen;
	private PanelVencimientosGraficos panelVencimientosGraficos;

	private final AlertaManager alertaManager;
	private final InversionRepository inversionRepository;
	private final InversionService inversionService;
	private final InversionController inversionController;

	private final PanelResumenGastos panelResumenGastos;

	private final IngresoService ingresoService;

	private PanelVencimiento panelVencimiento;

	private CuentaService cuentaService;
	private MovimientoService movimientoService;

	private PanelCuentas panelCuentas;
	private PanelMovimientos panelMovimientos;
	private TarjetaCreditoService tarjetaCreditoService;
	private List<TarjetaCredito> tarjetas;
	private PanelTarjetaCredito panelTarjetaCredito;
	private EntityManager em;
	private BancoService bancoService;
	private Usuario usuario;

	public DashboardFrame(CuentaService cuentaService, MovimientoService movimientoService, EntityManager em) {
		this.em = em;
		this.cuentaService = cuentaService;
		this.movimientoService = movimientoService;

		this.alertaManager = new AlertaManager();
		this.inversionRepository = new InversionRepositoryJPA(em);
		this.inversionService = new InversionService(inversionRepository);
		this.inversionController = new InversionController(inversionService);
		this.panelResumenGastos = new PanelResumenGastos(movimientoService);
		this.panelTarjetaCredito = new PanelTarjetaCredito(em);
		this.bancoService = new BancoService(em);
		this.ingresoService = new IngresoService(em);
		this.tarjetaCreditoService = new TarjetaCreditoService(em);
		this.usuario = SesionUsuario.getUsuarioActual();
		this.tarjetas = tarjetaCreditoService.getTarjetasUsuario(usuario.getUsuarioID());

		panelResumen = new PanelResumenFinanciero(inversionService, movimientoService, ingresoService);

		setTitle("Control Finanzas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 700);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// ===============================
		// Paneles
		// ===============================
		PanelBancos panelBancos = new PanelBancos(bancoService);
		PanelResumenTarjeta panelResumenTarjeta = new PanelResumenTarjeta(cuentaService, em);

		PanelGastos panelGastos = new PanelGastos(cuentaService, movimientoService, tarjetaCreditoService,
				panelResumenTarjeta);

		panelGastos.setActualizaGastos(() -> {
			panelMovimientos.cargarMovimientos();
			panelCuentas.cargarCuentas();
		});

		panelTarjetaCredito.setActualizarTarjeta(() -> {
			panelGastos.actualizarTarjetaCredito();
			panelResumenTarjeta.refrescar();
		});

		panelAlertas = new PanelAlertas();
		panelVencimiento = new PanelVencimiento();
		panelVencimientosGraficos = new PanelVencimientosGraficos(List.of());

		PanelInversionesAvanzado panelInversiones = new PanelInversionesAvanzado(inversionController, panelVencimiento,
				em);

		// NUEVOS: cuentas y movimientos
		panelCuentas = new PanelCuentas(cuentaService, movimientoService, bancoService);
		panelMovimientos = new PanelMovimientos(null, cuentaService, movimientoService);

		// Sincronización entre paneles
		panelCuentas.setCuentaSeleccionadaListener(cuenta -> panelMovimientos.actualizarCuenta(cuenta));

		panelCuentas.setActualizarCuentas(() -> {
			refrescarEstadoFinanciero();
			panelGastos.refrescar();
			panelTarjetaCredito.actualizarBancos();
		});

		panelResumenTarjeta.setActualizar(() -> {
			panelMovimientos.cargarMovimientos();
		});

		panelMovimientos.setActualizarPanelCuentasCallback(() -> {
			panelCuentas.cargarCuentas();
			panelTarjetaCredito.actualizarBancos();
			refrescarEstadoFinanciero();
		});

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
		tabs.addTab("Bancos", panelBancos);
		tabs.addTab("Cuentas", panelCuentas);
		tabs.addTab("Movimientos", panelMovimientos);
		tabs.add("Tarjetas", panelTarjetaCredito);
		tabs.addTab("KPIs", panelResumenKPIs);
		tabs.addTab("Resumen Tarjeta", panelResumenTarjeta);
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
		cargarCuentas();
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
		PosicionService posicionService = new PosicionService(new InversionRepositoryJPA(em));
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

		List<Alerta> alertas = alertaManager.generarTodas(inversiones, cuentas, tarjetas, movimientoService,
				tarjetaCreditoService, usuario);

		panelAlertas.actualizarAlertas(alertas);

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