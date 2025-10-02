package com.cianmetalurgica.view;

import com.cianmetalurgica.view.ClienteWindow;
import com.cianmetalurgica.view.PedidoWindow;
import com.cianmetalurgica.view.MaterialWindow;
import com.cianmetalurgica.view.MaterialWindow.Mode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;



public class MainWindow extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER_COLOR = new Color(0, 0, 0);

    public MainWindow() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        setTitle("Cian Metalúrgica Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(BUTTON_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel titleLabel = new JLabel("CIAN METALÚRGICA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Sistema de Gestión", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);

        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sección Clientes
        JLabel clientesLabel = new JLabel("CLIENTES");
        clientesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        clientesLabel.setForeground(new Color(0, 0, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        buttonPanel.add(clientesLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; buttonPanel.add(createButton("Ver Clientes", e -> openClientesList()), gbc);
        gbc.gridx = 1; buttonPanel.add(createButton("Nuevo Cliente", e -> openClienteForm()), gbc);
        gbc.gridx = 2; buttonPanel.add(createButton("Buscar Cliente", e -> openClienteSearch()), gbc);

        //Sección Pedidos
        JLabel pedidosLabel = new JLabel("PEDIDOS");
        pedidosLabel.setFont(new Font("Arial", Font.BOLD, 16));
        pedidosLabel.setForeground(new Color(00, 00, 00));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        buttonPanel.add(pedidosLabel, gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 3;
        gbc.gridx = 0; buttonPanel.add(createButton("Ver Pedidos", e -> openPedidosList()), gbc);
        gbc.gridx = 1; buttonPanel.add(createButton("Nuevo Pedido", e -> openPedidoForm()), gbc);
        gbc.gridx = 2; buttonPanel.add(createButton("Buscar Pedido", e -> openPedidoSearch()), gbc);
        
        
        // Sección Materiales
        JLabel materialesLabel = new JLabel("MATERIALES");
        materialesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        materialesLabel.setForeground(new Color(0, 0, 0));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        buttonPanel.add(materialesLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 5;
        gbc.gridx = 0; buttonPanel.add(createButton("Ver Materiales", e -> openMaterialesList()), gbc);
        gbc.gridx = 1; buttonPanel.add(createButton("Nuevo Material", e -> openMaterialForm()), gbc);
        gbc.gridx = 2; buttonPanel.add(createButton("Buscar Material", e -> openMaterialSearch()), gbc);

        // Botón Pedidos (simplificado)
     
        
        
        
        
        add(buttonPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(236, 240, 241));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel footerLabel = new JLabel("© Terra Code - Todos los derechos reservados", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(127, 140, 141));

        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        button.addActionListener(action);
        return button;
    }

    // Event handlers
    private void openClientesList() { new ClienteWindow(this, Mode.LIST).setVisible(true); }
    private void openClienteForm() { new ClienteWindow(this, Mode.CREATE).setVisible(true); }
    private void openClienteSearch() { new ClienteWindow(this, Mode.SEARCH).setVisible(true); }

    private void openPedidosList() { new PedidoWindow(this, Mode.LIST).setVisible(true); }
    private void openPedidoForm() { new PedidoWindow(this, Mode.CREATE).setVisible(true); }
    private void openPedidoSearch() { new PedidoWindow(this, Mode.SEARCH).setVisible(true); }
    
    private void openMaterialesList() { new MaterialWindow(this, Mode.LIST).setVisible(true); }
    private void openMaterialForm() { new MaterialWindow(this, Mode.CREATE).setVisible(true); }
    private void openMaterialSearch() { new MaterialWindow(this, Mode.SEARCH).setVisible(true); }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}