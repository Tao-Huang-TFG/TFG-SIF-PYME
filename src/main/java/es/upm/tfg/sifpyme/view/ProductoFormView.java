package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.model.entity.TipoIva;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Formulario para registro/edición de producto
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 * Con cálculo automático de Precio <-> PrecioBase según el IVA seleccionado
 */
public class ProductoFormView extends BaseFormView<Producto> {

    private final ProductoController controller;

    // Campos específicos de producto
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtPrecioBase;
    private JComboBox<TipoIva> cmbTipoIva;
    private JTextField txtTipoRetencion;

    // Flags para evitar bucles infinitos en el cálculo
    private boolean calculandoPrecio = false;
    private boolean calculandoPrecioBase = false;

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
    }

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel, Producto productoEditar) {
        super(cardLayout, cardPanel, productoEditar);
        this.controller = new ProductoController();
        cargarTiposIva();
    }

    @Override
    protected void configurarColores() {
        // Usar el color definido en UITheme para productos
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
        // Usar el icono centralizado de UITheme
        return UITheme.ICONO_PRODUCTOS;
    }

    @Override
    protected String getNombreCardLista() {
        return "listaProductos";
    }

    @Override
    protected void inicializarCamposEspecificos() {
        // Usar UIHelper para crear campos consistentes
        txtCodigo = UIHelper.crearCampoTexto(20);
        txtNombre = UIHelper.crearCampoTexto(30);

        txtDescripcion = UIHelper.crearAreaTexto(3, 30);

        txtPrecio = UIHelper.crearCampoTexto(15);
        txtPrecioBase = UIHelper.crearCampoTexto(15);
        txtTipoRetencion = UIHelper.crearCampoTexto(10);
        txtTipoRetencion.setText("0.00");

        // ComboBox de tipos de IVA usando UIHelper
        cmbTipoIva = UIHelper.crearComboBox();

        // Configurar listeners para cálculo automático
        configurarCalculoAutomatico();
    }

    private void cargarTiposIva() {
        List<TipoIva> tipos = controller.obtenerTiposIva();
        for (TipoIva tipo : tipos) {
            cmbTipoIva.addItem(tipo);
        }
        if (!tipos.isEmpty()) {
            cmbTipoIva.setSelectedIndex(0);
        }
    }

    /**
     * Configura los listeners para el cálculo automático bidireccional
     * Precio (con IVA) <-> PrecioBase (sin IVA)
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
        cmbTipoIva.addActionListener(e -> recalcularSegunCampoActivo());
    }

    /**
     * Calcula el PrecioBase a partir del Precio (con IVA)
     * Fórmula: PrecioBase = Precio / (1 + IVA/100)
     */
    private void calcularPrecioBase() {
        if (calculandoPrecioBase)
            return; // Evitar bucle infinito

        String precioStr = txtPrecio.getText().trim();
        if (precioStr.isEmpty()) {
            return;
        }

        try {
            calculandoPrecio = true;

            BigDecimal precio = new BigDecimal(precioStr);
            TipoIva tipoIva = (TipoIva) cmbTipoIva.getSelectedItem();

            if (tipoIva != null && precio.compareTo(BigDecimal.ZERO) > 0) {
                // Fórmula: PrecioBase = Precio / (1 + IVA/100)
                BigDecimal porcentajeIva = tipoIva.getPorcentaje();
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
     * Fórmula: Precio = PrecioBase * (1 + IVA/100)
     */
    private void calcularPrecio() {
        if (calculandoPrecio)
            return; // Evitar bucle infinito

        String precioBaseStr = txtPrecioBase.getText().trim();
        if (precioBaseStr.isEmpty()) {
            return;
        }

        try {
            calculandoPrecioBase = true;

            BigDecimal precioBase = new BigDecimal(precioBaseStr);
            TipoIva tipoIva = (TipoIva) cmbTipoIva.getSelectedItem();

            if (tipoIva != null && precioBase.compareTo(BigDecimal.ZERO) > 0) {
                // Fórmula: Precio = PrecioBase * (1 + IVA/100)
                BigDecimal porcentajeIva = tipoIva.getPorcentaje();
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
     * Recalcula el precio cuando cambia el IVA, basándose en el último campo
     * editado
     */
    private void recalcularSegunCampoActivo() {
        String precioStr = txtPrecio.getText().trim();
        String precioBaseStr = txtPrecioBase.getText().trim();

        // Priorizar PrecioBase si ambos están llenos (es más común trabajar con precios
        // sin IVA)
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

        // Panel de datos básicos usando UIHelper
        JPanel datosPanel = UIHelper.crearSeccionPanel("Información del Producto", COLOR_PRIMARIO);
        addFormField(datosPanel, "Código:", txtCodigo, true, 0);
        addFormField(datosPanel, "Nombre:", txtNombre, true, 1);
        addFormFieldTextArea(datosPanel, "Descripción:", txtDescripcion, false, 2);
        panel.add(datosPanel, gbc);

        // Panel de precios CON INDICACIONES
        gbc.gridy = 1;
        JPanel preciosPanel = crearSeccionPanelConAyuda("Precios e IVA",
                "💡 Introduce Precio (con IVA) o Precio Base (sin IVA). El otro se calculará automáticamente.");

        addFormField(preciosPanel, "Precio (con IVA):", txtPrecio, false, 1);
        addFormField(preciosPanel, "Precio Base (sin IVA):", txtPrecioBase, false, 2);
        addFormFieldCombo(preciosPanel, "Tipo de IVA:", cmbTipoIva, true, 3);
        addFormField(preciosPanel, "% Retención:", txtTipoRetencion, false, 4);
        panel.add(preciosPanel, gbc);

        // Nota informativa adicional
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

    /**
     * Crea un panel de sección con título y texto de ayuda
     * REFACTORIZADO: Usa UITheme para colores y fuentes
     */
    private JPanel crearSeccionPanelConAyuda(String titulo, String textoAyuda) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(UITheme.FUENTE_SUBTITULO_NEGRITA);
        lblTitulo.setForeground(COLOR_PRIMARIO);
        panel.add(lblTitulo, gbc);

        // Texto de ayuda usando fuentes de UITheme
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        JLabel lblAyuda = new JLabel("<html>" + textoAyuda + "</html>");
        lblAyuda.setFont(UITheme.FUENTE_SUBTITULO);
        lblAyuda.setForeground(new Color(100, 100, 100));
        panel.add(lblAyuda, gbc);

        return panel;
    }

    @Override
    protected void cargarDatosEntidad() {
        if (entidadEditar != null) {
            if (entidadEditar.getCodigo() != null) {
                txtCodigo.setText(entidadEditar.getCodigo());
            }
            txtNombre.setText(entidadEditar.getNombre());

            if (entidadEditar.getDescripcion() != null) {
                txtDescripcion.setText(entidadEditar.getDescripcion());
            }

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

            if (entidadEditar.getTipoRetencion() != null) {
                txtTipoRetencion.setText(entidadEditar.getTipoRetencion().toString());
            }

            // Seleccionar el tipo de IVA correspondiente
            if (entidadEditar.getIdTipoIva() != null) {
                for (int i = 0; i < cmbTipoIva.getItemCount(); i++) {
                    TipoIva tipo = cmbTipoIva.getItemAt(i);
                    if (tipo.getIdTipoIva().equals(entidadEditar.getIdTipoIva())) {
                        cmbTipoIva.setSelectedIndex(i);
                        break;
                    }
                }
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

        if (cmbTipoIva.getSelectedItem() == null) {
            errores.append("• Debe seleccionar un tipo de IVA\n");
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

        String descripcion = txtDescripcion.getText().trim();
        producto.setDescripcion(descripcion.isEmpty() ? null : descripcion);

        // Precios
        String precio = txtPrecio.getText().trim();
        producto.setPrecio(precio.isEmpty() ? null : new BigDecimal(precio));

        String precioBase = txtPrecioBase.getText().trim();
        producto.setPrecioBase(precioBase.isEmpty() ? null : new BigDecimal(precioBase));

        // Tipo IVA
        TipoIva tipoIvaSeleccionado = (TipoIva) cmbTipoIva.getSelectedItem();
        if (tipoIvaSeleccionado != null) {
            producto.setIdTipoIva(tipoIvaSeleccionado.getIdTipoIva());
        }

        // Retención
        String retencion = txtTipoRetencion.getText().trim();
        producto.setTipoRetencion(retencion.isEmpty() ? BigDecimal.ZERO : new BigDecimal(retencion));

        boolean success = modoEdicion ? controller.actualizarProducto(producto) : controller.guardarProducto(producto);

        if (!success) {
            // Verificar si es por código duplicado
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