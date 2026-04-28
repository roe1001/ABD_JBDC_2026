package lsi.ubu;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lsi.ubu.tests.Tests;
import lsi.ubu.tests.TestGestionDonaciones;
import lsi.ubu.util.ExecuteScript;

public class BorrarCliente {

	private static final Logger LOGGER = LoggerFactory.getLogger(BorrarCliente.class);

	public static void main(String[] args) throws SQLException {

		LOGGER.info("Comienzo de los tests");
		
		// Crear las tablas y filas en base de datos para la prueba [cite: 14]
		ExecuteScript.run("sql/BorraCliente.sql");
		ExecuteScript.run("sql/Gestion_Donaciones_Sangre.sql");
		
		// Ejecutar los tests
		Tests tests = new Tests();
		tests.ejecutarTests();
		TestGestionDonaciones.ejecutarTests();

		LOGGER.info("Fin de los tests");
	}
}