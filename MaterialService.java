package com.cianmetalurgica.service;

import com.cianmetalurgica.dao.MaterialDAO;
import com.cianmetalurgica.model.Material;
import java.sql.SQLException;
import java.util.List;

public class MaterialService {
    private MaterialDAO materialDAO = new MaterialDAO();
    
    public void saveMaterial(Material material) throws SQLException {
        if (material.getTipo() == null || material.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de material es obligatorio");
        }
        materialDAO.save(material);
    }
    
    public void updateMaterial(Material material) throws SQLException {
        if (material.getIdMaterial() == null) {
            throw new IllegalArgumentException("El ID del material es obligatorio para actualizar");
        }
        if (material.getTipo() == null || material.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de material es obligatorio");
        }
        materialDAO.update(material);
    }
    
    public void deleteMaterial(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El ID del material es obligatorio");
        }
        materialDAO.delete(id);
    }
    
    public Material getMaterialById(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El ID del material es obligatorio");
        }
        return materialDAO.findById(id);
    }
    
    public List<Material> getAllMateriales() throws SQLException {
        return materialDAO.findAll();
    }
    
    public List<Material> searchMaterialesByType(String type) throws SQLException {
        if (type == null || type.trim().isEmpty()) {
            return getAllMateriales();
        }
        return materialDAO.findByType(type.trim());
    }
}