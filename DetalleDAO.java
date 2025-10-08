package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Detalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleDAO {

    public DetalleDAO() {}

public List<Detalle> findByPedidoId(long pedidoId) {
    List<Detalle> list = new ArrayList<>();
    String sql = "SELECT * FROM detalles WHERE id_pedido = ?";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, pedidoId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDetalle(rs));
            }
        }
    } catch (SQLException ex) {
        throw new RuntimeException("Error en findByPedidoId Detalles: " + ex.getMessage(), ex);
    }
    return list;



}

    

public void save(Detalle d) throws SQLException {
    String sql = "INSERT INTO detalles (id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza, descontar_stock) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        if (d.getIdPedido() != null) ps.setLong(1, d.getIdPedido()); else ps.setNull(1, Types.BIGINT);
        if (d.getIdMaterial() != null) ps.setLong(2, d.getIdMaterial()); else ps.setNull(2, Types.BIGINT);
        ps.setString(3, d.getMaterialTipo());
        if (d.getCantidad() != null) ps.setInt(4, d.getCantidad()); else ps.setNull(4, Types.INTEGER);
        ps.setString(5, d.getDimensionesPieza());
        if (d.getNumeroCortes() != null) ps.setInt(6, d.getNumeroCortes()); else ps.setNull(6, Types.INTEGER);
        if (d.getPesoPieza() != null) ps.setDouble(7, d.getPesoPieza()); else ps.setNull(7, Types.DOUBLE);
        // descontar_stock como entero
        if (d.getDescontarStock() != null) ps.setInt(8, d.getDescontarStock()); else ps.setInt(8, 1);

        ps.executeUpdate();
        try (ResultSet gk = ps.getGeneratedKeys()) {
            if (gk.next()) {
                d.setIdDetalle(gk.getLong(1));
            }
        }
    }
}


public void update(Detalle d) throws SQLException {
    String sql = "UPDATE detalles SET id_pedido = ?, id_material = ?, material_tipo = ?, cantidad = ?, dimensiones_pieza = ?, numero_cortes = ?, peso_pieza = ?, descontar_stock = ? " +
                 "WHERE id_detalle = ?";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        if (d.getIdPedido() != null) ps.setLong(1, d.getIdPedido()); else ps.setNull(1, Types.BIGINT);
        if (d.getIdMaterial() != null) ps.setLong(2, d.getIdMaterial()); else ps.setNull(2, Types.BIGINT);
        ps.setString(3, d.getMaterialTipo());
        if (d.getCantidad() != null) ps.setInt(4, d.getCantidad()); else ps.setNull(4, Types.INTEGER);
        ps.setString(5, d.getDimensionesPieza());
        if (d.getNumeroCortes() != null) ps.setInt(6, d.getNumeroCortes()); else ps.setNull(6, Types.INTEGER);
        if (d.getPesoPieza() != null) ps.setDouble(7, d.getPesoPieza()); else ps.setNull(7, Types.DOUBLE);
        if (d.getDescontarStock() != null) ps.setInt(8, d.getDescontarStock()); else ps.setInt(8, 1);
        ps.setLong(9, d.getIdDetalle());
        ps.executeUpdate();
    }
}


    public void delete(Long id) {
        String sql = "DELETE FROM detalles WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error eliminando detalle: " + ex.getMessage(), ex);
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
            throw new RuntimeException("Error en findById Detalle: " + ex.getMessage(), ex);
        }
    }

    public List<Detalle> findAll() {
        List<Detalle> lista = new ArrayList<>();
        String sql = "SELECT id_detalle, id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza FROM detalles ORDER BY id_detalle";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToDetalle(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findAll Detalle: " + ex.getMessage(), ex);
        }
    }

private Detalle mapResultSetToDetalle(ResultSet rs) throws SQLException {
    Detalle d = new Detalle();
    d.setIdDetalle(rs.getLong("id_detalle"));
    long idPedido = rs.getLong("id_pedido");
    if (!rs.wasNull()) d.setIdPedido(idPedido);

    long idMat = rs.getLong("id_material");
    if (!rs.wasNull()) d.setIdMaterial(idMat);

    d.setMaterialTipo(rs.getString("material_tipo"));
    int cant = rs.getInt("cantidad");
    if (!rs.wasNull()) d.setCantidad(cant);
    d.setDimensionesPieza(rs.getString("dimensiones_pieza"));
    int cortes = rs.getInt("numero_cortes");
    if (!rs.wasNull()) d.setNumeroCortes(cortes);
    double peso = rs.getDouble("peso_pieza");
    if (!rs.wasNull()) d.setPesoPieza(peso);

    // leer la columna descontar_stock como int (0/1) y mapear a Integer
    try {
        int desc = rs.getInt("descontar_stock");
        if (!rs.wasNull()) d.setDescontarStock(desc);
        else d.setDescontarStock(1); // fallback
    } catch (SQLException ex) {
        // si la columna no existe (versión antigua), asumimos default = 1
        d.setDescontarStock(1);
    }

    return d;
}
}
