package ar.com.controlfinanzas.util;

import ar.com.controlfinanzas.model.Cuenta;
import ar.com.controlfinanzas.model.FormaPago;
import ar.com.controlfinanzas.model.TarjetaCredito;

public class GastoContexto {
	private Cuenta cuenta;
	private TarjetaCredito tarjeta;
	private FormaPago formaPago;

	public static GastoContexto desdeCuenta(Cuenta cuenta) {
		GastoContexto ctx = new GastoContexto();
		ctx.cuenta = cuenta;
		ctx.formaPago = FormaPago.DEBITO;
		return ctx;
	}

	// futuros casos
	public static GastoContexto desdeTarjeta(TarjetaCredito tarjeta) {
		GastoContexto ctx = new GastoContexto();
		ctx.tarjeta = tarjeta;
		ctx.formaPago = FormaPago.CREDITO;
		return ctx;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public TarjetaCredito getTarjeta() {
		return tarjeta;
	}

	public FormaPago getFormaPago() {
		return formaPago;
	}

	// getters

}