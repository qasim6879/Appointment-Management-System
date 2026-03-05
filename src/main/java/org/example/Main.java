package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the AppointEase Swing UI prototype.
 * Manages the CardLayout window and switches between:
 *   - LoginPanel
 *   - UserDashboard
 *   - AdminDashboard
 *
 * Run this class directly with: java ui.AppointEaseApp
 * Or via Maven: mvn exec:java -Dexec.mainClass="ui.AppointEaseApp"
 *
 * @author AppointEase
 * @version 1.0
 */
public class Main {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_USER  = "USER";
    private static final String CARD_ADMIN = "ADMIN";

    private JFrame      frame;
    private CardLayout  cardLayout;
    private JPanel      cardPanel;

    /**
     * Initialises and displays the main application window.
     */
    public void start() {
        // Use system look & feel as a base, then override with our theme
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global UI defaults
        UIManager.put("Panel.background",          Theme.PAPER);
        UIManager.put("OptionPane.background",      Theme.CARD);
        UIManager.put("OptionPane.messageFont",     Theme.FONT_BODY);
        UIManager.put("Button.font",                Theme.FONT_BUTTON);
        UIManager.put("ComboBox.font",              Theme.FONT_BODY);
        UIManager.put("TextField.font",             Theme.FONT_BODY);
        UIManager.put("Table.font",                 Theme.FONT_BODY);
        UIManager.put("TableHeader.font",           Theme.FONT_LABEL);
        UIManager.put("ScrollBar.width",            8);

        frame = new JFrame("AppointEase — Appointment Scheduling System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        // Build screens
        LoginPanel login = new LoginPanel((username, isAdmin) -> {
            if (isAdmin) {
                showAdmin(username);
            } else {
                showUser(username);
            }
        });

        cardPanel.add(login, CARD_LOGIN);
        frame.setContentPane(cardPanel);
        frame.setVisible(true);

        // Start on login
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    private void showUser(String username) {
        // Remove old user panel if any, create fresh
        Component old = findCard(CARD_USER);
        if (old != null) cardPanel.remove(old);

        UserDashboard user = new UserDashboard(username, () -> {
            cardLayout.show(cardPanel, CARD_LOGIN);
        });
        cardPanel.add(user, CARD_USER);
        cardLayout.show(cardPanel, CARD_USER);
        frame.revalidate();
    }

    private void showAdmin(String adminName) {
        Component old = findCard(CARD_ADMIN);
        if (old != null) cardPanel.remove(old);

        AdminDashboard admin = new AdminDashboard(
            capitalize(adminName),
            () -> cardLayout.show(cardPanel, CARD_LOGIN)
        );
        cardPanel.add(admin, CARD_ADMIN);
        cardLayout.show(cardPanel, CARD_ADMIN);
        frame.revalidate();
    }

    private Component findCard(String name) {
        for (Component c : cardPanel.getComponents()) {
            if (name.equals(c.getName())) return c;
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Admin";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Application main method.
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }
}