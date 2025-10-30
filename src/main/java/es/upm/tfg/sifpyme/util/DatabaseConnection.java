package es.upm.tfg.sifpyme.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    
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
                
                // Ejecutar scripts de inicialización
                initializeDatabase(props);
                
                initialized = true;
                System.out.println("✓ Base de datos inicializada correctamente");
                
            } catch (Exception e) {
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
                System.err.println("⚠ No se encontró el script: " + scriptPath);
                return;
            }
            
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Ignorar comentarios y líneas vacías
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                sqlBuilder.append(line).append(" ");
                
                // Si la línea termina en punto y coma, ejecutar la sentencia
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    // Remover el punto y coma final
                    sql = sql.substring(0, sql.length() - 1);
                    
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            // Ignorar errores de "tabla ya existe" pero mostrar otros
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println("⚠ Error ejecutando: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                                System.err.println("  " + e.getMessage());
                            }
                        }
                    }
                    
                    // Limpiar el builder para la siguiente sentencia
                    sqlBuilder.setLength(0);
                }
            }
            
            System.out.println("✓ Script ejecutado: " + scriptPath);
            
        } catch (Exception e) {
            System.err.println("✗ Error al ejecutar script " + scriptPath);
            e.printStackTrace();
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
            System.out.println("✓ Pool de conexiones cerrado");
        }
    }
    
    /**
     * Verifica si la base de datos está inicializada
     */
    public static boolean isInitialized() {
        return initialized;
    }
}