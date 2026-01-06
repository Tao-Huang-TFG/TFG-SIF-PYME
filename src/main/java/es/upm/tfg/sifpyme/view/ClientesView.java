package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ClienteController;
import es.upm.tfg.sifpyme.model.entity.Cliente;

import javax.swing.*;
import java.util.List;

/**
 * Vista de lista de clientes
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
 */
public class ClientesView extends BaseListView<Cliente> {

    private ClienteController controller;

    public ClientesView() {
        this.controller = new ClienteController();
        cargarDatos();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = UITheme.COLOR_CLIENTES;
        COLOR_SECUNDARIO = UITheme.COLOR_CLIENTES;
    }

    @Override
    protected String getTituloVentana() {
        return "Gestión de Clientes - SifPyme";
    }
    
    @Override
    protected String getTituloHeader() {
        return "Gestión de Clientes";
    }

    @Override
    protected String getSubtituloHeader() {
        return "Administra tu base de datos de clientes";
    }

    @Override
    protected String getIconoHeader() {
        return UITheme.ICONO_CLIENTES;
    }

    @Override
    protected String[] getNombresColumnas() {
        return new String[]{ 
            "ID", "Nombre Fiscal", "NIF", "Dirección", "Teléfono", "Email" 
        };
    }

    @Override
    protected String getNombreCardLista() {
        return "listaClientes";
    }

    @Override
    protected String getNombreCardFormulario() {
        return "formularioCliente";
    }

    @Override
    protected String getNombreEntidadSingular() {
        return "cliente";
    }

    @Override
    protected String getNombreEntidadPlural() {
        return "clientes";
    }

    @Override
    protected void configurarAnchoColumnas() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(200);
    }

    @Override
    protected void cargarDatos() {
        // Verificar que el controller no sea null
        if (controller == null) {
            controller = new ClienteController();
        }
        
        modeloTabla.setRowCount(0);

        List<Cliente> clientes = controller.obtenerTodosLosClientes();

        for (Cliente cliente : clientes) {
            Object[] fila = {
                cliente.getIdCliente(),
                cliente.getNombreFiscal(),
                cliente.getNif(),
                cliente.getDireccion() != null ? cliente.getDireccion() : "",
                cliente.getTelefono() != null ? cliente.getTelefono() : "",
                cliente.getEmail() != null ? cliente.getEmail() : ""
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotal();
    }

    @Override
    protected JPanel crearFormularioNuevo() {
        return new ClienteFormView(cardLayout, cardPanel);
    }

    @Override
    protected JPanel crearFormularioEdicion(Integer id) {
        Cliente cliente = controller.obtenerClientePorId(id);
        if (cliente != null) {
            return new ClienteFormView(cardLayout, cardPanel, cliente);
        }
        return null;
    }

    @Override
    protected boolean eliminarRegistro(Integer id) {
        return controller.eliminarCliente(id);
    }
}