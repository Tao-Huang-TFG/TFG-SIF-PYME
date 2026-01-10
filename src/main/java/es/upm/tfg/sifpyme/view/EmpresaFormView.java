package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Formulario para registro/edición de empresa
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 * CORREGIDO: Eliminados campos inexistentes (ciudad, provincia, código postal, web)
 */
public class EmpresaFormView extends BaseFormView<Empresa> {

    private final EmpresaController controller;

    // Campos específicos de empresa (solo los que existen en el modelo)
    private JTextField txtNombreComercial;
    private JTextField txtRazonSocial;
    private JTextField txtNif;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtTipoRetencionIrpf;
    private JCheckBox chkPorDefecto;

    public EmpresaFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
    }

    public EmpresaFormView(CardLayout cardLayout, JPanel cardPanel, Empresa empresaEditar) {
        super(cardLayout, cardPanel, empresaEditar);
        this.controller = new EmpresaController();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = UITheme.COLOR_EMPRESAS;
    }

    @Override
    protected String getTituloFormulario() {
        return modoEdicion ? "Editar Empresa" : "Nueva Empresa";
    }

    @Override
    protected String getSubtituloFormulario() {
        return modoEdicion ? 
            "Modifica los datos de la empresa" : 
            "Registra una nueva empresa en el sistema";
    }

    @Override
    protected String getIconoFormulario() {
        return UITheme.ICONO_EMPRESAS;
    }

    @Override
    protected String getNombreCardLista() {
        return "listaEmpresas";
    }

    @Override
    protected void inicializarCamposEspecificos() {
        // Solo inicializar campos que existen en el modelo Empresa
        txtNombreComercial = UIHelper.crearCampoTexto(30);
        txtRazonSocial = UIHelper.crearCampoTexto(30);
        txtNif = UIHelper.crearCampoTexto(15);
        txtDireccion = UIHelper.crearCampoTexto(40);
        txtTelefono = UIHelper.crearCampoTexto(15);
        txtEmail = UIHelper.crearCampoTexto(30);
        txtTipoRetencionIrpf = UIHelper.crearCampoTexto(10);
        txtTipoRetencionIrpf.setText("15.00");

        chkPorDefecto = new JCheckBox("Establecer como empresa por defecto");
        chkPorDefecto.setFont(UITheme.FUENTE_CAMPO);
        chkPorDefecto.setBackground(Color.WHITE);
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
        JPanel datosBasicos = UIHelper.crearSeccionPanel("Datos Básicos", COLOR_PRIMARIO);
        addFormField(datosBasicos, "Nombre Comercial:", txtNombreComercial, true, 0);
        addFormField(datosBasicos, "Razón Social:", txtRazonSocial, true, 1);
        addFormField(datosBasicos, "NIF:", txtNif, true, 2);
        panel.add(datosBasicos, gbc);

        // Panel de dirección (solo dirección, sin ciudad/provincia/código postal)
        gbc.gridy = 1;
        JPanel direccionPanel = UIHelper.crearSeccionPanel("Dirección", COLOR_PRIMARIO);
        addFormField(direccionPanel, "Dirección:", txtDireccion, true, 0);
        panel.add(direccionPanel, gbc);

        // Panel de contacto (sin sitio web)
        gbc.gridy = 2;
        JPanel contactoPanel = UIHelper.crearSeccionPanel("Contacto y Datos Fiscales", COLOR_PRIMARIO);
        addFormField(contactoPanel, "Teléfono:", txtTelefono, false, 0);
        addFormField(contactoPanel, "Email:", txtEmail, false, 1);
        addFormField(contactoPanel, "% Retención IRPF:", txtTipoRetencionIrpf, false, 2);
        
        // Checkbox de empresa por defecto
        GridBagConstraints gbcCheck = new GridBagConstraints();
        gbcCheck.gridx = 0;
        gbcCheck.gridy = 3;
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

    @Override
    protected void cargarDatosEntidad() {
        if (entidadEditar != null) {
            txtNombreComercial.setText(entidadEditar.getNombreComercial());
            txtRazonSocial.setText(entidadEditar.getRazonSocial());
            txtNif.setText(entidadEditar.getNif());
            
            if (entidadEditar.getDireccion() != null) {
                txtDireccion.setText(entidadEditar.getDireccion());
            }
            if (entidadEditar.getTelefono() != null) {
                txtTelefono.setText(entidadEditar.getTelefono());
            }
            if (entidadEditar.getEmail() != null) {
                txtEmail.setText(entidadEditar.getEmail());
            }
            if (entidadEditar.getTipoRetencionIrpf() != null) {
                txtTipoRetencionIrpf.setText(entidadEditar.getTipoRetencionIrpf().toString());
            }
            
            chkPorDefecto.setSelected(entidadEditar.getPorDefecto() != null && entidadEditar.getPorDefecto());
        }
    }

    @Override
    protected boolean validarCampos() {
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

        // Validar email si se proporciona
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errores.append("• El formato del email no es válido\n");
        }

        // Validar porcentaje de retención
        try {
            BigDecimal retencion = new BigDecimal(txtTipoRetencionIrpf.getText().trim());
            if (retencion.compareTo(BigDecimal.ZERO) < 0 || retencion.compareTo(new BigDecimal("100")) > 0) {
                errores.append("• El porcentaje de retención debe estar entre 0 y 100\n");
            }
        } catch (NumberFormatException e) {
            errores.append("• Porcentaje de retención inválido\n");
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
            Empresa empresa = modoEdicion ? entidadEditar : new Empresa();

            if (modoEdicion && entidadEditar != null) {
                empresa.setIdEmpresa(entidadEditar.getIdEmpresa());
            }

            empresa.setNombreComercial(txtNombreComercial.getText().trim());
            empresa.setRazonSocial(txtRazonSocial.getText().trim());
            empresa.setNif(txtNif.getText().trim().toUpperCase());
            empresa.setDireccion(txtDireccion.getText().trim());

            String telefono = txtTelefono.getText().trim();
            empresa.setTelefono(telefono.isEmpty() ? null : telefono);

            String email = txtEmail.getText().trim();
            empresa.setEmail(email.isEmpty() ? null : email);

            try {
                BigDecimal retencion = new BigDecimal(txtTipoRetencionIrpf.getText().trim());
                empresa.setTipoRetencionIrpf(retencion);
            } catch (NumberFormatException e) {
                empresa.setTipoRetencionIrpf(new BigDecimal("15.00"));
            }

            empresa.setPorDefecto(chkPorDefecto.isSelected());

            boolean success = modoEdicion ? 
                controller.actualizarEmpresa(empresa) : 
                controller.guardarEmpresa(empresa);

            return success;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error inesperado: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
}