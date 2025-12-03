package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.model.entity.TipoIva;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel para el formulario de registro/edición de producto
 */
public class ProductoFormView extends JPanel {

    private final ProductoController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    // Componentes del formulario
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtPrecioBase;
    private JComboBox<TipoIva> cmbTipoIva;
    private JTextField txtTipoRetencion;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private Producto productoEditar;
    private boolean modoEdicion;

    // Colores y fuentes consistentes
    private final Color COLOR_PRIMARIO = new Color(52, 152, 219);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 14);

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
    }

    public ProductoFormView(CardLayout cardLayout, JPanel cardPanel, Producto productoEditar) {
        this.controller = new ProductoController();
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.productoEditar = productoEditar;
        this.modoEdicion = (productoEditar != null);

        initComponents();
        setupLayout();

        if (modoEdicion) {
            cargarDatosProducto();
        }
    }

    private void initComponents() {
        configurarCamposTexto();
        configurarBotones();

        btnGuardar.addActionListener(e -> guardarProducto());
        btnCancelar.addActionListener(e -> volverALista());
    }

    private void configurarCamposTexto() {
        txtCodigo = crearCampoTexto(20);
        txtNombre = crearCampoTexto(30);
        
        txtDescripcion = new JTextArea(3, 30);
        txtDescripcion.setFont(FUENTE_CAMPO);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBackground(Color.WHITE);
        
        txtPrecio = crearCampoTexto(15);
        txtPrecioBase = crearCampoTexto(15);
        txtTipoRetencion = crearCampoTexto(10);
        txtTipoRetencion.setText("0.00");
        
        // ComboBox de tipos de IVA
        cmbTipoIva = new JComboBox<>();
        cmbTipoIva.setFont(FUENTE_CAMPO);
        cmbTipoIva.setBackground(Color.WHITE);
        cargarTiposIva();
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setFont(FUENTE_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        campo.setBackground(Color.WHITE);
        return campo;
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

    private void configurarBotones() {
        String textoGuardar = modoEdicion ? "Actualizar" : "Guardar";
        btnGuardar = new JButton(textoGuardar);
        btnGuardar.setBackground(COLOR_EXITO);
        btnGuardar.setForeground(COLOR_EXITO);
        btnGuardar.setFont(FUENTE_BOTON);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(COLOR_PELIGRO);
        btnCancelar.setForeground(COLOR_PELIGRO);
        btnCancelar.setFont(FUENTE_BOTON);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_FONDO);

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_FONDO);
        add(scrollPane, BorderLayout.CENTER);

        // Botones
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        String titulo = modoEdicion ? "Editar Producto" : "Nuevo Producto";
        String subtitulo = modoEdicion ? 
            "Modifica los datos del producto" : 
            "Registra un nuevo producto o servicio";

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(subtitulo);
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel("📦", SwingConstants.RIGHT);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Panel de datos básicos
        JPanel datosPanel = createSeccionPanel("Información del Producto");

        addFormField(datosPanel, "Código:", txtCodigo, false, 0);
        addFormField(datosPanel, "Nombre:", txtNombre, true, 1);
        addFormFieldTextArea(datosPanel, "Descripción:", txtDescripcion, false, 2);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);

        panel.add(datosPanel, gbc);

        // Panel de precios
        JPanel preciosPanel = createSeccionPanel("Precios e IVA");

        addFormField(preciosPanel, "Precio:", txtPrecio, false, 0);
        addFormField(preciosPanel, "Precio Base:", txtPrecioBase, false, 1);
        addFormFieldCombo(preciosPanel, "Tipo de IVA:", cmbTipoIva, true, 2);
        addFormField(preciosPanel, "% Retención:", txtTipoRetencion, false, 3);

        gbc.gridy = 1;
        panel.add(preciosPanel, gbc);

        // Nota informativa
        JLabel lblNota = new JLabel("<html><i>* Debe proporcionar al menos Precio o Precio Base</i></html>");
        lblNota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(Color.GRAY);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel.add(lblNota, gbc);

        // Espacio flexible
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }

    private JPanel createSeccionPanel(String titulo) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(COLOR_PRIMARIO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(lblTitulo, gbc);

        return panel;
    }

    private void addFormField(JPanel panel, String label, JTextField field, 
                             boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FUENTE_ETIQUETA);
        if (required) {
            lbl.setText(label + " *");
            lbl.setForeground(COLOR_PELIGRO);
        } else {
            lbl.setForeground(Color.DARK_GRAY);
        }
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(field, gbc);
    }

    private void addFormFieldTextArea(JPanel panel, String label, JTextArea field, 
                                     boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FUENTE_ETIQUETA);
        if (required) {
            lbl.setText(label + " *");
            lbl.setForeground(COLOR_PELIGRO);
        } else {
            lbl.setForeground(Color.DARK_GRAY);
        }
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);
        
        JScrollPane scrollPane = new JScrollPane(field);
        scrollPane.setBorder(field.getBorder());
        panel.add(scrollPane, gbc);
    }

    private void addFormFieldCombo(JPanel panel, String label, JComboBox<TipoIva> combo, 
                                  boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FUENTE_ETIQUETA);
        if (required) {
            lbl.setText(label + " *");
            lbl.setForeground(COLOR_PELIGRO);
        } else {
            lbl.setForeground(Color.DARK_GRAY);
        }
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(combo, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        Dimension tamanoBoton = new Dimension(140, 40);
        btnCancelar.setPreferredSize(tamanoBoton);
        btnGuardar.setPreferredSize(tamanoBoton);

        panel.add(btnCancelar);
        panel.add(btnGuardar);

        return panel;
    }

    private void cargarDatosProducto() {
        if (productoEditar != null) {
            if (productoEditar.getCodigo() != null) {
                txtCodigo.setText(productoEditar.getCodigo());
            }
            txtNombre.setText(productoEditar.getNombre());
            
            if (productoEditar.getDescripcion() != null) {
                txtDescripcion.setText(productoEditar.getDescripcion());
            }
            if (productoEditar.getPrecio() != null) {
                txtPrecio.setText(productoEditar.getPrecio().toString());
            }
            if (productoEditar.getPrecioBase() != null) {
                txtPrecioBase.setText(productoEditar.getPrecioBase().toString());
            }
            if (productoEditar.getTipoRetencion() != null) {
                txtTipoRetencion.setText(productoEditar.getTipoRetencion().toString());
            }
            
            // Seleccionar el tipo de IVA correspondiente
            if (productoEditar.getIdTipoIva() != null) {
                for (int i = 0; i < cmbTipoIva.getItemCount(); i++) {
                    TipoIva tipo = cmbTipoIva.getItemAt(i);
                    if (tipo.getIdTipoIva().equals(productoEditar.getIdTipoIva())) {
                        cmbTipoIva.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void guardarProducto() {
        if (!validarCampos()) {
            return;
        }

        try {
            Producto producto = modoEdicion ? productoEditar : new Producto();

            if (modoEdicion && productoEditar != null) {
                producto.setIdProducto(productoEditar.getIdProducto());
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

            boolean success = modoEdicion ? 
                controller.actualizarProducto(producto) : 
                controller.guardarProducto(producto);

            if (success) {
                String mensaje = modoEdicion ?
                    "Producto actualizado exitosamente." :
                    "Producto registrado exitosamente.";

                JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                volverALista();
            } else {
                String mensajeError = "Error al guardar el producto.\n";
                if (producto.getCodigo() != null && 
                    controller.obtenerProductoPorCodigo(producto.getCodigo()) != null) {
                    mensajeError += "Ya existe un producto con ese código.";
                } else {
                    mensajeError += "Por favor, verifica los datos.";
                }

                JOptionPane.showMessageDialog(
                    this,
                    mensajeError,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error en el formato de los números.\nVerifica precios y retención.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error inesperado: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• Nombre es obligatorio\n");
        }

        String precio = txtPrecio.getText().trim();
        String precioBase = txtPrecioBase.getText().trim();
        
        if (precio.isEmpty() && precioBase.isEmpty()) {
            errores.append("• Debe proporcionar al menos Precio o Precio Base\n");
        }

        // Validar formato de números
        try {
            if (!precio.isEmpty()) {
                BigDecimal p = new BigDecimal(precio);
                if (p.compareTo(BigDecimal.ZERO) < 0) {
                    errores.append("• El precio no puede ser negativo\n");
                }
            }
        } catch (NumberFormatException e) {
            errores.append("• Formato de precio inválido\n");
        }

        try {
            if (!precioBase.isEmpty()) {
                BigDecimal pb = new BigDecimal(precioBase);
                if (pb.compareTo(BigDecimal.ZERO) < 0) {
                    errores.append("• El precio base no puede ser negativo\n");
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
            JOptionPane.showMessageDialog(
                this,
                "Por favor, corrige los siguientes errores:\n\n" + errores.toString(),
                "Validación",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    private void volverALista() {
        cardLayout.show(cardPanel, "listaProductos");
    }
}