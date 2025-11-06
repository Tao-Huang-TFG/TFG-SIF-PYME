package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.model.entity.TipoIva;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Vista para gestionar la lista de productos
 */
public class ProductosView extends JFrame {

    private final ProductoController controller;

    // Componentes
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnRefrescar;
    private JLabel lblTotalProductos;
    private TableRowSorter<DefaultTableModel> sorter;

    // Colores y fuentes
    private final Color COLOR_PRIMARIO = new Color(155, 89, 182);
    private final Color COLOR_SECUNDARIO = new Color(142, 68, 173);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_INFO = new Color(52, 152, 219);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);

    public ProductosView() {
        this.controller = new ProductoController();

        configurarVentana();
        initComponents();
        setupLayout();
        cargarProductos();
    }

    private void configurarVentana() {
        setTitle("Gestión de Productos - SifPyme");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1300, 700));
        setMinimumSize(new Dimension(1100, 600));
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Tabla
        String[] columnas = { "ID", "Código", "Nombre", "Descripción", "Precio", "Precio Base", "IVA %", "Retención %" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(FUENTE_TABLA);
        tablaProductos.setRowHeight(35);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaProductos.setForeground(Color.DARK_GRAY);
        tablaProductos.setSelectionForeground(Color.WHITE);

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(200);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(250);
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(6).setPreferredWidth(80);
        tablaProductos.getColumnModel().getColumn(7).setPreferredWidth(100);

        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(COLOR_SECUNDARIO));

        // Sorter para búsqueda
        sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);

        // Double-click para editar
        tablaProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarProducto();
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
                buscarProductos();
            }
        });

        // Botones
        btnNuevo = crearBoton("➕ Nuevo Producto", COLOR_EXITO);
        btnEditar = crearBoton("✏️ Editar", COLOR_INFO);
        btnEliminar = crearBoton("🗑️ Eliminar", COLOR_PELIGRO);
        btnRefrescar = crearBoton("🔄 Refrescar", COLOR_PRIMARIO);

        btnNuevo.addActionListener(e -> nuevoProducto());
        btnEditar.addActionListener(e -> editarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnRefrescar.addActionListener(e -> cargarProductos());

        // Label de totales
        lblTotalProductos = new JLabel("Total: 0 productos");
        lblTotalProductos.setFont(FUENTE_SUBTITULO);
        lblTotalProductos.setForeground(Color.DARK_GRAY);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(5, 5));
        boton.setBackground(Color.WHITE);
        boton.setFocusPainted(false);

        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
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
                lblTexto.setForeground(Color.WHITE);
                lblIcono.setForeground(Color.WHITE);
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color.darker(), 2),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(Color.WHITE);
                lblTexto.setForeground(color.darker().darker());
                lblIcono.setForeground(color.darker().darker());
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
            }
        });

        return boton;
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_FONDO);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel central con tabla
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(COLOR_FONDO);
        centerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Toolbar con búsqueda y botones
        JPanel toolbarPanel = createToolbarPanel();
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);

        // Tabla
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer con totales
        JPanel footerPanel = createFooterPanel();
        centerPanel.add(footerPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel("Gestión de Productos");
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Administra tu catálogo de productos y servicios");
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel("📦", SwingConstants.RIGHT);
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

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
        buttonsPanel.add(btnRefrescar);

        panel.add(searchPanel, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        panel.add(lblTotalProductos);

        return panel;
    }

    private void cargarProductos() {
        modeloTabla.setRowCount(0);

        List<Producto> productos = controller.obtenerTodosLosProductos();

        for (Producto producto : productos) {
            // Obtener el porcentaje de IVA
            String ivaStr = "";
            if (producto.getIdTipoIva() != null) {
                TipoIva tipoIva = controller.obtenerTipoIvaPorId(producto.getIdTipoIva());
                if (tipoIva != null) {
                    ivaStr = tipoIva.getPorcentaje() + "%";
                }
            }

            Object[] fila = {
                    producto.getIdProducto(),
                    producto.getCodigo() != null ? producto.getCodigo() : "",
                    producto.getNombre(),
                    producto.getDescripcion() != null ? truncarTexto(producto.getDescripcion(), 50) : "",
                    formatearPrecio(producto.getPrecio()),
                    formatearPrecio(producto.getPrecioBase()),
                    ivaStr,
                    formatearPrecio(producto.getTipoRetencion())
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotalProductos();
    }

    private String truncarTexto(String texto, int maxLength) {
        if (texto.length() <= maxLength) {
            return texto;
        }
        return texto.substring(0, maxLength) + "...";
    }

    private String formatearPrecio(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return String.format("%.2f", valor);
    }

    private void buscarProductos() {
        String termino = txtBuscar.getText().trim();

        if (termino.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + termino, 1, 2, 3);
            sorter.setRowFilter(rf);
        }

        actualizarTotalProductos();
    }

    private void nuevoProducto() {
        ProductoFormView form = new ProductoFormView(this);
        form.setVisible(true);

        if (form.isGuardadoExitoso()) {
            cargarProductos();
        }
    }

    private void editarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona un producto de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(filaSeleccionada);
        Integer idProducto = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        Producto producto = controller.obtenerProductoPorId(idProducto);

        if (producto != null) {
            ProductoFormView form = new ProductoFormView(this, producto);
            form.setVisible(true);

            if (form.isGuardadoExitoso()) {
                cargarProductos();
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar el producto seleccionado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, selecciona un producto de la lista.",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(filaSeleccionada);
        Integer idProducto = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombreProducto = (String) modeloTabla.getValueAt(filaModelo, 2);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar el producto:\n" + nombreProducto + "?\n\n" +
                        "Esta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controller.eliminarProducto(idProducto);

            if (eliminado) {
                JOptionPane.showMessageDialog(
                        this,
                        "Producto eliminado exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo eliminar el producto.\n" +
                                "Puede que esté asociado a facturas existentes.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarTotalProductos() {
        int total = tablaProductos.getRowCount();
        lblTotalProductos.setText("Total: " + total + " producto" + (total != 1 ? "s" : ""));
    }
}