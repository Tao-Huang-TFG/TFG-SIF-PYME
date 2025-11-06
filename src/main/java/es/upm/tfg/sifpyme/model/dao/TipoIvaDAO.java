package es.upm.tfg.sifpyme.model.dao;

import es.upm.tfg.sifpyme.model.entity.TipoIva;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de tipos de IVA en la base de datos
 */
public class TipoIvaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(TipoIvaDAO.class);
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM Tipo_IVA ORDER BY porcentaje DESC";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM Tipo_IVA WHERE id_tipo_iva = ?";
    
    /**
     * Obtiene todos los tipos de IVA
     */
    public List<TipoIva> obtenerTodos() {
        List<TipoIva> tipos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                tipos.add(mapResultSetToTipoIva(rs));
            }
            
            logger.info("Se obtuvieron {} tipos de IVA", tipos.size());
            
        } catch (SQLException e) {
            logger.error("Error al obtener tipos de IVA", e);
        }
        
        return tipos;
    }
    
    /**
     * Obtiene un tipo de IVA por su ID
     */
    public TipoIva obtenerPorId(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTipoIva(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener tipo de IVA por ID", e);
        }
        
        return null;
    }
    
    /**
     * Mapea un ResultSet a un objeto TipoIva
     */
    private TipoIva mapResultSetToTipoIva(ResultSet rs) throws SQLException {
        TipoIva tipoIva = new TipoIva();
        
        tipoIva.setIdTipoIva(rs.getInt("id_tipo_iva"));
        tipoIva.setNombre(rs.getString("nombre"));
        tipoIva.setPorcentaje(rs.getBigDecimal("porcentaje"));
        
        return tipoIva;
    }
}