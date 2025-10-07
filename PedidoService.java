package com.cianmetalurgica.service;

import com.cianmetalurgica.dao.PedidoDAO;
import com.cianmetalurgica.model.Pedido;
import com.cianmetalurgica.model.Detalle;
import java.time.LocalDate;
import java.util.List;

public class PedidoService {
    private PedidoDAO pedidoDAO = new PedidoDAO();
    
    

    public void savePedido(Pedido pedido) {
        if (pedido.getIdCliente() == null) {
            throw new IllegalArgumentException("El cliente (idCliente) es obligatorio");
        }
        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(LocalDate.now());
        }
        pedidoDAO.save(pedido);
    }

    public void savePedidoConDetalles(Pedido pedido, List<Detalle> detalles) {
        if (pedido.getIdCliente() == null) throw new IllegalArgumentException("El cliente (idCliente) es obligatorio");
        if (pedido.getFechaPedido() == null) pedido.setFechaPedido(LocalDate.now());
        pedidoDAO.saveWithDetalles(pedido, detalles);
    }

    public void updatePedido(Pedido pedido) {
        if (pedido.getIdPedido() == null) {
            throw new IllegalArgumentException("El ID del pedido es obligatorio para actualizar");
        }
        if (pedido.getIdCliente() == null) {
    throw new IllegalArgumentException("El cliente (idCliente) es obligatorio");
}

        pedidoDAO.update(pedido);
    }

    public void deletePedido(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del pedido es obligatorio");
        }
        pedidoDAO.delete(id);
    }

    public Pedido getPedidoById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del pedido es obligatorio");
        }
        return pedidoDAO.findById(id);
    }

    public List<Pedido> getAllPedidos() {
        return pedidoDAO.findAll();
    }

    public List<Pedido> getPedidosByCliente(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        return pedidoDAO.findByClientId(clientId);
    }

    public List<Pedido> searchPedidosByClienteName(String searchText) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
   
    

}
