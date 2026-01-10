package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.model.entity.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Diálogo para agregar o editar una línea de factura
 * CORREGIDO: Todos los errores de compilación resueltos
 */
public class LineaFacturaDialog extends JDialog {

    private final FacturaController facturaController;
    private final ProductoController productoController;
    private LineaFactura linea;
    private boolean confirmado = false;

    // Componentes
    private JTextField txtProducto;
    private JTextField txtCantidad;
    private JTextField txtPrecio;
    private JTextField txtDescuento;
    private JTextField txtPorcentajeIva;
    private JTextField txtPorcentajeRetencion;

    private JLabel lblSubtotal;
    private JLabel lblImporteIva;
    private JLabel lblImporteRetencion;
    private JLabel lblTotal;

    private JButton btnAceptar;
    private JButton btnCancelar;

    private JPopupMenu popupProductos;
    private JList<Producto> listaProductos;
    private DefaultListModel<Producto> modeloProductos;

    private Producto productoSeleccionado;
    private List<Producto> todosLosProductos;

    private boolean calculando = false;

    public LineaFacturaDialog(JFrame parent, FacturaController controller, LineaFactura lineaEditar) {
        super(parent, lineaEditar == null ? "Agregar Línea" : "Editar Línea", true);
        this.facturaController = controller;
        this.productoController = new ProductoController();
        this.linea = lineaEditar != null ? lineaEditar : new LineaFactura();

        todosLosProductos = facturaController.obtenerProductos();

        initComponents();
        setupLayout();
        configurarCalculoAutomatico();

        if (lineaEditar != null) {
            cargarLineaExistente();
        }

        setSize(750, 700);
        setMinimumSize(new Dimension(650, 600));
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initComponents() {
        txtProducto = UIHelper.crearCampoTexto(30);
        txtProducto.setColumns(20);

        txtProducto.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int length = txtProducto.getText().length();
                    txtProducto.setCaretPosition(length);
                    txtProducto.moveCaretPosition(length);
                });
            }
        });

        modeloProductos = new DefaultListModel<>();
        listaProductos = new JList<>(modeloProductos);
        listaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaProductos.setVisibleRowCount(6);

        listaProductos.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String display = value.getNombre();
            if (value.getCodigo() != null && !value.getCodigo().isEmpty()) {
                display = value.getCodigo() + " - " + value.getNombre();
            }
            JLabel lbl = new JLabel(display);
            lbl.setFont(UITheme.FUENTE_CAMPO);
            if (isSelected) {
                lbl.setOpaque(true);
                lbl.setBackground(UITheme.COLOR_FACTURAS);
                lbl.setForeground(Color.WHITE);
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return lbl;
        });

        popupProductos = new JPopupMenu();
        popupProductos.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDE));
        JScrollPane scrollPane = new JScrollPane(listaProductos);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        popupProductos.add(scrollPane);

        popupProductos.setFocusable(false);

        popupProductos.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (txtProducto.isDisplayable()) {
                        txtProducto.requestFocusInWindow();
                        txtProducto.setCaretPosition(txtProducto.getText().length());
                    }
                });
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                popupMenuWillBecomeInvisible(e);
            }
        });

        txtProducto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrarProductos();
            }

            public void removeUpdate(DocumentEvent e) {
                filtrarProductos();
            }

            public void changedUpdate(DocumentEvent e) {
                filtrarProductos();
            }
        });

        listaProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1)
                    seleccionarProducto();
            }
        });

        listaProductos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    seleccionarProducto();
            }
        });

        // Campos de texto
        txtCantidad = UIHelper.crearCampoTexto(10);
        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(150, 35));

        txtPrecio = UIHelper.crearCampoTexto(10);
        txtPrecio.setText("0.00");
        txtPrecio.setPreferredSize(new Dimension(150, 35));

        txtDescuento = UIHelper.crearCampoTexto(5);
        txtDescuento.setText("0");
        txtDescuento.setPreferredSize(new Dimension(100, 35));

        txtPorcentajeIva = UIHelper.crearCampoTexto(5);
        txtPorcentajeIva.setText("21.00");
        txtPorcentajeIva.setPreferredSize(new Dimension(100, 35));

        txtPorcentajeRetencion = UIHelper.crearCampoTexto(5);
        txtPorcentajeRetencion.setText("0");
        txtPorcentajeRetencion.setPreferredSize(new Dimension(100, 35));

        // Labels de resultados
        lblSubtotal = new JLabel("0,00 €");
        lblImporteIva = new JLabel("0,00 €");
        lblImporteRetencion = new JLabel("0,00 €");
        lblTotal = new JLabel("0,00 €");

        lblSubtotal.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteIva.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteRetencion.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotal.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTotal.setForeground(UITheme.COLOR_FACTURAS);

        // Botones
        btnAceptar = UIHelper.crearBotonAccion("guardar", "Aceptar");
        btnAceptar.addActionListener(e -> aceptar());

        btnCancelar = UIHelper.crearBotonAccion("cancelar", "Cancelar");
        btnCancelar.addActionListener(e -> dispose());
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(UITheme.COLOR_FONDO);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_FACTURAS);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("Línea de Factura");
        lblTitulo.setFont(UITheme.FUENTE_TITULO);
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblIcono = new JLabel(UITheme.ICONO_FACTURAS);
        lblIcono.setFont(UITheme.FUENTE_ICONO_MEDIANO);
        lblIcono.setForeground(Color.WHITE);

        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(lblIcono, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel de campos
        JPanel camposPanel = new JPanel(new GridBagLayout());
        camposPanel.setBackground(Color.WHITE);
        camposPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 10, 15);

        int fila = 0;

        // Panel de ayuda
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel ayudaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ayudaPanel.setBackground(new Color(230, 240, 255));
        ayudaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_INFO, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel lblAyuda = new JLabel(
                "<html><b>💡 Consejo:</b> Escribe el código o nombre del producto para buscarlo rápidamente</html>");
        lblAyuda.setFont(UITheme.FUENTE_SUBTITULO);
        lblAyuda.setForeground(UITheme.COLOR_INFO.darker());
        ayudaPanel.add(lblAyuda);

        camposPanel.add(ayudaPanel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 0, 10, 15);

        agregarCampo(camposPanel, "Producto:", txtProducto, true, fila++, gbc);

        gbc.insets = new Insets(10, 0, 10, 15);
        agregarCampo(camposPanel, "Cantidad:", txtCantidad, true, fila++, gbc);
        agregarCampo(camposPanel, "Precio Unitario:", txtPrecio, true, fila++, gbc);
        agregarCampo(camposPanel, "Descuento %:", txtDescuento, false, fila++, gbc);
        agregarCampo(camposPanel, "IVA %:", txtPorcentajeIva, true, fila++, gbc);
        agregarCampo(camposPanel, "Retención %:", txtPorcentajeRetencion, false, fila++, gbc);

        // Separador
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 15, 0);
        camposPanel.add(new JSeparator(), gbc);

        // Resultados
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 15);

        agregarResultado(camposPanel, "Subtotal:", lblSubtotal, fila++, gbc);
        agregarResultado(camposPanel, "Importe IVA:", lblImporteIva, fila++, gbc);
        agregarResultado(camposPanel, "Importe Retención:", lblImporteRetencion, fila++, gbc);

        // Separador
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        camposPanel.add(new JSeparator(), gbc);

        // Total
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 15);
        agregarResultado(camposPanel, "TOTAL LÍNEA:", lblTotal, fila++, gbc);

        JScrollPane scrollPane = new JScrollPane(camposPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesPanel.setBackground(UITheme.COLOR_FONDO);
        botonesPanel.add(btnCancelar);
        botonesPanel.add(btnAceptar);

        mainPanel.add(botonesPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void agregarCampo(JPanel panel, String label, JComponent campo,
            boolean requerido, int fila, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        if (requerido) {
            lbl.setText(label + " *");
            lbl.setForeground(UITheme.COLOR_PELIGRO);
        } else {
            lbl.setForeground(Color.DARK_GRAY);
        }
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(campo, gbc);
    }

    private void agregarResultado(JPanel panel, String label, JLabel valor,
            int fila, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        lbl.setForeground(UITheme.COLOR_FACTURAS);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(valor, gbc);
        gbc.anchor = GridBagConstraints.WEST;
    }

    private void filtrarProductos() {
        String texto = txtProducto.getText().trim().toLowerCase();
        modeloProductos.clear();

        if (texto.isEmpty()) {
            popupProductos.setVisible(false);
            return;
        }

        List<Producto> filtrados = todosLosProductos.stream()
                .filter(p -> {
                    String codigo = p.getCodigo() != null ? p.getCodigo().toLowerCase() : "";
                    String nombre = p.getNombre() != null ? p.getNombre().toLowerCase() : "";
                    return codigo.contains(texto) || nombre.contains(texto);
                })
                .limit(10)
                .toList();

        filtrados.forEach(modeloProductos::addElement);

        if (!filtrados.isEmpty()) {
            boolean txtProductoTieneFoco = txtProducto.hasFocus();
            popupProductos.show(txtProducto, 0, txtProducto.getHeight());

            if (txtProductoTieneFoco) {
                SwingUtilities.invokeLater(() -> {
                    txtProducto.requestFocusInWindow();
                });
            }
        } else {
            popupProductos.setVisible(false);
        }
    }

    private void seleccionarProducto() {
        productoSeleccionado = listaProductos.getSelectedValue();
        if (productoSeleccionado == null)
            return;

        // Crear el texto directamente sin reasignaciones
        String texto = productoSeleccionado.getCodigo() != null &&
                !productoSeleccionado.getCodigo().isEmpty()
                        ? productoSeleccionado.getCodigo() + " - " + productoSeleccionado.getNombre()
                        : productoSeleccionado.getNombre();

        txtProducto.setText(texto);
        txtProducto.setToolTipText(texto);

        SwingUtilities.invokeLater(() -> {
            int length = texto.length();
            txtProducto.setCaretPosition(length);
        });

        popupProductos.setVisible(false);
        autoCompletarDesdeProducto(productoSeleccionado);
    }

    private void configurarCalculoAutomatico() {
        DocumentListener calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calcular();
            }

            public void removeUpdate(DocumentEvent e) {
                calcular();
            }

            public void changedUpdate(DocumentEvent e) {
                calcular();
            }
        };

        txtCantidad.getDocument().addDocumentListener(calcListener);
        txtPrecio.getDocument().addDocumentListener(calcListener);
        txtDescuento.getDocument().addDocumentListener(calcListener);
        txtPorcentajeIva.getDocument().addDocumentListener(calcListener);
        txtPorcentajeRetencion.getDocument().addDocumentListener(calcListener);
    }

    private void calcular() {
        if (calculando)
            return;

        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            BigDecimal descuento = new BigDecimal(txtDescuento.getText().trim());
            BigDecimal iva = new BigDecimal(txtPorcentajeIva.getText().trim());
            BigDecimal retencion = new BigDecimal(txtPorcentajeRetencion.getText().trim());

            LineaFactura temp = new LineaFactura();
            temp.setCantidad(cantidad);
            temp.setPrecioUnitario(precio);
            temp.setPrecioBase(precio);
            temp.setDescuento(descuento);
            temp.setPorcentajeIva(iva);
            temp.setPorcentajeRetencion(retencion);

            facturaController.calcularLinea(temp);

            lblSubtotal.setText(formatearMoneda(temp.getSubtotalLinea()));
            lblImporteIva.setText(formatearMoneda(temp.getImporteIva()));
            lblImporteRetencion.setText(formatearMoneda(temp.getImporteRetencion()));
            lblTotal.setText(formatearMoneda(temp.getTotalLinea()));

        } catch (NumberFormatException e) {
            // Formato inválido - mantener valores actuales
        }
    }

    private void autoCompletarDesdeProducto(Producto producto) {
        if (producto == null)
            return;

        calculando = true;

        // Precio
        BigDecimal precio = producto.getPrecio();
        if (precio == null) {
            precio = producto.getPrecioBase();
        }
        if (precio != null) {
            txtPrecio.setText(precio.toString());
        }

        // IVA
        if (producto.getTipoIva() != null) {
            txtPorcentajeIva.setText(producto.getTipoIva().toString());
        }

        // Retención
        if (producto.getTipoRetencion() != null &&
                producto.getTipoRetencion().compareTo(BigDecimal.ZERO) > 0) {
            txtPorcentajeRetencion.setText(producto.getTipoRetencion().toString());
        }

        calculando = false;
        calcular();
    }

    private void cargarLineaExistente() {
        calculando = true;

        productoSeleccionado = facturaController.obtenerProductoPorId(linea.getIdProducto());
        if (productoSeleccionado != null) {
            String texto = productoSeleccionado.getNombre();
            if (productoSeleccionado.getCodigo() != null && !productoSeleccionado.getCodigo().isEmpty()) {
                texto = productoSeleccionado.getCodigo() + " - " + productoSeleccionado.getNombre();
            }
            txtProducto.setText(texto);
        }

        txtCantidad.setText(linea.getCantidad().toString());
        txtPrecio.setText(linea.getPrecioUnitario().toString());
        txtDescuento.setText(linea.getDescuento().toString());
        txtPorcentajeIva.setText(linea.getPorcentajeIva().toString());
        txtPorcentajeRetencion.setText(linea.getPorcentajeRetencion().toString());

        calculando = false;
        calcular();
    }

    private void aceptar() {
        if (!validar()) {
            return;
        }

        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar un producto válido",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        linea.setIdProducto(productoSeleccionado.getIdProducto());
        linea.setProducto(productoSeleccionado);
        linea.setCantidad(new BigDecimal(txtCantidad.getText().trim()));
        linea.setPrecioUnitario(new BigDecimal(txtPrecio.getText().trim()));
        linea.setPrecioBase(new BigDecimal(txtPrecio.getText().trim()));
        linea.setDescuento(new BigDecimal(txtDescuento.getText().trim()));
        linea.setPorcentajeIva(new BigDecimal(txtPorcentajeIva.getText().trim()));
        linea.setPorcentajeRetencion(new BigDecimal(txtPorcentajeRetencion.getText().trim()));

        facturaController.calcularLinea(linea);

        confirmado = true;
        dispose();
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (productoSeleccionado == null) {
            errores.append("• Debe seleccionar un producto válido\n");
        }

        try {
            BigDecimal cant = new BigDecimal(txtCantidad.getText().trim());
            if (cant.compareTo(BigDecimal.ZERO) <= 0) {
                errores.append("• La cantidad debe ser mayor que cero\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• Cantidad inválida\n");
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("• El precio no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• Precio inválido\n");
        }

        try {
            BigDecimal desc = new BigDecimal(txtDescuento.getText().trim());
            if (desc.compareTo(BigDecimal.ZERO) < 0 ||
                    desc.compareTo(new BigDecimal("100")) > 0) {
                errores.append("• El descuento debe estar entre 0 y 100\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• Descuento inválido\n");
        }

        if (errores.length() > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Corrige los siguientes errores:\n\n" + errores.toString(),
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null)
            return "0,00 €";
        return String.format("%,.2f €", valor);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public LineaFactura getLinea() {
        return linea;
    }
}