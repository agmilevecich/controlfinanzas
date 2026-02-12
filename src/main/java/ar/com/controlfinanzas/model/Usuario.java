package ar.com.controlfinanzas.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "UsuarioID")
	private Integer usuarioID;

	@Column(name = "Nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name = "Email", length = 100)
	private String email;

	@Column(name = "Telefono", length = 20)
	private String telefono;

	@Column(name = "FechaNacimiento")
	private LocalDate fechaNacimiento;

	@Column(name = "DocumentoIdentidad", length = 50)
	private String documentoIdentidad;

	@Column(name = "Direccion", length = 200)
	private String direccion;

	// Constructor vacío obligatorio para JPA
	public Usuario() {
	}

	// Constructor útil
	public Usuario(String nombre) {
		this.nombre = nombre;
	}

	// Getters y setters
	public Integer getUsuarioID() {
		return usuarioID;
	}

	public void setUsuarioID(Integer usuarioID) {
		this.usuarioID = usuarioID;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getDocumentoIdentidad() {
		return documentoIdentidad;
	}

	public void setDocumentoIdentidad(String documentoIdentidad) {
		this.documentoIdentidad = documentoIdentidad;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
}
