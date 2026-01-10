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

                // SOLO ejecutar script de creación de tablas (V1__create_tables.sql)
                executeCreateTablesScript(props);

                initialized = true;
                logger.info("Base de datos inicializada correctamente (solo tablas creadas)");

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
     * Ejecuta solo el script de creación de tablas
     */
    private static void executeCreateTablesScript(Properties props) {
        String createTablesScript = "database/V1__create_tables.sql";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = DatabaseConnection.class.getClassLoader()
                     .getResourceAsStream(createTablesScript);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                logger.warn("No se encontró el script de creación de tablas: {}", createTablesScript);
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
                            logger.debug("Ejecutado: {}", sql.substring(0, Math.min(80, sql.length())) + "...");
                        } catch (SQLException e) {
                            // Ignorar errores de "already exists" para tablas
                            if (!e.getMessage().toLowerCase().contains("already exists")) {
                                logger.warn("Error ejecutando creación de tabla: {}", 
                                        sql.substring(0, Math.min(50, sql.length())), e);
                            }
                        }
                    }
                    sqlBuilder.setLength(0);
                }
            }

            logger.info("Script de creación de tablas ejecutado: {}", createTablesScript);

        } catch (Exception e) {
            logger.error("Error al ejecutar script de creación de tablas: {}", createTablesScript, e);
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
                throw new IOException("No se encontró application.properties");
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