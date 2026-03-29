package ar.com.controlfinanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "movimientos")
public class Movimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate fecha;

	@Column(name = "descripcion", length = 150, nullable = false)
	private String descripcion;

	@Column(precision = 19, scale = 4)
	private BigDecimal monto;

	@Enumerated(EnumType.STRING)
	private TipoMovimiento tipo;

	@Enumerated(EnumType.STRING)
	private FormaPago formaPago;

	private String compraId;
	private String transferenciaId;
	private int numeroCuotas;
	private int totalCuotas;
	private int cuotasPendientes;
	private BigDecimal interes;
	private String periodo;

	private BigDecimal montoPagado = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	private CategoriaGasto categoria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cuenta_id")
	private Cuenta cuenta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tarjeta_id")
	private TarjetaCredito tarjeta;

	@Column(nullable = false)
	private boolean pendiente = false;

	@ManyToOne
	private Usuario usuario;

	// 🔹 Constructor vacío obligatorio para Hibernate
	protected Movimiento() {
	}

	public Movimiento(LocalDate fecha, String descripcion, BigDecimal monto, TipoMovimiento tipo) {
		this.fecha = fecha;
		this.descripcion = descripcion;
		this.monto = (monto != null) ? monto : BigDecimal.ZERO;
		this.tipo = tipo;
	}

	// ---------------- Getters y Setters ----------------

	public Long getId() {
		return id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public TipoMovimiento getTipo() {
		return tipo;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public FormaPago getFormaPago() {
		return formaPago;
	}

	public TarjetaCredito getTarjeta() {
		return tarjeta;
	}

	public boolean isPendiente() {
		return pendiente;
	}

	public int getCuotasPendientes() {
		return cuotasPendientes;
	}

	public BigDecimal getInteres() {
		return interes;
	}

	public String getCompraId() {
		return compraId;
	}

	public String getTransferenciaId() {
		return transferenciaId;
	}

	public int getNumeroCuotas() {
		return numeroCuotas;
	}

	public int getTotalCuotas() {
		return totalCuotas;
	}

	public String getPeriodo() {
		return periodo;
	}

	public BigDecimal getMontoPagado() {
		return montoPagado;
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

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = (monto != null) ? monto : BigDecimal.ZERO;
	}

	public void setTipo(TipoMovimiento tipo) {
		this.tipo = tipo;
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}

	public void setFormaPago(FormaPago formaPago) {
		this.formaPago = formaPago;
	}

	public void setTarjeta(TarjetaCredito tarjeta) {
		this.tarjeta = tarjeta;
	}

	public void setPendiente(boolean pendiente) {
		this.pendiente = pendiente;
	}

	public void setCuotasPendientes(int cuotasPendientes) {
		this.cuotasPendientes = cuotasPendientes;
	}

	public void setInteres(BigDecimal interes) {
		this.interes = interes;
	}

	public void setCompraId(String compraId) {
		this.compraId = compraId;
	}

	public void setTransferenciaId(String transferencia) {
		this.transferenciaId = transferencia;
	}

	public void setNumeroCuotas(int numeroCuotas) {
		this.numeroCuotas = numeroCuotas;
	}

	public void setTotalCuotas(int totalCuotas) {
		this.totalCuotas = totalCuotas;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public void setMontoPagado(BigDecimal montoPagado) {
		this.montoPagado = (montoPagado != null) ? montoPagado : BigDecimal.ZERO;
	}

	public void setCategoria(CategoriaGasto categoria) {
		this.categoria = categoria;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public BigDecimal getRestante() {
		BigDecimal restante = monto.subtract(montoPagado);
		return restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante;
	}

	// Validación básica
	public void validar() {
		if (tipo == TipoMovimiento.GASTO && categoria == null) {
			throw new IllegalStateException("Los gastos deben tener categoría");
		}

		if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalStateException("El monto debe ser mayor que 0");
		}

		if (tipo == TipoMovimiento.GASTO && monto.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalStateException("Los gastos no pueden tener monto negativo");
		}

		if (formaPago == FormaPago.CREDITO) {
			if (tarjeta == null) {
				throw new IllegalStateException("Los movimientos con tarjeta deben tener tarjeta");
			}
			if (cuenta != null) {
				throw new IllegalStateException("Compras con tarjeta no deben tener cuenta");
			}
		} else {
			if (tarjeta != null) {
				throw new IllegalStateException("Solo CREDITO puede tener tarjeta");
			}
			if (cuenta == null) {
				throw new IllegalStateException("Movimientos no CREDITO deben tener cuenta");
			}
		}
	}
}