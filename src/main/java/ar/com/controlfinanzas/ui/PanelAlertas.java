package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ar.com.controlfinanzas.model.Alerta;

public class PanelAlertas extends JPanel {

	private JTextPane textPane;
	private JLabel resumenLabel;

	public PanelAlertas() {
		setLayout(new BorderLayout());

		resumenLabel = new JLabel(" ");
		add(resumenLabel, BorderLayout.NORTH);

		textPane = new JTextPane();
		textPane.setEditable(false);

		add(new JScrollPane(textPane), BorderLayout.CENTER);
	}

	public void actualizarAlertas(List<Alerta> alertas) {
		textPane.setText("");

		if (alertas == null || alertas.isEmpty()) {
			resumenLabel.setText("Sin alertas activas");
			append("No hay alertas activas.\n", Color.GRAY);
			return;
		}

		actualizarResumen(alertas);

		alertas.stream().sorted((a1, a2) -> prioridad(a1.getNivel()) - prioridad(a2.getNivel()))
				.forEach(this::appendAlerta);
	}

	private void actualizarResumen(List<Alerta> alertas) {
		Map<Alerta.Nivel, Integer> conteo = new EnumMap<>(Alerta.Nivel.class);

		for (Alerta alerta : alertas) {
			Alerta.Nivel nivel = alerta.getNivel();
			conteo.put(nivel, conteo.getOrDefault(nivel, 0) + 1);
		}

		String texto = String.format("HOY: %d  ·  CRÍTICAS: %d  ·  PRÓXIMAS: %d  ·  INFO: %d",
				conteo.getOrDefault(Alerta.Nivel.HOY, 0), conteo.getOrDefault(Alerta.Nivel.CRITICA, 0),
				conteo.getOrDefault(Alerta.Nivel.PROXIMA, 0), conteo.getOrDefault(Alerta.Nivel.INFO, 0));

		resumenLabel.setText(texto);
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
