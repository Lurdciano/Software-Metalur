package com.cianmetalurgica.model;

public class Material {
    private Long idMaterial;
    private String tipo;
    private Double espesor;
    private String dimensiones;
    private Integer cantidad;
    private String proveedor;
    
    public Material() {}
    
    public Material(String tipo, Double espesor, String dimensiones, Integer cantidad, String proveedor) {
        this.tipo = tipo;
        this.espesor = espesor;
        this.dimensiones = dimensiones;
        this.cantidad = cantidad;
        this.proveedor = proveedor;
    }
    
    // Getters y Setters
    public Long getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public Double getEspesor() { return espesor; }
    public void setEspesor(Double espesor) { this.espesor = espesor; }
    
    public String getDimensiones() { return dimensiones; }
    public void setDimensiones(String dimensiones) { this.dimensiones = dimensiones; }
    
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    @Override
    public String toString() {
        return tipo + " - " + (dimensiones != null ? dimensiones : "");
    }
}