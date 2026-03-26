package ar.com.controlfinanzas.util;

import java.math.BigDecimal;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class SwingUtils {

	public static void configurarCampoNumerico(JTextField textField) {
		((javax.swing.text.AbstractDocument) textField.getDocument())
				.setDocumentFilter(new javax.swing.text.DocumentFilter() {

					@Override
					public void insertString(FilterBypass fb, int offset, String string,
							javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {

						if (esValido(string)) {
							super.insertString(fb, offset, string, attr);
						}
					}

					@Override
					public void replace(FilterBypass fb, int offset, int length, String text,
							javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {

						if (esValido(text)) {
							super.replace(fb, offset, length, text, attrs);
						}
					}

					private boolean esValido(String text) {
						String permitido = "0123456789.,";
						for (char c : text.toCharArray()) {
							if (permitido.indexOf(c) == -1) {
								return false;
							}
						}
						return true;
					}
				});
	}

	public static void configurarCampoDecimal(JTextField textField, int maxDecimales) {
		((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {

			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				replace(fb, offset, 0, string, attr);
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {

				Document doc = fb.getDocument();
				String actual = doc.getText(0, doc.getLength());

				String nuevoTexto = actual.substring(0, offset) + text + actual.substring(offset + length);

				if (esDecimalValido(nuevoTexto, maxDecimales)) {
					super.replace(fb, offset, length, text, attrs);
				}
			}

			private boolean esDecimalValido(String text, int maxDecimales) {
				if (text.isEmpty()) {
					return true;
				}

				// Solo números, coma o punto
				if (!text.matches("[0-9.,]*")) {
					return false;
				}

				// Solo un separador decimal
				int separadores = text.length() - text.replace(",", "").length() + text.length()
						- text.replace(".", "").length();

				if (separadores > 1) {
					return false;
				}

				// Si hay decimal, validar cantidad de decimales
				String[] partes = text.split("[.,]");

				if (partes.length == 2) {
					return partes[1].length() <= maxDecimales;
				}

				return true;
			}
		});
	}

	public static void configurarCampoMoneda(JTextField textField) {

		// 1. filtro (el que ya tenés)
		configurarCampoDecimal(textField, 2);

		// 2. formateo en vivo
		final boolean[] formateando = { false };

		textField.getDocument().addDocumentListener(new DocumentListener() {

			private void actualizar() {

				if (formateando[0]) {
					return;
				}

				// 👇 NO formatear mientras el usuario escribe
				if (textField.hasFocus()) {
					return;
				}

				SwingUtilities.invokeLater(() -> {
					try {
						formateando[0] = true;

						String texto = textField.getText();
						if (texto == null || texto.isEmpty()) {
							return;
						}

						String limpio = texto.replace(".", "").replace(",", ".");

						BigDecimal valor = new BigDecimal(limpio);

						String formateado = NumeroUtils.formatearMonedaARS(valor);

						textField.setText(formateado);

					} catch (Exception e) {
						// ignorar
					} finally {
						formateando[0] = false;
					}
				});
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				actualizar();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				actualizar();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				actualizar();
			}
		});
	}
}
