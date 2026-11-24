package Servicio;

import estructuras.grafo.Arista;
import estructuras.grafo.Grafo;
import EstructurasLineales.ListaEnlazada;
import Excepciones.ExcepcionMetro;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import Modelo.Estacion;
import Modelo.EstacionTransbordo;
import Modelo.LineaMetro;

/**
 * Guarda y carga la red del metro en un archivo de texto (.txt, UTF-8).
 *
 * <p>
 * Formato, un registro por línea y campos separados por {@code ;}:
 * 
 * <pre>
 * ESTACION;id;nombre;codigoLinea;lineaConexion   (lineaConexion vacía = estación normal)
 * CONEXION;idA;idB;minutos
 * LINEA;codigo;nombre;id1,id2,id3                 (ids de las estaciones, en orden)
 * </pre>
 * 
 * Las líneas que empiezan por {@code #} y las vacías se ignoran. Las
 * estaciones deben aparecer antes de las conexiones y de las líneas.
 */
public final class PersistenciaGrafo {

    private static final String SEPARADOR = ";";
    private static final String ESTACION = "ESTACION";
    private static final String CONEXION = "CONEXION";
    private static final String LINEA = "LINEA";

    private PersistenciaGrafo() {
    }

    /** Guarda solo el grafo (estaciones y conexiones). */
    public static void guardar(Grafo grafo, String ruta) {
        guardar(grafo, null, ruta);
    }

    /**
     * Guarda el grafo y, si no es null, también las líneas.
     *
     * @throws ExcepcionMetro si no se puede escribir el archivo
     */
    public static void guardar(Grafo grafo, Iterable<LineaMetro> lineas, String ruta) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo.");
        }
        validarRuta(ruta);
        try (BufferedWriter escritor = Files.newBufferedWriter(Paths.get(ruta), StandardCharsets.UTF_8)) {
            escribirLinea(escritor, "# Red del metro - generado por PersistenciaGrafo");
            escribirEstaciones(escritor, grafo);
            escribirConexiones(escritor, grafo);
            if (lineas != null) {
                escribirLineas(escritor, lineas);
            }
        } catch (IOException e) {
            throw new ExcepcionMetro("No se pudo guardar la red en '" + ruta + "': " + e.getMessage(), e);
        }
    }

    /** Carga solo el grafo; los registros LINEA se leen pero se descartan. */
    public static Grafo cargar(String ruta) {
        return cargar(ruta, new ListaEnlazada<LineaMetro>());
    }

    /**
     * Carga el grafo desde un archivo y deja las líneas leídas en
     * {@code lineasDestino}. Si el archivo es inválido se lanza la excepción
     * antes de modificar nada del sistema.
     *
     * @throws ExcepcionMetro si el archivo no existe o su contenido no es válido
     */
    public static Grafo cargar(String ruta, ListaEnlazada<LineaMetro> lineasDestino) {
        validarRuta(ruta);
        if (lineasDestino == null) {
            throw new IllegalArgumentException("La lista de líneas destino no puede ser nula.");
        }
        Grafo grafo = new Grafo();
        try (BufferedReader lector = Files.newBufferedReader(Paths.get(ruta), StandardCharsets.UTF_8)) {
            int numero = 0;
            String linea = lector.readLine();
            while (linea != null) {
                numero++;
                procesarLinea(linea.trim(), numero, grafo, lineasDestino);
                linea = lector.readLine();
            }
        } catch (NoSuchFileException e) {
            throw new ExcepcionMetro("No se pudo leer la red: el archivo '" + ruta + "' no existe.", e);
        } catch (IOException e) {
            throw new ExcepcionMetro("No se pudo leer la red de '" + ruta + "': " + e.getMessage(), e);
        }
        return grafo;
    }

    // ------------------------------------------------------------ escritura

    private static void escribirEstaciones(BufferedWriter escritor, Grafo grafo) throws IOException {
        for (int i = 0; i < grafo.cantidadEstaciones(); i++) {
            Estacion estacion = grafo.estacionEn(i);
            String conexion = "";
            if (estacion instanceof EstacionTransbordo) {
                conexion = textoSeguro(((EstacionTransbordo) estacion).getLineaConexion());
            }
            escribirLinea(escritor, ESTACION + SEPARADOR + estacion.getId() + SEPARADOR
                    + textoSeguro(estacion.getNombre()) + SEPARADOR
                    + textoSeguro(estacion.getCodigoLinea()) + SEPARADOR + conexion);
        }
    }

    /** Cada conexión se escribe una sola vez (índice menor primero). */
    private static void escribirConexiones(BufferedWriter escritor, Grafo grafo) throws IOException {
        for (int i = 0; i < grafo.cantidadEstaciones(); i++) {
            for (Arista arista : grafo.vecinosDe(i)) {
                if (arista.getDestino() > i) {
                    escribirLinea(escritor, CONEXION + SEPARADOR + grafo.estacionEn(i).getId()
                            + SEPARADOR + grafo.estacionEn(arista.getDestino()).getId()
                            + SEPARADOR + arista.getMinutos());
                }
            }
        }
    }

    private static void escribirLineas(BufferedWriter escritor, Iterable<LineaMetro> lineas) throws IOException {
        for (LineaMetro linea : lineas) {
            StringBuilder ids = new StringBuilder();
            for (Estacion estacion : linea.getEstaciones()) {
                if (ids.length() > 0) {
                    ids.append(',');
                }
                ids.append(estacion.getId());
            }
            escribirLinea(escritor, LINEA + SEPARADOR + textoSeguro(linea.getCodigoLinea())
                    + SEPARADOR + textoSeguro(linea.getNombre()) + SEPARADOR + ids);
        }
    }

    private static void escribirLinea(BufferedWriter escritor, String texto) throws IOException {
        escritor.write(texto);
        escritor.newLine();
    }

    /**
     * El texto no puede contener el separador ni saltos de línea: rompería el
     * formato.
     */
    private static String textoSeguro(String texto) {
        if (texto == null) {
            return "";
        }
        if (texto.contains(SEPARADOR) || texto.contains("\n") || texto.contains("\r")) {
            throw new IllegalArgumentException(
                    "El texto '" + texto + "' contiene caracteres no permitidos para guardar (; o salto de línea).");
        }
        return texto;
    }

    // -------------------------------------------------------------- lectura

    private static void procesarLinea(String linea, int numero, Grafo grafo,
            ListaEnlazada<LineaMetro> lineas) {
        if (linea.isEmpty() || linea.startsWith("#")) {
            return;
        }
        String[] campos = linea.split(SEPARADOR, -1);
        try {
            switch (campos[0]) {
                case ESTACION:
                    leerEstacion(campos, grafo);
                    break;
                case CONEXION:
                    leerConexion(campos, grafo);
                    break;
                case LINEA:
                    leerLinea(campos, grafo, lineas);
                    break;
                default:
                    throw new ExcepcionMetro("tipo de registro desconocido '" + campos[0] + "'");
            }
        } catch (NumberFormatException e) {
            throw new ExcepcionMetro("Archivo inválido, línea " + numero + ": número mal escrito.", e);
        } catch (IllegalArgumentException | ExcepcionMetro e) {
            throw new ExcepcionMetro("Archivo inválido, línea " + numero + ": " + e.getMessage(), e);
        }
    }

    private static void leerEstacion(String[] campos, Grafo grafo) {
        exigirCampos(campos, 5);
        int id = Integer.parseInt(campos[1].trim());
        String codigo = vacioANulo(campos[3]);
        String conexion = vacioANulo(campos[4]);
        Estacion estacion = conexion == null
                ? new Estacion(id, campos[2], codigo)
                : new EstacionTransbordo(id, campos[2], codigo, conexion);
        if (!grafo.agregarEstacion(estacion)) {
            throw new ExcepcionMetro("estación con id repetido: " + id);
        }
    }

    private static void leerConexion(String[] campos, Grafo grafo) {
        exigirCampos(campos, 4);
        Estacion a = buscarPorId(grafo, Integer.parseInt(campos[1].trim()));
        Estacion b = buscarPorId(grafo, Integer.parseInt(campos[2].trim()));
        grafo.conectar(a, b, Integer.parseInt(campos[3].trim()));
    }

    private static void leerLinea(String[] campos, Grafo grafo, ListaEnlazada<LineaMetro> lineas) {
        exigirCampos(campos, 4);
        LineaMetro linea = new LineaMetro(campos[1], campos[2]);
        String ids = campos[3].trim();
        if (!ids.isEmpty()) {
            for (String id : ids.split(",")) {
                linea.agregarEstacion(buscarPorId(grafo, Integer.parseInt(id.trim())));
            }
        }
        lineas.agregar(linea);
    }

    private static Estacion buscarPorId(Grafo grafo, int id) {
        for (int i = 0; i < grafo.cantidadEstaciones(); i++) {
            if (grafo.estacionEn(i).getId() == id) {
                return grafo.estacionEn(i);
            }
        }
        throw new ExcepcionMetro("la estación con id " + id + " no está definida");
    }

    private static void exigirCampos(String[] campos, int esperados) {
        if (campos.length != esperados) {
            throw new ExcepcionMetro("se esperaban " + esperados + " campos y hay " + campos.length);
        }
    }

    private static String vacioANulo(String texto) {
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private static void validarRuta(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
        }
    }
}