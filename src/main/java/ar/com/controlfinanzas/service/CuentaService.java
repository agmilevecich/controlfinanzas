package ar.com.controlfinanzas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ar.com.controlfinanzas.model.Banco;
import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.Movimiento;
import ar.com.controlfinanzas.model.TipoCuenta;
import ar.com.controlfinanzas.model.TipoMovimiento;
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

	public Cuenta crearCuenta(Usuario usuario, String nombre, Banco banco, String descripcion, TipoCuenta tipo,
			Moneda moneda, BigDecimal capitalInicial, double interesDiario, LocalDate fechaInicio) {

		Cuenta cuenta = new Cuenta(nombre, banco, tipo, moneda, interesDiario, fechaInicio, usuario);

		em.getTransaction().begin();
		em.persist(cuenta);

		// Si hay capital inicial lo registramos como movimiento
		if (capitalInicial != null && capitalInicial.compareTo(BigDecimal.ZERO) > 0) {
			Movimiento movInicial = new Movimiento(fechaInicio, descripcion, capitalInicial, TipoMovimiento.INGRESO);
			cuenta.getMovimientos().add(movInicial);
			movInicial.setCuenta(cuenta);
			em.persist(movInicial);
		}

		em.getTransaction().commit();

		return cuenta;
	}

	public Cuenta crearCuenta(Usuario usuario, String nombre, Banco banco, TipoCuenta tipo, Moneda moneda,
			double interesDiario, LocalDate fechaInicio) {

		return crearCuenta(usuario, nombre, banco, "", tipo, moneda, BigDecimal.ZERO, interesDiario, fechaInicio);
	}

	public void actualizarCuenta(Cuenta cuenta) {

		Cuenta cuentaPersistida = em.find(Cuenta.class, cuenta.getId());

		if (cuentaPersistida == null) {
			throw new RuntimeException("La cuenta no existe");
		}

		if (!cuentaPersistida.puedeCambiarMoneda(cuenta.getMoneda())) {
			throw new RuntimeException("No se puede cambiar la moneda con saldo en cuenta");
		}

		em.getTransaction().begin();

		cuentaPersistida.actualizarDatos(cuenta.getNombre(), cuenta.getTipoCuenta(), cuenta.getMoneda(),
				cuenta.getBanco());

		em.getTransaction().commit();
	}

	public void guardar(Cuenta cuenta) {

		em.getTransaction().begin();

		if (cuenta.getId() == null) {
			em.persist(cuenta);
		} else {
			em.merge(cuenta);
		}

		em.getTransaction().commit();
	}

	public void eliminar(Long id) {

		Cuenta cuenta = em.find(Cuenta.class, id);

		if (cuenta == null) {
			throw new RuntimeException("La cuenta no existe");
		}

		if (!cuenta.getMovimientos().isEmpty()) {
			throw new RuntimeException("No podés eliminar una cuenta con movimientos");
		}

		em.getTransaction().begin();

		em.remove(cuenta);

		em.getTransaction().commit();
	}

}