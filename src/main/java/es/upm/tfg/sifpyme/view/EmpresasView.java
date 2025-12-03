package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.util.NavigationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Vista para gestionar la lista de empresas
 */
public class EmpresasView extends JFrame {

    private final EmpresaController controller;

    // Componentes
    private JTable tablaEmpresas;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnVolver;
    private JButton btnEstablecerDefecto;
    private JLabel lblTotalEmpresas;
    private TableRowSorter<DefaultTableModel> sorter;

    // CardLayout para navegación interna
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private EmpresaFormView empresaFormView;

    // Colores y fuentes
    private final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private final Color COLOR_SECUNDARIO = new Color(52, 152, 219);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_INFO = new Color(52, 152, 219);
    private final Color COLOR_WARNING = new Color(241, 196, 15);
    private final Color COLOR_VOLVER = new Color(149, 165, 166);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);

    public EmpresasView() {
        this.controller = new EmpresaController();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        configurarVentana();
        initComponents();
        setupLayout();
        cargarEmpresas();
    }

    private void configurarVentana() {
        setTitle("Gestión de Empresas - SifPyme");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                NavigationManager.getInstance().navigateBack();
            }
        });

        setPreferredSize(new Dimension(1300, 700));
        setMinimumSize(new Dimension(1100, 600));
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Tabla
        String[] columnas = { "ID", "Nombre Comercial", "Razón Social", "NIF", "Ciudad", "Provincia", "Email", "Por Defecto" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 7) return Boolean.class;
                return String.class;
            }
        };

        tablaEmpresas = new JTable(modeloTabla);
        tablaEmpresas.setFont(FUENTE_TABLA);
        tablaEmpresas.setRowHeight(35);
        tablaEmpresas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaEmpresas.setForeground(Color.DARK_GRAY);
        tablaEmpresas.setSelectionForeground(Color.DARK_GRAY);

        tablaEmpresas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaEmpresas.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaEmpresas.getColumnModel().getColumn(2).setPreferredWidth(200);
        tablaEmpresas.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaEmpresas.getColumnModel().getColumn(4).setPreferredWidth(150);
        tablaEmpresas.getColumnModel().getColumn(5).setPreferredWidth(150);
        tablaEmpresas.getColumnModel().getColumn(6).setPreferredWidth(200);
        tablaEmpresas.getColumnModel().getColumn(7).setPreferredWidth(100);

        JTableHeader header = tablaEmpresas.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.DARK_GRAY);
        header.setBorder(BorderFactory.createLineBorder(COLOR_SECUNDARIO));

        // Sorter para búsqueda
        sorter = new TableRowSorter<>(modeloTabla);
        tablaEmpresas.setRowSorter(sorter);

        // Double-click para editar
        tablaEmpresas.addMouseListener(new java.awt.event.MouseAdapter() {
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
                buscarEmpresas();
            }
        });

        // Botones
        btnNuevo = crearBoton("➕ Nueva Empresa", COLOR_EXITO);
        btnEditar = crearBoton("✏️ Editar", COLOR_INFO);
        btnEliminar = crearBoton("🗑️ Eliminar", COLOR_PELIGRO);
        btnEstablecerDefecto = crearBoton("⭐ Establecer Defecto", COLOR_WARNING);
        btnVolver = crearBoton("← Volver", COLOR_VOLVER);

        btnNuevo.addActionListener(e -> mostrarFormularioNuevo());
        btnEditar.addActionListener(e -> mostrarFormularioEdicion());
        btnEliminar.addActionListener(e -> eliminarEmpresa());
        btnEstablecerDefecto.addActionListener(e -> establecerEmpresaPorDefecto());
        btnVolver.addActionListener(e -> NavigationManager.getInstance().navigateBack());

        // Label de totales
        lblTotalEmpresas = new JLabel("Total: 0 empresas");
        lblTotalEmpresas.setFont(FUENTE_SUBTITULO);
        lblTotalEmpresas.setForeground(Color.DARK_GRAY);
    }

    private JButton crearBoton(String texto, Color color) {
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
                boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
                if (lblTexto != null) {
                    lblTexto.setForeground(color.darker().darker());
                }
                lblIcono.setForeground(color.darker().darker());
                boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }
        });

        return boton;
    }

    private void setupLayout() {
        // Crear el panel de lista (vista principal)
        JPanel listaPanel = crearListaPanel();
        listaPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e){
                cargarEmpresas();
            }
        });

        cardPanel.add(listaPanel, "listaEmpresas");

        // Inicialmente mostrar la lista
        cardLayout.show(cardPanel, "listaEmpresas");

        add(cardPanel);
        pack();
    }

    private JPanel crearListaPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(COLOR_FONDO);

        // Header
        JPanel headerPanel = createHeaderPanel();
        panel.add(headerPanel, BorderLayout.NORTH);

        // Panel central con tabla
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(COLOR_FONDO);
        centerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Toolbar con búsqueda y botones
        JPanel toolbarPanel = createToolbarPanel();
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);

        // Tabla
        JScrollPane scrollPane = new JScrollPane(tablaEmpresas);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer con totales
        JPanel footerPanel = createFooterPanel();
        centerPanel.add(footerPanel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel("Gestión de Empresas");
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Administra las empresas desde las que facturas");
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel("🏢", SwingConstants.RIGHT);
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

        // Panel de volver a la izquierda
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        backPanel.setOpaque(false);
        backPanel.add(btnVolver);

        // Panel de búsqueda (centrado)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lblBuscar.setForeground(Color.DARK_GRAY);
        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);

        // Panel de botones de acción
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnNuevo);
        buttonsPanel.add(btnEditar);
        buttonsPanel.add(btnEliminar);
        buttonsPanel.add(btnEstablecerDefecto);

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

        panel.add(lblTotalEmpresas);

        return panel;
    }

    public void mostrarFormularioNuevo() {
        empresaFormView = new EmpresaFormView(cardLayout, cardPanel);
        cardPanel.add(empresaFormView, "formularioEmpresa");
        cardLayout.show(cardPanel, "formularioEmpresa");
    }

    private void mostrarFormularioEdicion() {
        int filaSeleccionada = tablaEmpresas.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona una empresa de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaEmpresas.convertRowIndexToModel(filaSeleccionada);
        Integer idEmpresa = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        Empresa empresa = controller.obtenerEmpresaPorId(idEmpresa);

        if (empresa != null) {
            empresaFormView = new EmpresaFormView(cardLayout, cardPanel, empresa);
            cardPanel.add(empresaFormView, "formularioEmpresa");
            cardLayout.show(cardPanel, "formularioEmpresa");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar la empresa seleccionada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarEmpresas() {
        modeloTabla.setRowCount(0);

        List<Empresa> empresas = controller.obtenerTodasLasEmpresas();

        for (Empresa empresa : empresas) {
            Object[] fila = {
                    empresa.getIdEmpresa(),
                    empresa.getNombreComercial(),
                    empresa.getRazonSocial(),
                    empresa.getNif(),
                    empresa.getCiudad(),
                    empresa.getProvincia(),
                    empresa.getEmail() != null ? empresa.getEmail() : "",
                    empresa.getPorDefecto() != null && empresa.getPorDefecto()
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotalEmpresas();
    }

    private void buscarEmpresas() {
        String termino = txtBuscar.getText().trim();

        if (termino.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + termino, 1, 2, 3, 4, 5, 6);
            sorter.setRowFilter(rf);
        }

        actualizarTotalEmpresas();
    }

    private void establecerEmpresaPorDefecto() {
        int filaSeleccionada = tablaEmpresas.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona una empresa de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaEmpresas.convertRowIndexToModel(filaSeleccionada);
        Integer idEmpresa = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombreEmpresa = (String) modeloTabla.getValueAt(filaModelo, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Establecer '" + nombreEmpresa + "' como empresa por defecto?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            Empresa empresa = controller.obtenerEmpresaPorId(idEmpresa);
            if (empresa != null) {
                empresa.setPorDefecto(true);
                boolean actualizado = controller.actualizarEmpresa(empresa);

                if (actualizado) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Empresa establecida como predeterminada.",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarEmpresas();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Error al establecer empresa por defecto.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void eliminarEmpresa() {
        int filaSeleccionada = tablaEmpresas.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona una empresa de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaEmpresas.convertRowIndexToModel(filaSeleccionada);
        Integer idEmpresa = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombreEmpresa = (String) modeloTabla.getValueAt(filaModelo, 1);
        Boolean esPorDefecto = (Boolean) modeloTabla.getValueAt(filaModelo, 7);

        if (esPorDefecto) {
            JOptionPane.showMessageDialog(
                    this,
                    "No puedes eliminar la empresa por defecto.\nPrimero establece otra como predeterminada.",
                    "Operación no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar la empresa:\n" + nombreEmpresa + "?\n\n" +
                        "Esta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controller.eliminarEmpresaporId(idEmpresa);

            if (eliminado) {
                JOptionPane.showMessageDialog(
                        this,
                        "Empresa eliminada exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarEmpresas();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo eliminar la empresa.\n" +
                                "Puede que esté asociada a facturas existentes.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarTotalEmpresas() {
        int total = tablaEmpresas.getRowCount();
        lblTotalEmpresas.setText("Total: " + total + " empresa" + (total != 1 ? "s" : ""));
    }
}