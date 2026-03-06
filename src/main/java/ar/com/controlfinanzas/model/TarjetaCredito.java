package ar.com.controlfinanzas.model;

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

	@Column(nullable = false)
	private int diaCierre;

	@Column(nullable = false)
	private int diaVencimiento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	protected TarjetaCredito() {
	}

	public TarjetaCredito(String nombre, int diaCierre, int diaVencimiento, Usuario usuario) {
		this.nombre = nombre;
		this.diaCierre = diaCierre;
		this.diaVencimiento = diaVencimiento;
		this.usuario = usuario;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public int getDiaCierre() {
		return diaCierre;
	}

	public int getDiaVencimiento() {
		return diaVencimiento;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setDiaCierre(int diaCierre) {
		this.diaCierre = diaCierre;
	}

	public void setDiaVencimiento(int diaVencimiento) {
		this.diaVencimiento = diaVencimiento;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override
	public String toString() {
		return nombre + " (Cierre: " + diaCierre + " / Vto: " + diaVencimiento + ")";
	}
}