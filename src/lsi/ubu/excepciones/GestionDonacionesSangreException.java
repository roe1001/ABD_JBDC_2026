package lsi.ubu.excepciones;

import java.sql.SQLException;

public class GestionDonacionesSangreException extends SQLException {

    private static final long serialVersionUID = 1L;

    // Códigos de error definidos en el enunciado
    public static final int DONANTE_INEXISTENTE          = 1;
    public static final int TIPO_SANGRE_INEXISTENTE      = 2;
    public static final int HOSPITAL_INEXISTENTE         = 3;
    public static final int DONANTE_EXCEDE_CUPO          = 4;
    public static final int CANTIDAD_DONACION_INCORRECTA = 5;
    public static final int CANTIDAD_TRASPASO_INCORRECTA = 6;

    private static final String[] MENSAJES = {
        "",
        "Donante inexistente",
        "Tipo Sangre inexistente",
        "Hospital Inexistene",          // typo del enunciado, detectado al final, no lo he cambiado :P
        "Donante excede el cupo de donación",
        "Valor de cantidad de donación incorrecto",
        "Valor de cantidad de traspaso por debajo de lo requerido"
    };

    public GestionDonacionesSangreException(int codigo) {
        super(MENSAJES[codigo], null, codigo);
    }
}
