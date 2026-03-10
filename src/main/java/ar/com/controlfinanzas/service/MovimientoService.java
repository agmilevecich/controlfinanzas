package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JOptionPane;

import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
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

	public Movimiento registrarMovimiento(Cuenta cuenta, Movimiento movimiento) {

		try {

			movimiento.validar();

			movimiento.setCuenta(cuenta); // SIEMPRE se asigna

			em.getTransaction().begin();

			em.persist(movimiento);

			if (movimiento.getFormaPago() != FormaPago.CREDITO) {
				cuenta.getMovimientos().add(movimiento);
			} else {
				movimiento.setPendiente(true); // deuda de tarjeta
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
				"SELECT m FROM Movimiento m WHERE m.cuenta = :cuenta ORDER BY m.fecha DESC", Movimiento.class);

		query.setParameter("cuenta", cuenta);

		return query.getResultList();
	}

	public void registrarCompraCuotas(Cuenta cuenta, TarjetaCredito tarjeta, String descripcion, BigDecimal montoTotal,
			int cuotas, BigDecimal interes) {

		BigDecimal totalFinanciado = montoTotal;

		if (interes != null && interes.compareTo(BigDecimal.ZERO) > 0) {
			totalFinanciado = montoTotal.add(montoTotal.multiply(interes).divide(new BigDecimal("100")));
		}

		BigDecimal montoCuota = totalFinanciado.divide(new BigDecimal(cuotas), 2, RoundingMode.HALF_UP);

		LocalDate fechaBase = LocalDate.now();

		for (int i = 1; i <= cuotas; i++) {

			LocalDate fechaCuota = fechaBase.plusMonths(i - 1);

			Movimiento mov = new Movimiento(fechaCuota, descripcion + " (" + i + "/" + cuotas + ")", montoCuota,
					TipoMovimiento.GASTO);

			mov.setFormaPago(FormaPago.CREDITO);
			mov.setTarjeta(tarjeta);
			mov.setCuotas(cuotas);
			mov.setCuotasPendientes(cuotas - i);
			mov.setInteres(interes);
			mov.setPendiente(true);

			registrarMovimiento(cuenta, mov);
		}
	}

}