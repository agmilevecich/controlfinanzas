package ar.com.controlfinanzas.db;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

	public static void inicializar() {
		try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {

			String sql = "CREATE TABLE IF NOT EXISTS inversion (" + "id IDENTITY PRIMARY KEY," + "tipo VARCHAR(50),"
					+ "moneda VARCHAR(10)," + "descripcion VARCHAR(255)," + "capital_inicial DECIMAL(15,2),"
					+ "rendimiento_esperado DECIMAL(5,2)," + "fecha_inicio DATE," + "fecha_vencimiento DATE" + ")";

			stmt.execute(sql);

			String sqlGastos = "CREATE TABLE IF NOT EXISTS gastos (" + "id IDENTITY PRIMARY KEY,"
					+ "fecha DATE NOT NULL," + "descripcion VARCHAR(255)," + "monto DOUBLE NOT NULL,"
					+ "categoria VARCHAR(100)" + ")";

			stmt.execute(sqlGastos);

			System.out.println("Tabla inversion verificada/creada correctamente");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
