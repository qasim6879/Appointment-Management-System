package org.example;

// qasem was here
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the AppointEase Swing UI.
 * Manages the top-level JFrame and switches between screens.
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
        // --- [خطوة الربط] التأكد من وجود بيانات JSON قبل تشغيل الواجهة ---
        initializeSystemData(); 
        // -----------------------------------------------------------

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
     * ميثود فحص وإنشاء ملفات الـ JSON إذا لم تكن موجودة.
     * تضمن وجود مستخدم (User) ومسؤول (Admin) وموعد (Appointment) تجريبي.
     */
    private void initializeSystemData() {
        // 1. فحص ملف المسؤولين (Administrators)
        // بناءً على رسالة الخطأ السابقة، الكونستركتور يأخذ (Name, Email, Password)
        File adminFile = new File("admins.json");
        if (!adminFile.exists()) {
            List<Administrator> admins = new ArrayList<>();
            admins.add(new Administrator("Qasem Admin", "admin@appoint.com", "12345"));
            JsonHandler.saveList(admins, "admins.json");
        }

        // 2. فحص ملف المستخدمين (Users)
        // بناءً على رسالتك، الكونستركتور مرتب وصحيح (Email, Password غالباً أو الاسم أولاً)
        File userFile = new File("users.json");
        if (!userFile.exists()) {
            List<User> users = new ArrayList<>();
            users.add(new User("user@test.com", "user123")); 
            JsonHandler.saveList(users, "users.json");
        }

        // 3. فحص ملف المواعيد (مطابق تماماً لكلاس Appointment الخاص بك)
        File appFile = new File("appointments.json");
        if (!appFile.exists()) {
            List<Appointment> appointments = new ArrayList<>();
            
            // الترتيب: (String id, LocalDate date, LocalTime startTime, int duration, AppointmentType type)
            appointments.add(new Appointment(
                "AP-001",                   
                LocalDate.now(),            
                LocalTime.of(10, 0),        
                45,                         
                AppointmentType.IN_PERSON   
            ));
            
            JsonHandler.saveList(appointments, "appointments.json");
        }
    }

    /**
     * Builds and shows the User Dashboard for the given username.
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

    /** Removes a card by name if it already exists. */
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
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    
       // List<Notification> list = new ArrayList<>();
        //list.add(new Notification("your appointment tommorrow at 9 am", true)); 
        //JsonHandler.saveList(list, "notifications.json");
    }
}