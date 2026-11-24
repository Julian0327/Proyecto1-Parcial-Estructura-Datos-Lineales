package Modelo;

/** Clase abstracta que representa a cualquier persona del sistema. */
public abstract class Persona {

    private final String id;
    private String nombre;

    protected Persona(String id, String nombre) {
        validarTexto(id, "El id");
        validarTexto(nombre, "El nombre");
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        validarTexto(nombre, "El nombre");
        this.nombre = nombre;
    }

    /** Cada tipo de persona describe su propio rol (polimorfismo). */
    public abstract String descripcion();

    private static void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
    }

    @Override
    public String toString() {
        return descripcion();
    }
}