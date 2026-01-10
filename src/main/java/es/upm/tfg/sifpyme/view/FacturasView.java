package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.FacturaController;
import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.service.FacturaPDFService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de lista de facturas
 * REFACTORIZADO:
 * - Columnas actualizadas: ahora muestra "ID Factura" en vez de Serie/Número
 * - Usa UIHelper y UITheme
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
        // CAMBIADO: ID Factura en vez de Serie + Número
        return new String[]{ 
            "ID Factura", "Fecha", "Cliente", "Empresa",
            "Subtotal", "IVA", "Total", "Método Pago"
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
        // CAMBIADO: Ajustado para nueva estructura de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(150);  // ID Factura
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);  // Fecha
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);  // Cliente
        tabla.getColumnModel().getColumn(3).setPreferredWidth(200);  // Empresa
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Subtotal
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // IVA
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);  // Total
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120);  // Método Pago
    }

    @Override
    protected void cargarDatos() {
        if (controller == null) {
            controller = new FacturaController();
        }
        
        modeloTabla.setRowCount(0);

        List<Factura> facturas = controller.obtenerTodasLasFacturas();

        for (Factura factura : facturas) {
            String nombreCliente = factura.getCliente() != null ? 
                factura.getCliente().getNombreFiscal() : "";
            
            String nombreEmpresa = factura.getEmpresa() != null ? 
                factura.getEmpresa().getNombreComercial() : "";

            Object[] fila = {
                factura.getIdFactura(),  // CAMBIADO: ID completo
                factura.getFechaEmision().format(DATE_FORMATTER),
                nombreCliente,
                nombreEmpresa,
                formatearMoneda(factura.getSubtotal()),
                formatearMoneda(factura.getTotalIva()),
                formatearMoneda(factura.getTotal()),
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
        // NOTA: Ya no usamos Integer ID, ahora es String
        // Este método no se usa, pero lo mantenemos por la interfaz
        return null;
    }
    
    /**
     * NUEVO: Método sobrecargado para usar String ID
     */
    protected JPanel crearFormularioEdicion(String idFactura) {
        Factura factura = controller.obtenerFacturaPorId(idFactura);
        
        if (factura != null) {
            if (factura.getLineas() == null || factura.getLineas().isEmpty()) {
                System.err.println("ADVERTENCIA: La factura " + idFactura + " no tiene líneas cargadas");
                factura = controller.obtenerFacturaPorId(idFactura);
            }
            
            int numLineas = factura.getLineas() != null ? factura.getLineas().size() : 0;
            System.out.println("DEBUG: Cargando factura " + idFactura + " con " + numLineas + " líneas");
            
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
    protected void mostrarFormularioEdicion() {
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
        String idFactura = (String) modeloTabla.getValueAt(filaModelo, 0); // CAMBIADO: String en vez de Integer

        JPanel formulario = crearFormularioEdicion(idFactura); // CAMBIADO: Usar el método sobrecargado
        if (formulario != null) {
            Component[] components = cardPanel.getComponents();
            for (Component comp : components) {
                if (comp.getName() != null && comp.getName().equals(getNombreCardFormulario())) {
                    cardPanel.remove(comp);
                    break;
                }
            }
            
            formulario.setName(getNombreCardFormulario());
            cardPanel.add(formulario, getNombreCardFormulario());
            cardLayout.show(cardPanel, getNombreCardFormulario());
        } else {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar la factura seleccionada.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected boolean eliminarRegistro(Integer id) {
        // Este método no se usa porque ahora usamos String
        return false;
    }
    
    /**
     * NUEVO: Método sobrecargado para eliminar con String ID
     */
    protected boolean eliminarRegistro(String idFactura) {
        return controller.eliminarFactura(idFactura);
    }
    
    @Override
    protected void eliminar() {
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
        String idFactura = (String) modeloTabla.getValueAt(filaModelo, 0); // CAMBIADO: String

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que deseas eliminar la factura:\n" +
                idFactura + "?\n\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = eliminarRegistro(idFactura); // CAMBIADO: Usar String

            if (eliminado) {
                JOptionPane.showMessageDialog(
                    this,
                    "Factura eliminada exitosamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo eliminar la factura.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void agregarBotonesAdicionales(JPanel buttonsPanel) {
        JButton btnGenerarPDF = UIHelper.crearBoton(
            "Generar PDF", 
            new Color(231, 76, 60),
            UITheme.ICONO_PDF
        );
        btnGenerarPDF.addActionListener(e -> generarPDF());
        buttonsPanel.add(btnGenerarPDF, 0);
    }

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
        String idFactura = (String) modeloTabla.getValueAt(filaModelo, 0); // CAMBIADO: String

        Factura factura = controller.obtenerFacturaPorId(idFactura);
        if (factura == null) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar la factura seleccionada.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return; 
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Factura PDF");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String nombreArchivo = String.format("Factura_%s.pdf", idFactura.replace("/", "-"));
        fileChooser.setSelectedFile(new File(nombreArchivo));

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
            
            if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
                archivo = new File(archivo.getAbsolutePath() + ".pdf");
            }
            
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
            
            generarPDFEnSegundoPlano(factura, archivo.getAbsolutePath());
        }
    }

    private void generarPDFEnSegundoPlano(Factura factura, String rutaDestino) {
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