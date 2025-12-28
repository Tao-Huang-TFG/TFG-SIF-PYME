package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FacturasView extends BaseListView<Factura> {

    private FacturaController controller;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FacturasView() {
        this.controller = new FacturaController();
        cargarDatos();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = new Color(46, 204, 113);
        COLOR_SECUNDARIO = new Color(39, 174, 96);
    }

    @Override
    protected String getTituloVentana() {
        return "Gestión de Facturas - SifPyme";
    }
    
    @Override
    protected String getTituloHeader() {
        return "Gestión de Facturas";
    }

    @Override
    protected String getSubtituloHeader() {
        return "Administra tus facturas emitidas";
    }

    @Override
    protected String getIconoHeader() {
        return "🧾";
    }

    @Override
    protected String[] getNombresColumnas() {
        return new String[]{ 
            "ID", "Número", "Serie", "Fecha", "Cliente", 
            "Subtotal", "IVA", "Total", "Estado", "Método Pago"
        };
    }

    @Override
    protected String getNombreCardLista() {
        return "listaFacturas";
    }

    @Override
    protected String getNombreCardFormulario() {
        return "formularioFactura";
    }

    @Override
    protected String getNombreEntidadSingular() {
        return "factura";
    }

    @Override
    protected String getNombreEntidadPlural() {
        return "facturas";
    }

    @Override
    protected void configurarAnchoColumnas() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);  // Número
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);   // Serie
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);  // Fecha
        tabla.getColumnModel().getColumn(4).setPreferredWidth(200);  // Cliente
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // Subtotal
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);  // IVA
        tabla.getColumnModel().getColumn(7).setPreferredWidth(100);  // Total
        tabla.getColumnModel().getColumn(8).setPreferredWidth(100);  // Estado
        tabla.getColumnModel().getColumn(9).setPreferredWidth(120);  // Método Pago
    }

    @Override
    protected void cargarDatos() {
        if (controller == null) {
            controller = new FacturaController();
        }
        
        modeloTabla.setRowCount(0);

        List<Factura> facturas = controller.obtenerTodasLasFacturas();

        for (Factura factura : facturas) {
            // Obtener el cliente para mostrar su nombre
            String nombreCliente = "";
            if (factura.getIdCliente() != null) {
                Factura facturaCompleta = controller.obtenerFacturaPorId(factura.getIdFactura());
                if (facturaCompleta != null && facturaCompleta.getCliente() != null) {
                    nombreCliente = facturaCompleta.getCliente().getNombreFiscal();
                }
            }

            Object[] fila = {
                factura.getIdFactura(),
                factura.getNumeroFactura(),
                factura.getSerie(),
                factura.getFechaEmision().format(DATE_FORMATTER),
                nombreCliente,
                formatearMoneda(factura.getSubtotal()),
                formatearMoneda(factura.getTotalIva()),
                formatearMoneda(factura.getTotal()),
                factura.getEstado(),
                factura.getMetodoPago()
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotal();
    }

    @Override
    protected JPanel crearFormularioNuevo() {
        return new FacturaFormView(cardLayout, cardPanel);
    }

    @Override
    protected JPanel crearFormularioEdicion(Integer id) {
        Factura factura = controller.obtenerFacturaPorId(id);
        if (factura != null) {
            return new FacturaFormView(cardLayout, cardPanel, factura);
        }
        return null;
    }

    @Override
    protected boolean eliminarRegistro(Integer id) {
        return controller.eliminarFactura(id);
    }

    @Override
    protected void agregarBotonesAdicionales(JPanel buttonsPanel) {
        // Botón para ver/imprimir factura
        JButton btnVer = crearBoton("👁️ Ver", new Color(52, 152, 219));
        btnVer.addActionListener(e -> verFactura());
        buttonsPanel.add(btnVer, 0); // Añadir al principio
    }

    private void verFactura() {
        int filaSeleccionada = tabla.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, selecciona una factura de la lista.",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);
        Integer id = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        Factura factura = controller.obtenerFacturaPorId(id);
        if (factura != null) {
            mostrarVistaPrevia(factura);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar la factura seleccionada.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarVistaPrevia(Factura factura) {
        // TODO: Implementar vista previa/impresión de factura
        StringBuilder sb = new StringBuilder();
        sb.append("FACTURA ").append(factura.getNumeroCompleto()).append("\n\n");
        sb.append("Fecha: ").append(factura.getFechaEmision().format(DATE_FORMATTER)).append("\n");
        sb.append("Cliente: ").append(factura.getCliente().getNombreFiscal()).append("\n");
        sb.append("NIF: ").append(factura.getCliente().getNif()).append("\n\n");
        
        sb.append("LÍNEAS DE FACTURA:\n");
        sb.append("─────────────────────────────────\n");
        for (LineaFactura linea : factura.getLineas()) {
            sb.append(linea.getProducto().getNombre())
              .append(" x ").append(linea.getCantidad())
              .append(" = ").append(formatearMoneda(linea.getTotalLinea())).append("\n");
        }
        
        sb.append("\n─────────────────────────────────\n");
        sb.append("Subtotal: ").append(formatearMoneda(factura.getSubtotal())).append("\n");
        sb.append("IVA: ").append(formatearMoneda(factura.getTotalIva())).append("\n");
        if (factura.getTotalRetencion().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Retención: -").append(formatearMoneda(factura.getTotalRetencion())).append("\n");
        }
        sb.append("TOTAL: ").append(formatearMoneda(factura.getTotal())).append("\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Vista Previa - " + factura.getNumeroCompleto(),
            JOptionPane.PLAIN_MESSAGE);
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) {
            return "0,00 €";
        }
        return String.format("%,.2f €", valor);
    }
}