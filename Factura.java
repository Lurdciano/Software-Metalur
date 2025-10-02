package com.cianmetalurgica.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private Long idFactura;
    private Long idPedido;
    private LocalDate fechaEmision;
    private Double total;
    private String nroFactura;
    private String observaciones;

    // detalles (no se persisten automáticamente, se pasan al DAO)
    private List<FacturaDetalle> detalles = new ArrayList<>();

    public Factura() {}

    public Long getIdFactura() { return idFactura; }
    public void setIdFactura(Long idFactura) { this.idFactura = idFactura; }

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getNroFactura() { return nroFactura; }
    public void setNroFactura(String nroFactura) { this.nroFactura = nroFactura; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<FacturaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<FacturaDetalle> detalles) { this.detalles = detalles; }

    public void addDetalle(FacturaDetalle detalle) {
        this.detalles.add(detalle);
    }
}
