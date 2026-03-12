package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
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
		LocalDate cierreEsteMes = calcularCierreDelMes(diaCierre, hoy);

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

		LocalDate cierreEsteMes = calcularCierreDelMes(diaCierre, hoy);

		if (hoy.isAfter(cierreEsteMes)) {
			return cierreEsteMes.plusMonths(1);
		}

		return cierreEsteMes;
	}

	private LocalDate calcularCierreDelMes(int diaCierre, LocalDate fecha) {

		int dia = Math.min(diaCierre, fecha.lengthOfMonth());
		return LocalDate.of(fecha.getYear(), fecha.getMonth(), dia);
	}

	public List<Movimiento> getMovimientosCiclo(TarjetaCredito tarjeta) {

		LocalDate inicio = obtenerInicioCiclo(tarjeta);
		LocalDate cierre = obtenerCierreActual(tarjeta);

		String jpql = """
				    SELECT m
				    FROM Movimiento m
				    WHERE m.tarjeta = :tarjeta
				    AND m.fecha BETWEEN :inicio AND :cierre
				    ORDER BY m.fecha
				""";

		return em.createQuery(jpql, Movimiento.class).setParameter("tarjeta", tarjeta).setParameter("inicio", inicio)
				.setParameter("cierre", cierre).getResultList();
	}

	public List<Movimiento> getMovimientosTarjeta(TarjetaCredito tarjeta) {

		return em.createQuery("SELECT m FROM Movimiento m WHERE m.tarjeta = :tarjeta ORDER BY m.fecha DESC",
				Movimiento.class).setParameter("tarjeta", tarjeta).getResultList();
	}

	public void pagarTarjeta(TarjetaCredito tarjeta, Cuenta cuenta, BigDecimal montoPago) {

		if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}

		em.getTransaction().begin();

		List<Movimiento> movimientos = getMovimientosPendientes(tarjeta);

		BigDecimal restante = montoPago;

		for (Movimiento m : movimientos) {

			if (!m.isPendiente()) {
				continue;
			}

			BigDecimal montoMovimiento = m.getMonto();

			if (restante.compareTo(montoMovimiento) >= 0) {

				m.setPendiente(false);
				em.merge(m);

				restante = restante.subtract(montoMovimiento);

			} else {
				break;
			}

			if (restante.compareTo(BigDecimal.ZERO) == 0) {
				break;
			}
		}

		Movimiento pago = new Movimiento(LocalDate.now(), "Pago tarjeta " + tarjeta.getNombre(), montoPago,
				TipoMovimiento.GASTO);

		pago.setFormaPago(FormaPago.DEBITO);
		pago.setCuenta(cuenta);
		pago.setPendiente(false);

		em.persist(pago);

		em.getTransaction().commit();
	}

	public List<Movimiento> getMovimientosPendientes(TarjetaCredito tarjeta) {

		return em.createQuery("SELECT m FROM Movimiento m " + "WHERE m.tarjeta = :tarjeta " + "AND m.pendiente = true "
				+ "ORDER BY m.fecha", Movimiento.class).setParameter("tarjeta", tarjeta).getResultList();
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

			m.setNumeroCuotas(cuotas);
			m.setTotalCuotas(cuotas);
			m.setCompraId(compraId);

			m.setPendiente(true);

			em.persist(m);
		}

		em.getTransaction().commit();
	}

	public BigDecimal calcularTotalFinanciado(BigDecimal monto, BigDecimal interes) {

		if (interes.compareTo(BigDecimal.ZERO) <= 0) {
			return monto;
		}

		BigDecimal recargo = monto.multiply(interes);
		return monto.add(recargo);
	}

	public BigDecimal calcularCuota(BigDecimal total, int cuotas) {

		return total.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);
	}

	public BigDecimal calcularDeudaTotal(TarjetaCredito tarjeta) {

		List<Movimiento> movimientos = getMovimientosTarjeta(tarjeta);

		BigDecimal deuda = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {
			if (m.isPendiente()) {
				deuda = deuda.add(m.getMonto());
			}
		}

		return deuda;
	}

	public BigDecimal calcularDeudaCiclo(TarjetaCredito tarjeta) {

		List<Movimiento> movimientos = getMovimientosCiclo(tarjeta);

		BigDecimal deuda = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {
			if (m.isPendiente()) {
				deuda = deuda.add(m.getMonto());
			}
		}

		return deuda;
	}

	public BigDecimal calcularDisponible(TarjetaCredito tarjeta) {

		BigDecimal deudaTotal = calcularDeudaTotal(tarjeta);

		return tarjeta.getLimite().subtract(deudaTotal);
	}

	public BigDecimal calcularSaldoFavor(TarjetaCredito tarjeta) {

		BigDecimal deuda = calcularDeudaTotal(tarjeta);

		if (deuda.compareTo(BigDecimal.ZERO) < 0) {
			return deuda.abs();
		}

		return BigDecimal.ZERO;
	}

}