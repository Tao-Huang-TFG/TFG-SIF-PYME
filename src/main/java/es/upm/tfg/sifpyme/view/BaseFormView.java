package es.upm.tfg.sifpyme.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clase base abstracta para todos los formularios (Cliente, Empresa, Producto)
 * Implementa funcionalidad común: layout, botones, estilos, validación
 * REFACTORIZADO: Ahora usa UIHelper y UITheme para consistencia visual
 */
public abstract class BaseFormView<T> extends JPanel {

    protected final CardLayout cardLayout;
    protected final JPanel cardPanel;
    protected JButton btnGuardar;
    protected JButton btnCancelar;
    protected T entidadEditar;
    protected boolean modoEdicion;

    // Colores del tema (pueden ser sobrescritos)
    protected Color COLOR_PRIMARIO = UITheme.COLOR_INFO;

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

    protected abstract String getTituloFormulario();
    protected abstract String getSubtituloFormulario();
    protected abstract String getIconoFormulario();
    protected abstract String getNombreCardLista();
    protected abstract void inicializarCamposEspecificos();
    protected abstract JPanel crearPanelCampos();
    protected abstract void cargarDatosEntidad();
    protected abstract boolean validarCampos();
    protected abstract boolean guardarEntidad();

    // ==================== MÉTODOS OPCIONALES (pueden sobrescribirse) ====================

    protected void configurarColores() {
        // Por defecto usa los colores base - las subclases pueden sobrescribirlo
    }

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
        String tipoGuardar = modoEdicion ? "actualizar" : "guardar";
        String textoGuardar = modoEdicion ? "Actualizar" : "Guardar";
        
        btnGuardar = UIHelper.crearBotonAccion(tipoGuardar, textoGuardar);
        btnCancelar = UIHelper.crearBotonAccion("cancelar", "Cancelar");
        
        Dimension tamanoBoton = new Dimension(140, 40);
        btnGuardar.setPreferredSize(tamanoBoton);
        btnCancelar.setPreferredSize(tamanoBoton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.COLOR_FONDO);

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Formulario con scroll
        JPanel formPanel = crearPanelCampos();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.COLOR_FONDO);
        add(scrollPane, BorderLayout.CENTER);

        // Botones
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel(getTituloFormulario());
        lblTitle.setFont(UITheme.FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel(getSubtituloFormulario());
        lblSubtitle.setFont(UITheme.FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        JLabel iconLabel = new JLabel(getIconoFormulario(), SwingConstants.RIGHT);
        iconLabel.setFont(UITheme.FUENTE_ICONO_TEXTO);
        iconLabel.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(UITheme.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        panel.add(btnCancelar);
        panel.add(btnGuardar);

        return panel;
    }

    // ==================== MÉTODOS AUXILIARES COMUNES ====================

    protected JTextField crearCampoTexto(int columnas) {
        return UIHelper.crearCampoTexto(columnas);
    }

    protected JTextArea crearAreaTexto(int filas, int columnas) {
       return UIHelper.crearAreaTexto(filas, columnas);
    }

    protected <E> JComboBox<E> crearComboBox() {
        return UIHelper.crearComboBox();
    }

    protected JPanel crearSeccionPanel(String titulo) {
        return UIHelper.crearSeccionPanel(titulo, COLOR_PRIMARIO);
    }

    protected void addFormField(JPanel panel, String label, JTextField field,
            boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        if (required) {
            lbl.setText(label + " *");
            lbl.setForeground(UITheme.COLOR_PELIGRO);
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

    protected void addFormFieldTextArea(JPanel panel, String label, JTextArea field,
            boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        if (required) {
            lbl.setText(label + " *");
            lbl.setForeground(UITheme.COLOR_PELIGRO);
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

    protected <E> void addFormFieldCombo(JPanel panel, String label, JComboBox<E> combo,
            boolean required, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FUENTE_ETIQUETA);
        if (required) {
            lbl.setText(label + " *");
            lbl.setForeground(UITheme.COLOR_PELIGRO);
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
                volverALista();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Error al guardar el registro.\nPor favor, verifica los datos.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error inesperado: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void volverALista() {
        cardLayout.show(cardPanel, getNombreCardLista());
    }

    protected void mostrarErroresValidacion(StringBuilder errores) {
        JOptionPane.showMessageDialog(
                this,
                "Por favor, corrige los siguientes errores:\n\n" + errores.toString(),
                "Validación",
                JOptionPane.WARNING_MESSAGE);
    }
}