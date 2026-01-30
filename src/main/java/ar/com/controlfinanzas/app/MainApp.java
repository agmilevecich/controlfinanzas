package ar.com.controlfinanzas.app;

import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.ui.DashboardFrame;

public class MainApp {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new DashboardFrame().setVisible(true);
		});
	}
}
