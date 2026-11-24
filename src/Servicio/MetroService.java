package Servicio;

import algoritmos.AlgoritmoRuta;
import estructuras.grafo.Arista;
import estructuras.grafo.Grafo;
import EstructurasArbol.ArbolBinario;
import EstructurasArbol.IArbol;
import EstructurasHash.IMapa;
import EstructurasHash.TablaHash;
import EstructurasLineales.Cola;
import EstructurasLineales.ICola;
import EstructurasLineales.IPila;
import EstructurasLineales.ListaEnlazada;
import EstructurasLineales.Pila;
import Excepciones.EstacionNoExisteException;
import Excepciones.ExcepcionMetro;
import Modelo.ConsultaRuta;
import Modelo.Estacion;
import Modelo.EstacionTransbordo;
import Modelo.LineaMetro;
import Modelo.Pasajero;
import Modelo.ResultadoRuta;

/**
 * Fachada de la lógica del metro. La interfaz (consola o Swing) habla solo con
 * esta clase.
 *
 * <p>
 * Depende de la interfaz {@link AlgoritmoRuta} (inyección de dependencias):
 * se puede cambiar de BFS a Dijkstra con {@link #setAlgoritmo} sin tocar nada
 * más. Une todas las estructuras del proyecto: grafo (red), árbol binario de
 * búsqueda (líneas por código), tabla hash (pasajeros por id), cola (andén) y
 * pila (historial).
 */
public class MetroService {

    private AlgoritmoRuta algoritmo;
    private Grafo grafo = new Grafo();
    private IArbol<String, LineaMetro> lineas = new ArbolBinario<>();
    private IMapa<String, Pasajero> pasajeros = new TablaHash<>();
    private ICola<Pasajero> anden = new Cola<>();
    private IPila<ConsultaRuta> historial = new Pila<>();

    public MetroService(AlgoritmoRuta algoritmo) {
        setAlgoritmo(algoritmo);
    }

    // ---------------------------------------------------------- algoritmo

    public final void setAlgoritmo(AlgoritmoRuta algoritmo) {
        if (algoritmo == null) {
            throw new IllegalArgumentException("El algoritmo de ruta no puede ser nulo.");
        }
        this.algoritmo = algoritmo;
    }

    public AlgoritmoRuta getAlgoritmo() {
        return algoritmo;
    }

    // ------------------------------------------------------ red y líneas

    /**
     * Registra una línea y agrega sus estaciones al grafo (las que ya estaban,
     * como los transbordos, no se duplican). Las conexiones se crean aparte
     * con {@link #conectar}.
     *
     * @throws ExcepcionMetro si ya existe una línea con ese código
     */
    public void registrarLinea(LineaMetro linea) {
        if (linea == null) {
            throw new IllegalArgumentException("La línea no puede ser nula.");
        }
        String clave = normalizarCodigo(linea.getCodigoLinea());
        if (clave.isEmpty()) {
            throw new IllegalArgumentException("El código de la línea no puede estar vacío.");
        }
        if (lineas.contiene(clave)) {
            throw new ExcepcionMetro("Ya existe una línea con el código '" + linea.getCodigoLinea() + "'.");
        }
        lineas.insertar(clave, linea);
        for (Estacion estacion : linea.getEstaciones()) {
            grafo.agregarEstacion(estacion);
        }
    }

    /**
     * Conecta dos estaciones en ambos sentidos con su tiempo de viaje.
     *
     * @throws EstacionNoExisteException si alguna no está registrada
     */
    public void conectar(Estacion a, Estacion b, int minutos) {
        grafo.conectar(a, b, minutos);
    }

    /** Devuelve la línea con ese código o null si no existe. O(log n) promedio. */
    public LineaMetro buscarLinea(String codigo) {
        if (codigo == null) {
            return null;
        }
        return lineas.buscar(normalizarCodigo(codigo));
    }

    /** Líneas registradas en orden alfabético de código (recorrido inorden). */
    public Iterable<LineaMetro> lineas() {
        return lineas.valoresEnOrden();
    }

    public int cantidadLineas() {
        return lineas.tamano();
    }

    public int alturaArbolLineas() {
        return lineas.altura();
    }

    public int cantidadEstaciones() {
        return grafo.cantidadEstaciones();
    }

    public int cantidadConexiones() {
        return grafo.cantidadConexiones();
    }

    public int indiceDe(Estacion estacion) {
        return grafo.indiceDe(estacion);
    }

    public Estacion estacionEn(int indice) {
        return grafo.estacionEn(indice);
    }

    public Iterable<Arista> vecinosDe(int indice) {
        return grafo.vecinosDe(indice);
    }

    /** Copia de las estaciones de la red en orden de registro. */
    public Estacion[] estaciones() {
        Estacion[] copia = new Estacion[grafo.cantidadEstaciones()];
        for (int i = 0; i < copia.length; i++) {
            copia[i] = grafo.estacionEn(i);
        }
        return copia;
    }

    /**
     * Busca una estación por nombre (sin distinguir mayúsculas) o por id.
     *
     * @throws EstacionNoExisteException si no hay ninguna que coincida
     */
    public Estacion buscarEstacion(String nombreOId) {
        String texto = nombreOId == null ? "" : nombreOId.trim();
        for (int i = 0; i < grafo.cantidadEstaciones(); i++) {
            Estacion estacion = grafo.estacionEn(i);
            if (estacion.getNombre().equalsIgnoreCase(texto)
                    || String.valueOf(estacion.getId()).equals(texto)) {
                return estacion;
            }
        }
        throw new EstacionNoExisteException(texto);
    }

    // ------------------------------------------------------------- rutas

    /**
     * Calcula la ruta entre dos estaciones dadas por nombre o id y la apila
     * en el historial.
     */
    public ResultadoRuta calcularRuta(String origen, String destino) {
        return calcularRuta(buscarEstacion(origen), buscarEstacion(destino));
    }

    /**
     * Calcula la ruta con el algoritmo actual. Solo las consultas exitosas
     * quedan en el historial.
     *
     * @throws EstacionNoExisteException         si alguna estación no está en la
     *                                           red
     * @throws excepciones.RutaNoExisteException si no hay camino entre ellas
     */
    public ResultadoRuta calcularRuta(Estacion origen, Estacion destino) {
        ResultadoRuta resultado = algoritmo.calcular(grafo, origen, destino);
        historial.apilar(new ConsultaRuta(origen, destino, resultado.getMinutosTotales()));
        return resultado;
    }

    // --------------------------------------------------------- historial

    /** @throws excepciones.PilaVaciaException si no hay consultas */
    public ConsultaRuta ultimaConsulta() {
        return historial.verCima();
    }

    /**
     * Quita la última consulta del historial y la devuelve.
     *
     * @throws excepciones.PilaVaciaException si no hay consultas
     */
    public ConsultaRuta deshacerUltimaConsulta() {
        return historial.desapilar();
    }

    /** Consultas de la más reciente a la más antigua (solo lectura). */
    public Iterable<ConsultaRuta> historial() {
        return historial;
    }

    public int cantidadConsultas() {
        return historial.tamano();
    }

    // --------------------------------------------------------- pasajeros

    /**
     * Registra un pasajero ubicado en una estación de la red.
     *
     * @throws ExcepcionMetro            si el id ya está registrado
     * @throws EstacionNoExisteException si la estación no existe
     */
    public Pasajero registrarPasajero(String id, String nombre, String estacionActual) {
        Estacion estacion = buscarEstacion(estacionActual);
        if (pasajeros.contiene(id)) {
            throw new ExcepcionMetro("Ya existe un pasajero con el id '" + id + "'.");
        }
        Pasajero pasajero = new Pasajero(id, nombre, estacion);
        pasajeros.insertar(pasajero.getId(), pasajero);
        return pasajero;
    }

    /** Devuelve el pasajero con ese id o null si no existe. O(1) promedio. */
    public Pasajero buscarPasajero(String id) {
        if (id == null) {
            return null;
        }
        return pasajeros.obtener(id);
    }

    public Iterable<Pasajero> pasajeros() {
        return pasajeros.valores();
    }

    public int cantidadPasajeros() {
        return pasajeros.tamano();
    }

    public int cantidadColisionesPasajeros() {
        return pasajeros instanceof TablaHash
                ? ((TablaHash<?, ?>) pasajeros).cantidadColisiones()
                : 0;
    }

    // ------------------------------------------------------------- andén

    /**
     * El pasajero se pone al final de la cola del andén.
     *
     * @throws ExcepcionMetro si ya estaba esperando
     */
    public void esperarEnAnden(Pasajero pasajero) {
        if (pasajero == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        if (estaEnAnden(pasajero)) {
            throw new ExcepcionMetro("El pasajero '" + pasajero.getNombre() + "' ya está esperando en el andén.");
        }
        anden.encolar(pasajero);
    }

    public boolean estaEnAnden(Pasajero pasajero) {
        for (Pasajero esperando : anden) {
            if (esperando.equals(pasajero)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saca al primer pasajero de la cola (FIFO).
     *
     * @throws excepciones.ColaVaciaException si el andén está vacío
     */
    public Pasajero abordarSiguiente() {
        return anden.desencolar();
    }

    /** Pasajeros esperando, del primero al último (solo lectura). */
    public Iterable<Pasajero> pasajerosEnAnden() {
        return anden;
    }

    public int cantidadEnAnden() {
        return anden.tamano();
    }

    // ------------------------------------------------------- persistencia

    /** Guarda la red (estaciones, conexiones y líneas) en un archivo .txt. */
    public void guardarRed(String ruta) {
        PersistenciaGrafo.guardar(grafo, lineas(), ruta);
    }

    /**
     * Reemplaza la red por la del archivo. Si el archivo es inválido se
     * lanza la excepción y el sistema queda como estaba. Al cargar, los
     * pasajeros, el andén y el historial se reinician porque se referían a la
     * red anterior.
     */
    public void cargarRed(String ruta) {
        ListaEnlazada<LineaMetro> lineasNuevas = new ListaEnlazada<>();
        Grafo grafoNuevo = PersistenciaGrafo.cargar(ruta, lineasNuevas);
        reconstruirLineasFaltantes(grafoNuevo, lineasNuevas);
        IArbol<String, LineaMetro> arbolNuevo = new ArbolBinario<>();
        for (LineaMetro linea : lineasNuevas) {
            arbolNuevo.insertar(normalizarCodigo(linea.getCodigoLinea()), linea);
        }
        grafo = grafoNuevo;
        lineas = arbolNuevo;
        pasajeros = new TablaHash<>();
        anden = new Cola<>();
        historial = new Pila<>();
    }

    private static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    /**
     * Si el archivo no traía registros LINEA (solo grafo), se arman las
     * líneas a partir del código de cada estación.
     */
    private void reconstruirLineasFaltantes(Grafo grafoNuevo, ListaEnlazada<LineaMetro> destino) {
        if (!destino.estaVacia()) {
            return;
        }
        for (int i = 0; i < grafoNuevo.cantidadEstaciones(); i++) {
            Estacion estacion = grafoNuevo.estacionEn(i);
            agregarALinea(destino, estacion.getCodigoLinea(), estacion);
            if (estacion instanceof EstacionTransbordo) {
                agregarALinea(destino, ((EstacionTransbordo) estacion).getLineaConexion(), estacion);
            }
        }
    }

    private static void agregarALinea(ListaEnlazada<LineaMetro> destino, String codigo, Estacion estacion) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return;
        }
        for (LineaMetro linea : destino) {
            if (linea.getCodigoLinea().equals(codigo)) {
                linea.agregarEstacion(estacion);
                return;
            }
        }
        LineaMetro nueva = new LineaMetro(codigo, "Linea " + codigo);
        nueva.agregarEstacion(estacion);
        destino.agregar(nueva);
    }
}