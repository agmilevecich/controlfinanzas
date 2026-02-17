package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.repository.InversionRepository;

public class InversionService {

	private final InversionRepository repository;

	public InversionService(InversionRepository inversionRepositoty) {
		this.repository = inversionRepositoty;
	}

	public Inversion crearInversion(Inversion inversion) {
		// Acá iría lógica de negocio futura
		return repository.guardar(inversion);
	}

	public List<Inversion> obtenerTodas() {
		return repository.listarPorUsuario(MainApp.getUsuarioActivo().getUsuarioID());
	}

	public void eliminarInversion(Long id) {
		repository.eliminar(id);
	}

	public BigDecimal calcularCapitalTotal() {
		List<Inversion> inversiones = obtenerTodas();

		return inversiones.stream().map(inv -> {
			// Si tiene cantidad y precio, calculamos por ahí
			if (inv.getCantidad() != null && inv.getPrecioUnitario() != null
					&& inv.getCantidad().compareTo(BigDecimal.ZERO) > 0
					&& inv.getPrecioUnitario().compareTo(BigDecimal.ZERO) > 0) {
				return inv.getCantidad().multiply(inv.getPrecioUnitario());
			}
			return inv.getCapitalInicial() != null ? inv.getCapitalInicial() : BigDecimal.ZERO;
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
