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
 * REFACTORIZADO: 
 * - Ahora usa id_factura personalizado (escrito por el usuario)
 * - Eliminados campos obsoletos (serie, numero)
 * - Usa UIHelper y UITheme
 */
public class FacturaFormView extends BaseFormView<Factura> {

    private FacturaController controller;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Campos de encabezado
    private JComboBox<Empresa> cmbEmpresa;
    private JComboBox<Cliente> cmbCliente;
    private JTextField txtIdFactura;  // CAMBIADO: ID personalizado
    private JTextField txtFecha;
    private JComboBox<String> cmbMetodoPago;
    
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
        cargarCombos();
        
        if (!modoEdicion) {
            establecerValoresPorDefecto();
        }
    }

    @Override
    protected void configurarColores() {
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
        return UITheme.ICONO_FACTURAS;
    }

    @Override
    protected String getNombreCardLista() {
        return "listaFacturas";
    }

    @Override
    protected void inicializarCamposEspecificos() {
        this.controller = new FacturaController();
        this.lineasFactura = new ArrayList<>();

        // Encabezado
        cmbEmpresa = UIHelper.crearComboBox();
        cmbCliente = UIHelper.crearComboBox();
        
        // CAMBIADO: Campo para ID personalizado
        txtIdFactura = UIHelper.crearCampoTexto(20);
        txtIdFactura.setToolTipText("Ejemplo: FAC-2025-001, FACT001, 2025/001");
        
        txtFecha = UIHelper.crearCampoTexto(15);
        txtFecha.setText(LocalDate.now().format(DATE_FORMATTER));
        
        cmbMetodoPago = UIHelper.crearComboBox();
        cmbMetodoPago.addItem("Transferencia");
        cmbMetodoPago.addItem("Tarjeta");
        cmbMetodoPago.addItem("Efectivo");
        cmbMetodoPago.addItem("PayPal");
        cmbMetodoPago.addItem("Bizum");
        
        // Tabla de líneas
        String[] columnasLineas = {
            "Nº", "Cantidad", "Precio", "Descuento %", 
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
        
        configurarAnchoColumnasLineas();
        
        // Botones de líneas
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
        
        lblSubtotal.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotalIva.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotalRetencion.setFont(UITheme.FUENTE_ETIQUETA);
        lblTotal.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTotal.setForeground(COLOR_PRIMARIO);
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

        panel.add(crearPanelEncabezado(), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(crearPanelLineas(), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(crearPanelTotales(), gbc);

        return panel;
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = UIHelper.crearSeccionPanel("Datos de la Factura", COLOR_PRIMARIO);
        
        // Panel de ayuda para el ID
        GridBagConstraints gbcAyuda = new GridBagConstraints();
        gbcAyuda.gridx = 0;
        gbcAyuda.gridy = 0;
        gbcAyuda.gridwidth = 2;
        gbcAyuda.fill = GridBagConstraints.HORIZONTAL;
        gbcAyuda.insets = new Insets(0, 0, 15, 0);
        
        JPanel ayudaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ayudaPanel.setBackground(new Color(230, 240, 255));
        ayudaPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_INFO, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblAyuda = new JLabel(
            "<html><b>💡 ID de Factura:</b> Introduce un identificador único (ej: FAC-2025-001, FACT001, 2025/001)</html>"
        );
        lblAyuda.setFont(UITheme.FUENTE_SUBTITULO);
        lblAyuda.setForeground(UITheme.COLOR_INFO.darker());
        ayudaPanel.add(lblAyuda);
        
        panel.add(ayudaPanel, gbcAyuda);
        
        // Campos del formulario
        addFormFieldCombo(panel, "Empresa:", cmbEmpresa, true, 1);
        addFormFieldCombo(panel, "Cliente:", cmbCliente, true, 2);
        addFormField(panel, "ID Factura:", txtIdFactura, true, 3);
        addFormField(panel, "Fecha:", txtFecha, true, 4);
        addFormFieldCombo(panel, "Método de Pago:", cmbMetodoPago, true, 5);
        
        return panel;
    }

    private JPanel crearPanelLineas() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel lblTitulo = new JLabel("Líneas de Factura");
        lblTitulo.setFont(UITheme.FUENTE_SUBTITULO_NEGRITA);
        lblTitulo.setForeground(COLOR_PRIMARIO);
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(tablaLineas);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
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
        panel.add(new JSeparator(), gbc);
        
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
        tablaLineas.getColumnModel().getColumn(1).setPreferredWidth(80);   // Cantidad
        tablaLineas.getColumnModel().getColumn(2).setPreferredWidth(80);   // Precio
        tablaLineas.getColumnModel().getColumn(3).setPreferredWidth(80);   // Descuento
        tablaLineas.getColumnModel().getColumn(4).setPreferredWidth(90);   // Subtotal
        tablaLineas.getColumnModel().getColumn(5).setPreferredWidth(70);   // IVA %
        tablaLineas.getColumnModel().getColumn(6).setPreferredWidth(80);   // Imp. IVA
        tablaLineas.getColumnModel().getColumn(7).setPreferredWidth(80);   // Ret %
        tablaLineas.getColumnModel().getColumn(8).setPreferredWidth(80);   // Imp. Ret
        tablaLineas.getColumnModel().getColumn(9).setPreferredWidth(90);   // Total
    }

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
        // Sugerir formato de ID basado en fecha actual
        String sugerencia = "FAC-" + LocalDate.now().getYear() + "-001";
        txtIdFactura.setText(sugerencia);
        txtIdFactura.selectAll(); // Seleccionar para facilitar sobrescritura
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
            
            txtIdFactura.setText(entidadEditar.getIdFactura());
            txtIdFactura.setEnabled(false); // No permitir cambiar ID en edición
            
            txtFecha.setText(entidadEditar.getFechaEmision().format(DATE_FORMATTER));
            cmbMetodoPago.setSelectedItem(entidadEditar.getMetodoPago());
            
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
        
        String idFactura = txtIdFactura.getText().trim();
        if (idFactura.isEmpty()) {
            errores.append("• ID de factura es obligatorio\n");
        } else if (idFactura.length() > 20) {
            errores.append("• ID de factura no puede exceder 20 caracteres\n");
        } else if (!modoEdicion && controller.obtenerFacturaPorId(idFactura) != null) {
            errores.append("• Ya existe una factura con este ID\n");
        }
        
        if (cmbMetodoPago.getSelectedItem() == null) {
            errores.append("• Debe seleccionar un método de pago\n");
        }
        
        if (lineasFactura == null || lineasFactura.isEmpty()) {
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

            Empresa empresa = (Empresa) cmbEmpresa.getSelectedItem();
            Cliente cliente = (Cliente) cmbCliente.getSelectedItem();

            factura.setIdFactura(txtIdFactura.getText().trim());
            factura.setIdEmpresa(empresa.getIdEmpresa());
            factura.setIdCliente(cliente.getIdCliente());
            factura.setFechaEmision(LocalDate.parse(txtFecha.getText(), DATE_FORMATTER));
            factura.setMetodoPago((String) cmbMetodoPago.getSelectedItem());
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
            if (lineasFactura == null) {
                lineasFactura = new ArrayList<>();
            }
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
            Object[] fila = {
                linea.getNumeroLinea(),
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