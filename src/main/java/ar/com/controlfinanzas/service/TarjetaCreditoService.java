package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.repository.TarjetaCreditoRepository;
import jakarta.persistence.EntityManager;

public class TarjetaCreditoService {

	private final TarjetaCreditoRepository repo;
	private final EntityManager em;

	public TarjetaCreditoService(EntityManager em) {
		this.em = em;
		this.repo = new TarjetaCreditoRepository();
	}

	public TarjetaCredito guardar(TarjetaCredito tarjeta) {
		em.getTransaction().begin();
		repo.guardar(em, tarjeta);
		em.getTransaction().commit();
		return tarjeta;
	}

	public List<TarjetaCredito> getTarjetasUsuario(Integer usuarioId) {
		return repo.buscarPorUsuario(em, usuarioId);
	}

	public TarjetaCredito buscarPorId(Long id) {
		return repo.buscarPorId(em, id);
	}

	public LocalDate obtenerInicioCiclo(TarjetaCredito tarjeta) {

		int diaCierre = tarjeta.getDiaCierre();
		LocalDate hoy = LocalDate.now();

		LocalDate cierreEsteMes = LocalDate.of(hoy.getYear(), hoy.getMonth(), diaCierre);

		if (hoy.isAfter(cierreEsteMes)) {
			return cierreEsteMes.plusDays(1);
		} else {
			LocalDate cierreMesAnterior = cierreEsteMes.minusMonths(1);
			return cierreMesAnterior.plusDays(1);
		}
	}

	public LocalDate obtenerCierreActual(TarjetaCredito tarjeta) {

		int diaCierre = tarjeta.getDiaCierre();
		LocalDate hoy = LocalDate.now();

		LocalDate cierreEsteMes = LocalDate.of(hoy.getYear(), hoy.getMonth(), diaCierre);

		if (hoy.isAfter(cierreEsteMes)) {
			return cierreEsteMes.plusMonths(1);
		}

		return cierreEsteMes;
	}

	public List<Movimiento> getMovimientosCiclo(TarjetaCredito tarjeta) {

		LocalDate inicio = obtenerInicioCiclo(tarjeta);
		LocalDate cierre = obtenerCierreActual(tarjeta);

		return em.createQuery(
				"SELECT m FROM Movimiento m WHERE m.tarjeta = :tarjeta AND m.fecha BETWEEN :inicio AND :cierre ORDER BY m.fecha",
				Movimiento.class).setParameter("tarjeta", tarjeta).setParameter("inicio", inicio)
				.setParameter("cierre", cierre).getResultList();
	}

	public List<Movimiento> getMovimientosTarjeta(TarjetaCredito tarjeta) {

		return em.createQuery("SELECT m FROM Movimiento m WHERE m.tarjeta = :tarjeta ORDER BY m.fecha DESC",
				Movimiento.class).setParameter("tarjeta", tarjeta).getResultList();
	}

	public void pagarTarjeta(TarjetaCredito tarjeta) {

		em.getTransaction().begin();

		List<Movimiento> movimientos = getMovimientosTarjeta(tarjeta);

		for (Movimiento m : movimientos) {
			if (m.isPendiente()) {
				m.setPendiente(false);
				em.merge(m);
			}
		}

		em.getTransaction().commit();
	}

	public void registrarCompraCuotas(TarjetaCredito tarjeta, BigDecimal montoTotal, int cuotas, String descripcion) {

		em.getTransaction().begin();

		BigDecimal montoCuota = montoTotal.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);

		String compraId = UUID.randomUUID().toString();

		for (int i = 1; i <= cuotas; i++) {

			Movimiento m = new Movimiento();

			m.setTarjeta(tarjeta);
			m.setDescripcion(descripcion + " (" + i + "/" + cuotas + ")");
			m.setMonto(montoCuota);
			m.setFecha(LocalDate.now().plusMonths(i - 1));

			m.setNumeroCuota(i);
			m.setTotalCuotas(cuotas);
			m.setCompraId(compraId);

			m.setPendiente(true);

			em.persist(m);
		}

		em.getTransaction().commit();
	}
}