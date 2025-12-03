package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Panel para el formulario de registro/edición de empresa
 */
public class EmpresaFormView extends JPanel {

    private final EmpresaController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

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
    private JCheckBox chkPorDefecto;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private Empresa empresaEditar;
    private boolean modoEdicion;

    // Colores y fuentes consistentes
    private final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 14);

    public EmpresaFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
    }

    public EmpresaFormView(CardLayout cardLayout, JPanel cardPanel, Empresa empresaEditar) {
        this.controller = new EmpresaController();
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.empresaEditar = empresaEditar;
        this.modoEdicion = (empresaEditar != null);

        initComponents();
        setupLayout();

        if (modoEdicion) {
            cargarDatosEmpresa();
        }
    }

    private void initComponents() {
        configurarCamposTexto();
        configurarBotones();

        btnGuardar.addActionListener(e -> guardarEmpresa());
        btnCancelar.addActionListener(e -> volverALista());
    }

    private void configurarCamposTexto() {
        txtNombreComercial = crearCampoTexto(30);
        txtRazonSocial = crearCampoTexto(30);
        txtNif = crearCampoTexto(15);
        txtDireccion = crearCampoTexto(40);
        txtCodigoPostal = crearCampoTexto(10);
        txtCiudad = crearCampoTexto(30);
        txtProvincia = crearCampoTexto(30);
        txtTelefono = crearCampoTexto(15);
        txtEmail = crearCampoTexto(30);
        txtWeb = crearCampoTexto(30);
        txtTipoRetencionIrpf = crearCampoTexto(10);
        txtTipoRetencionIrpf.setText("15.00");

        chkPorDefecto = new JCheckBox("Establecer como empresa por defecto");
        chkPorDefecto.setFont(FUENTE_CAMPO);
        chkPorDefecto.setBackground(Color.WHITE);
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

        String titulo = modoEdicion ? "Editar Empresa" : "Nueva Empresa";
        String subtitulo = modoEdicion ? 
            "Modifica los datos de la empresa" : 
            "Registra una nueva empresa en el sistema";

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

        JLabel iconLabel = new JLabel("🏢", SwingConstants.RIGHT);
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);

        // Panel de datos básicos
        JPanel datosBasicos = createSeccionPanel("Datos Básicos");
        addFormField(datosBasicos, "Nombre Comercial:", txtNombreComercial, true, 0);
        addFormField(datosBasicos, "Razón Social:", txtRazonSocial, true, 1);
        addFormField(datosBasicos, "NIF:", txtNif, true, 2);
        panel.add(datosBasicos, gbc);

        // Panel de dirección
        gbc.gridy = 1;
        JPanel direccionPanel = createSeccionPanel("Dirección");
        addFormField(direccionPanel, "Dirección:", txtDireccion, true, 0);
        addFormField(direccionPanel, "Código Postal:", txtCodigoPostal, true, 1);
        addFormField(direccionPanel, "Ciudad:", txtCiudad, true, 2);
        addFormField(direccionPanel, "Provincia:", txtProvincia, true, 3);
        panel.add(direccionPanel, gbc);

        // Panel de contacto
        gbc.gridy = 2;
        JPanel contactoPanel = createSeccionPanel("Contacto y Datos Fiscales");
        addFormField(contactoPanel, "Teléfono:", txtTelefono, false, 0);
        addFormField(contactoPanel, "Email:", txtEmail, false, 1);
        addFormField(contactoPanel, "Sitio Web:", txtWeb, false, 2);
        addFormField(contactoPanel, "% Retención IRPF:", txtTipoRetencionIrpf, false, 3);
        
        // Añadir checkbox
        GridBagConstraints gbcCheck = new GridBagConstraints();
        gbcCheck.gridx = 0;
        gbcCheck.gridy = 5;
        gbcCheck.gridwidth = 2;
        gbcCheck.anchor = GridBagConstraints.WEST;
        gbcCheck.insets = new Insets(15, 0, 0, 0);
        contactoPanel.add(chkPorDefecto, gbcCheck);
        
        panel.add(contactoPanel, gbc);

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

    private void cargarDatosEmpresa() {
        if (empresaEditar != null) {
            txtNombreComercial.setText(empresaEditar.getNombreComercial());
            txtRazonSocial.setText(empresaEditar.getRazonSocial());
            txtNif.setText(empresaEditar.getNif());
            txtDireccion.setText(empresaEditar.getDireccion());
            txtCodigoPostal.setText(empresaEditar.getCodigoPostal());
            txtCiudad.setText(empresaEditar.getCiudad());
            txtProvincia.setText(empresaEditar.getProvincia());

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
            
            chkPorDefecto.setSelected(empresaEditar.getPorDefecto() != null && empresaEditar.getPorDefecto());
        }
    }

    private void guardarEmpresa() {
        if (!validarCampos()) {
            return;
        }

        try {
            Empresa empresa = modoEdicion ? empresaEditar : new Empresa();

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

            String telefono = txtTelefono.getText().trim();
            empresa.setTelefono(telefono.isEmpty() ? null : telefono);

            String email = txtEmail.getText().trim();
            empresa.setEmail(email.isEmpty() ? null : email);

            String web = txtWeb.getText().trim();
            empresa.setWeb(web.isEmpty() ? null : web);

            try {
                BigDecimal retencion = new BigDecimal(txtTipoRetencionIrpf.getText().trim());
                empresa.setTipoRetencionIrpf(retencion);
            } catch (NumberFormatException e) {
                empresa.setTipoRetencionIrpf(new BigDecimal("15.00"));
            }

            empresa.setPorDefecto(chkPorDefecto.isSelected());
            empresa.setActivo(true);

            boolean success = modoEdicion ? 
                controller.actualizarEmpresa(empresa) : 
                controller.guardarEmpresa(empresa);

            if (success) {
                String mensaje = modoEdicion ?
                    "Empresa actualizada exitosamente." :
                    "Empresa registrada exitosamente.";

                JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                volverALista();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar la empresa.\nPor favor, verifica los datos.",
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

        // Validar email si se proporciona
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errores.append("• El formato del email no es válido\n");
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
        cardLayout.show(cardPanel, "listaEmpresas");
    }
}