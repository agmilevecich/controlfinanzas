package ar.com.controlfinanzas.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarjetas_credito")
public class TarjetaCredito {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String nombre;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal limite;

	@Column(name = "dia_cierre", nullable = false)
	private int diaCierre;

	@Column(name = "dia_vencimiento", nullable = false)
	private int diaVencimiento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cuenta_id", nullable = false)
	private Cuenta cuenta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	public TarjetaCredito() {
	}

	public TarjetaCredito(String nombre, BigDecimal limite, int diaCierre, int diaVencimiento, Cuenta cuenta,
			Usuario usuario) {
		this.nombre = nombre;
		this.limite = limite;
		this.diaCierre = diaCierre;
		this.diaVencimiento = diaVencimiento;
		this.cuenta = cuenta;
		this.usuario = usuario;
	}

	// GETTERS

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public BigDecimal getLimite() {
		return limite;
	}

	public int getDiaCierre() {
		return diaCierre;
	}

	public int getDiaVencimiento() {
		return diaVencimiento;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	// SETTERS

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setLimite(BigDecimal limite) {
		this.limite = limite;
	}

	public void setDiaCierre(int diaCierre) {
		this.diaCierre = diaCierre;
	}

	public void setDiaVencimiento(int diaVencimiento) {
		this.diaVencimiento = diaVencimiento;
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override
	public String toString() {
		return nombre + " - " + cuenta.getNombre();
	}
}