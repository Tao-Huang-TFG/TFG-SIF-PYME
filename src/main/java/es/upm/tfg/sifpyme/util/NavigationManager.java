// NavigationManager.java
package es.upm.tfg.sifpyme.util;

import javax.swing.*;
import java.util.Stack;

/**
 * Gestor de navegación entre vistas usando un stack
 */
public class NavigationManager {
    private static NavigationManager instance;
    private Stack<JFrame> viewStack;
    private JFrame currentView;

    private NavigationManager() {
        viewStack = new Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    /**
     * Navega a una nueva vista
     */
    public void navigateTo(JFrame newView) {
        if (currentView != null) {
            viewStack.push(currentView);
            currentView.setVisible(false);
        }
        
        currentView = newView;
        currentView.setVisible(true);
        centerView(currentView);
    }

    /**
     * Navega a una nueva vista cerrando la actual
     */
    public void navigateToAndCloseCurrent(JFrame newView) {
        if (currentView != null) {
            currentView.dispose();
        }
        
        currentView = newView;
        currentView.setVisible(true);
        centerView(currentView);
    }

    /**
     * Vuelve a la vista anterior
     */
    public void navigateBack() {
        if (!viewStack.isEmpty()) {
            if (currentView != null) {
                currentView.dispose();
            }
            
            currentView = viewStack.pop();
            currentView.setVisible(true);
            centerView(currentView);
        } else {
            // Si no hay vistas anteriores, preguntar si quiere salir
            int option = JOptionPane.showConfirmDialog(
                currentView,
                "¿Estás seguro de que quieres salir de la aplicación?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }

    /**
     * Vuelve al menú principal cerrando todas las vistas intermedias
     */
    public void navigateToMainMenu() {
        // Cerrar todas las vistas excepto el menú principal
        while (!viewStack.isEmpty()) {
            JFrame view = viewStack.pop();
            if (view instanceof es.upm.tfg.sifpyme.view.MainMenuView) {
                currentView = view;
                currentView.setVisible(true);
                centerView(currentView);
                return;
            } else {
                view.dispose();
            }
        }
        
        // Si no se encontró el menú principal, crear uno nuevo
        navigateToAndCloseCurrent(new es.upm.tfg.sifpyme.view.MainMenuView());
    }

    private void centerView(JFrame view) {
        view.setLocationRelativeTo(null);
    }

    public JFrame getCurrentView() {
        return currentView;
    }

    public boolean hasPreviousViews() {
        return !viewStack.isEmpty();
    }
}