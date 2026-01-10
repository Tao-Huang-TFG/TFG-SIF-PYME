package es.upm.tfg.sifpyme;

import es.upm.tfg.sifpyme.util.DatabaseConnection;
import es.upm.tfg.sifpyme.util.NavigationManager;
import es.upm.tfg.sifpyme.view.MainMenuView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("=== Aplicación de Facturación SifPyme ===");

        try {
            // Configurar Look and Feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Navegar directamente al menú principal
            logger.info("Iniciando aplicación, mostrando menú principal...");
            SwingUtilities.invokeLater(() -> {
                MainMenuView mainMenu = new MainMenuView();
                NavigationManager navigationManager = NavigationManager.getInstance();
                navigationManager.navigateToAndCloseCurrent(mainMenu);
            });

        } catch (Exception e) {
            logger.error("Error fatal en la aplicación", e);
            JOptionPane.showMessageDialog(
                    null,
                    "Error al iniciar la aplicación:\n" + e.getMessage(),
                    "Error Fatal",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } finally {
            // Cerrar al finalizar la aplicación
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseConnection.close();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }));
        }
    }
}