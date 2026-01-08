package es.upm.tfg.sifpyme.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import es.upm.tfg.sifpyme.model.entity.Cliente;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generar PDFs de facturas con diseño profesional
 */
public class FacturaPDFService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaPDFService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    // Colores corporativos
    private static final BaseColor COLOR_PRIMARIO = new BaseColor(52, 152, 219);
    private static final BaseColor COLOR_SECUNDARIO = new BaseColor(236, 240, 241);
    private static final BaseColor COLOR_TEXTO = BaseColor.BLACK;
    private static final BaseColor COLOR_GRIS = new BaseColor(127, 140, 141);
    
    // Fuentes
    private Font fuenteTitulo;
    private Font fuenteSubtitulo;
    private Font fuenteNormal;
    private Font fuenteNormalBold;
    private Font fuentePequena;
    private Font fuenteTablaHeader;
    private Font fuenteTabla;

    public FacturaPDFService() {
        inicializarFuentes();
    }

    private void inicializarFuentes() {
        try {
            // Intentar usar fuentes del sistema
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont baseFontBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            
            fuenteTitulo = new Font(baseFontBold, 18, Font.BOLD, COLOR_PRIMARIO);
            fuenteSubtitulo = new Font(baseFontBold, 14, Font.BOLD, COLOR_TEXTO);
            fuenteNormal = new Font(baseFont, 10, Font.NORMAL, COLOR_TEXTO);
            fuenteNormalBold = new Font(baseFontBold, 10, Font.BOLD, COLOR_TEXTO);
            fuentePequena = new Font(baseFont, 8, Font.NORMAL, COLOR_GRIS);
            fuenteTablaHeader = new Font(baseFontBold, 9, Font.BOLD, BaseColor.WHITE);
            fuenteTabla = new Font(baseFont, 9, Font.NORMAL, COLOR_TEXTO);
            
        } catch (Exception e) {
            logger.error("Error al cargar fuentes", e);
            // Usar fuentes por defecto
            fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARIO);
            fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            fuenteNormalBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            fuentePequena = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_GRIS);
            fuenteTablaHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            fuenteTabla = FontFactory.getFont(FontFactory.HELVETICA, 9);
        }
    }

    /**
     * Genera un PDF de factura y lo guarda en el directorio especificado
     */
    public String generarPDF(Factura factura, String rutaDestino) throws DocumentException, IOException {
        // Crear directorio si no existe
        Path directorio = Paths.get(rutaDestino).getParent();
        if (directorio != null && !Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        
        document.open();
        
        // Agregar contenido
        agregarEncabezado(document, factura);
        agregarDatosEmisorReceptor(document, factura);
        agregarDatosFactura(document, factura);
        agregarTablaLineas(document, factura);
        agregarTotales(document, factura);
        agregarPiePagina(document);
        
        document.close();
        
        logger.info("PDF generado exitosamente en: {}", rutaDestino);
        return rutaDestino;
    }

    private void agregarEncabezado(Document document, Factura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2});
        
        // Columna izquierda - Logo/Nombre empresa
        PdfPCell cellEmpresa = new PdfPCell();
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        
        Paragraph nombreEmpresa = new Paragraph(factura.getEmpresa().getNombreComercial(), fuenteTitulo);
        nombreEmpresa.setSpacingAfter(5);
        cellEmpresa.addElement(nombreEmpresa);
        
        Paragraph razonSocial = new Paragraph(factura.getEmpresa().getRazonSocial(), fuenteNormal);
        cellEmpresa.addElement(razonSocial);
        
        table.addCell(cellEmpresa);
        
        // Columna derecha - FACTURA
        PdfPCell cellFactura = new PdfPCell();
        cellFactura.setBorder(Rectangle.NO_BORDER);
        cellFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellFactura.setVerticalAlignment(Element.ALIGN_TOP);
        
        Paragraph facturaTitulo = new Paragraph("FACTURA", fuenteTitulo);
        facturaTitulo.setAlignment(Element.ALIGN_RIGHT);
        cellFactura.addElement(facturaTitulo);
        
        // CAMBIO: Usar id_factura en lugar de serie + numero_factura
        Paragraph numeroFactura = new Paragraph(
            factura.getIdFactura(), 
            fuenteSubtitulo
        );
        numeroFactura.setAlignment(Element.ALIGN_RIGHT);
        cellFactura.addElement(numeroFactura);
        
        table.addCell(cellFactura);
        
        document.add(table);
        document.add(new Paragraph(" ", fuenteNormal)); // Espacio
    }

    private void agregarDatosEmisorReceptor(Document document, Factura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        // EMISOR
        PdfPCell cellEmisor = crearCelda("EMISOR", true);
        table.addCell(cellEmisor);
        
        // RECEPTOR
        PdfPCell cellReceptor = crearCelda("RECEPTOR", true);
        table.addCell(cellReceptor);
        
        // Datos del emisor
        Empresa empresa = factura.getEmpresa();
        StringBuilder emisorInfo = new StringBuilder();
        emisorInfo.append("Emisor: ").append(empresa.getRazonSocial()).append("\n");
        emisorInfo.append("Domicilio: ").append(empresa.getDireccion()).append("\n");
        emisorInfo.append("NIF: ").append(empresa.getNif());
        
        // CAMBIO: Campos eliminados en el nuevo esquema
        if (empresa.getTelefono() != null && !empresa.getTelefono().isEmpty()) {
            emisorInfo.append("\nTeléfono: ").append(empresa.getTelefono());
        }
        if (empresa.getEmail() != null && !empresa.getEmail().isEmpty()) {
            emisorInfo.append("\nEmail: ").append(empresa.getEmail());
        }
        
        PdfPCell cellEmisorDatos = new PdfPCell(new Phrase(emisorInfo.toString(), fuenteNormal));
        cellEmisorDatos.setPadding(10);
        cellEmisorDatos.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        table.addCell(cellEmisorDatos);
        
        // Datos del receptor
        Cliente cliente = factura.getCliente();
        StringBuilder receptorInfo = new StringBuilder();
        receptorInfo.append("Receptor: ").append(cliente.getNombreFiscal()).append("\n");
        if (cliente.getDireccion() != null) {
            receptorInfo.append("Domicilio: ").append(cliente.getDireccion()).append("\n");
        }
        receptorInfo.append("NIF: ").append(cliente.getNif());
        
        if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
            receptorInfo.append("\nTeléfono: ").append(cliente.getTelefono());
        }
        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
            receptorInfo.append("\nEmail: ").append(cliente.getEmail());
        }
        
        PdfPCell cellReceptorDatos = new PdfPCell(new Phrase(receptorInfo.toString(), fuenteNormal));
        cellReceptorDatos.setPadding(10);
        cellReceptorDatos.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        table.addCell(cellReceptorDatos);
        
        document.add(table);
    }

    private void agregarDatosFactura(Document document, Factura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1, 1});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        // Headers
        table.addCell(crearCelda("Número de Factura", true));
        table.addCell(crearCelda("Fecha de Expedición", true));
        table.addCell(crearCelda("Método de Pago", true));
        
        // Datos
        // CAMBIO: Usar id_factura directamente
        table.addCell(crearCelda(factura.getIdFactura(), false));
        table.addCell(crearCelda(factura.getFechaEmision().format(DATE_FORMATTER), false));
        table.addCell(crearCelda(factura.getMetodoPago(), false));
        
        document.add(table);
    }

    private void agregarTablaLineas(Document document, Factura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1.5f, 1.5f, 1.5f, 1.5f, 2});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        // Headers con fondo de color
        String[] headers = {"Descripción", "Cantidad", "Precio (€)", "IVA (%)", "Descuento (%)", "Total (€)"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fuenteTablaHeader));
            cell.setBackgroundColor(COLOR_PRIMARIO);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        
        // Líneas de factura
        int numeroLinea = 1;
        for (LineaFactura linea : factura.getLineas()) {
            // CAMBIO: Ya no tenemos referencia a producto, usar descripción general
            // Usar "Línea X" como descripción
            PdfPCell cellDescripcion = new PdfPCell(new Phrase("Línea " + numeroLinea, fuenteTabla));
            cellDescripcion.setPadding(8);
            table.addCell(cellDescripcion);
            
            // Cantidad
            table.addCell(crearCeldaNumero(formatearNumero(linea.getCantidad())));
            
            // Precio
            table.addCell(crearCeldaNumero(formatearMoneda(linea.getPrecioUnitario())));
            
            // IVA
            table.addCell(crearCeldaNumero(formatearNumero(linea.getPorcentajeIva())));
            
            // Descuento
            table.addCell(crearCeldaNumero(formatearNumero(linea.getDescuento())));
            
            // Total
            table.addCell(crearCeldaNumero(formatearMoneda(linea.getTotalLinea())));
            
            numeroLinea++;
        }
        
        document.add(table);
    }

    private void agregarTotales(Document document, Factura factura) throws DocumentException {
        // Tabla de resumen
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 2, 2, 2});
        table.setSpacingBefore(10);
        
        // Fila de headers
        table.addCell(crearCeldaTotal("Total Bruto (€)", true));
        table.addCell(crearCeldaTotal("Base Imponible (€)", true));
        table.addCell(crearCeldaTotal("Tipo IVA (%)", true));
        table.addCell(crearCeldaTotal("Total Neto (€)", true));
        
        // Calcular base imponible (subtotal sin IVA)
        BigDecimal baseImponible = factura.getSubtotal();
        BigDecimal totalBruto = factura.getSubtotal().add(factura.getTotalIva());
        
        // Calcular el porcentaje de IVA promedio (si hay múltiples)
        BigDecimal porcentajeIva = BigDecimal.ZERO;
        if (factura.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            porcentajeIva = factura.getTotalIva()
                .multiply(new BigDecimal("100"))
                .divide(factura.getSubtotal(), 2, RoundingMode.HALF_UP);
        }
        
        // Fila de datos
        table.addCell(crearCeldaTotal(formatearMoneda(totalBruto), false));
        table.addCell(crearCeldaTotal(formatearMoneda(baseImponible), false));
        table.addCell(crearCeldaTotal(formatearNumero(porcentajeIva) + " %", false));
        table.addCell(crearCeldaTotal(formatearMoneda(factura.getTotal()), false));
        
        // Si hay retención, añadir una fila adicional
        if (factura.getTotalRetencion().compareTo(BigDecimal.ZERO) > 0) {
            PdfPCell cellRetencion = new PdfPCell(new Phrase("Total Retención (€)", fuenteNormalBold));
            cellRetencion.setPadding(8);
            cellRetencion.setColspan(3);
            cellRetencion.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellRetencion.setBackgroundColor(COLOR_SECUNDARIO);
            table.addCell(cellRetencion);
            
            table.addCell(crearCeldaTotal("-" + formatearMoneda(factura.getTotalRetencion()), false));
        }
        
        document.add(table);
        
        // Total final destacado
        PdfPTable tableFinal = new PdfPTable(2);
        tableFinal.setWidthPercentage(100);
        tableFinal.setWidths(new float[]{3, 1});
        tableFinal.setSpacingBefore(15);
        
        PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL FACTURA", fuenteSubtitulo));
        cellTotalLabel.setPadding(12);
        cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotalLabel.setBackgroundColor(COLOR_PRIMARIO);
        tableFinal.addCell(cellTotalLabel);
        
        PdfPCell cellTotalValue = new PdfPCell(new Phrase(formatearMoneda(factura.getTotal()) + " €", fuenteSubtitulo));
        cellTotalValue.setPadding(12);
        cellTotalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTotalValue.setBackgroundColor(COLOR_SECUNDARIO);
        tableFinal.addCell(cellTotalValue);
        
        document.add(tableFinal);
    }

    private void agregarPiePagina(Document document) throws DocumentException {
        // Información adicional
        document.add(new Paragraph(" ", fuenteNormal));
        
        Paragraph info = new Paragraph(
            "Documento generado electrónicamente por SifPyme", 
            fuentePequena
        );
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingBefore(20);
        document.add(info);
    }

    // Métodos auxiliares
    
    private PdfPCell crearCelda(String texto, boolean esHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, esHeader ? fuenteNormalBold : fuenteNormal));
        cell.setPadding(8);
        if (esHeader) {
            cell.setBackgroundColor(COLOR_SECUNDARIO);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        return cell;
    }

    private PdfPCell crearCeldaNumero(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuenteTabla));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell crearCeldaTotal(String texto, boolean esHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, esHeader ? fuenteNormalBold : fuenteNormal));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (esHeader) {
            cell.setBackgroundColor(COLOR_SECUNDARIO);
        }
        return cell;
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) return "0.00";
        return String.format("%.2f", valor).replace(",", ".");
    }

    private String formatearNumero(BigDecimal valor) {
        if (valor == null) return "0.00";
        return String.format("%.2f", valor).replace(",", ".");
    }
    
    /**
     * Método mejorado para generar nombre de archivo
     */
    public String generarNombreArchivo(Factura factura) {
        // Formato: Factura_{id_factura}_{fecha}.pdf
        return String.format("Factura_%s_%s.pdf", 
            factura.getIdFactura(),
            factura.getFechaEmision().format(DATE_FORMATTER));
    }
    
    /**
     * Método sobrecargado con ruta por defecto
     */
    public String generarPDF(Factura factura) throws DocumentException, IOException {
        String nombreArchivo = generarNombreArchivo(factura);
        String rutaDestino = System.getProperty("user.home") + "/Downloads/" + nombreArchivo;
        return generarPDF(factura, rutaDestino);
    }
}