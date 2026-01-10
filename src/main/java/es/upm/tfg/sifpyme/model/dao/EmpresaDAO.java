package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de empresas en la base de datos
 */
public class EmpresaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(EmpresaDAO.class);
    
    // Consultas SQL actualizadas (sin nombre_comercial)
    private static final String SQL_INSERT = 
        "INSERT INTO Empresa (razon_social, nif, direccion, " +
        "telefono, email, tipo_retencion_irpf, por_defecto) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = 
        "UPDATE Empresa SET razon_social = ?, nif = ?, " +
        "direccion = ?, telefono = ?, email = ?, tipo_retencion_irpf = ?, " +
        "por_defecto = ? WHERE id_empresa = ?";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM Empresa ORDER BY razon_social";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM Empresa WHERE id_empresa = ?";
    
    private static final String SQL_SELECT_POR_DEFECTO = 
        "SELECT * FROM Empresa WHERE por_defecto = TRUE LIMIT 1";
    
    private static final String SQL_DESACTIVAR_POR_DEFECTO = 
        "UPDATE Empresa SET por_defecto = FALSE WHERE por_defecto = TRUE";
    
    private static final String SQL_DELETE = 
        "DELETE FROM Empresa WHERE id_empresa = ?";
    
    private static final String SQL_EXISTS_NIF = 
        "SELECT COUNT(*) FROM Empresa WHERE nif = ? AND (? IS NULL OR id_empresa != ?)";
    
    /**
     * Inserta una nueva empresa en la base de datos
     */
    public Integer insertar(Empresa empresa) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, 
                     Statement.RETURN_GENERATED_KEYS)) {
            
            setEmpresaParameters(stmt, empresa);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        empresa.setIdEmpresa(id);
                        logger.info("Empresa insertada con ID: {}", id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al insertar empresa", e);
        }
        
        return null;
    }
    
    /**
     * Actualiza una empresa existente
     */
    public boolean actualizar(Empresa empresa) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            setEmpresaParameters(stmt, empresa);
            stmt.setInt(9, empresa.getIdEmpresa());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Empresa actualizada: {}", empresa.getIdEmpresa());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar empresa", e);
        }
        
        return false;
    }
    
    /**
     * Obtiene todas las empresas
     */
    public List<Empresa> obtenerTodas() {
        List<Empresa> empresas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                empresas.add(mapResultSetToEmpresa(rs));
            }
            
            logger.info("Se obtuvieron {} empresas", empresas.size());
            
        } catch (SQLException e) {
            logger.error("Error al obtener empresas", e);
        }
        
        return empresas;
    }
    
    /**
     * Obtiene una empresa por su ID
     */
    public Empresa obtenerPorId(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmpresa(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener empresa por ID", e);
        }
        
        return null;
    }
    
    /**
     * Obtiene la empresa configurada por defecto
     */
    public Empresa obtenerPorDefecto() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_POR_DEFECTO)) {
            
            if (rs.next()) {
                return mapResultSetToEmpresa(rs);
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener empresa por defecto", e);
        }
        
        return null;
    }
    
    /**
     * Desactiva todas las empresas marcadas como por defecto
     */
    public void desactivarEmpresasPorDefecto() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(SQL_DESACTIVAR_POR_DEFECTO);
            logger.info("Empresas por defecto desactivadas");
            
        } catch (SQLException e) {
            logger.error("Error al desactivar empresas por defecto", e);
        }
    }
    
    /**
     * Elimina una empresa
     */
    public boolean eliminar(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                logger.info("Empresa eliminada: {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar empresa", e);
        }
        
        return false;
    }
    
    /**
     * Verifica si existe una empresa con el NIF dado
     */
    public boolean existeNif(String nif, Integer excludeId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS_NIF)) {
            
            stmt.setString(1, nif);
            stmt.setObject(2, excludeId);
            stmt.setObject(3, excludeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar NIF de empresa", e);
        }
        
        return false;
    }
    
    /**
     * Establece los parámetros de un PreparedStatement con los datos de la empresa
     */
    private void setEmpresaParameters(PreparedStatement stmt, Empresa empresa) 
            throws SQLException {
        stmt.setString(1, empresa.getRazonSocial());
        stmt.setString(2, empresa.getNif());
        stmt.setString(3, empresa.getDireccion());
        stmt.setString(4, empresa.getTelefono());
        stmt.setString(5, empresa.getEmail());
        stmt.setBigDecimal(6, empresa.getTipoRetencionIrpf());
        stmt.setBoolean(7, empresa.getPorDefecto() != null ? empresa.getPorDefecto() : false);
    }
    
    /**
     * Mapea un ResultSet a un objeto Empresa
     */
    private Empresa mapResultSetToEmpresa(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa();
        
        empresa.setIdEmpresa(rs.getInt("id_empresa"));
        empresa.setRazonSocial(rs.getString("razon_social"));
        empresa.setNif(rs.getString("nif"));
        empresa.setDireccion(rs.getString("direccion"));
        empresa.setTelefono(rs.getString("telefono"));
        empresa.setEmail(rs.getString("email"));
        
        BigDecimal retencion = rs.getBigDecimal("tipo_retencion_irpf");
        empresa.setTipoRetencionIrpf(retencion != null ? retencion : new BigDecimal("15.00"));
        
        empresa.setPorDefecto(rs.getBoolean("por_defecto"));
        
        return empresa;
    }
}