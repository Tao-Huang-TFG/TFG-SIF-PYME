package es.upm.tfg.sifpyme.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clase base abstracta para todos los formularios (Cliente, Empresa, Producto)
 * Implementa funcionalidad común: layout, botones, estilos, validación
 */
public abstract class BaseFormView<T> extends JPanel {

    protected final CardLayout cardLayout;
    protected final JPanel cardPanel;
    protected JButton btnGuardar;
    protected JButton btnCancelar;
    protected T entidadEditar;
    protected boolean modoEdicion;

    // Colores comunes (pueden ser sobrescritos)
    protected Color COLOR_PRIMARIO = new Color(52, 152, 219);
    protected final Color COLOR_EXITO = new Color(46, 204, 113);
    protected final Color COLOR_PELIGRO = new Color(231, 76, 60);
    protected final Color COLOR_FONDO = new Color(245, 245, 245);
    protected final Color COLOR_BORDE = new Color(220, 220, 220);

    // Fuentes comunes
    protected final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    protected final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    protected final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    protected final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    protected final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 14);

    public BaseFormView(CardLayout cardLayout, JPanel cardPanel, T entidadEditar) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.entidadEditar = entidadEditar;
        this.modoEdicion = (entidadEditar != null);

        // Permitir colores personalizados
        configurarColores();
        
        initComponents();
        setupLayout();

        if (modoEdicion) {
            cargarDatosEntidad();
        }
    }

    // ==================== MÉTODOS ABSTRACTOS (deben implementarse) ====================

    /**
     * Retorna el título del formulario según el modo (ej: "Editar Cliente" o "Nuevo Cliente")
     */
    protected abstract String getTituloFormulario();

    /**
     * Retorna el subtítulo del formulario según el modo
     */
    protected abstract String getSubtituloFormulario();

    /**
     * Retorna el icono emoji del formulario (ej: "👤")
     */
    protected abstract String getIconoFormulario();

    /**
     * Retorna el nombre de la card de la lista (ej: "listaClientes")
     */
    protected abstract String getNombreCardLista();

    /**
     * Inicializa los campos específicos del formulario
     */
    protected abstract void inicializarCamposEspecificos();

    /**
     * Crea y retorna el panel con los campos del formulario
     */
    protected abstract JPanel crearPanelCampos();

    /**
     * Carga los datos de la entidad en los campos cuando está en modo edición
     */
    protected abstract void cargarDatosEntidad();

    /**
     * Valida los campos del formulario
     * @return true si todos los campos son válidos
     */
    protected abstract boolean validarCampos();

    /**
     * Guarda o actualiza la entidad en la base de datos
     * @return true si se guardó correctamente
     */
    protected abstract boolean guardarEntidad();

    // ==================== MÉTODOS OPCIONALES (pueden sobrescribirse) ====================

    /**
     * Permite configurar colores personalizados por formulario
     */
    protected void configurarColores() {
        // Por defecto usa los colores base - las subclases pueden sobrescribirlo
    }

    /**
     * Mensaje de éxito personalizado
     */
    protected String getMensajeExito() {
        return modoEdicion ? "Registro actualizado exitosamente." : "Registro creado exitosamente.";
    }

    // ==================== IMPLEMENTACIÓN COMÚN ====================

    private void initComponents() {
        inicializarCamposEspecificos();
        configurarBotones();

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> volverALista());
    }

    protected void configurarBotones() {
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
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Formulario con scroll
        JPanel formPanel = crearPanelCampos();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_FONDO);
        add(scrollPane, BorderLayout.CENTER);

        // Botones
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel(getTituloFormulario());
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(getSubtituloFormulario());
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel(getIconoFormulario(), SwingConstants.RIGHT);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
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

    // ==================== MÉTODOS AUXILIARES COMUNES ====================

    /**
     * Crea un campo de texto con estilo común
     */
    protected JTextField crearCampoTexto(int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setFont(FUENTE_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        campo.setBackground(Color.WHITE);
        return campo;
    }

    /**
     * Crea un área de texto con estilo común
     */
    protected JTextArea crearAreaTexto(int filas, int columnas) {
        JTextArea area = new JTextArea(filas, columnas);
        area.setFont(FUENTE_CAMPO);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Color.WHITE);
        return area;
    }

    /**
     * Crea un combo box con estilo común
     */
    protected <E> JComboBox<E> crearComboBox() {
        JComboBox<E> combo = new JComboBox<>();
        combo.setFont(FUENTE_CAMPO);
        combo.setBackground(Color.WHITE);
        return combo;
    }

    /**
     * Crea un panel de sección con título
     */
    protected JPanel crearSeccionPanel(String titulo) {
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
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(lblTitulo, gbc);

        return panel;
    }

    /**
     * Añade un campo al panel con su etiqueta
     */
    protected void addFormField(JPanel panel, String label, JTextField field, 
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

    /**
     * Añade un área de texto al panel con su etiqueta
     */
    protected void addFormFieldTextArea(JPanel panel, String label, JTextArea field, 
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

    /**
     * Añade un combo box al panel con su etiqueta
     */
    protected <E> void addFormFieldCombo(JPanel panel, String label, JComboBox<E> combo, 
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

    // ==================== LÓGICA DE GUARDADO ====================

    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            boolean success = guardarEntidad();

            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    getMensajeExito(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );
                volverALista();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar el registro.\nPor favor, verifica los datos.",
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

    protected void volverALista() {
        cardLayout.show(cardPanel, getNombreCardLista());
    }

    /**
     * Muestra un diálogo de validación con errores
     */
    protected void mostrarErroresValidacion(StringBuilder errores) {
        JOptionPane.showMessageDialog(
            this,
            "Por favor, corrige los siguientes errores:\n\n" + errores.toString(),
            "Validación",
            JOptionPane.WARNING_MESSAGE
        );
    }
}