package es.upm.tfg.sifpyme.view;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * CardLayout personalizado que mantiene el tamaño de los componentes
 */
public class ResizableCardLayout extends CardLayout {
    
    private Map<Component, Dimension> preferredSizes = new HashMap<>();
    private Dimension currentSize = null;
    
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        super.addLayoutComponent(comp, constraints);
        if (currentSize != null) {
            comp.setPreferredSize(currentSize);
        }
    }
    
    @Override
    public void layoutContainer(Container parent) {
        // Guardar el tamaño actual
        currentSize = parent.getSize();
        
        // Actualizar el tamaño preferido de todos los componentes
        for (Component comp : parent.getComponents()) {
            comp.setPreferredSize(currentSize);
            preferredSizes.put(comp, currentSize);
        }
        
        super.layoutContainer(parent);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        // Si hay un tamaño guardado, usarlo
        if (currentSize != null) {
            return currentSize;
        }
        return super.preferredLayoutSize(parent);
    }
}