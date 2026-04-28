package lsi.ubu.solucion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.excepciones.GestionDonacionesSangreException;
import lsi.ubu.tests.TestGestionDonaciones;
import lsi.ubu.util.PoolDeConexiones;

public class GestionDonacionesSangre {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionDonacionesSangre.class);

    public static void realizar_donacion(String m_NIF, float m_Cantidad,
            int m_ID_Hospital, Date m_Fecha_Donacion) throws SQLException {

        PoolDeConexiones pool = PoolDeConexiones.getInstance();
        Connection con = null;
        PreparedStatement psDonante    = null;
        PreparedStatement psHospital   = null;
        PreparedStatement psUltimaDon  = null;
        PreparedStatement psInsert     = null;
        PreparedStatement psReserva    = null;
        ResultSet rsDonante   = null;
        ResultSet rsHospital  = null;
        ResultSet rsUltimaDon = null;

        try {
            con = pool.getConnection();

            // Validar cantidad
            if (m_Cantidad <= 0 || m_Cantidad > 0.45f) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.CANTIDAD_DONACION_INCORRECTA);
            }

            // Comprobar que el donante existe y obtener su tipo de sangre
            psDonante = con.prepareStatement(
                    "SELECT id_tipo_sangre FROM donante WHERE NIF = ?");
            psDonante.setString(1, m_NIF);
            rsDonante = psDonante.executeQuery();
            if (!rsDonante.next()) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.DONANTE_INEXISTENTE);
            }
            int idTipoSangre = rsDonante.getInt(1);

            // Comprobar que el hospital existe
            psHospital = con.prepareStatement(
                    "SELECT COUNT(*) FROM hospital WHERE id_hospital = ?");
            psHospital.setInt(1, m_ID_Hospital);
            rsHospital = psHospital.executeQuery();
            rsHospital.next();
            if (rsHospital.getInt(1) == 0) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.HOSPITAL_INEXISTENTE);
            }

            // Comprobar que el donante no ha donado en los últimos 15 días
            psUltimaDon = con.prepareStatement(
                    "SELECT MAX(fecha_donacion) FROM donacion WHERE nif_donante = ?");
            psUltimaDon.setString(1, m_NIF);
            rsUltimaDon = psUltimaDon.executeQuery();
            if (rsUltimaDon.next()) {
                Date ultimaDonacion = rsUltimaDon.getDate(1);
                if (ultimaDonacion != null) {
                    long diasDiferencia = (m_Fecha_Donacion.getTime() - ultimaDonacion.getTime())
                            / (1000L * 60 * 60 * 24);
                    if (diasDiferencia < 15) {
                        throw new GestionDonacionesSangreException(
                                GestionDonacionesSangreException.DONANTE_EXCEDE_CUPO);
                    }
                }
            }

            // Insertar donación
            psInsert = con.prepareStatement(
                    "INSERT INTO donacion VALUES (sec_donacion.nextval, ?, ?, ?)");
            psInsert.setString(1, m_NIF);
            psInsert.setFloat(2, m_Cantidad);
            psInsert.setDate(3, m_Fecha_Donacion);
            psInsert.executeUpdate();

            // Incrementar reserva del hospital según el tipo de sangre del donante
            psReserva = con.prepareStatement(
                    "UPDATE reserva_hospital SET cantidad = cantidad + ? "
                  + "WHERE id_tipo_sangre = ? AND id_hospital = ?");
            psReserva.setFloat(1, m_Cantidad);
            psReserva.setInt(2, idTipoSangre);
            psReserva.setInt(3, m_ID_Hospital);
            psReserva.executeUpdate();

            con.commit();

        } catch (GestionDonacionesSangreException e) {
            if (con != null) con.rollback();
            throw e;
        } catch (SQLException e) {
            LOGGER.error("SQLException en realizar_donacion. Codigo: {} Mensaje: {}",
                    e.getErrorCode(), e.getMessage());
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (rsDonante   != null) rsDonante.close();
            if (rsHospital  != null) rsHospital.close();
            if (rsUltimaDon != null) rsUltimaDon.close();
            if (psDonante   != null) psDonante.close();
            if (psHospital  != null) psHospital.close();
            if (psUltimaDon != null) psUltimaDon.close();
            if (psInsert    != null) psInsert.close();
            if (psReserva   != null) psReserva.close();
            if (con         != null) con.close();
        }
    }

    public static void anular_traspaso(int m_ID_Tipo_Sangre, int m_ID_Hospital_Origen,
            int m_ID_Hospital_Destino, Date m_Fecha_Traspaso) throws SQLException {

        PoolDeConexiones pool = PoolDeConexiones.getInstance();
        Connection con       = null;
        PreparedStatement psTipoSangre  = null;
        PreparedStatement psHospOrigen  = null;
        PreparedStatement psHospDestino = null;
        PreparedStatement psSelect      = null;
        PreparedStatement psDelete      = null;
        PreparedStatement psRestaDest   = null;
        PreparedStatement psSumaOrigen  = null;
        ResultSet rsTipoSangre  = null;
        ResultSet rsHospOrigen  = null;
        ResultSet rsHospDestino = null;
        ResultSet rsTraspaso    = null;

        try {
            con = pool.getConnection();

            // Validar que el tipo de sangre existe
            psTipoSangre = con.prepareStatement(
                    "SELECT COUNT(*) FROM tipo_sangre WHERE id_tipo_sangre = ?");
            psTipoSangre.setInt(1, m_ID_Tipo_Sangre);
            rsTipoSangre = psTipoSangre.executeQuery();
            rsTipoSangre.next();
            if (rsTipoSangre.getInt(1) == 0) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.TIPO_SANGRE_INEXISTENTE);
            }

            // Validar hospital origen
            psHospOrigen = con.prepareStatement(
                    "SELECT COUNT(*) FROM hospital WHERE id_hospital = ?");
            psHospOrigen.setInt(1, m_ID_Hospital_Origen);
            rsHospOrigen = psHospOrigen.executeQuery();
            rsHospOrigen.next();
            if (rsHospOrigen.getInt(1) == 0) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.HOSPITAL_INEXISTENTE);
            }

            // Validar hospital destino
            psHospDestino = con.prepareStatement(
                    "SELECT COUNT(*) FROM hospital WHERE id_hospital = ?");
            psHospDestino.setInt(1, m_ID_Hospital_Destino);
            rsHospDestino = psHospDestino.executeQuery();
            rsHospDestino.next();
            if (rsHospDestino.getInt(1) == 0) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.HOSPITAL_INEXISTENTE);
            }

            // Obtener los traspasos que coinciden con los parámetros.
            // La tabla traspaso registra el hospital DESTINO en id_hospital.
            // El origen se identifica comparando reservas: se buscan traspasos
            // del tipo y fecha indicados hacia el hospital destino.
            // Nota: el esquema solo guarda id_hospital (destino) en traspaso,
            // por lo que filtramos por destino, tipo y fecha y procesamos cada fila.
            psSelect = con.prepareStatement(
                    "SELECT id_traspaso, cantidad FROM traspaso "
                  + "WHERE id_tipo_sangre = ? AND id_hospital = ? AND fecha_traspaso = ?");
            psSelect.setInt(1, m_ID_Tipo_Sangre);
            psSelect.setInt(2, m_ID_Hospital_Destino);
            psSelect.setDate(3, m_Fecha_Traspaso);
            rsTraspaso = psSelect.executeQuery();

            psDelete     = con.prepareStatement("DELETE FROM traspaso WHERE id_traspaso = ?");
            psRestaDest  = con.prepareStatement(
                    "UPDATE reserva_hospital SET cantidad = cantidad - ? "
                  + "WHERE id_tipo_sangre = ? AND id_hospital = ?");
            psSumaOrigen = con.prepareStatement(
                    "UPDATE reserva_hospital SET cantidad = cantidad + ? "
                  + "WHERE id_tipo_sangre = ? AND id_hospital = ?");

            while (rsTraspaso.next()) {
                int   idTraspaso = rsTraspaso.getInt(1);
                float cantidad   = rsTraspaso.getFloat(2);

                if (cantidad < 0) {
                    throw new GestionDonacionesSangreException(
                            GestionDonacionesSangreException.CANTIDAD_TRASPASO_INCORRECTA);
                }

                // Restar al hospital destino
                psRestaDest.setFloat(1, cantidad);
                psRestaDest.setInt(2, m_ID_Tipo_Sangre);
                psRestaDest.setInt(3, m_ID_Hospital_Destino);
                psRestaDest.executeUpdate();

                // Sumar al hospital origen
                psSumaOrigen.setFloat(1, cantidad);
                psSumaOrigen.setInt(2, m_ID_Tipo_Sangre);
                psSumaOrigen.setInt(3, m_ID_Hospital_Origen);
                psSumaOrigen.executeUpdate();

                // Borrar el traspaso
                psDelete.setInt(1, idTraspaso);
                psDelete.executeUpdate();
            }

            con.commit();

        } catch (GestionDonacionesSangreException e) {
            if (con != null) con.rollback();
            throw e;
        } catch (SQLException e) {
            LOGGER.error("SQLException en anular_traspaso. Codigo: {} Mensaje: {}",
                    e.getErrorCode(), e.getMessage());
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (rsTipoSangre  != null) rsTipoSangre.close();
            if (rsHospOrigen  != null) rsHospOrigen.close();
            if (rsHospDestino != null) rsHospDestino.close();
            if (rsTraspaso    != null) rsTraspaso.close();
            if (psTipoSangre  != null) psTipoSangre.close();
            if (psHospOrigen  != null) psHospOrigen.close();
            if (psHospDestino != null) psHospDestino.close();
            if (psSelect      != null) psSelect.close();
            if (psDelete      != null) psDelete.close();
            if (psRestaDest   != null) psRestaDest.close();
            if (psSumaOrigen  != null) psSumaOrigen.close();
            if (con           != null) con.close();
        }
    }

    public static void consulta_traspasos(String m_Tipo_Sangre) throws SQLException {

        PoolDeConexiones pool = PoolDeConexiones.getInstance();
        Connection con  = null;
        PreparedStatement psTipo     = null;
        PreparedStatement psConsulta = null;
        ResultSet rsTipo     = null;
        ResultSet rsConsulta = null;

        try {
            con = pool.getConnection();

            // Validar que el tipo de sangre existe por descripción
            psTipo = con.prepareStatement(
                    "SELECT id_tipo_sangre FROM tipo_sangre WHERE descripcion = ?");
            psTipo.setString(1, m_Tipo_Sangre);
            rsTipo = psTipo.executeQuery();
            if (!rsTipo.next()) {
                throw new GestionDonacionesSangreException(
                        GestionDonacionesSangreException.TIPO_SANGRE_INEXISTENTE);
            }
            int idTipoSangre = rsTipo.getInt(1);

            // Consulta ordenada por hospital destino y fecha
            psConsulta = con.prepareStatement(
                    "SELECT t.id_traspaso, t.id_hospital, h.nombre, h.localidad, "
                  + "       t.id_tipo_sangre, ts.descripcion, "
                  + "       t.cantidad, t.fecha_traspaso, "
                  + "       rh.cantidad AS reserva "
                  + "FROM traspaso t "
                  + "JOIN hospital h         ON t.id_hospital    = h.id_hospital "
                  + "JOIN tipo_sangre ts      ON t.id_tipo_sangre = ts.id_tipo_sangre "
                  + "JOIN reserva_hospital rh ON rh.id_hospital   = t.id_hospital "
                  + "                        AND rh.id_tipo_sangre = t.id_tipo_sangre "
                  + "WHERE t.id_tipo_sangre = ? "
                  + "ORDER BY t.id_hospital, t.fecha_traspaso");
            psConsulta.setInt(1, idTipoSangre);
            rsConsulta = psConsulta.executeQuery();

            System.out.println("Traspasos de tipo de sangre: " + m_Tipo_Sangre);
            System.out.printf("%-12s %-6s %-25s %-15s %-6s %-10s %-10s %-12s%n",
                    "ID_TRASPASO", "ID_H", "HOSPITAL", "LOCALIDAD",
                    "TIPO", "DESCRIPCION", "CANTIDAD", "FECHA");
            System.out.println("-".repeat(100));

            while (rsConsulta.next()) {
                System.out.printf("%-12d %-6d %-25s %-15s %-6d %-10s %-10.2f %-12s%n",
                        rsConsulta.getInt("id_traspaso"),
                        rsConsulta.getInt("id_hospital"),
                        rsConsulta.getString("nombre"),
                        rsConsulta.getString("localidad"),
                        rsConsulta.getInt("id_tipo_sangre"),
                        rsConsulta.getString("descripcion"),
                        rsConsulta.getFloat("cantidad"),
                        rsConsulta.getDate("fecha_traspaso").toString());
            }

            con.commit();

        } catch (GestionDonacionesSangreException e) {
            if (con != null) con.rollback();
            throw e;
        } catch (SQLException e) {
            LOGGER.error("SQLException en consulta_traspasos. Codigo: {} Mensaje: {}",
                    e.getErrorCode(), e.getMessage());
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (rsTipo     != null) rsTipo.close();
            if (rsConsulta != null) rsConsulta.close();
            if (psTipo     != null) psTipo.close();
            if (psConsulta != null) psConsulta.close();
            if (con        != null) con.close();
        }
    }

    public static void main(String[] args) {
        TestGestionDonaciones.ejecutarTests();
    }
}
