package es.upm.tfg.sifpyme.controller;

import es.upm.tfg.sifpyme.model.dao.*;
import es.upm.tfg.sifpyme.model.entity.*;
import es.upm.tfg.sifpyme.service.CalculadoraFacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador para gestionar las operaciones relacionadas con Factura
 */
public class FacturaController {
    
    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);
    private final FacturaDAO facturaDAO;
    private final EmpresaDAO empresaDAO;
    private final ClienteDAO clienteDAO;
    private final ProductoDAO productoDAO;
    private final CalculadoraFacturaService calculadoraService;
    
    public FacturaController() {
        this.facturaDAO = new FacturaDAO();
        this.empresaDAO = new EmpresaDAO();
        this.clienteDAO = new ClienteDAO();
        this.productoDAO = new ProductoDAO();
        this.calculadoraService = new CalculadoraFacturaService();
    }
    
    /**
     * Guarda una nueva factura en la base de datos
     */
    public boolean guardarFactura(Factura factura) {
        try {
            logger.info("Guardando factura: {}-{}", factura.getSerie(), factura.getNumeroFactura());
            
            if (!validarFactura(factura)) {
                logger.warn("Validación de factura fallida");
                return false;
            }
            
            // Verificar número duplicado
            if (facturaDAO.existeNumero(factura.getIdEmpresa(), 
                    factura.getSerie(), factura.getNumeroFactura(), null)) {
                logger.warn("Ya existe una factura con el número: {}-{}", 
                    factura.getSerie(), factura.getNumeroFactura());
                return false;
            }
            
            // Recalcular totales por seguridad
            calculadoraService.recalcularFacturaCompleta(factura);
            
            // Guardar la factura
            Integer idGenerado = facturaDAO.insertar(factura);
            
            if (idGenerado != null && idGenerado > 0) {
                logger.info("Factura guardada exitosamente con ID: {}", idGenerado);
                return true;
            } else {
                logger.error("Error al guardar la factura");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al guardar factura", e);
            return false;
        }
    }
    
    /**
     * Actualiza una factura existente
     */
    public boolean actualizarFactura(Factura factura) {
        try {
            logger.info("Actualizando factura ID: {}", factura.getIdFactura());
            
            if (!validarFactura(factura)) {
                return false;
            }
            
            // Verificar número duplicado (excluyendo la factura actual)
            if (facturaDAO.existeNumero(factura.getIdEmpresa(), 
                    factura.getSerie(), factura.getNumeroFactura(), 
                    factura.getIdFactura())) {
                logger.warn("Ya existe otra factura con el número: {}-{}", 
                    factura.getSerie(), factura.getNumeroFactura());
                return false;
            }
            
            // Recalcular totales
            calculadoraService.recalcularFacturaCompleta(factura);
            
            boolean actualizado = facturaDAO.actualizar(factura);
            
            if (actualizado) {
                logger.info("Factura actualizada exitosamente");
            } else {
                logger.error("Error al actualizar la factura");
            }
            
            return actualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar factura", e);
            return false;
        }
    }
    
    /**
     * Elimina una factura
     */
    public boolean eliminarFactura(Integer id) {
        try {
            logger.info("Eliminando factura ID: {}", id);
            
            if (id == null || id <= 0) {
                logger.warn("ID de factura inválido");
                return false;
            }
            
            // Verificar si la factura existe
            Factura factura = facturaDAO.obtenerPorId(id);
            if (factura == null) {
                logger.warn("Factura no encontrada: {}", id);
                return false;
            }
            
            boolean eliminado = facturaDAO.eliminar(id);
            
            if (eliminado) {
                logger.info("Factura eliminada exitosamente");
            } else {
                logger.error("Error al eliminar la factura");
            }
            
            return eliminado;
            
        } catch (Exception e) {
            logger.error("Error al eliminar factura", e);
            return false;
        }
    }
    
    /**
     * Obtiene todas las facturas
     */
    public List<Factura> obtenerTodasLasFacturas() {
        try {
            return facturaDAO.obtenerTodas();
        } catch (Exception e) {
            logger.error("Error al obtener facturas", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene una factura por su ID, con todas sus líneas y relaciones
     */
    public Factura obtenerFacturaPorId(Integer id) {
        try {
            Factura factura = facturaDAO.obtenerPorId(id);
            
            if (factura != null) {
                // Cargar empresa
                Empresa empresa = empresaDAO.obtenerPorId(factura.getIdEmpresa());
                factura.setEmpresa(empresa);
                
                // Cargar cliente
                Cliente cliente = clienteDAO.obtenerPorId(factura.getIdCliente());
                factura.setCliente(cliente);
                
                // Cargar productos en las líneas
                if (factura.getLineas() != null) {
                    for (LineaFactura linea : factura.getLineas()) {
                        Producto producto = productoDAO.obtenerPorId(linea.getIdProducto());
                        linea.setProducto(producto);
                    }
                }
            }
            
            return factura;
        } catch (Exception e) {
            logger.error("Error al obtener factura por ID", e);
            return null;
        }
    }
    
    /**
     * Busca facturas por término de búsqueda
     */
    public List<Factura> buscarFacturas(String termino) {
        try {
            if (termino == null || termino.trim().isEmpty()) {
                return obtenerTodasLasFacturas();
            }
            return facturaDAO.buscar(termino.trim());
        } catch (Exception e) {
            logger.error("Error al buscar facturas", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene el total de facturas registradas
     */
    public int contarFacturas() {
        try {
            return facturaDAO.contarFacturas();
        } catch (Exception e) {
            logger.error("Error al contar facturas", e);
            return 0;
        }
    }
    
    /**
     * Obtiene el siguiente número de factura para una empresa y serie
     */
    public String obtenerSiguienteNumero(Integer idEmpresa, String serie) {
        try {
            return facturaDAO.obtenerSiguienteNumero(idEmpresa, serie);
        } catch (Exception e) {
            logger.error("Error al obtener siguiente número", e);
            return "000001";
        }
    }
    
    /**
     * Obtiene todas las empresas disponibles
     */
    public List<Empresa> obtenerEmpresas() {
        try {
            return empresaDAO.obtenerTodas();
        } catch (Exception e) {
            logger.error("Error al obtener empresas", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene todos los clientes disponibles
     */
    public List<Cliente> obtenerClientes() {
        try {
            return clienteDAO.obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener clientes", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene todos los productos disponibles
     */
    public List<Producto> obtenerProductos() {
        try {
            return productoDAO.obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener productos", e);
            return List.of();
        }
    }
    
    /**
     * Busca clientes por término
     */
    public List<Cliente> buscarClientes(String termino) {
        try {
            return clienteDAO.buscar(termino);
        } catch (Exception e) {
            logger.error("Error al buscar clientes", e);
            return List.of();
        }
    }
    
    /**
     * Busca productos por término
     */
    public List<Producto> buscarProductos(String termino) {
        try {
            return productoDAO.buscar(termino);
        } catch (Exception e) {
            logger.error("Error al buscar productos", e);
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
            logger.error("Error al obtener producto", e);
            return null;
        }
    }
    
    /**
     * Calcula una línea de factura
     */
    public void calcularLinea(LineaFactura linea) {
        calculadoraService.calcularImportesLinea(linea);
    }
    
    /**
     * Calcula los totales de una factura
     */
    public void calcularTotales(Factura factura) {
        calculadoraService.calcularTotalesFactura(factura);
    }
    
    /**
     * Valida los datos de una factura
     */
    private boolean validarFactura(Factura factura) {
        if (factura == null) {
            logger.warn("Factura es null");
            return false;
        }
        
        if (factura.getIdEmpresa() == null) {
            logger.warn("Empresa no especificada");
            return false;
        }
        
        if (factura.getIdCliente() == null) {
            logger.warn("Cliente no especificado");
            return false;
        }
        
        if (factura.getSerie() == null || factura.getSerie().trim().isEmpty()) {
            logger.warn("Serie vacía");
            return false;
        }
        
        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().trim().isEmpty()) {
            logger.warn("Número de factura vacío");
            return false;
        }
        
        if (factura.getMetodoPago() == null || factura.getMetodoPago().trim().isEmpty()) {
            logger.warn("Método de pago vacío");
            return false;
        }
        
        if (factura.getLineas() == null || factura.getLineas().isEmpty()) {
            logger.warn("Factura sin líneas");
            return false;
        }
        
        // Validar cada línea
        for (LineaFactura linea : factura.getLineas()) {
            if (linea.getIdProducto() == null) {
                logger.warn("Línea sin producto");
                return false;
            }
            if (linea.getCantidad() == null || linea.getCantidad().signum() <= 0) {
                logger.warn("Cantidad inválida");
                return false;
            }
            if (linea.getPrecioUnitario() == null || linea.getPrecioUnitario().signum() < 0) {
                logger.warn("Precio unitario inválido");
                return false;
            }
        }
        
        return true;
    }
}