package EstructurasLineales;

import java.util.Iterator;

/**
 * Lista enlazada simple implementada desde cero.
 * Implementa Iterable para poder recorrerla con for-each sin usar colecciones
 * de Java.
 *
 * @param <T> tipo de los elementos
 */
public class ListaEnlazada<T> implements Iterable<T> {

    /** Nodo interno: no se expone al exterior (encapsulamiento). */
    private static class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    /** Agrega al final. O(1) porque se guarda la referencia a la cola. */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamano++;
    }

    /** Inserta en una posición (0 = inicio). O(n). */
    public void insertarEn(int posicion, T dato) {
        if (posicion < 0 || posicion > tamano) {
            throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        }
        if (posicion == tamano) {
            agregar(dato);
            return;
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        if (posicion == 0) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
        } else {
            Nodo<T> anterior = cabeza;
            for (int i = 0; i < posicion - 1; i++) {
                anterior = anterior.siguiente;
            }
            nuevo.siguiente = anterior.siguiente;
            anterior.siguiente = nuevo;
        }
        tamano++;
    }

    /**
     * Elimina la primera aparición del dato. Devuelve true si lo encontró. O(n).
     */
    public boolean eliminar(T dato) {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.dato.equals(dato)) {
                if (anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == cola) {
                    cola = anterior;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    /** Indica si el dato está en la lista. O(n). */
    public boolean contiene(T dato) {
        return buscar(cabeza, dato);
    }

    /** Búsqueda recursiva auxiliar. */
    private boolean buscar(Nodo<T> nodo, T dato) {
        if (nodo == null) {
            return false;
        }
        if (nodo.dato.equals(dato)) {
            return true;
        }
        return buscar(nodo.siguiente, dato);
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    /** Imprime los elementos usando un método auxiliar recursivo. */
    public void mostrar() {
        mostrar(cabeza);
        System.out.println();
    }

    private void mostrar(Nodo<T> nodo) {
        if (nodo == null) {
            return;
        }
        System.out.print(nodo.dato + (nodo.siguiente != null ? " -> " : ""));
        mostrar(nodo.siguiente);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}