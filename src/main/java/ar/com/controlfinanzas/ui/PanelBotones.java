package ar.com.controlfinanzas.ui;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class PanelBotones extends JPanel {

	private JButton[] botones;

	public PanelBotones() {
		String[] texto = new String[] { "Agregar", "Modificar", "Eliminar", "Cancelar" };
		botones = new JButton[texto.length];

		setLayout(new GridLayout(1, texto.length, 5, 10));

		for (int i = 0; i < botones.length; i++) {
			botones[i] = new JButton(texto[i]);
			add(botones[i]);
		}
	}

	public JButton[] getBotones() {
		return botones;
	}

}
