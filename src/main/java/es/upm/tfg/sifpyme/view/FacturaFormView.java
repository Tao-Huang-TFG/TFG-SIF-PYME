package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.model.entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear/editar facturas
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 * Incluye tabla de líneas interactiva
 */
public class FacturaFormView extends BaseFormView<Factura> {

    private final FacturaController controller;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Campos de encabezado
    private JComboBox<Empresa> cmbEmpresa;
    private JComboBox<Cliente> cmbCliente;
    private JTextField txtSerie;
    private JTextField txtNumero;
    private JTextField txtFecha;
    private JComboBox<String> cmbMetodoPago;
    private JTextArea txtObservaciones;
    
    // Tabla de líneas
    private JTable tablaLineas;
    private DefaultTableModel modeloLineas;
    private JButton btnAgregarLinea;
    private JButton btnEliminarLinea;
    private JButton btnEditarLinea;
    
    // Totales
    private JLabel lblSubtotal;
    private JLabel lblTotalIva;
    private JLabel lblTotalRetencion;
    private JLabel lblTotal;
    
    // Lista de líneas en memoria
    private List<LineaFactura> lineasFactura;

    public FacturaFormView(CardLayout cardLayout, JPanel cardPanel) {
        this(cardLayout, cardPanel, null);
    }

    public FacturaFormView(CardLayout cardLayout, JPanel cardPanel, Factura facturaEditar) {
        super(cardLayout, cardPanel, facturaEditar);
        this.controller = new FacturaController();
        this.lineasFactura = new ArrayList<>();
        
        cargarCombos();
        
        if (!modoEdicion) {
            // Configurar valores por defecto para nueva factura
            establecerValoresPorDefecto();
        }
    }

    @Override
    protected void configurarColores() {
        // Usar el color definido en UITheme para facturas
        COLOR_PRIMARIO = UITheme.COLOR_FACTURAS;
    }

    @Override
    protected String getTituloFormulario() {
        return modoEdicion ? "Editar Factura" : "Nueva Factura";
    }

    @Override
    protected String getSubtituloFormulario() {
        return modoEdicion ? 
            "Modifica los datos de la factura" : 
            "Crea una nueva factura para tu cliente";
    }

    @Override
    protected String getIconoFormulario() {
        // Usar el icono centralizado de UITheme
        return UITheme.ICONO_FACTURAS;
    }

    @Override
    protected String getNombreCardLista() {
        return "listaFacturas";
    }

    @Override
    protected void inicializarCamposEspecificos() {
        // Encabezado usando UIHelper
        cmbEmpresa = UIHelper.crearComboBox();
        cmbCliente = UIHelper.crearComboBox();
        txtSerie = UIHelper.crearCampoTexto(10);
        txtNumero = UIHelper.crearCampoTexto(15);
        txtFecha = UIHelper.crearCampoTexto(15);
        txtFecha.setText(LocalDate.now().format(DATE_FORMATTER));
        
        cmbMetodoPago = UIHelper.crearComboBox();
        cmbMetodoPago.addItem("Transferencia");
        cmbMetodoPago.addItem("Tarjeta");
        cmbMetodoPago.addItem("Efectivo");
        cmbMetodoPago.addItem("PayPal");
        cmbMetodoPago.addItem("Bizum");
        
        txtObservaciones = UIHelper.crearAreaTexto(3, 30);
        
        // Tabla de líneas
        String[] columnasLineas = {
            "Nº", "Producto", "Cantidad", "Precio", "Descuento %", 
            "Subtotal", "IVA %", "Imp. IVA", "Retención %", "Imp. Ret.", "Total"
        };
        modeloLineas = new DefaultTableModel(columnasLineas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaLineas = new JTable(modeloLineas);
        tablaLineas.setFont(UITheme.FUENTE_TABLA);
        tablaLineas.setRowHeight(35);
        tablaLineas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaLineas.setForeground(Color.DARK_GRAY);
        
        configurarAnchoColumnasLineas();
        
        // Botones de líneas usando UIHelper
        btnAgregarLinea = UIHelper.crearBoton("Agregar Línea", UITheme.COLOR_EXITO, UITheme.ICONO_AGREGAR);
        btnAgregarLinea.addActionListener(e -> agregarLinea());
        
        btnEditarLinea = UIHelper.crearBotonAccion("editar", "Editar");
        btnEditarLinea.addActionListener(e -> editarLinea());
        
        btnEliminarLinea = UIHelper.crearBotonAccion("eliminar", "Eliminar");
        btnEliminarLinea.addActionListener(e -> eliminarLinea());
        
        // Labels de totales
        lblSubtotal = new JLabel("0,00 €");
        lblTotalIva = new JLabel("0,00 €");
        lblTotalRetencion = new JLabel("0,00 €");
        lblTotal = new JLabel("0,00 €");
        
        // Usar fuentes de UITheme
        lblSubtotal.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotalIva.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotalRetencion.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotal.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTotal.setForeground(COLOR_PRIMARIO);
        
        // Listener para cambio de empresa -> actualizar número
        cmbEmpresa.addActionListener(e -> actualizarNumeroFactura());
        txtSerie.addActionListener(e -> actualizarNumeroFactura());
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

        // Panel de encabezado usando UIHelper
        panel.add(crearPanelEncabezado(), gbc);

        // Panel de líneas
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(crearPanelLineas(), gbc);

        // Panel de totales
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(crearPanelTotales(), gbc);

        return panel;
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = UIHelper.crearSeccionPanel("Datos de la Factura", COLOR_PRIMARIO);
        
        addFormFieldCombo(panel, "Empresa:", cmbEmpresa, true, 0);
        addFormFieldCombo(panel, "Cliente:", cmbCliente, true, 1);
        
        // Fila de serie y número
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);
        
        JLabel lblSerie = new JLabel("Serie: *");
        lblSerie.setFont(UITheme.FUENTE_ETIQUETA);
        lblSerie.setForeground(UITheme.COLOR_PELIGRO);
        panel.add(lblSerie, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(txtSerie, gbc);
        
        // Número al lado
        gbc.gridx = 1;
        gbc.insets = new Insets(8, 150, 8, 0);
        JLabel lblNumero = new JLabel("Número: *");
        lblNumero.setFont(UITheme.FUENTE_ETIQUETA);
        lblNumero.setForeground(UITheme.COLOR_PELIGRO);
        panel.add(lblNumero, gbc);
        
        gbc.gridx = 1;
        gbc.insets = new Insets(8, 250, 8, 0);
        panel.add(txtNumero, gbc);
        
        // Fecha y método de pago
        gbc.insets = new Insets(8, 0, 8, 15);
        addFormField(panel, "Fecha:", txtFecha, true, 3);
        addFormFieldCombo(panel, "Método de Pago:", cmbMetodoPago, true, 4);
        addFormFieldTextArea(panel, "Observaciones:", txtObservaciones, false, 5);
        
        return panel;
    }

    private JPanel crearPanelLineas() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Título usando fuentes de UITheme
        JLabel lblTitulo = new JLabel("Líneas de Factura");
        lblTitulo.setFont(UITheme.FUENTE_SUBTITULO_NEGRITA);
        lblTitulo.setForeground(COLOR_PRIMARIO);
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(tablaLineas);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        botonesPanel.setBackground(Color.WHITE);
        botonesPanel.add(btnAgregarLinea);
        botonesPanel.add(btnEditarLinea);
        botonesPanel.add(btnEliminarLinea);
        panel.add(botonesPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        
        // Subtotal
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lbl1 = new JLabel("Subtotal:");
        lbl1.setFont(UITheme.FUENTE_ETIQUETA);
        panel.add(lbl1, gbc);
        
        gbc.gridx = 1;
        panel.add(lblSubtotal, gbc);
        
        // Total IVA
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lbl2 = new JLabel("Total IVA:");
        lbl2.setFont(UITheme.FUENTE_ETIQUETA);
        panel.add(lbl2, gbc);
        
        gbc.gridx = 1;
        panel.add(lblTotalIva, gbc);
        
        // Total Retención
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lbl3 = new JLabel("Total Retención:");
        lbl3.setFont(UITheme.FUENTE_ETIQUETA);
        panel.add(lbl3, gbc);
        
        gbc.gridx = 1;
        panel.add(lblTotalRetencion, gbc);
        
        // Línea separadora
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        JSeparator separator = new JSeparator();
        panel.add(separator, gbc);
        
        // TOTAL
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel lblTotalLabel = new JLabel("TOTAL:");
        lblTotalLabel.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTotalLabel.setForeground(COLOR_PRIMARIO);
        panel.add(lblTotalLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(lblTotal, gbc);
        
        return panel;
    }

    private void configurarAnchoColumnasLineas() {
        tablaLineas.getColumnModel().getColumn(0).setPreferredWidth(40);   // Nº
        tablaLineas.getColumnModel().getColumn(1).setPreferredWidth(200);  // Producto
        tablaLineas.getColumnModel().getColumn(2).setPreferredWidth(80);   // Cantidad
        tablaLineas.getColumnModel().getColumn(3).setPreferredWidth(80);   // Precio
        tablaLineas.getColumnModel().getColumn(4).setPreferredWidth(80);   // Descuento
        tablaLineas.getColumnModel().getColumn(5).setPreferredWidth(90);   // Subtotal
        tablaLineas.getColumnModel().getColumn(6).setPreferredWidth(70);   // IVA %
        tablaLineas.getColumnModel().getColumn(7).setPreferredWidth(80);   // Imp. IVA
        tablaLineas.getColumnModel().getColumn(8).setPreferredWidth(80);   // Ret %
        tablaLineas.getColumnModel().getColumn(9).setPreferredWidth(80);   // Imp. Ret
        tablaLineas.getColumnModel().getColumn(10).setPreferredWidth(90);  // Total
    }

    // Métodos auxiliares
    private void cargarCombos() {
        // Cargar empresas
        List<Empresa> empresas = controller.obtenerEmpresas();
        for (Empresa empresa : empresas) {
            cmbEmpresa.addItem(empresa);
        }
        
        // Seleccionar empresa por defecto
        for (int i = 0; i < cmbEmpresa.getItemCount(); i++) {
            Empresa emp = cmbEmpresa.getItemAt(i);
            if (emp.getPorDefecto() != null && emp.getPorDefecto()) {
                cmbEmpresa.setSelectedIndex(i);
                break;
            }
        }
        
        // Cargar clientes
        List<Cliente> clientes = controller.obtenerClientes();
        for (Cliente cliente : clientes) {
            cmbCliente.addItem(cliente);
        }
    }

    private void establecerValoresPorDefecto() {
        txtSerie.setText("A");
        actualizarNumeroFactura();
    }

    private void actualizarNumeroFactura() {
        if (!modoEdicion && cmbEmpresa.getSelectedItem() != null) {
            Empresa empresa = (Empresa) cmbEmpresa.getSelectedItem();
            String serie = txtSerie.getText().trim();
            
            if (!serie.isEmpty()) {
                String siguiente = controller.obtenerSiguienteNumero(
                    empresa.getIdEmpresa(), serie);
                txtNumero.setText(siguiente);
            }
        }
    }

    @Override
    protected void cargarDatosEntidad() {
        if (entidadEditar != null) {
            // Cargar empresa
            for (int i = 0; i < cmbEmpresa.getItemCount(); i++) {
                if (cmbEmpresa.getItemAt(i).getIdEmpresa().equals(entidadEditar.getIdEmpresa())) {
                    cmbEmpresa.setSelectedIndex(i);
                    break;
                }
            }
            
            // Cargar cliente
            for (int i = 0; i < cmbCliente.getItemCount(); i++) {
                if (cmbCliente.getItemAt(i).getIdCliente().equals(entidadEditar.getIdCliente())) {
                    cmbCliente.setSelectedIndex(i);
                    break;
                }
            }
            
            txtSerie.setText(entidadEditar.getSerie());
            txtNumero.setText(entidadEditar.getNumeroFactura());
            txtFecha.setText(entidadEditar.getFechaEmision().format(DATE_FORMATTER));
            cmbMetodoPago.setSelectedItem(entidadEditar.getMetodoPago());
            
            if (entidadEditar.getObservaciones() != null) {
                txtObservaciones.setText(entidadEditar.getObservaciones());
            }
            
            // Cargar líneas
            if (entidadEditar.getLineas() != null) {
                lineasFactura = new ArrayList<>(entidadEditar.getLineas());
                actualizarTablaLineas();
                actualizarTotales();
            }
        }
    }

    @Override
    protected boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (cmbEmpresa.getSelectedItem() == null) {
            errores.append("• Debe seleccionar una empresa\n");
        }
        
        if (cmbCliente.getSelectedItem() == null) {
            errores.append("• Debe seleccionar un cliente\n");
        }
        
        if (txtSerie.getText().trim().isEmpty()) {
            errores.append("• Serie es obligatoria\n");
        }
        
        if (txtNumero.getText().trim().isEmpty()) {
            errores.append("• Número de factura es obligatorio\n");
        }
        
        if (cmbMetodoPago.getSelectedItem() == null) {
            errores.append("• Debe seleccionar un método de pago\n");
        }
        
        if (lineasFactura.isEmpty()) {
            errores.append("• Debe agregar al menos una línea a la factura\n");
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
            Factura factura = modoEdicion ? entidadEditar : new Factura();

            if (modoEdicion && entidadEditar != null) {
                factura.setIdFactura(entidadEditar.getIdFactura());
            }

            Empresa empresa = (Empresa) cmbEmpresa.getSelectedItem();
            Cliente cliente = (Cliente) cmbCliente.getSelectedItem();

            factura.setIdEmpresa(empresa.getIdEmpresa());
            factura.setIdCliente(cliente.getIdCliente());
            factura.setSerie(txtSerie.getText().trim());
            factura.setNumeroFactura(txtNumero.getText().trim());
            factura.setFechaEmision(LocalDate.parse(txtFecha.getText(), DATE_FORMATTER));
            factura.setMetodoPago((String) cmbMetodoPago.getSelectedItem());
            
            String obs = txtObservaciones.getText().trim();
            factura.setObservaciones(obs.isEmpty() ? null : obs);
            
            factura.setEstado("EMITIDA");
            factura.setLineas(lineasFactura);

            boolean success = modoEdicion ? 
                controller.actualizarFactura(factura) : 
                controller.guardarFactura(factura);

            return success;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al guardar: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    // Métodos para gestionar líneas
    private void agregarLinea() {
        LineaFacturaDialog dialog = new LineaFacturaDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            controller,
            null
        );
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            LineaFactura nuevaLinea = dialog.getLinea();
            nuevaLinea.setNumeroLinea(lineasFactura.size() + 1);
            lineasFactura.add(nuevaLinea);
            actualizarTablaLineas();
            actualizarTotales();
        }
    }

    private void editarLinea() {
        int filaSeleccionada = tablaLineas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Selecciona una línea para editar",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        LineaFactura lineaEditar = lineasFactura.get(filaSeleccionada);
        
        LineaFacturaDialog dialog = new LineaFacturaDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            controller,
            lineaEditar
        );
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            lineasFactura.set(filaSeleccionada, dialog.getLinea());
            actualizarTablaLineas();
            actualizarTotales();
        }
    }

    private void eliminarLinea() {
        int filaSeleccionada = tablaLineas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Selecciona una línea para eliminar",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Eliminar esta línea?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            lineasFactura.remove(filaSeleccionada);
            
            // Renumerar líneas
            for (int i = 0; i < lineasFactura.size(); i++) {
                lineasFactura.get(i).setNumeroLinea(i + 1);
            }
            
            actualizarTablaLineas();
            actualizarTotales();
        }
    }

    private void actualizarTablaLineas() {
        modeloLineas.setRowCount(0);
        
        for (LineaFactura linea : lineasFactura) {
            Producto producto = controller.obtenerProductoPorId(linea.getIdProducto());
            String nombreProducto = producto != null ? producto.getNombre() : "N/A";
            
            Object[] fila = {
                linea.getNumeroLinea(),
                nombreProducto,
                formatearNumero(linea.getCantidad()),
                formatearMoneda(linea.getPrecioUnitario()),
                formatearNumero(linea.getDescuento()) + "%",
                formatearMoneda(linea.getSubtotalLinea()),
                formatearNumero(linea.getPorcentajeIva()) + "%",
                formatearMoneda(linea.getImporteIva()),
                formatearNumero(linea.getPorcentajeRetencion()) + "%",
                formatearMoneda(linea.getImporteRetencion()),
                formatearMoneda(linea.getTotalLinea())
            };
            
            modeloLineas.addRow(fila);
        }
    }

    private void actualizarTotales() {
        Factura facturaTemp = new Factura();
        facturaTemp.setLineas(lineasFactura);
        controller.calcularTotales(facturaTemp);
        
        lblSubtotal.setText(formatearMoneda(facturaTemp.getSubtotal()));
        lblTotalIva.setText(formatearMoneda(facturaTemp.getTotalIva()));
        lblTotalRetencion.setText(formatearMoneda(facturaTemp.getTotalRetencion()));
        lblTotal.setText(formatearMoneda(facturaTemp.getTotal()));
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) return "0,00 €";
        return String.format("%,.2f €", valor);
    }

    private String formatearNumero(BigDecimal valor) {
        if (valor == null) return "0";
        return String.format("%.2f", valor);
    }
}