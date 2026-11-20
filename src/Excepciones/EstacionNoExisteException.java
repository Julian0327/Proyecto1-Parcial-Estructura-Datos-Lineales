package Excepciones;

/** Se lanza cuando se consulta una estación que no está registrada. */
public class EstacionNoExisteException extends ExcepcionMetro {

    public EstacionNoExisteException(String nombreOId) {
        super("La estación '" + nombreOId + "' no existe en la red del metro.");
    }
}