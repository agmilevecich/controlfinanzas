package ar.com.controlfinanzas.ui.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import ar.com.controlfinanzas.model.Cuenta;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class ComboRendererGenerico<T> extends DefaultListCellRenderer {

    private String mensaje;
    private Function<T, String> formatter;

    public ComboRendererGenerico(String mensaje, Function<T, String> formatter) {
        this.mensaje = mensaje;
        this.formatter = formatter;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        if (value == null) {
            label.setText(mensaje);
        } else {
            @SuppressWarnings("unchecked")
            T item = (T) value;
            label.setText(formatter.apply(item));
        }

        return label;
    }
}