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

		alertas.stream().sorted((a1, a2) -> prioridad(a1.getNivel()) - prioridad(a2.getNivel())).forEach(alerta -> {
			textArea.append("• " + alerta.getTitulo() + ": " + alerta.getMensaje() + "\n");
		});
	}

	private int prioridad(Alerta.Nivel nivel) {
		if (nivel == null) {
			return 99;
		}

		switch (nivel) {
		case HOY:
			return 1;
		case CRITICA:
			return 2;
		case PROXIMA:
			return 3;
		case INFO:
		default:
			return 4;
		}
	}

}
