package es.upm.tfg.sifpyme.model.entity;

import java.util.Objects;

/**
 * Entidad que representa un cliente
 */
public class Cliente {
    
    private Integer idCliente;
    private String nombreFiscal;
    private String nif;
    private String direccion;
    private String telefono;
    private String email;
    
    // Constructores
    public Cliente() {
    }
    
    public Cliente(Integer idCliente, String nombreFiscal, String nif, 
                   String direccion, String telefono, String email) {
        this.idCliente = idCliente;
        this.nombreFiscal = nombreFiscal;
        this.nif = nif;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }
    
    public Cliente(String nombreFiscal, String nif) {
        this.nombreFiscal = nombreFiscal;
        this.nif = nif;
    }
    
    // Getters y Setters
    public Integer getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
    
    public String getNombreFiscal() {
        return nombreFiscal;
    }
    
    public void setNombreFiscal(String nombreFiscal) {
        this.nombreFiscal = nombreFiscal;
    }
    
    public String getNif() {
        return nif;
    }
    
    public void setNif(String nif) {
        this.nif = nif;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    // Métodos de utilidad
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(idCliente, cliente.idCliente);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idCliente);
    }
    
    @Override
    public String toString() {
        return nombreFiscal + " (" + nif + ")";
    }
}