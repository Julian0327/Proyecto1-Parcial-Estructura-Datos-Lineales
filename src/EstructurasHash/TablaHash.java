package EstructurasHash;

import EstructurasLineales.ListaEnlazada;
import java.lang.reflect.Array;

/**
 * Tabla hash con encadenamiento: cada cubeta guarda una lista enlazada de
 * nodos, así que dos claves que caen en la misma cubeta (colisión) conviven
 * en la misma cadena. Implementada desde cero, sin colecciones de Java.
 *
 * <p>
 * Complejidad: insertar, obtener y eliminar son O(1) promedio y O(n) en el
 * peor caso (todas las claves en una cubeta). Cuando el factor de carga pasa
 * de 0.75 la tabla duplica su capacidad (rehash) para mantener el O(1).
 *
 * @param <K> tipo de la clave (debe tener bien definidos equals y hashCode)
 * @param <V> tipo del valor
 */
public class TablaHash<K, V> implements IMapa<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    private NodoHash<K, V>[] cubetas;
    private int tamano;

    public TablaHash() {
        this(CAPACIDAD_INICIAL);
    }

    public TablaHash(int capacidadInicial) {
        if (capacidadInicial <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        this.cubetas = crearCubetas(capacidadInicial);
    }

    /**
     * Java no permite crear arreglos genéricos con new; se crea con
     * Array.newInstance y se convierte.
     */
    @SuppressWarnings("unchecked")
    private NodoHash<K, V>[] crearCubetas(int capacidad) {
        return (NodoHash<K, V>[]) Array.newInstance(NodoHash.class, capacidad);
    }

    /**
     * Cubeta que le corresponde a la clave. Es pública para poder demostrar
     * en la exposición que dos claves distintas caen en la misma cubeta.
     * La máscara 0x7fffffff evita índices negativos.
     */
    public int indiceDe(K clave) {
        validarClave(clave);
        return (clave.hashCode() & 0x7fffffff) % cubetas.length;
    }

    /** Inserta o actualiza. O(1) promedio. */
    @Override
    public void insertar(K clave, V valor) {
        int indice = indiceDe(clave);
        NodoHash<K, V> existente = buscarNodo(indice, clave);
        if (existente != null) {
            existente.setValor(valor);
            return;
        }

        // El nodo nuevo se pone al inicio de la cadena (O(1)).
        cubetas[indice] = new NodoHash<>(clave, valor, cubetas[indice]);
        tamano++;
        if ((double) tamano / cubetas.length > FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
    }

    /** Busca por clave. O(1) promedio. Devuelve null si no existe. */
    @Override
    public V obtener(K clave) {
        NodoHash<K, V> nodo = buscarNodo(indiceDe(clave), clave);
        return nodo == null ? null : nodo.getValor();
    }

    @Override
    public boolean contiene(K clave) {
        return buscarNodo(indiceDe(clave), clave) != null;
    }

    /** Elimina un nodo de la cadena de su cubeta. O(1) promedio. */
    @Override
    public boolean eliminar(K clave) {
        int indice = indiceDe(clave);
        NodoHash<K, V> anterior = null;
        NodoHash<K, V> actual = cubetas[indice];
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                if (anterior == null) {
                    cubetas[indice] = actual.getSiguiente();
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return false;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0;
    }

    /** Recorre todas las cubetas. O(capacidad + n). */
    @Override
    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> resultado = new ListaEnlazada<>();
        for (NodoHash<K, V> cabeza : cubetas) {
            for (NodoHash<K, V> n = cabeza; n != null; n = n.getSiguiente()) {
                resultado.agregar(n.getClave());
            }
        }
        return resultado;
    }

    @Override
    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        for (NodoHash<K, V> cabeza : cubetas) {
            for (NodoHash<K, V> n = cabeza; n != null; n = n.getSiguiente()) {
                resultado.agregar(n.getValor());
            }
        }
        return resultado;
    }

    public int capacidad() {
        return cubetas.length;
    }

    /**
     * Cuenta cuántos elementos comparten cubeta con otro (por cada cubeta,
     * elementos - 1).
     */
    public int cantidadColisiones() {
        int colisiones = 0;
        for (NodoHash<K, V> cabeza : cubetas) {
            int enCubeta = 0;
            for (NodoHash<K, V> n = cabeza; n != null; n = n.getSiguiente()) {
                enCubeta++;
            }
            if (enCubeta > 1) {
                colisiones += enCubeta - 1;
            }
        }
        return colisiones;
    }

    /**
     * Imprime las cubetas ocupadas y sus cadenas (útil para mostrar la colisión).
     */
    public void mostrar() {
        System.out.println("TablaHash (tamaño " + tamano + ", capacidad "
                + cubetas.length + ", colisiones " + cantidadColisiones() + ")");
        for (int i = 0; i < cubetas.length; i++) {
            if (cubetas[i] == null) {
                continue;
            }
            StringBuilder linea = new StringBuilder("  [" + i + "] ");
            for (NodoHash<K, V> n = cubetas[i]; n != null; n = n.getSiguiente()) {
                linea.append(n.getClave()).append(" -> ").append(n.getValor());
                if (n.getSiguiente() != null) {
                    linea.append(" | ");
                }
            }
            System.out.println(linea);
        }
    }

    /** Recorre la cadena de una cubeta buscando la clave. */
    private NodoHash<K, V> buscarNodo(int indice, K clave) {
        for (NodoHash<K, V> n = cubetas[indice]; n != null; n = n.getSiguiente()) {
            if (n.getClave().equals(clave)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Duplica la capacidad y reubica cada nodo (rehash), sin crear nodos nuevos.
     */
    private void redimensionar() {
        NodoHash<K, V>[] anteriores = cubetas;
        cubetas = crearCubetas(anteriores.length * 2);
        for (NodoHash<K, V> cabeza : anteriores) {
            NodoHash<K, V> actual = cabeza;
            while (actual != null) {
                NodoHash<K, V> siguiente = actual.getSiguiente();
                int indice = indiceDe(actual.getClave());
                actual.setSiguiente(cubetas[indice]);
                cubetas[indice] = actual;
                actual = siguiente;
            }
        }
    }

    private void validarClave(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser null.");
        }
    }
}