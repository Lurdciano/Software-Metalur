package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Pedido;
import com.cianmetalurgica.model.Detalle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public PedidoDAO() {}

    public List<Pedido> findAll() {
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, "
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.kilo_cantidad "
                   + "FROM pedidos p "
                   + "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente "
                   + "ORDER BY p.fecha_pedido DESC";

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
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, "
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.kilo_cantidad "
                   + "FROM pedidos p "
                   + "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente "
                   + "WHERE c.nombre_razon_social LIKE ? "
                   + "ORDER BY p.fecha_pedido DESC";

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
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, "
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.kilo_cantidad "
                   + "FROM pedidos p "
                   + "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente "
                   + "WHERE p.id_pedido = ?";

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

    public void save(Pedido pedido) {
        String sql = "INSERT INTO pedidos (id_cliente, fecha_pedido, fecha_entrega_estimada, kilo_cantidad) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, pedido.getIdCliente());
            ps.setDate(2, pedido.getFechaPedido() != null ? Date.valueOf(pedido.getFechaPedido()) : Date.valueOf(LocalDate.now()));
            if (pedido.getFechaEntregaEstimada() != null) {
                ps.setDate(3, Date.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            if (pedido.getKiloCantidad() != null) {
                ps.setDouble(4, pedido.getKiloCantidad());
            } else {
                ps.setNull(4, java.sql.Types.DOUBLE);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insert de pedido falló, no se creó ninguna fila.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pedido.setIdPedido(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error en save Pedido: " + ex.getMessage(), ex);
        }
    }

    public void update(Pedido pedido) {
        String sql = "UPDATE pedidos SET id_cliente = ?, fecha_pedido = ?, fecha_entrega_estimada = ?, kilo_cantidad = ? WHERE id_pedido = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pedido.getIdCliente());
            ps.setDate(2, pedido.getFechaPedido() != null ? Date.valueOf(pedido.getFechaPedido()) : null);
            if (pedido.getFechaEntregaEstimada() != null) {
                ps.setDate(3, Date.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            if (pedido.getKiloCantidad() != null) ps.setDouble(4, pedido.getKiloCantidad()); else ps.setNull(4, java.sql.Types.DOUBLE);
            ps.setLong(5, pedido.getIdPedido());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Update de pedido falló, id no encontrado: " + pedido.getIdPedido());
            }
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
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, "
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.kilo_cantidad "
                   + "FROM pedidos p "
                   + "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente "
                   + "WHERE p.id_cliente = ? ORDER BY p.fecha_pedido DESC";

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

    // ---------- Helpers ----------
    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getLong("id_pedido"));

        Long idCliente = rs.getLong("id_cliente");
        if (rs.wasNull()) idCliente = null;
        p.setIdCliente(idCliente);

        String clienteNombre = null;
        try {
            clienteNombre = rs.getString("cliente_nombre");
        } catch (SQLException e) {
            clienteNombre = null;
        }
        p.setClienteNombre(clienteNombre);

        Date fechaPedidoSql = rs.getDate("fecha_pedido");
        if (fechaPedidoSql != null) p.setFechaPedido(fechaPedidoSql.toLocalDate());

        Date fechaEntregaSql = rs.getDate("fecha_entrega_estimada");
        if (fechaEntregaSql != null) p.setFechaEntregaEstimada(fechaEntregaSql.toLocalDate());

        double kilo = rs.getDouble("kilo_cantidad");
        if (!rs.wasNull()) p.setKiloCantidad(kilo); else p.setKiloCantidad(null);

        return p;
    }
    
    public void saveWithDetalles(Pedido pedido, List<Detalle> detalles) {
    String sqlPedido = "INSERT INTO pedidos (id_cliente, fecha_pedido, fecha_entrega_estimada, kilo_cantidad) VALUES (?, ?, ?, ?)";
    DetalleDAO detalleDAO = new DetalleDAO();

    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // iniciar transacción

        // Insert pedido y obtener id
        try (PreparedStatement ps = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pedido.getIdCliente());
            ps.setDate(2, pedido.getFechaPedido() != null ? Date.valueOf(pedido.getFechaPedido()) : Date.valueOf(LocalDate.now()));
            if (pedido.getFechaEntregaEstimada() != null) ps.setDate(3, Date.valueOf(pedido.getFechaEntregaEstimada())); else ps.setNull(3, java.sql.Types.DATE);
            if (pedido.getKiloCantidad() != null) ps.setDouble(4, pedido.getKiloCantidad()); else ps.setNull(4, java.sql.Types.DOUBLE);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insert de pedido falló, no se creó ninguna fila.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setIdPedido(rs.getLong(1));
                } else {
                    throw new SQLException("No se obtuvo id generado para pedido.");
                }
            }
        }

        // Insertar cada detalle usando la misma conexión
        if (detalles != null) {
            for (Detalle d : detalles) {
                d.setIdPedido(pedido.getIdPedido()); // asignar FK
                detalleDAO.saveWithConnection(d, conn);
            }
        }

        conn.commit();
    } catch (SQLException ex) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException e) { /* log */ }
        }
        throw new RuntimeException("Error en saveWithDetalles Pedido: " + ex.getMessage(), ex);
    } finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* log */ }
        }
    }
}
    
}
