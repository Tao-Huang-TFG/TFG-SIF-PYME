package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.Cliente;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de clientes en la base de datos
 */
public class ClienteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteDAO.class);
    
    // Las consultas SQL ya están correctas para el nuevo esquema
    private static final String SQL_INSERT = 
        "INSERT INTO Cliente (nombre_fiscal, nif, direccion, telefono, email) " +
        "VALUES (?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = 
        "UPDATE Cliente SET nombre_fiscal = ?, nif = ?, direccion = ?, " +
        "telefono = ?, email = ? WHERE id_cliente = ?";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM Cliente ORDER BY nombre_fiscal";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM Cliente WHERE id_cliente = ?";
    
    private static final String SQL_SELECT_BY_NIF = 
        "SELECT * FROM Cliente WHERE nif = ?";
    
    private static final String SQL_DELETE = 
        "DELETE FROM Cliente WHERE id_cliente = ?";
    
    private static final String SQL_SEARCH = 
        "SELECT * FROM Cliente WHERE " +
        "LOWER(nombre_fiscal) LIKE LOWER(?) OR " +
        "LOWER(nif) LIKE LOWER(?) OR " +
        "LOWER(email) LIKE LOWER(?) " +
        "ORDER BY nombre_fiscal";
    
    private static final String SQL_COUNT = 
        "SELECT COUNT(*) FROM Cliente";
    
    /**
     * Inserta un nuevo cliente en la base de datos
     */
    public Integer insertar(Cliente cliente) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, 
                     Statement.RETURN_GENERATED_KEYS)) {
            
            setClienteParameters(stmt, cliente);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        cliente.setIdCliente(id);
                        logger.info("Cliente insertado con ID: {}", id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al insertar cliente", e);
        }
        
        return null;
    }
    
    /**
     * Actualiza un cliente existente
     */
    public boolean actualizar(Cliente cliente) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            setClienteParameters(stmt, cliente);
            stmt.setInt(6, cliente.getIdCliente());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Cliente actualizado: {}", cliente.getIdCliente());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar cliente", e);
        }
        
        return false;
    }
    
    /**
     * Obtiene todos los clientes
     */
    public List<Cliente> obtenerTodos() {
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
            logger.info("Se obtuvieron {} clientes", clientes.size());
            
        } catch (SQLException e) {
            logger.error("Error al obtener clientes", e);
        }
        
        return clientes;
    }
    
    /**
     * Obtiene un cliente por su ID
     */
    public Cliente obtenerPorId(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener cliente por ID", e);
        }
        
        return null;
    }
    
    /**
     * Obtiene un cliente por su NIF
     */
    public Cliente obtenerPorNif(String nif) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_NIF)) {
            
            stmt.setString(1, nif);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener cliente por NIF", e);
        }
        
        return null;
    }
    
    /**
     * Busca clientes por nombre, NIF o email
     */
    public List<Cliente> buscar(String termino) {
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SEARCH)) {
            
            String terminoBusqueda = "%" + termino + "%";
            stmt.setString(1, terminoBusqueda);
            stmt.setString(2, terminoBusqueda);
            stmt.setString(3, terminoBusqueda);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
            
            logger.info("Búsqueda '{}' encontró {} clientes", termino, clientes.size());
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes", e);
        }
        
        return clientes;
    }
    
    /**
     * Elimina un cliente
     */
    public boolean eliminar(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Cliente eliminado: {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar cliente", e);
        }
        
        return false;
    }
    
    /**
     * Cuenta el total de clientes registrados
     */
    public int contarClientes() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_COUNT)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Error al contar clientes", e);
        }
        
        return 0;
    }
    
    /**
     * Verifica si existe un cliente con el NIF dado (excluyendo un ID específico)
     */
    public boolean existeNif(String nif, Integer excludeId) {
        String sql = excludeId != null ? 
            "SELECT COUNT(*) FROM Cliente WHERE nif = ? AND id_cliente != ?" :
            "SELECT COUNT(*) FROM Cliente WHERE nif = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nif);
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar NIF duplicado", e);
        }
        
        return false;
    }
    
    /**
     * Establece los parámetros de un PreparedStatement con los datos del cliente
     */
    private void setClienteParameters(PreparedStatement stmt, Cliente cliente) 
            throws SQLException {
        stmt.setString(1, cliente.getNombreFiscal());
        stmt.setString(2, cliente.getNif());
        stmt.setString(3, cliente.getDireccion());
        stmt.setString(4, cliente.getTelefono());
        stmt.setString(5, cliente.getEmail());
    }
    
    /**
     * Mapea un ResultSet a un objeto Cliente
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombreFiscal(rs.getString("nombre_fiscal"));
        cliente.setNif(rs.getString("nif"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        
        return cliente;
    }
}