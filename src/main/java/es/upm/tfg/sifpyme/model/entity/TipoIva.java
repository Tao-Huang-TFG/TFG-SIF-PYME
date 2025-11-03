package es.upm.tfg.sifpyme.model.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa los tipos de IVA aplicables
 */
public class TipoIva {
    
    private Integer idTipoIva;
    private String nombre;
    private BigDecimal porcentaje;
    
    // Constructores
    public TipoIva() {
    }
    
    public TipoIva(Integer idTipoIva, String nombre, BigDecimal porcentaje) {
        this.idTipoIva = idTipoIva;
        this.nombre = nombre;
        this.porcentaje = porcentaje;
    }
    
    public TipoIva(String nombre, BigDecimal porcentaje) {
        this.nombre = nombre;
        this.porcentaje = porcentaje;
    }
    
    // Getters y Setters
    public Integer getIdTipoIva() {
        return idTipoIva;
    }
    
    public void setIdTipoIva(Integer idTipoIva) {
        this.idTipoIva = idTipoIva;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public BigDecimal getPorcentaje() {
        return porcentaje;
    }
    
    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
    
    // Métodos de utilidad
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoIva tipoIva = (TipoIva) o;
        return Objects.equals(idTipoIva, tipoIva.idTipoIva);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idTipoIva);
    }
    
    @Override
    public String toString() {
        return nombre + " (" + porcentaje + "%)";
    }
}