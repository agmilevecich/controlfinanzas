package ar.com.controlfinanzas.ui.components;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class PanelMargen extends JPanel {

    private BigDecimal porcentaje = BigDecimal.ZERO;

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (porcentaje == null) return;

        int width = getWidth();
        int height = 20;

        int barra = (int) (width * porcentaje.doubleValue() / 100.0);

        // 🎨 color dinámico
        if (porcentaje.compareTo(BigDecimal.valueOf(20)) < 0) {
            g.setColor(Color.RED);
        } else if (porcentaje.compareTo(BigDecimal.valueOf(50)) < 0) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.GREEN);
        }

        g.fillRect(0, 0, barra, height);

        g.setColor(Color.GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
    }
}