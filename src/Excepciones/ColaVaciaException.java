package Excepciones;

/** Se lanza al intentar sacar un elemento de una cola vacía (andén vacío). */
public class ColaVaciaException extends ExcepcionMetro {

    public ColaVaciaException() {
        super("La cola de pasajeros está vacía: no hay nadie esperando en el andén.");
    }
}