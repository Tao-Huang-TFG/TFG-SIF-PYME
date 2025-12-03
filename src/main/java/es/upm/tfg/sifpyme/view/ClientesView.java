package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ClienteController;
import es.upm.tfg.sifpyme.model.entity.Cliente;
import es.upm.tfg.sifpyme.util.NavigationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * Vista para gestionar la lista de clientes
 */
public class ClientesView extends JFrame {

    private final ClienteController controller;

    // Componentes
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnVolver;
    private JLabel lblTotalClientes;
    private TableRowSorter<DefaultTableModel> sorter;

    // CardLayout para navegación interna
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ClienteFormView clienteFormView;
    private JPanel listaPanel; // Referencia al panel de lista

    // Colores y fuentes
    private final Color COLOR_PRIMARIO = new Color(155, 89, 182);
    private final Color COLOR_SECUNDARIO = new Color(142, 68, 173);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_INFO = new Color(52, 152, 219);
    private final Color COLOR_VOLVER = new Color(149, 165, 166);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);

    public ClientesView() {
        this.controller = new ClienteController();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        configurarVentana();
        initComponents();
        setupLayout();
        cargarClientes();
    }

    private void configurarVentana() {
        setTitle("Gestión de Clientes - SifPyme");
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
    }

    private void initComponents() {
        // Tabla
        String[] columnas = { "ID", "Nombre Fiscal", "NIF", "Dirección", "Teléfono", "Email" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setFont(FUENTE_TABLA);
        tablaClientes.setRowHeight(35);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Aplicar color de texto consistente con los botones
        tablaClientes.setForeground(Color.DARK_GRAY);
        tablaClientes.setSelectionForeground(Color.DARK_GRAY);

        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaClientes.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(250);
        tablaClientes.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaClientes.getColumnModel().getColumn(5).setPreferredWidth(200);

        JTableHeader header = tablaClientes.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.DARK_GRAY);
        header.setBorder(BorderFactory.createLineBorder(COLOR_SECUNDARIO));

        // Sorter para búsqueda
        sorter = new TableRowSorter<>(modeloTabla);
        tablaClientes.setRowSorter(sorter);

        // Double-click para editar
        tablaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
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
                buscarClientes();
            }
        });

        // Botones
        btnNuevo = crearBoton("➕ Nuevo Cliente", COLOR_EXITO);
        btnEditar = crearBoton("✏️ Editar", COLOR_INFO);
        btnEliminar = crearBoton("🗑️ Eliminar", COLOR_PELIGRO);
        btnVolver = crearBoton("← Volver", COLOR_VOLVER);

        btnNuevo.addActionListener(e -> mostrarFormularioNuevo());
        btnEditar.addActionListener(e -> mostrarFormularioEdicion());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnVolver.addActionListener(e -> NavigationManager.getInstance().navigateBack());

        // Label de totales
        lblTotalClientes = new JLabel("Total: 0 clientes");
        lblTotalClientes.setFont(FUENTE_SUBTITULO);
        lblTotalClientes.setForeground(Color.DARK_GRAY);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(5, 5));
        boton.setBackground(color);
        boton.setForeground(color);
        boton.setFocusPainted(false);

        // Borde con el color original
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Separar el emoji del texto
        String[] partes = texto.split(" ", 2);
        String emoji = partes[0];
        String textoRestante = partes.length > 1 ? partes[1] : "";

        // Panel interno para el contenido
        JPanel contenidoPanel = new JPanel(new BorderLayout(8, 0));
        contenidoPanel.setOpaque(false);

        // Icono - usar fuente específica para emojis con mejor alineación
        JLabel lblIcono = new JLabel(emoji);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcono.setVerticalAlignment(SwingConstants.CENTER);
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);

        // Texto - usa una versión más oscura del color para buen contraste
        JLabel lblTexto = new JLabel(textoRestante);
        lblTexto.setFont(FUENTE_BOTON);
        lblTexto.setForeground(color.darker().darker());
        lblTexto.setVerticalAlignment(SwingConstants.CENTER);

        // Para botones con solo emoji
        if (textoRestante.isEmpty()) {
            contenidoPanel.add(lblIcono, BorderLayout.CENTER);
        } else {
            // Para botones con emoji + texto
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

        // Efecto hover - invertir colores
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
        listaPanel = crearListaPanel();
        
        // NUEVO: Añadir listener para detectar cuando el panel se hace visible
        listaPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Refrescar la lista cada vez que el panel se muestra
                cargarClientes();
            }
        });
        
        cardPanel.add(listaPanel, "listaClientes");

        // Inicialmente mostrar la lista
        cardLayout.show(cardPanel, "listaClientes");

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
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
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

        JLabel lblTitle = new JLabel("Gestión de Clientes");
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Administra tu base de datos de clientes");
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel("👥", SwingConstants.RIGHT);
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

        panel.add(lblTotalClientes);

        return panel;
    }

    private void mostrarFormularioNuevo() {
        clienteFormView = new ClienteFormView(cardLayout, cardPanel);
        cardPanel.add(clienteFormView, "formularioCliente");
        cardLayout.show(cardPanel, "formularioCliente");
    }

    private void mostrarFormularioEdicion() {
        int filaSeleccionada = tablaClientes.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona un cliente de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);
        Integer idCliente = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        Cliente cliente = controller.obtenerClientePorId(idCliente);

        if (cliente != null) {
            clienteFormView = new ClienteFormView(cardLayout, cardPanel, cliente);
            cardPanel.add(clienteFormView, "formularioCliente");
            cardLayout.show(cardPanel, "formularioCliente");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar el cliente seleccionado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarClientes() {
        modeloTabla.setRowCount(0);

        List<Cliente> clientes = controller.obtenerTodosLosClientes();

        for (Cliente cliente : clientes) {
            Object[] fila = {
                    cliente.getIdCliente(),
                    cliente.getNombreFiscal(),
                    cliente.getNif(),
                    cliente.getDireccion() != null ? cliente.getDireccion() : "",
                    cliente.getTelefono() != null ? cliente.getTelefono() : "",
                    cliente.getEmail() != null ? cliente.getEmail() : ""
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotalClientes();
    }

    private void buscarClientes() {
        String termino = txtBuscar.getText().trim();

        if (termino.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + termino, 1, 2, 3, 4, 5);
            sorter.setRowFilter(rf);
        }

        actualizarTotalClientes();
    }

    private void eliminarCliente() {
        int filaSeleccionada = tablaClientes.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona un cliente de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);
        Integer idCliente = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombreCliente = (String) modeloTabla.getValueAt(filaModelo, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar al cliente:\n" + nombreCliente + "?\n\n" +
                        "Esta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controller.eliminarCliente(idCliente);

            if (eliminado) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cliente eliminado exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo eliminar el cliente.\n" +
                                "Puede que esté asociado a facturas existentes.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarTotalClientes() {
        int total = tablaClientes.getRowCount();
        lblTotalClientes.setText("Total: " + total + " cliente" + (total != 1 ? "s" : ""));
    }
}