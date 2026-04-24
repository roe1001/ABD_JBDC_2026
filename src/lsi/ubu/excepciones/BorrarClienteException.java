package lsi.ubu.excepciones;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BorrarClienteException: Implementa las excepciones contextualizadas de la
 * transaccion de BorrarCliente
 * 
 * @author <a href="mailto:jmaudes@ubu.es">Jesus Maudes</a>
 * @author <a href="mailto:rmartico@ubu.es">Raul Marticorena</a>
 * @version 1.0
 * @since 1.0
 */
public class BorrarClienteException extends SQLException {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(BorrarClienteException.class);

	public static final int CLIENTE_NO_EXIST = 20001;
	public static final int CLIENTE_TIENE_PEDIDOS = 20002;

	private int codigo; // = -1;
	private String mensaje;

	public BorrarClienteException(int code) {

		this.codigo = code;
		if (code == CLIENTE_NO_EXIST) {
			this.mensaje = "El cliente no existe\n";
		} else if (code == CLIENTE_TIENE_PEDIDOS) {
			this.mensaje = "El cliente tiene pedidos, así que no puede eliminarse todavía";
		} else {
			this.mensaje = "Error desconocido";
		}

		LOGGER.debug(mensaje);

		// Traza_de_pila
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			LOGGER.debug(ste.toString());
		}

	}

	@Override
	public String getMessage() { // Redefinicion del metodo de la clase Exception
		return mensaje;
	}

	@Override
	public int getErrorCode() { // Redefinicion del metodo de la clase SQLException
		return codigo;
	}
}
