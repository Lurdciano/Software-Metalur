package com.cianmetalurgica.service;

import com.cianmetalurgica.dao.DetalleDAO;
import com.cianmetalurgica.model.Detalle;
import java.sql.SQLException;
import java.util.List;

public class DetalleService {
    private DetalleDAO detalleDAO = new DetalleDAO();
    
public void saveDetalle(Detalle detalle) throws SQLException {
    if (detalle.getIdPedido() == null) {
        throw new IllegalArgumentException("El ID del pedido es obligatorio");
    }
    if (detalle.getIdMaterial() == null) {
        throw new IllegalArgumentException("El ID del material es obligatorio");
    }
    if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
        throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
    }
    detalleDAO.save(detalle); // puede lanzar SQLException
}

public void updateDetalle(Detalle detalle) throws SQLException {
    if (detalle.getIdDetalle() == null) {
        throw new IllegalArgumentException("El ID del detalle es obligatorio para actualizar");
    }
    if (detalle.getIdPedido() == null) {
        throw new IllegalArgumentException("El ID del pedido es obligatorio");
    }
    if (detalle.getIdMaterial() == null) {
        throw new IllegalArgumentException("El ID del material es obligatorio");
    }
    if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
        throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
    }
    detalleDAO.update(detalle); // puede lanzar SQLException
}

    
    
    public void deleteDetalle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del detalle es obligatorio");
        }
        detalleDAO.delete(id);
    }
    
    public Detalle getDetalleById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del detalle es obligatorio");
        }
        return detalleDAO.findById(id);
    }
    
    public List<Detalle> getAllDetalles() {
        return detalleDAO.findAll();
    }
    
    public List<Detalle> getDetallesByPedido(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("El ID del pedido es obligatorio");
        }
        return detalleDAO.findByPedidoId(pedidoId);
    }
}
