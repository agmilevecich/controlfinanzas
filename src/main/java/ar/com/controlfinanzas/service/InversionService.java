package ar.com.controlfinanzas.service;

import java.util.List;

import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.repository.InversionRepository;

public class InversionService {

	private final InversionRepository repository;

	public InversionService() {
		this.repository = new InversionRepository();
	}

	public Inversion crearInversion(Inversion inversion) {
		// Acá iría lógica de negocio futura
		return repository.guardar(inversion);
	}

	public List<Inversion> obtenerTodas() {
		return repository.listar();
	}
}
