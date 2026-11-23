package EstructurasHash;

import EstructurasLineales.ListaEnlazada;

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private NodoHash<K, V>[] crearCubetas(int capacidad) {
        return (NodoHash<K, V>[]) new NodoHash[capacidad];
    }

    public int indiceDe(K clave) {
        validarClave(clave);
        return (clave.hashCode() & 0x7fffffff) % cubetas.length;
    }

    @Override
    public void insertar(K clave, V valor) {
        int indice = indiceDe(clave);
        NodoHash<K, V> existente = buscarNodo(indice, clave);
        if (existente != null) {
            existente.setValor(valor);
            return;
        }

        cubetas[indice] = new NodoHash<>(clave, valor, cubetas[indice]);
        tamano++;
        if ((double) tamano / cubetas.length > FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
    }

    @Override
    public V obtener(K clave) {
        NodoHash<K, V> nodo = buscarNodo(indiceDe(clave), clave);
        return nodo == null ? null : nodo.getValor();
    }

    @Override
    public boolean contiene(K clave) {
        return buscarNodo(indiceDe(clave), clave) != null;
    }

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

    private NodoHash<K, V> buscarNodo(int indice, K clave) {
        for (NodoHash<K, V> n = cubetas[indice]; n != null; n = n.getSiguiente()) {
            if (n.getClave().equals(clave)) {
                return n;
            }
        }
        return null;
    }

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