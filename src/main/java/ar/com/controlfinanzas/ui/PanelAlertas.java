package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ar.com.controlfinanzas.model.Alerta;

public class PanelAlertas extends JPanel {

	private JTextArea textArea;

	public PanelAlertas() {
		setLayout(new BorderLayout());

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		add(new JScrollPane(textArea), BorderLayout.CENTER);

	}

	public void actualizarAlertas(List<Alerta> alertas) {
		textArea.setText("");

		if (alertas == null || alertas.isEmpty()) {
			textArea.append("No hay alertas activas.\n");
			return;
		}

		for (Alerta alerta : alertas) {
			textArea.append("• " + alerta.getTitulo() + ":" + alerta.getMensaje() + "\n");
		}
	}
}
