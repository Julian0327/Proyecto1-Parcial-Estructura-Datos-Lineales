package algoritmos;

import estructuras.grafo.Grafo;
import modelo.Estacion;
import modelo.ResultadoRuta;

/**
 * Contrato de cualquier algoritmo de ruta (principio abierto/cerrado e
 * inversión de dependencias): {@code MetroService} depende de esta interfaz
 * y puede cambiar de algoritmo sin modificarse.
 */
public interface AlgoritmoRuta {

    /**
     * Calcula la ruta entre dos estaciones del grafo.
     *
     * @return el camino completo (origen y destino incluidos) y su tiempo
     * @throws excepciones.EstacionNoExisteException si alguna estación no está en el grafo
     * @throws excepciones.RutaNoExisteException     si no hay camino entre ellas
     * @throws IllegalArgumentException              si algún argumento es nulo
     */
    ResultadoRuta calcular(Grafo grafo, Estacion origen, Estacion destino);

    /** Nombre legible del algoritmo, para mostrar en la interfaz. */
    String nombre();
}
