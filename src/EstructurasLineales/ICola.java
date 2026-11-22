package EstructurasLineales;

public interface ICola<T> {

    //Agrega un elemento al final de la cola.
    void encolar(T elemento);

    //Saca y devuelve el elemento del frente. Lanza ColaVaciaException si está vacía.
    T desencolar();

    //Devuelve el elemento del frente sin sacarlo. Lanza ColaVaciaException si está vacía.
    T verFrente();

    boolean estaVacia();

    int tamano();
}