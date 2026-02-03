package ar.com.controlfinanzas.model;

import java.time.LocalDate;

public class Alerta {

	public enum TipoAlerta {
		VENCIMIENTO
	}

	// NUEVO: nivel de urgencia
	public enum Nivel {
		INFO, PROXIMA, CRITICA, HOY
	}

	private String titulo;
	private String mensaje;
	private LocalDate fecha;
	private TipoAlerta tipo;
	private Nivel nivel;

	// Constructor EXISTENTE (para no romper nada)
	public Alerta(String titulo, String mensaje, LocalDate fecha, TipoAlerta tipo) {
		this.titulo = titulo;
		this.mensaje = mensaje;
		this.fecha = fecha;
		this.tipo = tipo;
		this.nivel = Nivel.INFO; // valor por defecto
	}

	// Constructor NUEVO (con nivel)
	public Alerta(String titulo, String mensaje, LocalDate fecha, TipoAlerta tipo, Nivel nivel) {
		this.titulo = titulo;
		this.mensaje = mensaje;
		this.fecha = fecha;
		this.tipo = tipo;
		this.nivel = nivel;
	}

	// Getters
	public String getTitulo() {
		return titulo;
	}

	public String getMensaje() {
		return mensaje;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public TipoAlerta getTipo() {
		return tipo;
	}

	public Nivel getNivel() {
		return nivel;
	}
}
