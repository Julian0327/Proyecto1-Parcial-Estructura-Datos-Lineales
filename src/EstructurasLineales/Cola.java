package EstructurasLineales;

import Excepciones.ColaVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Cola<T> implements ICola<T> {

    private NodoCola<T> frente;
    private NodoCola<T> fin;
    private int tamano;

    // Agrega al final de la cola. O(1).
    @Override
    public void encolar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se puede encolar un elemento nulo.");
        }
        NodoCola<T> nuevo = new NodoCola<>(elemento);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamano++;
    }

    // Saca el elemento del frente. O(1). Lanza ColaVaciaException si no hay nadie.
    @Override
    public T desencolar() {
        T elemento = verFrente();
        frente = frente.siguiente;
        if (frente == null) {
            fin = null; // la cola quedó vacía
        }
        tamano--;
        return elemento;
    }

    // Consulta el frente sin sacarlo. O(1).
    @Override
    public T verFrente() {
        if (estaVacia()) {
            throw new ColaVaciaException();
        }
        return frente.dato;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    // Recorre la cola desde el frente hasta el final sin modificarla.
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodoCola<T> actual = frente;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay más elementos en la cola.");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
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