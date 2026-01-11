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

    // Consultas SQL actualizadas según el nuevo esquema
    private static final String SQL_INSERT = "INSERT INTO Factura (id_factura, id_empresa, id_cliente, fecha_emision, "
            +
            "metodo_pago, subtotal, total_iva, total_retencion, total) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE = "UPDATE Factura SET id_empresa = ?, id_cliente = ?, fecha_emision = ?, " +
            "metodo_pago = ?, subtotal = ?, total_iva = ?, total_retencion = ?, " +
            "total = ? WHERE id_factura = ?";

    private static final String SQL_SELECT_ALL = "SELECT * FROM Factura ORDER BY fecha_emision DESC";

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM Factura WHERE id_factura = ?";

    private static final String SQL_DELETE = "DELETE FROM Factura WHERE id_factura = ?";

    private static final String SQL_SEARCH = "SELECT * FROM Factura WHERE " +
            "LOWER(id_factura) LIKE LOWER(?) OR " +
            "id_cliente IN (SELECT id_cliente FROM Cliente WHERE LOWER(nombre_fiscal) LIKE LOWER(?)) " +
            "ORDER BY fecha_emision DESC";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM Factura";

    private static final String SQL_NEXT_ID = "SELECT COALESCE(MAX(CAST(SUBSTRING(id_factura, 4) AS INT)), 0) + 1 " +
            "FROM Factura WHERE id_factura LIKE 'FAC%' AND SUBSTRING(id_factura, 4) ~ '^[0-9]+$'";

    private static final String SQL_EXISTS_ID = "SELECT COUNT(*) FROM Factura " +
            "WHERE id_factura = ? AND (? IS NULL OR id_factura != ?)";

    private static final String SQL_SELECT_BY_CLIENTE = "SELECT * FROM Factura WHERE id_cliente = ? ORDER BY fecha_emision DESC";

    private static final String SQL_SELECT_BY_EMPRESA = "SELECT * FROM Factura WHERE id_empresa = ? ORDER BY fecha_emision DESC";

    private static final String SQL_SELECT_BY_FECHA_RANGO = "SELECT * FROM Factura WHERE fecha_emision BETWEEN ? AND ? ORDER BY fecha_emision DESC";

    public FacturaDAO() {
        this.lineaFacturaDAO = new LineaFacturaDAO();
    }

    /**
     * Inserta una nueva factura con sus líneas en una transacción
     */
    public boolean insertar(Factura factura) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insertar factura
            if (insertarFactura(conn, factura)) {

                // Insertar líneas
                if (factura.getLineas() != null && !factura.getLineas().isEmpty()) {
                    for (LineaFactura linea : factura.getLineas()) {
                        linea.setIdFactura(factura.getIdFactura());
                        lineaFacturaDAO.insertar(conn, linea);
                    }
                }

                conn.commit();
                logger.info("Factura insertada: {}", factura.getIdFactura());
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            logger.error("Error al insertar factura", e);
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
     * Inserta solo la factura (sin líneas) en una conexión existente
     */
    private boolean insertarFactura(Connection conn, Factura factura) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            setFacturaParameters(stmt, factura);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
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
                setFacturaParametersUPDATE(stmt, factura);
                stmt.setString(9, factura.getIdFactura());
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
                Factura factura = mapResultSetToFactura(rs);
                cargarLineasFactura(factura);
                facturas.add(factura);
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
    public Factura obtenerPorId(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Factura factura = mapResultSetToFactura(rs);
                    cargarLineasFactura(factura);
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

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Factura factura = mapResultSetToFactura(rs);
                    cargarLineasFactura(factura);
                    facturas.add(factura);
                }
            }

            logger.info("Búsqueda '{}' encontró {} facturas", termino, facturas.size());

        } catch (SQLException e) {
            logger.error("Error al buscar facturas", e);
        }

        return facturas;
    }

    /**
     * Obtiene facturas por cliente
     */
    public List<Factura> obtenerPorCliente(Integer idCliente) {
        List<Factura> facturas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_CLIENTE)) {

            stmt.setInt(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Factura factura = mapResultSetToFactura(rs);
                    cargarLineasFactura(factura);
                    facturas.add(factura);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener facturas por cliente", e);
        }

        return facturas;
    }

    /**
     * Obtiene facturas por empresa
     */
    public List<Factura> obtenerPorEmpresa(Integer idEmpresa) {
        List<Factura> facturas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_EMPRESA)) {

            stmt.setInt(1, idEmpresa);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Factura factura = mapResultSetToFactura(rs);
                    cargarLineasFactura(factura);
                    facturas.add(factura);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener facturas por empresa", e);
        }

        return facturas;
    }

    /**
     * Obtiene facturas por rango de fechas
     */
    public List<Factura> obtenerPorRangoFechas(LocalDate desde, LocalDate hasta) {
        List<Factura> facturas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_FECHA_RANGO)) {

            stmt.setDate(1, Date.valueOf(desde));
            stmt.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Factura factura = mapResultSetToFactura(rs);
                    cargarLineasFactura(factura);
                    facturas.add(factura);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener facturas por rango de fechas", e);
        }

        return facturas;
    }

    /**
     * Elimina una factura y sus líneas (por CASCADE)
     */
    public boolean eliminar(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setString(1, id);
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
     * Genera el siguiente ID de factura
     */
    public String generarSiguienteId() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_NEXT_ID)) {

            if (rs.next()) {
                int siguiente = rs.getInt(1);
                return String.format("FAC%06d", siguiente);
            }

        } catch (SQLException e) {
            logger.error("Error al generar siguiente ID de factura", e);
        }

        return "FAC000001";
    }

    /**
     * Verifica si existe una factura con el ID dado
     */
    public boolean existeId(String id, String excludeId) {
        String sql = SQL_EXISTS_ID;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.setObject(2, excludeId);
            stmt.setObject(3, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar ID duplicado", e);
        }

        return false;
    }

    /**
     * Carga las líneas de una factura
     */
    private void cargarLineasFactura(Factura factura) {
        if (factura != null && factura.getIdFactura() != null) {
            List<LineaFactura> lineas = lineaFacturaDAO.obtenerPorFactura(factura.getIdFactura());
            factura.setLineas(lineas);
        }
    }

    /**
     * Establece los parámetros de un PreparedStatement con los datos de la factura
     */
    private void setFacturaParameters(PreparedStatement stmt, Factura factura)
            throws SQLException {
        stmt.setString(1, factura.getIdFactura());
        stmt.setInt(2, factura.getIdEmpresa());
        stmt.setInt(3, factura.getIdCliente());
        stmt.setDate(4, Date.valueOf(factura.getFechaEmision()));
        stmt.setString(5, factura.getMetodoPago());
        stmt.setBigDecimal(6, factura.getSubtotal());
        stmt.setBigDecimal(7, factura.getTotalIva());
        stmt.setBigDecimal(8, factura.getTotalRetencion());
        stmt.setBigDecimal(9, factura.getTotal());
    }

    private void setFacturaParametersUPDATE(PreparedStatement stmt, Factura factura)
            throws SQLException {

        stmt.setInt(1, factura.getIdEmpresa());
        stmt.setInt(2, factura.getIdCliente());
        stmt.setDate(3, Date.valueOf(factura.getFechaEmision()));
        stmt.setString(4, factura.getMetodoPago());
        stmt.setBigDecimal(5, factura.getSubtotal());
        stmt.setBigDecimal(6, factura.getTotalIva());
        stmt.setBigDecimal(7, factura.getTotalRetencion());
        stmt.setBigDecimal(8, factura.getTotal());

        // WHERE
        stmt.setString(9, factura.getIdFactura());
    }

    /**
     * Mapea un ResultSet a un objeto Factura
     */
    private Factura mapResultSetToFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();

        factura.setIdFactura(rs.getString("id_factura"));
        factura.setIdEmpresa(rs.getInt("id_empresa"));
        factura.setIdCliente(rs.getInt("id_cliente"));

        Date fecha = rs.getDate("fecha_emision");
        factura.setFechaEmision(fecha != null ? fecha.toLocalDate() : LocalDate.now());

        factura.setMetodoPago(rs.getString("metodo_pago"));
        factura.setSubtotal(rs.getBigDecimal("subtotal"));
        factura.setTotalIva(rs.getBigDecimal("total_iva"));
        factura.setTotalRetencion(rs.getBigDecimal("total_retencion"));
        factura.setTotal(rs.getBigDecimal("total"));

        return factura;
    }
}