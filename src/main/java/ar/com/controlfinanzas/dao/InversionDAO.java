package ar.com.controlfinanzas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.db.DatabaseManager;
import ar.com.controlfinanzas.model.Inversion;
import ar.com.controlfinanzas.model.Moneda;
import ar.com.controlfinanzas.model.TipoInversion;

public class InversionDAO {

	public void guardarInversion(Inversion inv) throws Exception {

		String sql = """
				INSERT INTO inversion
				(tipo, moneda, descripcion, capital_inicial, rendimiento_esperado,
				 fecha_inicio, fecha_vencimiento,
				 cantidad, precio_unitario, broker, crypto_tipo)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, inv.getTipo().name());
			ps.setString(2, inv.getMoneda().name());
			ps.setString(3, inv.getDescripcion());
			ps.setBigDecimal(4, inv.getCapitalInicial());
			ps.setBigDecimal(5, inv.getRendimientoEsperado());
			ps.setDate(6, java.sql.Date.valueOf(inv.getFechaInicio()));
			ps.setDate(7, java.sql.Date.valueOf(inv.getFechaVencimiento()));

			ps.setBigDecimal(8, inv.getCantidad());
			ps.setBigDecimal(9, inv.getPrecioUnitario());
			ps.setString(10, inv.getBroker());
			ps.setString(11, inv.getCryptoTipo());

			ps.executeUpdate();
		}
	}

	public List<Inversion> listarInversiones() throws Exception {

		List<Inversion> lista = new ArrayList<>();

		String sql = "SELECT * FROM inversion";

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				Inversion inv = new Inversion(TipoInversion.valueOf(rs.getString("tipo")),
						Moneda.valueOf(rs.getString("moneda")), rs.getString("descripcion"),
						rs.getBigDecimal("capital_inicial"), rs.getBigDecimal("rendimiento_esperado"),
						rs.getDate("fecha_inicio").toLocalDate(), rs.getDate("fecha_vencimiento").toLocalDate());

				inv.setId(rs.getLong("id"));
				inv.setCantidad(rs.getBigDecimal("cantidad"));
				inv.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
				inv.setBroker(rs.getString("broker"));
				inv.setCryptoTipo(rs.getString("crypto_tipo"));

				lista.add(inv);
			}
		}

		return lista;
	}
}
