package ar.com.controlfinanzas.controller;

import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.service.InversionService;

public class InversionController {

	private InversionService inversionService;
	private List<Inversion> inversiones;

	private List<Runnable> listeners = new ArrayList<>();

	public InversionController(InversionService inversionService) {
		this.inversionService = inversionService;
		this.inversiones = new ArrayList<>();
		cargarInversiones();
	}

	public void cargarInversiones() {
		inversiones = inversionService.obtenerTodas();
		notificarCambios();
	}

	public void agregarInversion(Inversion inversion) {
		inversionService.crearInversion(inversion);
		cargarInversiones();
	}

	public void eliminarInversion(Long id) {
		inversionService.eliminarInversion(id);
		cargarInversiones();
	}

	public List<Inversion> getInversiones() {
		return inversiones;
	}

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}

	private void notificarCambios() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}
}
