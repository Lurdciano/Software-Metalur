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

    /**
     * Cambia el stock (cantidad) del material sumando 'delta' (delta puede ser negativo).
     * Realiza la operación en una transacción: lee -> calcula -> update -> commit
     * Lanza RuntimeException si falla.
     */
    public void changeStock(Long idMaterial, int delta) {
        String selectSql = "SELECT cantidad FROM materiales WHERE id_material = ?";
        String updateSql = "UPDATE materiales SET cantidad = ? WHERE id_material = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Integer current = null;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setLong(1, idMaterial);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getObject("cantidad", Integer.class);
                    } else {
                        throw new SQLException("Material no encontrado con id: " + idMaterial);
                    }
                }
            }

            int curVal = current != null ? current.intValue() : 0;
            int newVal = curVal + delta;
            if (newVal < 0) {
                // decidir política: impedir stock negativo
                throw new IllegalArgumentException("Stock insuficiente para material id=" + idMaterial + ". Disponible: " + curVal + ", requerido: " + (-delta));
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                ps2.setInt(1, newVal);
                ps2.setLong(2, idMaterial);
                ps2.executeUpdate();
            }

            conn.commit();
        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Error en MaterialDAO.changeStock: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
