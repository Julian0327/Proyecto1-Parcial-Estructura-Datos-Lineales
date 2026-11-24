package Servicio;

import Excepciones.ExcepcionMetro;
import Modelo.Estacion;
import Modelo.EstacionTransbordo;
import Modelo.LineaMetro;
import Modelo.Pasajero;

/**
 * Datos de ejemplo para probar el sistema sin escribir nada a mano.
 *
 * <p>Red de 4 líneas y 12 estaciones, con transbordos en Centro (L1 y L2) y
 * Plaza Mayor (L1 y L3). Hay un tramo directo lento Norte-Plaza Mayor (20 min)
 * para que BFS (menos paradas) y Dijkstra (menos minutos) den rutas distintas,
 * y la estación Cochera queda aislada para probar "ruta inexistente".
 */
public final class DatosDemo {

    private static final String[] NOMBRES_PASAJEROS = {"Ana", "Luis", "Marta", "Pedro", "Sofia", "Diego"};

    private DatosDemo() {
    }

    /** Carga las líneas, estaciones y conexiones de ejemplo en el servicio. */
    public static void cargar(MetroService servicio) {
        Estacion norte = new Estacion(1, "Norte", "L1");
        Estacion universidad = new Estacion(2, "Universidad", "L1");
        Estacion centro = new EstacionTransbordo(3, "Centro", "L1", "L2");
        Estacion plazaMayor = new EstacionTransbordo(4, "Plaza Mayor", "L1", "L3");
        Estacion sur = new Estacion(5, "Sur", "L1");

        Estacion aeropuerto = new Estacion(6, "Aeropuerto", "L2");
        Estacion terminal = new Estacion(7, "Terminal", "L2");
        Estacion estadio = new Estacion(8, "Estadio", "L2");
        Estacion parque = new Estacion(9, "Parque", "L2");

        Estacion mercado = new Estacion(10, "Mercado", "L3");
        Estacion biblioteca = new Estacion(11, "Biblioteca", "L3");

        Estacion cochera = new Estacion(12, "Cochera", "L4");

        LineaMetro roja = new LineaMetro("L1", "Linea Roja");
        roja.agregarEstacion(norte);
        roja.agregarEstacion(universidad);
        roja.agregarEstacion(centro);
        roja.agregarEstacion(plazaMayor);
        roja.agregarEstacion(sur);

        LineaMetro azul = new LineaMetro("L2", "Linea Azul");
        azul.agregarEstacion(aeropuerto);
        azul.agregarEstacion(terminal);
        azul.agregarEstacion(centro);
        azul.agregarEstacion(estadio);
        azul.agregarEstacion(parque);

        LineaMetro verde = new LineaMetro("L3", "Linea Verde");
        verde.agregarEstacion(mercado);
        verde.agregarEstacion(plazaMayor);
        verde.agregarEstacion(biblioteca);

        LineaMetro naranja = new LineaMetro("L4", "Linea Naranja (en construccion)");
        naranja.agregarEstacion(cochera);

        servicio.registrarLinea(roja);
        servicio.registrarLinea(azul);
        servicio.registrarLinea(verde);
        servicio.registrarLinea(naranja);

        servicio.conectar(norte, universidad, 4);
        servicio.conectar(universidad, centro, 5);
        servicio.conectar(centro, plazaMayor, 3);
        servicio.conectar(plazaMayor, sur, 6);
        servicio.conectar(norte, plazaMayor, 20); // tramo directo pero lento

        servicio.conectar(aeropuerto, terminal, 7);
        servicio.conectar(terminal, centro, 6);
        servicio.conectar(centro, estadio, 4);
        servicio.conectar(estadio, parque, 5);

        servicio.conectar(mercado, plazaMayor, 2);
        servicio.conectar(plazaMayor, biblioteca, 3);
    }

    /**
     * Registra 6 pasajeros de ejemplo con origen y destino distintos (tomados
     * de las estaciones de la red) y los pone a esperar en el andén. Si un
     * pasajero ya existe se reutiliza.
     *
     * @throws ExcepcionMetro si la red tiene menos de 2 estaciones
     */
    public static void cargarPasajeros(MetroService servicio) {
        Estacion[] estaciones = servicio.estaciones();
        int n = estaciones.length;
        if (n < 2) {
            throw new ExcepcionMetro("Se necesitan al menos 2 estaciones para crear pasajeros de ejemplo.");
        }
        for (int i = 0; i < NOMBRES_PASAJEROS.length; i++) {
            int indiceOrigen = (i * 3) % n;
            int indiceDestino = (i * 5 + 4) % n;
            if (indiceDestino == indiceOrigen) {
                indiceDestino = (indiceOrigen + 1) % n;
            }
            String id = String.format("P%03d", i + 1);
            Pasajero pasajero = servicio.buscarPasajero(id);
            if (pasajero == null) {
                pasajero = servicio.registrarPasajero(id, NOMBRES_PASAJEROS[i], estaciones[indiceOrigen].getNombre());
            }
            pasajero.setDestino(estaciones[indiceDestino]);
            if (!servicio.estaEnAnden(pasajero)) {
                servicio.esperarEnAnden(pasajero);
            }
        }
    }
}
