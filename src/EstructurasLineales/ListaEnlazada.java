package EstructurasLineales;

import java.util.Iterator;
import java.util.NoSuchElementException;

//Lista enlazada simple implementada desde cero. I
// Implementa Iterable para poder recorrerla con for-each sin usar colecciones

public class ListaEnlazada<T> implements Iterable<T> {

    private NodoLista<T> cabeza;
    private NodoLista<T> cola;
    private int tamano;

    // Agrega al final. O(1) porque se guarda la referencia a la cola.
    public void agregar(T dato) {
        NodoLista<T> nuevo = new NodoLista<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamano++;
    }

    // Agrega al inicio. O(1). Útil para armar caminos hacia atrás. 
    public void agregarAlInicio(T dato) {
        insertarEn(0, dato);
    }

    // Inserta en una posición (0 = inicio). O(n). 
    public void insertarEn(int posicion, T dato) {
        if (posicion < 0 || posicion > tamano) {
            throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        }
        if (posicion == tamano) {
            agregar(dato);
            return;
        }
        NodoLista<T> nuevo = new NodoLista<>(dato);
        if (posicion == 0) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
        } else {
            NodoLista<T> anterior = nodoEn(posicion - 1);
            nuevo.siguiente = anterior.siguiente;
            anterior.siguiente = nuevo;
        }
        tamano++;
    }

    // Devuelve el elemento de una posición (0 = primero). O(n). 
    public T obtener(int posicion) {
        validarIndice(posicion);
        return nodoEn(posicion).dato;
    }

    // Elimina la primera aparición del dato. Devuelve true si lo encontró. O(n)
    public boolean eliminar(T dato) {
        NodoLista<T> anterior = null;
        NodoLista<T> actual = cabeza;
        while (actual != null) {
            if (actual.dato.equals(dato)) {
                desenlazar(anterior, actual);
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    // Elimina y devuelve el elemento de una posición.
    public T eliminarEn(int posicion) {
        validarIndice(posicion);
        NodoLista<T> anterior = posicion == 0 ? null : nodoEn(posicion - 1);
        NodoLista<T> actual = anterior == null ? cabeza : anterior.siguiente;
        desenlazar(anterior, actual);
        return actual.dato;
    }

    // Vacía la lista. O(1): el recolector de basura libera los nodos. 
    public void limpiar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    // Indica si el dato está en la lista. 
    public boolean contiene(T dato) {
        return buscar(cabeza, dato);
    }

    // Búsqueda recursiva auxiliar. 
    private boolean buscar(NodoLista<T> nodo, T dato) {
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

    // Imprime los elementos usando un método auxiliar recursivo. 
    public void mostrar() {
        mostrar(cabeza);
        System.out.println();
    }

    private void mostrar(NodoLista<T> nodo) {
        if (nodo == null) {
            return;
        }
        System.out.print(nodo.dato + (nodo.siguiente != null ? " -> " : ""));
        mostrar(nodo.siguiente);
    }

    // Quita el nodo actual de la cadena, cuyo anterior es "anterior" (o null si es la cabeza).
    private void desenlazar(NodoLista<T> anterior, NodoLista<T> actual) {
        if (anterior == null) {
            cabeza = actual.siguiente;
        } else {
            anterior.siguiente = actual.siguiente;
        }
        if (actual == cola) {
            cola = anterior;
        }
        tamano--;
    }

    // Devuelve el nodo de una posición ya validada. */
    private NodoLista<T> nodoEn(int posicion) {
        NodoLista<T> actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.siguiente;
        }
        return actual;
    }

    private void validarIndice(int posicion) {
        if (posicion < 0 || posicion >= tamano) {
            throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodoLista<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay más elementos en la lista.");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}