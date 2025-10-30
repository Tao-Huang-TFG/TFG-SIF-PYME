package es.upm.tfg.sifpyme.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    private static boolean initialized = false;

    private DatabaseConnection() {
        // Constructor privado para patrón Singleton
    }

    /**
     * Inicializa el pool de conexiones
     */
    private static void initialize() {
        if (!initialized) {
            try {
                Properties props = loadProperties();

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(props.getProperty("db.url"));
                config.setUsername(props.getProperty("db.username"));
                config.setPassword(props.getProperty("db.password"));
                config.setDriverClassName(props.getProperty("db.driver"));

                // Configuración del pool
                config.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10"))
                );
                config.setMinimumIdle(
                    Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2"))
                );
                config.setConnectionTimeout(
                    Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000"))
                );

                dataSource = new HikariDataSource(config);

                // Verificar si se deben ejecutar los scripts
                if (shouldRunInitialScripts()) {
                    initializeDatabase(props);
                } else {
                    logger.info("Datos iniciales detectados: se omite la ejecucion de scripts.");
                }

                initialized = true;
                logger.info("Base de datos inicializada correctamente");

            } catch (Exception e) {
                logger.error("Error al inicializar la base de datos", e);
                throw new RuntimeException("Error al inicializar la base de datos", e);
            }
        }
    }

    /**
     * Obtiene una conexión del pool
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * Determina si deben ejecutarse los scripts iniciales (solo si la tabla está vacía)
     */
    private static boolean shouldRunInitialScripts() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Comprobamos si existen registros en la tabla Tipo_IVA
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TIPO_IVA'");
            if (rs.next() && rs.getInt(1) == 0) {
                logger.info("Tabla Tipo_IVA no existe. Se ejecutaran scripts iniciales.");
                return true;
            }

            rs = stmt.executeQuery("SELECT COUNT(*) FROM Tipo_IVA");
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                if (count == 0) {
                    logger.info("Tabla Tipo_IVA vacia. Se ejecutarán scripts iniciales.");
                    return true;
                } else {
                    logger.info("La tabla Tipo_IVA ya contiene datos ({} registros).", count);
                    return false;
                }
            }

        } catch (Exception e) {
            logger.warn("Error comprobando la existencia de datos iniciales. Se ejecutaran scripts por precaución.", e);
            return true;
        }
        return true;
    }

    /**
     * Ejecuta los scripts de inicialización
     */
    private static void initializeDatabase(Properties props) {
        String scriptsProperty = props.getProperty("db.init.scripts");
        if (scriptsProperty != null && !scriptsProperty.isEmpty()) {
            String[] scripts = scriptsProperty.split(",");

            for (String script : scripts) {
                script = script.trim();
                executeScript(script);
            }
        }
    }

    /**
     * Ejecuta un script SQL completo
     */
    private static void executeScript(String scriptPath) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = DatabaseConnection.class.getClassLoader()
                     .getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                logger.warn("No se encontro el script: {}", scriptPath);
                return;
            }

            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                sqlBuilder.append(line).append(" ");

                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    sql = sql.substring(0, sql.length() - 1);

                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            if (!e.getMessage().contains("already exists")) {
                                logger.warn("Error ejecutando sentencia SQL: {}...",
                                        sql.substring(0, Math.min(50, sql.length())), e);
                            }
                        }
                    }
                    sqlBuilder.setLength(0);
                }
            }

            logger.info("Script ejecutado: {}", scriptPath);

        } catch (Exception e) {
            logger.error("Error al ejecutar script: {}", scriptPath, e);
        }
    }

    /**
     * Carga las propiedades de configuración
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new IOException("No se encontro application.properties");
            }

            props.load(is);
            return props;
        }
    }

    /**
     * Cierra el pool de conexiones (llamar al cerrar la aplicación)
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de conexiones cerrado");
        }
    }

    /**
     * Verifica si la base de datos está inicializada
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
