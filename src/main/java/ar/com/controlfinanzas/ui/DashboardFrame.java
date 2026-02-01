package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class DashboardFrame extends JFrame {

	private JTabbedPane tabs = new JTabbedPane();

	public DashboardFrame() {
		setTitle("Control de Finanzas Personales");
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tabs.addTab("Patrimonio", new JPanel()); // gráfico línea
		tabs.addTab("Distribución", new JPanel()); // gráfico torta
		tabs.addTab("Vencimientos", new JPanel()); // gráfico barras
		tabs.addTab("Inversion", new JPanel());

		add(tabs, BorderLayout.CENTER);
	}

	public JTabbedPane getTabbedPane() {
		return tabs;
	}

}
