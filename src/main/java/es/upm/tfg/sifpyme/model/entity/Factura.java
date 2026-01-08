package es.upm.tfg.sifpyme.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa una factura
 */
public class Factura {
    
    private String idFactura;  // Ahora es String (ej: "2025-001")
    private Integer idEmpresa;
    private Integer idCliente;
    private LocalDate fechaEmision;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal totalIva;
    private BigDecimal totalRetencion;
    private BigDecimal total;
    
    // Relaciones
    private Empresa empresa;
    private Cliente cliente;
    private List<LineaFactura> lineas;
    
    // Constructores
    public Factura() {
        this.fechaEmision = LocalDate.now();
        this.subtotal = BigDecimal.ZERO;
        this.totalIva = BigDecimal.ZERO;
        this.totalRetencion = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.lineas = new ArrayList<>();
    }
    
    public Factura(String idFactura, Integer idEmpresa, Integer idCliente, String metodoPago) {
        this();
        this.idFactura = idFactura;
        this.idEmpresa = idEmpresa;
        this.idCliente = idCliente;
        this.metodoPago = metodoPago;
    }
    
    // Getters y Setters
    public String getIdFactura() {
        return idFactura;
    }
    
    public void setIdFactura(String idFactura) {
        this.idFactura = idFactura;
    }
    
    public Integer getIdEmpresa() {
        return idEmpresa;
    }
    
    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }
    
    public Integer getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
    
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }
    
    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTotalIva() {
        return totalIva;
    }
    
    public void setTotalIva(BigDecimal totalIva) {
        this.totalIva = totalIva;
    }
    
    public BigDecimal getTotalRetencion() {
        return totalRetencion;
    }
    
    public void setTotalRetencion(BigDecimal totalRetencion) {
        this.totalRetencion = totalRetencion;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public Empresa getEmpresa() {
        return empresa;
    }
    
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
        if (empresa != null) {
            this.idEmpresa = empresa.getIdEmpresa();
        }
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            this.idCliente = cliente.getIdCliente();
        }
    }
    
    public List<LineaFactura> getLineas() {
        return lineas;
    }
    
    public void setLineas(List<LineaFactura> lineas) {
        this.lineas = lineas;
    }
    
    // Método para agregar una línea
    public void addLinea(LineaFactura linea) {
        if (this.lineas == null) {
            this.lineas = new ArrayList<>();
        }
        this.lineas.add(linea);
        linea.setIdFactura(this.idFactura);
    }
    
    // Métodos de utilidad
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factura factura = (Factura) o;
        return Objects.equals(idFactura, factura.idFactura);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idFactura);
    }
    
    @Override
    public String toString() {
        return idFactura + " - " + fechaEmision;
    }
}