package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.CompraTarjeta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class MovimientoService {

	private EntityManager em;

	public MovimientoService(EntityManager em) {
		this.em = em;
	}

	public Movimiento registrarMovimiento(Movimiento movimiento) {

		try {
			// 🔥 PRIMERO ajustar estado
			if (movimiento.getFormaPago() == FormaPago.CREDITO) {
				movimiento.setCuenta(null);
				movimiento.setPendiente(true);
			}
			if (movimiento.getTipo() == TipoMovimiento.GASTO && movimiento.getFormaPago() != FormaPago.CREDITO) {

				Cuenta cuenta = movimiento.getCuenta();

				if (cuenta == null && movimiento.getFormaPago() != FormaPago.CREDITO) {
					throw new RuntimeException("La cuenta es obligatoria");
				}

				BigDecimal saldo = obtenerSaldoCuenta(cuenta);

				if (saldo.compareTo(movimiento.getMonto()) < 0) {
					throw new RuntimeException("Saldo insuficiente en la cuenta");
				}
			}
			// ✅ DESPUÉS validar
			movimiento.validar();

			em.getTransaction().begin();

			if (movimiento.getFormaPago() != FormaPago.CREDITO) {
				em.persist(movimiento);
				movimiento.getCuenta().getMovimientos().add(movimiento);
			} else {
				em.persist(movimiento);
			}

			em.getTransaction().commit();

		} catch (Exception e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

		return movimiento;
	}

	public List<Movimiento> getMovimientosCuenta(Cuenta cuenta) {

		TypedQuery<Movimiento> query = em.createQuery(
				"SELECT m FROM Movimiento m " + "WHERE m.cuenta = :cuenta "
						+ "AND (m.formaPago <> :credito or m.formaPago is null) ORDER BY m.fecha DESC",
				Movimiento.class);

		query.setParameter("cuenta", cuenta);
		query.setParameter("credito", FormaPago.CREDITO);

		return query.getResultList();
	}

	public void registrarCompraCuotas(TarjetaCredito tarjeta, String descripcion, BigDecimal montoTotal, int cuotas,
			BigDecimal interes, CategoriaGasto categoria) {

		try {
			em.getTransaction().begin();

			BigDecimal totalFinanciado = montoTotal;

			if (interes != null && interes.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal recargo = montoTotal.multiply(interes).divide(new BigDecimal("100"), 2,
						RoundingMode.HALF_UP);

				totalFinanciado = montoTotal.add(recargo);
			}

			BigDecimal montoBase = totalFinanciado.divide(new BigDecimal(cuotas), 2, RoundingMode.DOWN);

			BigDecimal totalCalculado = montoBase.multiply(BigDecimal.valueOf(cuotas));
			BigDecimal diferencia = totalFinanciado.subtract(totalCalculado);

			LocalDate fechaBase = LocalDate.now();

			UUID compraId = generarCompraId();

			CompraTarjeta compra = new CompraTarjeta();
			compra.setId(compraId);
			compra.setComercio(descripcion);
			compra.setMontoTotal(montoTotal);
			compra.setCuotas(cuotas);
			compra.setInteres(interes);
			compra.setFechaCompra(LocalDate.now());
			compra.setTarjeta(tarjeta);

			em.persist(compra);

			for (int i = 1; i <= cuotas; i++) {

				LocalDate fechaCuota = fechaBase.plusMonths(i - 1);

				BigDecimal montoCuota = montoBase;

				if (i == cuotas) {
					montoCuota = montoCuota.add(diferencia);
				}

				String desCuotas = descripcion + " (" + i + "/" + cuotas + ")";

				Movimiento mov = new Movimiento(fechaCuota, desCuotas, montoCuota, TipoMovimiento.GASTO);

				mov.setUsuario(SesionUsuario.getUsuarioActual());
				mov.setPeriodo(generarPeriodo(mov));
				mov.setCompraId(compraId.toString());
				mov.setCategoria(categoria);
				mov.setFormaPago(FormaPago.CREDITO);
				mov.setTarjeta(tarjeta);

				mov.setNumeroCuotas(i);
				mov.setTotalCuotas(cuotas);
				mov.setCuotasPendientes(cuotas - i);
				mov.setMontoPagado(BigDecimal.ZERO);
				mov.setInteres(interes);
				mov.setPendiente(true);

				// 🔴 IMPORTANTE: NO usar registrarMovimiento acá
				em.persist(mov);
			}

			em.getTransaction().commit();

		} catch (Exception e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	private UUID generarCompraId() {
		return UUID.randomUUID();
	}

	private String generarPeriodo(Movimiento mov) {
		return mov.getFecha().getYear() + "-" + String.format("%02d", mov.getFecha().getMonthValue());
	}

	public BigDecimal calcularDeudaTarjeta(TarjetaCredito tarjeta) {

		BigDecimal total = em
				.createQuery("SELECT COALESCE(SUM(m.monto),0) FROM Movimiento m "
						+ "WHERE m.tarjeta = :tarjeta AND m.pendiente = true", BigDecimal.class)
				.setParameter("tarjeta", tarjeta).getSingleResult();

		return total;
	}

	public List<Movimiento> getCuotasCompra(String compraId) {

		String jpql = """
				    SELECT m
				    FROM Movimiento m
				    WHERE m.compraId = :compraId
				    ORDER BY m.numeroCuotas
				""";

		return em.createQuery(jpql, Movimiento.class).setParameter("compraId", compraId).getResultList();
	}

	public int calcularCuotasPagadas(String compraId) {

		String jpql = """
				    SELECT COUNT(m)
				    FROM Movimiento m
				    WHERE m.compraId = :compraId
				    AND m.pendiente = false
				""";

		Long pagadas = em.createQuery(jpql, Long.class).setParameter("compraId", compraId).getSingleResult();

		return pagadas.intValue();
	}

	public List<Object[]> obtenerGastosPorCategoria() {

		String jpql = """
				    SELECT m.categoria, SUM(m.monto)
				    FROM Movimiento m
				    WHERE m.tipo = :tipo
				    AND m.usuario = :usuario
				    GROUP BY m.categoria
				""";

		return em.createQuery(jpql, Object[].class).setParameter("tipo", TipoMovimiento.GASTO)
				.setParameter("usuario", SesionUsuario.getUsuarioActual()).getResultList();
	}

	public List<Object[]> obtenerDeudaPorCategoria() {

		String jpql = """
				    SELECT m.categoria, SUM(m.monto)
				    FROM Movimiento m
				    WHERE m.formaPago = :credito
				    AND m.pendiente = true
				    AND m.usuario = :usuario
				    GROUP BY m.categoria
				""";

		return em.createQuery(jpql, Object[].class).setParameter("credito", FormaPago.CREDITO)
				.setParameter("usuario", SesionUsuario.getUsuarioActual()).getResultList();
	}

	public List<Movimiento> listarPorUsuario() {
		String jpql = """
				    SELECT m FROM Movimiento m
				    WHERE m.usuario = :usuario
				    AND m.tipo = :tipo
				    ORDER BY m.fecha DESC
				""";

		return em.createQuery(jpql, Movimiento.class).setParameter("usuario", SesionUsuario.getUsuarioActual())
				.setParameter("tipo", TipoMovimiento.GASTO).getResultList();
	}

	public BigDecimal obtenerTotalPorUsuario(Integer usuarioId) {
		return em
				.createQuery("SELECT COALESCE(SUM(g.monto), 0) " + "FROM Gasto g "
						+ "WHERE g.usuario.usuarioID = :usuarioId", BigDecimal.class)
				.setParameter("usuarioId", usuarioId).getSingleResult();
	}

	public void guardar(Movimiento movimiento) {

		try {
			em.getTransaction().begin();
			em.persist(movimiento);
			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;

		}
	}

	public void eliminar(Integer id) {

		try {
			em.getTransaction().begin();

			Movimiento movimiento = em.find(Movimiento.class, id);
			if (movimiento != null) {
				em.remove(movimiento);
			}

			em.getTransaction().commit();

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

	public List<Movimiento> listarPorUsuarioYPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {

		return em.createQuery("""
				SELECT m FROM Movimiento m
				WHERE m.usuario.usuarioID = :usuarioId
				AND m.fecha BETWEEN :inicio AND :fin
				""", Movimiento.class).setParameter("usuarioId", SesionUsuario.getUsuarioActual().getUsuarioID())
				.setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

	}

	public BigDecimal calcularTotalGastos() {
		List<Movimiento> movimientos = listarPorUsuario();

		return movimientos.stream().map(m -> m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularTotalHistorico(Integer usuarioId) {
		return listarPorUsuario().stream().map(m -> m.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularTotalPorMes(Integer usuarioId, YearMonth mes) {
		return listarPorUsuario().stream().filter(m -> YearMonth.from(m.getFecha()).equals(mes)).map(m -> m.getMonto())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calcularTotalGastosHistoricos(Usuario usuario) {
		return obtenerTotalPorUsuario(usuario.getUsuarioID());
	}

	public BigDecimal calcularTotalPorCategoria(CategoriaGasto categoria) {

		List<Movimiento> movimientos = listarPorUsuario();

		BigDecimal total = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			if (m.getCategoria() == categoria && m.getMonto() != null) {
				total = total.add(m.getMonto());
			}
		}

		return total;
	}

	public BigDecimal calcularTotalesPorCategoriaYMes(CategoriaGasto categoria, YearMonth mes) {

		List<Movimiento> movimientos = listarPorUsuario();

		BigDecimal total = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			if (m.getCategoria() != null && m.getCategoria().equals(categoria) && m.getFecha() != null
					&& YearMonth.from(m.getFecha()).equals(mes) && m.getMonto() != null) {

				total = total.add(m.getMonto());
			}
		}

		return total;
	}

	public Map<CategoriaGasto, BigDecimal> calcularTotalesPorCategoriaYMes(YearMonth mes) {

		List<Movimiento> movimiento = listarPorUsuario();

		Map<CategoriaGasto, BigDecimal> totales = new EnumMap<>(CategoriaGasto.class);

		// inicializar todas en cero
		for (CategoriaGasto c : CategoriaGasto.values()) {
			totales.put(c, BigDecimal.ZERO);
		}

		for (Movimiento m : movimiento) {

			if (m.getFecha() == null || m.getMonto() == null || m.getCategoria() == null) {
				continue;
			}

			if (YearMonth.from(m.getFecha()).equals(mes)) {
				CategoriaGasto categoria = m.getCategoria();
				totales.put(categoria, totales.get(categoria).add(m.getMonto()));
			}
		}

		return totales;
	}

	public LinkedHashMap<CategoriaGasto, BigDecimal> rankingCategoriasPorMes(YearMonth mes) {

		List<Movimiento> movimiento = listarPorUsuario();

		Map<CategoriaGasto, BigDecimal> acumulado = new HashMap<CategoriaGasto, BigDecimal>();

		for (Movimiento m : movimiento) {
			if (m.getFecha() != null && YearMonth.from(m.getFecha()).equals(mes) && m.getMonto() != null) {
				acumulado.merge(m.getCategoria(), m.getMonto(), BigDecimal::add);
			}
		}

		return acumulado.entrySet().stream().sorted(Map.Entry.<CategoriaGasto, BigDecimal>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
	}

	public Map<CategoriaGasto, BigDecimal> obtenerTotalesPorPeriodo(Integer usuarioId, YearMonth periodo) {
		LocalDate inicio = periodo.atDay(1);
		LocalDate fin = periodo.atEndOfMonth();

		List<Movimiento> movimientos = listarPorUsuarioYPeriodo(inicio, fin);

		Map<CategoriaGasto, BigDecimal> totales = new HashMap<>();

		for (Movimiento m : movimientos) {
			totales.merge(m.getCategoria(), m.getMonto(), BigDecimal::add);
		}

		return totales;

	}

	public BigDecimal obtenerTotalDelMes(YearMonth periodo) {
		LocalDate inicio = periodo.atDay(1);
		LocalDate fin = periodo.atEndOfMonth();

		List<Movimiento> movimientos = listarPorUsuarioYPeriodo(inicio, fin);

		return movimientos.stream().map(Movimiento::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

	}

	public List<Object[]> obtenerDeudaPorMes() {
		String jpql = """
				    SELECT YEAR(m.fecha), MONTH(m.fecha), SUM(m.monto)
				    FROM Movimiento m
				    WHERE m.formaPago = :credito
				    AND m.pendiente = true
				    AND m.usuario = :usuario
				    GROUP BY YEAR(m.fecha), MONTH(m.fecha)
				    ORDER BY YEAR(m.fecha), MONTH(m.fecha)
				""";

		return em.createQuery(jpql, Object[].class).setParameter("credito", FormaPago.CREDITO)
				.setParameter("usuario", SesionUsuario.getUsuarioActual()).getResultList();
	}

	public List<Object[]> obtenerDeudaPorPeriodo() {

		String jpql = """
				    SELECT m.periodo, SUM(m.monto)
				    FROM Movimiento m
				    WHERE m.formaPago = :credito
				    AND m.pendiente = true
				    AND m.usuario.usuarioID = :usuarioId
				    GROUP BY m.periodo
				    ORDER BY m.periodo
				""";

		return em.createQuery(jpql, Object[].class).setParameter("credito", FormaPago.CREDITO)
				.setParameter("usuarioId", SesionUsuario.getUsuarioActual().getUsuarioID()).getResultList();
	}

	public BigDecimal obtenerSaldoCuenta(Cuenta cuenta) {

		return em.createQuery("""
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

	}

	public List<Object[]> obtenerSaldoAcumuladoPorMes() {

		return em.createQuery("""
				    SELECT
				        YEAR(m.fecha),
				        MONTH(m.fecha),
				        SUM(
				            CASE
				                WHEN m.tipo = :ingreso THEN m.monto
				                WHEN m.tipo = :gasto THEN -m.monto
				            END
				        )
				    FROM Movimiento m
				    WHERE m.formaPago IS NULL OR m.formaPago <> :credito
				    GROUP BY YEAR(m.fecha), MONTH(m.fecha)
				    ORDER BY YEAR(m.fecha), MONTH(m.fecha)
				""", Object[].class).setParameter("ingreso", TipoMovimiento.INGRESO)
				.setParameter("gasto", TipoMovimiento.GASTO).setParameter("credito", FormaPago.CREDITO).getResultList();
	}
}