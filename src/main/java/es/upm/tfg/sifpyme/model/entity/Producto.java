package es.upm.tfg.sifpyme.model.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa un producto o servicio
 */
public class Producto {
    
    private Integer idProducto;
    private Integer idTipoIva;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioBase;
    private BigDecimal tipoRetencion;
    
    // Relación con TipoIva (opcional, para facilitar el uso)
    private TipoIva tipoIva;
    
    // Constructores
    public Producto() {
        this.tipoRetencion = BigDecimal.ZERO;
    }
    
    public Producto(Integer idProducto, Integer idTipoIva, String codigo, 
                    String nombre, String descripcion, BigDecimal precio, 
                    BigDecimal precioBase, BigDecimal tipoRetencion) {
        this.idProducto = idProducto;
        this.idTipoIva = idTipoIva;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioBase = precioBase;
        this.tipoRetencion = tipoRetencion != null ? tipoRetencion : BigDecimal.ZERO;
    }
    
    public Producto(String nombre, BigDecimal precio, Integer idTipoIva) {
        this.nombre = nombre;
        this.precio = precio;
        this.idTipoIva = idTipoIva;
        this.tipoRetencion = BigDecimal.ZERO;
    }
    
    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }
    
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    
    public Integer getIdTipoIva() {
        return idTipoIva;
    }
    
    public void setIdTipoIva(Integer idTipoIva) {
        this.idTipoIva = idTipoIva;
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
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
    
    public TipoIva getTipoIva() {
        return tipoIva;
    }
    
    public void setTipoIva(TipoIva tipoIva) {
        this.tipoIva = tipoIva;
        if (tipoIva != null) {
            this.idTipoIva = tipoIva.getIdTipoIva();
        }
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