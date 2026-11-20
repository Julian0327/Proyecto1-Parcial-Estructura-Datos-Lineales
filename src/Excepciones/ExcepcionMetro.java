package Excepciones;

/**
 * Clase base de todas las excepciones del sistema de metro.
 * Es no verificada (RuntimeException) para no llenar el código de throws.
 */
public class ExcepcionMetro extends RuntimeException {

    public ExcepcionMetro(String mensaje) {
        super(mensaje);
    }
}