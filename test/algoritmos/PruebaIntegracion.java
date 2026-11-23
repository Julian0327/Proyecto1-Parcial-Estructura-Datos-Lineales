package algoritmos;

import estructuras.grafo.Grafo;
import modelo.Estacion;
import modelo.EstacionTransbordo;
import modelo.LineaMetro;
import modelo.ResultadoRuta;

/** Integra P1 (modelo), P2 (ListaEnlazada) y P4 (grafo + algoritmos). */
public class PruebaIntegracion {
    public static void main(String[] args) {
        LineaMetro l1 = new LineaMetro("L1", "Linea Roja");
        LineaMetro l2 = new LineaMetro("L2", "Linea Azul");
        Estacion norte = new Estacion(1, "Norte", "L1");
        Estacion centro = new EstacionTransbordo(2, "Centro", "L1", "L2");
        Estacion sur = new Estacion(3, "Sur", "L1");
        Estacion aeropuerto = new Estacion(4, "Aeropuerto", "L2");
        l1.agregarEstacion(norte); l1.agregarEstacion(centro); l1.agregarEstacion(sur);
        l2.agregarEstacion(centro); l2.agregarEstacion(aeropuerto);

        Grafo grafo = new Grafo();
        for (LineaMetro linea : new LineaMetro[] {l1, l2}) {
            Estacion previa = null;
            for (Estacion e : linea.getEstaciones()) {
                grafo.agregarEstacion(e);
                if (previa != null) {
                    grafo.conectar(previa, e, 3);
                }
                previa = e;
            }
        }
        AlgoritmoRuta algoritmo = new DijkstraRuta();
        ResultadoRuta r = algoritmo.calcular(grafo, norte, aeropuerto);
        System.out.println(algoritmo.nombre() + ": " + r);
        System.out.println("Estaciones: " + grafo.cantidadEstaciones() + ", conexiones: " + grafo.cantidadConexiones());
        System.out.print(grafo);
        if (r.getMinutosTotales() != 6 || r.getEstaciones().tamano() != 3) {
            throw new AssertionError("Integración fallida");
        }
        System.out.println("INTEGRACION OK");
    }
}
