package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.util.NavigationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Menú principal de la aplicación SifPyme
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 */
public class MainMenuView extends JFrame {

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
        // Botones usando UITheme para colores e iconos
        btnEmpresas = crearBotonMenu(UITheme.ICONO_EMPRESAS, "Empresas",
                "Gestionar datos de tu empresa", UITheme.COLOR_EMPRESAS);

        btnClientes = crearBotonMenu(UITheme.ICONO_CLIENTES, "Clientes",
                "Gestionar base de datos de clientes", UITheme.COLOR_CLIENTES);

        btnProductos = crearBotonMenu(UITheme.ICONO_PRODUCTOS, "Productos y Servicios",
                "Gestionar catálogo de productos", UITheme.COLOR_PRODUCTOS);

        btnFacturas = crearBotonMenu(UITheme.ICONO_FACTURAS, "Facturas",
                "Crear y gestionar facturas", UITheme.COLOR_FACTURAS);

        btnConfiguracion = crearBotonMenu(UITheme.ICONO_CONFIG, "Configuración",
                "Ajustes del sistema", new Color(241, 196, 15));

        btnSalir = crearBotonMenu(UITheme.ICONO_SALIR, "Salir",
                "Cerrar la aplicación", UITheme.COLOR_PELIGRO);

        btnVolver = crearBotonMenu(UITheme.ICONO_VOLVER, "Volver al Menú Principal",
                "Regresar al menú principal", UITheme.COLOR_VOLVER);
        btnVolver.setVisible(false); // Oculto en menú principal
    }

    private JButton crearBotonMenu(String icono, String texto, String descripcion, Color color) {
        // Usar UIHelper para crear botones consistentes
        JButton boton = UIHelper.crearBoton("", color, icono);
        
        // Configurar layout personalizado para botones del menú
        boton.setLayout(new BorderLayout(10, 10));
        
        // Panel interno para el contenido del botón
        JPanel contenidoPanel = new JPanel(new BorderLayout(5, 5));
        contenidoPanel.setOpaque(false);

        // Icono
        JLabel lblIcono = new JLabel(icono, SwingConstants.CENTER);
        lblIcono.setFont(UITheme.FUENTE_ICONO_GRANDE);
        lblIcono.setBorder(new EmptyBorder(30, 0, 0, 0));

        // Texto principal
        JLabel lblTexto = new JLabel(texto, SwingConstants.CENTER);
        lblTexto.setFont(UITheme.FUENTE_ETIQUETA);
        lblTexto.setForeground(color.darker().darker());

        // Descripción
        JLabel lblDescripcion = new JLabel(descripcion, SwingConstants.CENTER);
        lblDescripcion.setFont(UITheme.FUENTE_SUBTITULO);
        lblDescripcion.setForeground(color.darker().darker());

        JPanel textoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textoPanel.setOpaque(false);
        textoPanel.add(lblTexto);
        textoPanel.add(lblDescripcion);

        contenidoPanel.add(lblIcono, BorderLayout.NORTH);
        contenidoPanel.add(textoPanel, BorderLayout.CENTER);

        // Limpiar el contenido anterior y añadir el nuevo
        boton.removeAll();
        boton.add(contenidoPanel, BorderLayout.CENTER);

        return boton;
    }

    private void setupLayout() {
        // Panel principal con fondo
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UITheme.COLOR_FONDO);
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
        panel.setBackground(UITheme.COLOR_MENU_PRINCIPAL);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel("SifPyme");
        lblTitle.setFont(UITheme.FUENTE_TITULO);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Sistema de Facturación Profesional");
        lblSubtitle.setFont(UITheme.FUENTE_SUBTITULO);
        lblSubtitle.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblSubtitle);

        // Información de estado
        JLabel lblEstado = new JLabel("Modo: Demo - Bienvenido", SwingConstants.RIGHT);
        lblEstado.setFont(UITheme.FUENTE_SUBTITULO);
        lblEstado.setForeground(new Color(240, 240, 240));

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(lblEstado, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_FONDO);
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
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDE),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        JLabel lblVersion = new JLabel("SifPyme v1.0.0 - © 2026");
        lblVersion.setFont(UITheme.FUENTE_SUBTITULO);
        lblVersion.setForeground(Color.GRAY);

        JLabel lblEstado = new JLabel("Sistema listo", SwingConstants.RIGHT);
        lblEstado.setFont(UITheme.FUENTE_SUBTITULO);
        lblEstado.setForeground(UITheme.COLOR_EXITO);

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

        // Listener para Clientes
        btnClientes.addActionListener(e -> {
            ClientesView clientesView = new ClientesView();
            NavigationManager.getInstance().navigateTo(clientesView);
        });

        // Listener para Productos
        btnProductos.addActionListener(e -> {
            ProductosView productosView = new ProductosView();
            NavigationManager.getInstance().navigateTo(productosView);
        });

        // Listener para Facturas
        btnFacturas.addActionListener(e -> {
            FacturasView facturasView = new FacturasView();
            NavigationManager.getInstance().navigateTo(facturasView);
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

    // Método principal para testing
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