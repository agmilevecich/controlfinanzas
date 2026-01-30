package ar.com.controlfinanzas.ui;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    public DashboardFrame() {
        setTitle("Control de Finanzas Personales");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Patrimonio", new JPanel());   // gráfico línea
        tabs.addTab("Distribución", new JPanel()); // gráfico torta
        tabs.addTab("Vencimientos", new JPanel()); // gráfico barras

        add(tabs, BorderLayout.CENTER);
    }
}
