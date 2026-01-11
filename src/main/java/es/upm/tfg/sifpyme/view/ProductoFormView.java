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

    // Flags para evitar bucles infinitos en el cálculo
    private boolean calculandoPrecio = false;
    private boolean calculandoPrecioBase = false;

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
        afterConstruction();
    }

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel, Producto productoEditar) {
        super(cardLayout, cardPanel, productoEditar);
        this.controller = new ProductoController();
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

        // Configurar listeners para cálculo automático
        configurarCalculoAutomatico();
    }

    /**
     * Configura los listeners para el cálculo automático bidireccional
     */
    private void configurarCalculoAutomatico() {
        // Listener para Precio (con IVA) -> calcula PrecioBase
        txtPrecio.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcularPrecioBase();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcularPrecioBase();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcularPrecioBase();
            }
        });

        // Listener para PrecioBase (sin IVA) -> calcula Precio
        txtPrecioBase.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcularPrecio();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcularPrecio();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcularPrecio();
            }
        });

        // Listener para cambio de IVA -> recalcula según el campo que esté lleno
        txtTipoIva.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recalcularSegunCampoActivo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recalcularSegunCampoActivo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                recalcularSegunCampoActivo();
            }
        });
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

        preciosPanel.add(camposPrecios, BorderLayout.SOUTH);
        panel.add(preciosPanel, gbc);

        // Nota informativa
        JLabel lblNota = new JLabel(
                "<html><i>* El sistema calculará automáticamente el precio con/sin IVA según el que introduzcas</i></html>");
        lblNota.setFont(UITheme.FUENTE_SUBTITULO);
        lblNota.setForeground(COLOR_PRIMARIO);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel.add(lblNota, gbc);

        // Espacio flexible
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
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

            // CAMBIADO: Cargar tipo IVA directamente
            if (entidadEditar.getTipoIva() != null) {
                txtTipoIva.setText(entidadEditar.getTipoIva().toString());
            }

            if (entidadEditar.getTipoRetencion() != null) {
                txtTipoRetencion.setText(entidadEditar.getTipoRetencion().toString());
            }
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