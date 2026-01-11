package es.upm.tfg.sifpyme.view;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para seleccionar el formato de exportación (PDF o Excel)
 * Usa UIHelper y UITheme para consistencia visual
 */
public class FormatoExportacionDialog extends JDialog {

    public enum FormatoSeleccionado {
        PDF, EXCEL, CANCELAR
    }

    private FormatoSeleccionado formatoSeleccionado = FormatoSeleccionado.CANCELAR;

    public FormatoExportacionDialog(JFrame parent) {
        super(parent, "Seleccionar Formato", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // Header
        add(crearHeaderPanel(), BorderLayout.NORTH);

        // Opciones
        add(crearPanelOpciones(), BorderLayout.CENTER);

        // Inferior
        add(crearPanelInferior(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(520, 320));
        setMaximumSize(new Dimension(650, 420));
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    private JPanel crearHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.COLOR_FACTURAS);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("Exportar Factura");
        lblTitulo.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Selecciona el formato de exportación");
        lblSubtitulo.setFont(UITheme.FUENTE_SUBTITULO);
        lblSubtitulo.setForeground(new Color(240, 240, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(lblTitulo);
        textPanel.add(lblSubtitulo);

        JLabel lblIcono = new JLabel(UITheme.ICONO_FACTURAS, SwingConstants.RIGHT);
        lblIcono.setFont(UITheme.FUENTE_ICONO_TEXTO);
        lblIcono.setForeground(Color.WHITE);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(lblIcono, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 35));
        panel.setBackground(UITheme.COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton btnPDF = crearBotonFormato("eliminar", "PDF");
        btnPDF.addActionListener(e -> {
            formatoSeleccionado = FormatoSeleccionado.PDF;
            dispose();
        });

        JButton btnExcel = crearBotonFormato("guardar", "EXCEL");
        btnExcel.addActionListener(e -> {
            formatoSeleccionado = FormatoSeleccionado.EXCEL;
            dispose();
        });

        panel.add(btnPDF);
        panel.add(btnExcel);

        return panel;
    }

    /**
     * Botón grande tipo tarjeta basado en UIHelper
     */
    private JButton crearBotonFormato(String tipoAccion, String texto) {
        JButton boton = UIHelper.crearBotonAccion(tipoAccion, texto);

        Color colorBase = boton.getBackground();

        boton.setFont(UITheme.FUENTE_TITULO_SECUNDARIO);
        boton.setBackground(Color.WHITE);
        boton.setForeground(colorBase);
        boton.setFocusPainted(false);

        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBase, 2),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        boton.setPreferredSize(new Dimension(140, 80));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(colorBase);
                boton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(colorBase);
            }
        });

        return boton;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(UITheme.COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        JButton btnCancelar = UIHelper.crearBotonAccion("cancelar", "Cancelar");
        btnCancelar.addActionListener(e -> {
            formatoSeleccionado = FormatoSeleccionado.CANCELAR;
            dispose();
        });

        panel.add(btnCancelar);
        return panel;
    }

    public FormatoSeleccionado getFormatoSeleccionado() {
        return formatoSeleccionado;
    }

    /**
     * Método estático para mostrar el diálogo y obtener la selección
     */
    public static FormatoSeleccionado mostrarDialogo(JFrame parent) {
        FormatoExportacionDialog dialog = new FormatoExportacionDialog(parent);
        dialog.setVisible(true);
        return dialog.getFormatoSeleccionado();
    }
}
