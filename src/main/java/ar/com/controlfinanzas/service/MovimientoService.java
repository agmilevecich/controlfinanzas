package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.CompraTarjeta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.ReglaCategoria;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
import ar.com.controlfinanzas.model.TipoMovimiento;
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

			if (movimiento.getTipo() == TipoMovimiento.GASTO && movimiento.getFormaPago() != FormaPago.CREDITO
					&& movimiento.getCategoria() != CategoriaGasto.AJUSTE
					&& movimiento.getCategoria() != CategoriaGasto.TRANSFERENCIA) {

				Cuenta cuenta = movimiento.getCuenta();

				if (cuenta == null) {
					throw new RuntimeException("La cuenta es obligatoria");
				}

				BigDecimal saldo = obtenerSaldoCuenta(cuenta);

				if (saldo.compareTo(movimiento.getMonto()) < 0) {
					throw new RuntimeException("Saldo insuficiente en la cuenta");
				}
			}

			// ✅ validar
			movimiento.validar();

			em.getTransaction().begin();

			em.persist(movimiento);

			if (movimiento.getFormaPago() != FormaPago.CREDITO) {
				movimiento.getCuenta().getMovimientos().add(movimiento);
			}

			// 🔥 APRENDIZAJE (ACÁ VA)
			if (movimiento.getTipo() == TipoMovimiento.GASTO && movimiento.getCategoria() != null
					&& movimiento.getCategoria() != CategoriaGasto.AJUSTE
					&& movimiento.getCategoria() != CategoriaGasto.TRANSFERENCIA) {

				guardarReglaCategoria(movimiento.getDescripcion(), movimiento.getCategoria(), movimiento.getUsuario());
			}
			em.getTransaction().commit();

			// 🔥 APRENDIZAJE (ACÁ VA)
			if (movimiento.getTipo() == TipoMovimiento.GASTO && movimiento.getCategoria() != null
					&& movimiento.getCategoria() != CategoriaGasto.AJUSTE
					&& movimiento.getCategoria() != CategoriaGasto.TRANSFERENCIA) {

				guardarReglaCategoria(movimiento.getDescripcion(), movimiento.getCategoria(), movimiento.getUsuario());
			}

		} catch (Exception e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

		return movimiento;
	}

	public List<Movimiento> getMovimientosCuenta(Cuenta cuenta) {

		TypedQuery<Movimiento> query = em.createQuery("SELECT m FROM Movimiento m " + "WHERE m.cuenta = :cuenta "
				+ "AND (m.formaPago <> :credito or m.formaPago is null)", Movimiento.class);

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

			Movimiento movimiento = null;

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
				movimiento = mov;
			}

			// 🔥 APRENDIZAJE (ACÁ VA)
			if (movimiento.getTipo() == TipoMovimiento.GASTO && movimiento.getCategoria() != null
					&& movimiento.getCategoria() != CategoriaGasto.AJUSTE
					&& movimiento.getCategoria() != CategoriaGasto.TRANSFERENCIA) {

				guardarReglaCategoria(movimiento.getDescripcion(), movimiento.getCategoria(), tarjeta.getUsuario());
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
				    AND m.tipo = :tipo AND m.categoria != :categoria
				    ORDER BY m.fecha DESC
				""";

		return em.createQuery(jpql, Movimiento.class).setParameter("usuario", SesionUsuario.getUsuarioActual())
				.setParameter("tipo", TipoMovimiento.GASTO).setParameter("categoria", CategoriaGasto.TRANSFERENCIA)
				.getResultList();
	}

	public BigDecimal obtenerTotalPorUsuario(Integer usuarioId) {
		return em
				.createQuery("SELECT COALESCE(SUM(g.monto), 0) " + "FROM movimientos m "
						+ "WHERE m.usuario.usuarioID = :usuarioId AND m.tipo = :tipo AND categoria != :categoria",
						BigDecimal.class)
				.setParameter("usuarioId", usuarioId).setParameter("tipo", TipoMovimiento.GASTO)
				.setParameter("categoria", CategoriaGasto.TRANSFERENCIA).getSingleResult();
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

	public BigDecimal calcularTotalMesActual(Integer usuarioId) {

		YearMonth mesActual = YearMonth.now();

		return em.createQuery("""
				SELECT COALESCE(SUM(m.monto), 0)
				FROM Movimiento m
				WHERE m.usuario.usuarioID = :usuarioId
				AND m.tipo = :tipo
				AND FUNCTION('YEAR', m.fecha) = :anio
				AND FUNCTION('MONTH', m.fecha) = :mes
				""", BigDecimal.class).setParameter("usuarioId", usuarioId).setParameter("tipo", TipoMovimiento.GASTO)
				.setParameter("anio", mesActual.getYear()).setParameter("mes", mesActual.getMonthValue())
				.getSingleResult();
	}

	public BigDecimal calcularTotalMesAnterior(Integer usuarioId) {

		YearMonth mesAnterior = YearMonth.now().minusMonths(1);

		return em.createQuery("""
				SELECT COALESCE(SUM(m.monto), 0)
				FROM Movimiento m
				WHERE m.usuario.usuarioID = :usuarioId
				AND m.tipo = :tipo
				AND FUNCTION('YEAR', m.fecha) = :anio
				AND FUNCTION('MONTH', m.fecha) = :mes
				""", BigDecimal.class).setParameter("usuarioId", usuarioId).setParameter("tipo", TipoMovimiento.GASTO)
				.setParameter("anio", mesAnterior.getYear()).setParameter("mes", mesAnterior.getMonthValue())
				.getSingleResult();
	}

	public BigDecimal calcularIngresosMesActual(Integer usuarioId) {

		LocalDate inicio = LocalDate.now().withDayOfMonth(1);
		LocalDate fin = inicio.plusMonths(1).minusDays(1);

		return em.createQuery("""
				SELECT COALESCE(SUM(m.monto), 0)
				FROM Movimiento m
				WHERE m.usuario.usuarioID = :usuarioId
				AND m.tipo = :tipo
				AND m.fecha BETWEEN :inicio AND :fin
				""", BigDecimal.class).setParameter("usuarioId", usuarioId).setParameter("tipo", TipoMovimiento.INGRESO)
				.setParameter("inicio", inicio).setParameter("fin", fin).getSingleResult();
	}

	public BigDecimal calcularIngresosMesAnterior(Integer usuarioId) {

		LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
		LocalDate fin = inicio.plusMonths(1).minusDays(1);

		return em.createQuery("""
				SELECT COALESCE(SUM(m.monto), 0)
				FROM Movimiento m
				WHERE m.usuario.usuarioID = :usuarioId
				AND m.tipo = :tipo
				AND m.fecha BETWEEN :inicio AND :fin
				""", BigDecimal.class).setParameter("usuarioId", usuarioId).setParameter("tipo", TipoMovimiento.INGRESO)
				.setParameter("inicio", inicio).setParameter("fin", fin).getSingleResult();
	}

	public void ajustarSaldo(Cuenta cuenta, BigDecimal nuevoSaldo) {

		BigDecimal saldoActual = obtenerSaldoCuenta(cuenta);

		BigDecimal diferencia = nuevoSaldo.subtract(saldoActual);

		if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
			return; // no hay nada que hacer
		}

		TipoMovimiento tipo = diferencia.compareTo(BigDecimal.ZERO) > 0 ? TipoMovimiento.INGRESO : TipoMovimiento.GASTO;

		BigDecimal monto = diferencia.abs();

		Movimiento ajuste = new Movimiento(java.time.LocalDate.now(), "Ajuste de saldo", monto, tipo);
		ajuste.setCategoria(CategoriaGasto.AJUSTE);
		ajuste.setCuenta(cuenta);
		ajuste.setUsuario(SesionUsuario.getUsuarioActual());

		registrarMovimiento(ajuste);
	}

	public void transferir(Cuenta origen, Cuenta destino, BigDecimal monto, String descripcion, Usuario usuario) {

		if (origen == null || destino == null) {
			throw new IllegalArgumentException("Cuentas inválidas");
		}
		if (origen.equals(destino)) {
			throw new IllegalArgumentException("No podés transferir a la misma cuenta");
		}
		if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Monto inválido");
		}
		if (origen.getSaldo().compareTo(monto) < 0) {
			throw new IllegalArgumentException("Saldo insuficiente");
		}

		Movimiento origenMovimiento = new Movimiento(LocalDate.now(),
				descripcion + " (" + origen.getNombre() + " -> " + destino.getNombre() + ")", monto, // resta
																										// del
																										// origen
				TipoMovimiento.GASTO);

		String transferenciaId = UUID.randomUUID().toString();
		origenMovimiento.setCuenta(origen);
		origenMovimiento.setCategoria(CategoriaGasto.TRANSFERENCIA);
		origenMovimiento.setTransferenciaId(transferenciaId);
		origenMovimiento.setUsuario(usuario);

		Movimiento destinoMovimiento = new Movimiento(LocalDate.now(),
				descripcion + " (" + origen.getNombre() + " -> " + destino.getNombre() + ")", monto, // suma al destino
				TipoMovimiento.INGRESO);

		destinoMovimiento.setCuenta(destino);
		destinoMovimiento.setCategoria(CategoriaGasto.TRANSFERENCIA);
		destinoMovimiento.setTransferenciaId(transferenciaId);
		destinoMovimiento.setUsuario(usuario);
		registrarMovimiento(origenMovimiento);
		registrarMovimiento(destinoMovimiento);
	}

	public CategoriaGasto sugerirCategoria(String descripcion) {

		if (descripcion == null || descripcion.isBlank()) {
			return null;
		}

		List<Movimiento> movimientos = listarPorUsuario();

		Map<CategoriaGasto, BigDecimal> montoPorCategoria = new HashMap<>();
		Map<CategoriaGasto, Long> frecuencia = new HashMap<>();

		for (Movimiento m : movimientos) {

			if (m.getCategoria() == null || m.getDescripcion() == null) {
				continue;
			}

			if (!m.getDescripcion().toLowerCase().contains(descripcion.toLowerCase())) {
				continue;
			}

			CategoriaGasto cat = m.getCategoria();

			frecuencia.put(cat, frecuencia.getOrDefault(cat, 0L) + 1);
			montoPorCategoria.put(cat, montoPorCategoria.getOrDefault(cat, BigDecimal.ZERO).add(m.getMonto()));
		}

		CategoriaGasto mejor = null;
		double mejorScore = 0;

		for (CategoriaGasto cat : frecuencia.keySet()) {

			long freq = frecuencia.get(cat);
			BigDecimal monto = montoPorCategoria.get(cat);

			double score = freq + (monto.doubleValue() * 0.0001);

			if (score > mejorScore) {
				mejorScore = score;
				mejor = cat;
			}
		}

		return mejor;
	}

	public CategoriaGasto sugerirCategoriaAvanzada(String descripcion) {

		if (descripcion == null || descripcion.isBlank()) {
			return null;
		}

		String texto = limpiarDescripcion(descripcion).toLowerCase();

		// 🔥 1. BUSCAR REGLA
		ReglaCategoria regla = em.createQuery(
				"SELECT r FROM ReglaCategoria r WHERE :texto LIKE CONCAT('%', r.palabraClave, '%') AND r.usuario = :usuario",
				ReglaCategoria.class).setParameter("texto", texto)
				.setParameter("usuario", SesionUsuario.getUsuarioActual()).setMaxResults(1).getResultStream()
				.findFirst().orElse(null);

		if (regla != null) {
			return regla.getCategoria();
		}

		// 🔁 2. fallback al método anterior (historial)
		return sugerirCategoriaPorHistorial(texto);
	}

	public CategoriaGasto sugerirCategoriaPorHistorial(String texto) {

		if (texto == null || texto.isBlank()) {
			return null;
		}

		Map<CategoriaGasto, Integer> contador = new HashMap<>();

		for (Movimiento m : listarPorUsuario()) {

			if (m.getCategoria() == null) {
				continue;
			}

			String desc = limpiarDescripcion(m.getDescripcion()).toLowerCase();

			// 🔎 coincidencia flexible
			if (desc.startsWith(texto) || texto.startsWith(desc)) {

				contador.put(m.getCategoria(), contador.getOrDefault(m.getCategoria(), 0) + 1);
			}
		}

		// 🎯 devolver la categoría más frecuente
		return contador.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
	}

	public List<String> sugerirDescripciones(String texto) {

		if (texto == null || texto.isBlank()) {
			return List.of();
		}

		String textoLower = texto.toLowerCase();

		List<String> descripciones = listarPorUsuario().stream().map(Movimiento::getDescripcion)
				.map(this::limpiarDescripcion).filter(d -> d != null).distinct().toList();

		// Primero intentamos coincidencias que empiezan igual
		List<String> startsWith = descripciones.stream().filter(d -> d.toLowerCase().startsWith(textoLower)).limit(5)
				.toList();

		if (!startsWith.isEmpty()) {
			return startsWith;
		}

		// Si no hay, usamos contains
		return descripciones.stream().filter(d -> d.toLowerCase().contains(textoLower)).limit(5).toList();
	}

	private void guardarReglaCategoria(String descripcion, CategoriaGasto categoria, Usuario usuario) {

		String clave = limpiarDescripcion(descripcion).toLowerCase();

		if (clave.length() < 3) {
			return;
		}

		ReglaCategoria existente = em
				.createQuery("SELECT r FROM ReglaCategoria r WHERE r.palabraClave = :clave AND r.usuario = :usuario",
						ReglaCategoria.class)
				.setParameter("clave", clave).setParameter("usuario", usuario).getResultStream().findFirst()
				.orElse(null);

		if (existente == null) {
			ReglaCategoria r = new ReglaCategoria();
			r.setPalabraClave(clave);
			r.setCategoria(categoria);
			r.setUsuario(usuario);
			em.persist(r);
		} else {
			existente.setCategoria(categoria); // se actualiza si cambió
		}
	}

	public boolean esGastoRecurrente(String descripcion, BigDecimal monto, Usuario usuario) {

		String desLimpio = limpiarDescripcion(descripcion);

		List<Movimiento> movimientos = listarPorUsuario().stream().filter(m -> m.getTipo() == TipoMovimiento.GASTO)
				.filter(m -> m.getDescripcion() != null)
				.filter(m -> limpiarDescripcion(m.getDescripcion()).startsWith(desLimpio)).toList();
		if (movimientos.size() < 2) {
			return false;
		}

		movimientos = movimientos.stream().sorted((a, b) -> a.getFecha().compareTo(b.getFecha())).toList();

		int coincidencias = 0;

		for (int i = 1; i < movimientos.size(); i++) {
			Movimiento anterior = movimientos.get(i - 1);
			Movimiento actual = movimientos.get(i);

			long dias = ChronoUnit.DAYS.between(anterior.getFecha(), actual.getFecha());
			BigDecimal diferenciaMonto = anterior.getMonto().subtract(actual.getMonto().abs());

			boolean mensual = dias >= 25 && dias <= 35;
			boolean montoSimilar = diferenciaMonto.compareTo(new BigDecimal("200")) < 0;

			if (mensual && montoSimilar) {
				coincidencias++;
			}
		}

		return coincidencias >= 1;

	}

	public BigDecimal predecirGastoMesSiguiente(Integer usuarioId) {

		List<Movimiento> movimientos = listarPorUsuario().stream().filter(m -> m.getTipo() == TipoMovimiento.GASTO)
				.filter(m -> m.getCategoria() != CategoriaGasto.AJUSTE)
				.filter(m -> m.getCategoria() != CategoriaGasto.TRANSFERENCIA).toList();

		if (movimientos.isEmpty()) {
			return BigDecimal.ZERO;
		}

		// agrupar por mes (yyyy-MM)
		Map<String, BigDecimal> gastosPorMes = new TreeMap<>();

		for (Movimiento m : movimientos) {

			String clave = m.getFecha().getYear() + "-" + String.format("%02d", m.getFecha().getMonthValue());

			gastosPorMes.put(clave, gastosPorMes.getOrDefault(clave, BigDecimal.ZERO).add(m.getMonto()));
		}

		List<BigDecimal> ultimosMeses = gastosPorMes.values().stream().skip(Math.max(0, gastosPorMes.size() - 3))
				.toList();

		if (ultimosMeses.isEmpty()) {
			return BigDecimal.ZERO;
		}

		// promedio
		BigDecimal suma = ultimosMeses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal promedio = suma.divide(BigDecimal.valueOf(ultimosMeses.size()), 2, RoundingMode.HALF_UP);

		// 🔥 tendencia simple (último vs anterior)
		if (ultimosMeses.size() >= 2) {
			BigDecimal ultimo = ultimosMeses.get(ultimosMeses.size() - 1);
			BigDecimal anterior = ultimosMeses.get(ultimosMeses.size() - 2);

			BigDecimal diferencia = ultimo.subtract(anterior);

			// suavizamos impacto
			promedio = promedio.add(diferencia.multiply(new BigDecimal("0.5")));
		}

		return promedio.max(BigDecimal.ZERO);
	}

	public BigDecimal calcularPromedioGasto(Integer usuarioId) {

		YearMonth mesActual = YearMonth.now();

		List<Movimiento> movimientos = listarPorUsuario();

		List<Movimiento> gastos = movimientos.stream().filter(m -> m.getTipo() == TipoMovimiento.GASTO)
				.filter(m -> m.getUsuario().getUsuarioID().equals(usuarioId))
				.filter(m -> m.getCategoria() != CategoriaGasto.AJUSTE)
				.filter(m -> m.getCategoria() != CategoriaGasto.TRANSFERENCIA)
				.filter(m -> YearMonth.from(m.getFecha()).equals(mesActual)).toList();

		if (gastos.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = gastos.stream().map(Movimiento::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

		return total.divide(BigDecimal.valueOf(gastos.size()), 2, RoundingMode.HALF_UP);
	}

	private String limpiarDescripcion(String descripcion) {
		if (descripcion == null) {
			return "";
		}

		return descripcion.replaceAll("\\s*\\([^)]*\\)$", "");
	}

}