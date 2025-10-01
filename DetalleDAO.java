package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Detalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleDAO {

    // Método usado por operaciones independientes (abre su propia conexión)
    public List<Detalle> findByPedidoId(Long pedidoId) {
        List<Detalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalles WHERE id_pedido = ? ORDER BY id_detalle";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToDetalle(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findByPedidoId Detalle: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public void save(Detalle detalle) {
        String sql = "INSERT INTO detalles (id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, detalle.getIdPedido());
            if (detalle.getIdMaterial() != null) ps.setLong(2, detalle.getIdMaterial()); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, detalle.getMaterialTipo());
            if (detalle.getCantidad() != null) ps.setObject(4, detalle.getCantidad()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, detalle.getDimensionesPieza());
            if (detalle.getNumeroCortes() != null) ps.setObject(6, detalle.getNumeroCortes()); else ps.setNull(6, java.sql.Types.INTEGER);
            if (detalle.getPesoPieza() != null) ps.setDouble(7, detalle.getPesoPieza()); else ps.setNull(7, java.sql.Types.DOUBLE);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) detalle.setIdDetalle(rs.getLong(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error en save Detalle: " + ex.getMessage(), ex);
        }
    }

    public void update(Detalle detalle) {
        String sql = "UPDATE detalles SET id_pedido=?, id_material=?, material_tipo=?, cantidad=?, dimensiones_pieza=?, numero_cortes=?, peso_pieza=? WHERE id_detalle=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, detalle.getIdPedido());
            if (detalle.getIdMaterial() != null) ps.setLong(2, detalle.getIdMaterial()); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, detalle.getMaterialTipo());
            if (detalle.getCantidad() != null) ps.setObject(4, detalle.getCantidad()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, detalle.getDimensionesPieza());
            if (detalle.getNumeroCortes() != null) ps.setObject(6, detalle.getNumeroCortes()); else ps.setNull(6, java.sql.Types.INTEGER);
            if (detalle.getPesoPieza() != null) ps.setDouble(7, detalle.getPesoPieza()); else ps.setNull(7, java.sql.Types.DOUBLE);
            ps.setLong(8, detalle.getIdDetalle());

            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error en update Detalle: " + ex.getMessage(), ex);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM detalles WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error en delete Detalle: " + ex.getMessage(), ex);
        }
    }

    public Detalle findById(Long id) {
        String sql = "SELECT * FROM detalles WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToDetalle(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findById Detalle: " + ex.getMessage(), ex);
        }
        return null;
    }

    public List<Detalle> findAll() {
        List<Detalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalles ORDER BY id_detalle";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapResultSetToDetalle(rs));
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findAll Detalles: " + ex.getMessage(), ex);
        }
        return lista;
    }

    // Métodos usados internamente por transacciones: aceptan una Connection existente
    void saveWithConnection(Detalle detalle, Connection conn) throws SQLException {
        String sql = "INSERT INTO detalles (id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, detalle.getIdPedido());
            if (detalle.getIdMaterial() != null) ps.setLong(2, detalle.getIdMaterial()); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, detalle.getMaterialTipo());
            if (detalle.getCantidad() != null) ps.setObject(4, detalle.getCantidad()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, detalle.getDimensionesPieza());
            if (detalle.getNumeroCortes() != null) ps.setObject(6, detalle.getNumeroCortes()); else ps.setNull(6, java.sql.Types.INTEGER);
            if (detalle.getPesoPieza() != null) ps.setDouble(7, detalle.getPesoPieza()); else ps.setNull(7, java.sql.Types.DOUBLE);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) detalle.setIdDetalle(rs.getLong(1));
            }
        }
    }

    private Detalle mapResultSetToDetalle(ResultSet rs) throws SQLException {
        Detalle d = new Detalle();
        d.setIdDetalle(rs.getLong("id_detalle"));
        d.setIdPedido(rs.getLong("id_pedido"));
        long idMat = rs.getLong("id_material");
        if (rs.wasNull()) d.setIdMaterial(null); else d.setIdMaterial(idMat);
        d.setMaterialTipo(rs.getString("material_tipo"));
        d.setCantidad(rs.getObject("cantidad", Integer.class));
        d.setDimensionesPieza(rs.getString("dimensiones_pieza"));
        d.setNumeroCortes(rs.getObject("numero_cortes", Integer.class));
        d.setPesoPieza(rs.getObject("peso_pieza", Double.class));
        return d;
    }
}
