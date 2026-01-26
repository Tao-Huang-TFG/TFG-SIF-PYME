package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formulario para registro/edición de producto
 * CORREGIDO: Simplificado - eliminada referencia a TipoIva
 * El producto ahora tiene tipo_iva como BigDecimal directamente
 * MEJORADO: Agregado cálculo de precio final con impuestos
 */
public class ProductoFormView extends BaseFormView<Producto> {

    private final ProductoController controller;

    // Campos específicos de producto
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtPrecioBase;
    private JTextField txtTipoIva; // CAMBIADO: De JComboBox a JTextField
    private JTextField txtTipoRetencion;
    private JTextField txtRecargoEquivalencia;
    
    // Labels para mostrar el precio final
    private JLabel lblSubtotal;
    private JLabel lblImporteIva;
    private JLabel lblImporteRetencion;
    private JLabel lblImporteRecargo;
    private JLabel lblTotal;

    // Flags para evitar bucles infinitos en el cálculo
    private boolean calculandoPrecio = false;
    private boolean calculandoPrecioBase = false;
    private boolean calculandoTotales = false;

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
        afterConstruction();
    }

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel, Producto productoEditar) {
        super(cardLayout, cardPanel, productoEditar);
        this.controller = new ProductoController();
        
        // Configurar cálculo automático después de inicializar componentes
        SwingUtilities.invokeLater(() -> configurarCalculoAutomatico());
        afterConstruction();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = UITheme.COLOR_PRODUCTOS;
    }

    @Override
    protected String getTituloFormulario() {
        return modoEdicion ? "Editar Producto" : "Nuevo Producto";
    }

    @Override
    protected String getSubtituloFormulario() {
        return modoEdicion ? "Modifica los datos del producto" : "Registra un nuevo producto o servicio";
    }

    @Override
    protected String getIconoFormulario() {
        return UITheme.ICONO_PRODUCTOS;
    }

    @Override
    protected String getNombreCardLista() {
        return "listaProductos";
    }

    @Override
    protected void inicializarCamposEspecificos() {
        txtCodigo = UIHelper.crearCampoTexto(20);
        txtNombre = UIHelper.crearCampoTexto(30);
        txtPrecio = UIHelper.crearCampoTexto(15);
        txtPrecioBase = UIHelper.crearCampoTexto(15);

        // CAMBIADO: txtTipoIva ahora es JTextField en vez de JComboBox
        txtTipoIva = UIHelper.crearCampoTexto(10);
        txtTipoIva.setText("21.00");
        txtTipoIva.setPreferredSize(new Dimension(100, 35));

        txtTipoRetencion = UIHelper.crearCampoTexto(10);
        txtTipoRetencion.setText("0.00");

        txtRecargoEquivalencia = UIHelper.crearCampoTexto(10);
        txtRecargoEquivalencia.setText("0.00");
        
        // Inicializar labels para mostrar el precio final
        lblSubtotal = new JLabel("0,00 €");
        lblImporteIva = new JLabel("0,00 €");
        lblImporteRetencion = new JLabel("0,00 €");
        lblImporteRecargo = new JLabel("0,00 €");
        lblTotal = new JLabel("0,00 €");
        
        lblSubtotal.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteIva.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteRetencion.setFont(UITheme.FUENTE_ETIQUETA);
        lblImporteRecargo.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotal.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTotal.setForeground(COLOR_PRIMARIO);
    }

    /**
     * Configura los listeners para el cálculo automático bidireccional
     */
    private void configurarCalculoAutomatico() {
        // Listener para Precio (con IVA) -> calcula PrecioBase
        DocumentListener precioListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcularPrecioBase();
                calcularTotales();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcularPrecioBase();
                calcularTotales();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcularPrecioBase();
                calcularTotales();
            }
        };
        
        txtPrecio.getDocument().addDocumentListener(precioListener);

        // Listener para PrecioBase (sin IVA) -> calcula Precio
        DocumentListener precioBaseListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcularPrecio();
                calcularTotales();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcularPrecio();
                calcularTotales();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcularPrecio();
                calcularTotales();
            }
        };
        
        txtPrecioBase.getDocument().addDocumentListener(precioBaseListener);

        // Listener para cambio de IVA -> recalcula según el campo que esté lleno
        DocumentListener ivaListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recalcularSegunCampoActivo();
                calcularTotales();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recalcularSegunCampoActivo();
                calcularTotales();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                recalcularSegunCampoActivo();
                calcularTotales();
            }
        };
        
        txtTipoIva.getDocument().addDocumentListener(ivaListener);
        
        // Listeners para retención y recargo
        DocumentListener impuestosListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcularTotales();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcularTotales();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcularTotales();
            }
        };
        
        txtTipoRetencion.getDocument().addDocumentListener(impuestosListener);
        txtRecargoEquivalencia.getDocument().addDocumentListener(impuestosListener);
    }

    /**
     * Calcula el PrecioBase a partir del Precio (con IVA)
     */
    private void calcularPrecioBase() {
        if (calculandoPrecioBase)
            return;

        String precioStr = txtPrecio.getText().trim();
        if (precioStr.isEmpty()) {
            return;
        }

        try {
            calculandoPrecio = true;

            BigDecimal precio = new BigDecimal(precioStr);
            String ivaStr = txtTipoIva.getText().trim();

            if (!ivaStr.isEmpty() && precio.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentajeIva = new BigDecimal(ivaStr);
                BigDecimal divisor = BigDecimal.ONE
                        .add(porcentajeIva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                BigDecimal precioBase = precio.divide(divisor, 2, RoundingMode.HALF_UP);

                txtPrecioBase.setText(precioBase.toString());
            }

        } catch (NumberFormatException e) {
            // Formato inválido - no hacer nada
        } finally {
            calculandoPrecio = false;
        }
    }

    /**
     * Calcula el Precio a partir del PrecioBase (sin IVA)
     */
    private void calcularPrecio() {
        if (calculandoPrecio)
            return;

        String precioBaseStr = txtPrecioBase.getText().trim();
        if (precioBaseStr.isEmpty()) {
            return;
        }

        try {
            calculandoPrecioBase = true;

            BigDecimal precioBase = new BigDecimal(precioBaseStr);
            String ivaStr = txtTipoIva.getText().trim();

            if (!ivaStr.isEmpty() && precioBase.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentajeIva = new BigDecimal(ivaStr);
                BigDecimal multiplicador = BigDecimal.ONE
                        .add(porcentajeIva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                BigDecimal precio = precioBase.multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);

                txtPrecio.setText(precio.toString());
            }

        } catch (NumberFormatException e) {
            // Formato inválido - no hacer nada
        } finally {
            calculandoPrecioBase = false;
        }
    }

    /**
     * Recalcula el precio cuando cambia el IVA
     */
    private void recalcularSegunCampoActivo() {
        String precioStr = txtPrecio.getText().trim();
        String precioBaseStr = txtPrecioBase.getText().trim();

        if (!precioBaseStr.isEmpty()) {
            calcularPrecio();
        } else if (!precioStr.isEmpty()) {
            calcularPrecioBase();
        }
    }
    
    /**
     * Calcula el precio final con todos los impuestos aplicados
     */
    private void calcularTotales() {
        if (calculandoTotales || calculandoPrecio || calculandoPrecioBase)
            return;
            
        try {
            calculandoTotales = true;
            
            // Obtener el precio base (sin IVA)
            String precioBaseStr = txtPrecioBase.getText().trim();
            if (precioBaseStr.isEmpty()) {
                // Si no hay precio base, intentar calcularlo desde el precio con IVA
                String precioStr = txtPrecio.getText().trim();
                String ivaStr = txtTipoIva.getText().trim();
                
                if (!precioStr.isEmpty() && !ivaStr.isEmpty()) {
                    BigDecimal precio = new BigDecimal(precioStr);
                    BigDecimal porcentajeIva = new BigDecimal(ivaStr);
                    BigDecimal divisor = BigDecimal.ONE
                            .add(porcentajeIva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    BigDecimal precioBase = precio.divide(divisor, 2, RoundingMode.HALF_UP);
                    precioBaseStr = precioBase.toString();
                } else {
                    // No hay datos suficientes
                    return;
                }
            }
            
            BigDecimal precioBase = new BigDecimal(precioBaseStr);
            BigDecimal ivaPorcentaje = new BigDecimal(txtTipoIva.getText().trim());
            BigDecimal retencionPorcentaje = new BigDecimal(txtTipoRetencion.getText().trim());
            BigDecimal recargoPorcentaje = new BigDecimal(txtRecargoEquivalencia.getText().trim());
            
            // Calcular importes
            BigDecimal importeIva = precioBase.multiply(ivaPorcentaje)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    
            BigDecimal importeRecargo = precioBase.multiply(recargoPorcentaje)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    
            BigDecimal importeRetencion = precioBase.multiply(retencionPorcentaje)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            // Calcular total
            BigDecimal total = precioBase
                    .add(importeIva)
                    .add(importeRecargo)
                    .subtract(importeRetencion);
            
            // Actualizar labels
            lblSubtotal.setText(formatearMoneda(precioBase));
            lblImporteIva.setText(formatearMoneda(importeIva));
            lblImporteRecargo.setText(formatearMoneda(importeRecargo));
            lblImporteRetencion.setText(formatearMoneda(importeRetencion));
            lblTotal.setText(formatearMoneda(total));
            
        } catch (NumberFormatException e) {
            // Formato inválido - no hacer nada
        } finally {
            calculandoTotales = false;
        }
    }
    
    private String formatearMoneda(BigDecimal valor) {
        if (valor == null)
            return "0,00 €";
        return String.format("%,.2f €", valor);
    }

    @Override
    protected JPanel crearPanelCampos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);

        // Panel de datos básicos
        JPanel datosPanel = UIHelper.crearSeccionPanel("Información del Producto", COLOR_PRIMARIO);
        addFormField(datosPanel, "Código:", txtCodigo, true, 0);
        addFormField(datosPanel, "Nombre:", txtNombre, true, 1);
        panel.add(datosPanel, gbc);

        // Panel de precios CON INDICACIONES
        gbc.gridy = 1;
        JPanel preciosPanel = UIHelper.crearSeccionPanelConAyudaEstilizada(
                "Precios e IVA",
                "Introduce Precio (con IVA) o Precio Base (sin IVA). El otro se calculará automáticamente.",
                COLOR_PRIMARIO);

        JPanel camposPrecios = new JPanel(new GridBagLayout());
        camposPrecios.setOpaque(false);

        GridBagConstraints gbcPrecios = new GridBagConstraints();
        gbcPrecios.gridx = 0;
        gbcPrecios.gridy = 0;
        gbcPrecios.gridwidth = 2;
        gbcPrecios.weightx = 1.0;
        gbcPrecios.fill = GridBagConstraints.HORIZONTAL;
        gbcPrecios.insets = new Insets(0, 0, 10, 0);

        addFormField(camposPrecios, "Precio (con IVA):", txtPrecio, true, 2);
        gbcPrecios.gridy = 1;
        addFormField(camposPrecios, "Precio Base (sin IVA):", txtPrecioBase, true, 1);
        gbcPrecios.gridy = 2;
        addFormField(camposPrecios, "Tipo de IVA (%):", txtTipoIva, true, 0);
        gbcPrecios.gridy = 3;
        addFormField(camposPrecios, "% Retención:", txtTipoRetencion, false, 3);
        gbcPrecios.gridy = 4;
        addFormField(camposPrecios, "% Recargo equiv.:", txtRecargoEquivalencia, false, 4);

        preciosPanel.add(camposPrecios, BorderLayout.NORTH);
        
        // Panel para mostrar los totales
        JPanel totalesPanel = new JPanel(new GridBagLayout());
        totalesPanel.setOpaque(false);
        totalesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDE),
                BorderFactory.createEmptyBorder(15, 10, 10, 10)
        ));
        
        GridBagConstraints gbcTotales = new GridBagConstraints();
        gbcTotales.gridx = 0;
        gbcTotales.gridy = 0;
        gbcTotales.weightx = 1.0;
        gbcTotales.fill = GridBagConstraints.HORIZONTAL;
        gbcTotales.insets = new Insets(3, 0, 3, 10);
        
        // Título de la sección de totales
        JLabel lblTituloTotales = new JLabel("Precio Final del Producto:");
        lblTituloTotales.setFont(UITheme.FUENTE_SUBTITULO);
        lblTituloTotales.setForeground(COLOR_PRIMARIO);
        gbcTotales.gridwidth = 2;
        totalesPanel.add(lblTituloTotales, gbcTotales);
        
        // Totales
        gbcTotales.gridwidth = 1;
        gbcTotales.gridy = 1;
        agregarResultado(totalesPanel, "Precio Base:", lblSubtotal, 1, gbcTotales);
        
        gbcTotales.gridy = 2;
        agregarResultado(totalesPanel, "IVA :", lblImporteIva, 2, gbcTotales);
        
        gbcTotales.gridy = 3;
        agregarResultado(totalesPanel, "Recargo :", lblImporteRecargo, 3, gbcTotales);
        
        gbcTotales.gridy = 4;
        agregarResultado(totalesPanel, "Retención :", lblImporteRetencion, 4, gbcTotales);
        
        // Separador
        gbcTotales.gridy = 5;
        gbcTotales.gridwidth = 2;
        gbcTotales.insets = new Insets(10, 0, 5, 0);
        totalesPanel.add(new JSeparator(), gbcTotales);
        
        // Total final
        gbcTotales.gridy = 6;
        gbcTotales.gridwidth = 1;
        gbcTotales.insets = new Insets(5, 0, 0, 10);
        agregarResultado(totalesPanel, "PRECIO FINAL:", lblTotal, 6, gbcTotales);
        
        preciosPanel.add(totalesPanel, BorderLayout.SOUTH);
        panel.add(preciosPanel, gbc);

        // Espacio flexible
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }
    
    private void agregarResultado(JPanel panel, String label, JLabel valor,
            int fila, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        lbl.setForeground(Color.DARK_GRAY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(valor, gbc);
        gbc.anchor = GridBagConstraints.WEST;
    }

    @Override
    protected void cargarDatosEntidad() {
        if (entidadEditar != null) {
            if (entidadEditar.getCodigo() != null) {
                txtCodigo.setText(entidadEditar.getCodigo());
            }
            txtNombre.setText(entidadEditar.getNombre());

            // Cargar precios (desactivar listeners temporalmente)
            calculandoPrecio = true;
            calculandoPrecioBase = true;

            if (entidadEditar.getPrecio() != null) {
                txtPrecio.setText(entidadEditar.getPrecio().toString());
            }
            if (entidadEditar.getPrecioBase() != null) {
                txtPrecioBase.setText(entidadEditar.getPrecioBase().toString());
            }

            calculandoPrecio = false;
            calculandoPrecioBase = false;

            if (entidadEditar.getTipoIva() != null) {
                txtTipoIva.setText(entidadEditar.getTipoIva().toString());
            }

            if (entidadEditar.getTipoRetencion() != null) {
                txtTipoRetencion.setText(entidadEditar.getTipoRetencion().toString());
            }

            if (entidadEditar.getRecargoEquivalencia() != null) {
                txtRecargoEquivalencia.setText(entidadEditar.getRecargoEquivalencia().toString());
            }
            
            // Calcular totales después de cargar los datos
            SwingUtilities.invokeLater(() -> calcularTotales());
        }
    }

    @Override
    protected boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtCodigo.getText().trim().isEmpty()) {
            errores.append("• Código es obligatorio\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• Nombre es obligatorio\n");
        }

        String precio = txtPrecio.getText().trim();
        String precioBase = txtPrecioBase.getText().trim();

        if (precio.isEmpty() && precioBase.isEmpty()) {
            errores.append("• Debe proporcionar Precio (con IVA) o Precio Base (sin IVA)\n");
        }

        // Validar formato de números
        try {
            if (!precio.isEmpty()) {
                BigDecimal p = new BigDecimal(precio);
                if (p.compareTo(BigDecimal.ZERO) <= 0) {
                    errores.append("• El precio debe ser mayor que cero\n");
                }
            }
        } catch (NumberFormatException e) {
            errores.append("• Formato de precio inválido\n");
        }

        try {
            if (!precioBase.isEmpty()) {
                BigDecimal pb = new BigDecimal(precioBase);
                if (pb.compareTo(BigDecimal.ZERO) <= 0) {
                    errores.append("• El precio base debe ser mayor que cero\n");
                }
            }
        } catch (NumberFormatException e) {
            errores.append("• Formato de precio base inválido\n");
        }

        // Validar IVA
        String iva = txtTipoIva.getText().trim();
        if (iva.isEmpty()) {
            errores.append("• Tipo de IVA es obligatorio\n");
        } else {
            try {
                BigDecimal ivaVal = new BigDecimal(iva);
                if (ivaVal.compareTo(BigDecimal.ZERO) < 0 || ivaVal.compareTo(new BigDecimal("100")) > 0) {
                    errores.append("• El tipo de IVA debe estar entre 0 y 100\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• Formato de IVA inválido\n");
            }
        }

        // Validar retención
        String retencion = txtTipoRetencion.getText().trim();
        if (!retencion.isEmpty()) {
            try {
                BigDecimal r = new BigDecimal(retencion);
                if (r.compareTo(BigDecimal.ZERO) < 0 || r.compareTo(new BigDecimal("100")) > 0) {
                    errores.append("• La retención debe estar entre 0 y 100\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• Formato de retención inválido\n");
            }
        }

        // Validar recargo equivalencia
        String recargo = txtRecargoEquivalencia.getText().trim();
        if (!recargo.isEmpty()) {
            try {
                BigDecimal rec = new BigDecimal(recargo);
                if (rec.compareTo(BigDecimal.ZERO) < 0 || rec.compareTo(new BigDecimal("100")) > 0) {
                    errores.append("• El recargo de equivalencia debe estar entre 0 y 100\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• Formato de recargo de equivalencia inválido\n");
            }
        }

        if (errores.length() > 0) {
            mostrarErroresValidacion(errores);
            return false;
        }

        return true;
    }

    @Override
    protected boolean guardarEntidad() {
        Producto producto = modoEdicion ? entidadEditar : new Producto();

        if (modoEdicion && entidadEditar != null) {
            producto.setIdProducto(entidadEditar.getIdProducto());
        }

        String codigo = txtCodigo.getText().trim();
        producto.setCodigo(codigo.isEmpty() ? null : codigo.toUpperCase());

        producto.setNombre(txtNombre.getText().trim());

        // Precios
        String precio = txtPrecio.getText().trim();
        producto.setPrecio(precio.isEmpty() ? null : new BigDecimal(precio));

        String precioBase = txtPrecioBase.getText().trim();
        producto.setPrecioBase(precioBase.isEmpty() ? null : new BigDecimal(precioBase));

        // CAMBIADO: Tipo IVA como BigDecimal directamente
        String iva = txtTipoIva.getText().trim();
        producto.setTipoIva(iva.isEmpty() ? new BigDecimal("21.00") : new BigDecimal(iva));

        // Retención
        String retencion = txtTipoRetencion.getText().trim();
        producto.setTipoRetencion(retencion.isEmpty() ? BigDecimal.ZERO : new BigDecimal(retencion));

        // Recargo equivalencia
        String recargo = txtRecargoEquivalencia.getText().trim();
        producto.setRecargoEquivalencia(recargo.isEmpty() ? BigDecimal.ZERO : new BigDecimal(recargo));

        boolean success = modoEdicion ? controller.actualizarProducto(producto) : controller.guardarProducto(producto);

        if (!success) {
            if (producto.getCodigo() != null &&
                    controller.obtenerProductoPorCodigo(producto.getCodigo()) != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error al guardar el producto.\nYa existe un producto con ese código.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        return success;
    }
}