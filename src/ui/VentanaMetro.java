package ui;

import algoritmos.AlgoritmoRuta;
import estructuras.grafo.Arista;
import EstructurasLineales.ListaEnlazada;
import Excepciones.ExcepcionMetro;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import Modelo.ConsultaRuta;
import Modelo.Estacion;
import Modelo.EstacionTransbordo;
import Modelo.LineaMetro;
import Modelo.Pasajero;
import Modelo.ResultadoRuta;
import Servicio.DatosDemo;
import Servicio.MetroService;
import Servicio.SimuladorTurno;

/**
 * Interfaz gráfica moderna (Swing) del Sistema de Metro.
 * Incluye mapa interactivo 2D de la red con resaltado de rutas, tabla de
 * estaciones, historial de consultas (pila) y simulador de andén (cola + hash).
 *
 * <p>
 * Usa exclusivamente arreglos y las estructuras propias del proyecto.
 */
public class VentanaMetro {

    // Paleta de colores moderna
    private static final Color COLOR_HEADER = new Color(15, 23, 42);
    private static final Color COLOR_FONDO = new Color(241, 245, 249);
    private static final Color COLOR_TARJETA = Color.WHITE;
    private static final Color COLOR_BORDE = new Color(203, 213, 225);
    private static final Color COLOR_TEXTO = new Color(30, 41, 59);
    private static final Color COLOR_TEXTO_SUAVE = new Color(100, 116, 139);
    private static final Color COLOR_PRIMARIO = new Color(37, 99, 235);
    private static final Color COLOR_EXITO = new Color(16, 185, 129);
    private static final Color COLOR_ACENTO = new Color(245, 158, 11);
    private static final Color COLOR_PELIGRO = new Color(239, 68, 68);

    private static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FUENTE_NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FUENTE_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private final MetroService servicio;
    private final AlgoritmoRuta[] algoritmos;

    private final JFrame marco = new JFrame("Sistema de Metro — Red, Rutas y Simulación");
    private final JLabel etiquetaStats = new JLabel();

    // Pestaña Rutas y Mapa
    private final JComboBox<Estacion> comboOrigen = new JComboBox<>();
    private final JComboBox<Estacion> comboDestino = new JComboBox<>();
    private final JComboBox<String> comboAlgoritmo;
    private final PanelMapaMetro panelMapa = new PanelMapaMetro();
    private final JTextArea areaResultado = new JTextArea();
    private final JTextArea areaHistorial = new JTextArea();

    // Pestaña Estaciones y Líneas
    private final JTable tablaEstaciones = new JTable();
    private final JTextArea areaArbolLineas = new JTextArea();

    // Pestaña Andén y Pasajeros
    private final JTextField campoPasajeroId = new JTextField(6);
    private final JTextField campoPasajeroNombre = new JTextField(12);
    private final JComboBox<Estacion> comboPasajeroOrigen = new JComboBox<>();
    private final JComboBox<Estacion> comboPasajeroDestino = new JComboBox<>();
    private final JTextField campoBuscarPasajero = new JTextField(8);
    private final JTable tablaAnden = new JTable();
    private final JTextArea areaSimulacion = new JTextArea();

    private ResultadoRuta rutaActual;

    private VentanaMetro(MetroService servicio, AlgoritmoRuta[] algoritmos) {
        this.servicio = servicio;
        this.algoritmos = algoritmos;
        String[] nombres = new String[algoritmos.length];
        for (int i = 0; i < algoritmos.length; i++) {
            nombres[i] = algoritmos[i].nombre();
        }
        this.comboAlgoritmo = new JComboBox<>(new ModeloCombo<>(nombres));
        construir();
        refrescar();
    }

    /** Abre la ventana en el hilo de Swing (EDT). */
    public static void mostrar(MetroService servicio, AlgoritmoRuta[] algoritmos) {
        if (servicio == null || algoritmos == null || algoritmos.length == 0) {
            throw new IllegalArgumentException("Se necesita el servicio y al menos un algoritmo.");
        }
        SwingUtilities.invokeLater(() -> {
            aplicarLookAndFeel();
            new VentanaMetro(servicio, algoritmos).marco.setVisible(true);
        });
    }

    private static void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception e) {
            // Usa el Look & Feel por defecto si Nimbus no está disponible
        }
    }

    // -------------------------------------------------------- construcción

    private void construir() {
        marco.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        marco.getContentPane().setBackground(COLOR_FONDO);
        marco.setLayout(new BorderLayout(0, 0));

        marco.add(crearHeader(), BorderLayout.NORTH);
        marco.add(crearPestanas(), BorderLayout.CENTER);

        marco.setSize(1180, 740);
        marco.setMinimumSize(new Dimension(960, 620));
        marco.setLocationRelativeTo(null);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(COLOR_HEADER);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izquierda.setOpaque(false);

        JLabel titulo = new JLabel("Sistema de Metro");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        etiquetaStats.setFont(new Font("SansSerif", Font.PLAIN, 12));
        etiquetaStats.setForeground(new Color(148, 163, 184));

        izquierda.add(titulo);
        izquierda.add(etiquetaStats);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        JButton btnGuardar = crearBoton("Guardar red", new Color(51, 65, 85), Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());
        JButton btnCargar = crearBoton("Cargar red", new Color(51, 65, 85), Color.WHITE);
        btnCargar.addActionListener(e -> cargar());
        derecha.add(btnGuardar);
        derecha.add(btnCargar);

        header.add(izquierda, BorderLayout.WEST);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(FUENTE_SUBTITULO);
        pestanas.setBackground(COLOR_FONDO);
        pestanas.setBorder(new EmptyBorder(8, 12, 12, 12));

        pestanas.addTab("Rutas y Mapa", crearPestanaRutas());
        pestanas.addTab("Estaciones y Líneas (Árbol)", crearPestanaEstaciones());
        pestanas.addTab("Andén y Simulación (Cola y Hash)", crearPestanaAnden());
        return pestanas;
    }

    // --------------------------------------------------- pestaña 1: rutas

    private JPanel crearPestanaRutas() {
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBackground(COLOR_FONDO);
        raiz.setBorder(new EmptyBorder(8, 4, 4, 4));

        // Barra superior de controles
        JPanel controles = crearTarjeta();
        controles.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controles.add(etiquetaCampo("Origen:"));
        comboOrigen.setFont(FUENTE_NORMAL);
        controles.add(comboOrigen);

        controles.add(etiquetaCampo("Destino:"));
        comboDestino.setFont(FUENTE_NORMAL);
        controles.add(comboDestino);

        controles.add(etiquetaCampo("Algoritmo:"));
        comboAlgoritmo.setFont(FUENTE_NORMAL);
        controles.add(comboAlgoritmo);

        JButton btnCalcular = crearBoton("Calcular ruta", COLOR_PRIMARIO, Color.WHITE);
        btnCalcular.addActionListener(e -> calcular());
        controles.add(btnCalcular);

        JButton btnDeshacer = crearBoton("Deshacer última", new Color(100, 116, 139), Color.WHITE);
        btnDeshacer.addActionListener(e -> deshacer());
        controles.add(btnDeshacer);

        // Panel central: Mapa a la izquierda, Resultado + Historial a la derecha
        JPanel tarjetaMapa = crearTarjeta();
        tarjetaMapa.setLayout(new BorderLayout(4, 4));
        JLabel tituloMapa = new JLabel("Mapa de la Red (Grafo)");
        tituloMapa.setFont(FUENTE_SUBTITULO);
        tituloMapa.setForeground(COLOR_TEXTO);
        tituloMapa.setBorder(new EmptyBorder(4, 6, 4, 6));
        tarjetaMapa.add(tituloMapa, BorderLayout.NORTH);
        tarjetaMapa.add(panelMapa, BorderLayout.CENTER);

        configurarAreaTexto(areaResultado, 13);
        configurarAreaTexto(areaHistorial, 12);

        JPanel tarjetaResultado = envolverConTitulo("Resultado de la Ruta", new JScrollPane(areaResultado));
        JPanel tarjetaHistorial = envolverConTitulo("Historial de Consultas (Pila LIFO)",
                new JScrollPane(areaHistorial));

        JSplitPane derecha = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tarjetaResultado, tarjetaHistorial);
        derecha.setResizeWeight(0.55);
        derecha.setBorder(null);
        derecha.setOpaque(false);

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tarjetaMapa, derecha);
        centro.setResizeWeight(0.64);
        centro.setBorder(null);
        centro.setOpaque(false);

        raiz.add(controles, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);
        return raiz;
    }

    // ------------------------------------------------- pestaña 2: estaciones

    private JPanel crearPestanaEstaciones() {
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBackground(COLOR_FONDO);
        raiz.setBorder(new EmptyBorder(8, 4, 4, 4));

        tablaEstaciones.setRowHeight(28);
        tablaEstaciones.setFont(FUENTE_NORMAL);
        tablaEstaciones.getTableHeader().setFont(FUENTE_SUBTITULO);
        tablaEstaciones.setGridColor(new Color(226, 232, 240));
        tablaEstaciones.setShowVerticalLines(false);
        tablaEstaciones.setDefaultRenderer(Object.class, new RenderizadorEstaciones());

        configurarAreaTexto(areaArbolLineas, 13);

        JPanel izq = envolverConTitulo("Estaciones Registradas", new JScrollPane(tablaEstaciones));
        JPanel der = envolverConTitulo("Líneas en Orden (Árbol Binario - Inorden)", new JScrollPane(areaArbolLineas));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izq, der);
        split.setResizeWeight(0.62);
        split.setBorder(null);
        split.setOpaque(false);

        raiz.add(split, BorderLayout.CENTER);
        return raiz;
    }

    // ----------------------------------------------------- pestaña 3: andén

    private JPanel crearPestanaAnden() {
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBackground(COLOR_FONDO);
        raiz.setBorder(new EmptyBorder(8, 4, 4, 4));

        // Panel superior: Registro de pasajero + acciones de simulación
        JPanel superior = new JPanel(new GridLayout(2, 1, 6, 6));
        superior.setOpaque(false);

        JPanel filaRegistro = crearTarjeta();
        filaRegistro.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filaRegistro.add(etiquetaCampo("ID:"));
        filaRegistro.add(campoPasajeroId);
        filaRegistro.add(etiquetaCampo("Nombre:"));
        filaRegistro.add(campoPasajeroNombre);
        filaRegistro.add(etiquetaCampo("Origen:"));
        filaRegistro.add(comboPasajeroOrigen);
        filaRegistro.add(etiquetaCampo("Destino:"));
        filaRegistro.add(comboPasajeroDestino);

        JButton btnAgregarPasajero = crearBoton("Agregar al andén", COLOR_PRIMARIO, Color.WHITE);
        btnAgregarPasajero.addActionListener(e -> registrarYEncolarPasajero());
        filaRegistro.add(btnAgregarPasajero);

        JPanel filaSimulacion = crearTarjeta();
        filaSimulacion.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));

        JButton btnDemoPasajeros = crearBoton("Cargar pasajeros demo", new Color(100, 116, 139), Color.WHITE);
        btnDemoPasajeros.addActionListener(e -> cargarPasajerosDemo());
        filaSimulacion.add(btnDemoPasajeros);

        JButton btnAbordar = crearBoton("Abordar siguiente (FIFO)", COLOR_ACENTO, Color.WHITE);
        btnAbordar.addActionListener(e -> abordarSiguiente());
        filaSimulacion.add(btnAbordar);

        JButton btnSimular = crearBoton("Simular turno completo", COLOR_EXITO, Color.WHITE);
        btnSimular.addActionListener(e -> simularTurnoCompleto());
        filaSimulacion.add(btnSimular);

        filaSimulacion.add(etiquetaCampo("   Buscar ID (Hash):"));
        filaSimulacion.add(campoBuscarPasajero);
        JButton btnBuscarHash = crearBoton("Buscar", new Color(51, 65, 85), Color.WHITE);
        btnBuscarHash.addActionListener(e -> buscarPasajeroHash());
        filaSimulacion.add(btnBuscarHash);

        superior.add(filaRegistro);
        superior.add(filaSimulacion);

        tablaAnden.setRowHeight(26);
        tablaAnden.setFont(FUENTE_NORMAL);
        tablaAnden.getTableHeader().setFont(FUENTE_SUBTITULO);
        tablaAnden.setShowVerticalLines(false);

        configurarAreaTexto(areaSimulacion, 12);

        JPanel izq = envolverConTitulo("Cola del Andén (FIFO)", new JScrollPane(tablaAnden));
        JPanel der = envolverConTitulo("Registro de Simulación y Búsqueda Hash", new JScrollPane(areaSimulacion));

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izq, der);
        centro.setResizeWeight(0.48);
        centro.setBorder(null);
        centro.setOpaque(false);

        raiz.add(superior, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);
        return raiz;
    }

    // ------------------------------------------------------------ acciones

    private void calcular() {
        Estacion origen = (Estacion) comboOrigen.getSelectedItem();
        Estacion destino = (Estacion) comboDestino.getSelectedItem();
        if (origen == null || destino == null) {
            avisar("La red no tiene estaciones. Cargue una red primero.");
            return;
        }
        try {
            servicio.setAlgoritmo(algoritmos[Math.max(0, comboAlgoritmo.getSelectedIndex())]);
            ResultadoRuta ruta = servicio.calcularRuta(origen, destino);
            this.rutaActual = ruta;
            panelMapa.repaint();
            areaResultado.setText(
                    "Algoritmo : " + servicio.getAlgoritmo().nombre() + "\n"
                            + "Origen    : " + origen + "\n"
                            + "Destino   : " + destino + "\n"
                            + "Paradas   : " + ruta.getEstaciones().tamano() + " estaciones\n"
                            + "Tiempo    : " + ruta.getMinutosTotales() + " min\n\n"
                            + "Recorrido:\n  " + describirCamino(ruta));
            actualizarHistorial();
        } catch (ExcepcionMetro e) {
            this.rutaActual = null;
            panelMapa.repaint();
            areaResultado.setText("No se pudo calcular la ruta:\n" + e.getMessage());
        }
    }

    private static String describirCamino(ResultadoRuta ruta) {
        StringBuilder camino = new StringBuilder();
        for (Estacion estacion : ruta.getEstaciones()) {
            if (camino.length() > 0) {
                camino.append(" -> ");
            }
            camino.append(estacion.getNombre());
        }
        return camino.toString();
    }

    private void deshacer() {
        try {
            ConsultaRuta quitada = servicio.deshacerUltimaConsulta();
            areaResultado.setText("Consulta deshecha del historial:\n  " + quitada);
            actualizarHistorial();
        } catch (ExcepcionMetro e) {
            avisar(e.getMessage());
        }
    }

    private void guardar() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new java.io.File("metro_red.txt"));
        if (selector.showSaveDialog(marco) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            servicio.guardarRed(selector.getSelectedFile().getPath());
            areaResultado.setText("Red guardada en:\n" + selector.getSelectedFile().getPath());
        } catch (ExcepcionMetro | IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void cargar() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(marco) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            servicio.cargarRed(selector.getSelectedFile().getPath());
            this.rutaActual = null;
            refrescar();
            areaResultado.setText("Red cargada: " + servicio.cantidadEstaciones() + " estaciones, "
                    + servicio.cantidadConexiones() + " conexiones.\n"
                    + "Se reiniciaron los pasajeros, el andén y el historial.");
        } catch (ExcepcionMetro | IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void registrarYEncolarPasajero() {
        String id = campoPasajeroId.getText().trim();
        String nombre = campoPasajeroNombre.getText().trim();
        Estacion origen = (Estacion) comboPasajeroOrigen.getSelectedItem();
        Estacion destino = (Estacion) comboPasajeroDestino.getSelectedItem();
        if (origen == null || destino == null) {
            avisar("No hay estaciones disponibles.");
            return;
        }
        try {
            Pasajero pasajero = servicio.buscarPasajero(id);
            if (pasajero == null) {
                pasajero = servicio.registrarPasajero(id, nombre, origen.getNombre());
            }
            pasajero.setDestino(destino);
            servicio.esperarEnAnden(pasajero);
            campoPasajeroId.setText("");
            campoPasajeroNombre.setText("");
            areaSimulacion.append("Encolado: " + pasajero.getNombre() + " [" + pasajero.getId()
                    + "] -> destino " + destino.getNombre() + "\n");
            actualizarAnden();
        } catch (ExcepcionMetro | IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void cargarPasajerosDemo() {
        try {
            DatosDemo.cargarPasajeros(servicio);
            areaSimulacion.append("Se cargaron pasajeros demo en el andén ("
                    + servicio.cantidadEnAnden() + " en cola).\n");
            actualizarAnden();
        } catch (ExcepcionMetro e) {
            avisar(e.getMessage());
        }
    }

    private void abordarSiguiente() {
        try {
            Pasajero p = servicio.abordarSiguiente();
            areaSimulacion.append("Abordó (FIFO): " + p + "\n");
            actualizarAnden();
        } catch (ExcepcionMetro e) {
            avisar(e.getMessage());
        }
    }

    private void simularTurnoCompleto() {
        SimuladorTurno simulador = new SimuladorTurno(servicio, 3);
        String informe = simulador.simular();
        areaSimulacion.append("\n" + informe + "\n\n");
        actualizarAnden();
        actualizarHistorial();
    }

    private void buscarPasajeroHash() {
        String id = campoBuscarPasajero.getText().trim();
        if (id.isEmpty()) {
            avisar("Escriba el ID del pasajero a buscar.");
            return;
        }
        Pasajero encontrado = servicio.buscarPasajero(id);
        if (encontrado == null) {
            areaSimulacion.append("Búsqueda Hash ['" + id + "']: no encontrado.\n");
        } else {
            areaSimulacion.append("Búsqueda Hash ['" + id + "']: " + encontrado
                    + " (destino: " + (encontrado.tieneDestino() ? encontrado.getDestino().getNombre() : "sin asignar")
                    + ")\n");
        }
    }

    // ------------------------------------------------------------ refresco

    private void refrescar() {
        Estacion[] estaciones = servicio.estaciones();
        comboOrigen.setModel(new ModeloCombo<>(estaciones));
        comboDestino.setModel(new ModeloCombo<>(estaciones));
        comboPasajeroOrigen.setModel(new ModeloCombo<>(estaciones));
        comboPasajeroDestino.setModel(new ModeloCombo<>(estaciones));
        if (estaciones.length > 1) {
            comboDestino.setSelectedIndex(estaciones.length / 2);
            comboPasajeroDestino.setSelectedIndex(estaciones.length - 1);
        }
        tablaEstaciones.setModel(new ModeloEstaciones(estaciones));
        if (tablaEstaciones.getColumnModel().getColumnCount() >= 4) {
            tablaEstaciones.getColumnModel().getColumn(0).setMaxWidth(60);
            tablaEstaciones.getColumnModel().getColumn(2).setMaxWidth(90);
        }
        actualizarStats();
        actualizarArbolLineas();
        actualizarHistorial();
        actualizarAnden();
        panelMapa.repaint();
    }

    private void actualizarStats() {
        etiquetaStats.setText("  |  " + servicio.cantidadLineas() + " Líneas   ·   "
                + servicio.cantidadEstaciones() + " Estaciones   ·   "
                + servicio.cantidadConexiones() + " Conexiones");
    }

    private void actualizarArbolLineas() {
        StringBuilder texto = new StringBuilder();
        texto.append("Altura del Árbol Binario: ").append(servicio.alturaArbolLineas()).append("\n");
        texto.append("Total de líneas: ").append(servicio.cantidadLineas()).append("\n\n");
        for (LineaMetro linea : servicio.lineas()) {
            texto.append("■ ").append(linea).append(" (").append(linea.cantidadEstaciones()).append(" estaciones)\n");
            for (Estacion est : linea.getEstaciones()) {
                texto.append("    • ").append(est).append("\n");
            }
            texto.append("\n");
        }
        areaArbolLineas.setText(texto.toString());
        areaArbolLineas.setCaretPosition(0);
    }

    private void actualizarHistorial() {
        StringBuilder texto = new StringBuilder();
        int numero = servicio.cantidadConsultas();
        for (ConsultaRuta consulta : servicio.historial()) {
            texto.append(numero--).append(". ").append(consulta).append('\n');
        }
        areaHistorial.setText(texto.length() == 0 ? "(sin consultas)" : texto.toString());
        areaHistorial.setCaretPosition(0);
    }

    private void actualizarAnden() {
        Pasajero[] enAnden = new Pasajero[servicio.cantidadEnAnden()];
        int i = 0;
        for (Pasajero p : servicio.pasajerosEnAnden()) {
            enAnden[i++] = p;
        }
        tablaAnden.setModel(new ModeloAnden(enAnden));
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(marco, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // --------------------------------------------------- helpers de diseño

    private static JPanel crearTarjeta() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                new EmptyBorder(8, 10, 8, 10)));
        return panel;
    }

    private static JPanel envolverConTitulo(String titulo, Component contenido) {
        JPanel tarjeta = crearTarjeta();
        tarjeta.setLayout(new BorderLayout(0, 6));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(FUENTE_SUBTITULO);
        lbl.setForeground(COLOR_TEXTO);
        lbl.setBorder(new EmptyBorder(2, 2, 4, 2));
        tarjeta.add(lbl, BorderLayout.NORTH);
        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }

    private static JLabel etiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXTO);
        return lbl;
    }

    private static JButton crearBoton(String texto, Color fondo, Color textoColor) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(textoColor);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private static void configurarAreaTexto(JTextArea area, int tamanoFuente) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(FUENTE_MONO.deriveFont((float) tamanoFuente));
        area.setForeground(COLOR_TEXTO);
        area.setBackground(new Color(248, 250, 252));
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
    }

    private static Color colorPorLinea(String codigo) {
        if (codigo == null) {
            return new Color(100, 116, 139);
        }
        switch (codigo.trim().toUpperCase()) {
            case "L1":
                return COLOR_PELIGRO;
            case "L2":
                return COLOR_PRIMARIO;
            case "L3":
                return COLOR_EXITO;
            case "L4":
                return COLOR_ACENTO;
            default:
                return new Color(99, 102, 241);
        }
    }

    // --------------------------------------------------- mapa 2D de la red

    private final class PanelMapaMetro extends JPanel {
        private static final long serialVersionUID = 1L;

        private PanelMapaMetro() {
            setBackground(new Color(248, 250, 252));
            setPreferredSize(new Dimension(620, 460));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int n = servicio.cantidadEstaciones();
            if (n == 0) {
                g2.setColor(COLOR_TEXTO_SUAVE);
                g2.setFont(FUENTE_NORMAL);
                g2.drawString("No hay estaciones cargadas en la red.", 30, 40);
                g2.dispose();
                return;
            }

            int ancho = getWidth();
            int alto = getHeight();
            int[] posX = new int[n];
            int[] posY = new int[n];
            calcularPosiciones(n, ancho, alto, posX, posY);

            boolean[] enRuta = new boolean[n];
            int[] rutaIndices = extraerIndicesRuta(enRuta);

            // 1. Dibujar todas las conexiones base
            dibujarAristasBase(g2, n, posX, posY, rutaIndices != null);

            // 2. Resaltar la ruta calculada
            if (rutaIndices != null && rutaIndices.length > 1) {
                dibujarRutaResaltada(g2, posX, posY, rutaIndices);
            }

            // 3. Dibujar nodos (estaciones) y etiquetas
            dibujarEstaciones(g2, n, posX, posY, enRuta, rutaIndices);

            // 4. Leyenda
            dibujarLeyenda(g2, alto);

            g2.dispose();
        }

        private void calcularPosiciones(int n, int ancho, int alto, int[] posX, int[] posY) {
            int margenX = 75;
            int margenY = 55;
            int utilW = Math.max(200, ancho - 2 * margenX);
            int utilH = Math.max(180, alto - 2 * margenY - 25);

            for (int i = 0; i < n; i++) {
                Estacion est = servicio.estacionEn(i);
                double[] norm = coordenadasNormalizadas(est.getNombre());
                if (norm != null) {
                    posX[i] = margenX + (int) (norm[0] * utilW);
                    posY[i] = margenY + (int) (norm[1] * utilH);
                } else {
                    double angulo = (2 * Math.PI * i) / n - Math.PI / 2;
                    posX[i] = ancho / 2 + (int) (Math.cos(angulo) * (utilW * 0.4));
                    posY[i] = alto / 2 - 10 + (int) (Math.sin(angulo) * (utilH * 0.4));
                }
            }
        }

        /**
         * Coordenadas limpias para el grafo demo; devuelve null para redes
         * personalizadas.
         */
        private double[] coordenadasNormalizadas(String nombre) {
            switch (nombre) {
                case "Norte":
                    return new double[] { 0.12, 0.14 };
                case "Universidad":
                    return new double[] { 0.30, 0.30 };
                case "Centro":
                    return new double[] { 0.50, 0.46 };
                case "Plaza Mayor":
                    return new double[] { 0.68, 0.62 };
                case "Sur":
                    return new double[] { 0.86, 0.80 };
                case "Aeropuerto":
                    return new double[] { 0.12, 0.80 };
                case "Terminal":
                    return new double[] { 0.30, 0.64 };
                case "Estadio":
                    return new double[] { 0.68, 0.30 };
                case "Parque":
                    return new double[] { 0.86, 0.14 };
                case "Mercado":
                    return new double[] { 0.46, 0.85 };
                case "Biblioteca":
                    return new double[] { 0.88, 0.52 };
                case "Cochera":
                    return new double[] { 0.12, 0.46 };
                default:
                    return null;
            }
        }

        private int[] extraerIndicesRuta(boolean[] enRuta) {
            if (rutaActual == null) {
                return null;
            }
            ListaEnlazada<Estacion> lista = rutaActual.getEstaciones();
            int[] indices = new int[lista.tamano()];
            int k = 0;
            for (Estacion est : lista) {
                int idx = servicio.indiceDe(est);
                indices[k++] = idx;
                enRuta[idx] = true;
            }
            return indices;
        }

        private void dibujarAristasBase(Graphics2D g2, int n, int[] posX, int[] posY, boolean atenuar) {
            Stroke trazoBase = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));

            for (int i = 0; i < n; i++) {
                for (Arista arista : servicio.vecinosDe(i)) {
                    int j = arista.getDestino();
                    if (j <= i) {
                        continue;
                    }
                    g2.setStroke(trazoBase);
                    g2.setColor(atenuar ? new Color(203, 213, 225) : new Color(148, 163, 184));
                    g2.drawLine(posX[i], posY[i], posX[j], posY[j]);

                    // Etiqueta de minutos en el punto medio
                    int mx = (posX[i] + posX[j]) / 2;
                    int my = (posY[i] + posY[j]) / 2;
                    String txt = arista.getMinutos() + "m";
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(txt);
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(mx - tw / 2 - 4, my - 9, tw + 8, 16, 6, 6);
                    g2.setColor(COLOR_TEXTO_SUAVE);
                    g2.drawString(txt, mx - tw / 2, my + 3);
                }
            }
        }

        private void dibujarRutaResaltada(Graphics2D g2, int[] posX, int[] posY, int[] rutaIndices) {
            // Halo exterior
            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(250, 204, 21, 110));
            for (int k = 0; k < rutaIndices.length - 1; k++) {
                int a = rutaIndices[k];
                int b = rutaIndices[k + 1];
                g2.drawLine(posX[a], posY[a], posX[b], posY[b]);
            }

            // Línea principal de ruta
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(234, 88, 12));
            for (int k = 0; k < rutaIndices.length - 1; k++) {
                int a = rutaIndices[k];
                int b = rutaIndices[k + 1];
                g2.drawLine(posX[a], posY[a], posX[b], posY[b]);
            }
        }

        private void dibujarEstaciones(Graphics2D g2, int n, int[] posX, int[] posY,
                boolean[] enRuta, int[] rutaIndices) {
            int radio = 11;
            int origenIdx = rutaIndices != null && rutaIndices.length > 0 ? rutaIndices[0] : -1;
            int destinoIdx = rutaIndices != null && rutaIndices.length > 0 ? rutaIndices[rutaIndices.length - 1] : -1;

            for (int i = 0; i < n; i++) {
                Estacion est = servicio.estacionEn(i);
                Color colorLinea = colorPorLinea(est.getCodigoLinea());
                int x = posX[i];
                int y = posY[i];

                if (enRuta[i]) {
                    g2.setColor(new Color(250, 204, 21));
                    g2.fillOval(x - radio - 5, y - radio - 5, (radio + 5) * 2, (radio + 5) * 2);
                }

                if (est instanceof EstacionTransbordo) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - radio - 2, y - radio - 2, (radio + 2) * 2, (radio + 2) * 2);
                    g2.setColor(COLOR_HEADER);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawOval(x - radio - 2, y - radio - 2, (radio + 2) * 2, (radio + 2) * 2);
                }

                g2.setColor(colorLinea);
                g2.fillOval(x - radio, y - radio, radio * 2, radio * 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x - radio, y - radio, radio * 2, radio * 2);

                // Nombre de la estación
                g2.setFont(new Font("SansSerif", enRuta[i] ? Font.BOLD : Font.PLAIN, 12));
                String etiqueta = est.getNombre();
                if (i == origenIdx) {
                    etiqueta = "▶ " + etiqueta;
                } else if (i == destinoIdx && destinoIdx != origenIdx) {
                    etiqueta = "★ " + etiqueta;
                }
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(etiqueta);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(x - tw / 2 - 4, y + radio + 4, tw + 8, 17, 6, 6);
                g2.setColor(COLOR_TEXTO);
                g2.drawString(etiqueta, x - tw / 2, y + radio + 17);
            }
        }

        private void dibujarLeyenda(Graphics2D g2, int alto) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            int y = alto - 14;
            int x = 16;
            String[] codigos = { "L1 Roja", "L2 Azul", "L3 Verde", "L4 Naranja" };
            Color[] colores = { COLOR_PELIGRO, COLOR_PRIMARIO, COLOR_EXITO, COLOR_ACENTO };
            for (int i = 0; i < codigos.length; i++) {
                g2.setColor(colores[i]);
                g2.fillOval(x, y - 9, 10, 10);
                g2.setColor(COLOR_TEXTO_SUAVE);
                g2.drawString(codigos[i], x + 14, y);
                x += 82;
            }
            g2.setColor(COLOR_HEADER);
            g2.drawOval(x, y - 9, 10, 10);
            g2.setColor(COLOR_TEXTO_SUAVE);
            g2.drawString("Transbordo", x + 14, y);
        }
    }

    // -------------------------------------------------------------- modelos

    /** Modelo de lista desplegable sobre un arreglo (sin Vector). */
    private static final class ModeloCombo<T> extends AbstractListModel<T> implements ComboBoxModel<T> {
        private static final long serialVersionUID = 1L;

        private final transient T[] elementos;
        private transient Object seleccionado;

        private ModeloCombo(T[] elementos) {
            this.elementos = elementos;
            this.seleccionado = elementos.length > 0 ? elementos[0] : null;
        }

        @Override
        public int getSize() {
            return elementos.length;
        }

        @Override
        public T getElementAt(int indice) {
            return elementos[indice];
        }

        @Override
        public void setSelectedItem(Object elemento) {
            seleccionado = elemento;
            fireContentsChanged(this, -1, -1);
        }

        @Override
        public Object getSelectedItem() {
            return seleccionado;
        }
    }

    /** Modelo de tabla de solo lectura sobre un arreglo de estaciones. */
    private static final class ModeloEstaciones extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMNAS = { "ID", "Nombre", "Línea", "Transbordo con" };

        private final transient Estacion[] estaciones;

        private ModeloEstaciones(Estacion[] estaciones) {
            this.estaciones = estaciones;
        }

        @Override
        public int getRowCount() {
            return estaciones.length;
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int columna) {
            return COLUMNAS[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Estacion estacion = estaciones[fila];
            switch (columna) {
                case 0:
                    return estacion.getId();
                case 1:
                    return estacion.getNombre();
                case 2:
                    return estacion.getCodigoLinea();
                default:
                    return estacion instanceof EstacionTransbordo
                            ? ((EstacionTransbordo) estacion).getLineaConexion()
                            : "-";
            }
        }
    }

    /**
     * Renderizador para dar color a la columna de línea en la tabla de estaciones.
     */
    private static final class RenderizadorEstaciones extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(column == 0 || column == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                if (column == 2 && value != null) {
                    c.setForeground(colorPorLinea(value.toString()));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(COLOR_TEXTO);
                }
            }
            return c;
        }
    }

    /** Modelo de tabla para los pasajeros en espera en el andén. */
    private static final class ModeloAnden extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMNAS = { "Turno", "ID", "Nombre", "Ubicación", "Destino" };

        private final transient Pasajero[] pasajeros;

        private ModeloAnden(Pasajero[] pasajeros) {
            this.pasajeros = pasajeros;
        }

        @Override
        public int getRowCount() {
            return pasajeros.length;
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int columna) {
            return COLUMNAS[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Pasajero p = pasajeros[fila];
            switch (columna) {
                case 0:
                    return fila + 1;
                case 1:
                    return p.getId();
                case 2:
                    return p.getNombre();
                case 3:
                    return p.getEstacionActual().getNombre();
                default:
                    return p.tieneDestino() ? p.getDestino().getNombre() : "-";
            }
        }
    }
}
