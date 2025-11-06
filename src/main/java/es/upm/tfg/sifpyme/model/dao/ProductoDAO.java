package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de productos en la base de datos
 */
public class ProductoDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoDAO.class);
    
    private static final String SQL_INSERT = 
        "INSERT INTO Producto (id_tipo_iva, codigo, nombre, descripcion, precio, precio_base, tipo_retencion) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = 
        "UPDATE Producto SET id_tipo_iva = ?, codigo = ?, nombre = ?, descripcion = ?, " +
        "precio = ?, precio_base = ?, tipo_retencion = ? WHERE id_producto = ?";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM Producto ORDER BY nombre";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM Producto WHERE id_producto = ?";
    
    private static final String SQL_SELECT_BY_CODIGO = 
        "SELECT * FROM Producto WHERE codigo = ?";
    
    private static final String SQL_DELETE = 
        "DELETE FROM Producto WHERE id_producto = ?";
    
    private static final String SQL_SEARCH = 
        "SELECT * FROM Producto WHERE " +
        "LOWER(nombre) LIKE LOWER(?) OR " +
        "LOWER(codigo) LIKE LOWER(?) OR " +
        "LOWER(descripcion) LIKE LOWER(?) " +
        "ORDER BY nombre";
    
    private static final String SQL_COUNT = 
        "SELECT COUNT(*) FROM Producto";
    
    /**
     * Inserta un nuevo producto en la base de datos
     */
    public Integer insertar(Producto producto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, 
                     Statement.RETURN_GENERATED_KEYS)) {
            
            setProductoParameters(stmt, producto);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        producto.setIdProducto(id);
                        logger.info("Producto insertado con ID: {}", id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al insertar producto", e);
        }
        
        return null;
    }
    
    /**
     * Actualiza un producto existente
     */
    public boolean actualizar(Producto producto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            setProductoParameters(stmt, producto);
            stmt.setInt(8, producto.getIdProducto());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Producto actualizado: {}", producto.getIdProducto());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar producto", e);
        }
        
        return false;
    }
    
    /**
     * Obtiene todos los productos
     */
    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
            
            logger.info("Se obtuvieron {} productos", productos.size());
            
        } catch (SQLException e) {
            logger.error("Error al obtener productos", e);
        }
        
        return productos;
    }
    
    /**
     * Obtiene un producto por su ID
     */
    public Producto obtenerPorId(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener producto por ID", e);
        }
        
        return null;
    }
    
    /**
     * Obtiene un producto por su código
     */
    public Producto obtenerPorCodigo(String codigo) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_CODIGO)) {
            
            stmt.setString(1, codigo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener producto por código", e);
        }
        
        return null;
    }
    
    /**
     * Busca productos por nombre, código o descripción
     */
    public List<Producto> buscar(String termino) {
        List<Producto> productos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SEARCH)) {
            
            String terminoBusqueda = "%" + termino + "%";
            stmt.setString(1, terminoBusqueda);
            stmt.setString(2, terminoBusqueda);
            stmt.setString(3, terminoBusqueda);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProducto(rs));
                }
            }
            
            logger.info("Búsqueda '{}' encontró {} productos", termino, productos.size());
            
        } catch (SQLException e) {
            logger.error("Error al buscar productos", e);
        }
        
        return productos;
    }
    
    /**
     * Elimina un producto
     */
    public boolean eliminar(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Producto eliminado: {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar producto", e);
        }
        
        return false;
    }
    
    /**
     * Cuenta el total de productos registrados
     */
    public int contarProductos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_COUNT)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Error al contar productos", e);
        }
        
        return 0;
    }
    
    /**
     * Verifica si existe un producto con el código dado (excluyendo un ID específico)
     */
    public boolean existeCodigo(String codigo, Integer excludeId) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        
        String sql = excludeId != null ? 
            "SELECT COUNT(*) FROM Producto WHERE codigo = ? AND id_producto != ?" :
            "SELECT COUNT(*) FROM Producto WHERE codigo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codigo);
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar código duplicado", e);
        }
        
        return false;
    }
    
    /**
     * Establece los parámetros de un PreparedStatement con los datos del producto
     */
    private void setProductoParameters(PreparedStatement stmt, Producto producto) 
            throws SQLException {
        stmt.setObject(1, producto.getIdTipoIva());
        stmt.setString(2, producto.getCodigo());
        stmt.setString(3, producto.getNombre());
        stmt.setString(4, producto.getDescripcion());
        stmt.setBigDecimal(5, producto.getPrecio());
        stmt.setBigDecimal(6, producto.getPrecioBase());
        stmt.setBigDecimal(7, producto.getTipoRetencion());
    }
    
    /**
     * Mapea un ResultSet a un objeto Producto
     */
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setIdTipoIva((Integer) rs.getObject("id_tipo_iva"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setPrecioBase(rs.getBigDecimal("precio_base"));
        producto.setTipoRetencion(rs.getBigDecimal("tipo_retencion"));
        
        return producto;
    }
}