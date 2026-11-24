package ui;

import algoritmos.AlgoritmoRuta;
import EstructurasLineales.Cola;
import EstructurasLineales.ICola;
import EstructurasLineales.IPila;
import EstructurasLineales.Pila;
import Excepciones.ColaVaciaException;
import Excepciones.ExcepcionMetro;
import java.awt.GraphicsEnvironment;
import java.util.Scanner;
import Modelo.ConsultaRuta;
import Modelo.Estacion;
import Modelo.LineaMetro;
import Modelo.Pasajero;
import Modelo.ResultadoRuta;
import Servicio.DatosDemo;
import Servicio.MetroService;
import Servicio.SimuladorTurno;

/**
 * Menú de consola. Cada opción muestra un caso real y un caso borde.
 * Solo habla con {@link MetroService}; no contiene lógica de negocio.
 *
 * <p>Las opciones 4 (tabla hash) y 5 (árbol de líneas) son de la parte P3 y
 * quedan como punto de integración: {@link #opcionTablaHash()} y
 * {@link #opcionArbolLineas()}.
 */
public class MenuConsola {

    private static final String ARCHIVO_POR_DEFECTO = "metro_red.txt";
    private static final int CUPOS_POR_TREN = 3;

    private final MetroService servicio;
    private final AlgoritmoRuta[] algoritmos;
    private final Scanner entrada = new Scanner(System.in, "UTF-8");
    private boolean entradaCerrada;

    public MenuConsola(MetroService servicio, AlgoritmoRuta[] algoritmos) {
        if (servicio == null || algoritmos == null || algoritmos.length == 0) {
            throw new IllegalArgumentException("Se necesita el servicio y al menos un algoritmo.");
        }
        this.servicio = servicio;
        this.algoritmos = algoritmos;
    }

    /** Ciclo principal: muestra el menú hasta que se elija salir o se acabe la entrada. */
    public void iniciar() {
        boolean salir = false;
        while (!salir && !entradaCerrada) {
            mostrarMenu();
            String opcion = leer("Opción: ");
            if (opcion == null) {
                break;
            }
            salir = ejecutar(opcion.trim());
        }
        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("===== METRO - MENÚ PRINCIPAL =====");
        System.out.println(" 1. Lista enlazada de estaciones");
        System.out.println(" 2. Cola de pasajeros");
        System.out.println(" 3. Pila de consultas");
        System.out.println(" 4. Tabla hash (pendiente: parte P3)");
        System.out.println(" 5. Árbol de líneas (pendiente: parte P3)");
        System.out.println(" 6. Calcular ruta más corta");
        System.out.println(" 7. Simular un turno de pasajeros");
        System.out.println(" 8. Guardar la red en archivo .txt");
        System.out.println(" 9. Cargar la red desde archivo .txt");
        System.out.println("10. Ver la red (líneas y estaciones)");
        System.out.println("11. Abrir interfaz gráfica (Swing)");
        System.out.println(" 0. Salir");
    }

    /** @return true si se debe salir */
    private boolean ejecutar(String opcion) {
        try {
            switch (opcion) {
                case "1":
                    opcionListaEstaciones();
                    break;
                case "2":
                    opcionColaPasajeros();
                    break;
                case "3":
                    opcionPilaConsultas();
                    break;
                case "4":
                    opcionTablaHash();
                    break;
                case "5":
                    opcionArbolLineas();
                    break;
                case "6":
                    opcionCalcularRuta();
                    break;
                case "7":
                    opcionSimularTurno();
                    break;
                case "8":
                    opcionGuardar();
                    break;
                case "9":
                    opcionCargar();
                    break;
                case "10":
                    opcionVerRed();
                    break;
                case "11":
                    opcionAbrirVentana();
                    break;
                case "0":
                    return true;
                default:
                    System.out.println("Opción no válida. Elija un número del menú.");
            }
        } catch (ExcepcionMetro | IllegalArgumentException e) {
            System.out.println("Aviso: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------- opción 1

    private void opcionListaEstaciones() {
        System.out.println("--- Lista enlazada de estaciones ---");
        LineaMetro demo = new LineaMetro("DEMO", "Linea de prueba");
        Estacion alfa = new Estacion(901, "Alfa", "DEMO");
        Estacion beta = new Estacion(902, "Beta", "DEMO");
        Estacion gamma = new Estacion(903, "Gamma", "DEMO");
        demo.agregarEstacion(alfa);
        demo.agregarEstacion(beta);
        demo.agregarEstacion(gamma);
        System.out.println("Caso real: se agregan 3 estaciones.");
        demo.mostrarEstaciones();

        demo.insertarEstacionEn(1, new Estacion(904, "Delta", "DEMO"));
        System.out.println("Se inserta 'Delta' en la posición 1:");
        demo.mostrarEstaciones();

        System.out.println("Se elimina 'Beta': " + demo.eliminarEstacion(beta));
        demo.mostrarEstaciones();
        System.out.println("Cantidad de estaciones: " + demo.cantidadEstaciones());

        System.out.println("Caso borde: eliminar una estación que no está -> "
                + demo.eliminarEstacion(new Estacion(999, "Fantasma", "DEMO")));
        try {
            demo.insertarEstacionEn(50, new Estacion(905, "Epsilon", "DEMO"));
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caso borde: insertar en posición inválida -> " + e.getMessage());
        }
    }

    // ---------------------------------------------------------- opción 2

    private void opcionColaPasajeros() {
        System.out.println("--- Cola de pasajeros (FIFO) ---");
        Estacion anden = new Estacion(900, "Anden demo", "DEMO");
        ICola<Pasajero> cola = new Cola<>();
        cola.encolar(new Pasajero("D1", "Camila", anden));
        cola.encolar(new Pasajero("D2", "Andres", anden));
        cola.encolar(new Pasajero("D3", "Laura", anden));
        System.out.println("Caso real: llegan Camila, Andres y Laura.");
        System.out.println("Cola: " + cola);
        System.out.println("Frente: " + cola.verFrente().getNombre());
        System.out.println("Sube al tren: " + cola.desencolar().getNombre());
        System.out.println("Cola: " + cola + " (tamaño " + cola.tamano() + ")");

        cola.desencolar();
        cola.desencolar();
        System.out.println("Caso borde: se vacía el andén y se intenta subir a otro pasajero.");
        try {
            cola.desencolar();
        } catch (ColaVaciaException e) {
            System.out.println("ColaVaciaException -> " + e.getMessage());
        }
    }

    // ---------------------------------------------------------- opción 3

    private void opcionPilaConsultas() {
        System.out.println("--- Pila de consultas (LIFO) ---");
        Estacion a = new Estacion(901, "Alfa", "DEMO");
        Estacion b = new Estacion(902, "Beta", "DEMO");
        Estacion c = new Estacion(903, "Gamma", "DEMO");
        IPila<ConsultaRuta> pila = new Pila<>();
        pila.apilar(new ConsultaRuta(a, b, 5));
        pila.apilar(new ConsultaRuta(b, c, 7));
        System.out.println("Caso real: se apilan 2 consultas.");
        System.out.println("Pila: " + pila);
        System.out.println("Última consulta: " + pila.verTope());
        System.out.println("Se deshace: " + pila.desapilar());
        System.out.println("Pila: " + pila);

        pila.desapilar();
        System.out.println("Caso borde: pila vacía y se intenta deshacer.");
        try {
            pila.desapilar();
        } catch (ColaVaciaException e) {
            System.out.println("ColaVaciaException -> " + e.getMessage());
        }
    }

    // ------------------------------------------------- opciones 4 y 5 (P3)

    /** Punto de integración de P3: aquí se demuestra la tabla hash (altas, búsqueda, colisión). */
    private void opcionTablaHash() {
        System.out.println("La tabla hash es la parte P3 y todavía no está integrada.");
        System.out.println("Cuando exista, esta opción debe mostrar altas, búsqueda y colisión de claves.");
    }

    /** Punto de integración de P3: aquí se demuestra el árbol (insertar, buscar, listar en orden). */
    private void opcionArbolLineas() {
        System.out.println("El árbol de líneas es la parte P3 y todavía no está integrado.");
        System.out.println("Cuando exista, esta opción debe mostrar insertar, buscar y listar en orden.");
    }

    // ---------------------------------------------------------- opción 6

    private void opcionCalcularRuta() {
        System.out.println("--- Calcular ruta más corta ---");
        listarEstaciones();
        AlgoritmoRuta elegido = elegirAlgoritmo();
        if (elegido == null) {
            return;
        }
        String origen = leer("Estación de origen (nombre o id): ");
        String destino = leer("Estación de destino (nombre o id): ");
        if (origen == null || destino == null) {
            return;
        }
        servicio.setAlgoritmo(elegido);
        ResultadoRuta resultado = servicio.calcularRuta(origen, destino);
        System.out.println("Algoritmo: " + elegido.nombre());
        System.out.println("Ruta: " + resultado);
        System.out.println("Consultas en el historial: " + servicio.cantidadConsultas());
        System.out.println("(Si el nombre no existe o no hay camino, se informa como aviso.)");
    }

    private AlgoritmoRuta elegirAlgoritmo() {
        System.out.println("Algoritmos disponibles:");
        for (int i = 0; i < algoritmos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + algoritmos[i].nombre());
        }
        String texto = leer("Elija el algoritmo [1-" + algoritmos.length + "]: ");
        if (texto == null) {
            return null;
        }
        try {
            int indice = Integer.parseInt(texto.trim()) - 1;
            if (indice >= 0 && indice < algoritmos.length) {
                return algoritmos[indice];
            }
        } catch (NumberFormatException e) {
            // se informa abajo
        }
        System.out.println("Algoritmo no válido; se usa el primero: " + algoritmos[0].nombre());
        return algoritmos[0];
    }

    // ---------------------------------------------------------- opción 7

    private void opcionSimularTurno() {
        System.out.println("--- Simulación de turno ---");
        if (servicio.cantidadEnAnden() == 0) {
            DatosDemo.cargarPasajeros(servicio);
        }
        System.out.println("Pasajeros esperando en el andén (" + servicio.cantidadEnAnden() + "):");
        for (Pasajero pasajero : servicio.pasajerosEnAnden()) {
            System.out.println("  " + pasajero);
        }
        System.out.println();
        SimuladorTurno simulador = new SimuladorTurno(servicio, CUPOS_POR_TREN);
        System.out.println(simulador.simular());
        System.out.println();
        System.out.println("Caso borde: se vuelve a simular con el andén vacío.");
        System.out.println(simulador.simular());
    }

    // ------------------------------------------------------ opciones 8 y 9

    private void opcionGuardar() {
        String ruta = leerRuta();
        if (ruta == null) {
            return;
        }
        servicio.guardarRed(ruta);
        System.out.println("Red guardada en '" + ruta + "' (" + servicio.cantidadEstaciones()
                + " estaciones, " + servicio.cantidadConexiones() + " conexiones).");
    }

    private void opcionCargar() {
        String ruta = leerRuta();
        if (ruta == null) {
            return;
        }
        servicio.cargarRed(ruta);
        System.out.println("Red cargada desde '" + ruta + "' (" + servicio.cantidadEstaciones()
                + " estaciones, " + servicio.cantidadConexiones() + " conexiones).");
        System.out.println("Se reiniciaron los pasajeros, el andén y el historial.");
    }

    private String leerRuta() {
        String texto = leer("Archivo [" + ARCHIVO_POR_DEFECTO + "]: ");
        if (texto == null) {
            return null;
        }
        return texto.trim().isEmpty() ? ARCHIVO_POR_DEFECTO : texto.trim();
    }

    // ------------------------------------------------------ opciones 10 y 11

    private void opcionVerRed() {
        System.out.println("--- Red del metro ---");
        System.out.println(servicio.cantidadLineas() + " líneas, " + servicio.cantidadEstaciones()
                + " estaciones, " + servicio.cantidadConexiones() + " conexiones.");
        for (LineaMetro linea : servicio.lineas()) {
            linea.mostrarEstaciones();
        }
    }

    private void opcionAbrirVentana() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Este equipo no tiene entorno gráfico: use el menú de consola.");
            return;
        }
        VentanaMetro.mostrar(servicio, algoritmos);
        System.out.println("Interfaz gráfica abierta. Puede seguir usando este menú.");
    }

    // ---------------------------------------------------------- utilidades

    private void listarEstaciones() {
        System.out.println("Estaciones de la red:");
        for (Estacion estacion : servicio.estaciones()) {
            System.out.println("  " + estacion.getId() + ". " + estacion);
        }
    }

    /** Lee una línea; devuelve null (y marca la entrada como cerrada) si no hay más datos. */
    private String leer(String mensaje) {
        System.out.print(mensaje);
        if (!entrada.hasNextLine()) {
            entradaCerrada = true;
            System.out.println();
            return null;
        }
        return entrada.nextLine();
    }
}
