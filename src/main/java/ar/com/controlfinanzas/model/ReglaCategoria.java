package ar.com.controlfinanzas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ReglaCategoria {

	@Id
	@GeneratedValue
	private Long id;

	private String palabraClave;

	@Enumerated(EnumType.STRING)
	private CategoriaGasto categoria;

	@ManyToOne
	private Usuario usuario;

	public Long getId() {
		return id;
	}

	public String getPalabraClave() {
		return palabraClave;
	}

	public CategoriaGasto getCategoria() {
		return categoria;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPalabraClave(String palabraClave) {
		this.palabraClave = palabraClave;
	}

	public void setCategoria(CategoriaGasto categoria) {
		this.categoria = categoria;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}