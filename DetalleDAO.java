package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Detalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'detalles'
 * Asegurate que la tabla en la BD tenga las columnas:
 * id_detalle, id_pedido, id_material, material_tipo,
 * cantidad, dimensiones_pieza, numero_cortes, peso_pieza
 */
public class DetalleDAO {

    public DetalleDAO() {}

    public List<Detalle> findByPedidoId(Long pedidoId) {
        String sql = "SELECT id_detalle, id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza " +
                     "FROM detalles WHERE id_pedido = ? ORDER BY id_detalle";
        List<Detalle> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Detalle d = mapResultSetToDetalle(rs);
                    lista.add(d);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en DetalleDAO.findByPedidoId: " + ex.getMessage(), ex);
        }
    }

    public void save(Connection conn, Detalle detalle) throws SQLException {
    String sql = "INSERT INTO detalles (id_pedido, id_material, cantidad, dimensiones_pieza, numero_cortes, peso_pieza) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setLong(1, detalle.getIdPedido());
        ps.setObject(2, detalle.getIdMaterial());
        ps.setObject(3, detalle.getCantidad());
        ps.setString(4, detalle.getDimensionesPieza());
        if (detalle.getNumeroCortes() != null) ps.setInt(5, detalle.getNumeroCortes()); else ps.setNull(5, Types.INTEGER);
        if (detalle.getPesoPieza() != null) ps.setDouble(6, detalle.getPesoPieza()); else ps.setNull(6, Types.DOUBLE);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) detalle.setIdDetalle(rs.getLong(1));
        }
    }
}


    public void update(Detalle detalle) {
        String sql = "UPDATE detalles SET id_pedido = ?, id_material = ?, material_tipo = ?, cantidad = ?, dimensiones_pieza = ?, numero_cortes = ?, peso_pieza = ? " +
                     "WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (detalle.getIdPedido() != null) ps.setLong(1, detalle.getIdPedido()); else ps.setNull(1, Types.BIGINT);
            if (detalle.getIdMaterial() != null) ps.setLong(2, detalle.getIdMaterial()); else ps.setNull(2, Types.BIGINT);
            ps.setString(3, detalle.getMaterialTipo());
            if (detalle.getCantidad() != null) ps.setInt(4, detalle.getCantidad()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, detalle.getDimensionesPieza());
            if (detalle.getNumeroCortes() != null) ps.setInt(6, detalle.getNumeroCortes()); else ps.setNull(6, Types.INTEGER);
            if (detalle.getPesoPieza() != null) ps.setDouble(7, detalle.getPesoPieza()); else ps.setNull(7, Types.DOUBLE);
            ps.setLong(8, detalle.getIdDetalle());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error en DetalleDAO.update: " + ex.getMessage(), ex);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM detalles WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error en DetalleDAO.delete: " + ex.getMessage(), ex);
        }
    }

    public void deleteByPedidoId(Long pedidoId) {
        String sql = "DELETE FROM detalles WHERE id_pedido = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pedidoId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error en DetalleDAO.deleteByPedidoId: " + ex.getMessage(), ex);
        }
    }

    public Detalle findById(Long id) {
        String sql = "SELECT id_detalle, id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza " +
                     "FROM detalles WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToDetalle(rs);
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en DetalleDAO.findById: " + ex.getMessage(), ex);
        }
    }

    public List<Detalle> findAll() {
        String sql = "SELECT id_detalle, id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza FROM detalles";
        List<Detalle> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapResultSetToDetalle(rs));
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en DetalleDAO.findAll: " + ex.getMessage(), ex);
        }
    }

    private Detalle mapResultSetToDetalle(ResultSet rs) throws SQLException {
        Detalle d = new Detalle();
        d.setIdDetalle(rs.getLong("id_detalle"));

        Long idPedido = rs.getLong("id_pedido");
        if (rs.wasNull()) idPedido = null;
        d.setIdPedido(idPedido);

        Long idMaterial = rs.getLong("id_material");
        if (rs.wasNull()) idMaterial = null;
        d.setIdMaterial(idMaterial);

        d.setMaterialTipo(rs.getString("material_tipo"));

        int cantidad = rs.getInt("cantidad");
        if (rs.wasNull()) d.setCantidad(null); else d.setCantidad(cantidad);

        d.setDimensionesPieza(rs.getString("dimensiones_pieza"));

        int cortes = rs.getInt("numero_cortes");
        if (rs.wasNull()) d.setNumeroCortes(null); else d.setNumeroCortes(cortes);

        double peso = rs.getDouble("peso_pieza");
        if (rs.wasNull()) d.setPesoPieza(null); else d.setPesoPieza(peso);

        return d;
    }

    void saveWithConnection(Detalle d, Connection conn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void save(Detalle d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
