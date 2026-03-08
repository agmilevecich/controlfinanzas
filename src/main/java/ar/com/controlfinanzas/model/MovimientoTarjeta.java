package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class MovimientoTarjeta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate fecha;

	private String descripcion;

	private BigDecimal monto;

	@ManyToOne
	@JoinColumn(name = "tarjeta_id")
	private TarjetaCredito tarjeta;

	public MovimientoTarjeta() {
	}

	public MovimientoTarjeta(LocalDate fecha, String descripcion, BigDecimal monto, TarjetaCredito tarjeta) {
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.tarjeta = tarjeta;
	}

	// getters y setters
}