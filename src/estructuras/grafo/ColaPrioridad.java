package estructuras.grafo;

import excepciones.ColaVaciaException;

/**
 * Cola de prioridad mínima (min-heap binario) implementada desde cero con
 * arreglos. Sale primero el elemento con el menor valor de prioridad.
 * Reemplaza a {@code java.util.PriorityQueue}, que el proyecto no permite.
 *
 * <p>Complejidad: insertar y extraer O(log n); ver el mínimo O(1).
 *
 * @param <T> tipo de los elementos
 */
public class ColaPrioridad<T> {

    private static final int CAPACIDAD_INICIAL = 16;

    private Object[] elementos = new Object[CAPACIDAD_INICIAL];
    private int[] prioridades = new int[CAPACIDAD_INICIAL];
    private int tamano;

    /** Inserta un elemento con su prioridad. O(log n). */
    public void insertar(T elemento, int prioridad) {
        if (tamano == elementos.length) {
            duplicarCapacidad();
        }
        elementos[tamano] = elemento;
        prioridades[tamano] = prioridad;
        subir(tamano);
        tamano++;
    }

    /**
     * Quita y devuelve el elemento de menor prioridad. O(log n).
     *
     * @throws ColaVaciaException si la cola está vacía
     */
    @SuppressWarnings("unchecked")
    public T extraerMinimo() {
        validarNoVacia();
        T minimo = (T) elementos[0];
        tamano--;
        elementos[0] = elementos[tamano];
        prioridades[0] = prioridades[tamano];
        elementos[tamano] = null; // evita retener referencias
        if (tamano > 0) {
            bajar(0);
        }
        return minimo;
    }

    /**
     * Prioridad del elemento que saldría primero, sin sacarlo. O(1).
     *
     * @throws ColaVaciaException si la cola está vacía
     */
    public int prioridadMinima() {
        validarNoVacia();
        return prioridades[0];
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    /** Sube el nodo hasta que su padre sea menor o igual. */
    private void subir(int posicion) {
        while (posicion > 0) {
            int padre = (posicion - 1) / 2;
            if (prioridades[padre] <= prioridades[posicion]) {
                return;
            }
            intercambiar(padre, posicion);
            posicion = padre;
        }
    }

    /** Baja el nodo hasta que sus hijos sean mayores o iguales. */
    private void bajar(int posicion) {
        while (true) {
            int izquierdo = 2 * posicion + 1;
            int derecho = izquierdo + 1;
            int menor = posicion;
            if (izquierdo < tamano && prioridades[izquierdo] < prioridades[menor]) {
                menor = izquierdo;
            }
            if (derecho < tamano && prioridades[derecho] < prioridades[menor]) {
                menor = derecho;
            }
            if (menor == posicion) {
                return;
            }
            intercambiar(posicion, menor);
            posicion = menor;
        }
    }

    private void intercambiar(int i, int j) {
        Object elementoAuxiliar = elementos[i];
        elementos[i] = elementos[j];
        elementos[j] = elementoAuxiliar;
        int prioridadAuxiliar = prioridades[i];
        prioridades[i] = prioridades[j];
        prioridades[j] = prioridadAuxiliar;
    }

    private void duplicarCapacidad() {
        Object[] nuevosElementos = new Object[elementos.length * 2];
        int[] nuevasPrioridades = new int[prioridades.length * 2];
        System.arraycopy(elementos, 0, nuevosElementos, 0, tamano);
        System.arraycopy(prioridades, 0, nuevasPrioridades, 0, tamano);
        elementos = nuevosElementos;
        prioridades = nuevasPrioridades;
    }

    private void validarNoVacia() {
        if (tamano == 0) {
            throw new ColaVaciaException("La cola de prioridad está vacía.");
        }
    }
}
