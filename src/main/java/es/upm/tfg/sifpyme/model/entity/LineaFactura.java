package es.upm.tfg.sifpyme.model.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa una línea de factura
 */
public class LineaFactura {
    
    private Integer idLinea;
    private String idFactura;
    private Integer idProducto;
    private String nombreProducto;  // NUEVO CAMPO
    private BigDecimal cantidad;
    private BigDecimal precioBase;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotalLinea;
    private BigDecimal porcentajeIva;
    private BigDecimal importeIva;
    private BigDecimal porcentajeRetencion;
    private BigDecimal importeRetencion;
    private BigDecimal totalLinea;
    private Integer numeroLinea;
    private transient int tempIndiceEditar = -1;
    
    // Relación con Producto 
    private Producto producto;
    
    // Constructores
    public LineaFactura() {
        this.descuento = BigDecimal.ZERO;
        this.porcentajeRetencion = BigDecimal.ZERO;
        this.importeRetencion = BigDecimal.ZERO;
    }
    
    public LineaFactura(String idFactura, Integer idProducto, String nombreProducto, // NUEVO PARÁMETRO
                       BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal porcentajeIva) {
        this();
        this.idFactura = idFactura;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;  // INICIALIZAR NUEVO CAMPO
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeIva = porcentajeIva;
    }
    
    // Getters y Setters
    public Integer getIdLinea() {
        return idLinea;
    }
    
    public void setIdLinea(Integer idLinea) {
        this.idLinea = idLinea;
    }
    
    public String getIdFactura() {
        return idFactura;
    }
    
    public void setIdFactura(String idFactura) {
        this.idFactura = idFactura;
    }
    
    public Integer getIdProducto() {
        return idProducto;
    }
    
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    
    // NUEVO GETTER Y SETTER PARA nombreProducto
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    public BigDecimal getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public BigDecimal getDescuento() {
        return descuento;
    }
    
    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }
    
    public BigDecimal getSubtotalLinea() {
        return subtotalLinea;
    }
    
    public void setSubtotalLinea(BigDecimal subtotalLinea) {
        this.subtotalLinea = subtotalLinea;
    }
    
    public BigDecimal getPorcentajeIva() {
        return porcentajeIva;
    }
    
    public void setPorcentajeIva(BigDecimal porcentajeIva) {
        this.porcentajeIva = porcentajeIva;
    }
    
    public BigDecimal getImporteIva() {
        return importeIva;
    }
    
    public void setImporteIva(BigDecimal importeIva) {
        this.importeIva = importeIva;
    }
    
    public BigDecimal getPorcentajeRetencion() {
        return porcentajeRetencion;
    }
    
    public void setPorcentajeRetencion(BigDecimal porcentajeRetencion) {
        this.porcentajeRetencion = porcentajeRetencion;
    }
    
    public BigDecimal getImporteRetencion() {
        return importeRetencion;
    }
    
    public void setImporteRetencion(BigDecimal importeRetencion) {
        this.importeRetencion = importeRetencion;
    }
    
    public BigDecimal getTotalLinea() {
        return totalLinea;
    }
    
    public void setTotalLinea(BigDecimal totalLinea) {
        this.totalLinea = totalLinea;
    }
    
    public Integer getNumeroLinea() {
        return numeroLinea;
    }
    
    public void setNumeroLinea(Integer numeroLinea) {
        this.numeroLinea = numeroLinea;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            this.idProducto = producto.getIdProducto();
            this.nombreProducto = producto.getNombre();  // ESTABLECER NOMBRE DESDE PRODUCTO
        }
    }
    
    // Métodos de utilidad
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaFactura that = (LineaFactura) o;
        return Objects.equals(idLinea, that.idLinea);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idLinea);
    }
    
    @Override
    public String toString() {
        return "Línea " + numeroLinea + ": " + nombreProducto + " - " + cantidad + " x " + precioUnitario;
    }

     public int getTempIndiceEditar() {
        return tempIndiceEditar;
    }
    
    public void setTempIndiceEditar(int tempIndiceEditar) {
        this.tempIndiceEditar = tempIndiceEditar;
    }
}