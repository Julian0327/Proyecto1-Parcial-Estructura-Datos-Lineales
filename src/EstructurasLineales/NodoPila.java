package EstructurasLineales;

class NodoPila<T> {

    final T dato;
    NodoPila<T> debajo;

    NodoPila(T dato, NodoPila<T> debajo) {
        this.dato = dato;
        this.debajo = debajo;
    }
}