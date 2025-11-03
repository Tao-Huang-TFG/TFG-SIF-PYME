package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Vista para el formulario de registro de empresa
 * Diseño mejorado con principios de Material Design y usabilidad
 */
public class EmpresaFormView extends JFrame {

    private final EmpresaController controller;

    // Componentes del formulario
    private JTextField txtNombreComercial;
    private JTextField txtRazonSocial;
    private JTextField txtNif;
    private JTextField txtDireccion;
    private JTextField txtCodigoPostal;
    private JTextField txtCiudad;
    private JTextField txtProvincia;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtWeb;
    private JTextField txtTipoRetencionIrpf;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private Empresa empresaEditar;
    private boolean modoEdicion;

    // Colores y fuentes mejorados
    private final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private final Color COLOR_SECUNDARIO = new Color(52, 152, 219);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);
    
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 28);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 14);

    public EmpresaFormView() {
        this(null);
    }

    public EmpresaFormView(Empresa empresaEditar) {
        this.controller = new EmpresaController();
        this.empresaEditar = empresaEditar;
        this.modoEdicion = (empresaEditar != null);
        
        // Configuración inicial mejorada
        configurarVentana();
        initComponents();
        setupLayout();
        
        if (modoEdicion) {
            cargarDatosEmpresa();
        }
    }

    private void configurarVentana() {
        setTitle("Registro de Empresa - SifPyme");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Tamaño responsive que se adapta mejor
        setPreferredSize(new Dimension(900, 750));
        setMinimumSize(new Dimension(800, 650));
        
        // Permitir redimensionado para mejor usabilidad
        setResizable(true);
        setLocationRelativeTo(null);
        
        // Icono de la aplicación
        try {
            // Puedes añadir un icono aquí
            // setIconImage(Toolkit.getDefaultToolkit().getImage("icono.png"));
        } catch (Exception e) {
            // Icono por defecto si no se encuentra
        }
    }

    private void initComponents() {
        // Configurar campos de texto con mejor diseño
        configurarCamposTexto();
        
        // Configurar botones con mejor diseño
        configurarBotones();
        
        // Agregar listeners
        btnGuardar.addActionListener(e -> guardarEmpresa());
        btnCancelar.addActionListener(e -> cancelar());
    }

    private void configurarCamposTexto() {
        int anchoCampo = 20; // Tamaño base más grande
        
        // Crear campos con diseño mejorado
        txtNombreComercial = crearCampoTexto(anchoCampo);
        txtRazonSocial = crearCampoTexto(anchoCampo);
        txtNif = crearCampoTexto(15);
        txtDireccion = crearCampoTexto(anchoCampo);
        txtCodigoPostal = crearCampoTexto(10);
        txtCiudad = crearCampoTexto(anchoCampo);
        txtProvincia = crearCampoTexto(anchoCampo);
        txtTelefono = crearCampoTexto(15);
        txtEmail = crearCampoTexto(anchoCampo);
        txtWeb = crearCampoTexto(anchoCampo);
        txtTipoRetencionIrpf = crearCampoTexto(10);
        txtTipoRetencionIrpf.setText("15.00");
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setFont(FUENTE_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        campo.setBackground(Color.WHITE);
        campo.setSelectionColor(COLOR_SECUNDARIO);
        return campo;
    }

    private void configurarBotones() {
        // Botón Guardar
        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(COLOR_EXITO);
        btnGuardar.setForeground(COLOR_EXITO);
        btnGuardar.setFont(FUENTE_BOTON);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover para botón guardar
        btnGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGuardar.setBackground(COLOR_EXITO.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnGuardar.setBackground(COLOR_EXITO);
            }
        });

        // Botón Cancelar
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.WHITE);
        btnCancelar.setForeground(COLOR_PELIGRO);
        btnCancelar.setFont(FUENTE_BOTON);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PELIGRO, 1),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover para botón cancelar
        btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancelar.setBackground(new Color(255, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancelar.setBackground(Color.WHITE);
            }
        });
    }

    private void setupLayout() {
        // Panel principal con fondo
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Panel de encabezado
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel de formulario con scroll para pantallas pequeñas
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_FONDO);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack(); // Ajustar tamaño automáticamente
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel("Configuración Inicial");
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Registra los datos de tu empresa para comenzar a facturar");
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        // Icono decorativo (puedes reemplazar con un icono real)
        JLabel iconLabel = new JLabel("🏢", SwingConstants.RIGHT);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Datos básicos
        JPanel datosBasicos = createSeccionPanel("Datos Básicos", "Información principal de la empresa");
        addFormField(datosBasicos, "Nombre Comercial:", txtNombreComercial, true, 0);
        addFormField(datosBasicos, "Razón Social:", txtRazonSocial, true, 1);
        addFormField(datosBasicos, "NIF:", txtNif, true, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(datosBasicos, gbc);

        // Dirección
        JPanel direccionPanel = createSeccionPanel("Dirección", "Ubicación física de la empresa");
        addFormField(direccionPanel, "Dirección:", txtDireccion, true, 0);
        addFormField(direccionPanel, "Código Postal:", txtCodigoPostal, true, 1);
        addFormField(direccionPanel, "Ciudad:", txtCiudad, true, 2);
        addFormField(direccionPanel, "Provincia:", txtProvincia, true, 3);

        gbc.gridy = 1;
        panel.add(direccionPanel, gbc);

        // Contacto
        JPanel contactoPanel = createSeccionPanel("Contacto y Datos Fiscales", "Información de contacto y configuración fiscal");
        addFormField(contactoPanel, "Teléfono:", txtTelefono, false, 0);
        addFormField(contactoPanel, "Email:", txtEmail, false, 1);
        addFormField(contactoPanel, "Sitio Web:", txtWeb, false, 2);
        addFormField(contactoPanel, "% Retención IRPF:", txtTipoRetencionIrpf, false, 3);

        gbc.gridy = 2;
        panel.add(contactoPanel, gbc);

        // Espacio flexible al final
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }

    private JPanel createSeccionPanel(String titulo, String descripcion) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Título de la sección
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(COLOR_PRIMARIO);

        // Descripción
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(FUENTE_SUBTITULO);
        lblDescripcion.setForeground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitulo, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(lblDescripcion, gbc);

        return panel;
    }

    private void addFormField(JPanel panel, String label, JTextField field, boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 2; // +2 por el título y descripción
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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Tamaños consistentes para botones
        Dimension tamanoBoton = new Dimension(160, 45);
        btnCancelar.setPreferredSize(tamanoBoton);
        btnGuardar.setPreferredSize(tamanoBoton);

        panel.add(btnCancelar);
        panel.add(btnGuardar);

        return panel;
    }

    /**
     * Carga los datos de la empresa en el formulario para edición
     */
    private void cargarDatosEmpresa() {
        if (empresaEditar == null) {
            return;
        }

        // Actualizar título de la ventana
        setTitle("Editar Empresa - SifPyme");

        // Actualizar texto del botón
        btnGuardar.setText("Actualizar Empresa");

        // Cargar datos en los campos
        txtNombreComercial.setText(empresaEditar.getNombreComercial());
        txtRazonSocial.setText(empresaEditar.getRazonSocial());
        txtNif.setText(empresaEditar.getNif());
        txtDireccion.setText(empresaEditar.getDireccion());
        txtCodigoPostal.setText(empresaEditar.getCodigoPostal());
        txtCiudad.setText(empresaEditar.getCiudad());
        txtProvincia.setText(empresaEditar.getProvincia());

        // Campos opcionales
        if (empresaEditar.getTelefono() != null) {
            txtTelefono.setText(empresaEditar.getTelefono());
        }
        
        if (empresaEditar.getEmail() != null) {
            txtEmail.setText(empresaEditar.getEmail());
        }
        
        if (empresaEditar.getWeb() != null) {
            txtWeb.setText(empresaEditar.getWeb());
        }
        
        if (empresaEditar.getTipoRetencionIrpf() != null) {
            txtTipoRetencionIrpf.setText(empresaEditar.getTipoRetencionIrpf().toString());
        }
    }

    private void guardarEmpresa() {
        // Validar campos obligatorios
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear objeto Empresa
            Empresa empresa = new Empresa();

            if (modoEdicion && empresaEditar != null) {
                empresa.setIdEmpresa(empresaEditar.getIdEmpresa());
            }

            empresa.setNombreComercial(txtNombreComercial.getText().trim());
            empresa.setRazonSocial(txtRazonSocial.getText().trim());
            empresa.setNif(txtNif.getText().trim().toUpperCase());
            empresa.setDireccion(txtDireccion.getText().trim());
            empresa.setCodigoPostal(txtCodigoPostal.getText().trim());
            empresa.setCiudad(txtCiudad.getText().trim());
            empresa.setProvincia(txtProvincia.getText().trim());

            // Campos opcionales
            String telefono = txtTelefono.getText().trim();
            if (!telefono.isEmpty()) {
                empresa.setTelefono(telefono);
            }

            String email = txtEmail.getText().trim();
            if (!email.isEmpty()) {
                empresa.setEmail(email);
            }

            String web = txtWeb.getText().trim();
            if (!web.isEmpty()) {
                empresa.setWeb(web);
            }

            // Tipo de retención IRPF
            try {
                BigDecimal retencion = new BigDecimal(txtTipoRetencionIrpf.getText().trim());
                empresa.setTipoRetencionIrpf(retencion);
            } catch (NumberFormatException e) {
                empresa.setTipoRetencionIrpf(new BigDecimal("15.00"));
            }

            // Configurar como empresa por defecto
            empresa.setPorDefecto(true);
            empresa.setActivo(true);

            // Guardar a través del controlador
            boolean success = controller.guardarEmpresa(empresa);

            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Empresa registrada exitosamente.\n¡Ya puedes comenzar a facturar!",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Aquí puedes abrir la ventana principal de tu aplicación
                // new MainView().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar la empresa. Por favor, verifica los datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }

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

        if (txtNombreComercial.getText().trim().isEmpty()) {
            errores.append("• Nombre Comercial es obligatorio\n");
        }

        if (txtRazonSocial.getText().trim().isEmpty()) {
            errores.append("• Razón Social es obligatoria\n");
        }

        if (txtNif.getText().trim().isEmpty()) {
            errores.append("• NIF es obligatorio\n");
        }

        if (txtDireccion.getText().trim().isEmpty()) {
            errores.append("• Dirección es obligatoria\n");
        }

        if (txtCodigoPostal.getText().trim().isEmpty()) {
            errores.append("• Código Postal es obligatorio\n");
        }

        if (txtCiudad.getText().trim().isEmpty()) {
            errores.append("• Ciudad es obligatoria\n");
        }

        if (txtProvincia.getText().trim().isEmpty()) {
            errores.append("• Provincia es obligatoria\n");
        }

        if (errores.length() > 0) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, completa los siguientes campos:\n\n" + errores.toString(),
                "Campos Obligatorios",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    private void cancelar() {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que quieres salir?\nNo se guardará ninguna información.",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}