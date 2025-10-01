package com.cianmetalurgica.view;

import com.cianmetalurgica.model.Material;
import com.cianmetalurgica.service.MaterialService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MaterialWindow extends JFrame {
    public static enum Mode { LIST, CREATE, SEARCH, EDIT }

    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(0, 0, 0);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private MaterialService materialService;
    private Mode mode;
    private JFrame parent;
    private Material materialToEdit;

    // Componentes del formulario
    private JTextField txtArticulo; // <-- nuevo campo Artículo
    private JTextField txtTipo;
    private JTextField txtEspesor;
    private JTextField txtDimensiones;
    private JTextField txtCantidad;
    private JTextField txtProveedor;
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;

    public MaterialWindow(JFrame parent, Mode mode) {
        this.parent = parent;
        this.mode = mode;
        this.materialService = new MaterialService();

        initComponents();
        setupLayout();
        setupEventHandlers();

        if (mode == Mode.LIST || mode == Mode.SEARCH) {
            loadData();
        }
    }

    public MaterialWindow(JFrame parent, Mode mode, Material material) {
        this(parent, mode);
        this.materialToEdit = material;
        if (mode == Mode.EDIT && material != null) {
            fillFormWithMaterial(material);
        }
    }

    private void initComponents() {
        String title = "";
        switch (mode) {
            case LIST: title = "Lista de Materiales"; break;
            case CREATE: title = "Nuevo Material"; break;
            case SEARCH: title = "Buscar Materiales"; break;
            case EDIT: title = "Editar Material"; break;
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

        // Artículo (nuevo)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Artículo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtArticulo = createTextField();
        panel.add(txtArticulo, gbc);

        // Tipo
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Tipo *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtTipo = createTextField();
        panel.add(txtTipo, gbc);

        // Espesor
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Espesor (mm):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtEspesor = createTextField();
        panel.add(txtEspesor, gbc);

        // Dimensiones
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Dimensiones:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtDimensiones = createTextField();
        panel.add(txtDimensiones, gbc);

        // Cantidad
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Cantidad:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtCantidad = createTextField();
        panel.add(txtCantidad, gbc);

        // Proveedor
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("Proveedor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtProveedor = createTextField();
        panel.add(txtProveedor, gbc);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (mode == Mode.SEARCH) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBackground(BACKGROUND_COLOR);
            searchPanel.add(createLabel("Buscar por tipo:"));
            txtBuscar = createTextField();
            txtBuscar.setPreferredSize(new Dimension(300, 30));
            searchPanel.add(txtBuscar);

            JButton btnBuscar = createButton("Buscar", e -> performSearch());
            searchPanel.add(btnBuscar);

            panel.add(searchPanel, BorderLayout.NORTH);
        }

        String[] columns = {"ID", "Artículo", "Tipo", "Espesor (mm)", "Dimensiones", "Cantidad", "Proveedor"};
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
            panel.add(createButton("Guardar", e -> saveMaterial()));
            panel.add(createButton("Limpiar", e -> clearForm()));
        } else if (mode == Mode.EDIT) {
            panel.add(createButton("Actualizar", e -> updateMaterial()));
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
        label.setForeground(new Color(0, 0, 0));
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
        button.setForeground(Color.BLACK);
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
            List<Material> materiales = materialService.getAllMateriales();
            updateTable(materiales);
        } catch (Exception e) {
            showErrorMessage("Error al cargar los datos: " + e.getMessage());
        }
    }

    private void updateTable(List<Material> materiales) {
        tableModel.setRowCount(0);
        for (Material material : materiales) {
            Object[] row = {
                material.getIdMaterial(),
                // Nuevo: artículo -> si tu modelo lo tiene, cambia material.getArticulo()
                material.getTipo(), // si quieres mostrar artículo distinto, adapta el modelo
                material.getTipo(),
                material.getEspesor() != null ? material.getEspesor().toString() : "",
                material.getDimensiones() != null ? material.getDimensiones() : "",
                material.getCantidad() != null ? material.getCantidad().toString() : "",
                material.getProveedor() != null ? material.getProveedor() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void performSearch() {
        try {
            String searchText = txtBuscar.getText().trim();
            List<Material> materiales = materialService.searchMaterialesByType(searchText);
            updateTable(materiales);
        } catch (Exception e) {
            showErrorMessage("Error al buscar: " + e.getMessage());
        }
    }

    private void saveMaterial() {
        try {
            Material material = createMaterialFromForm();
            materialService.saveMaterial(material);
            showSuccessMessage("Material guardado exitosamente");
            clearForm();
        } catch (Exception e) {
            showErrorMessage("Error al guardar: " + e.getMessage());
        }
    }

    private void updateMaterial() {
        try {
            if (materialToEdit == null) {
                showErrorMessage("No hay material seleccionado para editar");
                return;
            }

            Material material = createMaterialFromForm();
            material.setIdMaterial(materialToEdit.getIdMaterial());
            materialService.updateMaterial(material);
            showSuccessMessage("Material actualizado exitosamente");
            dispose();
        } catch (Exception e) {
            showErrorMessage("Error al actualizar: " + e.getMessage());
        }
    }

    private Material createMaterialFromForm() {
        Material material = new Material();
        material.setArticulo(txtArticulo.getText().trim());


        material.setTipo(txtTipo.getText().trim());

        // Espesor
        String espesorText = txtEspesor.getText().trim();
        if (!espesorText.isEmpty()) {
            try {
                material.setEspesor(Double.parseDouble(espesorText));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El espesor debe ser un número válido");
            }
        }

        material.setDimensiones(txtDimensiones.getText().trim());

        // Cantidad
        String cantidadText = txtCantidad.getText().trim();
        if (!cantidadText.isEmpty()) {
            try {
                material.setCantidad(Integer.parseInt(cantidadText));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("La cantidad debe ser un número entero válido");
            }
        }

        material.setProveedor(txtProveedor.getText().trim());
        return material;
    }

    private void fillFormWithMaterial(Material material) {
        // Si agregas campo articulo en modelo, rellénalo:
        // txtArticulo.setText(material.getArticulo() != null ? material.getArticulo() : "");
        txtTipo.setText(material.getTipo());
        txtEspesor.setText(material.getEspesor() != null ? material.getEspesor().toString() : "");
        txtDimensiones.setText(material.getDimensiones() != null ? material.getDimensiones() : "");
        txtCantidad.setText(material.getCantidad() != null ? material.getCantidad().toString() : "");
        txtProveedor.setText(material.getProveedor() != null ? material.getProveedor() : "");
    }

    private void clearForm() {
        txtArticulo.setText("");
        txtTipo.setText("");
        txtEspesor.setText("");
        txtDimensiones.setText("");
        txtCantidad.setText("");
        txtProveedor.setText("");
        txtTipo.requestFocus();
    }

    private void openCreateWindow() {
        new MaterialWindow(this, Mode.CREATE).setVisible(true);
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un material para editar");
            return;
        }

        Long materialId = (Long) tableModel.getValueAt(selectedRow, 0);
        try {
            Material material = materialService.getMaterialById(materialId);
            if (material != null) {
                new MaterialWindow(this, Mode.EDIT, material).setVisible(true);
            }
        } catch (Exception e) {
            showErrorMessage("Error al obtener el material: " + e.getMessage());
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Seleccione un material para eliminar");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea eliminar este material?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            Long materialId = (Long) tableModel.getValueAt(selectedRow, 0);
            try {
                materialService.deleteMaterial(materialId);
                showSuccessMessage("Material eliminado exitosamente");
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
