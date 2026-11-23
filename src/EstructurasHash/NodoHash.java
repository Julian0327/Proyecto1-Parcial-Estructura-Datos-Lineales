package EstructurasHash;

class NodoHash<K, V> {

    private final K clave;
    private V valor;
    private NodoHash<K, V> siguiente;

    NodoHash(K clave, V valor, NodoHash<K, V> siguiente) {
        this.clave = clave;
        this.valor = valor;
        this.siguiente = siguiente;
    }

    K getClave() {
        return clave;
    }

    V getValor() {
        return valor;
    }

    void setValor(V valor) {
        this.valor = valor;
    }

    NodoHash<K, V> getSiguiente() {
        return siguiente;
    }

    void setSiguiente(NodoHash<K, V> siguiente) {
        this.siguiente = siguiente;
    }
}