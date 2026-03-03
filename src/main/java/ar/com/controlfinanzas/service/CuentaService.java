package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class CuentaService {

	private EntityManager em;

	public CuentaService(EntityManager em) {
		this.em = em;
	}

	// Devuelve todas las cuentas de un usuario
	public List<Cuenta> getCuentasUsuario(Usuario usuario) {
		TypedQuery<Cuenta> query = em.createQuery("SELECT c FROM Cuenta c WHERE c.usuario = :usuario", Cuenta.class);
		query.setParameter("usuario", usuario);
		return query.getResultList();
	}

	// Crear una cuenta usando enums
	public Cuenta crearCuenta(Usuario usuario, String nombre, TipoCuenta tipo, Moneda moneda, BigDecimal capitalInicial,
			double interesDiario, LocalDate fechaInicio) {
		Cuenta cuenta = new Cuenta(nombre, capitalInicial, interesDiario, fechaInicio, tipo, moneda, usuario);

		em.getTransaction().begin();
		em.persist(cuenta);
		em.getTransaction().commit();

		return cuenta;
	}

	public Cuenta crearCuenta(Usuario usuario, String nombre, TipoCuenta tipo, Moneda moneda, double interesDiario,
			LocalDate fechaInicio) {
// Llamamos al método existente pasando capital inicial = 0
		return crearCuenta(usuario, nombre, tipo, moneda, BigDecimal.ZERO, interesDiario, fechaInicio);
	}
}