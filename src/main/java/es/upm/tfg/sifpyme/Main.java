package es.upm.tfg.sifpyme;

import es.upm.tfg.sifpyme.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("=== Aplicacion de Facturacion ===");
        
        try {
            // Probar conexión
            testDatabaseConnection();
            
            // Aquí iniciarás tu interfaz gráfica
            // SwingUtilities.invokeLater(() -> new MainView().setVisible(true));
            
        } catch (Exception e) {
            logger.error("Error fatal en la aplicacion", e);
        } finally {
            // Cerrar al finalizar la aplicación
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseConnection.close();
                try{
                    Thread.sleep(500);
                } catch (InterruptedException ignored){}
            }));
        }
    }
    
    /**
     * Prueba la conexión y muestra algunos datos
     */
    private static void testDatabaseConnection() {
        logger.info("Probando conexion a la base de datos...");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Verificar tipos de IVA
            ResultSet rs = stmt.executeQuery("SELECT * FROM Tipo_IVA");
            logger.info("Tipos de IVA disponibles:");
            while (rs.next()) {
                logger.info("  - {}: {}%", 
                    rs.getString("nombre"), 
                    String.format("%.2f", rs.getDouble("porcentaje"))
                );
            }
            rs.close();
            
            // Verificar empresas
            rs = stmt.executeQuery("SELECT * FROM Empresa");
            logger.info("Empresas registradas:");
            while (rs.next()) {
                logger.info("  - {} (NIF: {})", 
                    rs.getString("nombre_comercial"), 
                    rs.getString("nif")
                );
            }
            rs.close();
            
            logger.info("Conexion exitosa!");
            
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}