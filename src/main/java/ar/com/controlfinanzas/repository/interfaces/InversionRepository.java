package ar.com.controlfinanzas.repository.interfaces;

import java.util.List;

import ar.com.controlfinanzas.domain.inversion.Inversion;

public interface InversionRepository {

	Inversion guardar(Inversion inversion);

	List<Inversion> listarPorUsuario(Integer usuarioId);

	void eliminar(Long id);
}
