package es.upm.tfg.sifpyme.view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Clase de utilidad para crear componentes de UI consistentes
 * Siguiendo el tema definido en UITheme
 */
public class UIHelper {
    
    // ==================== MÉTODOS PARA COMPONENTES ====================
    
    /**
     * Crea un botón con estilo consistente y icono Unicode
     */
    public static JButton crearBoton(String texto, Color color, String iconoUnicode) {
        JButton boton = new JButton();
        
        // Configurar layout para icono + texto
        boton.setLayout(new BorderLayout(5, 5));
        boton.setBackground(color);
        boton.setForeground(color);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Crear contenido del botón
        JPanel contenido = new JPanel(new BorderLayout(8, 0));
        contenido.setOpaque(false);
        
        // Añadir icono si está presente
        if (iconoUnicode != null && !iconoUnicode.isEmpty()) {
            JLabel lblIcono = new JLabel(iconoUnicode);
            lblIcono.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
            lblIcono.setVerticalAlignment(SwingConstants.CENTER);
            contenido.add(lblIcono, BorderLayout.WEST);
        }
        
        // Añadir texto
        if (texto != null && !texto.isEmpty()) {
            JLabel lblTexto = new JLabel(texto);
            lblTexto.setFont(UITheme.FUENTE_BOTON);
            lblTexto.setForeground(color.darker().darker());
            lblTexto.setVerticalAlignment(SwingConstants.CENTER);
            contenido.add(lblTexto, BorderLayout.CENTER);
        }
        
        boton.add(contenido, BorderLayout.CENTER);
        
        // Configurar efecto hover
        configurarEfectoHover(boton, color);
        
        return boton;
    }
    
    /**
     * Crea un botón de acción común (guardar, editar, eliminar, etc.)
     */
    public static JButton crearBotonAccion(String tipoAccion, String texto) {
        Color color;
        String icono;
        
        switch (tipoAccion.toLowerCase()) {
            case "guardar":
            case "nuevo":
            case "agregar":
                color = UITheme.COLOR_EXITO;
                icono = UITheme.ICONO_GUARDAR;
                texto = texto != null ? texto : "Guardar";
                break;
                
            case "editar":
            case "actualizar":
                color = UITheme.COLOR_INFO;
                icono = UITheme.ICONO_EDITAR;
                texto = texto != null ? texto : "Editar";
                break;
                
            case "eliminar":
            case "borrar":
                color = UITheme.COLOR_PELIGRO;
                icono = UITheme.ICONO_ELIMINAR;
                texto = texto != null ? texto : "Eliminar";
                break;
                
            case "cancelar":
                color = UITheme.COLOR_PELIGRO;
                icono = UITheme.ICONO_CANCELAR;
                texto = texto != null ? texto : "Cancelar";
                break;
                
            case "volver":
                color = UITheme.COLOR_VOLVER;
                icono = UITheme.ICONO_VOLVER;
                texto = texto != null ? texto : "Volver";
                break;
                
            case "buscar":
                color = UITheme.COLOR_INFO;
                icono = UITheme.ICONO_BUSCAR;
                texto = texto != null ? texto : "Buscar";
                break;
                
            case "ver":
                color = UITheme.COLOR_INFO;
                icono = UITheme.ICONO_VER;
                texto = texto != null ? texto : "Ver";
                break;
                
            default:
                color = UITheme.COLOR_INFO;
                icono = "";
                texto = texto != null ? texto : "Botón";
        }
        
        return crearBoton(texto, color, icono);
    }
    
    /**
     * Crea un campo de texto con estilo consistente
     */
    public static JTextField crearCampoTexto(int columnas) {
        JTextField campo = new JTextField(columnas);
        aplicarEstiloCampo(campo);
        return campo;
    }
    
    /**
     * Crea un área de texto con estilo consistente
     */
    public static JTextArea crearAreaTexto(int filas, int columnas) {
        JTextArea area = new JTextArea(filas, columnas);
        area.setFont(UITheme.FUENTE_CAMPO);
        area.setBorder(crearBordeCampo());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Color.WHITE);
        return area;
    }
    
    /**
     * Crea un combo box con estilo consistente
     */
    public static <E> JComboBox<E> crearComboBox() {
        JComboBox<E> combo = new JComboBox<>();
        aplicarEstiloCampo(combo);
        return combo;
    }
    
    /**
     * Crea un panel de sección con título
     */
    public static JPanel crearSeccionPanel(String titulo, Color colorPrimario) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBordeSeccion());
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(colorPrimario);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(lblTitulo, gbc);
        
        return panel;
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    private static void configurarEfectoHover(JButton boton, Color colorBase) {
        // Obtener los componentes internos para cambiar su color en hover
        Component[] components = ((JPanel) boton.getComponent(0)).getComponents();
        JLabel lblIcono = null;
        JLabel lblTexto = null;
        
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont().getName().contains("Symbol")) {
                    lblIcono = label;
                } else {
                    lblTexto = label;
                }
            }
        }
        
        final JLabel finalLblIcono = lblIcono;
        final JLabel finalLblTexto = lblTexto;
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorBase);
                if (finalLblTexto != null) {
                    finalLblTexto.setForeground(colorBase);
                }
                if (finalLblIcono != null) {
                    finalLblIcono.setForeground(colorBase);
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorBase);
                if (finalLblTexto != null) {
                    finalLblTexto.setForeground(colorBase.darker().darker());
                }
                if (finalLblIcono != null) {
                    finalLblIcono.setForeground(colorBase.darker().darker());
                }
            }
        });
    }
    
    private static void aplicarEstiloCampo(JComponent componente) {
        componente.setFont(UITheme.FUENTE_CAMPO);
        componente.setBorder(crearBordeCampo());
        componente.setBackground(Color.WHITE);
    }
    
    private static Border crearBordeCampo() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );
    }
    
    private static Border crearBordeSeccion() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
    }
    
    // ==================== MÉTODOS PARA ACTUALIZAR CLASES EXISTENTES ====================
    
    /**
     * Método para actualizar los iconos en FacturaFormView
     */
    public static void actualizarIconosFacturaForm(FacturaFormView form) {
        // Estos métodos serían llamados desde el constructor de FacturaFormView
        // después de crear los botones
    }
}