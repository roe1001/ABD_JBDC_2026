package lsi.ubu.servicios;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.util.ExecuteScript;
import lsi.ubu.util.PoolDeConexiones;


/**
 * GestionDonacionesSangre:
 * Implementa la gestion de donaciones de sangre según el enunciado del ejercicio
 * 
 * @author <a href="mailto:jmaudes@ubu.es">Jesus Maudes</a>
 * @author <a href="mailto:rmartico@ubu.es">Raul Marticorena</a>
 * @author <a href="mailto:pgdiaz@ubu.es">Pablo Garcia</a>
 * @author <a href="mailto:srarribas@ubu.es">Sandra Rodriguez</a>
 * @version 1.5
 * @since 1.0 
 */
public class EsqueletoGestionDonacionesSangre {
	
	private static Logger logger = LoggerFactory.getLogger(EsqueletoGestionDonacionesSangre.class);

	private static final String script_path = "sql/";

	public static void main(String[] args) throws SQLException{		
		tests();

		System.out.println("FIN.............");
	}
	
	public static void realizar_donacion(String m_NIF, int m_ID_Hospital,
			float m_Cantidad,  Date m_Fecha_Donacion) throws SQLException {
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
						
			// --- BLOQUE 1: Validaciones de datos de entrada ---
            // El volumen debe ser positivo y no exceder los 0.45 litros 
            if (m_Cantidad < 0 || m_Cantidad > 0.45f) {
                throw new GestionDonacionesSangreException(GestionDonacionesSangreException.VALOR_CANTIDAD_DONACION_INCORRECTO);
            }

            // Comprobar existencia del donante para obtener su tipo de sangre 
            PreparedStatement stDni = con.prepareStatement("SELECT id_tipo_sangre FROM donante WHERE NIF = ?");
            stDni.setString(1, m_NIF);
            ResultSet rsDni = stDni.executeQuery();

            if (!rsDni.next()) {
                throw new GestionDonacionesSangreException(GestionDonacionesSangreException.DONANTE_NO_EXISTE);
            }
            int tipoSangreDonante = rsDni.getInt(1);
			
			// --- BLOQUE 2: Reglas de hospital y tiempo entre donaciones ---
            // Validar que el hospital de destino existe 
            PreparedStatement stHosp = con.prepareStatement("SELECT 1 FROM hospital WHERE id_hospital = ?");
            stHosp.setInt(1, m_ID_Hospital);
            if (!stHosp.executeQuery().next()) {
                throw new GestionDonacionesSangreException(GestionDonacionesSangreException.HOSPITAL_NO_EXISTE);
            }

            // Regla de los 15 días: Una persona no puede donar tan seguido
            PreparedStatement stFecha = con.prepareStatement("SELECT MAX(fecha_donacion) FROM donacion WHERE nif_donante = ?");
            stFecha.setString(1, m_NIF);
            ResultSet rsFecha = stFecha.executeQuery();

            if (rsFecha.next() && rsFecha.getDate(1) != null) {
                // Usamos la utilidad Misc para calcular la diferencia de días
                if (Misc.howManyDaysBetween(m_Fecha_Donacion, rsFecha.getDate(1)) < 15) {
                    throw new GestionDonacionesSangreException(GestionDonacionesSangreException.DONANTE_EXCEDE);
                }
            }
			
			
			
		} catch (SQLException e) {
			//Completar por el alumno			
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno*/
		}
		
		
	}
	
	public static void anular_traspaso(int m_ID_Tipo_Sangre, int m_ID_Hospital_Origen,int m_ID_Hospital_Destino,
			Date m_Fecha_Traspaso)
			throws SQLException {
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
			//Completar por el alumno
			
		} catch (SQLException e) {
			//Completar por el alumno			
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno*/
		}		
	}
	
	public static void consulta_traspasos(String m_Tipo_Sangre)
			throws SQLException {

				
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
			//Completar por el alumno
			
		} catch (SQLException e) {
			//Completar por el alumno			
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno*/
		}		
	}
	
	static public void creaTablas() {
		ExecuteScript.run(script_path + "gestion_donaciones_sangre.sql");
	}

	static void tests() throws SQLException{
		creaTablas();
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();		
		
		//Relatar caso por caso utilizando el siguiente procedure para inicializar los datos
		
		CallableStatement cll_reinicia=null;
		Connection conn = null;
		
		try {
			//Reinicio filas
			conn = pool.getConnection();
			cll_reinicia = conn.prepareCall("{call inicializa_test}");
			cll_reinicia.execute();
		} catch (SQLException e) {				
			logger.error(e.getMessage());			
		} finally {
			if (cll_reinicia!=null) cll_reinicia.close();
			if (conn!=null) conn.close();
		
		}			
		
	}
}
