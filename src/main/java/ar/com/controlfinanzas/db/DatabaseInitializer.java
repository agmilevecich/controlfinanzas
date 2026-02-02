package ar.com.controlfinanzas.db;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

	public static void inicializar() {
		try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {

			String sql = "CREATE TABLE IF NOT EXISTS inversion (id IDENTITY PRIMARY KEY,"
					+ "    tipo VARCHAR(50) NOT NULL, moneda VARCHAR(10) NOT NULL,"
					+ "    descripcion VARCHAR(255), capital_inicial DECIMAL(15,2),"
					+ "    rendimiento_esperado DECIMAL(5,2), fecha_inicio DATE, fecha_vencimiento DATE,"
					+ "    cantidad DECIMAL(15,4), precio_unitario DECIMAL(15,4), broker VARCHAR(100),"
					+ "    crypto_tipo VARCHAR(50));";

			stmt.execute(sql);

			stmt.execute("ALTER TABLE inversion ADD COLUMN IF NOT EXISTS cantidad DECIMAL(15,4)");
			stmt.execute("ALTER TABLE inversion ADD COLUMN IF NOT EXISTS precio_unitario DECIMAL(15,4)");
			stmt.execute("ALTER TABLE inversion ADD COLUMN IF NOT EXISTS broker VARCHAR(100)");
			stmt.execute("ALTER TABLE inversion ADD COLUMN IF NOT EXISTS crypto_tipo VARCHAR(50)");

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
