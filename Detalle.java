package com.cianmetalurgica.model;

public class Detalle {
    private Long idDetalle;
    private Long idPedido;
    private Long idMaterial;
    private String materialTipo; // Para mostrar en las tablas
    private Integer cantidad;
    private String dimensionesPieza;
    private Integer numeroCortes;
    private Double pesoPieza;
    
    public Detalle() {}
    
    public Detalle(Long idPedido, Long idMaterial, Integer cantidad, 
                   String dimensionesPieza, Integer numeroCortes, Double pesoPieza) {
        this.idPedido = idPedido;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad;
        this.dimensionesPieza = dimensionesPieza;
        this.numeroCortes = numeroCortes;
        this.pesoPieza = pesoPieza;
    }
    
    // Getters y Setters
    public Long getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }
    
    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    
    public Long getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }
    
    public String getMaterialTipo() { return materialTipo; }
    public void setMaterialTipo(String materialTipo) { this.materialTipo = materialTipo; }
    
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    
    public String getDimensionesPieza() { return dimensionesPieza; }
    public void setDimensionesPieza(String dimensionesPieza) { this.dimensionesPieza = dimensionesPieza; }
    
    public Integer getNumeroCortes() { return numeroCortes; }
    public void setNumeroCortes(Integer numeroCortes) { this.numeroCortes = numeroCortes; }
    
    public Double getPesoPieza() { return pesoPieza; }
    public void setPesoPieza(Double pesoPieza) { this.pesoPieza = pesoPieza; }
    
    @Override
    public String toString() {
        return materialTipo + " - Cant: " + cantidad;
    }

    public Object getPedido() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object getMaterial() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}