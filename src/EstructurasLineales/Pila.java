package EstructurasLineales;

import Excepciones.PilaVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;

//* Pila LIFO con nodos enlazados, implementada desde cero.
//* Apilar y desapilar ocurren siempre en la cima, por eso son O(1).
//* Es Iterable (de la cima hacia la base) para mostrar el historial completo de consultas, de la más reciente a la más antigua, sin modificar la pila.

public class Pila<T> implements IPila<T> {

    private NodoPila<T> cima;
    private int tamano;

    /** Pone el elemento en la cima. O(1). */
    @Override
    public void apilar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se puede apilar un elemento nulo.");
        }
        cima = new NodoPila<>(elemento, cima);
        tamano++;
    }

    /** Saca la cima ("deshacer"). O(1). Lanza PilaVaciaException si no hay nada. */
    @Override
    public T desapilar() {
        T elemento = verCima();
        cima = cima.debajo;
        tamano--;
        return elemento;
    }

    /** Consulta la cima sin sacarla (última consulta realizada). O(1). */
    @Override
    public T verCima() {
        if (estaVacia()) {
            throw new PilaVaciaException();
        }
        return cima.dato;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    /** Recorre la pila de la cima hacia la base sin modificarla. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodoPila<T> actual = cima;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay más elementos en la pila.");
                }
                T dato = actual.dato;
                actual = actual.debajo;
                return dato;
            }
        };
    }

    /** Texto con los elementos, para mostrarlos en la demo. */
    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder("[");
        for (T elemento : this) {
            if (texto.length() > 1) {
                texto.append(", ");
            }
            texto.append(elemento);
        }
        return texto.append("]").toString();
    }
}