package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.service.FacturaPDFService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de lista de facturas
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 * CORREGIDO: Carga completa de facturas con sus líneas para edición
 */
public class FacturasView extends BaseListView<Factura> {

    private FacturaController controller;
    private FacturaPDFService pdfService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FacturasView() {
        this.controller = new FacturaController();
        this.pdfService = new FacturaPDFService();
        cargarDatos();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = UITheme.COLOR_FACTURAS;
        COLOR_SECUNDARIO = UITheme.COLOR_FACTURAS;
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
        return UITheme.ICONO_FACTURAS;
    }

    @Override
    protected String[] getNombresColumnas() {
        return new String[]{ 
            "Serie", "Número", "Fecha", "Cliente", 
            "Subtotal", "IVA", "Total", "Emisor", "Método Pago"
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
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(8).setPreferredWidth(170);
    }

    @Override
    protected void cargarDatos() {
        if (controller == null) {
            controller = new FacturaController();
        }
        
        modeloTabla.setRowCount(0);

        List<Factura> facturas = controller.obtenerTodasLasFacturas();

        for (Factura factura : facturas) {
            String nombreCliente = "";
            if (factura.getIdCliente() != null) {
                Factura facturaCompleta = controller.obtenerFacturaPorId(factura.getIdFactura());
                if (facturaCompleta != null && facturaCompleta.getCliente() != null) {
                    nombreCliente = facturaCompleta.getCliente().getNombreFiscal();
                }
            }

            Object[] fila = {
                factura.getSerie(),
                factura.getNumeroFactura(),
                factura.getFechaEmision().format(DATE_FORMATTER),
                nombreCliente,
                formatearMoneda(factura.getSubtotal()),
                formatearMoneda(factura.getTotalIva()),
                formatearMoneda(factura.getTotal()),
                factura.getEmpresa(),
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
        // CORREGIDO: Cargar la factura COMPLETA con todas sus relaciones y líneas
        Factura factura = controller.obtenerFacturaPorId(id);
        
        if (factura != null) {
            // Verificar que las líneas se hayan cargado correctamente
            if (factura.getLineas() == null || factura.getLineas().isEmpty()) {
                System.err.println("ADVERTENCIA: La factura " + id + " no tiene líneas cargadas");
                // Intentar recargar
                factura = controller.obtenerFacturaPorId(id);
            }
            
            // Debug: mostrar cuántas líneas se cargaron
            int numLineas = factura.getLineas() != null ? factura.getLineas().size() : 0;
            System.out.println("DEBUG: Cargando factura " + id + " con " + numLineas + " líneas");
            
            return new FacturaFormView(cardLayout, cardPanel, factura);
        }
        
        JOptionPane.showMessageDialog(
            this,
            "Error: No se pudo cargar la factura completa.\nVerifique la conexión a la base de datos.",
            "Error de Carga",
            JOptionPane.ERROR_MESSAGE
        );
        
        return null;
    }

    @Override
    protected boolean eliminarRegistro(Integer id) {
        return controller.eliminarFactura(id);
    }

    @Override
    protected void agregarBotonesAdicionales(JPanel buttonsPanel) {
        // Botón para generar PDF
        JButton btnGenerarPDF = UIHelper.crearBoton(
            "Generar PDF", 
            new Color(231, 76, 60),
        UITheme.ICONO_PDF);
        btnGenerarPDF.addActionListener(e -> generarPDF());
        buttonsPanel.add(btnGenerarPDF, 0);
    }

    /**
     * Genera un PDF de la factura seleccionada
     */
    private void generarPDF() {
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
        if (factura == null) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar la factura seleccionada.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
                return; 
        }

        //Crear el diálogo de selección de carpeta
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Factura PDF");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Sugerir nombre de archivo
        String nombreArchivo = String.format("Factura_%s_%s.pdf", 
            factura.getSerie(), 
            factura.getNumeroFactura());
        fileChooser.setSelectedFile(new File(nombreArchivo));

        // Filtro para archivos PDF
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
            }
            
            @Override
            public String getDescription() {
                return "Archivos PDF (*.pdf)";
            }
        });

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            
            // Asegurar que tenga extensión .pdf
            if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
                archivo = new File(archivo.getAbsolutePath() + ".pdf");
            }
            
            // Verificar si el archivo ya existe
            if (archivo.exists()) {
                int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Deseas sobrescribirlo?",
                    "Confirmar Sobrescritura",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Generar el PDF en un hilo separado para no bloquear la UI
            generarPDFEnSegundoPlano(factura, archivo.getAbsolutePath());
        }
    }

    /**
     * Genera el PDF en un hilo separado y muestra un diálogo de progreso
     */
    private void generarPDFEnSegundoPlano(Factura factura, String rutaDestino) {
        // Crear diálogo de progreso
        JDialog dialogoProgreso = new JDialog(this, "Generando PDF", true);
        dialogoProgreso.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogoProgreso.setSize(350, 120);
        dialogoProgreso.setLocationRelativeTo(this);
        dialogoProgreso.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblMensaje = new JLabel("Generando factura PDF...");
        lblMensaje.setFont(UITheme.FUENTE_ETIQUETA);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(lblMensaje, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        dialogoProgreso.add(panel);
        
        // Worker para generar el PDF
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return pdfService.generarPDF(factura, rutaDestino);
            }
            
            @Override
            protected void done() {
                dialogoProgreso.dispose();
                
                try {
                    String ruta = get();
                    
                    // Preguntar si desea abrir el PDF
                    int respuesta = JOptionPane.showConfirmDialog(
                        FacturasView.this,
                        "PDF generado exitosamente en:\n" + ruta + "\n\n¿Deseas abrir el archivo?",
                        "PDF Generado",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    if (respuesta == JOptionPane.YES_OPTION) {
                        abrirPDF(ruta);
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        FacturasView.this,
                        "Error al generar el PDF:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        dialogoProgreso.setVisible(true);
    }

/**
     * Abre el PDF generado con la aplicación predeterminada del sistema
     */
    private void abrirPDF(String ruta) {
        try {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivo);
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "No se puede abrir el archivo automáticamente.\n" +
                        "Por favor, abre el archivo manualmente desde:\n" + ruta,
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al abrir el PDF:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) {
            return "0,00 €";
        }
        return String.format("%,.2f €", valor);
    }
}