package ar.com.controlfinanzas.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Posicion;
import ar.com.controlfinanzas.model.TipoActivo;
import ar.com.controlfinanzas.repository.InversionRepositoryJPA;
import ar.com.controlfinanzas.repository.interfaces.InversionRepository;

public class PosicionService {

	private final InversionRepository inversionRepository;

	public PosicionService() {
		inversionRepository = new InversionRepositoryJPA();
	}

	public List<Posicion> calcular(Integer usuarioId) {
		List<Inversion> inversiones = inversionRepository.listarPorUsuario(usuarioId);
		Map<String, Posicion> mapa = new LinkedHashMap<>();

		for (Inversion inv : inversiones) {
			String clave = inv.getDescripcion();
			TipoActivo tipo = inv.getTipoActivo();
			Posicion pos = mapa.computeIfAbsent(clave, k -> new Posicion(clave, tipo));
			pos.agregar(inv); // usa siempre getValuador() → precios, montos o indexados
		}

		return new ArrayList<>(mapa.values());
	}
}