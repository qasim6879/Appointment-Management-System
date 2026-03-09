package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the AppointEase Swing UI.
 * Manages the top-level JFrame and switches between screens
 * (Login → User Dashboard / Admin Dashboard → Login) via CardLayout.
 *
 * Run via Maven:
 *   mvn compile
 *   mvn exec:java -Dexec.mainClass="ui.Main"
 *
 * Or compile and run directly:
 *   javac -d out src/main/java/ui/*.java
 *   java -cp out ui.Main
 *
 * @author AppointEase
 * @version 1.0
 */
public class Main {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_USER  = "USER";
    private static final String CARD_ADMIN = "ADMIN";

    private JFrame     frame;
    private CardLayout cardLayout;
    private JPanel     cardPanel;

    /**
     * Initialises and displays the main application window.
     */
    public void start() {
        // Cross-platform L&F as base, then override with our theme tokens
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global UI defaults
        UIManager.put("Panel.background",     Theme.PAPER);
        UIManager.put("OptionPane.background", Theme.CARD);
        UIManager.put("OptionPane.messageFont",Theme.FONT_BODY);
        UIManager.put("Button.font",           Theme.FONT_BUTTON);
        UIManager.put("ComboBox.font",         Theme.FONT_BODY);
        UIManager.put("TextField.font",        Theme.FONT_BODY);
        UIManager.put("Table.font",            Theme.FONT_BODY);
        UIManager.put("TableHeader.font",      Theme.FONT_LABEL);
        UIManager.put("ScrollBar.width",       8);

        frame = new JFrame("AppointEase — Appointment Scheduling System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        LoginPanel login = new LoginPanel((username, isAdmin) -> {
            if (isAdmin) showAdmin(username);
            else         showUser(username);
        });
        cardPanel.add(login, CARD_LOGIN);

        frame.setContentPane(cardPanel);
        frame.setVisible(true);
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    /**
     * Builds and shows the User Dashboard for the given username.
     * @param username the logged-in user's name
     */
    private void showUser(String username) {
        removeCard(CARD_USER);
        UserDashboard user = new UserDashboard(username,
                () -> cardLayout.show(cardPanel, CARD_LOGIN));
        cardPanel.add(user, CARD_USER);
        cardLayout.show(cardPanel, CARD_USER);
        frame.revalidate();
    }

    /**
     * Builds and shows the Admin Dashboard for the given admin name.
     * @param adminName the logged-in admin's name
     */
    private void showAdmin(String adminName) {
        removeCard(CARD_ADMIN);
        AdminDashboard admin = new AdminDashboard(
                capitalize(adminName),
                () -> cardLayout.show(cardPanel, CARD_LOGIN));
        cardPanel.add(admin, CARD_ADMIN);
        cardLayout.show(cardPanel, CARD_ADMIN);
        frame.revalidate();
    }

    /** Removes a card by name if it already exists, to avoid stale panels. */
    private void removeCard(String name) {
        for (Component c : cardPanel.getComponents()) {
            if (name.equals(c.getName())) {
                cardPanel.remove(c);
                return;
            }
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Admin";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Application entry point.
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }
}
