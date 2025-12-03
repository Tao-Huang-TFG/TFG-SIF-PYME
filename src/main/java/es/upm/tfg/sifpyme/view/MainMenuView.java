package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.util.NavigationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Menú principal de la aplicación SifPyme
 * Diseño consistente con EmpresaFormView
 */
public class MainMenuView extends JFrame {

    // Colores y fuentes consistentes con EmpresaFormView
    private final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_FONDO = new Color(245, 245, 245);
    private final Color COLOR_BORDE = new Color(220, 220, 220);
    private final Color COLOR_VOLVER = new Color(149, 165, 166);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 28);
    private final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FUENTE_DESCRIPCION = new Font("Segoe UI", Font.PLAIN, 12);

    // Componentes del menú
    private JButton btnEmpresas;
    private JButton btnClientes;
    private JButton btnProductos;
    private JButton btnFacturas;
    private JButton btnConfiguracion;
    private JButton btnSalir;
    private JButton btnVolver;

    public MainMenuView() {
        configurarVentana();
        initComponents();
        setupLayout();
        configurarListeners();
    }

    private void configurarVentana() {
        setTitle("SifPyme - Sistema de Facturación");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Cambiado para usar navegación
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmarSalida();
            }
        });

        setPreferredSize(new Dimension(1000, 700));
        setMinimumSize(new Dimension(900, 600));
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Botón Empresas
        btnEmpresas = crearBotonMenu("🏢", "Empresas",
                "Gestionar datos de tu empresa", COLOR_PRIMARIO);

        // Botón Clientes
        btnClientes = crearBotonMenu("👥", "Clientes",
                "Gestionar base de datos de clientes", new Color(155, 89, 182));

        // Botón Productos
        btnProductos = crearBotonMenu("📦", "Productos y Servicios",
                "Gestionar catálogo de productos", new Color(52, 152, 219));

        // Botón Facturas
        btnFacturas = crearBotonMenu("🧾", "Facturas",
                "Crear y gestionar facturas", new Color(46, 204, 113));

        // Botón Configuración
        btnConfiguracion = crearBotonMenu("🔧", "Configuración",
                "Ajustes del sistema", new Color(241, 196, 15));

        // Botón Salir
        btnSalir = crearBotonMenu("🚪", "Salir",
                "Cerrar la aplicación", new Color(231, 76, 60));

        // Botón Volver (inicialmente oculto en menú principal)
        btnVolver = crearBotonMenu("←", "Volver al Menú Principal",
                "Regresar al menú principal", COLOR_VOLVER);
        btnVolver.setVisible(false); // Oculto en menú principal
    }

    private JButton crearBotonMenu(String icono, String texto, String descripcion, Color color) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(10, 10));

        // Fondo blanco para máximo contraste
        boton.setBackground(color);
        boton.setForeground(color);
        boton.setFont(FUENTE_BOTON);
        boton.setFocusPainted(false);

        // Borde con el color original
        boton.setBorder(BorderFactory.createLineBorder(color, 3));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Panel interno para el contenido del botón
        JPanel contenidoPanel = new JPanel(new BorderLayout(5, 5));
        contenidoPanel.setOpaque(false);

        // Icono - Añadido más espacio arriba y forzado centrado
        JLabel lblIcono = new JLabel(icono, SwingConstants.CENTER);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcono.setBorder(new EmptyBorder(30, 0, 0, 0)); // Más espacio arriba (15 píxeles)
        //lblIcono.setVerticalAlignment(SwingConstants.CENTER); // Forzar centrado vertical
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER); // Forzar centrado horizontal

        // Texto principal - usa una versión más oscura del color para buen contraste
        JLabel lblTexto = new JLabel(texto, SwingConstants.CENTER);
        lblTexto.setFont(FUENTE_BOTON);
        lblTexto.setForeground(color.darker().darker()); // Más oscuro para mejor contraste

        // Descripción - usa el mismo color oscuro
        JLabel lblDescripcion = new JLabel(descripcion, SwingConstants.CENTER);
        lblDescripcion.setFont(FUENTE_DESCRIPCION);
        lblDescripcion.setForeground(color.darker().darker()); // Más oscuro para mejor contraste

        JPanel textoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textoPanel.setOpaque(false);
        textoPanel.add(lblTexto);
        textoPanel.add(lblDescripcion);

        contenidoPanel.add(lblIcono, BorderLayout.NORTH);
        contenidoPanel.add(textoPanel, BorderLayout.CENTER);

        boton.add(contenidoPanel, BorderLayout.CENTER);

        return boton;
    }

    private void setupLayout() {
        // Panel principal con fondo
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel de menú con grid
        JPanel menuPanel = createMenuPanel();
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel("SifPyme");
        lblTitle.setFont(FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Sistema de Facturación Profesional");
        lblSubtitle.setFont(FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        // Información de estado (podría mostrar la empresa actual, etc.)
        JLabel lblEstado = new JLabel("Modo: Demo - Bienvenido", SwingConstants.RIGHT);
        lblEstado.setFont(FUENTE_SUBTITULO);
        lblEstado.setForeground(new Color(240, 240, 240));

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(lblEstado, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Fila 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(btnEmpresas, gbc);

        gbc.gridx = 1;
        panel.add(btnClientes, gbc);

        gbc.gridx = 2;
        panel.add(btnProductos, gbc);

        // Fila 2
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(btnFacturas, gbc);

        gbc.gridx = 1;
        panel.add(btnConfiguracion, gbc);

        gbc.gridx = 2;
        panel.add(btnSalir, gbc);

        // Fila 3 - Botón Volver (ocupa todo el ancho)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weighty = 0.3; // Menos peso vertical para el botón volver
        panel.add(btnVolver, gbc);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        JLabel lblVersion = new JLabel("SifPyme v1.0.0 - © 2024");
        lblVersion.setFont(FUENTE_DESCRIPCION);
        lblVersion.setForeground(Color.GRAY);

        JLabel lblEstado = new JLabel("Sistema listo", SwingConstants.RIGHT);
        lblEstado.setFont(FUENTE_DESCRIPCION);
        lblEstado.setForeground(COLOR_EXITO);

        panel.add(lblVersion, BorderLayout.WEST);
        panel.add(lblEstado, BorderLayout.EAST);

        return panel;
    }

    private void configurarListeners() {
        // Listener para Empresas
        btnEmpresas.addActionListener(e -> {
            EmpresasView empresasView = new EmpresasView();
            NavigationManager.getInstance().navigateTo(empresasView);
        });

        // Listener para Clientes - usa NavigationManager
        btnClientes.addActionListener(e -> {
            ClientesView clientesView = new ClientesView();
            NavigationManager.getInstance().navigateTo(clientesView);
        });

        // Listener para Productos - usa NavigationManager
        btnProductos.addActionListener(e -> {
            ProductosView productosView = new ProductosView();
            NavigationManager.getInstance().navigateTo(productosView);
        });

        // Listener para Facturas
        btnFacturas.addActionListener(e -> {
            mostrarFuncionalidadNoDisponible("Gestión de Facturas");
        });

        // Listener para Configuración
        btnConfiguracion.addActionListener(e -> {
            mostrarFuncionalidadNoDisponible("Configuración del Sistema");
        });

        // Listener para Salir
        btnSalir.addActionListener(e -> {
            confirmarSalida();
        });

        // Listener para Volver
        btnVolver.addActionListener(e -> {
            NavigationManager.getInstance().navigateBack();
        });
    }

    private void confirmarSalida() {
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que quieres salir de la aplicación?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // Métodos para asignar listeners (mantenidos por compatibilidad)
    public void setEmpresasListener(ActionListener listener) {
        btnEmpresas.addActionListener(listener);
    }

    public void setClientesListener(ActionListener listener) {
        btnClientes.addActionListener(listener);
    }

    public void setProductosListener(ActionListener listener) {
        btnProductos.addActionListener(listener);
    }

    public void setFacturasListener(ActionListener listener) {
        btnFacturas.addActionListener(listener);
    }

    public void setConfiguracionListener(ActionListener listener) {
        btnConfiguracion.addActionListener(listener);
    }

    public void setSalirListener(ActionListener listener) {
        btnSalir.addActionListener(listener);
    }

    public void setVolverListener(ActionListener listener) {
        btnVolver.addActionListener(listener);
    }

    // Método para controlar la visibilidad del botón volver
    public void setMostrarBotonVolver(boolean mostrar) {
        btnVolver.setVisible(mostrar);
    }

    // Método para mostrar mensajes de funcionalidad no implementada
    public void mostrarFuncionalidadNoDisponible(String modulo) {
        JOptionPane.showMessageDialog(
                this,
                "La funcionalidad de " + modulo + " estará disponible próximamente.",
                "Funcionalidad en Desarrollo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Método principal para testing (modificado para usar NavigationManager)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainMenuView menu = new MainMenuView();
            NavigationManager.getInstance().navigateToAndCloseCurrent(menu);
        });
    }
}