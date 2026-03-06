package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import ar.com.controlfinanzas.domain.finanzas.TipoMovimiento;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class CuentaService {

	private EntityManager em;

	public CuentaService(EntityManager em) {
		this.em = em;
	}

	public List<Cuenta> getCuentasUsuario(Usuario usuario) {
		TypedQuery<Cuenta> query = em.createQuery("SELECT c FROM Cuenta c WHERE c.usuario = :usuario", Cuenta.class);

		query.setParameter("usuario", usuario);
		return query.getResultList();
	}

	public Cuenta crearCuenta(Usuario usuario, String nombre, String descripcion, TipoCuenta tipo, Moneda moneda,
			BigDecimal capitalInicial, double interesDiario, LocalDate fechaInicio) {

		Cuenta cuenta = new Cuenta(nombre, tipo, moneda, interesDiario, fechaInicio, usuario);

		em.getTransaction().begin();
		em.persist(cuenta);

		// Si hay capital inicial lo registramos como movimiento
		if (capitalInicial != null && capitalInicial.compareTo(BigDecimal.ZERO) > 0) {
			Movimiento movInicial = new Movimiento(fechaInicio, descripcion, capitalInicial, TipoMovimiento.INGRESO);
			movInicial.setCuenta(cuenta);
			em.persist(movInicial);
		}

		em.getTransaction().commit();

		return cuenta;
	}

	public Cuenta crearCuenta(Usuario usuario, String nombre, TipoCuenta tipo, Moneda moneda, double interesDiario,
			LocalDate fechaInicio) {

		return crearCuenta(usuario, nombre, "", tipo, moneda, BigDecimal.ZERO, interesDiario, fechaInicio);
	}
}