package ar.com.controlfinanzas.util;

import javax.swing.JTextField;

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
}
