package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ar.com.controlfinanzas.model.MovimientoTarjeta;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.repository.MovimientoTarjetaRepository;
import jakarta.persistence.EntityManager;

public class MovimientoTarjetaService {

	private MovimientoTarjetaRepository repository;

	public MovimientoTarjetaService(EntityManager em) {
		this.repository = new MovimientoTarjetaRepository(em);
	}

	public void registrarConsumo(TarjetaCredito tarjeta, LocalDate fecha, String descripcion, BigDecimal monto) {

		MovimientoTarjeta movimiento = new MovimientoTarjeta(fecha, descripcion, monto, tarjeta);

		repository.guardar(movimiento);
	}

	public List<MovimientoTarjeta> obtenerMovimientos(TarjetaCredito tarjeta) {
		return repository.findByTarjetaId(tarjeta.getId());
	}
}