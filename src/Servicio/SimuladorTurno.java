package Servicio;

import Excepciones.ExcepcionMetro;
import Modelo.Estacion;
import Modelo.Pasajero;
import Modelo.ResultadoRuta;

/**
 * Simula un turno: los pasajeros del andén suben a los trenes en orden de
 * llegada (cola FIFO), con un máximo de cupos por tren. Cada pasajero viaja
 * por la ruta que calcula el {@link MetroService}.
 *
 * <p>Un pasajero sin destino, o cuya ruta no existe, no aborda: se informa
 * como incidencia y el turno continúa con el siguiente.
 */
public class SimuladorTurno {

    private final MetroService servicio;
    private final int cuposPorTren;
    private int viajesRealizados;
    private int minutosTotales;
    private int incidencias;
    private int trenes;

    public SimuladorTurno(MetroService servicio, int cuposPorTren) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        if (cuposPorTren <= 0) {
            throw new IllegalArgumentException("Los cupos por tren deben ser mayores que cero.");
        }
        this.servicio = servicio;
        this.cuposPorTren = cuposPorTren;
    }

    /**
     * Atiende a todos los pasajeros que esperan y devuelve el informe del turno.
     * Si el andén está vacío no hay nada que simular y se informa.
     */
    public String simular() {
        viajesRealizados = 0;
        minutosTotales = 0;
        incidencias = 0;
        trenes = 0;
        if (servicio.cantidadEnAnden() == 0) {
            return "El andén está vacío: no hay pasajeros para simular el turno.";
        }
        StringBuilder informe = new StringBuilder();
        while (servicio.cantidadEnAnden() > 0) {
            trenes++;
            informe.append("Tren ").append(trenes).append(" (").append(cuposPorTren)
                    .append(" cupos):").append(System.lineSeparator());
            for (int cupo = 0; cupo < cuposPorTren && servicio.cantidadEnAnden() > 0; cupo++) {
                informe.append(atender(servicio.abordarSiguiente())).append(System.lineSeparator());
            }
        }
        informe.append("Resumen: ").append(viajesRealizados).append(" viajes, ")
                .append(minutosTotales).append(" min en total, ")
                .append(incidencias).append(" incidencias, ")
                .append(trenes).append(" trenes.");
        return informe.toString();
    }

    private String atender(Pasajero pasajero) {
        if (!pasajero.tieneDestino()) {
            incidencias++;
            return "  - " + pasajero.getNombre() + ": no tiene destino, no aborda.";
        }
        Estacion destino = pasajero.getDestino();
        try {
            ResultadoRuta ruta = servicio.calcularRuta(pasajero.getEstacionActual(), destino);
            pasajero.registrarViaje(destino);
            viajesRealizados++;
            minutosTotales += ruta.getMinutosTotales();
            return "  - " + pasajero.getNombre() + ": " + ruta;
        } catch (ExcepcionMetro e) {
            incidencias++;
            return "  - " + pasajero.getNombre() + ": no pudo viajar (" + e.getMessage() + ")";
        }
    }

    public int getViajesRealizados() {
        return viajesRealizados;
    }

    public int getMinutosTotales() {
        return minutosTotales;
    }

    public int getIncidencias() {
        return incidencias;
    }

    public int getTrenes() {
        return trenes;
    }
}
