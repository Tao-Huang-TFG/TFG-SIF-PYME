package es.upm.tfg.sifpyme;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import es.upm.tfg.sifpyme.util.DatabaseConnection;
import es.upm.tfg.sifpyme.view.EmpresaFormView;
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
            
            // Verificar si ya existe una empresa registrada
            EmpresaController empresaController = new EmpresaController();
            Empresa empresaExistente = empresaController.obtenerEmpresaPorDefecto();
            
            if (empresaExistente == null) {
                // Primera vez: mostrar formulario de registro
                logger.info("No se encontró empresa registrada. Mostrando formulario inicial...");
                SwingUtilities.invokeLater(() -> {
                    EmpresaFormView formView = new EmpresaFormView();
                    formView.setVisible(true);
                    
                    // Cuando se cierre el formulario de empresa, abrir el menú principal
                    formView.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            // Verificar si se guardó la empresa exitosamente
                            Empresa empresaGuardada = empresaController.obtenerEmpresaPorDefecto();
                            if (empresaGuardada != null) {
                                abrirMenuPrincipal(empresaGuardada);
                            }
                        }
                    });
                });
            } else {
                // Ya existe empresa: abrir menú principal directamente
                logger.info("Empresa encontrada: {}", empresaExistente.getNombreComercial());
                SwingUtilities.invokeLater(() -> {
                    abrirMenuPrincipal(empresaExistente);
                });
            }
            
        } catch (Exception e) {
            logger.error("Error fatal en la aplicación", e);
            JOptionPane.showMessageDialog(
                null,
                "Error al iniciar la aplicación:\n" + e.getMessage(),
                "Error Fatal",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        } finally {
            // Cerrar al finalizar la aplicación
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseConnection.close();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }));
        }
    }
    
    /**
     * Abre el menú principal de la aplicación
     */
    private static void abrirMenuPrincipal(Empresa empresa) {
        logger.info("Abriendo menú principal para empresa: {}", empresa.getNombreComercial());
        
        MainMenuView mainMenu = new MainMenuView();
        
        // Configurar los listeners del menú principal
        configurarListenersMenuPrincipal(mainMenu, empresa);
        
        mainMenu.setVisible(true);
    }
    
    /**
     * Configura los listeners para las acciones del menú principal
     */
    private static void configurarListenersMenuPrincipal(MainMenuView menu, Empresa empresaActual) {
        
        // Listener para el botón de Empresas
        menu.setEmpresasListener(e -> {
            // Abrir formulario de empresas en modo edición
            EmpresaFormView empresaForm = new EmpresaFormView(empresaActual);
            empresaForm.setVisible(true);
            
            // Cuando se cierre el formulario, actualizar la empresa actual si es necesario
            empresaForm.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    // Podrías actualizar la información en el menú principal aquí
                    logger.info("Formulario de empresa cerrado");
                }
            });
        });
        
        // Listener para el botón de Clientes (funcionalidad futura)
        menu.setClientesListener(e -> {
            menu.mostrarFuncionalidadNoDisponible("Gestión de Clientes");
            // Aquí irá: new ClientesView().setVisible(true);
        });
        
        // Listener para el botón de Productos (funcionalidad futura)
        menu.setProductosListener(e -> {
            menu.mostrarFuncionalidadNoDisponible("Gestión de Productos y Servicios");
            // Aquí irá: new ProductosView().setVisible(true);
        });
        
        // Listener para el botón de Facturas (funcionalidad futura)
        menu.setFacturasListener(e -> {
            menu.mostrarFuncionalidadNoDisponible("Gestión de Facturas");
            // Aquí irá: new FacturasView().setVisible(true);
        });
        
        // Listener para el botón de Configuración (funcionalidad futura)
        menu.setConfiguracionListener(e -> {
            menu.mostrarFuncionalidadNoDisponible("Configuración del Sistema");
            // Aquí irá: new ConfiguracionView().setVisible(true);
        });
        
        // Listener para el botón de Salir
        menu.setSalirListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(
                menu,
                "¿Estás seguro de que quieres salir de la aplicación?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                logger.info("Cerrando aplicación desde menú principal");
                System.exit(0);
            }
        });
    }
}