package algoritmos;

import estructuras.grafo.Grafo;
import estructuras.lineales.ListaEnlazada;
import excepciones.RutaNoExisteException;
import modelo.Estacion;
import modelo.ResultadoRuta;

/**
 * Esqueleto común de los algoritmos de ruta (patrón Template Method).
 * Aquí viven las partes repetidas: validaciones, caso origen = destino,
 * detección de ruta inexistente y reconstrucción recursiva del camino.
 * Cada subclase solo implementa {@link #explorar}.
 */
public abstract class AlgoritmoRutaBase implements AlgoritmoRuta {

    /** Distancia de un vértice todavía no alcanzado. */
    protected static final int INFINITO = Integer.MAX_VALUE;

    /** Predecesor de un vértice sin camino previo (origen o no alcanzado). */
    protected static final int SIN_PREDECESOR = -1;

    @Override
    public final ResultadoRuta calcular(Grafo grafo, Estacion origen, Estacion destino) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo.");
        }
        int indiceOrigen = grafo.indiceDe(origen);
        int indiceDestino = grafo.indiceDe(destino);

        int[] distancia = new int[grafo.cantidadEstaciones()];
        int[] predecesor = new int[grafo.cantidadEstaciones()];
        for (int i = 0; i < distancia.length; i++) {
            distancia[i] = INFINITO;
            predecesor[i] = SIN_PREDECESOR;
        }
        distancia[indiceOrigen] = 0;

        if (indiceOrigen != indiceDestino) {
            explorar(grafo, indiceOrigen, indiceDestino, distancia, predecesor);
            if (distancia[indiceDestino] == INFINITO) {
                throw new RutaNoExisteException(origen.getNombre(), destino.getNombre());
            }
        }
        ListaEnlazada<Estacion> camino = new ListaEnlazada<>();
        reconstruirCamino(grafo, predecesor, indiceDestino, camino);
        return new ResultadoRuta(camino, distancia[indiceDestino]);
    }

    /**
     * Recorre el grafo desde el origen. Debe llenar {@code distancia} (minutos
     * acumulados por el camino elegido) y {@code predecesor} (de qué vértice
     * se llegó a cada uno). Puede detenerse al alcanzar el destino.
     */
    protected abstract void explorar(Grafo grafo, int origen, int destino,
                                     int[] distancia, int[] predecesor);

    /** Reconstruye el camino de forma recursiva: primero el resto, luego el vértice. */
    private void reconstruirCamino(Grafo grafo, int[] predecesor, int vertice,
                                   ListaEnlazada<Estacion> camino) {
        if (vertice == SIN_PREDECESOR) {
            return;
        }
        reconstruirCamino(grafo, predecesor, predecesor[vertice], camino);
        camino.agregar(grafo.estacionEn(vertice));
    }
}
