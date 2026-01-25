package es.upm.tfg.sifpyme.service;

import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Servicio para realizar cálculos relacionados con facturas y líneas de
 * factura.
 * Separa la lógica de cálculo de las entidades del modelo.
 */
public class CalculadoraFacturaService {

    private static final int ESCALA_DECIMAL = 2;
    private static final BigDecimal CIEN = new BigDecimal("100");

    /**
     * Calcula todos los importes de una línea de factura.
     * 
     * @param linea La línea de factura a calcular
     * @throws IllegalArgumentException si cantidad o precio unitario son nulos
     */
    public void calcularImportesLinea(LineaFactura linea) {
        if (linea.getCantidad() == null || linea.getPrecioBase() == null) {
            throw new IllegalArgumentException(
                    "La cantidad y el precio unitario no pueden ser nulos");
        }

        // Calcular subtotal con descuento aplicado
        BigDecimal subtotal = calcularSubtotalConDescuento(
                linea.getCantidad(),
                linea.getPrecioBase(),
                linea.getDescuento());
        linea.setSubtotalLinea(subtotal);

        // Calcular IVA
        BigDecimal importeIva = calcularImporteIva(
                subtotal,
                linea.getPorcentajeIva());
        linea.setImporteIva(importeIva);

        // Calcular retención
        BigDecimal importeRetencion = calcularImporteRetencion(
                subtotal,
                linea.getPorcentajeRetencion());
        linea.setImporteRetencion(importeRetencion);

        // CALCULAR RECARGO (NUEVO)
        BigDecimal importeRecargo = calcularImporteRecargo(
                subtotal,
                linea.getPorcentajeRecargo());
        linea.setImporteRecargo(importeRecargo);

        // Calcular total de la línea (ahora incluyendo recargo)
        BigDecimal totalLinea = calcularTotalLinea(
                subtotal,
                importeIva,
                importeRetencion,
                importeRecargo // Añadido
        );
        linea.setTotalLinea(totalLinea);
    }

    /**
     * Calcula el subtotal de una línea aplicando el descuento si existe.
     * 
     * @param cantidad   Cantidad de productos/servicios
     * @param precioBase PrecioBase por unidad
     * @param descuento  Porcentaje de descuento (puede ser null o 0)
     * @return Subtotal con descuento aplicado
     */
    public BigDecimal calcularSubtotalConDescuento(
            BigDecimal cantidad,
            BigDecimal precioBase,
            BigDecimal descuento) {

        // Base = cantidad * precio
        BigDecimal base = cantidad.multiply(precioBase);

        // Aplicar descuento si existe y es mayor que 0
        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal importeDescuento = base
                    .multiply(descuento)
                    .divide(CIEN, ESCALA_DECIMAL, RoundingMode.HALF_UP);
            base = base.subtract(importeDescuento);
        }

        return base.setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el importe del IVA sobre una base imponible.
     * 
     * @param baseImponible Base sobre la que calcular el IVA
     * @param porcentajeIva Porcentaje de IVA a aplicar
     * @return Importe del IVA
     */
    public BigDecimal calcularImporteIva(
            BigDecimal baseImponible,
            BigDecimal porcentajeIva) {

        if (porcentajeIva == null) {
            return BigDecimal.ZERO;
        }

        return baseImponible
                .multiply(porcentajeIva)
                .divide(CIEN, ESCALA_DECIMAL, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el importe de la retención sobre una base imponible.
     * 
     * @param baseImponible       Base sobre la que calcular la retención
     * @param porcentajeRetencion Porcentaje de retención a aplicar
     * @return Importe de la retención
     */
    public BigDecimal calcularImporteRetencion(
            BigDecimal baseImponible,
            BigDecimal porcentajeRetencion) {

        if (porcentajeRetencion == null ||
                porcentajeRetencion.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return baseImponible
                .multiply(porcentajeRetencion)
                .divide(CIEN, ESCALA_DECIMAL, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el importe del recargo sobre una base imponible.
     * (NUEVO MÉTODO)
     * 
     * @param baseImponible     Base sobre la que calcular el recargo
     * @param porcentajeRecargo Porcentaje de recargo a aplicar
     * @return Importe del recargo
     */
    public BigDecimal calcularImporteRecargo(
            BigDecimal baseImponible,
            BigDecimal porcentajeRecargo) {

        if (porcentajeRecargo == null ||
                porcentajeRecargo.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return baseImponible
                .multiply(porcentajeRecargo)
                .divide(CIEN, ESCALA_DECIMAL, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total de una línea: subtotal + IVA - retención + recargo.
     * (MODIFICADO para incluir recargo)
     * 
     * @param subtotal         Subtotal de la línea
     * @param importeIva       Importe del IVA
     * @param importeRetencion Importe de la retención
     * @param importeRecargo   Importe del recargo
     * @return Total de la línea
     */
    public BigDecimal calcularTotalLinea(
            BigDecimal subtotal,
            BigDecimal importeIva,
            BigDecimal importeRetencion,
            BigDecimal importeRecargo) {

        return subtotal
                .add(importeIva)
                .subtract(importeRetencion)
                .add(importeRecargo) // Añadido
                .setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP);
    }

    /**
     * Calcula y actualiza los totales de una factura basándose en sus líneas.
     * 
     * @param factura La factura a calcular
     */
    public void calcularTotalesFactura(Factura factura) {
        List<LineaFactura> lineas = factura.getLineas();

        if (lineas == null || lineas.isEmpty()) {
            factura.setSubtotal(BigDecimal.ZERO);
            factura.setTotalIva(BigDecimal.ZERO);
            factura.setTotalRetencion(BigDecimal.ZERO);
            factura.setTotalRecargo(BigDecimal.ZERO); // NUEVO
            factura.setTotal(BigDecimal.ZERO);
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal totalRetencion = BigDecimal.ZERO;
        BigDecimal totalRecargo = BigDecimal.ZERO; // NUEVO

        // Sumar los importes de todas las líneas
        for (LineaFactura linea : lineas) {
            subtotal = subtotal.add(
                    linea.getSubtotalLinea() != null ? linea.getSubtotalLinea() : BigDecimal.ZERO);
            totalIva = totalIva.add(
                    linea.getImporteIva() != null ? linea.getImporteIva() : BigDecimal.ZERO);
            totalRetencion = totalRetencion.add(
                    linea.getImporteRetencion() != null ? linea.getImporteRetencion() : BigDecimal.ZERO);
            totalRecargo = totalRecargo.add( // NUEVO
                    linea.getImporteRecargo() != null ? linea.getImporteRecargo() : BigDecimal.ZERO);
        }

        // Actualizar la factura
        factura.setSubtotal(subtotal.setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP));
        factura.setTotalIva(totalIva.setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP));
        factura.setTotalRetencion(totalRetencion.setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP));
        factura.setTotalRecargo(totalRecargo.setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP)); // NUEVO

        BigDecimal total = subtotal
                .add(totalIva)
                .subtract(totalRetencion)
                .add(totalRecargo) // Añadido
                .setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP);
        factura.setTotal(total);
    }

    /**
     * Recalcula una factura completa: todas sus líneas y sus totales.
     * 
     * @param factura La factura a recalcular
     */
    public void recalcularFacturaCompleta(Factura factura) {
        if (factura.getLineas() != null) {
            for (LineaFactura linea : factura.getLineas()) {
                calcularImportesLinea(linea);
            }
        }
        calcularTotalesFactura(factura);
    }
}