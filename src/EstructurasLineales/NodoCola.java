package EstructurasLineales;

class NodoCola<T> {

    final T dato;
    NodoCola<T> siguiente;

    NodoCola(T dato) {
        this.dato = dato;
    }
}