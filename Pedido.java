package com.cianmetalurgica.model;

import java.time.LocalDate;

public class Pedido {
    private Long idPedido;
    private Long idCliente;
    private String clienteNombre; // Para mostrar en las tablas
    private LocalDate fechaPedido;
    private LocalDate fechaEntregaEstimada;
    private String estadoPedido;
    private String formaCobro;
    private Double kiloCantidad;
    
    public Pedido() {}
    
    public Pedido(Long idCliente, LocalDate fechaPedido, LocalDate fechaEntregaEstimada, 
                  String estadoPedido, String formaCobro, Double kiloCantidad) {
        this.idCliente = idCliente;
        this.fechaPedido = fechaPedido;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.estadoPedido = estadoPedido;
        this.formaCobro = formaCobro;
        this.kiloCantidad = kiloCantidad;
    }
    
    // Getters y Setters
    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }
    
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    
    public LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDate fechaPedido) { this.fechaPedido = fechaPedido; }
    
    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }
    
    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }
    
    public String getFormaCobro() { return formaCobro; }
    public void setFormaCobro(String formaCobro) { this.formaCobro = formaCobro; }
    
    public Double getKiloCantidad() { return kiloCantidad; }
    public void setKiloCantidad(Double kiloCantidad) { this.kiloCantidad = kiloCantidad; }
    
    @Override
    public String toString() {
        return "Pedido #" + idPedido + " - " + clienteNombre;
    }

// Compatibilidad: devuelve el nombre del cliente (si deseas otro comportamiento, ajusta)
public Object getCliente() {
    return this.clienteNombre;
}
}