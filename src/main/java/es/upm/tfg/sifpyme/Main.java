package es.upm.tfg.sifpyme;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import es.upm.tfg.sifpyme.util.NavigationManager;
import es.upm.tfg.sifpyme.view.MainMenuView;
import es.upm.tfg.sifpyme.view.EmpresasView;

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

            // Verificar si ya existe una empresa registrada
            EmpresaController empresaController = new EmpresaController();
            Empresa empresaExistente = empresaController.obtenerEmpresaPorDefecto();

            NavigationManager navigationManager = NavigationManager.getInstance();

            if (empresaExistente == null) {
                // Primera vez: mostrar formulario de registro usando EmpresasView
                logger.info("No se encontró empresa registrada. Mostrando formulario inicial...");
                SwingUtilities.invokeLater(() -> {
                    // Crear la vista de empresas que manejará el formulario inicial
                    EmpresasView empresasView = new EmpresasView();
                    
                    // Forzar que se muestre el formulario de nueva empresa
                    empresasView.mostrarFormularioNuevo();
                    
                    // Modificar el formulario para que al guardar navegue al menú principal
                    empresasView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    
                    // Usar WindowListener para detectar cuando se cierra la ventana
                    empresasView.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            // Verificar si ahora hay una empresa registrada
                            Empresa empresaGuardada = empresaController.obtenerEmpresaPorDefecto();
                            if (empresaGuardada != null) {
                                logger.info("Empresa registrada: {}", empresaGuardada.getNombreComercial());
                                MainMenuView mainMenu = new MainMenuView();
                                navigationManager.navigateToAndCloseCurrent(mainMenu);
                            } else {
                                logger.info("No se registró ninguna empresa. Cerrando aplicación.");
                                System.exit(0);
                            }
                        }
                    });
                    
                    // Mostrar la vista de empresas
                    empresasView.setVisible(true);
                });
            } else {
                // Ya existe empresa: abrir menú principal directamente
                logger.info("Empresa encontrada: {}", empresaExistente.getNombreComercial());
                SwingUtilities.invokeLater(() -> {
                    MainMenuView mainMenu = new MainMenuView();
                    navigationManager.navigateToAndCloseCurrent(mainMenu);
                });
            }

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