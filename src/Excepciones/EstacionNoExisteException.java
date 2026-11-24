package Excepciones;

/** Se lanza cuando se consulta una estación que no está registrada. */
public class EstacionNoExisteException extends ExcepcionMetro {

    private static final long serialVersionUID = 1L;

    public EstacionNoExisteException(String nombreOId) {
        super("La estación '" + nombreOId + "' no existe en la red del metro.");
    }
}