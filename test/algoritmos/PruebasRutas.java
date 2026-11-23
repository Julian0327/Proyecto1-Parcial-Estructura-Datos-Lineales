package algoritmos;

import estructuras.grafo.ColaPrioridad;
import estructuras.grafo.Grafo;
import estructuras.lineales.ListaEnlazada;
import excepciones.ColaVaciaException;
import excepciones.EstacionNoExisteException;
import excepciones.RutaNoExisteException;
import java.util.Random;
import modelo.Estacion;
import modelo.ResultadoRuta;

/**
 * Pruebas de la parte P4 (grafo, cola de prioridad y algoritmos de ruta).
 * Se ejecuta con main, sin JUnit. En las pruebas aleatorias se compara contra
 * Floyd-Warshall como oráculo independiente.
 */
public class PruebasRutas {

    private static int ok;
    private static int fallos;

    public static void main(String[] args) {
        pruebasColaPrioridad();
        pruebasGrafo();
        pruebasCasosReales();
        pruebasCasosBorde();
        pruebaAleatoriaContraOraculo();
        System.out.println();
        System.out.println("Resultado: " + ok + " OK, " + fallos + " FALLOS");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------- cola
    private static void pruebasColaPrioridad() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();
        verificar("cola nueva está vacía", cola.estaVacia());
        cola.insertar("c", 30);
        cola.insertar("a", 10);
        cola.insertar("b", 20);
        verificar("prioridadMinima = 10", cola.prioridadMinima() == 10);
        verificar("sale primero 'a'", "a".equals(cola.extraerMinimo()));
        verificar("sale luego 'b'", "b".equals(cola.extraerMinimo()));
        verificar("sale al final 'c'", "c".equals(cola.extraerMinimo()));
        verificar("extraer de cola vacía lanza ColaVaciaException",
                lanza(ColaVaciaException.class, cola::extraerMinimo));
        verificar("prioridadMinima de cola vacía lanza ColaVaciaException",
                lanza(ColaVaciaException.class, cola::prioridadMinima));

        Random azar = new Random(7);
        ColaPrioridad<Integer> grande = new ColaPrioridad<>(); // fuerza crecimiento del arreglo
        for (int i = 0; i < 5000; i++) {
            int p = azar.nextInt(1000); // con repetidos
            grande.insertar(p, p);
        }
        boolean ordenada = true;
        int anterior = Integer.MIN_VALUE;
        while (!grande.estaVacia()) {
            int p = grande.prioridadMinima();
            int e = grande.extraerMinimo();
            ordenada &= (p >= anterior) && (e == p);
            anterior = p;
        }
        verificar("5000 inserciones aleatorias salen en orden no decreciente", ordenada);
    }

    // --------------------------------------------------------------- grafo
    private static void pruebasGrafo() {
        Grafo g = new Grafo();
        Estacion a = new Estacion(1, "A", "L1");
        Estacion b = new Estacion(2, "B", "L1");
        Estacion fuera = new Estacion(99, "Fuera", "L9");
        verificar("grafo nuevo está vacío", g.estaVacio());
        verificar("agregar estación nueva devuelve true", g.agregarEstacion(a));
        verificar("agregar duplicada (mismo id) devuelve false", !g.agregarEstacion(new Estacion(1, "Otra", "L2")));
        g.agregarEstacion(b);
        verificar("2 estaciones", g.cantidadEstaciones() == 2);
        g.conectar(a, b, 5);
        g.conectar(b, a, 3); // mejora el tiempo, no duplica la conexión
        g.conectar(a, b, 9); // peor tiempo: se ignora
        verificar("la conexión se cuenta una sola vez", g.cantidadConexiones() == 1);
        verificar("se conserva el menor tiempo (3)", tiempoEntre(g, a, b) == 3);
        verificar("conexión simétrica", tiempoEntre(g, b, a) == 3);
        verificar("conectar con estación inexistente lanza EstacionNoExisteException",
                lanza(EstacionNoExisteException.class, () -> g.conectar(a, fuera, 2)));
        verificar("conectar consigo misma lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> g.conectar(a, a, 2)));
        verificar("tiempo 0 lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> g.conectar(a, b, 0)));
        verificar("tiempo negativo lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> g.conectar(a, b, -4)));
        verificar("estación nula lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> g.agregarEstacion(null)));
        verificar("contiene(fuera) es false", !g.contiene(fuera));
        verificar("estacionEn fuera de rango lanza IndexOutOfBoundsException",
                lanza(IndexOutOfBoundsException.class, () -> g.estacionEn(5)));

        Grafo muchos = new Grafo(); // fuerza crecimiento del arreglo de vértices
        for (int i = 0; i < 100; i++) {
            muchos.agregarEstacion(new Estacion(i, "E" + i, "L1"));
        }
        verificar("100 estaciones registradas (crece el arreglo)", muchos.cantidadEstaciones() == 100);
        verificar("indiceDe respeta el orden de registro", muchos.indiceDe(new Estacion(57, "x", "L1")) == 57);
    }

    // ---------------------------------------------------------- casos reales
    /**
     * Red de ejemplo:
     * A -2- B -2- C -2- D   (camino corto en paradas: A-E-D = 2 paradas pero 20 min)
     * A -10- E -10- D
     */
    private static void pruebasCasosReales() {
        Grafo g = new Grafo();
        Estacion a = est(1, "A"), b = est(2, "B"), c = est(3, "C"), d = est(4, "D"), e = est(5, "E");
        for (Estacion x : new Estacion[] {a, b, c, d, e}) {
            g.agregarEstacion(x);
        }
        g.conectar(a, b, 2);
        g.conectar(b, c, 2);
        g.conectar(c, d, 2);
        g.conectar(a, e, 10);
        g.conectar(e, d, 10);

        ResultadoRuta dijkstra = new DijkstraRuta().calcular(g, a, d);
        verificar("Dijkstra A->D = 6 min", dijkstra.getMinutosTotales() == 6);
        verificar("Dijkstra A->D camino A,B,C,D", camino(dijkstra).equals("A,B,C,D"));

        ResultadoRuta bfs = new BfsRuta().calcular(g, a, d);
        verificar("BFS A->D usa menos paradas (A,E,D)", camino(bfs).equals("A,E,D"));
        verificar("BFS informa la suma real de minutos (20)", bfs.getMinutosTotales() == 20);

        verificar("Dijkstra funciona en sentido inverso D->A = 6",
                new DijkstraRuta().calcular(g, d, a).getMinutosTotales() == 6);
        verificar("nombres de algoritmos", !new BfsRuta().nombre().isEmpty() && !new DijkstraRuta().nombre().isEmpty());
    }

    // ------------------------------------------------------------ casos borde
    private static void pruebasCasosBorde() {
        Grafo g = new Grafo();
        Estacion a = est(1, "A"), b = est(2, "B"), aislada = est(3, "Aislada"), fuera = est(9, "Fuera");
        g.agregarEstacion(a);
        g.agregarEstacion(b);
        g.agregarEstacion(aislada);
        g.conectar(a, b, 4);

        for (AlgoritmoRuta alg : new AlgoritmoRuta[] {new BfsRuta(), new DijkstraRuta()}) {
            String n = alg.nombre();
            ResultadoRuta mismo = alg.calcular(g, a, a);
            verificar(n + ": origen = destino -> 0 min y 1 estación",
                    mismo.getMinutosTotales() == 0 && camino(mismo).equals("A"));
            verificar(n + ": estación aislada lanza RutaNoExisteException",
                    lanza(RutaNoExisteException.class, () -> alg.calcular(g, a, aislada)));
            verificar(n + ": origen inexistente lanza EstacionNoExisteException",
                    lanza(EstacionNoExisteException.class, () -> alg.calcular(g, fuera, a)));
            verificar(n + ": destino inexistente lanza EstacionNoExisteException",
                    lanza(EstacionNoExisteException.class, () -> alg.calcular(g, a, fuera)));
            verificar(n + ": origen nulo lanza IllegalArgumentException",
                    lanza(IllegalArgumentException.class, () -> alg.calcular(g, null, a)));
            verificar(n + ": grafo nulo lanza IllegalArgumentException",
                    lanza(IllegalArgumentException.class, () -> alg.calcular(null, a, b)));
            verificar(n + ": vecinos directos 4 min", alg.calcular(g, a, b).getMinutosTotales() == 4);
        }
        verificar("grafo vacío: estación inexistente",
                lanza(EstacionNoExisteException.class, () -> new DijkstraRuta().calcular(new Grafo(), a, b)));

        Grafo cadena = new Grafo(); // camino largo: comprueba la reconstrucción recursiva
        int n = 2000;
        for (int i = 0; i < n; i++) {
            cadena.agregarEstacion(est(i, "E" + i));
            if (i > 0) {
                cadena.conectar(est(i - 1, "E" + (i - 1)), est(i, "E" + i), 1);
            }
        }
        ResultadoRuta larga = new DijkstraRuta().calcular(cadena, est(0, "E0"), est(n - 1, "x"));
        verificar("cadena de 2000 estaciones: " + (n - 1) + " min y " + n + " paradas",
                larga.getMinutosTotales() == n - 1 && larga.getEstaciones().tamano() == n);
    }

    // ------------------------------------------------ aleatorio vs. oráculo
    private static void pruebaAleatoriaContraOraculo() {
        Random azar = new Random(2026);
        boolean dijkstraOk = true;
        boolean bfsConPesosUnoOk = true;
        boolean caminosValidos = true;
        int comparaciones = 0;
        for (int ronda = 0; ronda < 300; ronda++) {
            int n = 2 + azar.nextInt(25);
            boolean pesosUno = ronda % 2 == 0;
            Grafo g = new Grafo();
            Estacion[] es = new Estacion[n];
            for (int i = 0; i < n; i++) {
                es[i] = est(i, "E" + i);
                g.agregarEstacion(es[i]);
            }
            int[][] fw = new int[n][n];
            final int inf = 1_000_000_000;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    fw[i][j] = i == j ? 0 : inf;
                }
            }
            int aristas = azar.nextInt(n * 2);
            for (int k = 0; k < aristas; k++) {
                int i = azar.nextInt(n);
                int j = azar.nextInt(n);
                if (i == j) {
                    continue;
                }
                int peso = pesosUno ? 1 : 1 + azar.nextInt(20);
                g.conectar(es[i], es[j], peso);
                int mejor = Math.min(peso, fw[i][j]); // igual que Grafo: conserva el menor
                fw[i][j] = mejor;
                fw[j][i] = mejor;
            }
            for (int m = 0; m < n; m++) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        fw[i][j] = Math.min(fw[i][j], fw[i][m] + fw[m][j]);
                    }
                }
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    comparaciones++;
                    boolean alcanzable = fw[i][j] < inf;
                    int obtenidoDijkstra = minutosOExcepcion(new DijkstraRuta(), g, es[i], es[j]);
                    dijkstraOk &= alcanzable ? obtenidoDijkstra == fw[i][j] : obtenidoDijkstra == -1;
                    if (pesosUno) {
                        int obtenidoBfs = minutosOExcepcion(new BfsRuta(), g, es[i], es[j]);
                        bfsConPesosUnoOk &= alcanzable ? obtenidoBfs == fw[i][j] : obtenidoBfs == -1;
                    }
                    if (alcanzable) {
                        caminosValidos &= caminoEsValido(g, new DijkstraRuta().calcular(g, es[i], es[j]), es[i], es[j]);
                    }
                }
            }
        }
        System.out.println("  (" + comparaciones + " pares comparados contra Floyd-Warshall)");
        verificar("Dijkstra coincide con Floyd-Warshall (pesos 1-20)", dijkstraOk);
        verificar("BFS coincide con Floyd-Warshall cuando todos los pesos son 1", bfsConPesosUnoOk);
        verificar("el camino devuelto empieza en origen, termina en destino y suma los minutos", caminosValidos);
    }

    // ------------------------------------------------------------- auxiliares
    private static Estacion est(int id, String nombre) {
        return new Estacion(id, nombre, "L1");
    }

    private static String camino(ResultadoRuta r) {
        StringBuilder s = new StringBuilder();
        for (Estacion e : r.getEstaciones()) {
            if (s.length() > 0) {
                s.append(',');
            }
            s.append(e.getNombre());
        }
        return s.toString();
    }

    private static int tiempoEntre(Grafo g, Estacion a, Estacion b) {
        int destino = g.indiceDe(b);
        for (estructuras.grafo.Arista ar : g.vecinosDe(g.indiceDe(a))) {
            if (ar.getDestino() == destino) {
                return ar.getMinutos();
            }
        }
        return -1;
    }

    private static int minutosOExcepcion(AlgoritmoRuta alg, Grafo g, Estacion o, Estacion d) {
        try {
            return alg.calcular(g, o, d).getMinutosTotales();
        } catch (RutaNoExisteException ex) {
            return -1;
        }
    }

    /** Verifica que cada par consecutivo del camino esté conectado y que la suma coincida. */
    private static boolean caminoEsValido(Grafo g, ResultadoRuta r, Estacion origen, Estacion destino) {
        ListaEnlazada<Estacion> lista = r.getEstaciones();
        Estacion previa = null;
        int suma = 0;
        boolean primero = true;
        Estacion ultima = null;
        for (Estacion actual : lista) {
            if (primero) {
                if (!actual.equals(origen)) {
                    return false;
                }
                primero = false;
            } else {
                int t = tiempoEntre(g, previa, actual);
                if (t < 0) {
                    return false;
                }
                suma += t;
            }
            previa = actual;
            ultima = actual;
        }
        return ultima != null && ultima.equals(destino) && suma == r.getMinutosTotales();
    }

    private static boolean lanza(Class<? extends Throwable> tipo, Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (Throwable t) {
            return tipo.isInstance(t);
        }
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("  OK     " + descripcion);
        } else {
            fallos++;
            System.out.println("  FALLO  " + descripcion);
        }
    }
}
