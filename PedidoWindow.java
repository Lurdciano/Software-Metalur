package com.cianmetalurgica.view;

import com.cianmetalurgica.dao.ClienteDAO;
import com.cianmetalurgica.dao.DetalleDAO;
import com.cianmetalurgica.dao.MaterialDAO;
import com.cianmetalurgica.dao.PedidoDAO;
import com.cianmetalurgica.model.Detalle;
import com.cianmetalurgica.model.Material;
import com.cianmetalurgica.model.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PedidoWindow extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(44, 62, 80);

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private DetalleDAO detalleDAO = new DetalleDAO();
    private MaterialDAO materialDAO = new MaterialDAO();

    private String mode; // "LIST", "CREATE", "SEARCH", "EDIT"
    private JFrame parent;
    private Pedido pedidoToEdit;

    // Form components
    private JComboBox<Material> cmbClienteDummy; // not used - but we keep client combobox separate
    private JComboBox<String> cmbEstadoPedido;
    
    private javax.swing.JComboBox<com.cianmetalurgica.model.Cliente> cmbCliente;
    private JTextField txtFechaPedido;
    private JTextField txtFechaEntregaEstimada;
    private JTextField txtKiloCantidad;
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;

    // Detalles UI (for CREATE/EDIT)
    private JTable detallesTable;
    private DefaultTableModel detallesModel;
    private JComboBox<Material> cmbMaterialToAdd;
    private JTextField txtCantidadDetalle;
    private JTextField txtDimensionesDetalle;
    private JTextField txtCortesDetalle;
    private JTextField txtPesoDetalle;
    private JButton btnAgregarDetalle;
    private JButton btnEliminarDetalle;
    private final ClienteDAO clienteDAO;

public PedidoWindow(JFrame parent, String mode) {
    this.parent = parent;
    this.mode = mode;
    this.pedidoDAO = new PedidoDAO();
    this.clienteDAO = new ClienteDAO();
    this.detalleDAO = new DetalleDAO();

    initComponents();
    setupLayout();

    // <-- aquí cargamos los clientes para que el combo tenga contenido
    if ("CREATE".equals(mode) || "EDIT".equals(mode) || "LIST".equals(mode) || "SEARCH".equals(mode)) {
        loadClientes();
    }

    if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
        loadData();
    }

    if ("CREATE".equals(mode) || "EDIT".equals(mode)) {
        loadMaterialsForDetail();
    }
}

private void loadClientes() {
    try {
        // Crea si es null (por seguridad)
        if (cmbCliente == null) {
            cmbCliente = new JComboBox<>();
        }
        cmbCliente.removeAllItems();

        // Trae la lista desde el DAO (si clienteDAO lanza SQLException, lo capturamos)
        List<com.cianmetalurgica.model.Cliente> clientes = clienteDAO.findAll();
        for (com.cianmetalurgica.model.Cliente c : clientes) {
            cmbCliente.addItem(c);
        }

        // Renderer para mostrar el nombre (por si toString cambia)
        cmbCliente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof com.cianmetalurgica.model.Cliente) {
                    com.cianmetalurgica.model.Cliente cli = (com.cianmetalurgica.model.Cliente) value;
                    setText(cli.getNombreRazonSocial() != null ? cli.getNombreRazonSocial() : ("ID:" + cli.getIdCliente()));
                }
                return this;
            }
        });

        // opcional: seleccionar nada por defecto
        cmbCliente.setSelectedIndex(-1);

    } catch (Exception e) {
        // Mostrar el error para que no quede silencioso (muy importante para depurar)
        JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}


    public PedidoWindow(JFrame parent, String mode, Pedido pedido) {
        this(parent, mode);
        this.pedidoToEdit = pedido;
        if ("EDIT".equals(mode) && pedido != null) {
            fillFormWithPedido(pedido);
            loadDetallesIntoForm(pedido.getIdPedido());
        }
    }

    // constructor para compatibilidad con MainWindow que pasa el enum MaterialWindow.Mode
    public PedidoWindow(JFrame parent, MaterialWindow.Mode modeEnum) {
        this(parent, modeEnum != null ? modeEnum.name() : "LIST");
    }

    private void initComponents() {
        setTitle("Cian Metalúrgica - Pedidos");
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
        } else {
            add(createFormPanel(), BorderLayout.CENTER);
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        // We'll return a panel with BorderLayout: top = metadata form, center = detalles panel
        JPanel panel = new JPanel(new BorderLayout());
        JPanel metaPanel = new JPanel(new GridBagLayout());
        metaPanel.setBackground(BACKGROUND_COLOR);
        metaPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Arial", Font.PLAIN, 12);

        // Cliente - reuse existing variable name but load actual Cliente objects in run-time
        gbc.gridx = 0; gbc.gridy = 0;
        metaPanel.add(new JLabel("Cliente *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cmbCliente = new JComboBox(); // We'll populate with Cliente objects in loadClientes() if you prefer client DAO
        metaPanel.add(cmbCliente, gbc);

        // Fecha pedido
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        metaPanel.add(new JLabel("Fecha de Pedido (dd/mm/yyyy):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtFechaPedido = new JTextField(20);
        metaPanel.add(txtFechaPedido, gbc);

        // Fecha entrega estimada
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        metaPanel.add(new JLabel("Fecha Entrega Estimada (dd/mm/yyyy):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtFechaEntregaEstimada = new JTextField(20);
        metaPanel.add(txtFechaEntregaEstimada, gbc);

        // Estado (agregado)
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        metaPanel.add(new JLabel("Estado del Pedido:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cmbEstadoPedido = new JComboBox<>(new String[] { "CREADO", "EN_PROCESO", "TERMINADO", "CANCELADO" });
        metaPanel.add(cmbEstadoPedido, gbc);

        // Kilo/Cantidad
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        metaPanel.add(new JLabel("Kilo/Cantidad:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtKiloCantidad = new JTextField(20);
        metaPanel.add(txtKiloCantidad, gbc);

        panel.add(metaPanel, BorderLayout.NORTH);

        // Detalles panel (solo en CREATE/EDIT)
        JPanel detallesPanel = new JPanel(new BorderLayout());
        detallesPanel.setBackground(BACKGROUND_COLOR);
        detallesPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Pedido"));

        // Controls for adding a detail
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBackground(BACKGROUND_COLOR);
        cmbMaterialToAdd = new JComboBox<>();
        cmbMaterialToAdd.setPreferredSize(new Dimension(300, 25));
        addPanel.add(new JLabel("Material:"));
        addPanel.add(cmbMaterialToAdd);

        addPanel.add(new JLabel("Cantidad:"));
        txtCantidadDetalle = new JTextField(6);
        addPanel.add(txtCantidadDetalle);

        addPanel.add(new JLabel("Dimensiones:"));
        txtDimensionesDetalle = new JTextField(10);
        addPanel.add(txtDimensionesDetalle);

        addPanel.add(new JLabel("Cortes:"));
        txtCortesDetalle = new JTextField(4);
        addPanel.add(txtCortesDetalle);

        addPanel.add(new JLabel("Peso:"));
        txtPesoDetalle = new JTextField(6);
        addPanel.add(txtPesoDetalle);

        btnAgregarDetalle = new JButton("Agregar línea");
        btnAgregarDetalle.addActionListener(e -> onAgregarDetalle());
        addPanel.add(btnAgregarDetalle);

        btnEliminarDetalle = new JButton("Eliminar línea");
        btnEliminarDetalle.addActionListener(e -> onEliminarDetalle());
        addPanel.add(btnEliminarDetalle);

        detallesPanel.add(addPanel, BorderLayout.NORTH);

        // Tabla de detalles
        String[] cols = new String[] {
                "ID_DETALLE", "ID_MATERIAL", "Material", "Tipo", "Cantidad", "Dimensiones", "Cortes", "Peso"
        };
        detallesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                // editable: Tipo, Cantidad, Dimensiones, Cortes, Peso
                return column == 3 || column == 4 || column == 5 || column == 6 || column == 7;
            }

            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Double.class; // cantidad
                if (columnIndex == 6) return Integer.class; // cortes
                if (columnIndex == 7) return Double.class; // peso
                return String.class;
            }
        };
        detallesTable = new JTable(detallesModel);
        detallesTable.setRowHeight(24);

        // ocultar columnas ID
        TableColumn colIdDetalle = detallesTable.getColumnModel().getColumn(0);
        TableColumn colIdMaterial = detallesTable.getColumnModel().getColumn(1);
        colIdDetalle.setMinWidth(0); colIdDetalle.setMaxWidth(0); colIdDetalle.setPreferredWidth(0);
        colIdMaterial.setMinWidth(0); colIdMaterial.setMaxWidth(0); colIdMaterial.setPreferredWidth(0);

        JScrollPane scrollDet = new JScrollPane(detallesTable);
        scrollDet.setPreferredSize(new Dimension(900, 240));
        detallesPanel.add(scrollDet, BorderLayout.CENTER);

        panel.add(detallesPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(BACKGROUND_COLOR);

        if ("SEARCH".equals(mode)) {
            top.add(new JLabel("Buscar por cliente:"));
            txtBuscar = new JTextField(25);
            top.add(txtBuscar);
            JButton btnBuscar = createButton("Buscar", e -> {
                try {
                    performSearch();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al buscar pedidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            top.add(btnBuscar);
        }

        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"ID", "Cliente", "Fecha Pedido", "Fecha Entrega", "Estado", "Kilo/Cantidad"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if ("CREATE".equals(mode)) {
            panel.add(createButton("Guardar", e -> savePedido()));
            panel.add(createButton("Limpiar", e -> clearForm()));
        } else if ("EDIT".equals(mode)) {
            panel.add(createButton("Actualizar", e -> updatePedido()));
        } else if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            panel.add(createButton("Nuevo", e -> openCreateWindow()));
            panel.add(createButton("Editar", e -> editSelected()));
            panel.add(createButton("Eliminar", e -> deleteSelected()));
            panel.add(createButton("Ver Detalles", e -> viewDetails()));
            panel.add(createButton("Actualizar Lista", e -> loadData()));
        }

        panel.add(createButton("Cerrar", e -> dispose()));
        return panel;
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_HOVER_COLOR); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_COLOR); }
        });

        button.addActionListener(action);
        return button;
    }

    // -------------------- Data operations / helpers --------------------

    private void loadMaterialsForDetail() {
        try {
            List<Material> materiales = materialDAO.findAll();
            cmbMaterialToAdd.removeAllItems();
            for (Material m : materiales) cmbMaterialToAdd.addItem(m);
        } catch (Exception e) {
            // Si falla la carga de materiales, mostramos un aviso pero no bloqueamos la ventana
            JOptionPane.showMessageDialog(this, "No se pudieron cargar materiales: " + e.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void loadData() {
        try {
            List<com.cianmetalurgica.model.Pedido> pedidos = pedidoDAO.findAll();
            updateTable(pedidos);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Pedido> pedidos) {
        tableModel.setRowCount(0);
        for (Pedido p : pedidos) {
            Object[] row = {
                    p.getIdPedido(),
                    p.getClienteNombre() != null ? p.getClienteNombre() : p.getIdCliente(),
                    p.getFechaPedido() != null ? p.getFechaPedido().toString() : "",
                    p.getFechaEntregaEstimada() != null ? p.getFechaEntregaEstimada().toString() : "",
                    p.getEstadoPedido() != null ? p.getEstadoPedido() : "",
                    p.getKiloCantidad() != null ? p.getKiloCantidad().toString() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void performSearch() {
        try {
            String searchText = txtBuscar != null ? txtBuscar.getText().trim() : "";
            List<Pedido> pedidos;
            if (searchText.isEmpty()) pedidos = pedidoDAO.findAll(); else pedidos = pedidoDAO.findByClienteName(searchText);
            updateTable(pedidos);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en búsqueda: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePedido() {
        try {
            // Validaciones simples
            // (Aquí conviene validar cliente real; asumo que el combo contiene objetos Cliente si lo cargas)
            Pedido p = createPedidoFromForm();
            // Guardar pedido
            pedidoDAO.save(p);
            // Guardar detalles (si existen)
            saveDetallesForPedido(p.getIdPedido());
            JOptionPane.showMessageDialog(this, "Pedido guardado correctamente (ID: " + p.getIdPedido() + ")", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePedido() {
        try {
            if (pedidoToEdit == null) {
                JOptionPane.showMessageDialog(this, "No hay pedido para actualizar", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Pedido p = createPedidoFromForm();
            p.setIdPedido(pedidoToEdit.getIdPedido());
            pedidoDAO.update(p);
            // Simple approach: borrar detalles existentes y volver a insertar
            detalleDAO.deleteByPedidoId(p.getIdPedido());
            saveDetallesForPedido(p.getIdPedido());
            JOptionPane.showMessageDialog(this, "Pedido actualizado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Pedido createPedidoFromForm() {
        Pedido p = new Pedido();
        // Cliente: si tu combo carga Cliente, extraer id; aquí dejo como null si no lo tenés implementado
        Object clienteObj = cmbCliente != null ? cmbCliente.getSelectedItem() : null;
        if (clienteObj != null) {
            // intentar extraer id con reflexión/convención
            try {
                // Si es instancia de tu clase Cliente con getIdCliente()
                java.lang.reflect.Method m = clienteObj.getClass().getMethod("getIdCliente");
                Object idObj = m.invoke(clienteObj);
                if (idObj instanceof Number) p.setIdCliente(((Number) idObj).longValue());
            } catch (Exception ignore) {
                // si no se puede, dejamos null y el DAO deberá manejarlo (o ajustar aquí)
            }
        }

        // Fechas: para simplicidad, se deja parseo por PedidoDAO (si acepta String) o setear null
        String fechaPedidoText = txtFechaPedido != null ? txtFechaPedido.getText().trim() : "";
        if (!fechaPedidoText.isEmpty()) {
            try {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                p.setFechaPedido(java.time.LocalDate.parse(fechaPedidoText, fmt));
            } catch (Exception ex) {
                // Intentar formato ISO
                try { p.setFechaPedido(java.time.LocalDate.parse(fechaPedidoText)); } catch (Exception ex2) {}
            }
        }

        String fechaEntregaText = txtFechaEntregaEstimada != null ? txtFechaEntregaEstimada.getText().trim() : "";
        if (!fechaEntregaText.isEmpty()) {
            try {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                p.setFechaEntregaEstimada(java.time.LocalDate.parse(fechaEntregaText, fmt));
            } catch (Exception ex) {
                try { p.setFechaEntregaEstimada(java.time.LocalDate.parse(fechaEntregaText)); } catch (Exception ex2) {}
            }
        }

        // Estado
        if (cmbEstadoPedido != null && cmbEstadoPedido.getSelectedItem() != null)
            p.setEstadoPedido(cmbEstadoPedido.getSelectedItem().toString());

        // KiloCantidad
        try {
            String kilo = txtKiloCantidad != null ? txtKiloCantidad.getText().trim() : "";
            if (!kilo.isEmpty()) p.setKiloCantidad(Double.parseDouble(kilo));
        } catch (Exception ex) {
            // ignore
        }

        return p;
    }

    private void saveDetallesForPedido(Long idPedido) {
        // Recorremos detallesModel e insertamos cada detalle con detalleDAO.save(...)
        try {
            for (int r = 0; r < detallesModel.getRowCount(); r++) {
                Object idMatObj = detallesModel.getValueAt(r, 1);
                Long idMaterial = null;
                if (idMatObj instanceof Number) idMaterial = ((Number) idMatObj).longValue();
                else if (idMatObj != null) {
                    try { idMaterial = Long.valueOf(idMatObj.toString()); } catch (Exception ignored) {}
                }
                Detalle d = new Detalle();
                d.setIdPedido(idPedido);
                d.setIdMaterial(idMaterial);
                Object materialDesc = detallesModel.getValueAt(r, 2);
                d.setMaterialTipo(materialDesc != null ? materialDesc.toString() : null);

                Object cantidadObj = detallesModel.getValueAt(r, 4);
                if (cantidadObj instanceof Number) d.setCantidad(((Number) cantidadObj).intValue());
                else if (cantidadObj != null && !cantidadObj.toString().trim().isEmpty()) {
                    d.setCantidad(Integer.valueOf(cantidadObj.toString()));
                }

                d.setDimensionesPieza((String) detallesModel.getValueAt(r, 5));
                Object cortesObj = detallesModel.getValueAt(r, 6);
                if (cortesObj instanceof Number) d.setNumeroCortes(((Number) cortesObj).intValue());
                else if (cortesObj != null && !cortesObj.toString().trim().isEmpty()) d.setNumeroCortes(Integer.valueOf(cortesObj.toString()));

                Object pesoObj = detallesModel.getValueAt(r, 7);
                if (pesoObj instanceof Number) d.setPesoPieza(((Number) pesoObj).doubleValue());
                else if (pesoObj != null && !pesoObj.toString().trim().isEmpty()) d.setPesoPieza(Double.valueOf(pesoObj.toString()));

                detalleDAO.save(d);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error guardando detalles: " + ex.getMessage(), ex);
        }
    }

    private void loadDetallesIntoForm(Long pedidoId) {
        detallesModel.setRowCount(0);
        try {
            List<Detalle> detalles = detalleDAO.findByPedidoId(pedidoId);
            if (detalles != null) {
                for (Detalle d : detalles) {
                    Vector<Object> row = new Vector<>();
                    row.add(d.getIdDetalle());
                    row.add(d.getIdMaterial());
                    row.add(d.getMaterialTipo() != null ? d.getMaterialTipo() : (d.getIdMaterial() != null ? "ID:" + d.getIdMaterial() : ""));
                    row.add(""); // Tipo (si querés extraer del material, implementalo)
                    row.add(d.getCantidad() != null ? d.getCantidad().doubleValue() : 0.0);
                    row.add(d.getDimensionesPieza() != null ? d.getDimensionesPieza() : "");
                    row.add(d.getNumeroCortes() != null ? d.getNumeroCortes() : null);
                    row.add(d.getPesoPieza() != null ? d.getPesoPieza() : null);
                    detallesModel.addRow(row);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando detalles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- UI event helpers --------------------

    private void onAgregarDetalle() {
        Material sel = (Material) cmbMaterialToAdd.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un material", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String cantidadTxt = txtCantidadDetalle.getText().trim();
        double cantidad = 0.0;
        if (cantidadTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese cantidad", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            cantidad = Double.parseDouble(cantidadTxt);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vector<Object> row = new Vector<>();
        row.add(null); // id_detalle (nuevo)
        row.add(sel.getIdMaterial()); // id_material
        row.add(sel.getTipo() != null ? sel.getTipo() : sel.getTipo()); // desc material (si querés mostrar nombre, agregar campo en Material)
        row.add(sel.getTipo() != null ? sel.getTipo() : ""); // tipo
        row.add(cantidad);
        row.add(txtDimensionesDetalle.getText().trim());
        Integer cortes = null;
        try { if (!txtCortesDetalle.getText().trim().isEmpty()) cortes = Integer.valueOf(txtCortesDetalle.getText().trim()); } catch (Exception ignored) {}
        row.add(cortes);
        Double peso = null;
        try { if (!txtPesoDetalle.getText().trim().isEmpty()) peso = Double.valueOf(txtPesoDetalle.getText().trim()); } catch (Exception ignored) {}
        row.add(peso);

        detallesModel.addRow(row);
        // limpiar inputs
        txtCantidadDetalle.setText("");
        txtDimensionesDetalle.setText("");
        txtCortesDetalle.setText("");
        txtPesoDetalle.setText("");
    }

    private void onEliminarDetalle() {
        int sel = detallesTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una línea para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object idDetalleObj = detallesModel.getValueAt(sel, 0);
        if (idDetalleObj != null) {
            try {
                Long idDetalle = null;
                if (idDetalleObj instanceof Number) idDetalle = ((Number) idDetalleObj).longValue();
                else idDetalle = Long.valueOf(idDetalleObj.toString());
                if (idDetalle != null) detalleDAO.delete(idDetalle);
            } catch (Exception ex) {
                // si falla eliminar en BD, seguimos y removemos la fila localmente; el usuario verá la inconsistencia y deberá actualizar.
            }
        }
        detallesModel.removeRow(sel);
    }

    private void openCreateWindow() {
        new PedidoWindow(this, "CREATE").setVisible(true);
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Seleccione un pedido para editar", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Long pedidoId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        Pedido p = pedidoDAO.findById(pedidoId);
        if (p != null) new PedidoWindow(this, "EDIT", p).setVisible(true);
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Seleccione un pedido para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Long pedidoId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        int option = JOptionPane.showConfirmDialog(this, "¿Seguro quiere eliminar el pedido " + pedidoId + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            pedidoDAO.delete(pedidoId);
            detalleDAO.deleteByPedidoId(pedidoId);
            loadData();
        }
    }

    private void viewDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Seleccione un pedido", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Long pedidoId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        List<Detalle> detalles = detalleDAO.findByPedidoId(pedidoId);
        // Mostrar dialog con los detalles
        StringBuilder sb = new StringBuilder();
        for (Detalle d : detalles) {
            sb.append("Material: ").append(d.getMaterialTipo()).append("  Cant: ").append(d.getCantidad()).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.length() > 0 ? sb.toString() : "No hay detalles", "Detalles", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        txtFechaPedido.setText("");
        txtFechaEntregaEstimada.setText("");
        txtKiloCantidad.setText("");
        if (cmbEstadoPedido != null) cmbEstadoPedido.setSelectedIndex(0);
        detallesModel.setRowCount(0);
    }

    private void fillFormWithPedido(Pedido p) {
        if (p == null) return;
        // cargar campos (cliente no implementado aquí)
        if (p.getFechaPedido() != null) txtFechaPedido.setText(p.getFechaPedido().toString());
        if (p.getFechaEntregaEstimada() != null) txtFechaEntregaEstimada.setText(p.getFechaEntregaEstimada().toString());
        if (p.getEstadoPedido() != null) cmbEstadoPedido.setSelectedItem(p.getEstadoPedido());
        if (p.getKiloCantidad() != null) txtKiloCantidad.setText(p.getKiloCantidad().toString());
    }

   
}
