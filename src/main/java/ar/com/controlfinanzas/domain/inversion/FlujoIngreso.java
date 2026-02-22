package ar.com.controlfinanzas.domain.inversion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "flujo_ingreso")
public class FlujoIngreso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inversion_id", nullable = false)
	private Inversion inversion;

	@Column(nullable = false)
	private LocalDate fechaEsperada;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal montoEsperado;

	@Column(precision = 19, scale = 4)
	private BigDecimal montoReal;

	private LocalDate fechaCobro;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoFlujo estado;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoFlujo tipo;

	@Column(length = 10)
	private String moneda;

	@Column(nullable = false)
	private LocalDateTime creadoEn;

	public FlujoIngreso() {
		this.creadoEn = LocalDateTime.now();
		this.estado = EstadoFlujo.PROYECTADO;
	}

	public FlujoIngreso(Inversion inversion, LocalDate fechaEsperada, BigDecimal montoEsperado, TipoFlujo tipo,
			String moneda) {
		this();
		this.inversion = inversion;
		this.fechaEsperada = fechaEsperada;
		this.montoEsperado = montoEsperado;
		this.tipo = tipo;
		this.moneda = moneda;
	}

	public Long getId() {
		return id;
	}

	public Inversion getInversion() {
		return inversion;
	}

	public void setInversion(Inversion inversion) {
		this.inversion = inversion;
	}

	public LocalDate getFechaEsperada() {
		return fechaEsperada;
	}

	public void setFechaEsperada(LocalDate fechaEsperada) {
		this.fechaEsperada = fechaEsperada;
	}

	public BigDecimal getMontoEsperado() {
		return montoEsperado;
	}

	public void setMontoEsperado(BigDecimal montoEsperado) {
		this.montoEsperado = montoEsperado;
	}

	public BigDecimal getMontoReal() {
		return montoReal;
	}

	public void setMontoReal(BigDecimal montoReal) {
		this.montoReal = montoReal;
	}

	public LocalDate getFechaCobro() {
		return fechaCobro;
	}

	public void setFechaCobro(LocalDate fechaCobro) {
		this.fechaCobro = fechaCobro;
	}

	public EstadoFlujo getEstado() {
		return estado;
	}

	public void setEstado(EstadoFlujo estado) {
		this.estado = estado;
	}

	public TipoFlujo getTipo() {
		return tipo;
	}

	public void setTipo(TipoFlujo tipo) {
		this.tipo = tipo;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public LocalDateTime getCreadoEn() {
		return creadoEn;
	}

	public void marcarCobrado(BigDecimal montoReal, LocalDate fechaCobro) {
		this.montoReal = montoReal;
		this.fechaCobro = fechaCobro;
		this.estado = EstadoFlujo.COBRADO;
	}
}