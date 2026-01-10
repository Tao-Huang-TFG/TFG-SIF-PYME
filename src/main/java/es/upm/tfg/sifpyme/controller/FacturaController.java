package es.upm.tfg.sifpyme.controller;

import es.upm.tfg.sifpyme.model.dao.*;
import es.upm.tfg.sifpyme.model.entity.*;
import es.upm.tfg.sifpyme.service.CalculadoraFacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador para gestionar las operaciones relacionadas con Factura
 * REFACTORIZADO: Eliminados métodos obsoletos (generarSiguienteId, obtenerSiguienteNumero)
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
            logger.info("Guardando factura: {}", factura.getIdFactura());
            
            if (!validarFactura(factura)) {
                logger.warn("Validación de factura fallida");
                return false;
            }
            
            // Verificar ID duplicado
            if (facturaDAO.existeId(factura.getIdFactura(), null)) {
                logger.warn("Ya existe una factura con el ID: {}", factura.getIdFactura());
                return false;
            }
            
            // Recalcular totales por seguridad
            calculadoraService.recalcularFacturaCompleta(factura);
            
            // Guardar la factura
            boolean guardado = facturaDAO.insertar(factura);
            
            if (guardado) {
                logger.info("Factura guardada exitosamente: {}", factura.getIdFactura());
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
            
            // Verificar que la factura existe
            Factura existente = facturaDAO.obtenerPorId(factura.getIdFactura());
            if (existente == null) {
                logger.warn("Factura no encontrada: {}", factura.getIdFactura());
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
    public boolean eliminarFactura(String id) {
        try {
            logger.info("Eliminando factura ID: {}", id);
            
            if (id == null || id.trim().isEmpty()) {
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
            List<Factura> facturas = facturaDAO.obtenerTodas();
            
            // Cargar datos relacionados
            for (Factura factura : facturas) {
                cargarDatosRelacionados(factura);
            }
            
            return facturas;
        } catch (Exception e) {
            logger.error("Error al obtener facturas", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene una factura por su ID, con todas sus líneas y relaciones
     */
    public Factura obtenerFacturaPorId(String id) {
        try {
            Factura factura = facturaDAO.obtenerPorId(id);
            
            if (factura != null) {
                cargarDatosRelacionados(factura);
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
            
            List<Factura> facturas = facturaDAO.buscar(termino.trim());
            
            // Cargar datos relacionados
            for (Factura factura : facturas) {
                cargarDatosRelacionados(factura);
            }
            
            return facturas;
        } catch (Exception e) {
            logger.error("Error al buscar facturas", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene facturas por cliente
     */
    public List<Factura> obtenerFacturasPorCliente(Integer idCliente) {
        try {
            List<Factura> facturas = facturaDAO.obtenerPorCliente(idCliente);
            
            // Cargar datos relacionados
            for (Factura factura : facturas) {
                cargarDatosRelacionados(factura);
            }
            
            return facturas;
        } catch (Exception e) {
            logger.error("Error al obtener facturas por cliente", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene facturas por empresa
     */
    public List<Factura> obtenerFacturasPorEmpresa(Integer idEmpresa) {
        try {
            List<Factura> facturas = facturaDAO.obtenerPorEmpresa(idEmpresa);
            
            // Cargar datos relacionados
            for (Factura factura : facturas) {
                cargarDatosRelacionados(factura);
            }
            
            return facturas;
        } catch (Exception e) {
            logger.error("Error al obtener facturas por empresa", e);
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
     * Carga los datos relacionados de una factura
     */
    private void cargarDatosRelacionados(Factura factura) {
        try {
            // Cargar empresa
            if (factura.getIdEmpresa() != null) {
                Empresa empresa = empresaDAO.obtenerPorId(factura.getIdEmpresa());
                factura.setEmpresa(empresa);
            }
            
            // Cargar cliente
            if (factura.getIdCliente() != null) {
                Cliente cliente = clienteDAO.obtenerPorId(factura.getIdCliente());
                factura.setCliente(cliente);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar datos relacionados de factura", e);
        }
    }
    
    /**
     * Valida los datos de una factura
     */
    private boolean validarFactura(Factura factura) {
        if (factura == null) {
            logger.warn("Factura es null");
            return false;
        }
        
        if (factura.getIdFactura() == null || factura.getIdFactura().trim().isEmpty()) {
            logger.warn("ID de factura vacío");
            return false;
        }
        
        if (factura.getIdFactura().length() > 20) {
            logger.warn("ID de factura excede 20 caracteres");
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
            if (linea.getCantidad() == null || linea.getCantidad().signum() <= 0) {
                logger.warn("Cantidad inválida en línea");
                return false;
            }
            if (linea.getPrecioUnitario() == null || linea.getPrecioUnitario().signum() < 0) {
                logger.warn("Precio unitario inválido en línea");
                return false;
            }
            if (linea.getPorcentajeIva() == null || linea.getPorcentajeIva().signum() < 0) {
                logger.warn("Porcentaje de IVA inválido en línea");
                return false;
            }
        }
        
        return true;
    }
}