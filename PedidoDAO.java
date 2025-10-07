package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Pedido;
import com.cianmetalurgica.model.Detalle;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public PedidoDAO() {}

    /**
     * Lista pedidos (mínimo): id_pedido, id_cliente, nombre_razon_social (cliente), fecha_pedido
     * Ajustado para no requerir columnas opcionales que tu BD no tenga.
     */
    public List<Pedido> findAll() {
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, p.fecha_pedido " +
                     "FROM pedidos p " +
                     "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente " +
                     "ORDER BY p.fecha_pedido DESC";

        List<Pedido> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToPedido(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findAll Pedidos: " + ex.getMessage(), ex);
        }
    }

    public List<Pedido> findByClienteName(String name) {
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, p.fecha_pedido " +
                     "FROM pedidos p " +
                     "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente " +
                     "WHERE c.nombre_razon_social LIKE ? " +
                     "ORDER BY p.fecha_pedido DESC";

        List<Pedido> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPedido(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findByClienteName: " + ex.getMessage(), ex);
        }
    }

    public Pedido findById(Long id) {
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, p.fecha_pedido " +
                     "FROM pedidos p " +
                     "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente " +
                     "WHERE p.id_pedido = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findById Pedido: " + ex.getMessage(), ex);
        }
    }

    /**
     * Guarda pedido y sus detalles en una sola transacción.
     * Inserta en la tabla pedidos únicamente las columnas mínimas: id_cliente, fecha_pedido.
     * Luego inserta los detalles (tabla detalles). Si algo falla, hace rollback.
     */
    public void saveWithDetails(Pedido pedido, List<Detalle> detalles) {
        String sqlInsertPedido = "INSERT INTO pedidos (id_cliente, fecha_pedido) VALUES (?, ?)";
        String sqlInsertDetalle = "INSERT INTO detalles (id_pedido, id_material, material_tipo, cantidad, dimensiones_pieza, numero_cortes, peso_pieza) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert pedido
            try (PreparedStatement psPedido = conn.prepareStatement(sqlInsertPedido, Statement.RETURN_GENERATED_KEYS)) {
                if (pedido.getIdCliente() != null) psPedido.setLong(1, pedido.getIdCliente());
                else psPedido.setNull(1, Types.BIGINT);

                if (pedido.getFechaPedido() != null) psPedido.setDate(2, Date.valueOf(pedido.getFechaPedido()));
                else psPedido.setNull(2, Types.DATE);

                int affected = psPedido.executeUpdate();
                if (affected == 0) throw new SQLException("Insert pedido falló, no se creó ninguna fila.");

                try (ResultSet gk = psPedido.getGeneratedKeys()) {
                    if (gk.next()) pedido.setIdPedido(gk.getLong(1));
                    else throw new SQLException("Insert pedido falló: no se obtuvo ID generado.");
                }
            }

            // Insert detalles
            if (detalles != null && !detalles.isEmpty()) {
                try (PreparedStatement psDet = conn.prepareStatement(sqlInsertDetalle, Statement.RETURN_GENERATED_KEYS)) {
                    for (Detalle d : detalles) {
                        if (pedido.getIdPedido() != null) psDet.setLong(1, pedido.getIdPedido());
                        else psDet.setNull(1, Types.BIGINT);

                        if (d.getIdMaterial() != null) psDet.setLong(2, d.getIdMaterial()); else psDet.setNull(2, Types.BIGINT);
                        psDet.setString(3, d.getMaterialTipo());
                        if (d.getCantidad() != null) psDet.setInt(4, d.getCantidad()); else psDet.setNull(4, Types.INTEGER);
                        psDet.setString(5, d.getDimensionesPieza());
                        if (d.getNumeroCortes() != null) psDet.setInt(6, d.getNumeroCortes()); else psDet.setNull(6, Types.INTEGER);
                        if (d.getPesoPieza() != null) psDet.setDouble(7, d.getPesoPieza()); else psDet.setNull(7, Types.DOUBLE);

                        int af = psDet.executeUpdate();
                        if (af == 0) throw new SQLException("Insert detalle falló, no se creó ninguna fila.");

                        try (ResultSet gk = psDet.getGeneratedKeys()) {
                            if (gk.next()) d.setIdDetalle(gk.getLong(1));
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException r) { r.printStackTrace(); }
            }
            throw new RuntimeException("Error guardando pedidos y detalles: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    /**
     * Guarda pedido simple (legacy) usando solo columnas mínimas.
     */
    public void save(Pedido pedido) {
        String sql = "INSERT INTO pedidos (id_cliente, fecha_pedido) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (pedido.getIdCliente() != null) ps.setLong(1, pedido.getIdCliente()); else ps.setNull(1, Types.BIGINT);
            if (pedido.getFechaPedido() != null) ps.setDate(2, Date.valueOf(pedido.getFechaPedido())); else ps.setNull(2, Types.DATE);

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Insert de pedido falló, no se creó ninguna fila.");

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) pedido.setIdPedido(generatedKeys.getLong(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error en save Pedido: " + ex.getMessage(), ex);
        }
    }

    public void update(Pedido pedido) {
        String sql = "UPDATE pedidos SET id_cliente = ?, fecha_pedido = ? WHERE id_pedido = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (pedido.getIdCliente() != null) ps.setLong(1, pedido.getIdCliente()); else ps.setNull(1, Types.BIGINT);
            if (pedido.getFechaPedido() != null) ps.setDate(2, Date.valueOf(pedido.getFechaPedido())); else ps.setNull(2, Types.DATE);
            ps.setLong(3, pedido.getIdPedido());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Update de pedido falló, id no encontrado: " + pedido.getIdPedido());
        } catch (SQLException ex) {
            throw new RuntimeException("Error en update Pedido: " + ex.getMessage(), ex);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM pedidos WHERE id_pedido = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error en delete Pedido: " + ex.getMessage(), ex);
        }
    }

    public List<Pedido> findByClientId(Long clientId) {
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, p.fecha_pedido " +
                     "FROM pedidos p " +
                     "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente " +
                     "WHERE p.id_cliente = ? ORDER BY p.fecha_pedido DESC";

        List<Pedido> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPedido(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findByClientId: " + ex.getMessage(), ex);
        }
    }

    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getLong("id_pedido"));

        Long idCliente = rs.getLong("id_cliente");
        if (rs.wasNull()) idCliente = null;
        p.setIdCliente(idCliente);

        String clienteNombre = null;
        try { clienteNombre = rs.getString("cliente_nombre"); } catch (SQLException e) { clienteNombre = null; }
        p.setClienteNombre(clienteNombre);

        Date fechaPedidoSql = null;
        try { fechaPedidoSql = rs.getDate("fecha_pedido"); } catch (SQLException e) { fechaPedidoSql = null; }
        if (fechaPedidoSql != null) p.setFechaPedido(fechaPedidoSql.toLocalDate());

        // Como la DB puede no contener columnas opcionales, las dejamos null
        p.setEstadoPedido(null);
        p.setFormaCobro(null);
        p.setKiloCantidad(null);
        p.setFechaEntregaEstimada(null);

        return p;
    }

    public void saveWithDetalles(Pedido pedido, List<Detalle> detalles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
