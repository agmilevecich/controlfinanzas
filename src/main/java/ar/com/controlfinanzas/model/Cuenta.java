package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.util.NumeroUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "cuentas")
public class Cuenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;

	@Enumerated(EnumType.STRING)
	private TipoCuenta tipoCuenta;

	@Enumerated(EnumType.STRING)
	private Moneda moneda;

	private double interesDiario;

	private LocalDate fechaInicio;

	@ManyToOne
	private Banco banco;

	@ManyToOne
	private Usuario usuario;

	@Column(nullable = false)
	private boolean activa = true;

	@OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Movimiento> movimientos = new ArrayList<>();

	public Cuenta() {
	}

	public Cuenta(String nombre, Banco banco, TipoCuenta tipo, Moneda moneda, double interesDiario,
			LocalDate fechaInicio, Usuario usuario) {

		this.nombre = nombre;
		this.banco = banco;
		this.tipoCuenta = tipo;
		this.moneda = moneda;
		this.interesDiario = interesDiario;
		this.fechaInicio = fechaInicio;
		this.usuario = usuario;
	}

	@Transient
	public BigDecimal getSaldo() {
		BigDecimal saldo = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			// si fue pagado con tarjeta no afecta la cuenta
			if (m.getFormaPago() == FormaPago.CREDITO) {
				continue;
			}

			if (m.getTipo() == TipoMovimiento.INGRESO) {
				saldo = saldo.add(m.getMonto());
			} else {
				saldo = saldo.subtract(m.getMonto());
			}
		}

		return saldo;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public List<Movimiento> getMovimientos() {
		return movimientos;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public double getInteresDiario() {
		return interesDiario;
	}

	public boolean isActiva() {
		return activa;
	}

	public void desactivar() {
		this.activa = false;
	}

	public boolean estaActiva(LocalDate fecha) {
		return !fecha.isBefore(fechaInicio);
	}

	public TipoCuenta getTipoCuenta() {
		return tipoCuenta;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setTipoCuenta(TipoCuenta tipoCuenta) {
		this.tipoCuenta = tipoCuenta;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public void setInteresDiario(double interesDiario) {
		this.interesDiario = interesDiario;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}

	public void setMovimientos(List<Movimiento> movimientos) {
		this.movimientos = movimientos;
	}

	public void actualizarDatos(String nombre, TipoCuenta tipoCuenta, Moneda moneda, Banco banco) {
		this.nombre = nombre;
		this.tipoCuenta = tipoCuenta;
		this.moneda = moneda;
		this.banco = banco;
	}

	public boolean puedeCambiarMoneda(Moneda nuevaMoneda) {
		if (this.moneda.equals(nuevaMoneda)) {
			return true;
		}
		return getSaldo().compareTo(BigDecimal.ZERO) == 0;
	}

	@Override
	public String toString() {
		return String.format("%s | %s | %s | Saldo: %s", banco.getNombre(), nombre, moneda,
				NumeroUtils.formatearMonedaARS(getSaldo()));
	}
}