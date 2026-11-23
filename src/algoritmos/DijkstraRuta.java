package algoritmos;

import estructuras.grafo.Arista;
import estructuras.grafo.ColaPrioridad;
import estructuras.grafo.Grafo;

/**
 * Algoritmo de Dijkstra: encuentra la ruta de <b>menor tiempo total</b>
 * cuando los tiempos entre estaciones son distintos. Requiere tiempos
 * positivos, garantizado por {@link Arista}.
 *
 * <p>Usa una {@link ColaPrioridad} propia. En vez de "disminuir clave" se
 * insertan entradas repetidas y se descartan las obsoletas al extraerlas.
 * Complejidad: O((V + A) log V).
 */
public class DijkstraRuta extends AlgoritmoRutaBase {

    @Override
    protected void explorar(Grafo grafo, int origen, int destino,
                            int[] distancia, int[] predecesor) {
        ColaPrioridad<Integer> pendientes = new ColaPrioridad<>();
        pendientes.insertar(origen, 0);

        while (!pendientes.estaVacia()) {
            int distanciaEntrada = pendientes.prioridadMinima();
            int actual = pendientes.extraerMinimo();
            if (distanciaEntrada > distancia[actual]) {
                continue; // entrada obsoleta: ya se halló un camino mejor
            }
            if (actual == destino) {
                return;
            }
            relajarVecinos(grafo, actual, distancia, predecesor, pendientes);
        }
    }

    /** Mejora la distancia de cada vecino si pasar por {@code actual} es más rápido. */
    private void relajarVecinos(Grafo grafo, int actual, int[] distancia, int[] predecesor,
                                ColaPrioridad<Integer> pendientes) {
        for (Arista arista : grafo.vecinosDe(actual)) {
            int vecino = arista.getDestino();
            int nuevaDistancia = distancia[actual] + arista.getMinutos();
            if (nuevaDistancia < distancia[vecino]) {
                distancia[vecino] = nuevaDistancia;
                predecesor[vecino] = actual;
                pendientes.insertar(vecino, nuevaDistancia);
            }
        }
    }

    @Override
    public String nombre() {
        return "Dijkstra (menor tiempo)";
    }
}
