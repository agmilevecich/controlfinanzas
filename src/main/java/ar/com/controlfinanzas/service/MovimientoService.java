package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.CategoriaGasto;
import ar.com.controlfinanzas.model.CompraTarjeta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.SesionUsuario;
import ar.com.controlfinanzas.model.TarjetaCredito;
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
}