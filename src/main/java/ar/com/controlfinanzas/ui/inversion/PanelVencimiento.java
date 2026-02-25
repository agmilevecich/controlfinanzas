package ar.com.controlfinanzas.ui.inversion;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public class PanelVencimiento extends JPanel {

	private DefaultListModel<String> modelo;
	private JList<String> lista;

	public PanelVencimiento() {
		setLayout(new BorderLayout());

		modelo = new DefaultListModel<>();
		lista = new JList<>(modelo);

		add(new JScrollPane(lista), BorderLayout.CENTER);
	}

	public void refrescar(List<Inversion> inversiones) {

		modelo.clear();

		inversiones.stream().filter(i -> i.getFechaVencimiento() != null)
				.filter(i -> !i.getFechaVencimiento().isBefore(LocalDate.now()))
				.sorted(Comparator.comparing(Inversion::getFechaVencimiento)).forEach(i -> {

					BigDecimal capital = i.getCapitalTotalCalculado();
					BigDecimal interes = i.calcularInteresAlVencimiento();
					BigDecimal total = i.calcularCapitalFinalEstimado();

					String texto = String.format("%s — vence %s — Capital: %.2f — Interés: %.2f — Total: %.2f",
							i.getNombre(), i.getFechaVencimiento(), capital, interes, total);

					modelo.addElement(texto);
				});
	}
}