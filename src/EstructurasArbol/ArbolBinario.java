package EstructurasArbol;

import EstructurasLineales.ListaEnlazada;

/**
 * Árbol binario de búsqueda (BST) clave -> valor implementado desde cero.
 * Las claves se mantienen ordenadas según su orden natural
 * ({@link Comparable}).
 *
 * <p>
 * Complejidad: insertar, buscar y eliminar son O(h) donde h es la altura
 * del árbol (O(log n) promedio, O(n) en el peor caso); los recorridos inorden
 * son O(n).
 *
 * @param <K> tipo de la clave (comparable)
 * @param <V> tipo del valor
 */
public class ArbolBinario<K extends Comparable<K>, V> implements IArbol<K, V> {

    private NodoArbol<K, V> raiz;
    private int tamano;

    /** Inserta el par clave-valor. Si la clave ya existe, reemplaza su valor. */
    @Override
    public void insertar(K clave, V valor) {
        validarClave(clave);
        raiz = insertarRecursivo(raiz, clave, valor);
    }

    private NodoArbol<K, V> insertarRecursivo(NodoArbol<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            tamano++;
            return new NodoArbol<>(clave, valor);
        }
        int comparacion = clave.compareTo(nodo.getClave());
        if (comparacion < 0) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), clave, valor));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), clave, valor));
        } else {
            nodo.setValor(valor);
        }
        return nodo;
    }

    /** Devuelve el valor asociado a la clave o null si no existe. */
    @Override
    public V buscar(K clave) {
        if (clave == null) {
            return null;
        }
        NodoArbol<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.getClave());
            if (comparacion < 0) {
                actual = actual.getIzquierdo();
            } else if (comparacion > 0) {
                actual = actual.getDerecho();
            } else {
                return actual.getValor();
            }
        }
        return null;
    }

    /** Indica si la clave existe en el árbol. */
    @Override
    public boolean contiene(K clave) {
        if (clave == null) {
            return false;
        }
        NodoArbol<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.getClave());
            if (comparacion < 0) {
                actual = actual.getIzquierdo();
            } else if (comparacion > 0) {
                actual = actual.getDerecho();
            } else {
                return true;
            }
        }
        return false;
    }

    /** Elimina la clave del árbol. Devuelve true si existía. */
    @Override
    public boolean eliminar(K clave) {
        if (clave == null) {
            return false;
        }
        int tamanoAntes = tamano;
        raiz = eliminarRecursivo(raiz, clave);
        return tamano < tamanoAntes;
    }

    private NodoArbol<K, V> eliminarRecursivo(NodoArbol<K, V> nodo, K clave) {
        if (nodo == null) {
            return null;
        }
        int comparacion = clave.compareTo(nodo.getClave());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminarRecursivo(nodo.getIzquierdo(), clave));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), clave));
        } else {
            if (nodo.getIzquierdo() == null) {
                tamano--;
                return nodo.getDerecho();
            }
            if (nodo.getDerecho() == null) {
                tamano--;
                return nodo.getIzquierdo();
            }
            NodoArbol<K, V> sucesor = minimo(nodo.getDerecho());
            nodo.setClave(sucesor.getClave());
            nodo.setValor(sucesor.getValor());
            nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), sucesor.getClave()));
        }
        return nodo;
    }

    private NodoArbol<K, V> minimo(NodoArbol<K, V> nodo) {
        NodoArbol<K, V> actual = nodo;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    @Override
    public boolean estaVacio() {
        return tamano == 0;
    }

    /** Altura del árbol: 0 si está vacío, 1 si solo tiene la raíz. */
    @Override
    public int altura() {
        return alturaRecursiva(raiz);
    }

    private int alturaRecursiva(NodoArbol<K, V> nodo) {
        if (nodo == null) {
            return 0;
        }
        int izq = alturaRecursiva(nodo.getIzquierdo());
        int der = alturaRecursiva(nodo.getDerecho());
        return 1 + (izq > der ? izq : der);
    }

    /** Valores en orden ascendente de clave (recorrido inorden). */
    @Override
    public ListaEnlazada<V> valoresEnOrden() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        inordenValores(raiz, resultado);
        return resultado;
    }

    private void inordenValores(NodoArbol<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        inordenValores(nodo.getIzquierdo(), destino);
        destino.agregar(nodo.getValor());
        inordenValores(nodo.getDerecho(), destino);
    }

    /** Claves en orden ascendente (recorrido inorden). */
    @Override
    public ListaEnlazada<K> clavesEnOrden() {
        ListaEnlazada<K> resultado = new ListaEnlazada<>();
        inordenClaves(raiz, resultado);
        return resultado;
    }

    private void inordenClaves(NodoArbol<K, V> nodo, ListaEnlazada<K> destino) {
        if (nodo == null) {
            return;
        }
        inordenClaves(nodo.getIzquierdo(), destino);
        destino.agregar(nodo.getClave());
        inordenClaves(nodo.getDerecho(), destino);
    }

    private static <K> void validarClave(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser null.");
        }
    }
}
