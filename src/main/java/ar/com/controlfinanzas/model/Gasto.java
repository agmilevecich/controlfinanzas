package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "gastos")
public class Gasto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private LocalDate fecha;
	private String descripcion;
	private BigDecimal monto;
	private CategoriaGasto categoria;

	@ManyToOne(optional = false)
	@JoinColumn(name = "UsuarioID", nullable = false)
	private Usuario usuario;

	public Gasto() {
	}

	public Gasto(Integer id, LocalDate fecha, String descripcion, BigDecimal monto, CategoriaGasto categoria,
			Usuario usuario) {
		this.id = id;
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.categoria = categoria;
		this.usuario = usuario;
	}

	public Gasto(LocalDate fecha, String descripcion, BigDecimal monto, CategoriaGasto categoria, Usuario usuario) {
		super();
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = monto;
		this.categoria = categoria;
		this.usuario = usuario;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
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

	public CategoriaGasto getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaGasto categoria) {
		this.categoria = categoria;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}
