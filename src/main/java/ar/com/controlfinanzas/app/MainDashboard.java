package ar.com.controlfinanzas.app;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.dao.InversionDAO;
import ar.com.controlfinanzas.db.DatabaseInitializer;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.ui.PanelAlertas;
import ar.com.controlfinanzas.ui.PanelGastos;
import ar.com.controlfinanzas.ui.PanelInversionesAvanzado;
import ar.com.controlfinanzas.ui.PanelResumenFinanciero;
import ar.com.controlfinanzas.ui.PanelVencimientos;

public class MainDashboard {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {

			// Inicializamos base de datos
			DatabaseInitializer.inicializar();

			// Obtenemos lista de inversiones desde BD
			InversionDAO inversionDAO = new InversionDAO();
			List<Inversion> inversiones = null;
			try {
				inversiones = inversionDAO.listarInversiones();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Creamos ventana principal
			JFrame frame = new JFrame("Control Finanzas - Dashboard");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(1000, 600);
			frame.setLocationRelativeTo(null);

			// Paneles
			PanelVencimientos panelVencimientos = new PanelVencimientos(inversiones);
			PanelResumenFinanciero panelResumen = new PanelResumenFinanciero();
			PanelGastos panelGastos = new PanelGastos(panelResumen);
			PanelAlertas panelAlertas = new PanelAlertas();
			PanelInversionesAvanzado panelInversiones = new PanelInversionesAvanzado(panelVencimientos, panelResumen,
					panelAlertas);

			// Pestañas
			JTabbedPane tabs = new JTabbedPane();
			tabs.addTab("Resumen Financiero", panelResumen);
			tabs.addTab("Gastos", panelGastos);
			tabs.addTab("Inversiones", panelInversiones);
			tabs.addTab("Vencimientos", panelVencimientos);

			frame.add(tabs);
			frame.setVisible(true);
		});
	}
}
