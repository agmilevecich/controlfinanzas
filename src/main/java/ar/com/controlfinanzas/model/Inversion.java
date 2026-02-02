package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Inversion {

	private Long id;

	private TipoInversion tipo;
	private Moneda moneda;
	private String descripcion;

	private BigDecimal capitalInicial;
	private BigDecimal rendimientoEsperado;

	private LocalDate fechaInicio;
	private LocalDate fechaVencimiento;

	// Campos opcionales / avanzados
	private BigDecimal cantidad;
	private BigDecimal precioUnitario;
	private String broker;
	private String cryptoTipo;

	/*
	 * ====================== CONSTRUCTORES ======================
	 */

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

		this.cantidad = BigDecimal.ZERO;
		this.precioUnitario = BigDecimal.ZERO;
	}

	/*
	 * ====================== MÉTODOS DE NEGOCIO ======================
	 */

	public boolean tieneVencimiento() {
		return fechaVencimiento != null;
	}

	/**
	 * Usado por gráficos y tooltips
	 */
	public String getNombre() {
		return descripcion;
	}

	public BigDecimal getCapitalTotalCalculado() {
		if (cantidad != null && precioUnitario != null && cantidad.compareTo(BigDecimal.ZERO) > 0
				&& precioUnitario.compareTo(BigDecimal.ZERO) > 0) {
			return cantidad.multiply(precioUnitario);
		}
		return capitalInicial;
	}

	/*
	 * ====================== GETTERS & SETTERS ======================
	 */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public String getCryptoTipo() {
		return cryptoTipo;
	}

	public void setCryptoTipo(String cryptoTipo) {
		this.cryptoTipo = cryptoTipo;
	}
}
