package Modelo;

/** Estación que permite cambiar de una línea a otra. */
public class EstacionTransbordo extends Estacion {

    private final String lineaConexion;

    public EstacionTransbordo(int id, String nombre, String codigoLinea, String lineaConexion) {
        super(id, nombre, codigoLinea);
        this.lineaConexion = lineaConexion;
    }

    public String getLineaConexion() {
        return lineaConexion;
    }

    @Override
    public String toString() {
        return getNombre() + " (" + getCodigoLinea() + " <-> " + lineaConexion + ")";
    }
}