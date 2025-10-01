package com.cianmetalurgica.view;

import com.cianmetalurgica.dao.PedidoDAO;
import com.cianmetalurgica.dao.ClienteDAO;
import com.cianmetalurgica.dao.DetalleDAO;
import com.cianmetalurgica.model.Pedido;
import com.cianmetalurgica.model.Cliente;
import com.cianmetalurgica.model.Detalle;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PedidoWindow extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(44, 62, 80);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private com.cianmetalurgica.service.PedidoService pedidoService;
    private ClienteDAO clienteDAO;
    private DetalleDAO detalleDAO;
    private String mode; // "LIST", "CREATE", "SEARCH", "EDIT"
    private JFrame parent;
    private Pedido pedidoToEdit;

    // Debounce timer para evitar muchas búsquedas por tecla
private javax.swing.Timer clienteSearchTimer;

    
    // Componentes del formulario
    private JComboBox<Cliente> cmbCliente;
    private JTextField txtClienteEditor; // editor dentro del combo para escucha
    private JTextField txtFechaPedido;
    private JTextField txtFechaEntregaEstimada;
    private JTextField txtKiloCantidad;
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;

    public PedidoWindow(JFrame parent, String mode) {
        this.parent = parent;
        this.mode = mode;
        this.pedidoService = new com.cianmetalurgica.service.PedidoService();
        this.clienteDAO = new ClienteDAO();
        this.detalleDAO = new DetalleDAO();

        initComponents();
        setupLayout();

        if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            loadData();
        }

        if ("CREATE".equals(mode) || "EDIT".equals(mode)) {
            loadClientes();
            setupClienteAutoComplete();
        }
    }

    public PedidoWindow(JFrame parent, String mode, Pedido pedido) {
        this(parent, mode);
        this.pedidoToEdit = pedido;
        if ("EDIT".equals(mode) && pedido != null) {
            fillFormWithPedido(pedido);
        }
    }

    // Constructor compatible si MainWindow usa el enum Mode (MaterialWindow.Mode)
    public PedidoWindow(JFrame parent, MaterialWindow.Mode mode) {
        this(parent, mode != null ? mode.name() : "LIST");
    }

    private void initComponents() {
        String title = "";
        switch (mode) {
            case "LIST": title = "Lista de Pedidos"; break;
            case "CREATE": title = "Nuevo Pedido"; break;
            case "SEARCH": title = "Buscar Pedidos"; break;
            case "EDIT": title = "Editar Pedido"; break;
        }

        setTitle("Cian Metalúrgica - " + title);
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
        titleLabel.setForeground(Color.BLACK);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Content
        if ("LIST".equals(mode) || "SEARCH".equals(mode)) {
            add(createListPanel(), BorderLayout.CENTER);
        } else {
            add(createFormPanel(), BorderLayout.CENTER);
        }

        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Arial", Font.PLAIN, 12);

        // Cliente (editable combo con autocompletado)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblCliente = new JLabel("Cliente *:");
        lblCliente.setFont(labelFont);
        panel.add(lblCliente, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cmbCliente = new JComboBox<>();
        cmbCliente.setEditable(true);
        cmbCliente.setFont(labelFont);
        panel.add(cmbCliente, gbc);

        // Fecha de pedido
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel lblFechaPedido = new JLabel("Fecha de Pedido (dd/mm/yyyy):");
        lblFechaPedido.setFont(labelFont);
        panel.add(lblFechaPedido, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtFechaPedido = new JTextField(30);
        txtFechaPedido.setFont(labelFont);
        txtFechaPedido.setText(LocalDate.now().format(DATE_FORMATTER));
        panel.add(txtFechaPedido, gbc);

        // Fecha entrega estimada
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel lblFechaEntrega = new JLabel("Fecha Entrega Estimada (dd/mm/yyyy):");
        lblFechaEntrega.setFont(labelFont);
        panel.add(lblFechaEntrega, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtFechaEntregaEstimada = new JTextField(30);
        txtFechaEntregaEstimada.setFont(labelFont);
        panel.add(txtFechaEntregaEstimada, gbc);

        // Kilo/Cantidad
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel lblKilo = new JLabel("Kilo/Cantidad:");
        lblKilo.setFont(labelFont);
        panel.add(lblKilo, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtKiloCantidad = new JTextField(30);
        txtKiloCantidad.setFont(labelFont);
        panel.add(txtKiloCantidad, gbc);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if ("SEARCH".equals(mode)) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBackground(BACKGROUND_COLOR);

            JLabel lblBuscar = new JLabel("Buscar por cliente:");
            lblBuscar.setFont(new Font("Arial", Font.PLAIN, 12));
            searchPanel.add(lblBuscar);

            txtBuscar = new JTextField(25);
            txtBuscar.setFont(new Font("Arial", Font.PLAIN, 12));
            searchPanel.add(txtBuscar);

            JButton btnBuscar = createButton("Buscar", e -> performSearch());
            searchPanel.add(btnBuscar);

            panel.add(searchPanel, BorderLayout.NORTH);
        }

        // Tabla (sin columnas Estado/Forma)
        String[] columns = {"ID", "Cliente", "Fecha Pedido", "Fecha Entrega", "Kilo/Cantidad"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(BUTTON_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

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
            panel.add(createButton("Ver Detalles", e -> viewDetails()));
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

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_HOVER_COLOR); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_COLOR); }
        });

        button.addActionListener(action);
        return button;
    }

    private void loadClientes() {
        try {
            List<Cliente> clientes = clienteDAO.findAll();
            cmbCliente.removeAllItems();
            for (Cliente cliente : clientes) {
                cmbCliente.addItem(cliente);
            }
            // obtener referencia al editor para autocompletar
            setupClienteEditorRef();
        } catch (SQLException e) {
            showErrorMessage("Error al cargar clientes: " + e.getMessage());
        }
    }

    private void setupClienteEditorRef() {
    Component editorComp = cmbCliente.getEditor().getEditorComponent();
    if (editorComp instanceof JTextField) {
        txtClienteEditor = (JTextField) editorComp;
    } else {
        txtClienteEditor = null;
    }
}


    private void setupClienteAutoComplete() {
    setupClienteEditorRef();
    if (txtClienteEditor == null) return;

    // Si ya existe, cancela
    if (clienteSearchTimer != null) {
        clienteSearchTimer.stop();
    }

    // Debounce: espera 300ms después de la última tecla antes de buscar
    clienteSearchTimer = new javax.swing.Timer(300, e -> {
        // capturar el texto actual del editor
        final String text = txtClienteEditor.getText() != null ? txtClienteEditor.getText().trim() : "";

        // Si el texto está vacío, limpiar sugerencias (opcional: podrías mostrar todos)
        if (text.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                DefaultComboBoxModel<Cliente> model = new DefaultComboBoxModel<>();
                cmbCliente.setModel(model);
                cmbCliente.setPopupVisible(false);
                // preservar editor
                cmbCliente.getEditor().setItem(text);
            });
            return;
        }

        // Ejecutar la consulta en background para no bloquear la UI
        SwingWorker<List<Cliente>, Void> worker = new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() throws Exception {
                // **ESTO** corre fuera del EDT
                try {
                    return clienteDAO.findByName(text);
                } catch (SQLException ex) {
                    // retornar lista vacía en caso de error para no romper la UI
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Cliente> matches = get(); // ya en EDT
                    DefaultComboBoxModel<Cliente> model = new DefaultComboBoxModel<>();
                    for (Cliente c : matches) model.addElement(c);

                    // Mantener el texto que el usuario escribió
                    String currentText = txtClienteEditor.getText();

                    cmbCliente.setModel(model);
                    // Establecer el editor a lo escrito por el usuario
                    cmbCliente.getEditor().setItem(currentText);

                    // Mostrar popup solo si hay coincidencias
                    cmbCliente.setPopupVisible(!matches.isEmpty());
                } catch (Exception ex) {
                    // Silenciar o loggear; no romper la UI
                }
            }
        };

        worker.execute();
    });

    clienteSearchTimer.setRepeats(false); // que se dispare sólo después de idle
    // DocumentListener que reinicia el timer cada vez que el usuario teclea
    txtClienteEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        private void scheduleSearch() {
            if (clienteSearchTimer.isRunning()) {
                clienteSearchTimer.restart();
            } else {
                clienteSearchTimer.start();
            }
        }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
    });
}


    private void loadData() {
    List<Pedido> pedidos = pedidoService.getAllPedidos();
    updateTable(pedidos);
}


    private void updateTable(List<Pedido> pedidos) {
        tableModel.setRowCount(0);
        for (Pedido pedido : pedidos) {
            Object[] row = {
                pedido.getIdPedido(),
                pedido.getClienteNombre(),
                pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DATE_FORMATTER) : "",
                pedido.getFechaEntregaEstimada() != null ?
                    pedido.getFechaEntregaEstimada().format(DATE_FORMATTER) : "",
                pedido.getKiloCantidad() != null ? pedido.getKiloCantidad().toString() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void performSearch() {
    String searchText = txtBuscar.getText().trim();
    List<Pedido> pedidos;
    if (searchText.isEmpty()) {
        pedidos = pedidoService.getAllPedidos();
    } else {
        pedidos = pedidoService.searchPedidosByClienteName(searchText);
    }
    updateTable(pedidos);
}


    private void savePedido() {
        try {
            if (!validateForm()) return;

            Pedido pedido = createPedidoFromForm();
            pedidoService.savePedido(pedido);
            showSuccessMessage("Pedido guardado exitosamente");
            clearForm();
        } catch (Exception e) {
            showErrorMessage("Error: " + e.getMessage());
        }
    }

    private void updatePedido() {
        try {
            if (!validateForm()) return;
            if (pedidoToEdit == null) {
                showErrorMessage("No hay pedido seleccionado para editar");
                return;
            }

            Pedido pedido = createPedidoFromForm();
            pedido.setIdPedido(pedidoToEdit.getIdPedido());
            pedidoService.updatePedido(pedido);
            showSuccessMessage("Pedido actualizado exitosamente");
            dispose();
        } catch (Exception e) {
            showErrorMessage("Error: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        Object sel = cmbCliente.getSelectedItem();
        if (sel == null || !(sel instanceof Cliente)) {
            showErrorMessage("Debe seleccionar un cliente válido");
            txtClienteEditor.requestFocus();
            return false;
        }

        String fechaPedidoText = txtFechaPedido.getText().trim();
        if (fechaPedidoText.isEmpty()) {
            showErrorMessage("La fecha de pedido es obligatoria");
            txtFechaPedido.requestFocus();
            return false;
        }

        try {
            LocalDate.parse(fechaPedidoText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showErrorMessage("Formato de fecha inválido. Use dd/mm/yyyy");
            txtFechaPedido.requestFocus();
            return false;
        }

        String fechaEntregaText = txtFechaEntregaEstimada.getText().trim();
        if (!fechaEntregaText.isEmpty()) {
            try {
                LocalDate.parse(fechaEntregaText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                showErrorMessage("Formato de fecha de entrega inválido. Use dd/mm/yyyy");
                txtFechaEntregaEstimada.requestFocus();
                return false;
            }
        }

        String kiloCantidadText = txtKiloCantidad.getText().trim();
        if (!kiloCantidadText.isEmpty()) {
            try {
                Double.parseDouble(kiloCantidadText);
            } catch (NumberFormatException e) {
                showErrorMessage("La cantidad debe ser un número válido");
                txtKiloCantidad.requestFocus();
                return false;
            }
        }

        return true;
    }

    private Pedido createPedidoFromForm() {
        Pedido pedido = new Pedido();

        Object sel = cmbCliente.getSelectedItem();
        Cliente cliente;
        if (sel instanceof Cliente) {
            cliente = (Cliente) sel;
        } else {
            // si el editor solo tiene texto, intentar buscar primer match
            String text = cmbCliente.getEditor().getItem().toString();
            try {
                List<Cliente> matches = clienteDAO.findByName(text);
                cliente = matches.isEmpty() ? null : matches.get(0);
            } catch (SQLException ex) {
                cliente = null;
            }
        }

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no válido");
        }

        pedido.setIdCliente(cliente.getIdCliente());
        pedido.setClienteNombre(cliente.getNombreRazonSocial());

        pedido.setFechaPedido(LocalDate.parse(txtFechaPedido.getText().trim(), DATE_FORMATTER));

        String fechaEntregaText = txtFechaEntregaEstimada.getText().trim();
        if (!fechaEntregaText.isEmpty()) {
            pedido.setFechaEntregaEstimada(LocalDate.parse(fechaEntregaText, DATE_FORMATTER));
        }

        String kiloCantidadText = txtKiloCantidad.getText().trim();
        if (!kiloCantidadText.isEmpty()) {
            pedido.setKiloCantidad(Double.parseDouble(kiloCantidadText));
        }

        return pedido;
    }

    private void fillFormWithPedido(Pedido pedido) {
        // Seleccionar cliente si está cargado
        loadClientes(); // cargar clientes para intentar seleccionar
        for (int i = 0; i < cmbCliente.getItemCount(); i++) {
            Cliente cliente = cmbCliente.getItemAt(i);
            if (cliente != null && cliente.getIdCliente() != null && cliente.getIdCliente().equals(pedido.getIdCliente())) {
                cmbCliente.setSelectedItem(cliente);
                break;
            }
        }

        txtFechaPedido.setText(pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DATE_FORMATTER) : "");
        txtFechaEntregaEstimada.setText(pedido.getFechaEntregaEstimada() != null ? pedido.getFechaEntregaEstimada().format(DATE_FORMATTER) : "");
        txtKiloCantidad.setText(pedido.getKiloCantidad() != null ? pedido.getKiloCantidad().toString() : "");
    }

    private void clearForm() {
        cmbCliente.setSelectedIndex(-1);
        txtFechaPedido.setText(LocalDate.now().format(DATE_FORMATTER));
        txtFechaEntregaEstimada.setText("");
        txtKiloCantidad.setText("");
    }

    private void openCreateWindow() {
        new PedidoWindow(this, "CREATE").setVisible(true);
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un pedido para editar");
            return;
        }

        Long pedidoId = (Long) tableModel.getValueAt(selectedRow, 0);
        Pedido pedido = pedidoService.getPedidoById(pedidoId);
        if (pedido != null) {
            new PedidoWindow(this, "EDIT", pedido).setVisible(true);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un pedido para eliminar");
            return;
        }

        String clienteNombre = (String) tableModel.getValueAt(selectedRow, 1);
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea eliminar el pedido del cliente:\n" + clienteNombre + "?\n\n" +
            "ADVERTENCIA: Se eliminarán también todos los detalles del pedido.",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            Long pedidoId = (Long) tableModel.getValueAt(selectedRow, 0);
            pedidoService.deletePedido(pedidoId);
            showSuccessMessage("Pedido eliminado exitosamente");
            loadData();
        }
    }

    private void viewDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un pedido para ver los detalles");
            return;
        }

        Long pedidoId = (Long) tableModel.getValueAt(selectedRow, 0);
        String clienteNombre = (String) tableModel.getValueAt(selectedRow, 1);

        List<Detalle> detalles = detalleDAO.findByPedidoId(pedidoId);
        showDetallesDialog(pedidoId, clienteNombre, detalles);
    }

    private void showDetallesDialog(Long pedidoId, String clienteNombre, List<Detalle> detalles) {
        JDialog dialog = new JDialog(this, "Detalles del Pedido #" + pedidoId, true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblInfo = new JLabel("Cliente: " + clienteNombre + " - Pedido #" + pedidoId);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblInfo, BorderLayout.NORTH);

        // Tabla de detalles
        String[] columns = {"ID", "Material", "Cantidad", "Dimensiones", "Cortes", "Peso"};
        DefaultTableModel detalleModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Detalle detalle : detalles) {
            Object[] row = {
                detalle.getIdDetalle(),
                detalle.getMaterialTipo(),
                detalle.getCantidad(),
                detalle.getDimensionesPieza() != null ? detalle.getDimensionesPieza() : "",
                detalle.getNumeroCortes() != null ? detalle.getNumeroCortes().toString() : "",
                detalle.getPesoPieza() != null ? detalle.getPesoPieza().toString() : ""
            };
            detalleModel.addRow(row);
        }

        JTable detalleTable = new JTable(detalleModel);
        detalleTable.setFont(new Font("Arial", Font.PLAIN, 12));
        detalleTable.setRowHeight(25);
        detalleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(detalleTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCerrar);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showSuccessMessage(String message) { JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE); }
    private void showErrorMessage(String message) { JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarningMessage(String message) { JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE); }
}
