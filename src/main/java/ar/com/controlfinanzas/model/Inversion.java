package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Inversion {

	private int id;
	private TipoInversion tipo;
	private Moneda moneda;
	private String descripcion;
	private BigDecimal capitalInicial;
	private BigDecimal rendimientoEsperado;
	private LocalDate fechaInicio;
	private LocalDate fechaVencimiento;

	// 🔹 NUEVOS CAMPOS
	private BigDecimal cantidad;
	private BigDecimal precioUnitario;
	private String cryptoTipo;
	private String broker;

	public Inversion() {
	}

	public Inversion(TipoInversion tipo, Moneda moneda, String descripcion, BigDecimal capitalInicial,
			BigDecimal rendimientoEsperado, LocalDate fechaInicio, LocalDate fechaVencimiento) {
		this.tipo = tipo;
		this.moneda = moneda;
		this.descripcion = descripcion;
		this.capitalInicial = capitalInicial;
		this.rendimientoEsperado = rendimientoEsperado;
		this.fechaInicio = fechaInicio;
		this.fechaVencimiento = fechaVencimiento;
	}

	// GETTERS Y SETTERS EXISTENTES

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public TipoInversion getTipo() {
		return tipo;
	}

	public void setTipo(TipoInversion tipo) {
		this.tipo = tipo;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getCapitalInicial() {
		return capitalInicial;
	}

	public void setCapitalInicial(BigDecimal capitalInicial) {
		this.capitalInicial = capitalInicial;
	}

	public BigDecimal getRendimientoEsperado() {
		return rendimientoEsperado;
	}

	public void setRendimientoEsperado(BigDecimal rendimientoEsperado) {
		this.rendimientoEsperado = rendimientoEsperado;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(LocalDate fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	// 🔹 GETTERS Y SETTERS NUEVOS
	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public String getCryptoTipo() {
		return cryptoTipo;
	}

	public void setCryptoTipo(String cryptoTipo) {
		this.cryptoTipo = cryptoTipo;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public Comparable getNombre() {
		return getDescripcion();
	}

	public boolean tieneVencimiento() {
		return fechaVencimiento != null;
	}

}
