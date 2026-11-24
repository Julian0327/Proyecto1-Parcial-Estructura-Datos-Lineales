package estructuras.grafo;

import estructuras.lineales.ListaEnlazada;
import Excepciones.EstacionNoExisteException;
import Modelo.Estacion;

/**
 * Grafo no dirigido y ponderado de la red del metro, con lista de adyacencia.
 * Cada estación recibe un índice interno (0, 1, 2...) según el orden de
 * registro. Se implementa desde cero: solo arreglos y {@link ListaEnlazada}.
 *
 * <p>Complejidad: agregar estación O(V) para detectar duplicados; conectar
 * O(V + grado); recorrer los vecinos de un vértice O(grado).
 */
public class Grafo {

    private static final int CAPACIDAD_INICIAL = 16;

    /** Vértice interno: una estación y sus conexiones salientes. */
    private static final class Vertice {
        private final Estacion estacion;
        private final ListaEnlazada<Arista> vecinos = new ListaEnlazada<>();

        private Vertice(Estacion estacion) {
            this.estacion = estacion;
        }
    }

    private Vertice[] vertices = new Vertice[CAPACIDAD_INICIAL];
    private int cantidadEstaciones;
    private int cantidadConexiones;

    /**
     * Registra una estación.
     *
     * @return true si se agregó; false si ya estaba registrada (mismo id)
     */
    public boolean agregarEstacion(Estacion estacion) {
        validarNoNula(estacion);
        if (buscarIndice(estacion) != -1) {
            return false;
        }
        if (cantidadEstaciones == vertices.length) {
            duplicarCapacidad();
        }
        vertices[cantidadEstaciones++] = new Vertice(estacion);
        return true;
    }

    /**
     * Conecta dos estaciones en ambos sentidos. Si ya estaban conectadas se
     * conserva el menor tiempo.
     *
     * @throws EstacionNoExisteException si alguna estación no está registrada
     * @throws IllegalArgumentException  si son la misma estación o el tiempo
     *                                   no es positivo
     */
    public void conectar(Estacion a, Estacion b, int minutos) {
        int indiceA = indiceDe(a);
        int indiceB = indiceDe(b);
        if (indiceA == indiceB) {
            throw new IllegalArgumentException("No se puede conectar una estación consigo misma.");
        }
        boolean eraNueva = registrarSentido(indiceA, new Arista(indiceB, minutos));
        registrarSentido(indiceB, new Arista(indiceA, minutos));
        if (eraNueva) {
            cantidadConexiones++;
        }
    }

    /** Indica si la estación está registrada. */
    public boolean contiene(Estacion estacion) {
        return estacion != null && buscarIndice(estacion) != -1;
    }

    /**
     * Devuelve el índice interno de la estación.
     *
     * @throws EstacionNoExisteException si no está registrada
     */
    public int indiceDe(Estacion estacion) {
        validarNoNula(estacion);
        int indice = buscarIndice(estacion);
        if (indice == -1) {
            throw new EstacionNoExisteException(estacion.getNombre());
        }
        return indice;
    }

    /** Devuelve la estación que corresponde a un índice interno. */
    public Estacion estacionEn(int indice) {
        validarIndice(indice);
        return vertices[indice].estacion;
    }

    /**
     * Conexiones salientes de un vértice. Se devuelve como {@link Iterable}
     * de solo lectura para no exponer la lista interna (encapsulamiento).
     */
    public Iterable<Arista> vecinosDe(int indice) {
        validarIndice(indice);
        return vertices[indice].vecinos;
    }

    public int cantidadEstaciones() {
        return cantidadEstaciones;
    }

    /** Cantidad de conexiones (cada par de estaciones cuenta una vez). */
    public int cantidadConexiones() {
        return cantidadConexiones;
    }

    public boolean estaVacio() {
        return cantidadEstaciones == 0;
    }

    /**
     * Agrega o mejora la arista saliente de un vértice.
     *
     * @return true si la conexión no existía antes
     */
    private boolean registrarSentido(int origen, Arista nueva) {
        ListaEnlazada<Arista> vecinos = vertices[origen].vecinos;
        for (Arista existente : vecinos) {
            if (existente.getDestino() == nueva.getDestino()) {
                if (nueva.getMinutos() < existente.getMinutos()) {
                    vecinos.eliminar(existente);
                    vecinos.agregar(nueva);
                }
                return false;
            }
        }
        vecinos.agregar(nueva);
        return true;
    }

    private int buscarIndice(Estacion estacion) {
        for (int i = 0; i < cantidadEstaciones; i++) {
            if (vertices[i].estacion.equals(estacion)) {
                return i;
            }
        }
        return -1;
    }

    private void duplicarCapacidad() {
        Vertice[] ampliado = new Vertice[vertices.length * 2];
        System.arraycopy(vertices, 0, ampliado, 0, cantidadEstaciones);
        vertices = ampliado;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= cantidadEstaciones) {
            throw new IndexOutOfBoundsException("Índice de estación inválido: " + indice);
        }
    }

    private static void validarNoNula(Estacion estacion) {
        if (estacion == null) {
            throw new IllegalArgumentException("La estación no puede ser nula.");
        }
    }

    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < cantidadEstaciones; i++) {
            texto.append(vertices[i].estacion).append(": ");
            boolean primera = true;
            for (Arista arista : vertices[i].vecinos) {
                if (!primera) {
                    texto.append(", ");
                }
                texto.append(vertices[arista.getDestino()].estacion.getNombre())
                     .append(" (").append(arista.getMinutos()).append(" min)");
                primera = false;
            }
            texto.append(System.lineSeparator());
        }
        return texto.toString();
    }
}
