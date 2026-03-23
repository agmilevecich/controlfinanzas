package ar.com.controlfinanzas.alerts.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.model.Alerta;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.service.TarjetaCreditoService;
import ar.com.controlfinanzas.ui.DashboardFrame;

public class AlertaTarjetas implements GeneradorAlertas {

    private final List<TarjetaCredito> tarjetas;
    private final TarjetaCreditoService service;
    private final DashboardFrame dashboard;

    public AlertaTarjetas(List<TarjetaCredito> tarjetas,
                           TarjetaCreditoService service,
                           DashboardFrame dashboard) {
        this.tarjetas = tarjetas;
        this.service = service;
        this.dashboard = dashboard;
    }

    @Override
    public List<Alerta> generar() {

        List<Alerta> alertas = new ArrayList<>();

        if (tarjetas == null || tarjetas.isEmpty()) return alertas;

        LocalDate hoy = LocalDate.now();

        for (TarjetaCredito t : tarjetas) {

            BigDecimal deuda = service.calcularDeudaTotal(t);

            if (deuda == null || deuda.compareTo(BigDecimal.ZERO) <= 0) continue;

            LocalDate vencimiento = service.calcularProximoVencimiento(t);
            long dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, vencimiento);

            BigDecimal minimo = service.calcularPagoMinimo(t);

            Alerta alerta = null;

            if (dias == 0) {
                alerta = new Alerta("Tarjeta vence hoy",
                        t.getNombre() + " vence hoy | Deuda: $" + deuda,
                        hoy,
                        Alerta.TipoAlerta.VENCIMIENTO,
                        Alerta.Nivel.HOY);
            } else if (dias <= 3) {
                alerta = new Alerta("Tarjeta por vencer",
                        t.getNombre() + " vence en " + dias + " días | Deuda: $" + deuda,
                        hoy,
                        Alerta.TipoAlerta.VENCIMIENTO,
                        Alerta.Nivel.CRITICA);
            } else if (dias <= 7) {
                alerta = new Alerta("Tarjeta próxima",
                        t.getNombre() + " vence en " + dias + " días",
                        hoy,
                        Alerta.TipoAlerta.VENCIMIENTO,
                        Alerta.Nivel.PROXIMA);
            }

            if (alerta != null) {
                alerta.setAccion(() -> dashboard.irATarjetas());
                alertas.add(alerta);
            }
        }

        return alertas;
    }
}