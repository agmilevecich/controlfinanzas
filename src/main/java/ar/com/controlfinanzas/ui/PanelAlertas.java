package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ar.com.controlfinanzas.model.Alerta;

public class PanelAlertas extends JPanel {

	private JTextPane textPane;

	public PanelAlertas() {
		setLayout(new BorderLayout());

		textPane = new JTextPane();
		textPane.setEditable(false);

		add(new JScrollPane(textPane), BorderLayout.CENTER);
	}

	public void actualizarAlertas(List<Alerta> alertas) {
		textPane.setText("");

		if (alertas == null || alertas.isEmpty()) {
			append("No hay alertas activas.\n", Color.GRAY);
			return;
		}

		alertas.stream().sorted((a1, a2) -> prioridad(a1.getNivel()) - prioridad(a2.getNivel()))
				.forEach(this::appendAlerta);
	}

	private void appendAlerta(Alerta alerta) {
		Color color;
		String prefijo;

		switch (alerta.getNivel()) {
		case HOY:
			color = Color.RED;
			prefijo = "[HOY] ";
			break;
		case CRITICA:
			color = Color.ORANGE;
			prefijo = "[CRÍTICA] ";
			break;
		case PROXIMA:
			color = Color.BLUE;
			prefijo = "[PRÓXIMA] ";
			break;
		case INFO:
		default:
			color = Color.GRAY;
			prefijo = "[INFO] ";
		}

		append(prefijo + alerta.getMensaje() + "\n", color);
	}

	private void append(String texto, Color color) {
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setForeground(attrs, color);
		StyleConstants.setBold(attrs, true);

		try {
			textPane.getDocument().insertString(textPane.getDocument().getLength(), texto, attrs);
		} catch (Exception e) {
			// no-op
		}
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
