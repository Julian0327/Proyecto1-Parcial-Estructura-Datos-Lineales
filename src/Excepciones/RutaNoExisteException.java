package excepciones;

/** Se lanza cuando no existe ningún camino entre dos estaciones de la red. */
public class RutaNoExisteException extends ExcepcionMetro {

    private static final long serialVersionUID = 1L;

    public RutaNoExisteException(String origen, String destino) {
        super("No existe una ruta entre '" + origen + "' y '" + destino + "'.");
    }
}
