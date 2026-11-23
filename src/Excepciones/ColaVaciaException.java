package excepciones;

/** Se lanza al intentar sacar un elemento de una cola vacía (andén vacío). */
public class ColaVaciaException extends ExcepcionMetro {

    private static final long serialVersionUID = 1L;

    public ColaVaciaException() {
        super("La cola de pasajeros está vacía: no hay nadie esperando en el andén.");
    }

    /** Permite reutilizar la excepción en otras colas (p. ej. la de prioridad). */
    public ColaVaciaException(String mensaje) {
        super(mensaje);
    }
}
