package com.internship.reservation;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
                catch (Exception ignored) { }
                try {
                    Database.initialize();
                    new ReservationFrame().showLogin();
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Database could not be initialized: " + e.getMessage(), "Startup error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
