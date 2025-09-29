package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Material;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {
    
    public void save(Material material) throws SQLException {
        String sql = "INSERT INTO materiales (tipo, espesor, dimensiones, cantidad, proveedor) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, material.getTipo());
            stmt.setObject(2, material.getEspesor());
            stmt.setString(3, material.getDimensiones());
            stmt.setObject(4, material.getCantidad());
            stmt.setString(5, material.getProveedor());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    material.setIdMaterial(rs.getLong(1));
                }
            }
        }
    }
    
    public void update(Material material) throws SQLException {
        String sql = "UPDATE materiales SET tipo=?, espesor=?, dimensiones=?, cantidad=?, proveedor=? WHERE id_material=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, material.getTipo());
            stmt.setObject(2, material.getEspesor());
            stmt.setString(3, material.getDimensiones());
            stmt.setObject(4, material.getCantidad());
            stmt.setString(5, material.getProveedor());
            stmt.setLong(6, material.getIdMaterial());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM materiales WHERE id_material=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public Material findById(Long id) throws SQLException {
        String sql = "SELECT * FROM materiales WHERE id_material=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMaterial(rs);
                }
            }
        }
        return null;
    }
    
    public List<Material> findAll() throws SQLException {
        List<Material> materiales = new ArrayList<>();
        String sql = "SELECT * FROM materiales ORDER BY tipo";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                materiales.add(mapResultSetToMaterial(rs));
            }
        }
        return materiales;
    }
    
    public List<Material> findByType(String type) throws SQLException {
        List<Material> materiales = new ArrayList<>();
        String sql = "SELECT * FROM materiales WHERE tipo LIKE ? ORDER BY tipo";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + type + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    materiales.add(mapResultSetToMaterial(rs));
                }
            }
        }
        return materiales;
    }
    
    private Material mapResultSetToMaterial(ResultSet rs) throws SQLException {
        Material material = new Material();
        material.setIdMaterial(rs.getLong("id_material"));
        material.setTipo(rs.getString("tipo"));
        material.setEspesor(rs.getObject("espesor", Double.class));
        material.setDimensiones(rs.getString("dimensiones"));
        material.setCantidad(rs.getObject("cantidad", Integer.class));
        material.setProveedor(rs.getString("proveedor"));
        return material;
    }
}