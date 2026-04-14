package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.ResumenTarjeta;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.TipoMovimiento;
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

	private LocalDate obtenerInicioCiclo(TarjetaCredito tarjeta) {

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

	private LocalDate obtenerCierreActual(TarjetaCredito tarjeta) {

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

	public LocalDate calcularProximoVencimiento(TarjetaCredito tarjeta) {

		LocalDate hoy = LocalDate.now();

		int dia = Math.min(tarjeta.getDiaVencimiento(), hoy.lengthOfMonth());
		LocalDate vencimientoEsteMes = LocalDate.of(hoy.getYear(), hoy.getMonth(), dia);

		if (hoy.isAfter(vencimientoEsteMes)) {
			// ya pasó → siguiente mes
			LocalDate mesSiguiente = hoy.plusMonths(1);
			int diaAjustado = Math.min(tarjeta.getDiaVencimiento(), mesSiguiente.lengthOfMonth());
			return LocalDate.of(mesSiguiente.getYear(), mesSiguiente.getMonth(), diaAjustado);
		}

		return vencimientoEsteMes;
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

		if (montoPago == null || montoPago.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RuntimeException("El monto de pago debe ser mayor a 0");
		}

		if (cuenta == null) {
			throw new RuntimeException("Debe seleccionar una cuenta");
		}

		// 🔥 VALIDACIÓN DE SALDO
		BigDecimal saldo = em.createQuery("""
				    SELECT COALESCE(
				        SUM(
				            CASE
				                WHEN m.tipo = :ingreso THEN m.monto
				                WHEN m.tipo = :gasto THEN -m.monto
				            END
				        ), 0)
				    FROM Movimiento m
				    WHERE m.cuenta = :cuenta
				    AND (m.formaPago IS NULL OR m.formaPago <> :credito)
				""", BigDecimal.class).setParameter("cuenta", cuenta).setParameter("ingreso", TipoMovimiento.INGRESO)
				.setParameter("gasto", TipoMovimiento.GASTO).setParameter("credito", FormaPago.CREDITO)
				.getSingleResult();

		if (saldo.compareTo(montoPago) < 0) {
			throw new RuntimeException("Saldo insuficiente en la cuenta para realizar el pago");
		}

		em.getTransaction().begin();

		List<Movimiento> movimientos = getMovimientosPendientes(tarjeta);

		BigDecimal restante = montoPago;

		for (Movimiento m : movimientos) {

			BigDecimal pagado = m.getMontoPagado() == null ? BigDecimal.ZERO : m.getMontoPagado();
			BigDecimal deudaCuota = m.getRestante();

			if (deudaCuota.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			if (restante.compareTo(deudaCuota) >= 0) {

				// 🔥 paga cuota completa
				m.setMontoPagado(m.getMonto());
				m.setPendiente(false);

				restante = restante.subtract(deudaCuota);

			} else {

				// 🔥 pago parcial
				m.setMontoPagado(pagado.add(restante));
				restante = BigDecimal.ZERO;

				em.merge(m);
				break;
			}

			em.merge(m);

			if (restante.compareTo(BigDecimal.ZERO) == 0) {
				break;
			}
		}

		// 🔥 calcular cuánto realmente fue a deuda
		BigDecimal pagadoReal = montoPago.subtract(restante);

		// 🔥 registrar gasto REAL (solo lo que cancela deuda)
		if (pagadoReal.compareTo(BigDecimal.ZERO) > 0) {

			Movimiento pago = new Movimiento(LocalDate.now(), "Pago tarjeta " + tarjeta.getNombre(), pagadoReal,
					TipoMovimiento.GASTO);

			pago.setUsuario(SesionUsuario.getUsuarioActual());
			pago.setFormaPago(FormaPago.DEBITO);
			pago.setCuenta(cuenta);
			pago.setPendiente(false);

			em.persist(pago);
			cuenta.getMovimientos().add(pago);
		}

		// 🔥 saldo a favor (NO es gasto)
		if (restante.compareTo(BigDecimal.ZERO) > 0) {

			Movimiento saldoFavor = new Movimiento(LocalDate.now(), "Saldo a favor tarjeta " + tarjeta.getNombre(),
					restante, TipoMovimiento.INGRESO);

			saldoFavor.setUsuario(SesionUsuario.getUsuarioActual());
			saldoFavor.setTarjeta(tarjeta);
			saldoFavor.setPendiente(false);

			em.persist(saldoFavor);
		}

		em.getTransaction().commit();
	}

	public List<Movimiento> getMovimientosPendientes(TarjetaCredito tarjeta) {

		return em.createQuery(
				"SELECT m FROM Movimiento m WHERE m.tarjeta = :tarjeta AND m.pendiente = true ORDER BY m.fecha",
				Movimiento.class).setParameter("tarjeta", tarjeta).getResultList();
	}

	public BigDecimal calcularTotalFinanciado(BigDecimal monto, BigDecimal interes) {

		if (interes.compareTo(BigDecimal.ZERO) <= 0) {
			return monto;
		}

		BigDecimal recargo = monto.multiply(interes).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		return monto.add(recargo);
	}

	public BigDecimal calcularCuota(BigDecimal total, int cuotas) {

		return total.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);
	}

	public BigDecimal calcularDeudaTotal(TarjetaCredito tarjeta) {

		BigDecimal deuda = em.createQuery(
				"SELECT COALESCE(SUM(CASE WHEN m.tipo = :gasto THEN (m.monto - m.montoPagado) WHEN m.tipo = :ingreso THEN -m.monto "
						+ "END),0) FROM Movimiento m WHERE m.tarjeta = :tarjeta",
				BigDecimal.class).setParameter("tarjeta", tarjeta).setParameter("gasto", TipoMovimiento.GASTO)
				.setParameter("ingreso", TipoMovimiento.INGRESO).getSingleResult();

		return deuda;
	}

	public BigDecimal calcularDeudaCiclo(TarjetaCredito tarjeta) {

		LocalDate inicio = obtenerInicioCiclo(tarjeta);
		LocalDate cierre = obtenerCierreActual(tarjeta);

		List<Movimiento> movimientos = em.createQuery("""
				SELECT m
				FROM Movimiento m
				WHERE m.tarjeta = :tarjeta
				AND m.formaPago = :formaPago
				AND m.pendiente = true
				AND m.fecha BETWEEN :inicio AND :cierre
				ORDER BY m.fecha
				""", Movimiento.class).setParameter("tarjeta", tarjeta).setParameter("formaPago", FormaPago.CREDITO)
				.setParameter("inicio", inicio).setParameter("cierre", cierre).getResultList();

		BigDecimal deuda = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			BigDecimal restante = m.getMonto().subtract(m.getMontoPagado());

			if (restante.compareTo(BigDecimal.ZERO) > 0) {
				deuda = deuda.add(restante);
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

	public BigDecimal calcularPagoMinimo(TarjetaCredito tarjeta) {

		BigDecimal deuda = calcularDeudaCiclo(tarjeta);

		if (deuda.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal minimo = deuda.multiply(new BigDecimal("0.05"));

		BigDecimal minimoFijo = new BigDecimal("10000");

		if (minimo.compareTo(minimoFijo) < 0) {
			minimo = minimoFijo;
		}

		if (minimo.compareTo(deuda) > 0) {
			minimo = deuda;
		}

		return minimo;
	}

	public BigDecimal calcularDeudaMes(TarjetaCredito tarjeta, YearMonth periodo) {

		BigDecimal deudaMes = em
				.createQuery("SELECT COALESCE(SUM(m.monto - m.montoPagado),0) " + "FROM Movimiento m "
						+ "WHERE m.tarjeta = :tarjeta " + "AND m.tipo = :tipo " + "AND m.periodo = :periodo "
						+ "AND m.pendiente = true", BigDecimal.class)
				.setParameter("tarjeta", tarjeta).setParameter("tipo", TipoMovimiento.GASTO)
				.setParameter("periodo", periodo.toString()).getSingleResult();

		return deudaMes;
	}

	public BigDecimal calcularLimiteDisponible(TarjetaCredito tarjeta) {

		BigDecimal deuda = calcularDeudaTotal(tarjeta);

		return tarjeta.getLimite().subtract(deuda);
	}

	public ResumenTarjeta obtenerResumenTarjeta(TarjetaCredito tarjeta) {

		ResumenTarjeta resumen = new ResumenTarjeta();

		BigDecimal deudaTotal = calcularDeudaTotal(tarjeta);
		BigDecimal deudaMes = calcularDeudaMes(tarjeta, YearMonth.now());

		BigDecimal saldoFavor = BigDecimal.ZERO;

		if (deudaTotal.compareTo(BigDecimal.ZERO) < 0) {
			saldoFavor = deudaTotal.abs();
			deudaTotal = BigDecimal.ZERO;
		}

		BigDecimal limiteDisponible = calcularLimiteDisponible(tarjeta);

		resumen.setDeudaMes(deudaMes);
		resumen.setDeudaTotal(deudaTotal);
		resumen.setLimiteDisponible(limiteDisponible);
		resumen.setSaldoFavor(saldoFavor);

		return resumen;
	}

}