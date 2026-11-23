package EstructurasArbol;

/**
 * Nodo de un árbol binario de búsqueda: una clave, su valor y dos hijos.
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
class NodoArbol<K, V> {

    private K clave;
    private V valor;
    private NodoArbol<K, V> izquierdo;
    private NodoArbol<K, V> derecho;

    NodoArbol(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
    }

    K getClave() {
        return clave;
    }

    void setClave(K clave) {
        this.clave = clave;
    }

    V getValor() {
        return valor;
    }

    void setValor(V valor) {
        this.valor = valor;
    }

    NodoArbol<K, V> getIzquierdo() {
        return izquierdo;
    }

    void setIzquierdo(NodoArbol<K, V> izquierdo) {
        this.izquierdo = izquierdo;
    }

    NodoArbol<K, V> getDerecho() {
        return derecho;
    }

    void setDerecho(NodoArbol<K, V> derecho) {
        this.derecho = derecho;
    }
}