package ar.com.controlfinanzas.alerts;

import ar.com.controlfinanzas.model.EstadoInversion;
import ar.com.controlfinanzas.model.Inversion;

public class ResultadoAlertaInversion {

    private Inversion inversion;
    private EstadoInversion estado;
    private long diasRestantes;

    public ResultadoAlertaInversion(Inversion inversion,
                                    EstadoInversion estado,
                                    long diasRestantes) {
        this.inversion = inversion;
        this.estado = estado;
        this.diasRestantes = diasRestantes;
    }

    public Inversion getInversion() {
        return inversion;
    }

    public EstadoInversion getEstado() {
        return estado;
    }

    public long getDiasRestantes() {
        return diasRestantes;
    }
}
