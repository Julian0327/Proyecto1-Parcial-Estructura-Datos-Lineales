package Modelo;

/**
 * Pasajero del metro: sabe dónde está, a dónde va y cuántos viajes ha hecho.
 */
public class Pasajero extends Persona {

    private Estacion estacionActual;
    private Estacion destino;
    private int viajes;

    public Pasajero(String id, String nombre, Estacion estacionActual) {
        super(id, nombre);
        this.estacionActual = estacionActual;
        this.viajes = 0;
    }

    public Estacion getEstacionActual() {
        return estacionActual;
    }

    public Estacion getDestino() {
        return destino;
    }

    public void setDestino(Estacion destino) {
        this.destino = destino;
    }

    public boolean tieneDestino() {
        return destino != null;
    }

    public int getViajes() {
        return viajes;
    }

    /** Registra que el pasajero subió al tren y llegó a su destino. */
    public void registrarViaje(Estacion destino) {
        this.estacionActual = destino;
        this.viajes++;
    }

    @Override
    public String descripcion() {
        return "Pasajero " + getNombre() + " [" + getId() + "] en "
                + estacionActual + " - viajes: " + viajes;
    }
}