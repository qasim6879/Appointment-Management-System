package org.example;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AppointEase");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 720);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);

            LoginPanel.LoginListener[] ref = new LoginPanel.LoginListener[1];
            ref[0] = (username, isAdmin) -> {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setContentPane(isAdmin
                        ? new AdminDashboard(username, () -> { frame.setContentPane(new LoginPanel(ref[0])); frame.revalidate(); })
                        : new UserDashboard(username,  () -> { frame.setContentPane(new LoginPanel(ref[0])); frame.revalidate(); })
                );
                frame.revalidate();
            };

            frame.setContentPane(new LoginPanel(ref[0]));
            frame.setVisible(true);
        });
        UIManager.put("Button.select", new Color(0xC8, 0x4B, 0x2F));
        UIManager.put("ToggleButton.select", new Color(0xC8, 0x4B, 0x2F));
        UIManager.put("ComboBox.selectionBackground", new Color(180,180,180));
    }
}