package EstructurasLineales;

public interface IPila<T> extends Iterable<T> {

    // Pone un elemento en la cima.
    void apilar(T elemento);

    // Saca y devuelve la cima. Lanza PilaVaciaException si está vacía.
    T desapilar();

    // Devuelve la cima sin sacarla. Lanza PilaVaciaException si está vacía.
    T verCima();

    boolean estaVacia();

    int tamano();
}