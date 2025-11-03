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
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String pais;
    private String telefono;
    private String email;
    private String web;
    private BigDecimal tipoRetencionIrpf;
    private Boolean activo;
    private Boolean porDefecto;

    public Empresa() {
        this.pais = "España";
        this.tipoRetencionIrpf = new BigDecimal("15.00");
        this.activo = true;
        this.porDefecto = false;
    }

    public Empresa(String nombreComercial, String razonSocial, String nif,
            String direccion, String codigoPostal, String ciudad, String provincia) {
        this();
        this.nombreComercial = nombreComercial;
        this.razonSocial = razonSocial;
        this.nif = nif;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.provincia = provincia;
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

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
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

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public BigDecimal getTipoRetencionIrpf() {
        return tipoRetencionIrpf;
    }

    public void setTipoRetencionIrpf(BigDecimal tipoRetencionIrpf) {
        this.tipoRetencionIrpf = tipoRetencionIrpf;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getPorDefecto() {
        return porDefecto;
    }

    public void setPorDefecto(Boolean porDefecto) {
        this.porDefecto = porDefecto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
        return "Empresa{" +
                "idEmpresa=" + idEmpresa +
                ", nombreComercial='" + nombreComercial + '\'' +
                ", nif='" + nif + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", activo=" + activo +
                ", porDefecto=" + porDefecto +
                '}';
    }

}
