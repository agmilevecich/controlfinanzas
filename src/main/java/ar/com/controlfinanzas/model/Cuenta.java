package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas")
public class Cuenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;
	private BigDecimal capitalInicial;
	private double interesDiario; // porcentaje diario, ej: 0.001 = 0.1%
	private LocalDate fechaInicio;

	private boolean activa;

	@Enumerated(EnumType.STRING)
	private TipoCuenta tipo;

	@Enumerated(EnumType.STRING)
	private Moneda moneda;

	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	// Constructor vacío requerido por JPA
	public Cuenta() {
	}

	// Constructor completo
	public Cuenta(String nombre, BigDecimal capitalInicial, double interesDiario, LocalDate fechaInicio,
			TipoCuenta tipo, Moneda moneda, Usuario usuario) {
		this.nombre = nombre;
		this.capitalInicial = capitalInicial;
		this.interesDiario = interesDiario;
		this.fechaInicio = fechaInicio;
		this.tipo = tipo;
		this.moneda = moneda;
		this.usuario = usuario;
		this.activa = true;
	}

	// Getters y setters
	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public BigDecimal getCapitalInicial() {
		return capitalInicial;
	}

	public double getInteresDiario() {
		return interesDiario;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public boolean isActiva() {
		return activa;
	}

	public TipoCuenta getTipo() {
		return tipo;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setCapitalInicial(BigDecimal capitalInicial) {
		this.capitalInicial = capitalInicial;
	}

	public void setInteresDiario(double interesDiario) {
		this.interesDiario = interesDiario;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public void setTipo(TipoCuenta tipo) {
		this.tipo = tipo;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void desactivar() {
		this.activa = false;
	}

	// Método de negocio
	public boolean estaActiva(LocalDate fecha) {
		return !fecha.isBefore(fechaInicio) && activa;
	}

	@Override
	public String toString() {
		return nombre + " - " + moneda + "      Saldo Actual: " + getCapitalInicial();
	}
}