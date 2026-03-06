package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.util.ArrayList;

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
import jakarta.persistence.Transient;

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

	@OneToMany(mappedBy = "tarjeta", fetch = FetchType.LAZY)
	private List<Movimiento> movimientos = new ArrayList<>();

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

	@Transient
	public BigDecimal getDeuda() {

		BigDecimal deuda = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {

			if (m.getFormaPago() == FormaPago.CREDITO && m.isPendiente()) {
				deuda = deuda.add(m.getMonto());
			}

		}

		return deuda;
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