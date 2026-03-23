package ar.com.controlfinanzas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
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

	// 🔥 NUEVO: mantener referencia de alertas
	private List<Alerta> alertasActuales = new ArrayList<>();

	public PanelAlertas() {
		setLayout(new BorderLayout());

		resumenLabel = new JLabel(" ");
		add(resumenLabel, BorderLayout.NORTH);

		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setFocusable(false);

		// 🔥 CLICK SOBRE ALERTAS
		textPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				try {
					int pos = textPane.viewToModel2D(e.getPoint());
					int linea = textPane.getDocument().getDefaultRootElement().getElementIndex(pos);

					if (linea >= 0 && linea < alertasActuales.size()) {
						Alerta alerta = alertasActuales.get(linea);

						if (alerta.getAccion() != null) {
							alerta.getAccion().run();
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		textPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseMoved(java.awt.event.MouseEvent e) {

				int pos = textPane.viewToModel2D(e.getPoint());

				var root = textPane.getDocument().getDefaultRootElement();
				int linea = root.getElementIndex(pos);

				if (linea >= 0 && linea < alertasActuales.size()) {

					Alerta alerta = alertasActuales.get(linea);

					if (alerta.getAccion() != null) {
						textPane.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
						return;
					}
				}

				// cursor normal
				textPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		});

		add(new JScrollPane(textPane), BorderLayout.CENTER);
	}

	public void actualizarAlertas(List<Alerta> alertas) {

		textPane.setText("");

		// 🔥 guardar referencia ordenada
		alertasActuales.clear();

		if (alertas == null || alertas.isEmpty()) {
			resumenLabel.setText("Sin alertas activas");
			append("No hay alertas activas.\n", Color.GRAY);
			return;
		}

		actualizarResumen(alertas);

		alertas.stream().sorted(Comparator.comparingInt(a -> prioridad(a.getNivel()))).forEach(alerta -> {
			alertasActuales.add(alerta); // 👈 clave para mapear click
			appendAlerta(alerta);
		});
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
			color = new Color(192, 0, 0);
			prefijo = "🔴 HOY → ";
			break;

		case CRITICA:
			color = new Color(255, 140, 0);
			prefijo = "🟠 CRÍTICA → ";
			break;

		case PROXIMA:
			color = new Color(0, 102, 204);
			prefijo = "🔵 PRÓXIMA → ";
			break;

		case INFO:
		default:
			color = Color.GRAY;
			prefijo = "⚪ INFO → ";
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
			e.printStackTrace();
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