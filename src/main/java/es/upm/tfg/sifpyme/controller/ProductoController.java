package es.upm.tfg.sifpyme.controller;

import es.upm.tfg.sifpyme.model.dao.ProductoDAO;
import es.upm.tfg.sifpyme.model.dao.TipoIvaDAO;
import es.upm.tfg.sifpyme.model.entity.Producto;
import es.upm.tfg.sifpyme.model.entity.TipoIva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador para gestionar las operaciones relacionadas con Producto
 */
public class ProductoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    private final ProductoDAO productoDAO;
    private final TipoIvaDAO tipoIvaDAO;
    
    public ProductoController() {
        this.productoDAO = new ProductoDAO();
        this.tipoIvaDAO = new TipoIvaDAO();
    }
    
    /**
     * Guarda un nuevo producto en la base de datos
     */
    public boolean guardarProducto(Producto producto) {
        try {
            logger.info("Guardando producto: {}", producto.getNombre());
            
            // Validaciones
            if (!validarProducto(producto)) {
                logger.warn("Validación de producto fallida");
                return false;
            }
            
            // Verificar código duplicado si se proporciona
            if (producto.getCodigo() != null && !producto.getCodigo().trim().isEmpty()) {
                if (productoDAO.existeCodigo(producto.getCodigo(), null)) {
                    logger.warn("Ya existe un producto con el código: {}", producto.getCodigo());
                    return false;
                }
            }
            
            // Guardar el producto
            Integer idGenerado = productoDAO.insertar(producto);
            
            if (idGenerado != null && idGenerado > 0) {
                logger.info("Producto guardado exitosamente con ID: {}", idGenerado);
                return true;
            } else {
                logger.error("Error al guardar el producto");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al guardar producto", e);
            return false;
        }
    }
    
    /**
     * Actualiza un producto existente
     */
    public boolean actualizarProducto(Producto producto) {
        try {
            logger.info("Actualizando producto ID: {}", producto.getIdProducto());
            
            if (!validarProducto(producto)) {
                return false;
            }
            
            // Verificar código duplicado (excluyendo el producto actual)
            if (producto.getCodigo() != null && !producto.getCodigo().trim().isEmpty()) {
                if (productoDAO.existeCodigo(producto.getCodigo(), producto.getIdProducto())) {
                    logger.warn("Ya existe otro producto con el código: {}", producto.getCodigo());
                    return false;
                }
            }
            
            boolean actualizado = productoDAO.actualizar(producto);
            
            if (actualizado) {
                logger.info("Producto actualizado exitosamente");
            } else {
                logger.error("Error al actualizar el producto");
            }
            
            return actualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar producto", e);
            return false;
        }
    }
    
    /**
     * Elimina un producto
     */
    public boolean eliminarProducto(Integer id) {
        try {
            logger.info("Eliminando producto ID: {}", id);
            
            if (id == null || id <= 0) {
                logger.warn("ID de producto inválido");
                return false;
            }
            
            // Verificar si el producto existe
            Producto producto = productoDAO.obtenerPorId(id);
            if (producto == null) {
                logger.warn("Producto no encontrado: {}", id);
                return false;
            }
            
            boolean eliminado = productoDAO.eliminar(id);
            
            if (eliminado) {
                logger.info("Producto eliminado exitosamente");
            } else {
                logger.error("Error al eliminar el producto");
            }
            
            return eliminado;
            
        } catch (Exception e) {
            logger.error("Error al eliminar producto", e);
            return false;
        }
    }
    
    /**
     * Obtiene todos los productos
     */
    public List<Producto> obtenerTodosLosProductos() {
        try {
            return productoDAO.obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener productos", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene un producto por su ID
     */
    public Producto obtenerProductoPorId(Integer id) {
        try {
            return productoDAO.obtenerPorId(id);
        } catch (Exception e) {
            logger.error("Error al obtener producto por ID", e);
            return null;
        }
    }
    
    /**
     * Obtiene un producto por su código
     */
    public Producto obtenerProductoPorCodigo(String codigo) {
        try {
            return productoDAO.obtenerPorCodigo(codigo);
        } catch (Exception e) {
            logger.error("Error al obtener producto por código", e);
            return null;
        }
    }
    
    /**
     * Busca productos por término de búsqueda
     */
    public List<Producto> buscarProductos(String termino) {
        try {
            if (termino == null || termino.trim().isEmpty()) {
                return obtenerTodosLosProductos();
            }
            return productoDAO.buscar(termino.trim());
        } catch (Exception e) {
            logger.error("Error al buscar productos", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene el total de productos registrados
     */
    public int contarProductos() {
        try {
            return productoDAO.contarProductos();
        } catch (Exception e) {
            logger.error("Error al contar productos", e);
            return 0;
        }
    }
    
    /**
     * Obtiene todos los tipos de IVA disponibles
     */
    public List<TipoIva> obtenerTiposIva() {
        try {
            return tipoIvaDAO.obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener tipos de IVA", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene un tipo de IVA por su ID
     */
    public TipoIva obtenerTipoIvaPorId(Integer id) {
        try {
            return tipoIvaDAO.obtenerPorId(id);
        } catch (Exception e) {
            logger.error("Error al obtener tipo de IVA por ID", e);
            return null;
        }
    }
    
    /**
     * Valida los datos de un producto
     */
    private boolean validarProducto(Producto producto) {
        if (producto == null) {
            logger.warn("Producto es null");
            return false;
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            logger.warn("Nombre del producto vacío");
            return false;
        }
        
        // Validar que al menos uno de los precios esté presente
        if (producto.getPrecio() == null && producto.getPrecioBase() == null) {
            logger.warn("Debe proporcionar al menos precio o precio base");
            return false;
        }
        
        // Validar que los precios sean positivos
        if (producto.getPrecio() != null && producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("El precio no puede ser negativo");
            return false;
        }
        
        if (producto.getPrecioBase() != null && producto.getPrecioBase().compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("El precio base no puede ser negativo");
            return false;
        }
        
        // Validar tipo de retención
        if (producto.getTipoRetencion() != null) {
            if (producto.getTipoRetencion().compareTo(BigDecimal.ZERO) < 0 || 
                producto.getTipoRetencion().compareTo(new BigDecimal("100")) > 0) {
                logger.warn("El tipo de retención debe estar entre 0 y 100");
                return false;
            }
        }
        
        return true;
    }
}