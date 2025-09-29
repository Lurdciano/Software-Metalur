package com.cianmetalurgica.service;

import com.cianmetalurgica.dao.ClienteDAO;
import com.cianmetalurgica.model.Cliente;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {
    private ClienteDAO clienteDAO = new ClienteDAO();
    
    public void saveCliente(Cliente cliente) throws SQLException {
        if (cliente.getNombreRazonSocial() == null || cliente.getNombreRazonSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre/razón social es obligatorio");
        }
        if (cliente.getCuitDni() == null || cliente.getCuitDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El CUIT/DNI es obligatorio");
        }
        clienteDAO.save(cliente);
    }
    
    public void updateCliente(Cliente cliente) throws SQLException {
        if (cliente.getIdCliente() == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio para actualizar");
        }
        if (cliente.getNombreRazonSocial() == null || cliente.getNombreRazonSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre/razón social es obligatorio");
        }
        if (cliente.getCuitDni() == null || cliente.getCuitDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El CUIT/DNI es obligatorio");
        }
        clienteDAO.update(cliente);
    }
    
    public void deleteCliente(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        clienteDAO.delete(id);
    }
    
    public Cliente getClienteById(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        return clienteDAO.findById(id);
    }
    
    public List<Cliente> getAllClientes() throws SQLException {
        return clienteDAO.findAll();
    }
    
    public List<Cliente> searchClientesByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return getAllClientes();
        }
        return clienteDAO.findByName(name.trim());
    }

    public List<Cliente> searchClientesByNombreRazonSocial(String searchText) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}