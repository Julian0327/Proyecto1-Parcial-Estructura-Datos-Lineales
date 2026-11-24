package Excepciones;

//Se lanza al intentar ver o sacar un elemento de una pila vacía (sin historial).
public class PilaVaciaException extends ExcepcionMetro {

    private static final long serialVersionUID = 1L;

    public PilaVaciaException() {
        super("La pila está vacía: no hay consultas en el historial.");
    }
}