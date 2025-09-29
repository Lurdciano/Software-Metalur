package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
// usa java.sql.Date explícito para evitar confusiones con java.util.Date
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PedidoDAO con JOIN a la tabla clientes para obtener nombre del cliente.
 * Asegurate que:
 *  - la tabla de clientes se llame "clientes"
 *  - la PK de clientes sea "id_cliente"
 *  - la columna con el nombre/razon social sea "nombre_razon_social"
 *
 * Si tu tabla de clientes usa otros nombres, ajusta el SQL (alias cliente_nombre).
 */
public class PedidoDAO {

    public PedidoDAO() {}

    public List<Pedido> findAll() {
        // Hacemos JOIN con clientes para obtener el nombre del cliente
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, "
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.estado_pedido, p.forma_cobro, p.kilo_cantidad "
                   + "FROM pedidos p "
                   + "LEFT JOIN clientes c ON p.id_cliente = c.id_cliente "
                   + "ORDER BY p.fecha_pedido DESC";

        List<Pedido> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = mapResultSetToPedido(rs);
                lista.add(p);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error en findAll Pedidos: " + ex.getMessage(), ex);
        }
    }

    public List<Pedido> findByClienteName(String name) {
        String sql = "SELECT p.id_pedido, p.id_cliente, c.nombre_razon_social AS cliente_nombre, "
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.estado_pedido, p.forma_cobro, p.kilo_cantidad "
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
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.estado_pedido, p.forma_cobro, p.kilo_cantidad "
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
        // No insertamos cliente_nombre aquí (se obtiene por JOIN), solo id_cliente y campos del pedido
        String sql = "INSERT INTO pedidos (id_cliente, fecha_pedido, fecha_entrega_estimada, estado_pedido, forma_cobro, kilo_cantidad) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, pedido.getIdCliente());
            ps.setDate(2, pedido.getFechaPedido() != null ? Date.valueOf(pedido.getFechaPedido()) : Date.valueOf(LocalDate.now()));
            if (pedido.getFechaEntregaEstimada() != null) {
                ps.setDate(3, Date.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setString(4, pedido.getEstadoPedido());
            ps.setString(5, pedido.getFormaCobro());
            if (pedido.getKiloCantidad() != null) {
                ps.setDouble(6, pedido.getKiloCantidad());
            } else {
                ps.setNull(6, java.sql.Types.DOUBLE);
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
        String sql = "UPDATE pedidos SET id_cliente = ?, fecha_pedido = ?, fecha_entrega_estimada = ?, estado_pedido = ?, forma_cobro = ?, kilo_cantidad = ? WHERE id_pedido = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pedido.getIdCliente());
            ps.setDate(2, pedido.getFechaPedido() != null ? Date.valueOf(pedido.getFechaPedido()) : null);
            if (pedido.getFechaEntregaEstimada() != null) {
                ps.setDate(3, Date.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setString(4, pedido.getEstadoPedido());
            ps.setString(5, pedido.getFormaCobro());
            if (pedido.getKiloCantidad() != null) ps.setDouble(6, pedido.getKiloCantidad()); else ps.setNull(6, java.sql.Types.DOUBLE);
            ps.setLong(7, pedido.getIdPedido());

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
                   + "p.fecha_pedido, p.fecha_entrega_estimada, p.estado_pedido, p.forma_cobro, p.kilo_cantidad "
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

        // cliente_nombre viene del JOIN (alias en el SELECT)
        String clienteNombre = null;
        try {
            clienteNombre = rs.getString("cliente_nombre");
        } catch (SQLException e) {
            // Si por algún motivo no existe la columna, queda null
            clienteNombre = null;
        }
        p.setClienteNombre(clienteNombre);

        Date fechaPedidoSql = rs.getDate("fecha_pedido");
        if (fechaPedidoSql != null) p.setFechaPedido(fechaPedidoSql.toLocalDate());

        Date fechaEntregaSql = rs.getDate("fecha_entrega_estimada");
        if (fechaEntregaSql != null) p.setFechaEntregaEstimada(fechaEntregaSql.toLocalDate());

        p.setEstadoPedido(rs.getString("estado_pedido"));
        p.setFormaCobro(rs.getString("forma_cobro"));

        double kilo = rs.getDouble("kilo_cantidad");
        if (!rs.wasNull()) p.setKiloCantidad(kilo); else p.setKiloCantidad(null);

        return p;
    }
}
