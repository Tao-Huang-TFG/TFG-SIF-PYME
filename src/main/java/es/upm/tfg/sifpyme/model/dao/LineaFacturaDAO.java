package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de líneas de factura en la base de datos
 */
public class LineaFacturaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(LineaFacturaDAO.class);
    
    // Consultas SQL actualizadas según el nuevo esquema
    private static final String SQL_INSERT = 
        "INSERT INTO Linea_factura (id_factura, cantidad, precio_base, " +
        "precio_unitario, descuento, subtotal_linea, porcentaje_iva, " +
        "importe_iva, porcentaje_retencion, importe_retencion, total_linea, " +
        "numero_linea) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_SELECT_BY_FACTURA = 
        "SELECT * FROM Linea_factura WHERE id_factura = ? ORDER BY numero_linea";
    
    private static final String SQL_DELETE_BY_FACTURA = 
        "DELETE FROM Linea_factura WHERE id_factura = ?";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM Linea_factura WHERE id_linea = ?";
    
    private static final String SQL_UPDATE = 
        "UPDATE Linea_factura SET cantidad = ?, precio_base = ?, precio_unitario = ?, " +
        "descuento = ?, subtotal_linea = ?, porcentaje_iva = ?, importe_iva = ?, " +
        "porcentaje_retencion = ?, importe_retencion = ?, total_linea = ?, " +
        "numero_linea = ? WHERE id_linea = ?";
    
    private static final String SQL_DELETE = 
        "DELETE FROM Linea_factura WHERE id_linea = ?";
    
    /**
     * Inserta una línea de factura en una conexión existente
     */
    public Integer insertar(Connection conn, LineaFactura linea) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, 
                Statement.RETURN_GENERATED_KEYS)) {
            
            setLineaParameters(stmt, linea);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        linea.setIdLinea(id);
                        return id;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Inserta una línea de factura (nueva conexión)
     */
    public Integer insertar(LineaFactura linea) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, 
                     Statement.RETURN_GENERATED_KEYS)) {
            
            setLineaParameters(stmt, linea);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        linea.setIdLinea(id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al insertar línea de factura", e);
        }
        
        return null;
    }
    
    /**
     * Actualiza una línea de factura
     */
    public boolean actualizar(LineaFactura linea) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            setLineaParameters(stmt, linea);
            stmt.setInt(12, linea.getIdLinea());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar línea de factura", e);
        }
        
        return false;
    }
    
    /**
     * Obtiene una línea de factura por su ID
     */
    public LineaFactura obtenerPorId(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLinea(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener línea de factura por ID", e);
        }
        
        return null;
    }
    
    /**
     * Obtiene todas las líneas de una factura
     */
    public List<LineaFactura> obtenerPorFactura(String idFactura) {
        List<LineaFactura> lineas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_FACTURA)) {
            
            stmt.setString(1, idFactura);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lineas.add(mapResultSetToLinea(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener líneas de factura", e);
        }
        
        return lineas;
    }
    
    /**
     * Elimina una línea de factura por su ID
     */
    public boolean eliminar(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar línea de factura", e);
        }
        
        return false;
    }
    
    /**
     * Elimina todas las líneas de una factura en una conexión existente
     */
    public void eliminarPorFactura(Connection conn, String idFactura) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_FACTURA)) {
            stmt.setString(1, idFactura);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Calcula los totales de una línea de factura
     */
    public void calcularTotales(LineaFactura linea) {
        if (linea == null) return;
        
        BigDecimal cantidad = linea.getCantidad();
        BigDecimal precioUnitario = linea.getPrecioUnitario();
        BigDecimal descuento = linea.getDescuento() != null ? linea.getDescuento() : BigDecimal.ZERO;
        BigDecimal porcentajeIva = linea.getPorcentajeIva() != null ? linea.getPorcentajeIva() : BigDecimal.ZERO;
        BigDecimal porcentajeRetencion = linea.getPorcentajeRetencion() != null ? linea.getPorcentajeRetencion() : BigDecimal.ZERO;
        
        if (cantidad != null && precioUnitario != null) {
            // Subtotal antes de descuento
            BigDecimal subtotalSinDescuento = cantidad.multiply(precioUnitario);
            
            // Calcular descuento
            BigDecimal descuentoAplicado = subtotalSinDescuento
                .multiply(descuento)
                .divide(new BigDecimal("100"));
            
            // Subtotal línea
            BigDecimal subtotalLinea = subtotalSinDescuento.subtract(descuentoAplicado);
            linea.setSubtotalLinea(subtotalLinea);
            
            // Calcular IVA
            BigDecimal importeIva = subtotalLinea
                .multiply(porcentajeIva)
                .divide(new BigDecimal("100"));
            linea.setImporteIva(importeIva);
            
            // Calcular retención
            BigDecimal importeRetencion = subtotalLinea
                .multiply(porcentajeRetencion)
                .divide(new BigDecimal("100"));
            linea.setImporteRetencion(importeRetencion);
            
            // Calcular total línea
            BigDecimal totalLinea = subtotalLinea.add(importeIva).subtract(importeRetencion);
            linea.setTotalLinea(totalLinea);
        }
    }
    
    /**
     * Establece los parámetros de un PreparedStatement con los datos de la línea
     */
    private void setLineaParameters(PreparedStatement stmt, LineaFactura linea) 
            throws SQLException {
        stmt.setString(1, linea.getIdFactura());
        stmt.setBigDecimal(2, linea.getCantidad());
        stmt.setBigDecimal(3, linea.getPrecioBase());
        stmt.setBigDecimal(4, linea.getPrecioUnitario());
        stmt.setBigDecimal(5, linea.getDescuento());
        stmt.setBigDecimal(6, linea.getSubtotalLinea());
        stmt.setBigDecimal(7, linea.getPorcentajeIva());
        stmt.setBigDecimal(8, linea.getImporteIva());
        stmt.setBigDecimal(9, linea.getPorcentajeRetencion());
        stmt.setBigDecimal(10, linea.getImporteRetencion());
        stmt.setBigDecimal(11, linea.getTotalLinea());
        stmt.setInt(12, linea.getNumeroLinea());
    }
    
    /**
     * Mapea un ResultSet a un objeto LineaFactura
     */
    private LineaFactura mapResultSetToLinea(ResultSet rs) throws SQLException {
        LineaFactura linea = new LineaFactura();
        
        linea.setIdLinea(rs.getInt("id_linea"));
        linea.setIdFactura(rs.getString("id_factura"));
        linea.setCantidad(rs.getBigDecimal("cantidad"));
        linea.setPrecioBase(rs.getBigDecimal("precio_base"));
        linea.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        linea.setDescuento(rs.getBigDecimal("descuento"));
        linea.setSubtotalLinea(rs.getBigDecimal("subtotal_linea"));
        linea.setPorcentajeIva(rs.getBigDecimal("porcentaje_iva"));
        linea.setImporteIva(rs.getBigDecimal("importe_iva"));
        linea.setPorcentajeRetencion(rs.getBigDecimal("porcentaje_retencion"));
        linea.setImporteRetencion(rs.getBigDecimal("importe_retencion"));
        linea.setTotalLinea(rs.getBigDecimal("total_linea"));
        linea.setNumeroLinea(rs.getInt("numero_linea"));
        
        return linea;
    }
}