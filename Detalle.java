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

    // Nuevo campo: indicar si al finalizar debe descontarse del stock
    // Usamos Integer 0/1 para mapear directamente con la BD (TINYINT)
    private Integer descontarStock; // 1 = descontar, 0 = no descontar

    public Detalle() {
        // por defecto asumimos que descontar_stock = 1 (si la DB tiene default 1)
        this.descontarStock = 1;
    }

    public Detalle(Long idPedido, Long idMaterial, Integer cantidad,
                   String dimensionesPieza, Integer numeroCortes, Double pesoPieza) {
        this.idPedido = idPedido;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad;
        this.dimensionesPieza = dimensionesPieza;
        this.numeroCortes = numeroCortes;
        this.pesoPieza = pesoPieza;
        this.descontarStock = 1; // por defecto descontar
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

    public Integer getDescontarStock() { return descontarStock; }
    public void setDescontarStock(Integer descontarStock) { this.descontarStock = descontarStock; }

    @Override
    public String toString() {
        return "Detalle[" + idDetalle + "] material=" + materialTipo + " cantidad=" + cantidad;
    }

    // Si antes llamabas getPedido()/getMaterial(), dejalo lanzando UnsupportedOperationException
    public Object getPedido() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getMaterial() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
