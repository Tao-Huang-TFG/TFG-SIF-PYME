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
    protected Color COLOR_PRIMARIO = new Color(52, 152, 219);
    protected Color COLOR_SECUNDARIO = new Color(142, 68, 173);
    protected final Color COLOR_EXITO = new Color(46, 204, 113);
    protected final Color COLOR_PELIGRO = new Color(231, 76, 60);
    protected final Color COLOR_INFO = new Color(52, 152, 219);
    protected final Color COLOR_VOLVER = new Color(149, 165, 166);
    protected final Color COLOR_FONDO = new Color(245, 245, 245);
    protected final Color COLOR_BORDE = new Color(220, 220, 220);

    // Fuentes comunes
    protected final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    protected final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    protected final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    protected final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);

    public BaseListView() {
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        
        configurarVentana();
        initComponents();
        setupLayout();
    }

    // ==================== MÉTODOS ABSTRACTOS (deben implementarse) ====================
    
    /**
     * Retorna el título de la ventana (ej: "Gestión de Clientes - SifPyme")
     */
    protected abstract String getTituloVentana();
    
    /**
     * Retorna el título del encabezado (ej: "Gestión de Clientes")
     */
    protected abstract String getTituloHeader();
    
    /**
     * Retorna el subtítulo del encabezado (ej: "Administra tu base de datos de clientes")
     */
    protected abstract String getSubtituloHeader();
    
    /**
     * Retorna el icono emoji del encabezado (ej: "👥")
     */
    protected abstract String getIconoHeader();
    
    /**
     * Retorna los nombres de las columnas de la tabla
     */
    protected abstract String[] getNombresColumnas();
    
    /**
     * Retorna el nombre de la card para la lista (ej: "listaClientes")
     */
    protected abstract String getNombreCardLista();
    
    /**
     * Retorna el nombre de la card para el formulario (ej: "formularioCliente")
     */
    protected abstract String getNombreCardFormulario();
    
    /**
     * Retorna el nombre singular de la entidad (ej: "cliente")
     */
    protected abstract String getNombreEntidadSingular();
    
    /**
     * Retorna el nombre plural de la entidad (ej: "clientes")
     */
    protected abstract String getNombreEntidadPlural();
    
    /**
     * Carga todos los datos de la base de datos y los añade a la tabla
     */
    protected abstract void cargarDatos();
    
    /**
     * Crea y retorna el panel del formulario para nuevo registro
     */
    protected abstract JPanel crearFormularioNuevo();
    
    /**
     * Crea y retorna el panel del formulario para edición
     * @param id ID del registro a editar
     */
    protected abstract JPanel crearFormularioEdicion(Integer id);
    
    /**
     * Elimina un registro por su ID
     * @param id ID del registro a eliminar
     * @return true si se eliminó correctamente
     */
    protected abstract boolean eliminarRegistro(Integer id);
    
    /**
     * Configura los anchos preferidos de las columnas
     */
    protected abstract void configurarAnchoColumnas();

    // ==================== MÉTODOS OPCIONALES (pueden sobrescribirse) ====================
    
    /**
     * Permite agregar botones adicionales al toolbar (ej: btnEstablecerDefecto en Empresas)
     * @param buttonsPanel Panel donde agregar los botones
     */
    protected void agregarBotonesAdicionales(JPanel buttonsPanel) {
        // Por defecto no hace nada - las subclases pueden sobrescribirlo
    }
    
    /**
     * Permite configurar colores personalizados por vista
     */
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
        tabla.setFont(FUENTE_TABLA);
        tabla.setRowHeight(35);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setForeground(Color.DARK_GRAY);
        tabla.setSelectionForeground(Color.DARK_GRAY);

        configurarAnchoColumnas();

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

        // Campo de búsqueda
        txtBuscar = new JTextField(25);
        txtBuscar.setFont(FUENTE_TABLA);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buscar();
            }
        });

        // Botones
        btnNuevo = crearBoton("➕ Nuevo " + getNombreEntidadSingular().substring(0, 1).toUpperCase() + 
                             getNombreEntidadSingular().substring(1), COLOR_EXITO);
        btnEditar = crearBoton("✏️ Editar", COLOR_INFO);
        btnEliminar = crearBoton("🗑️ Eliminar", COLOR_PELIGRO);
        btnVolver = crearBoton("← Volver", COLOR_VOLVER);

        btnNuevo.addActionListener(e -> mostrarFormularioNuevo());
        btnEditar.addActionListener(e -> mostrarFormularioEdicion());
        btnEliminar.addActionListener(e -> eliminar());
        btnVolver.addActionListener(e -> NavigationManager.getInstance().navigateBack());

        // Label de totales
        lblTotal = new JLabel("Total: 0 " + getNombreEntidadPlural());
        lblTotal.setFont(FUENTE_SUBTITULO);
        lblTotal.setForeground(Color.DARK_GRAY);
    }

    protected JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(5, 5));
        boton.setBackground(color);
        boton.setForeground(color);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String[] partes = texto.split(" ", 2);
        String emoji = partes[0];
        String textoRestante = partes.length > 1 ? partes[1] : "";

        JPanel contenidoPanel = new JPanel(new BorderLayout(8, 0));
        contenidoPanel.setOpaque(false);

        JLabel lblIcono = new JLabel(emoji);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcono.setVerticalAlignment(SwingConstants.CENTER);
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblTexto = new JLabel(textoRestante);
        lblTexto.setFont(FUENTE_BOTON);
        lblTexto.setForeground(color.darker().darker());
        lblTexto.setVerticalAlignment(SwingConstants.CENTER);

        if (textoRestante.isEmpty()) {
            contenidoPanel.add(lblIcono, BorderLayout.CENTER);
        } else {
            JPanel horizontalPanel = new JPanel();
            horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));
            horizontalPanel.setOpaque(false);

            horizontalPanel.add(Box.createHorizontalStrut(5));
            horizontalPanel.add(lblIcono);
            horizontalPanel.add(Box.createHorizontalStrut(8));
            horizontalPanel.add(lblTexto);
            horizontalPanel.add(Box.createHorizontalStrut(5));

            contenidoPanel.add(horizontalPanel, BorderLayout.CENTER);
        }

        boton.add(contenidoPanel, BorderLayout.CENTER);

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
                if (lblTexto != null) {
                    lblTexto.setForeground(color);
                }
                lblIcono.setForeground(color);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
                if (lblTexto != null) {
                    lblTexto.setForeground(color.darker().darker());
                }
                lblIcono.setForeground(color.darker().darker());
            }
        });

        return boton;
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
        panel.setBackground(COLOR_FONDO);

        panel.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(COLOR_FONDO);
        centerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        centerPanel.add(createToolbarPanel(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));
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
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(getSubtituloHeader());
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel(getIconoHeader(), SwingConstants.RIGHT);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        backPanel.setOpaque(false);
        backPanel.add(btnVolver);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
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
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
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
}