package lsi.ubu.tests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.excepciones.BorrarClienteException;
import lsi.ubu.servicios.Servicio;
import lsi.ubu.servicios.ServicioImpl;
import lsi.ubu.util.PoolDeConexiones;

public class Tests {

	/** Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Tests.class);

	public void ejecutarTests() throws SQLException {

		Servicio servicio = new ServicioImpl();

		PoolDeConexiones pool = PoolDeConexiones.getInstance();

		/* Comprueba el borrado de cliente inexistente */
		try {
			servicio.borrarCliente(3);
			LOGGER.info("Cliente inexistente MAL");
		} catch (SQLException e) {
			if (e.getErrorCode() == BorrarClienteException.CLIENTE_NO_EXIST) {
				LOGGER.info("Cliente inexistente OK");
			} else {
				LOGGER.info("Cliente inexistente MAL");
				LOGGER.error(e.getMessage());
			}
		}

		/* Comprueba el borrado de cliente con pedidos */
		try {
			servicio.borrarCliente(1);
			LOGGER.info("Cliente con pedidos MAL");
		} catch (SQLException e) {
			if (e.getErrorCode() == BorrarClienteException.CLIENTE_TIENE_PEDIDOS) {
				LOGGER.info("Cliente con pedidos OK");
			} else {
				LOGGER.info("Cliente con pedidos MAL");
				LOGGER.error(e.getMessage());
			}
		}

		/*
		 * Comprueba un borrado correcto, primero borra el cliente y luego verifica via
		 * SELECT
		 */
		try {
			servicio.borrarCliente(2);
		} catch (SQLException e) {
			LOGGER.info("Borrado correcto MAL");
			LOGGER.error(e.getMessage());
		}

		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			con = pool.getConnection();

			st = con.prepareStatement("SELECT * FROM clientes WHERE idCliente = ?");
			st.setInt(1, 2);

			rs = st.executeQuery();

			if (!rs.next()) {
				LOGGER.info("Borrado correcto OK");
			} else {
				LOGGER.info("Borrado correcto MAL");
			}
		} catch (SQLException e) {
			LOGGER.info("Borrado correcto MAL");
			LOGGER.error(e.getMessage());
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (con != null) {
				con.close();
			}
		}
	}
}
