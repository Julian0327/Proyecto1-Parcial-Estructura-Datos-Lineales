package algoritmos;

import estructuras.grafo.Arista;
import estructuras.grafo.Grafo;

/**
 * Búsqueda en anchura: encuentra la ruta con <b>menos paradas</b>. Equivale
 * a la ruta más corta solo cuando todos los tiempos son iguales (peso 1); si
 * los tiempos difieren, usar {@link DijkstraRuta}. El tiempo informado es la
 * suma real de minutos del camino encontrado.
 *
 * <p>Complejidad: O(V + A). Usa una cola de arreglo: cada vértice entra una
 * sola vez, así que no hace falta una cola circular.
 */
public class BfsRuta extends AlgoritmoRutaBase {

    @Override
    protected void explorar(Grafo grafo, int origen, int destino,
                            int[] distancia, int[] predecesor) {
        int[] cola = new int[grafo.cantidadEstaciones()];
        int frente = 0;
        int ultimo = 0;
        cola[ultimo++] = origen;

        while (frente < ultimo) {
            int actual = cola[frente++];
            if (actual == destino) {
                return;
            }
            for (Arista arista : grafo.vecinosDe(actual)) {
                int vecino = arista.getDestino();
                if (distancia[vecino] == INFINITO) {
                    distancia[vecino] = distancia[actual] + arista.getMinutos();
                    predecesor[vecino] = actual;
                    cola[ultimo++] = vecino;
                }
            }
        }
    }

    @Override
    public String nombre() {
        return "BFS (menos paradas)";
    }
}
