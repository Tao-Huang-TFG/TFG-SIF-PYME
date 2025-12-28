package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de líneas de factura en la base de datos
 */
public class LineaFacturaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(LineaFacturaDAO.class);
    
    private static final String SQL_INSERT = 
        "INSERT INTO Linea_factura (id_factura, id_producto, cantidad, " +
        "precio_unitario, descuento, subtotal_linea, porcentaje_iva, " +
        "importe_iva, porcentaje_retencion, importe_retencion, total_linea, " +
        "numero_linea) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_SELECT_BY_FACTURA = 
        "SELECT * FROM Linea_factura WHERE id_factura = ? ORDER BY numero_linea";
    
    private static final String SQL_DELETE_BY_FACTURA = 
        "DELETE FROM Linea_factura WHERE id_factura = ?";
    
    /**
     * Inserta una línea de factura en una conexión existente (para transacciones)
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
     * Obtiene todas las líneas de una factura
     */
    public List<LineaFactura> obtenerPorFactura(Integer idFactura) {
        List<LineaFactura> lineas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_FACTURA)) {
            
            stmt.setInt(1, idFactura);
            
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
     * Elimina todas las líneas de una factura en una conexión existente
     */
    public void eliminarPorFactura(Connection conn, Integer idFactura) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_FACTURA)) {
            stmt.setInt(1, idFactura);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Establece los parámetros de un PreparedStatement con los datos de la línea
     */
    private void setLineaParameters(PreparedStatement stmt, LineaFactura linea) 
            throws SQLException {
        stmt.setInt(1, linea.getIdFactura());
        stmt.setInt(2, linea.getIdProducto());
        stmt.setBigDecimal(3, linea.getCantidad());
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
        linea.setIdFactura(rs.getInt("id_factura"));
        linea.setIdProducto(rs.getInt("id_producto"));
        linea.setCantidad(rs.getBigDecimal("cantidad"));
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