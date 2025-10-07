package com.cianmetalurgica.view;

import com.cianmetalurgica.dao.DetalleDAO;
import com.cianmetalurgica.dao.MaterialDAO;
import com.cianmetalurgica.dao.PedidoDAO;
import com.cianmetalurgica.model.Detalle;
import com.cianmetalurgica.model.Pedido;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Ventana para finalizar pedido — con generación de PDF usando PDFBox (directo).
 */
public class FinalizarPedidoWindow extends JFrame {
    private static final Color BG = new Color(245,245,245);
    private static final Color BTN = new Color(52,73,94);
    private static final Color BTN_HOVER = new Color(44,62,80);
    private static final DecimalFormat MONEDA = new DecimalFormat("#,##0.00");

    private Pedido pedido;
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private DetalleDAO detalleDAO = new DetalleDAO();
    private MaterialDAO materialDAO = new MaterialDAO();

    private DefaultTableModel model;
    private JTable table;
    private JCheckBox chkDescontarStock;
    private JLabel lblTotal;

    private TableModelListener modelListener;

    public FinalizarPedidoWindow(JFrame parent, Pedido pedido) {
        super("Finalizar pedido #" + (pedido != null ? pedido.getIdPedido() : ""));
        this.pedido = pedido;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG);
        initComponents();
        loadDetalles();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BTN);
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel title = new JLabel("FINALIZAR PEDIDO");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // table
        String[] cols = {"ID_DETALLE","ID_MATERIAL","Material","Cantidad","Precio Unit.","Total"};
        model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int row, int col) {
                // editable: Cantidad (3), Precio Unit (4)
                return col == 3 || col == 4;
            }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 3) return Double.class;
                if (col == 4) return Double.class;
                if (col == 5) return Double.class;
                return Object.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(24);

        // Listener: solo recalc cuando cambien columnas 3 o 4
        modelListener = e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if (col == TableModelEvent.ALL_COLUMNS || col == 3 || col == 4) {
                    recalcTotal();
                }
            }
        };
        model.addTableModelListener(modelListener);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // bottom controls
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setBackground(BG);
        chkDescontarStock = new JCheckBox("Descontar Stock al finalizar");
        left.add(chkDescontarStock);
        bottom.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(BG);

        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        right.add(lblTotal);

        JButton btnPdf = createButton("Generar PDF");
        btnPdf.addActionListener(e -> {
            try {
                generarPdf();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        right.add(btnPdf);

        JButton btnFinalizar = createButton("Finalizar y Guardar");
        btnFinalizar.addActionListener(e -> {
            try { onFinalizar(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error al finalizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); ex.printStackTrace(); }
        });
        right.add(btnFinalizar);

        JButton btnCerrar = createButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        right.add(btnCerrar);

        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BTN);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(BTN_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(BTN); }
        });
        return b;
    }

    private void loadDetalles() {
        if (pedido == null) return;
        model.setRowCount(0);

        // cargar filas sin activar listener múltiples
        model.removeTableModelListener(modelListener);
        List<Detalle> detalles = detalleDAO.findByPedidoId(pedido.getIdPedido());
        for (Detalle d : detalles) {
            Vector<Object> row = new Vector<>();
            row.add(d.getIdDetalle());
            row.add(d.getIdMaterial());
            row.add(d.getMaterialTipo());
            row.add(d.getCantidad() != null ? d.getCantidad().doubleValue() : 0.0);
            row.add(0.0); // precio por defecto
            row.add(0.0); // total por defecto
            model.addRow(row);
        }
        model.addTableModelListener(modelListener);
        recalcTotal();
    }

    private void recalcTotal() {
        double total = 0.0;
        for (int r = 0; r < model.getRowCount(); r++) {
            Object cantObj = model.getValueAt(r, 3);
            Object precioObj = model.getValueAt(r, 4);
            double cant = 0.0, precio = 0.0;
            if (cantObj instanceof Number) cant = ((Number) cantObj).doubleValue();
            else if (cantObj != null) {
                try { cant = Double.parseDouble(cantObj.toString()); } catch (Exception ignored) {}
            }
            if (precioObj instanceof Number) precio = ((Number) precioObj).doubleValue();
            else if (precioObj != null) {
                try { precio = Double.parseDouble(precioObj.toString()); } catch (Exception ignored) {}
            }
            double linea = cant * precio;
            model.setValueAt(linea, r, 5); // columna Total (no disparará recalc en nuestro listener)
            total += linea;
        }
        lblTotal.setText("Total: $" + MONEDA.format(total));
    }

    private void onFinalizar() {
        if (pedido == null) { JOptionPane.showMessageDialog(this, "Pedido nulo", "Error", JOptionPane.ERROR_MESSAGE); return; }

        List<Detalle> detallesParaGuardar = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            Object idDet = model.getValueAt(r, 0);
            Object idMat = model.getValueAt(r, 1);
            Object materialTipo = model.getValueAt(r, 2);
            Object cantObj = model.getValueAt(r, 3);

            Detalle d = new Detalle();
            if (idDet instanceof Number) d.setIdDetalle(((Number) idDet).longValue());
            if (idMat instanceof Number) d.setIdMaterial(((Number) idMat).longValue());
            d.setMaterialTipo(materialTipo != null ? materialTipo.toString() : null);
            Integer cantidad = null;
            if (cantObj instanceof Number) cantidad = ((Number) cantObj).intValue();
            else if (cantObj != null && !cantObj.toString().trim().isEmpty()) cantidad = Integer.valueOf(cantObj.toString());
            d.setCantidad(cantidad);
            d.setIdPedido(pedido.getIdPedido());
            detallesParaGuardar.add(d);
        }

        // Guardar detalles
        for (Detalle d : detallesParaGuardar) {
            if (d.getIdDetalle() != null) {
                detalleDAO.update(d);
            } else {
                detalleDAO.save(d);
            }
        }

        // Descontar stock si corresponde
        if (chkDescontarStock.isSelected()) {
            for (Detalle d : detallesParaGuardar) {
                if (d.getIdMaterial() != null && d.getCantidad() != null) {
                    try {
                        materialDAO.changeStock(d.getIdMaterial(), -d.getCantidad());
                    } catch (Exception ex) {
                        // manejar/loguear error, pero seguimos
                        ex.printStackTrace();
                    }
                }
            }
        }

        // actualizar pedido
        pedido.setEstadoPedido("TERMINADO");
        pedidoDAO.update(pedido);

        JOptionPane.showMessageDialog(this, "Pedido finalizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /**
     * Genera PDF de la factura usando PDFBox (requiere pdfbox y fontbox en librerías).
     */
    private void generarPdf() throws IOException {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay líneas para exportar.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("factura_pedido_" + (pedido != null && pedido.getIdPedido()!=null ? pedido.getIdPedido() : "X") + ".pdf"));
        int opt = fc.showSaveDialog(this);
        if (opt != JFileChooser.APPROVE_OPTION) return;
        File outFile = fc.getSelectedFile();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float x = margin;
                float y = yStart;

                cs.beginText();
                PDType0Font font = PDType0Font.load(doc, new File("C:\\Windows\\Fonts\\arial.ttf"));
cs.setFont(font, 12);
                cs.newLineAtOffset(x, y);
                cs.showText("FACTURA / COMPROBANTE - Pedido #" + (pedido != null && pedido.getIdPedido()!=null ? pedido.getIdPedido() : ""));
                cs.endText();

                y -= 25;
                cs.beginText();
               
cs.setFont(font, 12);
                cs.newLineAtOffset(x, y);
                String clienteInfo = "Cliente: " + (pedido != null && pedido.getClienteNombre()!=null ? pedido.getClienteNombre() : "");
                cs.showText(clienteInfo);
                cs.endText();

                y -= 20;
                // encabezado tabla
                cs.beginText();
               cs.setFont(font, 12);
                cs.newLineAtOffset(x, y);
                cs.showText(String.format("%-6s %-30s %10s %12s %12s", "ID", "Material", "Cant.", "P.Unit", "Total"));
                cs.endText();

                y -= 15;
                
cs.setFont(font, 12);

                double grandTotal = 0.0;
                for (int r = 0; r < model.getRowCount(); r++) {
                    if (y < 70) {
                        // nueva página
                        cs.close();
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs.close(); // just to be safe; but we always create a new stream in try-with-resources below
                        try (PDPageContentStream cs2 = new PDPageContentStream(doc, page)) {
                            // reassign cs for further writes (but we will continue the loop with cs2)
                            // Simplificamos: abrir nuevo stream y write header text again
                            y = page.getMediaBox().getHeight() - margin;
                            cs2.beginText();
                            
cs.setFont(font, 12);
                            cs2.newLineAtOffset(x, y);
                            // write line then close cs2
                            Object id = model.getValueAt(r, 0);
                            Object material = model.getValueAt(r, 2);
                            Object cant = model.getValueAt(r, 3);
                            Object punit = model.getValueAt(r, 4);
                            Object total = model.getValueAt(r, 5);
                            String line = String.format("%-6s %-30s %10s %12s %12s",
                                    id != null ? id.toString() : "",
                                    material != null ? truncate(material.toString(),30) : "",
                                    cant != null ? cant.toString() : "",
                                    punit != null ? punit.toString() : "",
                                    total != null ? total.toString() : "");
                            cs2.showText(line);
                            cs2.endText();
                            grandTotal += parseDoubleSafely(model.getValueAt(r,5));
                            y -= 15;
                        }
                    } else {
                        cs.beginText();
                        cs.newLineAtOffset(x, y);
                        Object id = model.getValueAt(r, 0);
                        Object material = model.getValueAt(r, 2);
                        Object cant = model.getValueAt(r, 3);
                        Object punit = model.getValueAt(r, 4);
                        Object total = model.getValueAt(r, 5);
                        String line = String.format("%-6s %-30s %10s %12s %12s",
                                id != null ? id.toString() : "",
                                material != null ? truncate(material.toString(),30) : "",
                                cant != null ? cant.toString() : "",
                                punit != null ? punit.toString() : "",
                                total != null ? total.toString() : "");
                        cs.showText(line);
                        cs.endText();
                        grandTotal += parseDoubleSafely(model.getValueAt(r,5));
                        y -= 15;
                    }
                }

                // total final
                y -= 20;
                cs.beginText();
                
cs.setFont(font, 12);
                cs.newLineAtOffset(x, y);
                cs.showText("TOTAL: $" + MONEDA.format(grandTotal));
                cs.endText();
            }

            doc.save(outFile);
        }

        JOptionPane.showMessageDialog(this, "PDF generado: " + outFile.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max-3) + "...";
    }

    private static double parseDoubleSafely(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception ex) { return 0.0; }
    }
}
