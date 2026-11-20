package Modelo;

/** Estación del metro. Dos estaciones son iguales si tienen el mismo id. */
public class Estacion {

    private final int id;
    private final String nombre;
    private final String codigoLinea;

    public Estacion(int id, String nombre, String codigoLinea) {
        if (id < 0) {
            throw new IllegalArgumentException("El id de la estación no puede ser negativo.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la estación no puede estar vacío.");
        }
        this.id = id;
        this.nombre = nombre;
        this.codigoLinea = codigoLinea;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoLinea() {
        return codigoLinea;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return id == ((Estacion) otro).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return nombre + " (" + codigoLinea + ")";
    }
}