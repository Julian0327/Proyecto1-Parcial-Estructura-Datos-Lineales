package modelo;

import estructuras.lineales.ListaEnlazada;

/**
 * Línea del metro: un código, un nombre y sus estaciones en orden de recorrido.
 */
public class LineaMetro {

    private final String codigoLinea;
    private final String nombre;
    private final ListaEnlazada<Estacion> estaciones;

    public LineaMetro(String codigoLinea, String nombre) {
        this.codigoLinea = codigoLinea;
        this.nombre = nombre;
        this.estaciones = new ListaEnlazada<>();
    }

    public String getCodigoLinea() {
        return codigoLinea;
    }

    public String getNombre() {
        return nombre;
    }

    public ListaEnlazada<Estacion> getEstaciones() {
        return estaciones;
    }

    public void agregarEstacion(Estacion estacion) {
        estaciones.agregar(estacion);
    }

    public void insertarEstacionEn(int posicion, Estacion estacion) {
        estaciones.insertarEn(posicion, estacion);
    }

    public boolean eliminarEstacion(Estacion estacion) {
        return estaciones.eliminar(estacion);
    }

    public int cantidadEstaciones() {
        return estaciones.tamano();
    }

    /** Busca por id recorriendo la lista. Devuelve null si no existe. O(n). */
    public Estacion buscarEstacionPorId(int id) {
        for (Estacion estacion : estaciones) {
            if (estacion.getId() == id) {
                return estacion;
            }
        }
        return null;
    }

    public void mostrarEstaciones() {
        System.out.println("Línea " + codigoLinea + " - " + nombre);
        for (Estacion estacion : estaciones) {
            System.out.println("  - " + estacion);
        }
    }

    /** Devuelve las estaciones ordenadas alfabéticamente sin alterar la línea. */
    public Estacion[] estacionesOrdenadasPorNombre() {
        Estacion[] copia = new Estacion[estaciones.tamano()];
        int posicion = 0;
        for (Estacion estacion : estaciones) {
            copia[posicion++] = estacion;
        }
        OrdenadorEstaciones.ordenarPorNombre(copia);
        return copia;
    }

    @Override
    public String toString() {
        return codigoLinea + " - " + nombre;
    }
}