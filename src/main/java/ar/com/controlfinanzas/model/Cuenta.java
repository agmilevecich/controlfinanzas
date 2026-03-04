package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.domain.finanzas.TipoCuenta;
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
	private TipoCuenta tipo;

	@Enumerated(EnumType.STRING)
	private Moneda moneda;

	private double interesDiario;

	private LocalDate fechaInicio;

	@ManyToOne
	private Usuario usuario;

	@Column(nullable = false)
	private boolean activa = true;

	@OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Movimiento> movimientos = new ArrayList<>();

	protected Cuenta() {
	}

	public Cuenta(String nombre, TipoCuenta tipo, Moneda moneda, double interesDiario, LocalDate fechaInicio,
			Usuario usuario) {

		this.nombre = nombre;
		this.tipo = tipo;
		this.moneda = moneda;
		this.interesDiario = interesDiario;
		this.fechaInicio = fechaInicio;
		this.usuario = usuario;
	}

	@Transient
	public BigDecimal getSaldo() {
		BigDecimal total = BigDecimal.ZERO;

		for (Movimiento m : movimientos) {
			if (m.getMonto() == null) {
				continue;
			}

			switch (m.getTipo()) {
			case INGRESO -> total = total.add(m.getMonto());
			case GASTO, TRANSFERENCIA -> total = total.subtract(m.getMonto());
			}
		}

		return total;
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

	@Override
	public String toString() {
		return getNombre() + "Saldo Actual: " + getSaldo();
	}
}