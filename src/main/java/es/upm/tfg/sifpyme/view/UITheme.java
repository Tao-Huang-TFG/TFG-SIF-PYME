package es.upm.tfg.sifpyme.view;

import javax.swing.*;
import java.awt.*;

/**
 * Clase centralizada para definir el tema visual de la aplicación.
 * Sigue el principio DRY y permite cambios globales en un solo lugar.
 */
public class UITheme {

    // ==================== COLORES PRIMARIOS POR MÓDULO ====================

    // Colores base
    public static final Color COLOR_EXITO = new Color(46, 204, 113);
    public static final Color COLOR_PELIGRO = new Color(231, 76, 60);
    public static final Color COLOR_INFO = new Color(52, 152, 219);
    public static final Color COLOR_FONDO = new Color(245, 245, 245);
    public static final Color COLOR_BORDE = new Color(220, 220, 220);
    public static final Color COLOR_VOLVER = new Color(149, 165, 166);

    // Colores por módulo (pueden sobrescribirse en subclases)
    public static final Color COLOR_EMPRESAS = new Color(41, 128, 185);
    public static final Color COLOR_CLIENTES = new Color(155, 89, 182);
    public static final Color COLOR_PRODUCTOS = new Color(52, 152, 219);
    public static final Color COLOR_FACTURAS = new Color(46, 204, 113);
    public static final Color COLOR_MENU_PRINCIPAL = new Color(41, 128, 185);

    // ==================== FUENTES ====================

    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FUENTE_TITULO_SECUNDARIO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_SUBTITULO_NEGRITA = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_BOTON = new Font("Segoe UI Symbol", Font.BOLD, 14);
    public static final Font FUENTE_TABLA = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_ICONO_TEXTO = new Font("Segoe UI Symbol", Font.PLAIN, 13);
    public static final Font FUENTE_MONOSPACE = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FUENTE_RESULTADOS = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_TOTAL = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FUENTE_ICONO_GRANDE = new Font("Segoe UI Emoji", Font.PLAIN, 36);
    public static final Font FUENTE_ICONO_MEDIANO = new Font("Segoe UI Emoji", Font.PLAIN, 28);
    public static final Font FUENTE_ICONO_PEQUENO = new Font("Segoe UI Symbol", Font.PLAIN, 16);
    public static final Font FUENTE_ICONO_AYUDA = new Font("Segoe UI Symbol", Font.PLAIN, 13);

    // ==================== ICONOS (Unicode) ====================

    // Iconos principales para encabezados
    public static final String ICONO_EMPRESAS = "🏢";
    public static final String ICONO_CLIENTES = "👥";
    public static final String ICONO_PRODUCTOS = "📦";
    public static final String ICONO_FACTURAS = "🧾";
    public static final String ICONO_MENU = "📋";

    // Iconos para botones (Unicode)
    public static final String ICONO_NUEVO = "\u002B"; // +
    public static final String ICONO_EDITAR = "\u270E"; // ✎
    public static final String ICONO_ELIMINAR = "\u2716"; // ✖
    public static final String ICONO_GUARDAR = "\u2714"; // ✔
    public static final String ICONO_CANCELAR = "\u2718"; // ✘
    public static final String ICONO_VOLVER = "\u2190"; // ←
    public static final String ICONO_BUSCAR = "\u2315"; // ⌕
    public static final String ICONO_VER = "\u25C9"; // ◉
    public static final String ICONO_CONFIG = "\u2699"; // ⚙
    public static final String ICONO_SALIR = "\u21AA"; // ↪
    public static final String ICONO_AGREGAR = "\u002B"; // +
    public static final String ICONO_ESTABLECER = "\u2605"; // ★
    public static final String ICONO_PDF = "📄"; // Emoji PDF

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el color primario para un módulo específico
     */
    public static Color getColorPrimario(String modulo) {
        switch (modulo.toLowerCase()) {
            case "empresas":
                return COLOR_EMPRESAS;
            case "clientes":
                return COLOR_CLIENTES;
            case "productos":
                return COLOR_PRODUCTOS;
            case "facturas":
                return COLOR_FACTURAS;
            case "menu":
                return COLOR_MENU_PRINCIPAL;
            default:
                return COLOR_INFO;
        }
    }

    /**
     * Obtiene el icono para un módulo específico
     */
    public static String getIconoModulo(String modulo) {
        switch (modulo.toLowerCase()) {
            case "empresas":
                return ICONO_EMPRESAS;
            case "clientes":
                return ICONO_CLIENTES;
            case "productos":
                return ICONO_PRODUCTOS;
            case "facturas":
                return ICONO_FACTURAS;
            case "menu":
                return ICONO_MENU;
            default:
                return "";
        }
    }

    /**
     * Crea un ícono de FontAwesome o similar (para uso futuro con librerías de
     * íconos)
     */
    public static Icon crearIcono(String codigoUnicode, Color color, int tamaño) {
        JLabel label = new JLabel(codigoUnicode);
        label.setFont(new Font("Segoe UI Symbol", Font.PLAIN, tamaño));
        label.setForeground(color);

        // Convertir JLabel a ImageIcon (simplificado)
        return new ImageIcon();
    }
}