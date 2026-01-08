package es.upm.tfg.sifpyme.model.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa una empresa
 */
public class Empresa {

    private Integer idEmpresa;
    private String nombreComercial;
    private String razonSocial;
    private String nif;
    private String direccion;
    private String telefono;
    private String email;
    private BigDecimal tipoRetencionIrpf;
    private Boolean porDefecto;

    public Empresa() {
        this.tipoRetencionIrpf = new BigDecimal("15.00");
        this.porDefecto = false;
    }

    public Empresa(String nombreComercial, String razonSocial, String nif, String direccion) {
        this();
        this.nombreComercial = nombreComercial;
        this.razonSocial = razonSocial;
        this.nif = nif;
        this.direccion = direccion;
    }

    // Getters y Setters
    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
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

    public BigDecimal getTipoRetencionIrpf() {
        return tipoRetencionIrpf;
    }

    public void setTipoRetencionIrpf(BigDecimal tipoRetencionIrpf) {
        this.tipoRetencionIrpf = tipoRetencionIrpf;
    }

    public Boolean getPorDefecto() {
        return porDefecto;
    }

    public void setPorDefecto(Boolean porDefecto) {
        this.porDefecto = porDefecto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        return Objects.equals(idEmpresa, empresa.idEmpresa) &&
                Objects.equals(nif, empresa.nif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEmpresa, nif);
    }

    @Override
    public String toString() {
        return nombreComercial + " (" + nif + ")";
    }
}