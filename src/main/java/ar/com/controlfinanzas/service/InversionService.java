package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import ar.com.controlfinanzas.app.MainApp;
import ar.com.controlfinanzas.domain.inversion.Inversion;
import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.repository.interfaces.InversionRepository;

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

	public BigDecimal calcularCapitalTotal(Integer usuarioId) {

		return repository.listarPorUsuario(usuarioId).stream()
				.map(inv -> inv.getCapitalTotalCalculado() != null ? inv.getCapitalTotalCalculado() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularTotalPorMes(Integer usuarioId, YearMonth mes) {

		return repository.listarPorUsuario(usuarioId).stream()
				.filter(inv -> inv.getFechaInicio() != null && YearMonth.from(inv.getFechaInicio()).equals(mes))
				.map(inv -> inv.getCapitalTotalCalculado() != null ? inv.getCapitalTotalCalculado() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularTotalInvertido(Usuario usuario) {

		List<Inversion> inversiones = repository.listarPorUsuario(usuario.getUsuarioID());

		BigDecimal total = BigDecimal.ZERO;

		for (Inversion inv : inversiones) {

			BigDecimal capital = inv.getCapitalTotalCalculado();

			if (capital != null) {
				total = total.add(capital);
			}
		}

		return total;
	}

	public List<Inversion> obtenerConVencimiento() {
		return obtenerTodas().stream().filter(i -> i.getFechaVencimiento() != null).toList();
	}

}
