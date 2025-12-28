package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.Factura;
import es.upm.tfg.sifpyme.model.entity.LineaFactura;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de facturas en la base de datos
 */
public class FacturaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(FacturaDAO.class);
    private final LineaFacturaDAO lineaFacturaDAO;
    
    private static final String SQL_INSERT = 
        "INSERT INTO Factura (id_empresa, id_cliente, numero_factura, serie, " +
        "fecha_emision, metodo_pago, subtotal, total_iva, total_retencion, " +
        "total, observaciones, estado) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = 
        "UPDATE Factura SET id_empresa = ?, id_cliente = ?, numero_factura = ?, " +
        "serie = ?, fecha_emision = ?, metodo_pago = ?, subtotal = ?, " +
        "total_iva = ?, total_retencion = ?, total = ?, observaciones = ?, " +
        "estado = ? WHERE id_factura = ?";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM Factura ORDER BY fecha_emision DESC, numero_factura DESC";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM Factura WHERE id_factura = ?";
    
    private static final String SQL_DELETE = 
        "DELETE FROM Factura WHERE id_factura = ?";
    
    private static final String SQL_SEARCH = 
        "SELECT * FROM Factura WHERE " +
        "LOWER(numero_factura) LIKE LOWER(?) OR " +
        "LOWER(serie) LIKE LOWER(?) OR " +
        "LOWER(estado) LIKE LOWER(?) " +
        "ORDER BY fecha_emision DESC";
    
    private static final String SQL_COUNT = 
        "SELECT COUNT(*) FROM Factura";
    
    private static final String SQL_NEXT_NUMBER = 
        "SELECT COALESCE(MAX(CAST(numero_factura AS INT)), 0) + 1 " +
        "FROM Factura WHERE id_empresa = ? AND serie = ? " +
        "AND numero_factura ~ '^[0-9]+$'";
    
    private static final String SQL_EXISTS_NUMBER = 
        "SELECT COUNT(*) FROM Factura " +
        "WHERE id_empresa = ? AND serie = ? AND numero_factura = ? " +
        "AND (? IS NULL OR id_factura != ?)";
    
    public FacturaDAO() {
        this.lineaFacturaDAO = new LineaFacturaDAO();
    }
    
    /**
     * Inserta una nueva factura con sus líneas en una transacción
     */
    public Integer insertar(Factura factura) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insertar factura
            Integer idFactura = insertarFactura(conn, factura);
            
            if (idFactura != null) {
                factura.setIdFactura(idFactura);
                
                // Insertar líneas
                if (factura.getLineas() != null && !factura.getLineas().isEmpty()) {
                    for (LineaFactura linea : factura.getLineas()) {
                        linea.setIdFactura(idFactura);
                        lineaFacturaDAO.insertar(conn, linea);
                    }
                }
                
                conn.commit();
                logger.info("Factura insertada con ID: {}", idFactura);
                return idFactura;
            }
            
            conn.rollback();
            return null;
            
        } catch (SQLException e) {
            logger.error("Error al insertar factura", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error al hacer rollback", ex);
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Inserta solo la factura (sin líneas) en una conexión existente
     */
    private Integer insertarFactura(Connection conn, Factura factura) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, 
                Statement.RETURN_GENERATED_KEYS)) {
            
            setFacturaParameters(stmt, factura);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Actualiza una factura existente con sus líneas en una transacción
     */
    public boolean actualizar(Factura factura) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Actualizar factura
            try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
                setFacturaParameters(stmt, factura);
                stmt.setInt(13, factura.getIdFactura());
                stmt.executeUpdate();
            }
            
            // Eliminar líneas existentes y reinsertar
            lineaFacturaDAO.eliminarPorFactura(conn, factura.getIdFactura());
            
            if (factura.getLineas() != null && !factura.getLineas().isEmpty()) {
                for (LineaFactura linea : factura.getLineas()) {
                    linea.setIdFactura(factura.getIdFactura());
                    lineaFacturaDAO.insertar(conn, linea);
                }
            }
            
            conn.commit();
            logger.info("Factura actualizada: {}", factura.getIdFactura());
            return true;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar factura", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error al hacer rollback", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Obtiene todas las facturas
     */
    public List<Factura> obtenerTodas() {
        List<Factura> facturas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                facturas.add(mapResultSetToFactura(rs));
            }
            
            logger.info("Se obtuvieron {} facturas", facturas.size());
            
        } catch (SQLException e) {
            logger.error("Error al obtener facturas", e);
        }
        
        return facturas;
    }
    
    /**
     * Obtiene una factura por su ID, incluyendo sus líneas
     */
    public Factura obtenerPorId(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Factura factura = mapResultSetToFactura(rs);
                    
                    // Cargar líneas
                    List<LineaFactura> lineas = lineaFacturaDAO.obtenerPorFactura(id);
                    factura.setLineas(lineas);
                    
                    return factura;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener factura por ID", e);
        }
        
        return null;
    }
    
    /**
     * Busca facturas por término de búsqueda
     */
    public List<Factura> buscar(String termino) {
        List<Factura> facturas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SEARCH)) {
            
            String terminoBusqueda = "%" + termino + "%";
            stmt.setString(1, terminoBusqueda);
            stmt.setString(2, terminoBusqueda);
            stmt.setString(3, terminoBusqueda);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    facturas.add(mapResultSetToFactura(rs));
                }
            }
            
            logger.info("Búsqueda '{}' encontró {} facturas", termino, facturas.size());
            
        } catch (SQLException e) {
            logger.error("Error al buscar facturas", e);
        }
        
        return facturas;
    }
    
    /**
     * Elimina una factura y sus líneas (por CASCADE)
     */
    public boolean eliminar(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Factura eliminada: {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar factura", e);
        }
        
        return false;
    }
    
    /**
     * Cuenta el total de facturas registradas
     */
    public int contarFacturas() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_COUNT)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Error al contar facturas", e);
        }
        
        return 0;
    }
    
    /**
     * Obtiene el siguiente número de factura para una empresa y serie
     */
    public String obtenerSiguienteNumero(Integer idEmpresa, String serie) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_NEXT_NUMBER)) {
            
            stmt.setInt(1, idEmpresa);
            stmt.setString(2, serie);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int siguiente = rs.getInt(1);
                    return String.format("%06d", siguiente);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener siguiente número", e);
        }
        
        return "000001";
    }
    
    /**
     * Verifica si existe una factura con el número dado
     */
    public boolean existeNumero(Integer idEmpresa, String serie, 
                               String numero, Integer excludeId) {
        String sql = SQL_EXISTS_NUMBER;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idEmpresa);
            stmt.setString(2, serie);
            stmt.setString(3, numero);
            stmt.setObject(4, excludeId);
            stmt.setObject(5, excludeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar número duplicado", e);
        }
        
        return false;
    }
    
    /**
     * Establece los parámetros de un PreparedStatement con los datos de la factura
     */
    private void setFacturaParameters(PreparedStatement stmt, Factura factura) 
            throws SQLException {
        stmt.setInt(1, factura.getIdEmpresa());
        stmt.setInt(2, factura.getIdCliente());
        stmt.setString(3, factura.getNumeroFactura());
        stmt.setString(4, factura.getSerie());
        stmt.setDate(5, Date.valueOf(factura.getFechaEmision()));
        stmt.setString(6, factura.getMetodoPago());
        stmt.setBigDecimal(7, factura.getSubtotal());
        stmt.setBigDecimal(8, factura.getTotalIva());
        stmt.setBigDecimal(9, factura.getTotalRetencion());
        stmt.setBigDecimal(10, factura.getTotal());
        stmt.setString(11, factura.getObservaciones());
        stmt.setString(12, factura.getEstado());
    }
    
    /**
     * Mapea un ResultSet a un objeto Factura
     */
    private Factura mapResultSetToFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        
        factura.setIdFactura(rs.getInt("id_factura"));
        factura.setIdEmpresa(rs.getInt("id_empresa"));
        factura.setIdCliente(rs.getInt("id_cliente"));
        factura.setNumeroFactura(rs.getString("numero_factura"));
        factura.setSerie(rs.getString("serie"));
        
        Date fecha = rs.getDate("fecha_emision");
        factura.setFechaEmision(fecha != null ? fecha.toLocalDate() : LocalDate.now());
        
        factura.setMetodoPago(rs.getString("metodo_pago"));
        factura.setSubtotal(rs.getBigDecimal("subtotal"));
        factura.setTotalIva(rs.getBigDecimal("total_iva"));
        factura.setTotalRetencion(rs.getBigDecimal("total_retencion"));
        factura.setTotal(rs.getBigDecimal("total"));
        factura.setObservaciones(rs.getString("observaciones"));
        factura.setEstado(rs.getString("estado"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            factura.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return factura;
    }
}