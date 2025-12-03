package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.EmpresaController;
import es.upm.tfg.sifpyme.model.entity.Empresa;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EmpresasView extends BaseListView<Empresa> {

    private EmpresaController controller;

    public EmpresasView() {
        this.controller = new EmpresaController();
        cargarDatos();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = new Color(41, 128, 185);
        COLOR_SECUNDARIO = new Color(52, 152, 219);
    }

    @Override
    protected String getTituloVentana() {
        return "Gestión de Empresas - SifPyme";
    }
    
    @Override
    protected String getTituloHeader() {
        return "Gestión de Empresas";
    }

    @Override
    protected String getSubtituloHeader() {
        return "Administra las empresas desde las que facturas";
    }

    @Override
    protected String getIconoHeader() {
        return "🏢";
    }

    @Override
    protected String[] getNombresColumnas() {
        return new String[]{ "ID", "Nombre Comercial", "Razón Social", "NIF", "Ciudad", "Provincia", "Email", "Por Defecto" };
    }

    @Override
    protected String getNombreCardLista() {
        return "listaEmpresas";
    }

    @Override
    protected String getNombreCardFormulario() {
        return "formularioEmpresa";
    }

    @Override
    protected String getNombreEntidadSingular() {
        return "empresa";
    }

    @Override
    protected String getNombreEntidadPlural() {
        return "empresas";
    }

    @Override
    protected void configurarAnchoColumnas() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(100);
    }

    @Override
    protected void cargarDatos() {
        // Verificar que el controller no sea null
        if (controller == null) {
            controller = new EmpresaController();
        }
        
        modeloTabla.setRowCount(0);

        List<Empresa> empresas = controller.obtenerTodasLasEmpresas();

        for (Empresa empresa : empresas) {
            Object[] fila = {
                empresa.getIdEmpresa(),
                empresa.getNombreComercial(),
                empresa.getRazonSocial(),
                empresa.getNif(),
                empresa.getCiudad(),
                empresa.getProvincia(),
                empresa.getEmail() != null ? empresa.getEmail() : "",
                empresa.getPorDefecto() != null && empresa.getPorDefecto()
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotal();
    }

    @Override
    protected JPanel crearFormularioNuevo() {
        return new EmpresaFormView(cardLayout, cardPanel);
    }

    @Override
    protected JPanel crearFormularioEdicion(Integer id) {
        Empresa empresa = controller.obtenerEmpresaPorId(id);
        if (empresa != null) {
            return new EmpresaFormView(cardLayout, cardPanel, empresa);
        }
        return null;
    }

    @Override
    protected boolean eliminarRegistro(Integer id) {
        return controller.eliminarEmpresaporId(id);
    }

    @Override
    protected void agregarBotonesAdicionales(JPanel buttonsPanel) {
        // Botón para establecer empresa por defecto
        JButton btnEstablecerDefecto = crearBoton("⭐ Establecer Defecto", new Color(241, 196, 15));
        btnEstablecerDefecto.addActionListener(e -> establecerEmpresaPorDefecto());
        buttonsPanel.add(btnEstablecerDefecto);
    }

    // Método específico para EmpresasView
    private void establecerEmpresaPorDefecto() {
        int filaSeleccionada = tabla.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, selecciona una empresa de la lista.",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);
        Integer idEmpresa = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombreEmpresa = (String) modeloTabla.getValueAt(filaModelo, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Establecer '" + nombreEmpresa + "' como empresa por defecto?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            Empresa empresa = controller.obtenerEmpresaPorId(idEmpresa);
            if (empresa != null) {
                empresa.setPorDefecto(true);
                boolean actualizado = controller.actualizarEmpresa(empresa);

                if (actualizado) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Empresa establecida como predeterminada.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Error al establecer empresa por defecto.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    protected void eliminar() {
        int filaSeleccionada = tabla.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, selecciona una empresa de la lista.",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);
        Boolean esPorDefecto = (Boolean) modeloTabla.getValueAt(filaModelo, 7);

        if (esPorDefecto) {
            JOptionPane.showMessageDialog(
                this,
                "No puedes eliminar la empresa por defecto.\nPrimero establece otra como predeterminada.",
                "Operación no permitida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        super.eliminar(); // Llama al método base para confirmar y eliminar
    }
}