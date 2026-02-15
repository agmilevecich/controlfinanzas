package ar.com.controlfinanzas.app;

import javax.swing.SwingUtilities;

import ar.com.controlfinanzas.ui.DashboardFrame;

public class MainApp {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {

			try {
				new DashboardFrame().setVisible(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
}
