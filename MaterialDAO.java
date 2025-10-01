package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Material;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public void save(Material material) throws SQLException {
        String sql = "INSERT INTO materiales (articulo, tipo, espesor, dimensiones, cantidad, proveedor) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, material.getArticulo());
            stmt.setString(2, material.getTipo());

            if (material.getEspesor() != null) {
                stmt.setDouble(3, material.getEspesor());
            } else {
                stmt.setNull(3, java.sql.Types.DOUBLE);
            }

            stmt.setString(4, material.getDimensiones());

            if (material.getCantidad() != null) {
                stmt.setInt(5, material.getCantidad());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            if (material.getProveedor() != null) {
                stmt.setString(6, material.getProveedor());
            } else {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    material.setIdMaterial(rs.getLong(1));
                }
            }
        }
    }

    public void update(Material material) throws SQLException {
        String sql = "UPDATE materiales SET articulo=?, tipo=?, espesor=?, dimensiones=?, cantidad=?, proveedor=? WHERE id_material=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, material.getArticulo());
            stmt.setString(2, material.getTipo());

            if (material.getEspesor() != null) {
                stmt.setDouble(3, material.getEspesor());
            } else {
                stmt.setNull(3, java.sql.Types.DOUBLE);
            }

            stmt.setString(4, material.getDimensiones());

            if (material.getCantidad() != null) {
                stmt.setInt(5, material.getCantidad());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            if (material.getProveedor() != null) {
                stmt.setString(6, material.getProveedor());
            } else {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }

            stmt.setLong(7, material.getIdMaterial());

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
        String sql = "SELECT * FROM materiales WHERE tipo LIKE ? OR articulo LIKE ? ORDER BY tipo";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + type + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

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
        material.setArticulo(rs.getString("articulo"));
        material.setTipo(rs.getString("tipo"));
        material.setEspesor(rs.getObject("espesor", Double.class));
        material.setDimensiones(rs.getString("dimensiones"));
        material.setCantidad(rs.getObject("cantidad", Integer.class));
        material.setProveedor(rs.getString("proveedor"));
        return material;
    }
}
