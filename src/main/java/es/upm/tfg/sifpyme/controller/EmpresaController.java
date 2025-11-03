package es.upm.tfg.sifpyme.controller;

import es.upm.tfg.sifpyme.model.dao.EmpresaDAO;
import es.upm.tfg.sifpyme.model.entity.Empresa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador para gestionar las operaciones relacionadas con Empresa
 */
public class EmpresaController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmpresaController.class);
    private final EmpresaDAO empresaDAO;
    
    public EmpresaController() {
        this.empresaDAO = new EmpresaDAO();
    }
    
    /**
     * Guarda una nueva empresa en la base de datos
     * 
     * @param empresa La empresa a guardar
     * @return true si se guardó correctamente, false en caso contrario
     */
    public boolean guardarEmpresa(Empresa empresa) {
        try {
            logger.info("Guardando empresa: {}", empresa.getNombreComercial());
            
            // Validaciones adicionales del controlador
            if (!validarEmpresa(empresa)) {
                logger.warn("Validación de empresa fallida");
                return false;
            }
            
            // Si esta empresa será la por defecto, desactivar las demás
            if (empresa.getPorDefecto() != null && empresa.getPorDefecto()) {
                empresaDAO.desactivarEmpresasPorDefecto();
            }
            
            // Guardar la empresa
            Integer idGenerado = empresaDAO.insertar(empresa);
            
            if (idGenerado != null && idGenerado > 0) {
                logger.info("Empresa guardada exitosamente con ID: {}", idGenerado);
                return true;
            } else {
                logger.error("Error al guardar la empresa");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al guardar empresa", e);
            return false;
        }
    }
    
    /**
     * Actualiza una empresa existente
     * 
     * @param empresa La empresa a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarEmpresa(Empresa empresa) {
        try {
            logger.info("Actualizando empresa ID: {}", empresa.getIdEmpresa());
            
            if (!validarEmpresa(empresa)) {
                return false;
            }
            
            // Si esta empresa será la por defecto, desactivar las demás
            if (empresa.getPorDefecto() != null && empresa.getPorDefecto()) {
                empresaDAO.desactivarEmpresasPorDefecto();
            }
            
            boolean actualizado = empresaDAO.actualizar(empresa);
            
            if (actualizado) {
                logger.info("Empresa actualizada exitosamente");
            } else {
                logger.error("Error al actualizar la empresa");
            }
            
            return actualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar empresa", e);
            return false;
        }
    }
    
    /**
     * Obtiene todas las empresas
     * 
     * @return Lista de empresas
     */
    public List<Empresa> obtenerTodasLasEmpresas() {
        try {
            return empresaDAO.obtenerTodas();
        } catch (Exception e) {
            logger.error("Error al obtener empresas", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene la empresa por defecto
     * 
     * @return La empresa por defecto o null si no existe
     */
    public Empresa obtenerEmpresaPorDefecto() {
        try {
            return empresaDAO.obtenerPorDefecto();
        } catch (Exception e) {
            logger.error("Error al obtener empresa por defecto", e);
            return null;
        }
    }
    
    /**
     * Obtiene una empresa por su ID
     * 
     * @param id El ID de la empresa
     * @return La empresa encontrada o null
     */
    public Empresa obtenerEmpresaPorId(Integer id) {
        try {
            return empresaDAO.obtenerPorId(id);
        } catch (Exception e) {
            logger.error("Error al obtener empresa por ID", e);
            return null;
        }
    }
    
    /**
     * Valida los datos de una empresa
     * 
     * @param empresa La empresa a validar
     * @return true si es válida, false en caso contrario
     */
    private boolean validarEmpresa(Empresa empresa) {
        if (empresa == null) {
            logger.warn("Empresa es null");
            return false;
        }
        
        if (empresa.getNombreComercial() == null || empresa.getNombreComercial().trim().isEmpty()) {
            logger.warn("Nombre comercial vacío");
            return false;
        }
        
        if (empresa.getRazonSocial() == null || empresa.getRazonSocial().trim().isEmpty()) {
            logger.warn("Razón social vacía");
            return false;
        }
        
        if (empresa.getNif() == null || empresa.getNif().trim().isEmpty()) {
            logger.warn("NIF vacío");
            return false;
        }
        
        if (empresa.getDireccion() == null || empresa.getDireccion().trim().isEmpty()) {
            logger.warn("Dirección vacía");
            return false;
        }
        
        if (empresa.getCodigoPostal() == null || empresa.getCodigoPostal().trim().isEmpty()) {
            logger.warn("Código postal vacío");
            return false;
        }
        
        if (empresa.getCiudad() == null || empresa.getCiudad().trim().isEmpty()) {
            logger.warn("Ciudad vacía");
            return false;
        }
        
        if (empresa.getProvincia() == null || empresa.getProvincia().trim().isEmpty()) {
            logger.warn("Provincia vacía");
            return false;
        }
        
        return true;
    }
}