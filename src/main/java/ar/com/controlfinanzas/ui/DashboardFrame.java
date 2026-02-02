package ar.com.controlfinanzas.ui;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ar.com.controlfinanzas.dao.InversionDAO;
import ar.com.controlfinanzas.model.Inversion;

public class DashboardFrame extends JFrame {

	private List<Inversion> inversiones;

	private PanelResumenFinanciero panelResumen;
	private PanelVencimientos panelVencimientos;
	private PanelInversionesAvanzado panelInversiones;
	private PanelGastos panelGastos;

	private InversionDAO inversionDAO;

	public DashboardFrame() throws Exception {
		setTitle("Control de Finanzas");
		setSize(1200, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		inversionDAO = new InversionDAO();
		inversiones = inversionDAO.listarInversiones();

		inicializarComponentes();
	}

	private void inicializarComponentes() {

		panelResumen = new PanelResumenFinanciero();
		panelVencimientos = new PanelVencimientos(inversiones);

		panelInversiones = new PanelInversionesAvanzado(panelVencimientos, panelResumen);

		panelGastos = new PanelGastos(panelResumen);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Resumen", panelResumen);
		tabs.addTab("Gastos", panelGastos);
		tabs.addTab("Inversiones", panelInversiones);
		tabs.addTab("Vencimientos", panelVencimientos);

		add(tabs);
	}
}
