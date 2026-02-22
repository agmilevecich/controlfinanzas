package ar.com.controlfinanzas.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Posicion;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.repository.InversionRepository;

public class PosicionService {

	private final InversionRepository repository;

	public PosicionService(InversionRepository repository) {
		this.repository = repository;
	}

	public List<Posicion> calcular(Integer usuarioId) {

		List<Inversion> inversiones = repository.listarPorUsuario(usuarioId);

		Map<String, Posicion> mapa = new HashMap<>();

		for (Inversion inv : inversiones) {

			String clave = generarClave(inv);

			mapa.computeIfAbsent(clave, k -> new Posicion(k, inv.getTipoActivo())).agregar(inv);
		}

		return new ArrayList<>(mapa.values());
	}

	private String generarClave(Inversion inv) {

		if (inv.getTipoActivo() == TipoActivo.CRIPTO) {

			if (inv.getCryptoTipo() != null && !inv.getCryptoTipo().isBlank()) {
				return inv.getCryptoTipo().toUpperCase();
			}

			return "CRIPTO";
		}

		if (inv.getDescripcion() != null && !inv.getDescripcion().isBlank()) {
			return inv.getDescripcion().toUpperCase();
		}

		return "SIN_DESCRIPCION";
	}
}