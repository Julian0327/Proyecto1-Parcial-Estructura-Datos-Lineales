package EstructurasArbol;

import EstructurasLineales.ListaEnlazada;

/**
 * Contrato de un árbol de búsqueda clave -> valor.
 *
 * @param <K> tipo de la clave (debe ser comparable)
 * @param <V> tipo del valor
 */
public interface IArbol<K extends Comparable<K>, V> {

    /** Inserta el par. Si la clave ya existe, reemplaza su valor. */
    void insertar(K clave, V valor);

    /** Devuelve el valor asociado a la clave o null si no existe. */
    V buscar(K clave);

    /** Indica si la clave existe en el árbol. */
    boolean contiene(K clave);

    /** Elimina la clave. Devuelve true si existía. */
    boolean eliminar(K clave);

    /** Cantidad de nodos del árbol. */
    int tamano();

    boolean estaVacio();

    /** Altura del árbol: 0 si está vacío, 1 si solo tiene la raíz. */
    int altura();

    /** Valores en orden ascendente de clave (recorrido inorden). */
    ListaEnlazada<V> valoresEnOrden();

    /** Claves en orden ascendente (recorrido inorden). */
    ListaEnlazada<K> clavesEnOrden();
}