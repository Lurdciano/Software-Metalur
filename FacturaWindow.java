package com.cianmetalurgica.view;

import com.cianmetalurgica.dao.DetalleDAO;
import com.cianmetalurgica.dao.FacturaDAO;
import com.cianmetalurgica.dao.PedidoDAO;
import com.cianmetalurgica.model.Detalle;
import com.cianmetalurgica.model.Factura;
import com.cianmetalurgica.model.FacturaDetalle;
import com.cianmetalurgica.model.Pedido;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Ventana para finalizar un pedido y generar una factura.
 * - Edita cantidades y precios unitarios por línea.
 * - Calcula subtotales y total en tiempo real.
 * - Permite "Descontar Stock" cuando genera la factura (delegado al DAO).
 * - Llama a FacturaDAO.saveWithTransaction en un SwingWorker para no bloquear la UI.
 *
 * Requiere: PedidoDAO, DetalleDAO, FacturaDAO implementados.
 */
public class FacturaWindow extends JDialog {
    private final Pedido pedido;
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetalleDAO detalleDAO = new DetalleDAO();
    private final FacturaDAO facturaDAO = new FacturaDAO();

    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTotal;
    private JCheckBox chkDescontarStock;
    private JButton btnGenerar;
    private JButton btnAplicarPrecioAll;
    private JTextField txtPrecioAplicar;

    // Column indices
    private static final int COL_MATERIAL = 0;
    private static final int COL_TIPO = 1;
    private static final int COL_CANTIDAD = 2;
    private static final int COL_PRECIO = 3;
    private static final int COL_SUBTOTAL = 4;

    // Tipos de material: sincronizar con MaterialWindow.tipoComboBox (mismo orden/valores)
    // MANTENER LAS OPCIONES COINCIDENTES con MaterialWindow: si las cambias allí, reflejalas aquí.
    private static final String[] TIPOS_MATERIAL = new String[] {
        "Chapa", "Perfil", "Tubo", "Placa", "Otro"
        // <-- Si cambiás/añadís opciones en MaterialWindow, copialas/actualizalas aquí.
    };

    public FacturaWindow(Frame parent, Long pedidoId) {
        super(parent, "Finalizar / Facturar Pedido", true);
        this.pedido = loadPedido(pedidoId);
        if (this.pedido == null) {
            JOptionPane.showMessageDialog(parent, "Pedido no encontrado: ID=" + pedidoId, "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        initComponents();
        loadDetalles();
        pack();
        setLocationRelativeTo(parent);
    }

    // Cargar pedido desde DAO (puede lanzar runtime exceptions si fallan conexiones)
    private Pedido loadPedido(Long pedidoId) {
        try {
            return pedidoDAO.findById(pedidoId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando pedido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

        JLabel lbl = new JLabel("<html><b>Pedido:</b> #" + pedido.getIdPedido() +
                " &nbsp;&nbsp; <b>Cliente:</b> " + (pedido.getClienteNombre() != null ? pedido.getClienteNombre() : pedido.getIdCliente()) +
                " &nbsp;&nbsp; <b>Fecha:</b> " + (pedido.getFechaPedido() != null ? pedido.getFechaPedido().toString() : "") + "</html>");
        header.add(lbl, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Tabla: Material | Tipo (combo) | Cantidad | Precio unitario | Subtotal
        String[] cols = {"Material", "Tipo", "Cantidad", "Precio unitario", "Subtotal"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                // editable: Tipo, Cantidad, Precio
                return column == COL_TIPO || column == COL_CANTIDAD || column == COL_PRECIO;
            }

            @Override public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case COL_CANTIDAD: return Double.class;
                    case COL_PRECIO: return Double.class;
                    case COL_SUBTOTAL: return Double.class;
                    default: return String.class;
                }
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        // Tipo column: cell editor ComboBox con tipos
        TableColumn tipoCol = table.getColumnModel().getColumn(COL_TIPO);
        JComboBox<String> comboTipos = new JComboBox<>(TIPOS_MATERIAL);
        tipoCol.setCellEditor(new DefaultCellEditor(comboTipos));

        // Centrar/formatos
        TableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        ((DefaultTableCellRenderer) rightRenderer).setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(COL_CANTIDAD).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(COL_PRECIO).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(COL_SUBTOTAL).setCellRenderer(rightRenderer);

        // Subtotal no editable — recalculado por TableModelListener
        model.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                // cuando cambia cantidad o precio, recalcular subtotal y total
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    if (col == COL_CANTIDAD || col == COL_PRECIO || col == TableModelEvent.ALL_COLUMNS) {
                        recomputeRowSubtotal(row);
                        recomputeTotal();
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(900, 300));
        add(scroll, BorderLayout.CENTER);

        // Panel inferior: controls y total
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        // Left controls
        JPanel leftCtrl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkDescontarStock = new JCheckBox("Descontar Stock");
        leftCtrl.add(chkDescontarStock);

        leftCtrl.add(new JLabel("Precio unitario:"));
        txtPrecioAplicar = new JTextField(8);
        leftCtrl.add(txtPrecioAplicar);

        btnAplicarPrecioAll = new JButton("Aplicar precio a todas");
        btnAplicarPrecioAll.addActionListener(e -> aplicarPrecioATodas());
        leftCtrl.add(btnAplicarPrecioAll);

        bottom.add(leftCtrl, BorderLayout.WEST);

        // Right: total y botones
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("Total: 0.00");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        totalPanel.add(lblTotal);
        rightPanel.add(totalPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGenerar = new JButton("Generar Factura");
        btnGenerar.addActionListener(e -> onGenerarFactura());
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        btnPanel.add(btnGenerar);
        btnPanel.add(btnCancelar);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        bottom.add(rightPanel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    // Carga detalles desde DetalleDAO y llena la tabla
    private void loadDetalles() {
        try {
            List<Detalle> detalles = detalleDAO.findByPedidoId(pedido.getIdPedido());
            model.setRowCount(0);
            if (detalles == null) detalles = new ArrayList<>();
            for (Detalle d : detalles) {
                String materialDesc = d.getMaterialTipo() != null ? d.getMaterialTipo() : (d.getIdMaterial() != null ? "ID:" + d.getIdMaterial() : "Detalle");
                Double cantidad = d.getCantidad() != null ? d.getCantidad().doubleValue() : 0.0;
                // Inicialmente sin precio (0) — el usuario lo define
                Double precio = 0.0;
                Double subtotal = cantidad * precio;
                Vector<Object> row = new Vector<>();
                row.add(materialDesc);
                // si material tiene tipo conocido, intentar seleccionar matching tipo, sino seleccionar índice -1
                String tipoVal = d.getMaterialTipo();
                row.add(tipoVal != null ? tipoVal : TIPOS_MATERIAL.length > 0 ? TIPOS_MATERIAL[0] : "");
                row.add(cantidad);
                row.add(precio);
                row.add(subtotal);
                model.addRow(row);
            }
            recomputeTotal();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando detalles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Recalcula el subtotal de una fila
    private void recomputeRowSubtotal(int row) {
        try {
            Object oCant = model.getValueAt(row, COL_CANTIDAD);
            Object oPrecio = model.getValueAt(row, COL_PRECIO);
            double cant = oCant instanceof Number ? ((Number)oCant).doubleValue() : Double.parseDouble(oCant.toString());
            double precio = oPrecio instanceof Number ? ((Number)oPrecio).doubleValue() : Double.parseDouble(oPrecio.toString());
            double subtotal = cant * precio;
            model.setValueAt(subtotal, row, COL_SUBTOTAL);
        } catch (Exception ex) {
            // si parse falla, ignoramos y ponemos 0
            model.setValueAt(0.0, row, COL_SUBTOTAL);
        }
    }

    // Recalcula el total
    private void recomputeTotal() {
        double total = 0.0;
        for (int r = 0; r < model.getRowCount(); r++) {
            Object o = model.getValueAt(r, COL_SUBTOTAL);
            if (o instanceof Number) total += ((Number)o).doubleValue();
            else {
                try { total += Double.parseDouble(o.toString()); } catch (Exception ignored) {}
            }
        }
        lblTotal.setText(String.format("Total: %.2f", total));
    }

    // Aplica el precio ingresado a todas las filas
    private void aplicarPrecioATodas() {
        String txt = txtPrecioAplicar.getText().trim();
        if (txt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un precio válido", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double precio = Double.parseDouble(txt);
            for (int r = 0; r < model.getRowCount(); r++) {
                model.setValueAt(precio, r, COL_PRECIO);
                recomputeRowSubtotal(r);
            }
            recomputeTotal();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cuando usuario presiona "Generar Factura"
    private void onGenerarFactura() {
        // validar: debe haber al menos una fila con cantidad>0 y precio>=0
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay líneas para facturar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<FacturaDetalle> detalles = new ArrayList<>();
        double totalCalc = 0.0;
        for (int r = 0; r < model.getRowCount(); r++) {
            try {
                String materialDesc = model.getValueAt(r, COL_MATERIAL).toString();
                String tipo = model.getValueAt(r, COL_TIPO).toString();
                Object oCant = model.getValueAt(r, COL_CANTIDAD);
                Object oPrecio = model.getValueAt(r, COL_PRECIO);
                double cant = oCant instanceof Number ? ((Number)oCant).doubleValue() : Double.parseDouble(oCant.toString());
                double precio = oPrecio instanceof Number ? ((Number)oPrecio).doubleValue() : Double.parseDouble(oPrecio.toString());
                if (cant <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0 en la fila " + (r+1), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (precio < 0) {
                    JOptionPane.showMessageDialog(this, "El precio no puede ser negativo en la fila " + (r+1), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double subtotal = cant * precio;
                FacturaDetalle fd = new FacturaDetalle();
                // Nota: no establecemos idMaterial aquí (si lo necesitas, modifica para obtener idMaterial desde Detalle original)
                fd.setMaterialTipo(tipo);
                fd.setDescripcion(materialDesc);
                fd.setCantidad(cant);
                fd.setPrecioUnitario(precio);
                fd.setSubtotal(subtotal);
                detalles.add(fd);
                totalCalc += subtotal;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error leyendo fila " + (r+1) + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Crear factura
        Factura factura = new Factura();
        factura.setIdPedido(pedido.getIdPedido());
        factura.setFechaEmision(java.time.LocalDate.now());
        factura.setTotal(totalCalc);
        factura.setNroFactura(null); // opcional: generar nro aquí o dejar que lo haga otro proceso
        factura.setObservaciones("Generada desde UI");

        boolean descontar = chkDescontarStock.isSelected();

        // Disable buttons while procesando
        btnGenerar.setEnabled(false);
        btnAplicarPrecioAll.setEnabled(false);

        // Ejecutar en SwingWorker para no bloquear EDT
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Exception exception;

            @Override protected Void doInBackground() {
                try {
                    facturaDAO.saveWithTransaction(factura, detalles, descontar);
                } catch (Exception ex) {
                    this.exception = ex;
                }
                return null;
            }

            @Override protected void done() {
                btnGenerar.setEnabled(true);
                btnAplicarPrecioAll.setEnabled(true);
                if (exception != null) {
                    JOptionPane.showMessageDialog(FacturaWindow.this, "Error generando factura: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(FacturaWindow.this, "Factura generada correctamente (ID: " + factura.getIdFactura() + ")", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    // Opcional: aquí podrías pedir al usuario generar/abrir PDF.
                    dispose();
                }
            }
        };
        worker.execute();
    }
}
