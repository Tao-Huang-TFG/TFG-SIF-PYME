package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.Producto;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Vista de lista de productos
 * CORREGIDO: Eliminada referencia a TipoIva - ahora usa tipo_iva directo
 */
public class ProductosView extends BaseListView<Producto> {

    private ProductoController controller;

    public ProductosView() {
        this.controller = new ProductoController();
        cargarDatos();
    }

    @Override
    protected void configurarColores() {
        COLOR_PRIMARIO = UITheme.COLOR_PRODUCTOS;
        COLOR_SECUNDARIO = UITheme.COLOR_PRODUCTOS;
    }

    @Override
    protected String getTituloVentana() {
        return "Gestión de Productos - SifPyme";
    }
    
    @Override
    protected String getTituloHeader() {
        return "Gestión de Productos";
    }

    @Override
    protected String getSubtituloHeader() {
        return "Administra tu catálogo de productos y servicios";
    }

    @Override
    protected String getIconoHeader() {
        return UITheme.ICONO_PRODUCTOS;
    }

    @Override
    protected String[] getNombresColumnas() {
        return new String[]{ 
            "ID", "Código", "Nombre", 
            "Precio", "Precio Base", "IVA %", "Retención %" 
        };
    }

    @Override
    protected String getNombreCardLista() {
        return "listaProductos";
    }

    @Override
    protected String getNombreCardFormulario() {
        return "formularioProducto";
    }

    @Override
    protected String getNombreEntidadSingular() {
        return "producto";
    }

    @Override
    protected String getNombreEntidadPlural() {
        return "productos";
    }

    @Override
    protected void configurarAnchoColumnas() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);  // Código
        tabla.getColumnModel().getColumn(2).setPreferredWidth(300);  // Nombre
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);  // Precio
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Precio Base
        tabla.getColumnModel().getColumn(5).setPreferredWidth(80);   // IVA %
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);  // Retención %
    }

    @Override
    protected void cargarDatos() {
        if (controller == null) {
            controller = new ProductoController();
        }
        
        modeloTabla.setRowCount(0);

        List<Producto> productos = controller.obtenerTodosLosProductos();

        for (Producto producto : productos) {
            // CAMBIADO: Obtener IVA directamente del producto (ya no hay TipoIva)
            String ivaStr = "";
            if (producto.getTipoIva() != null) {
                ivaStr = producto.getTipoIva() + "%";
            }

            Object[] fila = {
                producto.getIdProducto(),
                producto.getCodigo() != null ? producto.getCodigo() : "",
                producto.getNombre(),
                formatearPrecio(producto.getPrecio()),
                formatearPrecio(producto.getPrecioBase()),
                ivaStr,
                formatearPrecio(producto.getTipoRetencion())
            };
            modeloTabla.addRow(fila);
        }

        actualizarTotal();
    }

    @Override
    protected JPanel crearFormularioNuevo() {
        return new ProductoFormView(cardLayout, cardPanel);
    }

    @Override
    protected JPanel crearFormularioEdicion(Integer id) {
        Producto producto = controller.obtenerProductoPorId(id);
        if (producto != null) {
            return new ProductoFormView(cardLayout, cardPanel, producto);
        }
        return null;
    }

    @Override
    protected boolean eliminarRegistro(Integer id) {
        return controller.eliminarProducto(id);
    }

    // Métodos auxiliares
    private String formatearPrecio(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return String.format("%.2f", valor);
    }
}