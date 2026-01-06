package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.model.entity.TipoIva;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diálogo para agregar o editar una línea de factura
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 * Con búsqueda de productos por código o nombre
 */
public class LineaFacturaDialog extends JDialog {
    
    private final FacturaController facturaController;
    private final ProductoController productoController;
    private LineaFactura linea;
    private boolean confirmado = false;
    
    // Componentes
    private JTextField txtBuscarProducto;
    private JComboBox<Producto> cmbProducto;
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
    
    private boolean calculando = false; // Flag para evitar bucles
    private List<Producto> todosLosProductos; // Lista completa de productos

    public LineaFacturaDialog(JFrame parent, FacturaController controller, LineaFactura lineaEditar) {
        super(parent, lineaEditar == null ? "Agregar Línea" : "Editar Línea", true);
        this.facturaController = controller;
        this.productoController = new ProductoController();
        this.linea = lineaEditar != null ? lineaEditar : new LineaFactura();
        
        // Cargar todos los productos
        todosLosProductos = facturaController.obtenerProductos();
        
        initComponents();
        setupLayout();
        configurarCalculoAutomatico();
        
        if (lineaEditar != null) {
            cargarLineaExistente();
        }
        
        setSize(700, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        // Campo de búsqueda de productos usando UIHelper
        txtBuscarProducto = UIHelper.crearCampoTexto(25);
        txtBuscarProducto.setPreferredSize(new Dimension(300, 35));
        
        // Listener para búsqueda en tiempo real
        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { buscarProductos(); }
            @Override
            public void removeUpdate(DocumentEvent e) { buscarProductos(); }
            @Override
            public void changedUpdate(DocumentEvent e) { buscarProductos(); }
        });
        
        // ComboBox de productos usando UIHelper
        cmbProducto = UIHelper.crearComboBox();
        actualizarComboProductos(todosLosProductos);
        
        // Listener para auto-completar al seleccionar producto
        cmbProducto.addActionListener(e -> {
            Producto p = (Producto) cmbProducto.getSelectedItem();
            if (p != null && !calculando) {
                autoCompletarDesdeProducto(p);
            }
        });
        
        // Campos de texto usando UIHelper
        txtCantidad = UIHelper.crearCampoTexto(10);
        txtCantidad.setText("1");
        
        txtPrecio = UIHelper.crearCampoTexto(10);
        txtPrecio.setText("0.00");
        
        txtDescuento = UIHelper.crearCampoTexto(5);
        txtDescuento.setText("0");
        
        txtPorcentajeIva = UIHelper.crearCampoTexto(5);
        txtPorcentajeIva.setText("21.00");
        
        txtPorcentajeRetencion = UIHelper.crearCampoTexto(5);
        txtPorcentajeRetencion.setText("0");
        
        // Labels de resultados
        lblSubtotal = new JLabel("0,00 €");
        lblImporteIva = new JLabel("0,00 €");
        lblImporteRetencion = new JLabel("0,00 €");
        lblTotal = new JLabel("0,00 €");
        
        lblSubtotal.setFont(UITheme.FUENTE_RESULTADOS);
        lblImporteIva.setFont(UITheme.FUENTE_RESULTADOS);
        lblImporteRetencion.setFont(UITheme.FUENTE_RESULTADOS);
        lblTotal.setFont(UITheme.FUENTE_TOTAL);
        lblTotal.setForeground(UITheme.COLOR_FACTURAS);
        
        // Botones usando UIHelper
        btnAceptar = UIHelper.crearBotonAccion("guardar", "Aceptar");
        btnAceptar.addActionListener(e -> aceptar());
        
        btnCancelar = UIHelper.crearBotonAccion("cancelar", "Cancelar");
        btnCancelar.addActionListener(e -> dispose());
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(UITheme.COLOR_FONDO);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header usando el estilo de UITheme
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
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);
        
        int fila = 0;
        
        // Campo de búsqueda
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        
        JLabel lblBuscar = new JLabel(UITheme.ICONO_BUSCAR + " Buscar Producto:");
        lblBuscar.setFont(UITheme.FUENTE_ETIQUETA);
        lblBuscar.setForeground(Color.DARK_GRAY);
        camposPanel.add(lblBuscar, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        camposPanel.add(txtBuscarProducto, gbc);
        
        fila++;
        
        // Nota de ayuda para búsqueda
        gbc.gridx = 1;
        gbc.gridy = fila;
        gbc.insets = new Insets(0, 0, 8, 0);
        JLabel lblAyuda = new JLabel("<html><i>Busca por código o nombre del producto</i></html>");
        lblAyuda.setFont(UITheme.FUENTE_SUBTITULO);
        lblAyuda.setForeground(new Color(100, 100, 100));
        camposPanel.add(lblAyuda, gbc);
        
        fila++;
        gbc.insets = new Insets(8, 0, 8, 15);
        
        // Producto
        agregarCampo(camposPanel, "Producto:", cmbProducto, true, fila++, gbc);
        
        // Cantidad
        agregarCampo(camposPanel, "Cantidad:", txtCantidad, true, fila++, gbc);
        
        // Precio unitario
        agregarCampo(camposPanel, "Precio Unitario:", txtPrecio, true, fila++, gbc);
        
        // Descuento
        agregarCampo(camposPanel, "Descuento %:", txtDescuento, false, fila++, gbc);
        
        // IVA
        agregarCampo(camposPanel, "IVA %:", txtPorcentajeIva, true, fila++, gbc);
        
        // Retención
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
        
        mainPanel.add(camposPanel, BorderLayout.CENTER);
        
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
    
    private void configurarCalculoAutomatico() {
        DocumentListener calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calcular(); }
            public void removeUpdate(DocumentEvent e) { calcular(); }
            public void changedUpdate(DocumentEvent e) { calcular(); }
        };
        
        txtCantidad.getDocument().addDocumentListener(calcListener);
        txtPrecio.getDocument().addDocumentListener(calcListener);
        txtDescuento.getDocument().addDocumentListener(calcListener);
        txtPorcentajeIva.getDocument().addDocumentListener(calcListener);
        txtPorcentajeRetencion.getDocument().addDocumentListener(calcListener);
    }
    
    private void calcular() {
        if (calculando) return;
        
        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            BigDecimal descuento = new BigDecimal(txtDescuento.getText().trim());
            BigDecimal iva = new BigDecimal(txtPorcentajeIva.getText().trim());
            BigDecimal retencion = new BigDecimal(txtPorcentajeRetencion.getText().trim());
            
            // Crear línea temporal para calcular
            LineaFactura temp = new LineaFactura();
            temp.setCantidad(cantidad);
            temp.setPrecioUnitario(precio);
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
        calculando = true;
        
        // Precio
        BigDecimal precio = producto.getPrecioEfectivo();
        if (precio != null) {
            txtPrecio.setText(precio.toString());
        }
        
        // IVA
        if (producto.getIdTipoIva() != null) {
            TipoIva tipoIva = productoController.obtenerTipoIvaPorId(producto.getIdTipoIva());
            if (tipoIva != null) {
                txtPorcentajeIva.setText(tipoIva.getPorcentaje().toString());
            }
        }
        
        // Retención
        if (producto.getTipoRetencion() != null) {
            txtPorcentajeRetencion.setText(producto.getTipoRetencion().toString());
        }
        
        calculando = false;
        calcular();
    }
    
    /**
     * Busca productos por código o nombre
     */
    private void buscarProductos() {
        String termino = txtBuscarProducto.getText().trim().toLowerCase();
        
        if (termino.isEmpty()) {
            // Mostrar todos los productos
            actualizarComboProductos(todosLosProductos);
            return;
        }
        
        // Filtrar productos por código o nombre
        List<Producto> productosFiltrados = todosLosProductos.stream()
            .filter(p -> {
                String codigo = p.getCodigo() != null ? p.getCodigo().toLowerCase() : "";
                String nombre = p.getNombre().toLowerCase();
                return codigo.contains(termino) || nombre.contains(termino);
            })
            .collect(Collectors.toList());
        
        actualizarComboProductos(productosFiltrados);
    }
    
    /**
     * Actualiza el combo de productos con la lista filtrada
     */
    private void actualizarComboProductos(List<Producto> productos) {
        calculando = true; // Evitar que se disparen eventos durante la actualización
        
        Producto productoSeleccionado = (Producto) cmbProducto.getSelectedItem();
        
        cmbProducto.removeAllItems();
        for (Producto p : productos) {
            cmbProducto.addItem(p);
        }
        
        // Intentar mantener la selección anterior si existe en la nueva lista
        if (productoSeleccionado != null && productos.contains(productoSeleccionado)) {
            cmbProducto.setSelectedItem(productoSeleccionado);
        } else if (!productos.isEmpty()) {
            cmbProducto.setSelectedIndex(0);
        }
        
        calculando = false;
    }
    
    private void cargarLineaExistente() {
        calculando = true;
        
        // Cargar producto en el campo de búsqueda
        Producto producto = facturaController.obtenerProductoPorId(linea.getIdProducto());
        if (producto != null) {
            // Mostrar el nombre del producto en el campo de búsqueda
            txtBuscarProducto.setText(producto.getNombre());
            
            // Seleccionar producto en el combo
            for (int i = 0; i < cmbProducto.getItemCount(); i++) {
                if (cmbProducto.getItemAt(i).getIdProducto().equals(linea.getIdProducto())) {
                    cmbProducto.setSelectedIndex(i);
                    break;
                }
            }
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
        
        Producto producto = (Producto) cmbProducto.getSelectedItem();
        
        linea.setIdProducto(producto.getIdProducto());
        linea.setProducto(producto);
        linea.setCantidad(new BigDecimal(txtCantidad.getText().trim()));
        linea.setPrecioUnitario(new BigDecimal(txtPrecio.getText().trim()));
        linea.setDescuento(new BigDecimal(txtDescuento.getText().trim()));
        linea.setPorcentajeIva(new BigDecimal(txtPorcentajeIva.getText().trim()));
        linea.setPorcentajeRetencion(new BigDecimal(txtPorcentajeRetencion.getText().trim()));
        
        // Calcular importes finales
        facturaController.calcularLinea(linea);
        
        confirmado = true;
        dispose();
    }
    
    private boolean validar() {
        StringBuilder errores = new StringBuilder();
        
        if (cmbProducto.getSelectedItem() == null) {
            errores.append("• Debe seleccionar un producto\n");
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
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        
        return true;
    }
    
    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) return "0,00 €";
        return String.format("%,.2f €", valor);
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public LineaFactura getLinea() {
        return linea;
    }
}