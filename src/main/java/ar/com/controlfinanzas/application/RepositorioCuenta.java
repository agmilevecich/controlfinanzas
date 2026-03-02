package ar.com.controlfinanzas.application;

import ar.com.controlfinanzas.domain.finanzas.Cuenta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RepositorioCuenta {

    private List<Cuenta> cuentas = new ArrayList<>();

    public void guardar(Cuenta cuenta) {
        cuentas.add(cuenta);
    }

    public List<Cuenta> listarTodas() {
        return new ArrayList<>(cuentas);
    }

    public Optional<Cuenta> buscarPorId(UUID id) {
        return cuentas.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }
}