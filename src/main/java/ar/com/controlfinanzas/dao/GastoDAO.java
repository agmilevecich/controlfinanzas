package ar.com.controlfinanzas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.com.controlfinanzas.db.DatabaseManager;
import ar.com.controlfinanzas.model.Gasto;

public class GastoDAO {

	public void guardarGasto(Gasto gasto) throws Exception {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"INSERT INTO gastos (fecha, descripcion, monto, categoria) VALUES (?, ?, ?, ?)")) {
			ps.setDate(1, java.sql.Date.valueOf(gasto.getFecha()));
			ps.setString(2, gasto.getDescripcion());
			ps.setDouble(3, gasto.getMonto());
			ps.setString(4, gasto.getCategoria());
			ps.executeUpdate();
		}
	}

	public List<Gasto> listarGastos() throws Exception {
		List<Gasto> lista = new ArrayList<>();
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM gastos ORDER BY fecha DESC");
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				int id = rs.getInt("id");
				LocalDate fecha = rs.getDate("fecha").toLocalDate();
				String descripcion = rs.getString("descripcion");
				double monto = rs.getDouble("monto");
				String categoria = rs.getString("categoria");

				lista.add(new Gasto(id, fecha, descripcion, monto, categoria));
			}
		}
		return lista;
	}
}
