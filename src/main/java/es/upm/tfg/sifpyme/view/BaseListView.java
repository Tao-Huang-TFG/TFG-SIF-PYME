package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.util.NavigationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Clase base abstracta para todas las vistas de lista (Clientes, Empresas, Productos)
 * Implementa funcionalidad común: tabla, búsqueda, navegación, estilos
 * REFACTORIZADO: Ahora usa UIHelper y UITheme para consistencia visual
 */
public abstract class BaseListView<T> extends JFrame {

    // Componentes comunes
    protected JTable tabla;
    protected DefaultTableModel modeloTabla;
    protected JTextField txtBuscar;
    protected JButton btnNuevo;
    protected JButton btnEditar;
    protected JButton btnEliminar;
    protected JButton btnVolver;
    protected JLabel lblTotal;
    protected TableRowSorter<DefaultTableModel> sorter;

    // CardLayout para navegación interna
    protected CardLayout cardLayout;
    protected JPanel cardPanel;
    protected JPanel listaPanel;

    // Colores comunes (pueden ser sobrescritos)
    protected Color COLOR_PRIMARIO = UITheme.COLOR_INFO;
    protected Color COLOR_SECUNDARIO = UITheme.COLOR_INFO;

    public BaseListView() {
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        configurarVentana();
        initComponents();
        setupLayout();
    }

    // ==================== MÉTODOS ABSTRACTOS (deben implementarse) ====================

    protected abstract String getTituloVentana();
    protected abstract String getTituloHeader();
    protected abstract String getSubtituloHeader();
    protected abstract String getIconoHeader();
    protected abstract String[] getNombresColumnas();
    protected abstract String getNombreCardLista();
    protected abstract String getNombreCardFormulario();
    protected abstract String getNombreEntidadSingular();
    protected abstract String getNombreEntidadPlural();
    protected abstract void cargarDatos();
    protected abstract JPanel crearFormularioNuevo();
    protected abstract JPanel crearFormularioEdicion(Integer id);
    protected abstract boolean eliminarRegistro(Integer id);
    protected abstract void configurarAnchoColumnas();

    // ==================== MÉTODOS OPCIONALES (pueden sobrescribirse) ====================

    protected void agregarBotonesAdicionales(JPanel buttonsPanel) {
        // Por defecto no hace nada - las subclases pueden sobrescribirlo
    }

    protected void configurarColores() {
        // Por defecto usa los colores base - las subclases pueden sobrescribirlo
    }

    // ==================== IMPLEMENTACIÓN COMÚN ====================

    private void configurarVentana() {
        setTitle(getTituloVentana());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                NavigationManager.getInstance().navigateBack();
            }
        });

        setPreferredSize(new Dimension(1200, 700));
        setMinimumSize(new Dimension(1000, 600));
        setResizable(true);
        setLocationRelativeTo(null);

        // Permitir colores personalizados
        configurarColores();
    }

    private void initComponents() {
        // Tabla
        String[] columnas = getNombresColumnas();
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(UITheme.FUENTE_TABLA);
        tabla.setRowHeight(35);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setForeground(Color.DARK_GRAY);
        tabla.setSelectionForeground(Color.DARK_GRAY);

        configurarAnchoColumnas();

        JTableHeader header = tabla.getTableHeader();
        header.setFont(UITheme.FUENTE_ETIQUETA);
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.DARK_GRAY);
        header.setBorder(BorderFactory.createLineBorder(COLOR_SECUNDARIO));

        // Sorter para búsqueda
        sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);

        // Double-click para editar
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    mostrarFormularioEdicion();
                }
            }
        });

        // Campo de búsqueda - REFACTORIZADO
        txtBuscar = UIHelper.crearCampoTexto(25);
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buscar();
            }
        });

        // Botones - REFACTORIZADO usando UIHelper
        String nombreEntidad = getNombreEntidadSingular();
        String nombreCapitalizado = nombreEntidad.substring(0, 1).toUpperCase() + 
                                   nombreEntidad.substring(1);
        
        btnNuevo = UIHelper.crearBotonAccion("nuevo", "Nuevo " + nombreCapitalizado);
        btnEditar = UIHelper.crearBotonAccion("editar", "Editar");
        btnEliminar = UIHelper.crearBotonAccion("eliminar", "Eliminar");
        btnVolver = UIHelper.crearBotonAccion("volver", "Volver");

        btnNuevo.addActionListener(e -> mostrarFormularioNuevo());
        btnEditar.addActionListener(e -> mostrarFormularioEdicion());
        btnEliminar.addActionListener(e -> eliminar());
        btnVolver.addActionListener(e -> NavigationManager.getInstance().navigateBack());

        // Label de totales
        lblTotal = new JLabel("Total: 0 " + getNombreEntidadPlural());
        lblTotal.setFont(UITheme.FUENTE_SUBTITULO);
        lblTotal.setForeground(Color.DARK_GRAY);
    }

    private void setupLayout() {
        listaPanel = crearListaPanel();

        // Listener para refrescar automáticamente cuando se muestra la lista
        listaPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargarDatos();
            }
        });

        cardPanel.add(listaPanel, getNombreCardLista());
        cardLayout.show(cardPanel, getNombreCardLista());

        add(cardPanel);
        pack();
    }

    private JPanel crearListaPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UITheme.COLOR_FONDO);

        panel.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(UITheme.COLOR_FONDO);
        centerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        centerPanel.add(createToolbarPanel(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel(getTituloHeader());
        lblTitle.setFont(UITheme.FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(getSubtituloHeader());
        lblSubtitle.setFont(UITheme.FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel(getIconoHeader(), SwingConstants.RIGHT);
        iconLabel.setFont(UITheme.FUENTE_ICONO_GRANDE);
        iconLabel.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        backPanel.setOpaque(false);
        backPanel.add(btnVolver);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = new JLabel(UITheme.ICONO_BUSCAR + " Buscar:");
        lblBuscar.setFont(UITheme.FUENTE_ICONO_TEXTO);
        lblBuscar.setForeground(Color.DARK_GRAY);
        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnNuevo);
        buttonsPanel.add(btnEditar);
        buttonsPanel.add(btnEliminar);

        // Permitir botones adicionales
        agregarBotonesAdicionales(buttonsPanel);

        panel.add(backPanel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        panel.add(lblTotal);
        return panel;
    }

    public void mostrarFormularioNuevo() {
        JPanel formulario = crearFormularioNuevo();
        cardPanel.add(formulario, getNombreCardFormulario());
        cardLayout.show(cardPanel, getNombreCardFormulario());
    }

    protected void mostrarFormularioEdicion() {
        int filaSeleccionada = tabla.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona un " + getNombreEntidadSingular() + " de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);
        Integer id = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        JPanel formulario = crearFormularioEdicion(id);
        if (formulario != null) {
            cardPanel.add(formulario, getNombreCardFormulario());
            cardLayout.show(cardPanel, getNombreCardFormulario());
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar el " + getNombreEntidadSingular() + " seleccionado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void buscar() {
        String termino = txtBuscar.getText().trim();

        if (termino.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Buscar en todas las columnas excepto la primera (ID)
            int numColumnas = modeloTabla.getColumnCount();
            int[] columnas = new int[numColumnas - 1];
            for (int i = 0; i < numColumnas - 1; i++) {
                columnas[i] = i + 1;
            }
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + termino, columnas);
            sorter.setRowFilter(rf);
        }

        actualizarTotal();
    }

    protected void eliminar() {
        int filaSeleccionada = tabla.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona un " + getNombreEntidadSingular() + " de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);
        Integer id = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar el " + getNombreEntidadSingular() + ":\n" +
                        nombre + "?\n\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = eliminarRegistro(id);

            if (eliminado) {
                JOptionPane.showMessageDialog(
                        this,
                        getNombreEntidadSingular().substring(0, 1).toUpperCase() +
                                getNombreEntidadSingular().substring(1) + " eliminado exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo eliminar el " + getNombreEntidadSingular() + ".\n" +
                                "Puede que esté asociado a facturas existentes.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void actualizarTotal() {
        int total = tabla.getRowCount();
        lblTotal.setText("Total: " + total + " " +
                (total == 1 ? getNombreEntidadSingular() : getNombreEntidadPlural()));
    }
    
    // Método auxiliar para crear botones adicionales (usado por subclases)
    protected JButton crearBoton(String texto, Color color) {
        return UIHelper.crearBoton(texto, color, "");
    }
}