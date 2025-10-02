package com.cianmetalurgica.model;

import java.time.LocalDate;

public class Pedido {
    private Long idPedido;
    private Long idCliente;
    private String clienteNombre; // Para mostrar en las tablas
    private LocalDate fechaPedido;
    private LocalDate fechaEntregaEstimada;
    private Double kiloCantidad;
    private String estadoPedido;

    public Pedido() {}

    public Pedido(Long idCliente, LocalDate fechaPedido, LocalDate fechaEntregaEstimada,
                  Double kiloCantidad) {
        this.idCliente = idCliente;
        this.fechaPedido = fechaPedido;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.kiloCantidad = kiloCantidad;
    }

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

    public Double getKiloCantidad() { return kiloCantidad; }
    public void setKiloCantidad(Double kiloCantidad) { this.kiloCantidad = kiloCantidad; }

    @Override
    public String toString() {
        if (clienteNombre != null) {
            return "Pedido #" + idPedido + " - " + clienteNombre;
        }
        return "Pedido #" + idPedido;
    }

    public String getEstadoPedido() {
    return estadoPedido;
}
public void setEstadoPedido(String estadoPedido) {
    this.estadoPedido = estadoPedido;
}
    }

