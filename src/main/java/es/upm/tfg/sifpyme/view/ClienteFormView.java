package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ClienteController;
import es.upm.tfg.sifpyme.model.entity.Cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Formulario para registro/edición de cliente
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 */
public class ClienteFormView extends BaseFormView<Cliente> {

    private final ClienteController controller;

    // Campos específicos de cliente
    private JTextField txtNombreFiscal;
    private JTextField txtNif;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;

    public ClienteFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
        afterConstruction();
    }

    public ClienteFormView(CardLayout cardLayout, JPanel cardPanel, Cliente clienteEditar) {
        super(cardLayout, cardPanel, clienteEditar);
        this.controller = new ClienteController();
    }

    @Override
    protected void configurarColores() {
        // Usar el color definido en UITheme para clientes
        COLOR_PRIMARIO = UITheme.COLOR_CLIENTES;
    }

    @Override
    protected String getTituloFormulario() {
        return modoEdicion ? "Editar Cliente" : "Nuevo Cliente";
    }

    @Override
    protected String getSubtituloFormulario() {
        return modoEdicion ? 
            "Modifica los datos del cliente" : 
            "Registra un nuevo cliente en el sistema";
    }

    @Override
    protected String getIconoFormulario() {
        // Usar el icono centralizado de UITheme
        return UITheme.ICONO_CLIENTES;
    }

    @Override
    protected String getNombreCardLista() {
        return "listaClientes";
    }

    @Override
    protected void inicializarCamposEspecificos() {
        // Usar UIHelper para crear campos consistentes
        txtNombreFiscal = UIHelper.crearCampoTexto(30);
        txtNif = UIHelper.crearCampoTexto(15);
        txtDireccion = UIHelper.crearCampoTexto(40);
        txtTelefono = UIHelper.crearCampoTexto(15);
        txtEmail = UIHelper.crearCampoTexto(30);
    }

    @Override
    protected JPanel crearPanelCampos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Panel de datos usando UIHelper
        JPanel datosPanel = UIHelper.crearSeccionPanel("Información del Cliente", COLOR_PRIMARIO);

        addFormField(datosPanel, "Nombre Fiscal:", txtNombreFiscal, true, 0);
        addFormField(datosPanel, "NIF:", txtNif, true, 1);
        addFormField(datosPanel, "Dirección:", txtDireccion, true, 2);
        addFormField(datosPanel, "Teléfono:", txtTelefono, false, 3);
        addFormField(datosPanel, "Email:", txtEmail, false, 4);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        panel.add(datosPanel, gbc);

        // Espacio flexible
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }

    @Override
    protected void cargarDatosEntidad() {
        if (entidadEditar != null) {
            txtNombreFiscal.setText(entidadEditar.getNombreFiscal());
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
        }
    }

    @Override
    protected boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombreFiscal.getText().trim().isEmpty()) {
            errores.append("• Nombre Fiscal es obligatorio\n");
        }

        if (txtNif.getText().trim().isEmpty()) {
            errores.append("• NIF es obligatorio\n");
        }

        // Validar email si se proporciona
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errores.append("• El formato del email no es válido\n");
        }

        if (errores.length() > 0) {
            mostrarErroresValidacion(errores);
            return false;
        }

        return true;
    }

    @Override
    protected boolean guardarEntidad() {
        Cliente cliente = modoEdicion ? entidadEditar : new Cliente();

        if (modoEdicion && entidadEditar != null) {
            cliente.setIdCliente(entidadEditar.getIdCliente());
        }

        cliente.setNombreFiscal(txtNombreFiscal.getText().trim());
        cliente.setNif(txtNif.getText().trim().toUpperCase());

        String direccion = txtDireccion.getText().trim();
        cliente.setDireccion(direccion.isEmpty() ? null : direccion);

        String telefono = txtTelefono.getText().trim();
        cliente.setTelefono(telefono.isEmpty() ? null : telefono);

        String email = txtEmail.getText().trim();
        cliente.setEmail(email.isEmpty() ? null : email);

        boolean success = modoEdicion ? 
            controller.actualizarCliente(cliente) : 
            controller.guardarCliente(cliente);

        if (!success) {
            // Verificar si es por NIF duplicado
            if (controller.obtenerClientePorNif(cliente.getNif()) != null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar el cliente.\nYa existe un cliente con ese NIF.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

        return success;
    }
}