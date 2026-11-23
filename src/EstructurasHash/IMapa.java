package EstructurasHash;

import EstructurasLineales.ListaEnlazada;

public interface IMapa<K, V> {

    void insertar(K clave, V valor);

    V obtener(K clave);

    boolean contiene(K clave);

    boolean eliminar(K clave);

    int tamano();

    boolean estaVacia();

    ListaEnlazada<K> claves();

    ListaEnlazada<V> valores();
}