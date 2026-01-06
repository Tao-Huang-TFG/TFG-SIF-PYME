package es.upm.tfg.sifpyme.view;

import es.upm.tfg.sifpyme.controller.ProductoController;
import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.model.entity.TipoIva;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Vista de lista de productos
 * REFACTORIZADO: Ahora usa UIHelper y UITheme
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
            "ID", "Código", "Nombre", "Descripción", 
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
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(100);
    }

    @Override
    protected void cargarDatos() {
        // Verificar que el controller no sea null
        if (controller == null) {
            controller = new ProductoController();
        }
        
        modeloTabla.setRowCount(0);

        List<Producto> productos = controller.obtenerTodosLosProductos();

        for (Producto producto : productos) {
            // Obtener el porcentaje de IVA
            String ivaStr = "";
            if (producto.getIdTipoIva() != null) {
                TipoIva tipoIva = controller.obtenerTipoIvaPorId(producto.getIdTipoIva());
                if (tipoIva != null) {
                    ivaStr = tipoIva.getPorcentaje() + "%";
                }
            }

            Object[] fila = {
                producto.getIdProducto(),
                producto.getCodigo() != null ? producto.getCodigo() : "",
                producto.getNombre(),
                producto.getDescripcion() != null ? truncarTexto(producto.getDescripcion(), 50) : "",
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

    // Métodos auxiliares específicos de Productos
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        if (texto.length() <= maxLength) {
            return texto;
        }
        return texto.substring(0, maxLength) + "...";
    }

    private String formatearPrecio(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return String.format("%.2f", valor);
    }
}