package EstructurasLineales;

class NodoLista<T> {

    final T dato;
    NodoLista<T> siguiente;

    NodoLista(T dato) {
        this.dato = dato;
    }
}