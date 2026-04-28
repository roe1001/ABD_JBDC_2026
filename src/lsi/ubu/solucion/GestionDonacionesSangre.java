package lsi.ubu.solucion;

import java.sql.*;
import lsi.ubu.util.PoolDeConexiones;
import lsi.ubu.excepciones.GestionDonacionesSangreException;

public class GestionDonacionesSangre {

    // =========================
    // 1. REALIZAR DONACIÓN
    // =========================
    public static void realizar_donacion(String m_NIF, float m_Cantidad, int m_ID_Hospital, Date m_Fecha_Donacion)
            throws SQLException {

        Connection con = null;

        try {
            con = PoolDeConexiones.getConnection();
            con.setAutoCommit(false);

            // Validar cantidad
            if (m_Cantidad <= 0 || m_Cantidad > 0.45f) {
                throw new GestionDonacionesSangreException(5, "Valor de cantidad de donación incorrecto");
            }

            // Validar donante
            PreparedStatement ps = con.prepareStatement(
                    "SELECT id_tipo_sangre FROM donante WHERE NIF = ?");
            ps.setString(1, m_NIF);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new GestionDonacionesSangreException(1, "Donante inexistente");
            }

            int tipoSangre = rs.getInt(1);

            // Validar hospital
            ps = con.prepareStatement("SELECT 1 FROM hospital WHERE id_hospital = ?");
            ps.setInt(1, m_ID_Hospital);
            if (!ps.executeQuery().next()) {
                throw new GestionDonacionesSangreException(3, "Hospital inexistente");
            }

            // Validar 15 días
            ps = con.prepareStatement(
                    "SELECT MAX(fecha_donacion) FROM donacion WHERE nif_donante = ?");
            ps.setString(1, m_NIF);
            rs = ps.executeQuery();

            if (rs.next() && rs.getDate(1) != null) {
                long diff = m_Fecha_Donacion.getTime() - rs.getDate(1).getTime();
                long dias = diff / (1000 * 60 * 60 * 24);

                if (dias < 15) {
                    throw new GestionDonacionesSangreException(4, "Donante excede el cupo de donación");
                }
            }

            // Insertar donación
            ps = con.prepareStatement(
                    "INSERT INTO donacion VALUES (seq_donacion.nextval, ?, ?, ?)");
            ps.setString(1, m_NIF);
            ps.setFloat(2, m_Cantidad);
            ps.setDate(3, new java.sql.Date(m_Fecha_Donacion.getTime()));
            ps.executeUpdate();

            // Actualizar reserva
            ps = con.prepareStatement(
                    "UPDATE reserva_hospital SET cantidad = cantidad + ? WHERE id_hospital = ? AND id_tipo_sangre = ?");
            ps.setFloat(1, m_Cantidad);
            ps.setInt(2, m_ID_Hospital);
            ps.setInt(3, tipoSangre);
            ps.executeUpdate();

            con.commit();

        } catch (GestionDonacionesSangreException e) {
            if (con != null) con.rollback();
            throw e;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;

        } finally {
            if (con != null) con.close();
        }
    }

    // =========================
    // 2. ANULAR TRASPASO
    // =========================
    public static void anular_traspaso(int m_ID_Tipo_Sangre, int m_ID_Hospital_Origen,
                                       int m_ID_Hospital_Destino, Date m_Fecha_Traspaso)
            throws SQLException {

        Connection con = null;

        try {
            con = PoolDeConexiones.getConnection();
            con.setAutoCommit(false);

            // Validar tipo sangre
            PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM tipo_sangre WHERE id_tipo_sangre = ?");
            ps.setInt(1, m_ID_Tipo_Sangre);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new GestionDonacionesSangreException(2, "Tipo Sangre inexistente");
            }

            // Validar hospital origen
            ps = con.prepareStatement("SELECT 1 FROM hospital WHERE id_hospital = ?");
            ps.setInt(1, m_ID_Hospital_Origen);
            if (!ps.executeQuery().next()) {
                throw new GestionDonacionesSangreException(3, "Hospital inexistente");
            }

            // Validar hospital destino
            ps.setInt(1, m_ID_Hospital_Destino);
            if (!ps.executeQuery().next()) {
                throw new GestionDonacionesSangreException(3, "Hospital inexistente");
            }

            // Obtener traspasos
            ps = con.prepareStatement(
                    "SELECT cantidad FROM traspaso WHERE id_tipo_sangre=? AND id_hospital_origen=? AND id_hospital_destino=? AND fecha_traspaso=?");

            ps.setInt(1, m_ID_Tipo_Sangre);
            ps.setInt(2, m_ID_Hospital_Origen);
            ps.setInt(3, m_ID_Hospital_Destino);
            ps.setDate(4, new java.sql.Date(m_Fecha_Traspaso.getTime()));

            rs = ps.executeQuery();

            while (rs.next()) {
                float cantidad = rs.getFloat(1);

                if (cantidad < 0) {
                    throw new GestionDonacionesSangreException(6,
                            "Valor de cantidad de traspaso por debajo de lo requerido");
                }

                // Restar en destino
                PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE reserva_hospital SET cantidad = cantidad - ? WHERE id_hospital=? AND id_tipo_sangre=?");
                ps2.setFloat(1, cantidad);
                ps2.setInt(2, m_ID_Hospital_Destino);
                ps2.setInt(3, m_ID_Tipo_Sangre);
                ps2.executeUpdate();

                // Sumar en origen
                ps2 = con.prepareStatement(
                        "UPDATE reserva_hospital SET cantidad = cantidad + ? WHERE id_hospital=? AND id_tipo_sangre=?");
                ps2.setFloat(1, cantidad);
                ps2.setInt(2, m_ID_Hospital_Origen);
                ps2.setInt(3, m_ID_Tipo_Sangre);
                ps2.executeUpdate();
            }

            // Borrar traspasos
            ps = con.prepareStatement(
                    "DELETE FROM traspaso WHERE id_tipo_sangre=? AND id_hospital_origen=? AND id_hospital_destino=? AND fecha_traspaso=?");

            ps.setInt(1, m_ID_Tipo_Sangre);
            ps.setInt(2, m_ID_Hospital_Origen);
            ps.setInt(3, m_ID_Hospital_Destino);
            ps.setDate(4, new java.sql.Date(m_Fecha_Traspaso.getTime()));

            ps.executeUpdate();

            con.commit();

        } catch (GestionDonacionesSangreException e) {
            if (con != null) con.rollback();
            throw e;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;

        } finally {
            if (con != null) con.close();
        }
    }

    // =========================
    // 3. CONSULTA TRASPASOS
    // =========================
    public static void consulta_traspasos(String m_Tipo_Sangre) throws SQLException {

        Connection con = null;

        try {
            con = PoolDeConexiones.getConnection();

            // Validar tipo
            PreparedStatement ps = con.prepareStatement(
                    "SELECT id_tipo_sangre FROM tipo_sangre WHERE descripcion = ?");
            ps.setString(1, m_Tipo_Sangre);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new GestionDonacionesSangreException(2, "Tipo Sangre inexistente");
            }

            int tipo = rs.getInt(1);

            // Consulta
            ps = con.prepareStatement(
                    "SELECT t.id_traspaso, t.cantidad, t.fecha_traspaso, h.nombre " +
                    "FROM traspaso t JOIN hospital h ON t.id_hospital_destino = h.id_hospital " +
                    "WHERE t.id_tipo_sangre = ? " +
                    "ORDER BY t.id_hospital_destino, t.fecha_traspaso");

            ps.setInt(1, tipo);
            rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getInt(1) + " | " +
                        rs.getString(4) + " | " +
                        rs.getDate(3) + " | " +
                        rs.getFloat(2)
                );
            }

        } finally {
            if (con != null) con.close();
        }
    }
}
