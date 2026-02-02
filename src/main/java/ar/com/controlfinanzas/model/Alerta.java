package ar.com.controlfinanzas.model;

import java.time.LocalDate;

public class Alerta {

	private String titulo;
	private String descripcion;
	private LocalDate fechaEvento;
	private TipoAlerta tipo;

	public enum TipoAlerta {
		VENCIMIENTO, CUPON, RECORDATORIO
	}

	public Alerta(String titulo, String descripcion, LocalDate fechaEvento, TipoAlerta tipo) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.fechaEvento = fechaEvento;
		this.tipo = tipo;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public LocalDate getFechaEvento() {
		return fechaEvento;
	}

	public TipoAlerta getTipo() {
		return tipo;
	}
}
