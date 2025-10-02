package com.cianmetalurgica.dao;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.model.Factura;
import com.cianmetalurgica.model.FacturaDetalle;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class FacturaDAO {

    /**
     * Guarda factura + detalles en la base y opcionalmente descuenta stock,
     * todo en la misma transacción.
     *
     * @param factura       Objeto factura (idPedido, fechaEmision, total, nroFactura, observaciones)
     * @param detalles      Lista de detalles asociados (cantidad, precioUnitario, idMaterial opcional)
     * @param descontarStock si true, intenta decrementar stock en materiales y crea registros en stock_movimientos
     * @throws RuntimeException en caso de error (ya hace rollback)
     */
    public void saveWithTransaction(Factura factura, List<FacturaDetalle> detalles, boolean descontarStock) {
        String insertFacturaSql = "INSERT INTO facturas (id_pedido, fecha_emision, total, nro_factura, observaciones) VALUES (?, ?, ?, ?, ?)";
        String insertDetalleSql = "INSERT INTO factura_detalles (id_factura, id_material, material_tipo, descripcion, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String selectMaterialForUpdateSql = "SELECT cantidad FROM materiales WHERE id_material = ? FOR UPDATE";
        String updateMaterialSql = "UPDATE materiales SET cantidad = cantidad - ?, version = version + 1 WHERE id_material = ?";
        String insertStockMovimientoSql = "INSERT INTO stock_movimientos (id_material, tipo_movimiento, cantidad, referencia, comentario, usuario) VALUES (?, ?, ?, ?, ?, ?)";
        String updatePedidoEstadoSql = "UPDATE pedidos SET estado = ? WHERE id_pedido = ?";

        Connection conn = null;
        PreparedStatement psInsertFactura = null;
        PreparedStatement psInsertDetalle = null;
        PreparedStatement psSelectMaterial = null;
        PreparedStatement psUpdateMaterial = null;
        PreparedStatement psInsertStockMov = null;
        PreparedStatement psUpdatePedido = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // start transaction

            // 1) Insert factura
            psInsertFactura = conn.prepareStatement(insertFacturaSql, Statement.RETURN_GENERATED_KEYS);
            psInsertFactura.setLong(1, factura.getIdPedido());
            psInsertFactura.setDate(2, factura.getFechaEmision() != null ? Date.valueOf(factura.getFechaEmision()) : Date.valueOf(LocalDate.now()));
            psInsertFactura.setDouble(3, factura.getTotal() != null ? factura.getTotal() : 0.0);
            psInsertFactura.setString(4, factura.getNroFactura());
            psInsertFactura.setString(5, factura.getObservaciones());
            int affected = psInsertFactura.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insert de factura falló, no se creó ninguna fila.");
            }
            try (ResultSet gk = psInsertFactura.getGeneratedKeys()) {
                if (gk.next()) {
                    factura.setIdFactura(gk.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener id generado para la factura.");
                }
            }

            // prepare statements reuse
            psInsertDetalle = conn.prepareStatement(insertDetalleSql, Statement.RETURN_GENERATED_KEYS);
            psSelectMaterial = conn.prepareStatement(selectMaterialForUpdateSql);
            psUpdateMaterial = conn.prepareStatement(updateMaterialSql);
            psInsertStockMov = conn.prepareStatement(insertStockMovimientoSql);
            psUpdatePedido = conn.prepareStatement(updatePedidoEstadoSql);

            // 2) Insert detalles y (opcional) descontar stock
            for (FacturaDetalle det : detalles) {
                // calcular subtotal si no está (por seguridad)
                if (det.getSubtotal() == null) {
                    double cantidad = det.getCantidad() != null ? det.getCantidad() : 0.0;
                    double precio = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : 0.0;
                    det.setSubtotal(cantidad * precio);
                }

                psInsertDetalle.setLong(1, factura.getIdFactura());
                if (det.getIdMaterial() != null) psInsertDetalle.setLong(2, det.getIdMaterial()); else psInsertDetalle.setNull(2, Types.BIGINT);
                psInsertDetalle.setString(3, det.getMaterialTipo());
                psInsertDetalle.setString(4, det.getDescripcion());
                if (det.getCantidad() != null) psInsertDetalle.setDouble(5, det.getCantidad()); else psInsertDetalle.setNull(5, Types.DOUBLE);
                if (det.getPrecioUnitario() != null) psInsertDetalle.setDouble(6, det.getPrecioUnitario()); else psInsertDetalle.setNull(6, Types.DOUBLE);
                if (det.getSubtotal() != null) psInsertDetalle.setDouble(7, det.getSubtotal()); else psInsertDetalle.setNull(7, Types.DOUBLE);

                int detAffected = psInsertDetalle.executeUpdate();
                if (detAffected == 0) {
                    throw new SQLException("Insert detalle falló para factura " + factura.getIdFactura());
                }
                // optionally get generated id for detalle
                try (ResultSet gkDet = psInsertDetalle.getGeneratedKeys()) {
                    if (gkDet.next()) {
                        det.setId(gkDet.getLong(1));
                    }
                }

                if (descontarStock && det.getIdMaterial() != null && det.getCantidad() != null) {
                    // 2a) SELECT ... FOR UPDATE (bloqueo)
                    psSelectMaterial.setLong(1, det.getIdMaterial());
                    try (ResultSet rsMat = psSelectMaterial.executeQuery()) {
                        if (!rsMat.next()) {
                            throw new SQLException("Material id=" + det.getIdMaterial() + " no encontrado para descontar stock.");
                        }
                        double stockActual = rsMat.getDouble("cantidad");
                        if (Double.isNaN(stockActual)) stockActual = 0.0;
                        if (stockActual < det.getCantidad()) {
                            throw new RuntimeException("Stock insuficiente para material id=" + det.getIdMaterial() +
                                    " (solicitado=" + det.getCantidad() + ", disponible=" + stockActual + ")");
                        }
                    }

                    // 2b) UPDATE cantidad = cantidad - ?
                    psUpdateMaterial.setDouble(1, det.getCantidad());
                    psUpdateMaterial.setLong(2, det.getIdMaterial());
                    int updated = psUpdateMaterial.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("No se pudo actualizar stock para material id=" + det.getIdMaterial());
                    }

                    // 2c) Insertar movimiento en stock_movimientos
                    psInsertStockMov.setLong(1, det.getIdMaterial());
                    psInsertStockMov.setString(2, "SALIDA");
                    psInsertStockMov.setDouble(3, det.getCantidad());
                    // referencia: usamos nro_factura si existe, si no el id_factura
                    String referencia = factura.getNroFactura() != null ? factura.getNroFactura() : "FACT-" + factura.getIdFactura();
                    psInsertStockMov.setString(4, referencia);
                    psInsertStockMov.setString(5, "Salida por facturación");
                    psInsertStockMov.setString(6, System.getProperty("user.name")); // usuario del sistema; podés cambiarlo
                    psInsertStockMov.executeUpdate();
                }
            }

            // 3) Marcar pedido como TERMINADO
            psUpdatePedido.setString(1, "TERMINADO");
            psUpdatePedido.setLong(2, factura.getIdPedido());
            psUpdatePedido.executeUpdate();

            // 4) Commit
            conn.commit();
        } catch (Exception ex) {
            // rollback si algo falla
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rbe) { /* log */ }
            }
            throw new RuntimeException("Error al guardar factura con transacción: " + ex.getMessage(), ex);
        } finally {
            // cerrar recursos en orden inverso
            try { if (psUpdatePedido != null) psUpdatePedido.close(); } catch (SQLException ignore) {}
            try { if (psInsertStockMov != null) psInsertStockMov.close(); } catch (SQLException ignore) {}
            try { if (psUpdateMaterial != null) psUpdateMaterial.close(); } catch (SQLException ignore) {}
            try { if (psSelectMaterial != null) psSelectMaterial.close(); } catch (SQLException ignore) {}
            try { if (psInsertDetalle != null) psInsertDetalle.close(); } catch (SQLException ignore) {}
            try { if (psInsertFactura != null) psInsertFactura.close(); } catch (SQLException ignore) {}
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ignore) {}
        }
    }
}
