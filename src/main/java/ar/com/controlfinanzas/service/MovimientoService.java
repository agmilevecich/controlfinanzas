package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.CompraTarjeta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.Movimiento;
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
			// Validamos que cumpla las reglas de tarjeta vs cuenta
			movimiento.validar();

			em.getTransaction().begin();

			if (movimiento.getFormaPago() != FormaPago.CREDITO) {
				// Movimientos normales o pagos de tarjeta
				if (movimiento.getCuenta() == null) {
					throw new IllegalStateException(
							"Los movimientos que no son de tarjeta deben tener una cuenta asignada");
				}
				em.persist(movimiento);
				movimiento.getCuenta().getMovimientos().add(movimiento);
			} else {
				// Movimiento de tarjeta
				movimiento.setCuenta(null); // ✅ La cuenta no se asigna
				movimiento.setPendiente(true); // ✅ Marca como deuda pendiente
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
			BigDecimal interes) {

		// Calculamos el total financiado
		BigDecimal totalFinanciado = montoTotal;

		if (interes != null && interes.compareTo(BigDecimal.ZERO) > 0) {

			BigDecimal recargo = montoTotal.multiply(interes).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

			totalFinanciado = montoTotal.add(recargo);
		}

		// Calculamos monto por cuota
		BigDecimal montoCuota = totalFinanciado.divide(new BigDecimal(cuotas), 2, RoundingMode.HALF_UP);

		LocalDate fechaBase = LocalDate.now();

		UUID compraId = UUID.randomUUID();

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

			String desCuotas = descripcion + " (" + i + "/" + cuotas + ")";

			Movimiento mov = new Movimiento(fechaCuota, desCuotas, montoCuota, TipoMovimiento.GASTO);
			String periodo = mov.getFecha().getYear() + "-" + String.format("%02d", mov.getFecha().getMonthValue());

			mov.setPeriodo(periodo);
			mov.setCompraId(compraId.toString());

			mov.setFormaPago(FormaPago.CREDITO);

			mov.setCuenta(null); // compra con tarjeta no afecta cuenta
			mov.setTarjeta(tarjeta);

			mov.setNumeroCuotas(i); // cuota actual
			mov.setTotalCuotas(cuotas); // total de cuotas
			mov.setCuotasPendientes(cuotas - i);
			mov.setMontoPagado(BigDecimal.ZERO);
			mov.setInteres(interes);

			mov.setPendiente(true);

			registrarMovimiento(mov);
		}
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

}