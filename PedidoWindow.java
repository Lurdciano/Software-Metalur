package com.cianmetalurgica.view;

import com.cianmetalurgica.config.DatabaseConnection;
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
    private String mode; // "LIST", "CREATE", "SEARCH", "EDIT"
    private JFrame parent;
    private Pedido pedidoToEdit;

    // componentes form (cabecera)
    private JComboBox<Cliente> cmbCliente;
    private JTextField txtFechaPedido;

    // material / lineas (CREATE/EDIT)
    private JComboBox<Material> cmbMaterial;
    private JTextField txtCantidadLinea;
    private JTextField txtDimensionesLinea;
    private JTextField txtCortesLinea;
    private JTextField txtPesoLinea;
    private JButton btnAgregarLinea;
    private JTable table; // tabla de líneas (pedido en edición/creación)
    private DefaultTableModel tableModel;

    // Lista/Buscar pedidos (LIST/SEARCH)
    private JTable pedidosTable;
    private DefaultTableModel pedidosTableModel;
    private JTextField txtBuscar;

    private DetalleDAO detalleDAO;

    public PedidoWindow(JFrame parent, String mode) {
        this.parent = parent;
        this.mode = mode;
        this.pedidoDAO = new PedidoDAO();
        this.clienteDAO = new ClienteDAO();
        this.materialDAO = new MaterialDAO();
        this.detalleDAO = new DetalleDAO();

        initComponents();
        setupLayout();

        // cargar datos según modo
        if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            loadData();
        }
        if ("CREATE".equals(mode) || "EDIT".equals(mode)) {
            loadClientes();
            loadMaterials();
        }
    }

    public PedidoWindow(JFrame parent, String mode, Pedido pedido) {
        this(parent, mode);
        this.pedidoToEdit = pedido;
        if ("EDIT".equals(mode) && pedido != null) {
            // ensure UI components created and combos loaded
            loadClientes();
            loadMaterials();
            fillFormWithPedido(pedido);
        }
    }

    // Constructor helper para compatibilidad con MaterialWindow.Mode enum
    public PedidoWindow(JFrame parent, MaterialWindow.Mode mode) {
        this(parent, mode != null ? mode.name() : "LIST");
    }

    private void initComponents() {
        setTitle("Cian Metalúrgica - " + ("CREATE".equals(mode) ? "Nuevo Pedido" :
                ("EDIT".equals(mode) ? "Editar Pedido" : "Lista de Pedidos")));
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

        // Content
        if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            add(createListPanel(), BorderLayout.CENTER);
        } else { // CREATE or EDIT
            add(createFormPanel(), BorderLayout.CENTER);
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // ------------------------
    // PANEL: FORM (CREATE/EDIT)
    // ------------------------
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
        cmbCliente.setPreferredSize(new Dimension(300, 25));
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
        cmbMaterial.setPreferredSize(new Dimension(250,25));
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

        // Tabla de líneas (CREATE/EDIT)
        String[] cols = {"ID_DETALLE","ID_MATERIAL","Material","Cantidad","Dimensiones","Cortes","Peso","Descontar"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // permitir editar: Cantidad, Dimensiones, Cortes, Peso, Descontar
                return col == 3 || col == 4 || col == 5 || col == 6 || col == 7;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                // cols = {"ID_DETALLE","ID_MATERIAL","Material","Cantidad","Dimensiones","Cortes","Peso","Descontar"};
                switch (col) {
                    case 0: return Long.class;    // ID_DETALLE
                    case 1: return Long.class;    // ID_MATERIAL
                    case 2: return String.class;  // Material (nombre)
                    case 3: return Integer.class; // Cantidad
                    case 4: return String.class;  // Dimensiones
                    case 5: return Integer.class; // Cortes
                    case 6: return Double.class;  // Peso
                    case 7: return Boolean.class; // Descontar
                    default: return Object.class;
                }
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Líneas del pedido"));
        center.add(sp, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------
    // PANEL: LIST / SEARCH
    // ------------------------
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

        // tabla general de pedidos: referencia de clase para updateTable
        String[] columns = {"ID", "Cliente", "Fecha Pedido"};
        pedidosTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Long.class;
                return Object.class;
            }
        };
        pedidosTable = new JTable(pedidosTableModel);
        pedidosTable.setRowHeight(24);
        pedidosTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(pedidosTable);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------
    // BUTTONS PANEL
    // ------------------------
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

    // ------------------------
    // LOADERS
    // ------------------------
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
        row.add(null); // id_detalle
        row.add(m.getIdMaterial());        // id_material (Long)
        row.add(m.getTipo());             // materialTipo (String)
        row.add(cantidad);                // cantidad (Integer)
        row.add(dim);                     // dimensiones (String)
        row.add(cortes);                  // cortes (Integer)
        row.add(peso);                    // peso (Double)
        row.add(Boolean.FALSE);           // descontar (Boolean) por defecto

        tableModel.addRow(row);

        // limpiar inputs
        txtCantidadLinea.setText("");
        txtDimensionesLinea.setText("");
        txtCortesLinea.setText("");
        txtPesoLinea.setText("");
    }

    // ------------------------
    // GUARDAR PEDIDO + DETALLES (usa pedidoDAO.saveWithDetails)
    // ------------------------
    private void savePedido() {
        try {
            if (cmbCliente.getSelectedItem() == null) {
                showWarningMessage("Debe seleccionar un cliente");
                return;
            }

            // crear pedido
            Pedido p = new Pedido();
            Cliente c = (Cliente) cmbCliente.getSelectedItem();
            p.setIdCliente(c.getIdCliente());
            p.setFechaPedido(LocalDate.parse(txtFechaPedido.getText().trim(), DATE_FORMATTER));

            // construir lista de detalles desde la tabla
            List<Detalle> detalles = new ArrayList<>();
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Detalle d = new Detalle();

                Object idMatObj = tableModel.getValueAt(r, 1);
                if (idMatObj instanceof Number) d.setIdMaterial(((Number) idMatObj).longValue());
                else if (idMatObj instanceof String) {
                    try { d.setIdMaterial(Long.parseLong((String) idMatObj)); } catch (NumberFormatException ex) { d.setIdMaterial(null); }
                }

                d.setMaterialTipo(tableModel.getValueAt(r, 2) != null ? tableModel.getValueAt(r, 2).toString() : null);

                Object cantObj = tableModel.getValueAt(r, 3);
                if (cantObj instanceof Number) d.setCantidad(((Number) cantObj).intValue());
                else if (cantObj != null && !cantObj.toString().trim().isEmpty()) d.setCantidad(Integer.parseInt(cantObj.toString()));

                d.setDimensionesPieza(tableModel.getValueAt(r, 4) != null ? tableModel.getValueAt(r, 4).toString() : null);

                Object cortesObj = tableModel.getValueAt(r, 5);
                if (cortesObj instanceof Number) d.setNumeroCortes(((Number) cortesObj).intValue());
                else if (cortesObj != null && !cortesObj.toString().trim().isEmpty()) d.setNumeroCortes(Integer.parseInt(cortesObj.toString()));

                Object pesoObj = tableModel.getValueAt(r, 6);
                if (pesoObj instanceof Number) d.setPesoPieza(((Number) pesoObj).doubleValue());
                else if (pesoObj != null && !pesoObj.toString().trim().isEmpty()) d.setPesoPieza(Double.parseDouble(pesoObj.toString()));

                // Leer la columna Descontar (index 7)
                Object descontarObj = tableModel.getValueAt(r, 7);
                boolean descontar = false;
                if (descontarObj instanceof Boolean) descontar = (Boolean) descontarObj;
                else if (descontarObj instanceof Number) descontar = ((Number) descontarObj).intValue() != 0;
                else if (descontarObj instanceof String) descontar = "1".equals(descontarObj) || "true".equalsIgnoreCase((String)descontarObj);

                // Mapear a entero 0/1 si tu modelo lo espera
                d.setDescontarStock(descontar ? 1 : 0);

                d.setIdPedido(null); // será asignado por saveWithDetails cuando se inserte el pedido

                detalles.add(d);
            }

            if (detalles.isEmpty()) {
                showWarningMessage("Debe agregar al menos una línea al pedido");
                return;
            }

            // Guardar transaccionalmente (asegurate que pedidoDAO.saveWithDetails exista y funcione)
            pedidoDAO.saveWithDetails(p, detalles);

            showSuccessMessage("Pedido guardado correctamente (ID: " + p.getIdPedido() + ")");
            clearForm();
        } catch (Exception e) {
            showErrorMessage("Error al guardar pedido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePedido() {
        try {
            if (pedidoToEdit == null) {
                showWarningMessage("No hay pedido seleccionado para editar");
                return;
            }

            // actualizar datos generales del pedido si hubiera (aquí sólo fecha y cliente)
            if (cmbCliente.getSelectedItem() == null) {
                showWarningMessage("Debe seleccionar un cliente");
                return;
            }
            Cliente c = (Cliente) cmbCliente.getSelectedItem();
            pedidoToEdit.setIdCliente(c.getIdCliente());
            try {
                pedidoToEdit.setFechaPedido(LocalDate.parse(txtFechaPedido.getText().trim(), DATE_FORMATTER));
            } catch (Exception ex) {
                showWarningMessage("Fecha inválida. Use dd/MM/yyyy");
                return;
            }

            // Armamos la lista de detalles tomando los valores de la tabla
            List<Detalle> detalles = new ArrayList<>();
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                try {
                    Detalle d = new Detalle();

                    Object idDetalleObj = tableModel.getValueAt(r, 0);
                    Object idMaterialObj = tableModel.getValueAt(r, 1);
                    Object materialTipoObj = tableModel.getValueAt(r, 2);
                    Object cantidadObj = tableModel.getValueAt(r, 3);
                    Object dimensionesObj = tableModel.getValueAt(r, 4);
                    Object cortesObj = tableModel.getValueAt(r, 5);
                    Object pesoObj = tableModel.getValueAt(r, 6);
                    Object descontarObj = tableModel.getValueAt(r, 7);

                    // idDetalle (si existe)
                    Long idDetalle = toLongSafe(idDetalleObj);
                    if (idDetalle != null) d.setIdDetalle(idDetalle);

                    // idMaterial
                    Long idMaterial = toLongSafe(idMaterialObj);
                    if (idMaterial == null) {
                        throw new IllegalArgumentException("Fila " + (r+1) + ": id_material inválido -> " + String.valueOf(idMaterialObj));
                    }
                    d.setIdMaterial(idMaterial);

                    // materialTipo (nombre)
                    d.setMaterialTipo(materialTipoObj != null ? materialTipoObj.toString() : null);

                    // cantidad
                    Integer cantidad = toIntegerSafe(cantidadObj);
                    d.setCantidad(cantidad);

                    // dimensiones
                    d.setDimensionesPieza(dimensionesObj != null ? dimensionesObj.toString() : null);

                    // cortes
                    Integer cortes = toIntegerSafe(cortesObj);
                    d.setNumeroCortes(cortes);

                    // peso
                    Double peso = toDoubleSafe(pesoObj);
                    d.setPesoPieza(peso);

                    // descontar
                    boolean descontar = false;
                    if (descontarObj instanceof Boolean) descontar = (Boolean) descontarObj;
                    else if (descontarObj instanceof Number) descontar = ((Number) descontarObj).intValue() != 0;
                    else if (descontarObj instanceof String) descontar = "1".equals(descontarObj) || "true".equalsIgnoreCase((String)descontarObj);
                    d.setDescontarStock(descontar ? 1 : 0);

                    // Setear id_pedido
                    d.setIdPedido(pedidoToEdit.getIdPedido());

                    detalles.add(d);
                } catch (NumberFormatException nf) {
                    showErrorMessage("Error en fila " + (r+1) + ": valor numérico inválido (" + nf.getMessage() + ")");
                    return;
                } catch (IllegalArgumentException ia) {
                    showErrorMessage("Error en fila " + (r+1) + ": " + ia.getMessage());
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorMessage("Error procesando la fila " + (r+1) + ": " + ex.getMessage());
                    return;
                }
            }

            if (detalles.isEmpty()) {
                showWarningMessage("Debe haber al menos una línea en el pedido.");
                return;
            }

            // Aquí deberías tener un método transaccional que actualice pedido y sus detalles,
            // por ejemplo pedidoDAO.updateWithDetails(pedidoToEdit, detalles);
            pedidoDAO.updateWithDetails(pedidoToEdit, detalles);

            showSuccessMessage("Pedido actualizado correctamente.");
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error actualizando pedido: " + e.getMessage());
        }
    }

    // ---------- helpers seguros ----------
    private Long toLongSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        if (o instanceof String) {
            String s = ((String) o).trim();
            if (s.isEmpty()) return null;
            try { return Long.parseLong(s); } catch (NumberFormatException ex) { return null; }
        }
        return null;
    }

    private Integer toIntegerSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        String s = o.toString().trim();
        if (s.isEmpty()) return null;
        return Integer.parseInt(s);
    }

    private Double toDoubleSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        String s = o.toString().trim();
        if (s.isEmpty()) return null;
        return Double.parseDouble(s);
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
            updateTable(pedidos);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al realizar la búsqueda: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            List<Pedido> pedidos = pedidoDAO.findAll();
            if (pedidos == null || pedidos.isEmpty()) {
                pedidosTableModel.setRowCount(0);
                System.out.println("loadData: no hay pedidos");
            } else {
                System.out.println("loadData: pedidos encontrados = " + pedidos.size());
                updateTable(pedidos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar pedidos: " + e.getMessage());
        }
    }

    // ------------------------
    // TABLA: actualizar visualmente la tabla de pedidos (LIST)
    // ------------------------
    private void updateTable(List<Pedido> pedidos) {
        if (pedidosTableModel == null) {
            System.err.println("updateTable: pedidosTableModel es null");
            return;
        }
        try {
            pedidosTableModel.setRowCount(0);
            if (pedidos == null || pedidos.isEmpty()) return;

            for (Pedido pedido : pedidos) {
                Object[] row = new Object[] {
                        pedido.getIdPedido(),
                        pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "",
                        pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DATE_FORMATTER) : ""
                };
                pedidosTableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error actualizando la tabla de pedidos: " + ex.getMessage());
        }
    }

    // ------------------------
    // UTIL / acciones sobre la lista
    // ------------------------
    private void clearForm() {
        cmbCliente.setSelectedIndex(-1);
        txtFechaPedido.setText(LocalDate.now().format(DATE_FORMATTER));
        if (tableModel != null) tableModel.setRowCount(0);
    }

    private void editSelected() {
        int selected = pedidosTable.getSelectedRow();
        if (selected == -1) { showWarningMessage("Seleccione un pedido para editar"); return; }
        Object idObj = pedidosTableModel.getValueAt(selected, 0);
        if (idObj == null) { showErrorMessage("ID inválido"); return; }
        long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        Pedido p = pedidoDAO.findById(id);
        if (p == null) { showErrorMessage("Pedido no encontrado: " + id); return; }
        new PedidoWindow(this, "EDIT", p).setVisible(true);
    }

    private void deleteSelected() {
        int selected = pedidosTable.getSelectedRow();
        if (selected == -1) { showWarningMessage("Seleccione un pedido para eliminar"); return; }
        Object idObj = pedidosTableModel.getValueAt(selected, 0);
        if (idObj == null) { showErrorMessage("ID inválido"); return; }
        long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        int opt = JOptionPane.showConfirmDialog(this, "¿Confirma eliminar pedido ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            pedidoDAO.delete(id);
            showSuccessMessage("Pedido eliminado");
            loadData();
        }
    }

    private void openCreateWindow() {
        new PedidoWindow(this, "CREATE").setVisible(true);
    }

    private void openFinalizarWindow() {
        int selected = pedidosTable.getSelectedRow();
        if (selected == -1) { showWarningMessage("Seleccioná un pedido en la lista para finalizar."); return; }
        Object idObj = pedidosTableModel.getValueAt(selected, 0);
        if (idObj == null) { showErrorMessage("No se pudo obtener el ID del pedido seleccionado."); return; }
        long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        Pedido pedido = pedidoDAO.findById(id);
        if (pedido == null) { showErrorMessage("Pedido no encontrado: " + id); return; }
        FinalizarPedidoWindow fpw = new FinalizarPedidoWindow(this, pedido);
        fpw.setVisible(true);
    }

    // mensajes
    private void showSuccessMessage(String m) { JOptionPane.showMessageDialog(this, m, "Éxito", JOptionPane.INFORMATION_MESSAGE); }
    private void showErrorMessage(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarningMessage(String m) { JOptionPane.showMessageDialog(this, m, "Advertencia", JOptionPane.WARNING_MESSAGE); }

    private void fillFormWithPedido(Pedido pedido) {
        if (pedido == null) return;
        // Si querés cargar un pedido para editar: cargar cabecera y detalles
        try {
            // cargar cabecera
            if (pedido.getIdCliente() != null) {
                // asegurar que el combo esté relleno
                if (cmbCliente.getItemCount() == 0) loadClientes();
                for (int i = 0; i < cmbCliente.getItemCount(); i++) {
                    Cliente c = cmbCliente.getItemAt(i);
                    if (c != null && c.getIdCliente() != null && c.getIdCliente().equals(pedido.getIdCliente())) {
                        cmbCliente.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (pedido.getFechaPedido() != null) txtFechaPedido.setText(pedido.getFechaPedido().format(DATE_FORMATTER));

            // cargar líneas (manteniendo el orden correcto de columnas)
            if (tableModel == null) return;
            tableModel.setRowCount(0);
            List<Detalle> detalles = detalleDAO.findByPedidoId(pedido.getIdPedido());
            for (Detalle d : detalles) {
                Vector<Object> row = new Vector<>();
                row.add(d.getIdDetalle());                                 // ID_DETALLE
                row.add(d.getIdMaterial());                                // ID_MATERIAL
                row.add(d.getMaterialTipo() != null ? d.getMaterialTipo() : ""); // Material (nombre)
                row.add(d.getCantidad());                                  // Cantidad
                row.add(d.getDimensionesPieza());                          // Dimensiones
                row.add(d.getNumeroCortes());                              // Cortes
                row.add(d.getPesoPieza());                                 // Peso
                // descontarStock puede ser Integer 0/1 o Boolean; normalizamos a Boolean
                Boolean desc = Boolean.FALSE;
                if (d.getDescontarStock() != null) desc = d.getDescontarStock() != 0;
                row.add(desc);                                             // Descontar (Boolean)
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error cargando pedido para editar: " + e.getMessage());
        }
    }
}
