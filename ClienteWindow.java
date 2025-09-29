package com.cianmetalurgica.view;

import com.cianmetalurgica.model.Cliente;
import com.cianmetalurgica.service.ClienteService;
import com.cianmetalurgica.view.MaterialWindow.Mode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class ClienteWindow extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(0, 0, 0);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private ClienteService clienteService;
    private Mode mode;
    private JFrame parent;
    private Cliente clienteToEdit;

    // Campos del formulario (coinciden con Cliente.java)
    private JTextField txtNombreRazonSocial;
    private JTextField txtCuitDni;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;

    // Para lista/búsqueda
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;

    public ClienteWindow(JFrame parent, Mode mode) {
        this.parent = parent;
        this.mode = mode;
        this.clienteService = new ClienteService();

        initComponents();
        setupLayout();
        setupEventHandlers();

        if (mode == Mode.LIST || mode == Mode.SEARCH) {
            loadData();
        }
    }

    public ClienteWindow(JFrame parent, Mode mode, Cliente cliente) {
        this(parent, mode);
        this.clienteToEdit = cliente;
        if (mode == Mode.EDIT && cliente != null) {
            fillFormWithCliente(cliente);
        }
    }

    private void initComponents() {
        String title = "";
        switch (mode) {
            case LIST: title = "Lista de Clientes"; break;
            case CREATE: title = "Nuevo Cliente"; break;
            case SEARCH: title = "Buscar Clientes"; break;
            case EDIT: title = "Editar Cliente"; break;
        }

        setTitle("Cian Metalúrgica - " + title);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);

        if (mode == Mode.LIST || mode == Mode.SEARCH) {
            add(createListPanel(), BorderLayout.CENTER);
        } else {
            add(createFormPanel(), BorderLayout.CENTER);
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BUTTON_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel(getTitle().replace("Cian Metalúrgica - ", ""));
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.BLACK);
        panel.add(titleLabel);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre / Razón Social
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Nombre / Razón Social *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtNombreRazonSocial = createTextField();
        panel.add(txtNombreRazonSocial, gbc);

        // CUIT / DNI
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("CUIT / DNI:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtCuitDni = createTextField();
        panel.add(txtCuitDni, gbc);

        // Dirección
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtDireccion = createTextField();
        panel.add(txtDireccion, gbc);

        // Teléfono
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtTelefono = createTextField();
        panel.add(txtTelefono, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtEmail = createTextField();
        panel.add(txtEmail, gbc);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (mode == Mode.SEARCH) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBackground(BACKGROUND_COLOR);
            searchPanel.add(createLabel("Buscar por nombre/razón social:"));
            txtBuscar = createTextField();
            txtBuscar.setPreferredSize(new Dimension(300, 30));
            searchPanel.add(txtBuscar);
            JButton btnBuscar = createButton("Buscar", e -> performSearch());
            searchPanel.add(btnBuscar);
            panel.add(searchPanel, BorderLayout.NORTH);
        }

        String[] columns = {"ID", "Nombre / Razón Social", "CUIT/DNI", "Dirección", "Teléfono", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(LABEL_FONT);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        if (mode == Mode.CREATE) {
            panel.add(createButton("Guardar", e -> saveCliente()));
            panel.add(createButton("Limpiar", e -> clearForm()));
        } else if (mode == Mode.EDIT) {
            panel.add(createButton("Actualizar", e -> updateCliente()));
        } else if (mode == Mode.LIST || mode == Mode.SEARCH) {
            panel.add(createButton("Nuevo", e -> openCreateWindow()));
            panel.add(createButton("Editar", e -> editSelected()));
            panel.add(createButton("Eliminar", e -> deleteSelected()));
            panel.add(createButton("Actualizar", e -> loadData()));
        }

        panel.add(createButton("Cerrar", e -> dispose()));
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(LABEL_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    // Nota: foreground NEGRO para los botones (cambio pedido)
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(LABEL_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK); // <-- texto en negro
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_HOVER_COLOR); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(BUTTON_COLOR); }
        });

        button.addActionListener(action);
        return button;
    }

    private void setupEventHandlers() {
        if (mode == Mode.SEARCH && txtBuscar != null) {
            txtBuscar.addActionListener(e -> performSearch());
        }
    }

    private void loadData() {
        try {
            List<Cliente> clientes = clienteService.getAllClientes();
            updateTable(clientes);
        } catch (Exception e) {
            showErrorMessage("Error al cargar los datos: " + e.getMessage());
        }
    }

    private void updateTable(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente c : clientes) {
            Object[] row = {
                c.getIdCliente(),
                c.getNombreRazonSocial(),
                c.getCuitDni(),
                c.getDireccion() != null ? c.getDireccion() : "",
                c.getTelefono() != null ? c.getTelefono() : "",
                c.getEmail() != null ? c.getEmail() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void performSearch() {
        try {
            String searchText = txtBuscar.getText().trim();
            // Asegurate que tu ClienteService tenga este método; si no, ajusta el nombre aquí.
            List<Cliente> clientes = clienteService.searchClientesByNombreRazonSocial(searchText);
            updateTable(clientes);
        } catch (Exception e) {
            showErrorMessage("Error al buscar: " + e.getMessage());
        }
    }

    private void saveCliente() {
        try {
            Cliente c = new Cliente();
            c.setNombreRazonSocial(txtNombreRazonSocial.getText().trim());
            c.setCuitDni(txtCuitDni.getText().trim());
            c.setDireccion(txtDireccion.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setEmail(txtEmail.getText().trim());

            clienteService.saveCliente(c);
            showSuccessMessage("Cliente guardado exitosamente");
            clearForm();
            if (mode == Mode.LIST || mode == Mode.SEARCH) loadData();
        } catch (Exception e) {
            showErrorMessage("Error al guardar: " + e.getMessage());
        }
    }

    private void updateCliente() {
        try {
            if (clienteToEdit == null) {
                showErrorMessage("No hay cliente seleccionado para editar");
                return;
            }
            Cliente c = new Cliente();
            c.setIdCliente(clienteToEdit.getIdCliente());
            c.setNombreRazonSocial(txtNombreRazonSocial.getText().trim());
            c.setCuitDni(txtCuitDni.getText().trim());
            c.setDireccion(txtDireccion.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            clienteService.updateCliente(c);
            showSuccessMessage("Cliente actualizado exitosamente");
            dispose();
        } catch (Exception e) {
            showErrorMessage("Error al actualizar: " + e.getMessage());
        }
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { showWarningMessage("Seleccione un cliente para editar"); return; }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Long clienteId = (idObj instanceof Long) ? (Long) idObj : Long.valueOf(idObj.toString());
        try {
            Cliente c = clienteService.getClienteById(clienteId);
            if (c != null) {
                new ClienteWindow(this, Mode.EDIT, c).setVisible(true);
            }
        } catch (Exception e) {
            showErrorMessage("Error al obtener el cliente: " + e.getMessage());
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { showWarningMessage("Seleccione un cliente para eliminar"); return; }

        int option = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea eliminar este cliente?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            Object idObj = tableModel.getValueAt(selectedRow, 0);
            Long clienteId = (idObj instanceof Long) ? (Long) idObj : Long.valueOf(idObj.toString());
            try {
                clienteService.deleteCliente(clienteId);
                showSuccessMessage("Cliente eliminado exitosamente");
                loadData();
            } catch (Exception e) {
                showErrorMessage("Error al eliminar: " + e.getMessage());
            }
        }
    }

    private void fillFormWithCliente(Cliente c) {
        txtNombreRazonSocial.setText(c.getNombreRazonSocial());
        txtCuitDni.setText(c.getCuitDni());
        txtDireccion.setText(c.getDireccion() != null ? c.getDireccion() : "");
        txtTelefono.setText(c.getTelefono() != null ? c.getTelefono() : "");
        txtEmail.setText(c.getEmail() != null ? c.getEmail() : "");
    }

    private void clearForm() {
        txtNombreRazonSocial.setText("");
        txtCuitDni.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtNombreRazonSocial.requestFocus();
    }

    private void openCreateWindow() {
        new ClienteWindow(this, Mode.CREATE).setVisible(true);
    }

    private void showSuccessMessage(String message) { JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE); }
    private void showErrorMessage(String message) { JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarningMessage(String message) { JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE); }
}

/*public class ClienteWindow extends JFrame {

    ClienteWindow(MainWindow aThis, MaterialWindow.Mode mode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public enum Mode { LIST, CREATE, SEARCH, EDIT }
    
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(44, 62, 80);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    private ClienteService clienteService;
    private Mode mode;
    private JFrame parent;
    private Cliente clienteToEdit;
    
    // Componentes del formulario
    private JTextField txtNombreRazonSocial;
    private JTextField txtCuitDni;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;
    
    public ClienteWindow(JFrame parent, Mode mode) {
        this.parent = parent;
        this.mode = mode;
        this.clienteService = new ClienteService();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        if (mode == Mode.LIST || mode == Mode.SEARCH) {
            loadData();
        }
    }
    
    public ClienteWindow(JFrame parent, Mode mode, Cliente cliente) {
        this(parent, mode);
        this.clienteToEdit = cliente;
        if (mode == Mode.EDIT && cliente != null) {
            fillFormWithCliente(cliente);
        }
    }
    
    private void initComponents() {
        String title = "";
        switch (mode) {
            case LIST: title = "Lista de Clientes"; break;
            case CREATE: title = "Nuevo Cliente"; break;
            case SEARCH: title = "Buscar Clientes"; break;
            case EDIT: title = "Editar Cliente"; break;
        }
        
        setTitle("Cian Metalúrgica - " + title);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Content
        if (mode == Mode.LIST || mode == Mode.SEARCH) {
            add(createListPanel(), BorderLayout.CENTER);
        } else {
            add(createFormPanel(), BorderLayout.CENTER);
        }
        
        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BUTTON_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(getTitle().replace("Cian Metalúrgica - ", ""));
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        
        panel.add(titleLabel);
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nombre/Razón Social
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Nombre/Razón Social *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtNombreRazonSocial = createTextField();
        panel.add(txtNombreRazonSocial, gbc);
        
        // CUIT/DNI
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("CUIT/DNI *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtCuitDni = createTextField();
        panel.add(txtCuitDni, gbc);
        
        // Dirección
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtDireccion = createTextField();
        panel.add(txtDireccion, gbc);
        
        // Teléfono
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtTelefono = createTextField();
        panel.add(txtTelefono, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtEmail = createTextField();
        panel.add(txtEmail, gbc);
        
        return panel;
    }
    
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        if (mode == Mode.SEARCH) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBackground(BACKGROUND_COLOR);
            searchPanel.add(createLabel("Buscar:"));
            txtBuscar = createTextField();
            txtBuscar.setPreferredSize(new Dimension(300, 30));
            searchPanel.add(txtBuscar);
            
            JButton btnBuscar = createButton("Buscar", e -> performSearch());
            searchPanel.add(btnBuscar);
            
            panel.add(searchPanel, BorderLayout.NORTH);
        }
        
        // Tabla
        String[] columns = {"ID", "Nombre/Razón Social", "CUIT/DNI", "Dirección", "Teléfono", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(LABEL_FONT);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(BUTTON_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        if (mode == Mode.CREATE) {
            panel.add(createButton("Guardar", e -> saveCliente()));
            panel.add(createButton("Limpiar", e -> clearForm()));
        } else if (mode == Mode.EDIT) {
            panel.add(createButton("Actualizar", e -> updateCliente()));
        } else if (mode == Mode.LIST || mode == Mode.SEARCH) {
            panel.add(createButton("Nuevo", e -> openCreateWindow()));
            panel.add(createButton("Editar", e -> editSelected()));
            panel.add(createButton("Eliminar", e -> deleteSelected()));
            panel.add(createButton("Actualizar", e -> loadData()));
        }
        
        panel.add(createButton("Cerrar", e -> dispose()));
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(new Color(44, 62, 80));
        return label;
    }
    
    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(LABEL_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }
    
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(LABEL_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });
        
        button.addActionListener(action);
        return button;
    }
    
    private void setupEventHandlers() {
        if (mode == Mode.SEARCH && txtBuscar != null) {
            txtBuscar.addActionListener(e -> performSearch());
        }
    }
    
    private void loadData() {
        try {
            List<Cliente> clientes = clienteService.getAllClientes();
            updateTable(clientes);
        } catch (Exception e) {
            showErrorMessage("Error al cargar los datos: " + e.getMessage());
        }
    }
    
    private void updateTable(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente cliente : clientes) {
            Object[] row = {
                cliente.getIdCliente(),
                cliente.getNombreRazonSocial(),
                cliente.getCuitDni(),
                cliente.getDireccion() != null ? cliente.getDireccion() : "",
                cliente.getTelefono() != null ? cliente.getTelefono() : "",
                cliente.getEmail() != null ? cliente.getEmail() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void performSearch() {
        try {
            String searchText = txtBuscar.getText().trim();
            List<Cliente> clientes = clienteService.searchClientesByName(searchText);
            updateTable(clientes);
        } catch (Exception e) {
            showErrorMessage("Error al buscar: " + e.getMessage());
        }
    }
    
    private void saveCliente() {
        try {
            Cliente cliente = createClienteFromForm();
            clienteService.saveCliente(cliente);
            showSuccessMessage("Cliente guardado exitosamente");
            clearForm();
        } catch (Exception e) {
            showErrorMessage("Error al guardar: " + e.getMessage());
        }
    }
    
    private void updateCliente() {
        try {
            if (clienteToEdit == null) {
                showErrorMessage("No hay cliente seleccionado para editar");
                return;
            }
            
            Cliente cliente = createClienteFromForm();
            cliente.setIdCliente(clienteToEdit.getIdCliente());
            clienteService.updateCliente(cliente);
            showSuccessMessage("Cliente actualizado exitosamente");
            dispose();
        } catch (Exception e) {
            showErrorMessage("Error al actualizar: " + e.getMessage());
        }
    }
    
    private Cliente createClienteFromForm() {
        Cliente cliente = new Cliente();
        cliente.setNombreRazonSocial(txtNombreRazonSocial.getText().trim());
        cliente.setCuitDni(txtCuitDni.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setEmail(txtEmail.getText().trim());
        return cliente;
    }
    
    private void fillFormWithCliente(Cliente cliente) {
        txtNombreRazonSocial.setText(cliente.getNombreRazonSocial());
        txtCuitDni.setText(cliente.getCuitDni());
        txtDireccion.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");
        txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");
        txtEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
    }
    
    private void clearForm() {
        txtNombreRazonSocial.setText("");
        txtCuitDni.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtNombreRazonSocial.requestFocus();
    }
    
    private void openCreateWindow() {
        new ClienteWindow(this, Mode.CREATE).setVisible(true);
    }
    
    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un cliente para editar");
            return;
        }
        
        Long clienteId = (Long) tableModel.getValueAt(selectedRow, 0);
        try {
            Cliente cliente = clienteService.getClienteById(clienteId);
            if (cliente != null) {
                new ClienteWindow(this, Mode.EDIT, cliente).setVisible(true);
            }
        } catch (Exception e) {
            showErrorMessage("Error al obtener el cliente: " + e.getMessage());
        }
    }
    
    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un cliente para eliminar");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea eliminar este cliente?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            Long clienteId = (Long) tableModel.getValueAt(selectedRow, 0);
            try {
                clienteService.deleteCliente(clienteId);
                showSuccessMessage("Cliente eliminado exitosamente");
                loadData();
            } catch (Exception e) {
                showErrorMessage("Error al eliminar: " + e.getMessage());
            }
        }
    }
    
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
}
*/


