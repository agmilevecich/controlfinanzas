package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
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
@Table(name = "ingreso")
public class Ingreso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String descripcion;

	@Column(precision = 19, scale = 4, nullable = false)
	private BigDecimal monto;

	private LocalDate fecha;

	@Enumerated(EnumType.STRING)
	private CategoriaIngreso categoria;

	@ManyToOne(optional = false)
	@JoinColumn(name = "UsuarioID", nullable = false)
	private Usuario usuario;

	public Ingreso() {
	}

	public Ingreso(String descripcion, BigDecimal monto, LocalDate fecha, CategoriaIngreso categoria, Usuario usuario) {
		this.descripcion = descripcion;
		this.monto = monto;
		this.fecha = fecha;
		this.categoria = categoria;
		this.usuario = usuario;
	}

	public Long getId() {
		return id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public CategoriaIngreso getCategoria() {
		return this.categoria;
	}

	public void setcategoria(CategoriaIngreso categoria) {
		this.categoria = categoria;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}
