package Modelo;

import estructuras.lineales.ListaEnlazada;

/** Resultado de calcular una ruta: el camino y el tiempo total. */
public class ResultadoRuta {

    private final ListaEnlazada<Estacion> estaciones;
    private final int minutosTotales;

    public ResultadoRuta(ListaEnlazada<Estacion> estaciones, int minutosTotales) {
        this.estaciones = estaciones;
        this.minutosTotales = minutosTotales;
    }

    public ListaEnlazada<Estacion> getEstaciones() {
        return estaciones;
    }

    public int getMinutosTotales() {
        return minutosTotales;
    }

    @Override
    public String toString() {
        StringBuilder camino = new StringBuilder();
        for (Estacion estacion : estaciones) {
            if (camino.length() > 0) {
                camino.append(" -> ");
            }
            camino.append(estacion.getNombre());
        }
        return camino + " | Tiempo total: " + minutosTotales + " min";
    }
}