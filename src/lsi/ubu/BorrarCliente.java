package lsi.ubu;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.tests.Tests;
import lsi.ubu.util.ExecuteScript;

/**
 * BorrarCliente: Implementa el el borrado de un cliente que se supone no tiene
 * pedidos asociados segun PDF de la carpeta enunciado
 * 
 * @author <a href="mailto:jmaudes@ubu.es">Jesus Maudes</a>
 * @author <a href="mailto:rmartico@ubu.es">Raul Marticorena</a>
 * @version 1.0
 * @since 1.0
 */
public class BorrarCliente {

	private static final Logger LOGGER = LoggerFactory.getLogger(BorrarCliente.class);

	public static void main(String[] args) throws SQLException {

		LOGGER.info("Comienzo de los tests");

		// Crear las tablas y filas en base de datos para la prueba
		ExecuteScript.run("sql/BorraCliente.sql");

		// Ejecutar los tests
		Tests tests = new Tests();
		tests.ejecutarTests();

		LOGGER.info("Fin de los tests");
	}
}
