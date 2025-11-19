package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ClienteController;
import es.upm.tfg.sifpyme.model.entity.Cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel para el formulario de registro/edición de cliente
 */
public class ClienteFormView extends JPanel {

    private final ClienteController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    // Componentes del formulario
    private JTextField txtNombreFiscal;
    private JTextField txtNif;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private Cliente clienteEditar;
    private boolean modoEdicion;

    // Colores y fuentes consistentes
    private final Color COLOR_PRIMARIO = new Color(155, 89, 182);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_PELIGRO = new Color(231, 76, 60);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 14);

    public ClienteFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
    }

    public ClienteFormView(CardLayout cardLayout, JPanel cardPanel, Cliente clienteEditar) {
        this.controller = new ClienteController();
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.clienteEditar = clienteEditar;
        this.modoEdicion = (clienteEditar != null);

        initComponents();
        setupLayout();

        if (modoEdicion) {
            cargarDatosCliente();
        }
    }

    private void initComponents() {
        configurarCamposTexto();
        configurarBotones();

        btnGuardar.addActionListener(e -> guardarCliente());
        btnCancelar.addActionListener(e -> volverALista());
    }

    private void configurarCamposTexto() {
        txtNombreFiscal = crearCampoTexto(30);
        txtNif = crearCampoTexto(15);
        txtDireccion = crearCampoTexto(40);
        txtTelefono = crearCampoTexto(15);
        txtEmail = crearCampoTexto(30);
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
        btnCancelar.setBorder(
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        );
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

        String titulo = modoEdicion ? "Editar Cliente" : "Nuevo Cliente";
        String subtitulo = modoEdicion ? 
            "Modifica los datos del cliente" : 
            "Registra un nuevo cliente en el sistema";

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

        JLabel iconLabel = new JLabel("👤", SwingConstants.RIGHT);
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

        // Panel de datos
        JPanel datosPanel = createSeccionPanel("Información del Cliente");

        addFormField(datosPanel, "Nombre Fiscal:", txtNombreFiscal, true, 0);
        addFormField(datosPanel, "NIF:", txtNif, true, 1);
        addFormField(datosPanel, "Dirección:", txtDireccion, false, 2);
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

    private void cargarDatosCliente() {
        if (clienteEditar != null) {
            txtNombreFiscal.setText(clienteEditar.getNombreFiscal());
            txtNif.setText(clienteEditar.getNif());

            if (clienteEditar.getDireccion() != null) {
                txtDireccion.setText(clienteEditar.getDireccion());
            }
            if (clienteEditar.getTelefono() != null) {
                txtTelefono.setText(clienteEditar.getTelefono());
            }
            if (clienteEditar.getEmail() != null) {
                txtEmail.setText(clienteEditar.getEmail());
            }
        }
    }

    private void guardarCliente() {
        if (!validarCampos()) {
            return;
        }

        try {
            Cliente cliente = modoEdicion ? clienteEditar : new Cliente();

            if (modoEdicion && clienteEditar != null) {
                cliente.setIdCliente(clienteEditar.getIdCliente());
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

            if (success) {
                String mensaje = modoEdicion ?
                    "Cliente actualizado exitosamente." :
                    "Cliente registrado exitosamente.";

                JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                volverALista();
            } else {
                String mensajeError = "Error al guardar el cliente.\n";
                if (controller.obtenerClientePorNif(cliente.getNif()) != null) {
                    mensajeError += "Ya existe un cliente con ese NIF.";
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
        cardLayout.show(cardPanel, "listaClientes");
    }
}