package es.upm.tfg.sifpyme.controller;

import es.upm.tfg.sifpyme.model.dao.ClienteDAO;
import es.upm.tfg.sifpyme.model.entity.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador para gestionar las operaciones relacionadas con Cliente
 */
public class ClienteController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);
    private final ClienteDAO clienteDAO;
    
    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
    }
    
    /**
     * Guarda un nuevo cliente en la base de datos
     */
    public boolean guardarCliente(Cliente cliente) {
        try {
            logger.info("Guardando cliente: {}", cliente.getNombreFiscal());
            
            // Validaciones
            if (!validarCliente(cliente)) {
                logger.warn("Validación de cliente fallida");
                return false;
            }
            
            // Verificar NIF duplicado
            if (clienteDAO.existeNif(cliente.getNif(), null)) {
                logger.warn("Ya existe un cliente con el NIF: {}", cliente.getNif());
                return false;
            }
            
            // Guardar el cliente
            Integer idGenerado = clienteDAO.insertar(cliente);
            
            if (idGenerado != null && idGenerado > 0) {
                logger.info("Cliente guardado exitosamente con ID: {}", idGenerado);
                return true;
            } else {
                logger.error("Error al guardar el cliente");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al guardar cliente", e);
            return false;
        }
    }
    
    /**
     * Actualiza un cliente existente
     */
    public boolean actualizarCliente(Cliente cliente) {
        try {
            logger.info("Actualizando cliente ID: {}", cliente.getIdCliente());
            
            if (!validarCliente(cliente)) {
                return false;
            }
            
            // Verificar NIF duplicado (excluyendo el cliente actual)
            if (clienteDAO.existeNif(cliente.getNif(), cliente.getIdCliente())) {
                logger.warn("Ya existe otro cliente con el NIF: {}", cliente.getNif());
                return false;
            }
            
            boolean actualizado = clienteDAO.actualizar(cliente);
            
            if (actualizado) {
                logger.info("Cliente actualizado exitosamente");
            } else {
                logger.error("Error al actualizar el cliente");
            }
            
            return actualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar cliente", e);
            return false;
        }
    }
    
    /**
     * Elimina un cliente
     */
    public boolean eliminarCliente(Integer id) {
        try {
            logger.info("Eliminando cliente ID: {}", id);
            
            if (id == null || id <= 0) {
                logger.warn("ID de cliente inválido");
                return false;
            }
            
            // Verificar si el cliente existe
            Cliente cliente = clienteDAO.obtenerPorId(id);
            if (cliente == null) {
                logger.warn("Cliente no encontrado: {}", id);
                return false;
            }
            
            boolean eliminado = clienteDAO.eliminar(id);
            
            if (eliminado) {
                logger.info("Cliente eliminado exitosamente");
            } else {
                logger.error("Error al eliminar el cliente");
            }
            
            return eliminado;
            
        } catch (Exception e) {
            logger.error("Error al eliminar cliente", e);
            return false;
        }
    }
    
    /**
     * Obtiene todos los clientes
     */
    public List<Cliente> obtenerTodosLosClientes() {
        try {
            return clienteDAO.obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener clientes", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene un cliente por su ID
     */
    public Cliente obtenerClientePorId(Integer id) {
        try {
            return clienteDAO.obtenerPorId(id);
        } catch (Exception e) {
            logger.error("Error al obtener cliente por ID", e);
            return null;
        }
    }
    
    /**
     * Obtiene un cliente por su NIF
     */
    public Cliente obtenerClientePorNif(String nif) {
        try {
            return clienteDAO.obtenerPorNif(nif);
        } catch (Exception e) {
            logger.error("Error al obtener cliente por NIF", e);
            return null;
        }
    }
    
    /**
     * Busca clientes por término de búsqueda
     */
    public List<Cliente> buscarClientes(String termino) {
        try {
            if (termino == null || termino.trim().isEmpty()) {
                return obtenerTodosLosClientes();
            }
            return clienteDAO.buscar(termino.trim());
        } catch (Exception e) {
            logger.error("Error al buscar clientes", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene el total de clientes registrados
     */
    public int contarClientes() {
        try {
            return clienteDAO.contarClientes();
        } catch (Exception e) {
            logger.error("Error al contar clientes", e);
            return 0;
        }
    }
    
    /**
     * Valida los datos de un cliente
     */
    private boolean validarCliente(Cliente cliente) {
        if (cliente == null) {
            logger.warn("Cliente es null");
            return false;
        }
        
        if (cliente.getNombreFiscal() == null || cliente.getNombreFiscal().trim().isEmpty()) {
            logger.warn("Nombre fiscal vacío");
            return false;
        }
        
        if (cliente.getNif() == null || cliente.getNif().trim().isEmpty()) {
            logger.warn("NIF vacío");
            return false;
        }
        
        // Validaciones opcionales adicionales
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            if (!validarEmail(cliente.getEmail())) {
                logger.warn("Email inválido: {}", cliente.getEmail());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Valida el formato de un email
     */
    private boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
}