package lsi.ubu.tests;

import java.sql.Date;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.excepciones.GestionDonacionesSangreException;
import lsi.ubu.solucion.GestionDonacionesSangre;

public class TestGestionDonaciones {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestGestionDonaciones.class);

    public static void ejecutarTests() {
        LOGGER.info("=== Inicio tests GestionDonacionesSangre ===");

        testRealizarDonacion();
        testAnularTraspaso();
        testConsultaTraspasos();

        LOGGER.info("=== Fin tests GestionDonacionesSangre ===");
    }

    private static void testRealizarDonacion() {
        // Caso 1: Donante inexistente
        try {
            GestionDonacionesSangre.realizar_donacion(
                    "00000000Z", 1, 0.3f, Date.valueOf("2026-04-01"));
            LOGGER.error("FALLO: debería haber lanzado excepción - Donante inexistente");
        } catch (GestionDonacionesSangreException e) {
            if (e.getErrorCode() == GestionDonacionesSangreException.DONANTE_INEXISTENTE)
                LOGGER.info("OK: Donante inexistente");
            else
                LOGGER.error("FALLO: código inesperado {}", e.getErrorCode());
        } catch (SQLException e) {
            LOGGER.error("FALLO SQL inesperado: {}", e.getMessage());
        }

        // Caso 2: Hospital inexistente
        try {
            GestionDonacionesSangre.realizar_donacion(
                    "12345678A", 9999, 0.3f, Date.valueOf("2026-04-01"));
            LOGGER.error("FALLO: debería haber lanzado excepción - Hospital inexistente");
        } catch (GestionDonacionesSangreException e) {
            if (e.getErrorCode() == GestionDonacionesSangreException.HOSPITAL_INEXISTENTE)
                LOGGER.info("OK: Hospital inexistente");
            else
                LOGGER.error("FALLO: código inesperado {}", e.getErrorCode());
        } catch (SQLException e) {
            LOGGER.error("FALLO SQL inesperado: {}", e.getMessage());
        }
        
        // ... (resto de casos del código que pasaste)
    }

    private static void testAnularTraspaso() {
        // Implementación del test de anulación [cite: 24]
    }

    private static void testConsultaTraspasos() {
        // Implementación del test de consulta [cite: 26]
    }
}