package com.cianmetalurgica;

import com.cianmetalurgica.config.DatabaseConnection;
import com.cianmetalurgica.view.MainWindow;
import javax.swing.*;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.testConnection();
                
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error al conectar con la base de datos.\n" +
                    "Verifique que MySQL esté ejecutándose.\n\n" +
                    "Error: " + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}