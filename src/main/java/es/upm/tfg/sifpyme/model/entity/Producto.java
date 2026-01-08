package es.upm.tfg.sifpyme.model.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa un producto o servicio
 */
public class Producto {
    
    private Integer idProducto;
    private BigDecimal tipoIva;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private BigDecimal precioBase;
    private BigDecimal tipoRetencion;
    
    // Constructores
    public Producto() {
        this.tipoIva = new BigDecimal("21.00");
        this.tipoRetencion = BigDecimal.ZERO;
    }
    
    public Producto(Integer idProducto, BigDecimal tipoIva, String codigo, 
                    String nombre, BigDecimal precio, BigDecimal precioBase, 
                    BigDecimal tipoRetencion) {
        this.idProducto = idProducto;
        this.tipoIva = tipoIva != null ? tipoIva : new BigDecimal("21.00");
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.precioBase = precioBase;
        this.tipoRetencion = tipoRetencion != null ? tipoRetencion : BigDecimal.ZERO;
    }
    
    public Producto(String nombre, BigDecimal precio, BigDecimal tipoIva) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipoIva = tipoIva != null ? tipoIva : new BigDecimal("21.00");
        this.tipoRetencion = BigDecimal.ZERO;
    }
    
    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }
    
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    
    public BigDecimal getTipoIva() {
        return tipoIva;
    }
    
    public void setTipoIva(BigDecimal tipoIva) {
        this.tipoIva = tipoIva;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    
    public BigDecimal getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }
    
    public BigDecimal getTipoRetencion() {
        return tipoRetencion;
    }
    
    public void setTipoRetencion(BigDecimal tipoRetencion) {
        this.tipoRetencion = tipoRetencion;
    }
    
    // Método de utilidad para obtener el precio efectivo
    public BigDecimal getPrecioEfectivo() {
        return precio != null ? precio : precioBase;
    }
    
    // Métodos de utilidad
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(idProducto, producto.idProducto);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idProducto);
    }
    
    @Override
    public String toString() {
        return nombre + (codigo != null ? " (" + codigo + ")" : "");
    }
}