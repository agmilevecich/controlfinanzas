package ar.com.controlfinanzas.util;

import javax.swing.JTextField;
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

	public static void configurarCampoDecimal(JTextField textField) {
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

				if (esDecimalValido(nuevoTexto)) {
					super.replace(fb, offset, length, text, attrs);
				}
			}

			private boolean esDecimalValido(String text) {
				if (text.isEmpty()) {
					return true;
				}

				// Permitir solo números, coma o punto
				if (!text.matches("[0-9.,]*")) {
					return false;
				}

				// Solo un separador decimal
				int countComa = text.length() - text.replace(",", "").length();
				int countPunto = text.length() - text.replace(".", "").length();

				return (countComa + countPunto) <= 1;
			}
		});
	}
}
