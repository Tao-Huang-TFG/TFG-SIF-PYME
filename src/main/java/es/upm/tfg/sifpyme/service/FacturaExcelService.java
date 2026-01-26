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
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

            int filaActual = 0;

            // Agregar contenido
            filaActual = agregarEncabezado(sheet, factura, estiloTitulo, estiloSubtitulo, filaActual);
            filaActual = agregarDatosEmisorReceptor(sheet, factura, estiloHeader, estiloNormal, filaActual);
            filaActual = agregarDatosFactura(sheet, factura, estiloHeader, estiloNormal, filaActual);
            filaActual = agregarTablaLineas(sheet, factura, estiloHeader, estiloNormal, estiloMoneda, filaActual);
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

        // Headers de la tabla
        Row rowHeader = sheet.createRow(filaActual++);
        List<String> headerList = new ArrayList<>();
        headerList.add("Producto");
        headerList.add("Cantidad");
        headerList.add("Precio (€)");
        headerList.add("IVA (%)");
        headerList.add("Descuento (%)");
        if (hayRecargo) {
            headerList.add("Recargo (%)");
        }
        if (hayRetencion) {
            headerList.add("Retención (%)");
        }
        headerList.add("Total (€)");

        String[] headers = headerList.toArray(new String[0]);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloHeader);
        }

        // Líneas de factura
        for (LineaFactura linea : factura.getLineas()) {
            Row row = sheet.createRow(filaActual++);
            int colIndex = 0;

            // Producto
            Cell cellProducto = row.createCell(colIndex++);
            String nombreProducto = linea.getNombreProducto() != null && !linea.getNombreProducto().trim().isEmpty()
                    ? linea.getNombreProducto()
                    : "Línea " + linea.getNumeroLinea();
            cellProducto.setCellValue(nombreProducto);
            cellProducto.setCellStyle(estiloNormal);

            // Cantidad
            Cell cellCantidad = row.createCell(colIndex++);
            cellCantidad.setCellValue(formatearNumero(linea.getCantidad()));
            cellCantidad.setCellStyle(estiloNormal);

            // Precio
            Cell cellPrecio = row.createCell(colIndex++);
            cellPrecio.setCellValue(formatearNumeroDouble(linea.getPrecioBase()));
            cellPrecio.setCellStyle(estiloMoneda);

            // IVA
            Cell cellIva = row.createCell(colIndex++);
            cellIva.setCellValue(formatearNumero(linea.getPorcentajeIva()));
            cellIva.setCellStyle(estiloNormal);

            // Descuento
            Cell cellDescuento = row.createCell(colIndex++);
            cellDescuento.setCellValue(formatearNumero(linea.getDescuento()));
            cellDescuento.setCellStyle(estiloNormal);

            // Recargo (si hay en alguna línea)
            if (hayRecargo) {
                Cell cellRecargo = row.createCell(colIndex++);
                BigDecimal recargo = linea.getPorcentajeRecargo() != null ? linea.getPorcentajeRecargo() : BigDecimal.ZERO;
                cellRecargo.setCellValue(formatearNumeroDouble(recargo));
                cellRecargo.setCellStyle(estiloMoneda);
            }

            // Retención (si hay en alguna línea)
            if (hayRetencion) {
                Cell cellRetencion = row.createCell(colIndex++);
                BigDecimal retencion = linea.getPorcentajeRetencion() != null ? linea.getPorcentajeRetencion() : BigDecimal.ZERO;
                cellRetencion.setCellValue(formatearNumeroDouble(retencion));
                cellRetencion.setCellStyle(estiloMoneda);
            }

            // Total
            Cell cellTotal = row.createCell(colIndex++);
            cellTotal.setCellValue(formatearNumeroDouble(linea.getTotalLinea()));
            cellTotal.setCellStyle(estiloMoneda);
        }

        filaActual++; // Línea en blanco
        return filaActual;
    }

    

    private int agregarResumenFinal(Sheet sheet, Factura factura, CellStyle estiloHeader,
            CellStyle estiloMoneda, CellStyle estiloTotal, int filaActual) {

        // Agrupar por tipo de IVA
        Map<BigDecimal, BigDecimal> basePorIva = new TreeMap<>();
        Map<BigDecimal, BigDecimal> ivaPorTipo = new TreeMap<>();

        BigDecimal totalBruto = BigDecimal.ZERO;

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

            totalBruto = totalBruto.add(baseImponibleLinea);
        }

        // Fila de total bruto
        Row rowTotalBruto = sheet.createRow(filaActual++);

        Cell cellTotalBrutoLabel = rowTotalBruto.createCell(0);
        cellTotalBrutoLabel.setCellValue("TOTAL BRUTO");
        cellTotalBrutoLabel.setCellStyle(estiloHeader);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 2));

        Cell cellTotalBrutoValor = rowTotalBruto.createCell(3);
        cellTotalBrutoValor.setCellValue(formatearNumeroDouble(totalBruto));
        cellTotalBrutoValor.setCellStyle(estiloMoneda);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 3, 4));

        // Filas por tipo de IVA
        for (BigDecimal tipoIva : basePorIva.keySet()) {
            Row rowIva = sheet.createRow(filaActual++);

            BigDecimal importeIva = ivaPorTipo.get(tipoIva);

            Cell cellIvaLabel = rowIva.createCell(0);
            cellIvaLabel.setCellValue(String.format("IVA %s%%", formatearNumero(tipoIva)));
            cellIvaLabel.setCellStyle(estiloHeader);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 2));

            Cell cellIvaValor = rowIva.createCell(3);
            String valor = String.format("+%s",
                    formatearNumeroDouble(importeIva));
            cellIvaValor.setCellValue(valor);
            cellIvaValor.setCellStyle(estiloMoneda);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 3, 4));
        }

        // Fila de recargo si existe
        if (factura.getTotalRecargo() != null && factura.getTotalRecargo().compareTo(BigDecimal.ZERO) > 0) {
            Row rowRecargo = sheet.createRow(filaActual++);

            Cell cellRecargoLabel = rowRecargo.createCell(0);
            cellRecargoLabel.setCellValue("RECARGO");
            cellRecargoLabel.setCellStyle(estiloHeader);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 2));

            Cell cellRecargoValor = rowRecargo.createCell(3);
            cellRecargoValor.setCellValue("+" + formatearNumeroDouble(factura.getTotalRecargo()));
            cellRecargoValor.setCellStyle(estiloMoneda);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 3, 4));
        }

        // Fila de retención si existe
        if (factura.getTotalRetencion() != null && factura.getTotalRetencion().compareTo(BigDecimal.ZERO) > 0) {
            Row rowRetencion = sheet.createRow(filaActual++);

            Cell cellRetLabel = rowRetencion.createCell(0);
            cellRetLabel.setCellValue("RETENCIÓN IRPF");
            cellRetLabel.setCellStyle(estiloHeader);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 2));

            Cell cellRetValor = rowRetencion.createCell(3);
            cellRetValor.setCellValue("-" + formatearNumeroDouble(factura.getTotalRetencion()));
            cellRetValor.setCellStyle(estiloMoneda);
            sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 3, 4));
        }

        // Fila de total final
        Row rowTotal = sheet.createRow(filaActual++);

        Cell cellTotalLabel = rowTotal.createCell(0);
        cellTotalLabel.setCellValue("TOTAL FACTURA");
        cellTotalLabel.setCellStyle(estiloTotal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual - 1, filaActual - 1, 0, 3));

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