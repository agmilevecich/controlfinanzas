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

	// Guardar una inversión en la base de datos
	public void guardarInversion(Inversion inv) throws Exception {
		String sql = "INSERT INTO inversion (tipo, moneda, descripcion, capital_inicial, rendimiento_esperado, fecha_inicio, fecha_vencimiento) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, inv.getTipo().name());
			stmt.setString(2, inv.getMoneda().name());
			stmt.setString(3, inv.getDescripcion());
			stmt.setBigDecimal(4, inv.getCapitalInicial());
			stmt.setBigDecimal(5, inv.getRendimientoEsperado());
			stmt.setDate(6, java.sql.Date.valueOf(inv.getFechaInicio()));
			stmt.setDate(7, java.sql.Date.valueOf(inv.getFechaVencimiento()));

			stmt.executeUpdate();
		}
	}

	// Listar todas las inversiones
	public List<Inversion> listarInversiones() throws Exception {
		List<Inversion> inversiones = new ArrayList<>();
		String sql = "SELECT * FROM inversion";

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Inversion inv = new Inversion();
				inv.setId(rs.getInt("id"));
				inv.setTipo(TipoInversion.valueOf(rs.getString("tipo")));
				inv.setMoneda(Moneda.valueOf(rs.getString("moneda")));
				inv.setDescripcion(rs.getString("descripcion"));
				inv.setCapitalInicial(rs.getBigDecimal("capital_inicial"));
				inv.setRendimientoEsperado(rs.getBigDecimal("rendimiento_esperado"));
				inv.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
				inv.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());

				inversiones.add(inv);
			}
		}

		return inversiones;
	}
}
