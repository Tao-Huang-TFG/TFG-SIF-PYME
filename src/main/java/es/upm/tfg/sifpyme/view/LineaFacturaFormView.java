package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.model.entity.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Panel para agregar o editar una línea de factura
 * REFACTORIZADO: Ahora hereda de BaseFormView para reutilizar funcionalidad
 * común
 * y se integra en CardLayout en lugar de ser un diálogo modal separado
 */
public class LineaFacturaFormView extends BaseFormView<LineaFactura> {

    // Callback para notificar al formulario principal
    public interface LineaCallback {
        void onLineaConfirmada(LineaFactura linea, boolean esEdicion, int indiceEditar);
    }

    private final FacturaController facturaController;
    private final ProductoController productoController;
    private LineaFactura lineaEditar;
    private final LineaCallback callback;
    private final int indiceEditar;

    // Componentes específicos
    private JTextField txtNombreProducto;
    private JTextField txtCantidad;
    private JTextField txtPrecioUnitario;
    private JTextField txtPrecioBase;
    private JTextField txtDescuento;
    private JTextField txtPorcentajeIva;
    private JTextField txtPorcentajeRetencion;

    private JLabel lblSubtotal;
    private JLabel lblImporteIva;
    private JLabel lblImporteRetencion;
    private JLabel lblTotal;

    private JButton btnBuscarProducto;

    private JPopupMenu popupProductos;
    private JList<Producto> listaProductos;
    private DefaultListModel<Producto> modeloProductos;

    private Producto productoPlantilla;
    private List<Producto> todosLosProductos;

    // Flags para evitar bucles en cálculo bidireccional
    private boolean calculandoPrecio = false;
    private boolean calculandoPrecioBase = false;
    private boolean calculandoTotales = false;

    public LineaFacturaFormView(CardLayout cardLayout, JPanel cardPanel,
            String nombreCardAnterior, FacturaController controller,
            LineaFactura lineaEditar, LineaCallback callback, int indiceEditar) {
        super(cardLayout, cardPanel, lineaEditar);

        this.nombreCardListaCustom = nombreCardAnterior;
        this.callback = callback;
        this.indiceEditar = indiceEditar;
        this.facturaController = controller;
        this.productoController = new ProductoController();

        if (lineaEditar != null) {
            this.lineaEditar = lineaEditar;
            this.entidadEditar = this.lineaEditar;
            this.modoEdicion = true;
        } else {
            this.lineaEditar = new LineaFactura();
            this.entidadEditar = this.lineaEditar;
            this.modoEdicion = false;
        }

        if (modoEdicion && entidadEditar != null) {
            entidadEditar.setTempIndiceEditar(indiceEditar);
        }

        // Cargar productos para el buscador
        todosLosProductos = facturaController.obtenerProductos();

        // Configurar cálculo automático después de inicializar componentes
        SwingUtilities.invokeLater(() -> configurarCalculoAutomatico());

        afterConstruction();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = UITheme.COLOR_FACTURAS;
    }

    @Override
    protected String getTituloFormulario() {
        return modoEdicion ? "Editar Línea de Factura" : "Nueva Línea de Factura";
    }

    @Override
    protected String getSubtituloFormulario() {
        return modoEdicion ? "Modifica los datos de la línea de factura" : "Agrega una nueva línea a la factura";
    }

    @Override
    protected String getIconoFormulario() {
        return UITheme.ICONO_FACTURAS;
    }

    @Override
    protected String getNombreCardLista() {
        // Usamos el nombre personalizado pasado en el constructor
        return nombreCardListaCustom != null ? nombreCardListaCustom : "formularioFactura";
    }

    // Variable para almacenar el nombre del card anterior
    private String nombreCardListaCustom;

    @Override
    protected void inicializarCamposEspecificos() {
        // Campo de nombre de producto
        txtNombreProducto = crearCampoTexto(40);
        txtNombreProducto.setToolTipText("Nombre/descripción del producto o servicio");

        // Botón para buscar producto como plantilla
        btnBuscarProducto = UIHelper.crearBotonAccion("buscar", "Buscar Plantilla");
        btnBuscarProducto.addActionListener(e -> mostrarBuscadorProducto());

        // Campos numéricos
        txtCantidad = crearCampoTexto(10);
        txtCantidad.setText("1");

        txtPrecioUnitario = crearCampoTexto(10);
        txtPrecioUnitario.setText("0.00");

        txtPrecioBase = crearCampoTexto(10);
        txtPrecioBase.setText("0.00");

        txtDescuento = crearCampoTexto(5);
        txtDescuento.setText("0");

        txtPorcentajeIva = crearCampoTexto(5);
        txtPorcentajeIva.setText("21.00");

        txtPorcentajeRetencion = crearCampoTexto(5);
        txtPorcentajeRetencion.setText("0");

        // Labels de resultados
        lblSubtotal = new JLabel("0,00 €");
        lblImporteIva = new JLabel("0,00 €");
        lblImporteRetencion = new JLabel("0,00 €");
        lblTotal = new JLabel("0,00 €");

        lblSubtotal.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteIva.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteRetencion.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotal.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTotal.setForeground(COLOR_PRIMARIO);

        // Configurar popup de productos
        configurarPopupProductos();
    }

    @Override
    protected JPanel crearPanelCampos() {
        // Usar UIHelper para crear panel de ayuda estilizada
        JPanel panelAyuda = UIHelper.crearSeccionPanelConAyudaEstilizada(
                "Información Importante",
                "Puedes escribir libremente el nombre del producto o usar 'Buscar Plantilla' " +
                        "para cargar datos desde tu catálogo. Los precios se calculan automáticamente " +
                        "entre Precio Base (sin IVA) y Precio Unitario (con IVA).",
                COLOR_PRIMARIO);

        // Panel principal con GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 25, 0);

        // Añadir panel de ayuda
        panel.add(panelAyuda, gbc);

        // Panel para campos de formulario
        JPanel camposPanel = new JPanel(new GridBagLayout());
        camposPanel.setBackground(Color.WHITE);
        camposPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbcCampos = new GridBagConstraints();
        gbcCampos.anchor = GridBagConstraints.WEST;
        gbcCampos.insets = new Insets(8, 0, 8, 15);
        gbcCampos.fill = GridBagConstraints.HORIZONTAL;
        gbcCampos.weightx = 1.0;

        int fila = 0;

        // Nombre del producto con botón buscar
        gbcCampos.gridx = 0;
        gbcCampos.gridy = fila;
        gbcCampos.gridwidth = 1;

        JLabel lblNombre = new JLabel("Nombre/Descripción:");
        lblNombre.setFont(UITheme.FUENTE_ETIQUETA);
        lblNombre.setForeground(UITheme.COLOR_PELIGRO);
        lblNombre.setText("Nombre/Descripción: *");
        camposPanel.add(lblNombre, gbcCampos);

        gbcCampos.gridx = 1;
        gbcCampos.fill = GridBagConstraints.HORIZONTAL;

        // Panel para campo + botón
        JPanel nombrePanel = new JPanel(new BorderLayout(10, 0));
        nombrePanel.setOpaque(false);
        nombrePanel.add(txtNombreProducto, BorderLayout.CENTER);
        nombrePanel.add(btnBuscarProducto, BorderLayout.EAST);
        camposPanel.add(nombrePanel, gbcCampos);

        // Resto de campos usando métodos de BaseFormView
        fila++;
        addFormField(camposPanel, "Cantidad:", txtCantidad, true, fila++);
        addFormField(camposPanel, "IVA %:", txtPorcentajeIva, true, fila++);
        addFormField(camposPanel, "Precio Base (sin IVA):", txtPrecioBase, true, fila++);
        addFormField(camposPanel, "Precio Unitario (con IVA):", txtPrecioUnitario, true, fila++);
        addFormField(camposPanel, "Descuento %:", txtDescuento, false, fila++);
        addFormField(camposPanel, "Retención %:", txtPorcentajeRetencion, false, fila++);

        // Totales
        gbcCampos.gridwidth = 1;
        gbcCampos.insets = new Insets(5, 0, 5, 15);

        agregarResultado(camposPanel, "Subtotal:", lblSubtotal, fila++, gbcCampos);
        agregarResultado(camposPanel, "Importe IVA:", lblImporteIva, fila++, gbcCampos);
        agregarResultado(camposPanel, "Importe Retención:", lblImporteRetencion, fila++, gbcCampos);

        // Separador final
        gbcCampos.gridx = 0;
        gbcCampos.gridy = fila++;
        gbcCampos.gridwidth = 2;
        gbcCampos.insets = new Insets(10, 0, 10, 0);
        camposPanel.add(new JSeparator(), gbcCampos);

        // Total
        gbcCampos.gridwidth = 1;
        gbcCampos.insets = new Insets(5, 0, 5, 15);
        agregarResultado(camposPanel, "TOTAL LÍNEA:", lblTotal, fila++, gbcCampos);

        // Añadir camposPanel al panel principal
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(camposPanel, gbc);

        return panel;
    }

    @Override
    protected void cargarDatosEntidad() {
        if (entidadEditar != null) {
            txtNombreProducto.setText(entidadEditar.getNombreProducto());
            txtCantidad.setText(entidadEditar.getCantidad().toString());
            txtPrecioUnitario.setText(entidadEditar.getPrecioUnitario().toString());
            txtPrecioBase.setText(entidadEditar.getPrecioBase().toString());
            txtDescuento.setText(entidadEditar.getDescuento().toString());
            txtPorcentajeIva.setText(entidadEditar.getPorcentajeIva().toString());
            txtPorcentajeRetencion.setText(entidadEditar.getPorcentajeRetencion().toString());

            calcularTotales();
        }
    }

    @Override
    protected boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombreProducto.getText().trim().isEmpty()) {
            errores.append("• Nombre/descripción del producto es obligatorio\n");
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
            BigDecimal precio = new BigDecimal(txtPrecioUnitario.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("• El precio no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• Precio inválido\n");
        }

        try {
            BigDecimal precioBase = new BigDecimal(txtPrecioBase.getText().trim());
            if (precioBase.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("• El precio base no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• Precio base inválido\n");
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

        try {
            BigDecimal iva = new BigDecimal(txtPorcentajeIva.getText().trim());
            if (iva.compareTo(BigDecimal.ZERO) < 0 ||
                    iva.compareTo(new BigDecimal("100")) > 0) {
                errores.append("• El IVA debe estar entre 0 y 100\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• IVA inválido\n");
        }

        if (errores.length() > 0) {
            mostrarErroresValidacion(errores);
            return false;
        }

        return true;
    }

    @Override
    protected boolean guardarEntidad() {
        try {
            // usar entidadEditar (ya sincronizada con lineaEditar)
            LineaFactura linea = (LineaFactura) entidadEditar;
            linea.setNombreProducto(txtNombreProducto.getText().trim());
            linea.setCantidad(new BigDecimal(txtCantidad.getText().trim()));
            linea.setPrecioUnitario(new BigDecimal(txtPrecioUnitario.getText().trim()));
            linea.setPrecioBase(new BigDecimal(txtPrecioBase.getText().trim()));
            linea.setDescuento(new BigDecimal(txtDescuento.getText().trim()));
            linea.setPorcentajeIva(new BigDecimal(txtPorcentajeIva.getText().trim()));
            linea.setPorcentajeRetencion(new BigDecimal(txtPorcentajeRetencion.getText().trim()));
            linea.setIdProducto(null);

            // Calcular importes finales
            facturaController.calcularLinea(linea);

            // Notificar al callback
            if (callback != null) {
                callback.onLineaConfirmada(linea, modoEdicion, indiceEditar);
            }

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar la línea: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== MÉTODOS ESPECÍFICOS ====================

    private void configurarPopupProductos() {
        modeloProductos = new DefaultListModel<>();
        listaProductos = new JList<>(modeloProductos);
        listaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaProductos.setVisibleRowCount(8);

        listaProductos.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String display = value.getNombre();
            if (value.getCodigo() != null && !value.getCodigo().isEmpty()) {
                display = value.getCodigo() + " - " + value.getNombre();
            }
            JLabel lbl = new JLabel(display);
            lbl.setFont(UITheme.FUENTE_CAMPO);
            if (isSelected) {
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_PRIMARIO);
                lbl.setForeground(Color.WHITE);
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return lbl;
        });

        popupProductos = new JPopupMenu();
        popupProductos.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDE));
        JScrollPane scrollPane = new JScrollPane(listaProductos);
        scrollPane.setPreferredSize(new Dimension(500, 250));
        popupProductos.add(scrollPane);

        listaProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1)
                    seleccionarProductoPlantilla();
            }
        });

        listaProductos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    seleccionarProductoPlantilla();
            }
        });
    }

    private void configurarCalculoAutomatico() {
        DocumentListener calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calcularTotales();
            }

            public void removeUpdate(DocumentEvent e) {
                calcularTotales();
            }

            public void changedUpdate(DocumentEvent e) {
                calcularTotales();
            }
        };

        txtPrecioUnitario.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calcularPrecioBase();
            }

            public void removeUpdate(DocumentEvent e) {
                calcularPrecioBase();
            }

            public void changedUpdate(DocumentEvent e) {
                calcularPrecioBase();
            }
        });

        txtPrecioBase.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calcularPrecioUnitario();
            }

            public void removeUpdate(DocumentEvent e) {
                calcularPrecioUnitario();
            }

            public void changedUpdate(DocumentEvent e) {
                calcularPrecioUnitario();
            }
        });

        txtCantidad.getDocument().addDocumentListener(calcListener);
        txtDescuento.getDocument().addDocumentListener(calcListener);
        txtPorcentajeIva.getDocument().addDocumentListener(calcListener);
        txtPorcentajeRetencion.getDocument().addDocumentListener(calcListener);
    }

    private void calcularPrecioBase() {
        if (calculandoPrecioBase)
            return;

        String precioStr = txtPrecioUnitario.getText().trim();
        if (precioStr.isEmpty())
            return;

        try {
            calculandoPrecio = true;
            BigDecimal precio = new BigDecimal(precioStr);
            String ivaStr = txtPorcentajeIva.getText().trim();

            if (!ivaStr.isEmpty() && precio.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentajeIva = new BigDecimal(ivaStr);
                BigDecimal divisor = BigDecimal.ONE
                        .add(porcentajeIva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                BigDecimal precioBase = precio.divide(divisor, 2, RoundingMode.HALF_UP);
                txtPrecioBase.setText(precioBase.toString());
            }
        } catch (NumberFormatException e) {
            // Formato inválido
        } finally {
            calculandoPrecio = false;
        }
    }

    private void calcularPrecioUnitario() {
        if (calculandoPrecio)
            return;

        String precioBaseStr = txtPrecioBase.getText().trim();
        if (precioBaseStr.isEmpty())
            return;

        try {
            calculandoPrecioBase = true;
            BigDecimal precioBase = new BigDecimal(precioBaseStr);
            String ivaStr = txtPorcentajeIva.getText().trim();

            if (!ivaStr.isEmpty() && precioBase.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentajeIva = new BigDecimal(ivaStr);
                BigDecimal multiplicador = BigDecimal.ONE
                        .add(porcentajeIva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                BigDecimal precio = precioBase.multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);
                txtPrecioUnitario.setText(precio.toString());
            }
        } catch (NumberFormatException e) {
            // Formato inválido
        } finally {
            calculandoPrecioBase = false;
        }
    }

    private void calcularTotales() {
        if (calculandoTotales || calculandoPrecio || calculandoPrecioBase)
            return;

        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
            BigDecimal precio = new BigDecimal(txtPrecioUnitario.getText().trim());
            BigDecimal precioBase = new BigDecimal(txtPrecioBase.getText().trim());
            BigDecimal descuento = new BigDecimal(txtDescuento.getText().trim());
            BigDecimal iva = new BigDecimal(txtPorcentajeIva.getText().trim());
            BigDecimal retencion = new BigDecimal(txtPorcentajeRetencion.getText().trim());

            LineaFactura temp = new LineaFactura();
            temp.setCantidad(cantidad);
            temp.setPrecioUnitario(precio);
            temp.setPrecioBase(precioBase);
            temp.setDescuento(descuento);
            temp.setPorcentajeIva(iva);
            temp.setPorcentajeRetencion(retencion);

            facturaController.calcularLinea(temp);

            lblSubtotal.setText(formatearMoneda(temp.getSubtotalLinea()));
            lblImporteIva.setText(formatearMoneda(temp.getImporteIva()));
            lblImporteRetencion.setText(formatearMoneda(temp.getImporteRetencion()));
            lblTotal.setText(formatearMoneda(temp.getTotalLinea()));

        } catch (NumberFormatException e) {
            // Formato inválido
        }
    }

    private void mostrarBuscadorProducto() {
        modeloProductos.clear();
        todosLosProductos.forEach(modeloProductos::addElement);

        if (!todosLosProductos.isEmpty()) {
            Point location = btnBuscarProducto.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(location, btnBuscarProducto);

            popupProductos.show(btnBuscarProducto, 0, btnBuscarProducto.getHeight());
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay productos registrados en el sistema",
                    "Sin Productos",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void seleccionarProductoPlantilla() {
        productoPlantilla = listaProductos.getSelectedValue();
        if (productoPlantilla == null)
            return;

        popupProductos.setVisible(false);
        cargarDatosDesdeProductoPlantilla(productoPlantilla);
    }

    private void cargarDatosDesdeProductoPlantilla(Producto producto) {
        if (producto == null)
            return;

        calculandoPrecio = true;
        calculandoPrecioBase = true;

        txtNombreProducto.setText(producto.getNombre());

        if (producto.getPrecio() != null) {
            txtPrecioUnitario.setText(producto.getPrecio().toString());
        }
        if (producto.getPrecioBase() != null) {
            txtPrecioBase.setText(producto.getPrecioBase().toString());
        }
        if (producto.getTipoIva() != null) {
            txtPorcentajeIva.setText(producto.getTipoIva().toString());
        }
        if (producto.getTipoRetencion() != null &&
                producto.getTipoRetencion().compareTo(BigDecimal.ZERO) > 0) {
            txtPorcentajeRetencion.setText(producto.getTipoRetencion().toString());
        }

        calculandoPrecio = false;
        calculandoPrecioBase = false;
        calcularTotales();

        JOptionPane.showMessageDialog(
                this,
                "Datos cargados desde el producto.\nPuedes modificarlos libremente para esta factura.",
                "Plantilla Cargada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void agregarResultado(JPanel panel, String label, JLabel valor,
            int fila, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        lbl.setForeground(COLOR_PRIMARIO);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(valor, gbc);
        gbc.anchor = GridBagConstraints.WEST;
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null)
            return "0,00 €";
        return String.format("%,.2f €", valor);
    }

    @Override
    protected void configurarBotones() {
        super.configurarBotones(); // Usa los botones estándar de BaseFormView
    }

    @Override
    protected String getMensajeExito() {
        return modoEdicion ? "Línea actualizada exitosamente." : "Línea creada exitosamente.";
    }
}