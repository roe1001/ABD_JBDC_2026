package lsi.ubu.servicios;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.excepciones.BorrarClienteException;
import lsi.ubu.util.PoolDeConexiones;
import lsi.ubu.util.exceptions.SGBDError;
import lsi.ubu.util.exceptions.oracle.OracleSGBDErrorUtil;

public class ServicioImpl implements Servicio {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicioImpl.class);

    public void borrarCliente(int nroDelCliente) throws SQLException {
        PoolDeConexiones pool = PoolDeConexiones.getInstance();
        Connection con = null;
        CallableStatement cs = null;

        try {
            con = pool.getConnection();
            cs = con.prepareCall("{call borrarCliente(?)}");
            cs.setInt(1, nroDelCliente);
            cs.execute();

        } catch (SQLException e) {
            // Error 20001: cliente no existe
            if (e.getErrorCode() == BorrarClienteException.CLIENTE_NO_EXIST) {
                throw new BorrarClienteException(BorrarClienteException.CLIENTE_NO_EXIST);
            }
            // Error 20002: viene del RAISE_APPLICATION_ERROR del procedimiento (tiene pedidos)
            if (e.getErrorCode() == BorrarClienteException.CLIENTE_TIENE_PEDIDOS) {
                throw new BorrarClienteException(BorrarClienteException.CLIENTE_TIENE_PEDIDOS);
            }
            // Error ORA-02292: FK directa (por si acaso no la captura el procedimiento)
            OracleSGBDErrorUtil oracleUtil = new OracleSGBDErrorUtil();
            if (oracleUtil.checkExceptionToCode(e, SGBDError.FK_VIOLATED_DELETE)) {
                throw new BorrarClienteException(BorrarClienteException.CLIENTE_TIENE_PEDIDOS);
            }

            // Cualquier otro error inesperado: rollback y se propaga
            LOGGER.error("Error inesperado. Codigo: {} Mensaje: {}", e.getErrorCode(), e.getMessage());
            if (con != null) con.rollback();
            throw e;

        } finally {
            if (cs != null) cs.close();
            if (con != null) con.close();
        }
    }
}