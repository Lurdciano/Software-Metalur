package com.cianmetalurgica.view;

import com.cianmetalurgica.dao.ClienteDAO;
import com.cianmetalurgica.dao.DetalleDAO;
import com.cianmetalurgica.dao.MaterialDAO;
import com.cianmetalurgica.dao.PedidoDAO;
import com.cianmetalurgica.model.Cliente;
import com.cianmetalurgica.model.Material;
import com.cianmetalurgica.model.Pedido;
import com.cianmetalurgica.model.Detalle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PedidoWindow extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(44, 62, 80);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private MaterialDAO materialDAO;
    private DetalleDAO detalleDAO;

    private String mode; // "LIST", "CREATE", "SEARCH", "EDIT"
    private JFrame parent;
    private Pedido pedidoToEdit;

    // Form (create/edit)
    private JComboBox<Cliente> cmbCliente;
    private JTextField txtFechaPedido;

    // Material / lines
    private JComboBox<Material> cmbMaterial;
    private JTextField txtCantidadLinea;
    private JTextField txtDimensionesLinea;
    private JTextField txtCortesLinea;
    private JTextField txtPesoLinea;
    private JButton btnAgregarLinea;

    // Tabla de líneas (líneas del pedido)
    private JTable lineasTable;
    private DefaultTableModel lineasTableModel;

    // Tabla de pedidos (lista / búsqueda)
    private JTable pedidosTable;
    private DefaultTableModel pedidosTableModel;
    private JTextField txtBuscar;

    public PedidoWindow(JFrame parent, String mode) {
        this.parent = parent;
        this.mode = mode;
        this.pedidoDAO = new PedidoDAO();
        this.clienteDAO = new ClienteDAO();
        this.materialDAO = new MaterialDAO();
        this.detalleDAO = new DetalleDAO();

        initComponents();
        setupLayout();

        // si estamos en modo lista o búsqueda, aseguramos y cargamos datos
        if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            ensureListPanelIfNeeded();
            loadData();
        }

        // si queremos crear/editar cargamos recursos necesarios
        if ("CREATE".equals(mode) || "EDIT".equals(mode)) {
            loadClientes();
            loadMaterials();
        }
    }

    public PedidoWindow(JFrame parent, String mode, Pedido pedido) {
        this(parent, mode);
        this.pedidoToEdit = pedido;
        if ("EDIT".equals(mode) && pedido != null) {
            fillFormWithPedido(pedido);
        }
    }

    // Constructor auxiliar si tu MainWindow pasa MaterialWindow.Mode
    public PedidoWindow(JFrame parent, MaterialWindow.Mode mode) {
        this(parent, mode != null ? mode.name() : "LIST");
    }

    private void initComponents() {
        setTitle("Cian Metalúrgica - " + ("CREATE".equals(mode) ? "Nuevo Pedido" : "Pedidos"));
        setSize(1000, 700);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(BUTTON_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel(getTitle().replace("Cian Metalúrgica - ", ""));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center: dependiendo del modo
        if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            add(createListPanel(), BorderLayout.CENTER);
        } else {
            add(createFormPanel(), BorderLayout.CENTER);
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Top: cliente + fecha
        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(BACKGROUND_COLOR);
        top.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Cliente
        gbc.gridx = 0; gbc.gridy = 0;
        top.add(new JLabel("Cliente *:"), gbc);
        gbc.gridx = 1;
        cmbCliente = new JComboBox<>();
        cmbCliente.setPreferredSize(new Dimension(320, 25));
        top.add(cmbCliente, gbc);

        // Fecha pedido
        gbc.gridx = 0; gbc.gridy = 1;
        top.add(new JLabel("Fecha de Pedido:"), gbc);
        gbc.gridx = 1;
        txtFechaPedido = new JTextField(15);
        txtFechaPedido.setText(LocalDate.now().format(DATE_FORMATTER));
        top.add(txtFechaPedido, gbc);

        panel.add(top, BorderLayout.NORTH);

        // Center: sección líneas (material + inputs + tabla)
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND_COLOR);

        // Selector de material y campos
        JPanel formLine = new JPanel(new GridBagLayout());
        formLine.setBackground(BACKGROUND_COLOR);
        formLine.setBorder(BorderFactory.createTitledBorder("Agregar línea"));

        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(6,6,6,6);
        g2.anchor = GridBagConstraints.WEST;

        g2.gridx = 0; g2.gridy = 0; formLine.add(new JLabel("Material:"), g2);
        g2.gridx = 1;
        cmbMaterial = new JComboBox<>();
        cmbMaterial.setPreferredSize(new Dimension(300,25));
        formLine.add(cmbMaterial, g2);

        g2.gridx = 2; formLine.add(new JLabel("Cantidad:"), g2);
        g2.gridx = 3;
        txtCantidadLinea = new JTextField(8);
        formLine.add(txtCantidadLinea, g2);

        g2.gridx = 0; g2.gridy = 1; formLine.add(new JLabel("Dimensiones:"), g2);
        g2.gridx = 1;
        txtDimensionesLinea = new JTextField(15);
        formLine.add(txtDimensionesLinea, g2);

        g2.gridx = 2; formLine.add(new JLabel("Cortes:"), g2);
        g2.gridx = 3;
        txtCortesLinea = new JTextField(6);
        formLine.add(txtCortesLinea, g2);

        g2.gridx = 0; g2.gridy = 2; formLine.add(new JLabel("Peso (kg):"), g2);
        g2.gridx = 1;
        txtPesoLinea = new JTextField(8);
        formLine.add(txtPesoLinea, g2);

        g2.gridx = 3; g2.gridy = 2;
        btnAgregarLinea = new JButton("Agregar línea");
        btnAgregarLinea.addActionListener(e -> agregarLinea());
        btnAgregarLinea.setBackground(BUTTON_COLOR);
        btnAgregarLinea.setForeground(Color.BLACK);
        formLine.add(btnAgregarLinea, g2);

        center.add(formLine, BorderLayout.NORTH);

        // Tabla de líneas
        String[] cols = {"ID_DETALLE","ID_MATERIAL","Material","Cantidad","Dimensiones","Cortes","Peso"};
        lineasTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                // permitir editar cantidad, dimensiones, cortes, peso en la tabla si se quiere
                return col == 3 || col == 4 || col == 5 || col == 6;
            }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 3) return Integer.class;
                if (col == 6) return Double.class;
                return Object.class;
            }
        };
        lineasTable = new JTable(lineasTableModel);
        lineasTable.setRowHeight(24);
        JScrollPane sp = new JScrollPane(lineasTable);
        sp.setBorder(BorderFactory.createTitledBorder("Líneas del pedido"));
        center.add(sp, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if ("SEARCH".equals(mode)) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBackground(BACKGROUND_COLOR);
            searchPanel.add(new JLabel("Buscar por cliente:"));
            txtBuscar = new JTextField(25);
            searchPanel.add(txtBuscar);
            JButton btnBuscar = createButton("Buscar", e -> performSearch());
            searchPanel.add(btnBuscar);
            panel.add(searchPanel, BorderLayout.NORTH);
        }

        // tabla general de pedidos (simple)
        String[] columns = {"ID", "Cliente", "Fecha Pedido"};
        pedidosTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pedidosTable = new JTable(pedidosTableModel);
        pedidosTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(pedidosTable);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        if ("CREATE".equals(mode)) {
            panel.add(createButton("Guardar", e -> savePedido()));
            panel.add(createButton("Limpiar", e -> clearForm()));
        } else if ("EDIT".equals(mode)) {
            panel.add(createButton("Actualizar", e -> updatePedido()));
        } else if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            panel.add(createButton("Nuevo", e -> openCreateWindow()));
            panel.add(createButton("Editar", e -> editSelected()));
            panel.add(createButton("Eliminar", e -> deleteSelected()));
            panel.add(createButton("Finalizar", e -> openFinalizarWindow()));
            panel.add(createButton("Actualizar Lista", e -> loadData()));
        }

        panel.add(createButton("Cerrar", e -> dispose()));
        return panel;
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.addActionListener(action);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_HOVER_COLOR); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_COLOR); }
        });
        return button;
    }

    private void ensureListPanelIfNeeded() {
        // Si estamos en modo lista/ búsqueda y la tabla de pedidos no existe, creamos el panel de lista
        if (!("LIST".equals(mode) || "SEARCH".equals(mode))) return;
        if (pedidosTableModel != null) return;

        // Removemos cualquier centro actual y ponemos el panel de lista
        getContentPane().removeAll();
        setupLayout(); // reconstruye la UI con lista en el centro
        validate();
        repaint();
    }

    private void loadClientes() {
        try {
            List<Cliente> clientes = clienteDAO.findAll();
            cmbCliente.removeAllItems();
            for (Cliente c : clientes) cmbCliente.addItem(c);
        } catch (SQLException e) {
            showErrorMessage("Error al cargar clientes: " + e.getMessage());
        }
    }

    private void loadMaterials() {
        try {
            List<Material> materiales = materialDAO.findAll();
            cmbMaterial.removeAllItems();
            for (Material m : materiales) cmbMaterial.addItem(m);
        } catch (Exception e) {
            showErrorMessage("Error al cargar materiales: " + e.getMessage());
        }
    }

    private void agregarLinea() {
        Material m = (Material) cmbMaterial.getSelectedItem();
        if (m == null) { showWarningMessage("Seleccione un material"); return; }

        String cantText = txtCantidadLinea.getText().trim();
        Integer cantidad = null;
        if (!cantText.isEmpty()) {
            try { cantidad = Integer.parseInt(cantText); }
            catch (NumberFormatException ex) { showWarningMessage("Cantidad inválida"); return; }
        } else {
            showWarningMessage("Ingrese la cantidad"); return;
        }

        String dim = txtDimensionesLinea.getText().trim();
        String cortesText = txtCortesLinea.getText().trim();
        Integer cortes = null;
        if (!cortesText.isEmpty()) {
            try { cortes = Integer.parseInt(cortesText); } catch (NumberFormatException ex) { showWarningMessage("Cortes inválido"); return; }
        }
        String pesoText = txtPesoLinea.getText().trim();
        Double peso = null;
        if (!pesoText.isEmpty()) {
            try { peso = Double.parseDouble(pesoText); } catch (NumberFormatException ex) { showWarningMessage("Peso inválido"); return; }
        }

        Vector<Object> row = new Vector<>();
        row.add(null); // id_detalle (se genera en BD)
        row.add(m.getIdMaterial());
        row.add(m.getTipo()); // materialTipo (nombre)
        row.add(cantidad);
        row.add(dim);
        row.add(cortes);
        row.add(peso);
        lineasTableModel.addRow(row);

        // limpiar inputs
        txtCantidadLinea.setText("");
        txtDimensionesLinea.setText("");
        txtCortesLinea.setText("");
        txtPesoLinea.setText("");
    }

    private void savePedido() {
        try {
            if (cmbCliente.getSelectedItem() == null) {
                showWarningMessage("Debe seleccionar un cliente");
                return;
            }

            // crear pedido (sólo id_cliente y fecha_pedido)
            Pedido p = new Pedido();
            Cliente c = (Cliente) cmbCliente.getSelectedItem();
            p.setIdCliente(c.getIdCliente());
            p.setFechaPedido(LocalDate.parse(txtFechaPedido.getText().trim(), DATE_FORMATTER));

            // construir lista de detalles desde la tabla
            List<Detalle> detalles = new ArrayList<>();
            for (int r = 0; r < lineasTableModel.getRowCount(); r++) {
                Detalle d = new Detalle();
                Object idMatObj = lineasTableModel.getValueAt(r, 1);
                if (idMatObj instanceof Number) d.setIdMaterial(((Number) idMatObj).longValue());
                d.setMaterialTipo(lineasTableModel.getValueAt(r, 2) != null ? lineasTableModel.getValueAt(r, 2).toString() : null);
                Object cantObj = lineasTableModel.getValueAt(r, 3);
                if (cantObj instanceof Number) d.setCantidad(((Number) cantObj).intValue());
                else if (cantObj != null && !cantObj.toString().trim().isEmpty()) d.setCantidad(Integer.parseInt(cantObj.toString()));
                d.setDimensionesPieza(lineasTableModel.getValueAt(r, 4) != null ? lineasTableModel.getValueAt(r, 4).toString() : null);
                Object cortesObj = lineasTableModel.getValueAt(r, 5);
                if (cortesObj instanceof Number) d.setNumeroCortes(((Number) cortesObj).intValue());
                else if (cortesObj != null && !cortesObj.toString().trim().isEmpty()) d.setNumeroCortes(Integer.parseInt(cortesObj.toString()));
                Object pesoObj = lineasTableModel.getValueAt(r, 6);
                if (pesoObj instanceof Number) d.setPesoPieza(((Number) pesoObj).doubleValue());
                else if (pesoObj != null && !pesoObj.toString().trim().isEmpty()) d.setPesoPieza(Double.parseDouble(pesoObj.toString()));
                detalles.add(d);
            }

            // Validaciones mínimas
            if (detalles.isEmpty()) {
                showWarningMessage("Debe agregar al menos una línea al pedido");
                return;
            }

            // Guardar pedido + detalles (transaccional)
            // Nota: pide que PedidoDAO tenga saveWithDetails(Pedido, List<Detalle>)
            pedidoDAO.saveWithDetails(p, detalles);

            showSuccessMessage("Pedido guardado correctamente (ID: " + p.getIdPedido() + ")");
            clearForm();
        } catch (Exception e) {
            showErrorMessage("Error al guardar pedido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePedido() {
        showWarningMessage("Funcionalidad de actualizar no implementada en esta versión.");
    }

    private void performSearch() {
        try {
            if (txtBuscar == null) {
                showWarningMessage("Campo de búsqueda no disponible.");
                return;
            }
            String searchText = txtBuscar.getText().trim();
            List<Pedido> pedidos;
            if (searchText.isEmpty()) {
                pedidos = pedidoDAO.findAll();
            } else {
                pedidos = pedidoDAO.findByClienteName(searchText);
            }
            if (pedidos == null || pedidos.isEmpty()) {
                if (pedidosTableModel != null) pedidosTableModel.setRowCount(0);
                showWarningMessage("No se encontraron pedidos para: " + searchText);
            } else {
                updatePedidosTable(pedidos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al realizar la búsqueda: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            List<Pedido> pedidos = pedidoDAO.findAll();
            if (pedidos == null || pedidos.isEmpty()) {
                if (pedidosTableModel != null) pedidosTableModel.setRowCount(0);
                System.out.println("loadData: no hay pedidos");
            } else {
                System.out.println("loadData: pedidos encontrados = " + pedidos.size());
                updatePedidosTable(pedidos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar pedidos: " + e.getMessage());
        }
    }

    private void updatePedidosTable(List<Pedido> pedidos) {
        if (pedidosTableModel == null) {
            System.err.println("updatePedidosTable: pedidosTableModel es null");
            return;
        }
        pedidosTableModel.setRowCount(0);
        if (pedidos == null) return;

        for (Pedido pedido : pedidos) {
            String clienteNombre = pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "";
            String fechaPedidoStr = pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DATE_FORMATTER) : "";
            Object[] row = { pedido.getIdPedido(), clienteNombre, fechaPedidoStr };
            pedidosTableModel.addRow(row);
        }
    }

    private void clearForm() {
        if (cmbCliente != null) cmbCliente.setSelectedIndex(-1);
        if (txtFechaPedido != null) txtFechaPedido.setText(LocalDate.now().format(DATE_FORMATTER));
        if (lineasTableModel != null) lineasTableModel.setRowCount(0);
    }

    private void editSelected() {
        // Implementar si se quiere abrir ventana EDIT con pedido seleccionado
        showWarningMessage("Editar pedido no implementado (seleccione una fila y luego implementar).");
    }

    private void deleteSelected() {
        int sel = -1;
        if (pedidosTable != null) sel = pedidosTable.getSelectedRow();
        if (sel == -1) { showWarningMessage("Seleccione un pedido para eliminar"); return; }

        Object idObj = pedidosTableModel.getValueAt(sel, 0);
        Long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        int option = JOptionPane.showConfirmDialog(this, "¿Confirma eliminar pedido ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                pedidoDAO.delete(id);
                showSuccessMessage("Pedido eliminado");
                loadData();
            } catch (Exception e) {
                showErrorMessage("Error al eliminar pedido: " + e.getMessage());
            }
        }
    }

    private void openCreateWindow() {
        new PedidoWindow(this, "CREATE").setVisible(true);
    }

private void openFinalizarWindow() {
    int selectedRow = pedidosTable.getSelectedRow();
    if (selectedRow == -1) {
        showWarningMessage("Seleccioná un pedido en la lista para finalizar.");
        return;
    }

    // Obtener ID del pedido desde la tabla
    Object idObj = pedidosTableModel.getValueAt(selectedRow, 0);
    if (idObj == null) {
        showErrorMessage("No se pudo obtener el ID del pedido seleccionado.");
        return;
    }

    long idPedido;
    if (idObj instanceof Number) {
        idPedido = ((Number) idObj).longValue();
    } else {
        try {
            idPedido = Long.parseLong(idObj.toString());
        } catch (NumberFormatException e) {
            showErrorMessage("ID de pedido inválido.");
            return;
        }
    }

    try {
        // Cargar pedido completo desde DB
        Pedido pedido = pedidoDAO.findById(idPedido);
        if (pedido == null) {
            showErrorMessage("No se encontró el pedido con ID: " + idPedido);
            return;
        }

        // Abrir ventana de finalizar pedido
        FinalizarPedidoWindow finalizarWindow = new FinalizarPedidoWindow(this, pedido);
        finalizarWindow.setVisible(true);

    } catch (Exception e) {
        e.printStackTrace();
        showErrorMessage("Error al abrir ventana de finalización: " + e.getMessage());
    }
}


    private void showSuccessMessage(String m) { JOptionPane.showMessageDialog(this, m, "Éxito", JOptionPane.INFORMATION_MESSAGE); }
    private void showErrorMessage(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarningMessage(String m) { JOptionPane.showMessageDialog(this, m, "Advertencia", JOptionPane.WARNING_MESSAGE); }

    private void fillFormWithPedido(Pedido pedido) {
        if (pedido == null) return;
        // Seleccionar cliente
        if (cmbCliente != null) {
            for (int i = 0; i < cmbCliente.getItemCount(); i++) {
                Cliente cl = cmbCliente.getItemAt(i);
                if (cl != null && cl.getIdCliente() != null && cl.getIdCliente().equals(pedido.getIdCliente())) {
                    cmbCliente.setSelectedItem(cl);
                    break;
                }
            }
        }
        if (txtFechaPedido != null && pedido.getFechaPedido() != null) {
            txtFechaPedido.setText(pedido.getFechaPedido().format(DATE_FORMATTER));
        }

        // Cargar líneas del pedido si tu DetalleDAO implementa findByPedidoId
        if (lineasTableModel != null) {
            lineasTableModel.setRowCount(0);
            try {
                List<Detalle> detalles = detalleDAO.findByPedidoId(pedido.getIdPedido());
                if (detalles != null) {
                    for (Detalle d : detalles) {
                        Vector<Object> row = new Vector<>();
                        row.add(d.getIdDetalle());
                        row.add(d.getIdMaterial());
                        row.add(d.getMaterialTipo());
                        row.add(d.getCantidad());
                        row.add(d.getDimensionesPieza());
                        row.add(d.getNumeroCortes());
                        row.add(d.getPesoPieza());
                        lineasTableModel.addRow(row);
                    }
                }
            } catch (Exception e) {
                // si DetalleDAO no está implementado, no interrumpe la carga del pedido
                System.err.println("No se pudieron cargar detalles del pedido: " + e.getMessage());
            }
        }
    }
}
