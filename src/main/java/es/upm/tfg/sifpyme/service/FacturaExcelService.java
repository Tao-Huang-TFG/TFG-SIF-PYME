package es.upm.tfg.sifpyme.service;

import es.upm.tfg.sifpyme.model.entity.Cliente;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servicio para generar archivos Excel de facturas
 */
public class FacturaExcelService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaExcelService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Genera un archivo Excel de factura y lo guarda en el directorio especificado
     */
    public String generarExcel(Factura factura, String rutaDestino) throws IOException {
        // Crear directorio si no existe
        Path directorio = Paths.get(rutaDestino).getParent();
        if (directorio != null && !Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        Workbook workbook = new XSSFWorkbook();

        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            Sheet sheet = workbook.createSheet("Factura");

            // Configurar ancho de columnas
            sheet.setColumnWidth(0, 4000); // Columna A
            sheet.setColumnWidth(1, 4000); // Columna B
            sheet.setColumnWidth(2, 4000); // Columna C
            sheet.setColumnWidth(3, 4000); // Columna D
            sheet.setColumnWidth(4, 4000); // Columna E
            sheet.setColumnWidth(5, 4000); // Columna F

            // Crear estilos
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloHeader = crearEstiloHeader(workbook);
            CellStyle estiloNormal = crearEstiloNormal(workbook);
            CellStyle estiloMoneda = crearEstiloMoneda(workbook);
            CellStyle estiloTotal = crearEstiloTotal(workbook);
            CellStyle estiloSubtotal = crearEstiloSubtotal(workbook);

            int filaActual = 0;

            // Agregar contenido
            filaActual = agregarEncabezado(sheet, factura, estiloTitulo, estiloSubtitulo, filaActual);
            filaActual = agregarDatosEmisorReceptor(sheet, factura, estiloHeader, estiloNormal, filaActual);
            filaActual = agregarDatosFactura(sheet, factura, estiloHeader, estiloNormal, filaActual);
            filaActual = agregarTablaLineas(sheet, factura, estiloHeader, estiloNormal, estiloMoneda, filaActual);
            filaActual = agregarTotalesPorIva(sheet, factura, estiloHeader, estiloNormal, estiloMoneda, estiloSubtotal,
                    filaActual);
            filaActual = agregarResumenFinal(sheet, factura, estiloHeader, estiloMoneda, estiloTotal, filaActual);
            agregarPiePagina(sheet, estiloNormal, filaActual);

            workbook.write(fos);
            logger.info("Excel generado exitosamente en: {}", rutaDestino);
            return rutaDestino;

        } finally {
            workbook.close();
        }
    }

    private int agregarEncabezado(Sheet sheet, Factura factura, CellStyle estiloTitulo,
            CellStyle estiloSubtitulo, int filaActual) {
        Row row1 = sheet.createRow(filaActual++);
        Cell cellEmpresa = row1.createCell(0);
        cellEmpresa.setCellValue(factura.getEmpresa().getRazonSocial());
        cellEmpresa.setCellStyle(estiloTitulo);

        Cell cellFactura = row1.createCell(4);
        cellFactura.setCellValue("FACTURA");
        cellFactura.setCellStyle(estiloTitulo);

        Row row2 = sheet.createRow(filaActual++);
        Cell cellSubtitulo = row2.createCell(0);
        cellSubtitulo.setCellValue(factura.getEmpresa().getRazonSocial());
        cellSubtitulo.setCellStyle(estiloSubtitulo);

        Cell cellNumFactura = row2.createCell(4);
        cellNumFactura.setCellValue(factura.getIdFactura());
        cellNumFactura.setCellStyle(estiloSubtitulo);

        filaActual++; // Línea en blanco
        return filaActual;
    }

    private int agregarDatosEmisorReceptor(Sheet sheet, Factura factura, CellStyle estiloHeader,
            CellStyle estiloNormal, int filaActual) {
        // Headers
        Row rowHeader = sheet.createRow(filaActual++);
        Cell cellEmisorHeader = rowHeader.createCell(0);
        cellEmisorHeader.setCellValue("EMISOR");
        cellEmisorHeader.setCellStyle(estiloHeader);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 2));

        Cell cellReceptorHeader = rowHeader.createCell(3);
        cellReceptorHeader.setCellValue("RECEPTOR");
        cellReceptorHeader.setCellStyle(estiloHeader);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 3, 5));

        // Datos del emisor
        Empresa empresa = factura.getEmpresa();
        Row rowEmisor1 = sheet.createRow(filaActual);
        Cell cellEmisor1 = rowEmisor1.createCell(0);
        cellEmisor1.setCellValue("Emisor: " + empresa.getRazonSocial());
        cellEmisor1.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 2));

        // Datos del receptor
        Cliente cliente = factura.getCliente();
        Cell cellReceptor1 = rowEmisor1.createCell(3);
        cellReceptor1.setCellValue("Receptor: " + cliente.getNombreFiscal());
        cellReceptor1.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 3, 5));
        filaActual++;

        // Dirección emisor
        Row rowEmisor2 = sheet.createRow(filaActual);
        Cell cellEmisor2 = rowEmisor2.createCell(0);
        cellEmisor2.setCellValue("Domicilio: " + empresa.getDireccion());
        cellEmisor2.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 2));

        // Dirección receptor
        Cell cellReceptor2 = rowEmisor2.createCell(3);
        String direccionCliente = cliente.getDireccion() != null ? cliente.getDireccion() : "";
        if (!direccionCliente.isEmpty()) {
            cellReceptor2.setCellValue("Domicilio: " + direccionCliente);
        }
        cellReceptor2.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 3, 5));
        filaActual++;

        // NIF emisor
        Row rowEmisor3 = sheet.createRow(filaActual);
        Cell cellEmisor3 = rowEmisor3.createCell(0);
        cellEmisor3.setCellValue("NIF: " + empresa.getNif());
        cellEmisor3.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 2));

        // NIF receptor
        Cell cellReceptor3 = rowEmisor3.createCell(3);
        cellReceptor3.setCellValue("NIF: " + cliente.getNif());
        cellReceptor3.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 3, 5));
        filaActual++;

        // Teléfono y email emisor
        if (empresa.getTelefono() != null && !empresa.getTelefono().isEmpty()) {
            Row rowEmisor4 = sheet.createRow(filaActual);
            Cell cellEmisor4 = rowEmisor4.createCell(0);
            cellEmisor4.setCellValue("Teléfono: " + empresa.getTelefono());
            cellEmisor4.setCellStyle(estiloNormal);
            sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 2));

            // Teléfono receptor
            if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
                Cell cellReceptor4 = rowEmisor4.createCell(3);
                cellReceptor4.setCellValue("Teléfono: " + cliente.getTelefono());
                cellReceptor4.setCellStyle(estiloNormal);
                sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 3, 5));
            }
            filaActual++;
        }

        if (empresa.getEmail() != null && !empresa.getEmail().isEmpty()) {
            Row rowEmisor5 = sheet.createRow(filaActual);
            Cell cellEmisor5 = rowEmisor5.createCell(0);
            cellEmisor5.setCellValue("Email: " + empresa.getEmail());
            cellEmisor5.setCellStyle(estiloNormal);
            sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 2));

            // Email receptor
            if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
                Cell cellReceptor5 = rowEmisor5.createCell(3);
                cellReceptor5.setCellValue("Email: " + cliente.getEmail());
                cellReceptor5.setCellStyle(estiloNormal);
                sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 3, 5));
            }
            filaActual++;
        }

        filaActual++; // Línea en blanco
        return filaActual;
    }

    private int agregarDatosFactura(Sheet sheet, Factura factura, CellStyle estiloHeader,
            CellStyle estiloNormal, int filaActual) {
        // Headers
        Row rowHeader = sheet.createRow(filaActual++);

        Cell cellNumHeader = rowHeader.createCell(0);
        cellNumHeader.setCellValue("Número de Factura");
        cellNumHeader.setCellStyle(estiloHeader);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 1));

        Cell cellFechaHeader = rowHeader.createCell(2);
        cellFechaHeader.setCellValue("Fecha de Expedición");
        cellFechaHeader.setCellStyle(estiloHeader);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 2, 3));

        Cell cellMetodoHeader = rowHeader.createCell(4);
        cellMetodoHeader.setCellValue("Método de Pago");
        cellMetodoHeader.setCellStyle(estiloHeader);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 4, 5));

        // Datos
        Row rowDatos = sheet.createRow(filaActual++);

        Cell cellNum = rowDatos.createCell(0);
        cellNum.setCellValue(factura.getIdFactura());
        cellNum.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 1));

        Cell cellFecha = rowDatos.createCell(2);
        cellFecha.setCellValue(factura.getFechaEmision().format(DATE_FORMATTER));
        cellFecha.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 2, 3));

        Cell cellMetodo = rowDatos.createCell(4);
        cellMetodo.setCellValue(factura.getMetodoPago());
        cellMetodo.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 4, 5));

        filaActual++; // Línea en blanco
        return filaActual;
    }

    private int agregarTablaLineas(Sheet sheet, Factura factura, CellStyle estiloHeader,
            CellStyle estiloNormal, CellStyle estiloMoneda, int filaActual) {
        // Headers de la tabla
        Row rowHeader = sheet.createRow(filaActual++);
        String[] headers = { "Producto", "Cantidad", "Precio (€)", "IVA (%)", "Descuento (%)", "Total (€)" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloHeader);
        }

        // Líneas de factura
        for (LineaFactura linea : factura.getLineas()) {
            Row row = sheet.createRow(filaActual++);

            // Producto
            Cell cellProducto = row.createCell(0);
            String nombreProducto = linea.getNombreProducto() != null && !linea.getNombreProducto().trim().isEmpty()
                    ? linea.getNombreProducto()
                    : (linea.getProducto() != null && linea.getProducto().getNombre() != null
                            ? linea.getProducto().getNombre()
                            : "Línea " + linea.getNumeroLinea());
            cellProducto.setCellValue(nombreProducto);
            cellProducto.setCellStyle(estiloNormal);

            // Cantidad
            Cell cellCantidad = row.createCell(1);
            cellCantidad.setCellValue(formatearNumero(linea.getCantidad()));
            cellCantidad.setCellStyle(estiloNormal);

            // Precio
            Cell cellPrecio = row.createCell(2);
            cellPrecio.setCellValue(formatearNumeroDouble(linea.getPrecioUnitario()));
            cellPrecio.setCellStyle(estiloMoneda);

            // IVA
            Cell cellIva = row.createCell(3);
            cellIva.setCellValue(formatearNumero(linea.getPorcentajeIva()));
            cellIva.setCellStyle(estiloNormal);

            // Descuento
            Cell cellDescuento = row.createCell(4);
            cellDescuento.setCellValue(formatearNumero(linea.getDescuento()));
            cellDescuento.setCellStyle(estiloNormal);

            // Total
            Cell cellTotal = row.createCell(5);
            cellTotal.setCellValue(formatearNumeroDouble(linea.getTotalLinea()));
            cellTotal.setCellStyle(estiloMoneda);
        }

        filaActual++; // Línea en blanco
        return filaActual;
    }

    private int agregarTotalesPorIva(Sheet sheet, Factura factura,
            CellStyle estiloHeader, CellStyle estiloNormal,
            CellStyle estiloMoneda, CellStyle estiloSubtotal,
            int filaActual) {

        // Headers de la tabla de totales por IVA
        Row rowHeader = sheet.createRow(filaActual++);
        String[] headers = {
                "Total Bruto (€)",
                "Base Imponible (€)",
                "Tipo IVA (%)",
                "Subtotal Neto (€)",
                "Total Neto (€)"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloHeader);
        }

        // Agrupar importes por tipo de IVA
        Map<BigDecimal, BigDecimal> basePorIva = new TreeMap<>();
        Map<BigDecimal, BigDecimal> totalPorIva = new TreeMap<>();

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalBruto = BigDecimal.ZERO;

        for (LineaFactura linea : factura.getLineas()) {
            BigDecimal tipoIva = linea.getPorcentajeIva();

            basePorIva.putIfAbsent(tipoIva, BigDecimal.ZERO);
            totalPorIva.putIfAbsent(tipoIva, BigDecimal.ZERO);

            basePorIva.put(
                    tipoIva,
                    basePorIva.get(tipoIva).add(linea.getPrecioBase()));

            totalPorIva.put(
                    tipoIva,
                    totalPorIva.get(tipoIva).add(linea.getTotalLinea()));

            totalBase = totalBase.add(linea.getPrecioBase());
            totalBruto = totalBruto.add(linea.getTotalLinea());
        }

        boolean primeraFila = true;

        for (BigDecimal tipoIva : basePorIva.keySet()) {
            Row row = sheet.createRow(filaActual++);

            // 0️⃣ Subtotal (solo primera fila)
            Cell cellSubtotal = row.createCell(0);
            if (primeraFila) {
                cellSubtotal.setCellValue(formatearNumeroDouble(totalBase));
                cellSubtotal.setCellStyle(estiloSubtotal);
            }

            // 1️⃣ Base Imponible
            Cell cellBase = row.createCell(1);
            cellBase.setCellValue(formatearNumeroDouble(basePorIva.get(tipoIva)));
            cellBase.setCellStyle(estiloMoneda);

            // 2️⃣ Tipo IVA
            Cell cellTipoIva = row.createCell(2);
            cellTipoIva.setCellValue(formatearNumero(tipoIva));
            cellTipoIva.setCellStyle(estiloNormal);

            // 3️⃣ Subtotal Neto
            Cell cellSubtotalNeto = row.createCell(3);
            cellSubtotalNeto.setCellValue(formatearNumeroDouble(totalPorIva.get(tipoIva)));
            cellSubtotalNeto.setCellStyle(estiloMoneda);

            // 4️⃣ Total (solo primera fila)
            Cell cellTotal = row.createCell(4);
            if (primeraFila) {
                cellTotal.setCellValue(formatearNumeroDouble(totalBruto));
                cellTotal.setCellStyle(estiloSubtotal);
            }

            primeraFila = false;
        }

        filaActual++; // Línea en blanco
        return filaActual;
    }

    private int agregarResumenFinal(Sheet sheet, Factura factura, CellStyle estiloHeader,
            CellStyle estiloMoneda, CellStyle estiloTotal, int filaActual) {
        // Fila de retención si existe
        if (factura.getTotalRetencion().compareTo(BigDecimal.ZERO) > 0) {
            Row rowRetencion = sheet.createRow(filaActual++);

            // Etiqueta retención
            Cell cellRetLabel = rowRetencion.createCell(0);
            cellRetLabel.setCellValue("RETENCIÓN IRPF");
            cellRetLabel.setCellStyle(estiloHeader);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 2));

            // Valor retención
            Cell cellRetValor = rowRetencion.createCell(3);
            cellRetValor.setCellValue("-" + formatearNumeroDouble(factura.getTotalRetencion()));
            cellRetValor.setCellStyle(estiloMoneda);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 3, 4));

            filaActual++; // Espacio
        }

        // Fila de total final
        Row rowTotal = sheet.createRow(filaActual++);

        // Etiqueta TOTAL FACTURA
        Cell cellTotalLabel = rowTotal.createCell(0);
        cellTotalLabel.setCellValue("TOTAL FACTURA");
        cellTotalLabel.setCellStyle(estiloTotal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 3));

        // Valor total
        Cell cellTotalValor = rowTotal.createCell(4);
        cellTotalValor.setCellValue(formatearNumeroDouble(factura.getTotal()));
        cellTotalValor.setCellStyle(estiloTotal);

        filaActual++; // Línea en blanco
        return filaActual;
    }

    private void agregarPiePagina(Sheet sheet, CellStyle estiloNormal, int filaActual) {
        filaActual++; // Línea en blanco

        Row rowPie = sheet.createRow(filaActual);
        Cell cellPie = rowPie.createCell(0);
        cellPie.setCellValue("Documento generado electrónicamente por SifPyme");
        cellPie.setCellStyle(estiloNormal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 4));
    }

    // ==================== MÉTODOS PARA CREAR ESTILOS ====================

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle crearEstiloSubtitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloNormal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Formato de moneda
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));

        return style;
    }

    private CellStyle crearEstiloSubtotal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Formato de moneda
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));

        return style;
    }

    private CellStyle crearEstiloTotal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderTop(BorderStyle.DOUBLE);
        style.setBorderLeft(BorderStyle.DOUBLE);
        style.setBorderRight(BorderStyle.DOUBLE);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Formato de moneda
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));

        return style;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String formatearNumero(BigDecimal valor) {
        if (valor == null)
            return "0.00";
        return String.format("%.2f", valor).replace(",", ".");
    }

    private double formatearNumeroDouble(BigDecimal valor) {
        if (valor == null)
            return 0.00;
        return valor.doubleValue();
    }

    /**
     * Método mejorado para generar nombre de archivo
     */
    public String generarNombreArchivo(Factura factura) {
        return String.format("Factura_%s_%s.xlsx",
                factura.getIdFactura().replace("/", "-"),
                factura.getFechaEmision().format(DATE_FORMATTER));
    }

    /**
     * Método sobrecargado con ruta por defecto
     */
    public String generarExcel(Factura factura) throws IOException {
        String nombreArchivo = generarNombreArchivo(factura);
        String rutaDestino = System.getProperty("user.home") + "/Downloads/" + nombreArchivo;
        return generarExcel(factura, rutaDestino);
    }
}