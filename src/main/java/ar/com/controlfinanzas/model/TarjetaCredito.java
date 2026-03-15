package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
	private Banco banco;

	@OneToMany(mappedBy = "tarjeta")
	private List<Movimiento> movimientos;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@OneToMany(mappedBy = "tarjeta")
	private List<CompraTarjeta> compras = new ArrayList<>();

	public TarjetaCredito() {
	}

	public TarjetaCredito(String nombre, BigDecimal limite, int diaCierre, int diaVencimiento, Banco banco,
			Usuario usuario) {
		this.nombre = nombre;
		this.limite = limite;
		this.diaCierre = diaCierre;
		this.diaVencimiento = diaVencimiento;
		this.banco = banco;
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

	public Banco getBanco() {
		return banco;
	}

	public List<Movimiento> getMovimientos() {
		return movimientos;
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

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public void setMovimientos(List<Movimiento> movimientos) {
		this.movimientos = movimientos;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public BigDecimal getDeuda() {
		return movimientos.stream().filter(Movimiento::isPendiente).map(Movimiento::getMonto).reduce(BigDecimal.ZERO,
				BigDecimal::add);
	}

	public List<CompraTarjeta> getCompras() {
		return compras;
	}

	@Override
	public String toString() {
		return nombre + " - " + banco.getNombre();
	}

}