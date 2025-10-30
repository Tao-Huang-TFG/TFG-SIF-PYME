package es.upm.tfg.sifpyme;

import es.upm.tfg.sifpyme.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== Aplicación de Facturación ===\n");
        
        try {
            // Probar conexión
            testDatabaseConnection();
            
            // Aquí iniciarás tu interfaz gráfica
            // SwingUtilities.invokeLater(() -> new MainView().setVisible(true));
            
        } catch (Exception e) {
            System.err.println("Error fatal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar al finalizar la aplicación
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseConnection.close();
            }));
        }
    }
    
    /**
     * Prueba la conexión y muestra algunos datos
     */
    private static void testDatabaseConnection() {
        System.out.println("Probando conexión a la base de datos...\n");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Verificar tipos de IVA
            ResultSet rs = stmt.executeQuery("SELECT * FROM Tipo_IVA");
            System.out.println("Tipos de IVA disponibles:");
            while (rs.next()) {
                System.out.printf("  - %s: %.2f%%\n", 
                    rs.getString("nombre"), 
                    rs.getDouble("porcentaje")
                );
            }
            rs.close();
            
            // Verificar empresas
            rs = stmt.executeQuery("SELECT * FROM Empresa");
            System.out.println("\nEmpresas registradas:");
            while (rs.next()) {
                System.out.printf("  - %s (NIF: %s)\n", 
                    rs.getString("nombre_comercial"), 
                    rs.getString("nif")
                );
            }
            rs.close();
            
            System.out.println("\n✓ Conexión exitosa!\n");
            
        } catch (Exception e) {
            System.err.println("✗ Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}