package ar.com.controlfinanzas.ui.dashboard;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.service.BancoService;
import ar.com.controlfinanzas.service.CuentaService;
import ar.com.controlfinanzas.service.MovimientoService;

public class PanelCuentasMovimientos extends JPanel {

	private PanelCuentasNuevo panelCuentas;
	private PanelMovimientos panelMovimientos;
	private Consumer<Cuenta> onRegistroGastos;

	public PanelCuentasMovimientos(CuentaService cuentaService, MovimientoService movimientoService,
			BancoService bancoService) {

		setLayout(new BorderLayout());

		// ✔ Constructor correcto
		panelCuentas = new PanelCuentasNuevo(cuentaService, movimientoService, bancoService);

		panelMovimientos = new PanelMovimientos(null, cuentaService, movimientoService);

		// 🔥 SINCRONIZACIÓN CLAVE
		panelCuentas.setCuentaSeleccionadaListener(cuenta -> {
			panelMovimientos.actualizarCuenta(cuenta);
		});

		panelCuentas.setOnRegistroGastos(cuenta -> {

			if (onRegistroGastos != null) {
				onRegistroGastos.accept(cuenta);
			}

		});

		// 🔄 REFRESCO CRUZADO
		panelCuentas.setActualizarCuentas(() -> {
			panelMovimientos.cargarMovimientos();
		});

		panelMovimientos.setActualizarPanelCuentasCallback(() -> {
			panelCuentas.cargarCuentas();
		});

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCuentas, panelMovimientos);

		split.setDividerLocation(300);

		add(split, BorderLayout.CENTER);
	}

	// ✔ Método correcto (sin recrear paneles)
	public void refrescar() {
		panelCuentas.cargarCuentas();
		panelMovimientos.cargarMovimientos();
	}

	public void setIrAGastos(Runnable irAGastos) {
		if (panelCuentas != null) {
			panelCuentas.setIrAGastos(irAGastos);
		}
	}

	public void setOnRegistroGastos(Consumer<Cuenta> listener) {
		this.onRegistroGastos = listener;
	}
}