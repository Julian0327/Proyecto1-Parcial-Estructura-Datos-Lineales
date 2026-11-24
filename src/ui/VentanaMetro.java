package ui;

import algoritmos.AlgoritmoRuta;
import Excepciones.ExcepcionMetro;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import Modelo.ConsultaRuta;
import Modelo.Estacion;
import Modelo.EstacionTransbordo;
import Modelo.ResultadoRuta;
import Servicio.MetroService;

/**
 * Interfaz gráfica (Swing). Permite elegir origen, destino y algoritmo,
 * muestra la ruta y el tiempo total en un {@link JTextArea}, lista las
 * estaciones en una tabla y el historial de consultas.
 *
 * <p>Los modelos de la tabla y de las listas desplegables están hechos con
 * arreglos propios para no usar colecciones de Java (como Vector).
 */
public class VentanaMetro {

    private final MetroService servicio;
    private final AlgoritmoRuta[] algoritmos;

    private final JFrame marco = new JFrame("Metro - Rutas");
    private final JComboBox<Estacion> comboOrigen = new JComboBox<>();
    private final JComboBox<Estacion> comboDestino = new JComboBox<>();
    private final JComboBox<String> comboAlgoritmo;
    private final JTextArea areaResultado = new JTextArea();
    private final JTextArea areaHistorial = new JTextArea();
    private final JTable tablaEstaciones = new JTable();

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
        SwingUtilities.invokeLater(() -> new VentanaMetro(servicio, algoritmos).marco.setVisible(true));
    }

    // -------------------------------------------------------- construcción

    private void construir() {
        marco.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        marco.setLayout(new BorderLayout(8, 8));
        marco.add(crearPanelControles(), BorderLayout.NORTH);
        marco.add(crearPanelCentral(), BorderLayout.CENTER);
        marco.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        marco.setSize(1000, 580);
        marco.setLocationRelativeTo(null);
    }

    private JPanel crearPanelControles() {
        JPanel filaSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaSeleccion.add(new JLabel("Origen:"));
        filaSeleccion.add(comboOrigen);
        filaSeleccion.add(new JLabel("Destino:"));
        filaSeleccion.add(comboDestino);
        filaSeleccion.add(new JLabel("Algoritmo:"));
        filaSeleccion.add(comboAlgoritmo);

        JButton botonCalcular = new JButton("Calcular ruta");
        botonCalcular.addActionListener(e -> calcular());
        JButton botonDeshacer = new JButton("Deshacer última consulta");
        botonDeshacer.addActionListener(e -> deshacer());
        JButton botonGuardar = new JButton("Guardar red");
        botonGuardar.addActionListener(e -> guardar());
        JButton botonCargar = new JButton("Cargar red");
        botonCargar.addActionListener(e -> cargar());
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaBotones.add(botonCalcular);
        filaBotones.add(botonDeshacer);
        filaBotones.add(botonGuardar);
        filaBotones.add(botonCargar);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(filaSeleccion);
        panel.add(filaBotones);
        return panel;
    }

    private JSplitPane crearPanelCentral() {
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaHistorial.setEditable(false);
        areaHistorial.setLineWrap(true);
        areaHistorial.setWrapStyleWord(true);
        areaHistorial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane panelResultado = new JScrollPane(areaResultado);
        panelResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        JScrollPane panelHistorial = new JScrollPane(areaHistorial);
        panelHistorial.setBorder(BorderFactory.createTitledBorder("Historial (más reciente primero)"));
        JSplitPane derecha = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelResultado, panelHistorial);
        derecha.setResizeWeight(0.5);

        JScrollPane panelTabla = new JScrollPane(tablaEstaciones);
        panelTabla.setBorder(BorderFactory.createTitledBorder("Estaciones"));
        JSplitPane central = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTabla, derecha);
        central.setResizeWeight(0.45);
        return central;
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
            areaResultado.setText("Algoritmo: " + servicio.getAlgoritmo().nombre() + "\n"
                    + "Origen:  " + origen + "\n"
                    + "Destino: " + destino + "\n\n"
                    + "Ruta (" + ruta.getEstaciones().tamano() + " estaciones):\n"
                    + describirCamino(ruta) + "\n\n"
                    + "Tiempo total: " + ruta.getMinutosTotales() + " min");
            actualizarHistorial();
        } catch (ExcepcionMetro e) {
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
            areaResultado.setText("Consulta deshecha:\n" + quitada);
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
            refrescar();
            areaResultado.setText("Red cargada: " + servicio.cantidadEstaciones() + " estaciones, "
                    + servicio.cantidadConexiones() + " conexiones.\n"
                    + "Se reiniciaron los pasajeros, el andén y el historial.");
        } catch (ExcepcionMetro | IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    // ------------------------------------------------------------ refresco

    /** Vuelve a leer la red del servicio y actualiza combos, tabla e historial. */
    private void refrescar() {
        Estacion[] estaciones = servicio.estaciones();
        comboOrigen.setModel(new ModeloCombo<>(estaciones));
        comboDestino.setModel(new ModeloCombo<>(estaciones));
        if (estaciones.length > 1) {
            comboDestino.setSelectedIndex(estaciones.length / 2);
        }
        tablaEstaciones.setModel(new ModeloEstaciones(estaciones));
        actualizarHistorial();
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

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(marco, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
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
        private static final String[] COLUMNAS = {"ID", "Nombre", "Línea", "Transbordo con"};

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
                            ? ((EstacionTransbordo) estacion).getLineaConexion() : "-";
            }
        }
    }
}
