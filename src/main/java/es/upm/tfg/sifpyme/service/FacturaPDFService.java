package es.upm.tfg.sifpyme.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import es.upm.tfg.sifpyme.model.entity.Cliente;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            BaseFont baseFontBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252,
                    BaseFont.NOT_EMBEDDED);

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

        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            PdfWriter.getInstance(document, fos);
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
    }

    private void agregarEncabezado(Document document, Factura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3, 2 });

        // Columna izquierda - Logo/Nombre empresa
        PdfPCell cellEmpresa = new PdfPCell();
        cellEmpresa.setBorder(Rectangle.NO_BORDER);

        Paragraph nombreEmpresa = new Paragraph(factura.getEmpresa().getRazonSocial(), fuenteTitulo);
        nombreEmpresa.setSpacingAfter(5);
        cellEmpresa.addElement(nombreEmpresa);

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
                fuenteSubtitulo);
        numeroFactura.setAlignment(Element.ALIGN_RIGHT);
        cellFactura.addElement(numeroFactura);

        table.addCell(cellFactura);

        document.add(table);
        document.add(new Paragraph(" ", fuenteNormal)); // Espacio
    }

    private void agregarDatosEmisorReceptor(Document document, Factura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1, 1 });
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
        table.setWidths(new float[] { 1, 1, 1 });
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
        // Determinar si hay recargo o retención en alguna línea
        boolean hayRecargo = false;
        boolean hayRetencion = false;
        for (LineaFactura linea : factura.getLineas()) {
            if (linea.getImporteRecargo() != null && linea.getImporteRecargo().compareTo(BigDecimal.ZERO) > 0) {
                hayRecargo = true;
            }
            if (linea.getImporteRetencion() != null && linea.getImporteRetencion().compareTo(BigDecimal.ZERO) > 0) {
                hayRetencion = true;
            }
            if (hayRecargo && hayRetencion) {
                break;
            }
        }

        // Número de columnas
        int numColumnas = 6;
        if (hayRecargo)
            numColumnas++;
        if (hayRetencion)
            numColumnas++;

        PdfPTable table = new PdfPTable(numColumnas);
        table.setWidthPercentage(100);

        // Configurar anchos de columnas
        float[] anchos;
        if (hayRecargo && hayRetencion) {
            anchos = new float[] { 3, 1.2f, 1.2f, 0.8f, 1.2f, 1.2f, 1.2f, 1.2f };
        } else if (hayRecargo) {
            anchos = new float[] { 3, 1.2f, 1.2f, 0.8f, 1.2f, 1.2f, 1.2f };
        } else if (hayRetencion) {
            anchos = new float[] { 3, 1.2f, 1.2f, 0.8f, 1.2f, 1.2f, 1.2f };
        } else {
            anchos = new float[] { 3, 1.2f, 1.2f, 0.8f, 1.2f, 1.2f };
        }
        table.setWidths(anchos);

        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Headers con fondo de color
        List<String> headersList = new ArrayList<>();
        headersList.add("Producto");
        headersList.add("Cantidad");
        headersList.add("Precio Base (€)");
        headersList.add("IVA (%)");
        headersList.add("Descuento (%)");
        if (hayRecargo) {
            headersList.add("Recargo (%)");
        }
        if (hayRetencion) {
            headersList.add("Retención (%)");
        }
        headersList.add("Total (€)");

        for (String header : headersList) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fuenteTablaHeader));
            cell.setBackgroundColor(COLOR_PRIMARIO);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Líneas de factura
        for (LineaFactura linea : factura.getLineas()) {
            // CAMBIO: Usar nombre_producto si está disponible, o descripción por defecto
            String nombre_producto;
            if (linea.getNombreProducto() != null && !linea.getNombreProducto().trim().isEmpty()) {
                nombre_producto = linea.getNombreProducto();
            } else if (linea.getProducto() != null && linea.getProducto().getNombre() != null) {
                nombre_producto = linea.getProducto().getNombre();
            } else {
                // Si no hay nombre, usar "Línea X" como fallback
                nombre_producto = "Línea " + linea.getNumeroLinea();
            }

            PdfPCell cellProducto = new PdfPCell(new Phrase(nombre_producto, fuenteTabla));
            cellProducto.setPadding(6);
            table.addCell(cellProducto);

            // Cantidad
            table.addCell(crearCeldaNumero(formatearNumero(linea.getCantidad())));

            // Precio
            table.addCell(crearCeldaNumero(formatearMoneda(linea.getPrecioBase())));

            // IVA
            table.addCell(crearCeldaNumero(formatearNumero(linea.getPorcentajeIva())));

            // Descuento
            table.addCell(crearCeldaNumero(formatearNumero(linea.getDescuento())));

            // Recargo
            if (hayRecargo) {
                BigDecimal recargo = linea.getPorcentajeRecargo() != null ? linea.getPorcentajeRecargo() : BigDecimal.ZERO;
                table.addCell(crearCeldaNumero(formatearNumero(recargo)));
            }

            // Retención
            if (hayRetencion) {
                BigDecimal retencion = linea.getPorcentajeRetencion() != null ? linea.getPorcentajeRetencion() : BigDecimal.ZERO;
                table.addCell(crearCeldaNumero(formatearNumero(retencion)));
            }

            // Total
            table.addCell(crearCeldaNumero(formatearMoneda(linea.getTotalLinea())));
        }

        document.add(table);
    }

    private void agregarTotales(Document document, Factura factura) throws DocumentException {

        PdfPTable tablaResumen = new PdfPTable(2);
        tablaResumen.setWidthPercentage(50);
        tablaResumen.setWidths(new float[] { 3, 2 });
        tablaResumen.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // Total Bruto
        BigDecimal totalBruto = BigDecimal.ZERO;
        for (LineaFactura linea : factura.getLineas()) {
            BigDecimal precioBrutoLinea = linea.getPrecioBase()
                    .multiply(linea.getCantidad())
                    .multiply(
                            BigDecimal.ONE.subtract(
                                    linea.getDescuento().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
            totalBruto = totalBruto.add(precioBrutoLinea);
        }

        PdfPCell cellTotalBrutoLabel = new PdfPCell(new Phrase("TOTAL BRUTO:", fuenteNormalBold));
        cellTotalBrutoLabel.setBorder(Rectangle.NO_BORDER);
        cellTotalBrutoLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaResumen.addCell(cellTotalBrutoLabel);

        PdfPCell cellTotalBrutoValor = new PdfPCell(
                new Phrase(formatearMoneda(totalBruto), fuenteNormal));
        cellTotalBrutoValor.setBorder(Rectangle.NO_BORDER);
        cellTotalBrutoValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaResumen.addCell(cellTotalBrutoValor);

        // Agrupar por tipo de IVA para mostrar líneas individuales
        Map<BigDecimal, BigDecimal> basePorIva = new HashMap<>();
        Map<BigDecimal, BigDecimal> ivaPorTipo = new HashMap<>();

        for (LineaFactura linea : factura.getLineas()) {
            BigDecimal baseImponibleLinea = linea.getPrecioBase()
                    .multiply(linea.getCantidad())
                    .multiply(
                            BigDecimal.ONE.subtract(
                                    linea.getDescuento().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));

            basePorIva.merge(linea.getPorcentajeIva(), baseImponibleLinea, BigDecimal::add);

            BigDecimal ivaLinea = baseImponibleLinea
                    .multiply(linea.getPorcentajeIva())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            ivaPorTipo.merge(linea.getPorcentajeIva(), ivaLinea, BigDecimal::add);
        }

        // Mostrar una línea por cada tipo de IVA
        for (Map.Entry<BigDecimal, BigDecimal> entry : basePorIva.entrySet()) {
            BigDecimal tipoIva = entry.getKey();
            BigDecimal importeIva = ivaPorTipo.get(tipoIva);

            // Etiqueta con el tipo de IVA
            PdfPCell cellIvaLabel = new PdfPCell(
                    new Phrase(String.format("IVA %s%%:", formatearNumero(tipoIva)), fuenteNormalBold));
            cellIvaLabel.setBorder(Rectangle.NO_BORDER);
            cellIvaLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cellIvaLabel);

            // Valor con base imponible y subtotal neto
            String valor = String.format("+ %s",
                    formatearMoneda(importeIva));

            PdfPCell cellIvaValor = new PdfPCell(new Phrase(valor, fuenteNormal));
            cellIvaValor.setBorder(Rectangle.NO_BORDER);
            cellIvaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cellIvaValor);
        }

        // Recargo (si existe)
        if (factura.getTotalRecargo() != null && factura.getTotalRecargo().compareTo(BigDecimal.ZERO) > 0) {
            PdfPCell cellRecargoLabel = new PdfPCell(new Phrase("RECARGO:", fuenteNormalBold));
            cellRecargoLabel.setBorder(Rectangle.NO_BORDER);
            cellRecargoLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cellRecargoLabel);

            PdfPCell cellRecargoValor = new PdfPCell(
                    new Phrase("+" + formatearMoneda(factura.getTotalRecargo()), fuenteNormal));
            cellRecargoValor.setBorder(Rectangle.NO_BORDER);
            cellRecargoValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cellRecargoValor);
        }

        // Retención (si existe)
        if (factura.getTotalRetencion() != null && factura.getTotalRetencion().compareTo(BigDecimal.ZERO) > 0) {
            PdfPCell cellRetLabel = new PdfPCell(new Phrase("RETENCIÓN IRPF:", fuenteNormalBold));
            cellRetLabel.setBorder(Rectangle.NO_BORDER);
            cellRetLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cellRetLabel);

            PdfPCell cellRetValor = new PdfPCell(
                    new Phrase("-" + formatearMoneda(factura.getTotalRetencion()), fuenteNormal));
            cellRetValor.setBorder(Rectangle.NO_BORDER);
            cellRetValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaResumen.addCell(cellRetValor);
        }

        // Separador antes del total
        PdfPCell cellSeparadorLabel = new PdfPCell(new Phrase(" ", fuenteNormal));
        cellSeparadorLabel.setBorder(Rectangle.TOP);
        cellSeparadorLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaResumen.addCell(cellSeparadorLabel);

        PdfPCell cellSeparadorValor = new PdfPCell(new Phrase(" ", fuenteNormal));
        cellSeparadorValor.setBorder(Rectangle.TOP);
        cellSeparadorValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaResumen.addCell(cellSeparadorValor);

        // Total
        PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL FACTURA:", fuenteNormalBold));
        cellTotalLabel.setBorder(Rectangle.NO_BORDER);
        cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaResumen.addCell(cellTotalLabel);

        PdfPCell cellTotalValor = new PdfPCell(new Phrase(formatearMoneda(factura.getTotal()), fuenteNormalBold));
        cellTotalValor.setBorder(Rectangle.NO_BORDER);
        cellTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaResumen.addCell(cellTotalValor);

        document.add(tablaResumen);
    }

    private void agregarPiePagina(Document document) throws DocumentException {
        // Información adicional
        document.add(new Paragraph(" ", fuenteNormal));

        Paragraph info = new Paragraph(
                "Documento generado electrónicamente por SifPyme",
                fuentePequena);
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

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null)
            return "0.00";
        return String.format("%.2f", valor).replace(",", ".");
    }

    private String formatearNumero(BigDecimal valor) {
        if (valor == null)
            return "0.00";
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