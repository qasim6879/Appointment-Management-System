package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Login screen for AppointEase.
 * Split layout: left branding panel + right form panel.
 * Role (User / Administrator) is selected via a toggle before signing in.
 * Validates against JSON database.
 *
 * @author AppointEase
 * @version 1.1
 */
public class LoginPanel extends JPanel {

    /** Callback fired when the user successfully signs in. */
    public interface LoginListener {
        /**
         * @param username the entered username
         * @param isAdmin  true if the Administrator toggle was selected
         */
        void onLogin(String username, boolean isAdmin);
    }

    private JTextField      usernameField;
    private JPasswordField passwordField;
    private JToggleButton  userToggle;
    private JToggleButton  adminToggle;
    private JLabel          errorLabel;
    private final LoginListener listener;

    /**
     * @param listener called when login succeeds
     */
    public LoginPanel(LoginListener listener) {
        this.listener = listener;
        setLayout(new GridLayout(1, 2));
        setBackground(Theme.INK);
        add(buildLeft());
        add(buildRight());

        // إعداد منطق إخفاء النص التوضيحي (Placeholder) تلقائياً
        setupFocusLogic();
    }

    private void setupFocusLogic() {
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // إزالة النص التوضيحي عند بدء الكتابة
                if (usernameField.getText().equals("e.g. j.doe")) {
                    usernameField.setText("");
                    usernameField.setForeground(Theme.INK);
                }
                errorLabel.setVisible(false); // إخفاء النص الدموي عند محاولة التصحيح
            }
            @Override
            public void focusLost(FocusEvent e) {
                // إعادة النص التوضيحي إذا ترك الحقل فارغاً
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText("e.g. j.doe");
                    usernameField.setForeground(Theme.MUTED);
                }
            }
        });

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                errorLabel.setVisible(false);
            }
        });
    }

    // ── Left: Branding ────────────────────────────────────────

    private JPanel buildLeft() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 160, -60, 260, 260);
                g2.fillOval(-60, getHeight() - 120, 200, 200);
                g2.dispose();
            }
        };
        p.setBackground(Theme.INK);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("<html>"
            + "<span style='color:#f5f0e8'>Appoint</span>"
            + "<span style='color:#c84b2f'><i>Ease.</i></span></html>");
        brand.setFont(new Font("Serif", Font.BOLD, 36));
        JLabel tag = new JLabel("<html>"
            + "<span style='color:rgba(245,240,232,0.5)'>"
            + "A structured appointment scheduling<br>"
            + "system for your institution.</span></html>");
        tag.setFont(Theme.FONT_BODY);
        tag.setForeground(new Color(0xBB, 0xB4, 0xA8));
        top.add(brand);
        top.add(Box.createVerticalStrut(12));
        top.add(tag);

        JPanel features = new JPanel();
        features.setOpaque(false);
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        String[] feats = {
            "Book, modify & cancel appointments",
            "Multiple types: urgent, virtual, group…",
            "Automatic reminders & notifications",
            "Admin-level override & full schedule control"
        };
        for (String f : feats) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            row.setOpaque(false);
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("SansSerif", Font.PLAIN, 6));
            dot.setForeground(Theme.ACCENT);
            JLabel txt = new JLabel(f);
            txt.setFont(Theme.FONT_BODY);
            txt.setForeground(new Color(0xBB, 0xB4, 0xA8));
            row.add(dot); row.add(txt);
            features.add(row);
        }

        JLabel footer = new JLabel("JAVA 8 · MAVEN · JUNIT 5");
        footer.setFont(Theme.FONT_LABEL);
        footer.setForeground(new Color(0x55, 0x4E, 0x44));

        p.add(top,      BorderLayout.NORTH);
        p.add(features, BorderLayout.CENTER);
        p.add(footer,   BorderLayout.SOUTH);
        return p;
    }

    // ── Right: Form ───────────────────────────────────────────

    private JPanel buildRight() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.CARD);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 460));

        JLabel heading = new JLabel("Sign in");
        heading.setFont(new Font("Serif", Font.BOLD, 28));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Components.subtitle("Enter your credentials to continue.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JLabel roleLabel = Components.sectionLabel("I am a:");
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup roleGroup = new ButtonGroup();
        userToggle  = roleToggleBtn("👤  User");
        adminToggle = roleToggleBtn("🔧  Administrator");
        userToggle.setSelected(true);
        styleRoleBtn(userToggle,  true);
        styleRoleBtn(adminToggle, false);
        roleGroup.add(userToggle);
        roleGroup.add(adminToggle);
        userToggle.addActionListener(e -> {
            styleRoleBtn(userToggle, true);
            styleRoleBtn(adminToggle, false);
        });
        adminToggle.addActionListener(e -> {
            styleRoleBtn(adminToggle, true);
            styleRoleBtn(userToggle, false);
        });

        JPanel roleRow = new JPanel(new GridLayout(1, 2, 0, 0));
        roleRow.setOpaque(false);
        roleRow.setBorder(new LineBorder(Theme.BORDER, 1, true));
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        roleRow.setAlignmentX(LEFT_ALIGNMENT);
        roleRow.add(userToggle);
        roleRow.add(adminToggle);

        JLabel unLabel = Components.sectionLabel("Username");
        unLabel.setAlignmentX(LEFT_ALIGNMENT);
        usernameField = Components.textField("e.g. j.doe");
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel pwLabel = Components.sectionLabel("Password");
        pwLabel.setAlignmentX(LEFT_ALIGNMENT);
        passwordField = Components.passwordField();
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // إعداد النص الدموي (errorLabel)
        errorLabel = new JLabel("⚠  Please enter both username and password.");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.ACCENT);
        errorLabel.setBackground(new Color(0xFD, 0xF3, 0xF1));
        errorLabel.setOpaque(true);
        errorLabel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.ACCENT, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        errorLabel.setVisible(false);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton signIn = Components.primaryBtn("Sign In  →");
        signIn.setAlignmentX(LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // ربط زر الدخول بالمنطق البرمجي
        signIn.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        JLabel note = Components.subtitle("Session ends securely on logout.");
        note.setAlignmentX(LEFT_ALIGNMENT);

        form.add(heading);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(20));
        form.add(roleLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(roleRow);
        form.add(Box.createVerticalStrut(18));
        form.add(unLabel);
        form.add(Box.createVerticalStrut(5));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));
        form.add(pwLabel);
        form.add(Box.createVerticalStrut(5));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(14));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(signIn);
        form.add(Box.createVerticalStrut(10));
        form.add(note);

        outer.add(form);
        return outer;
    }

    // ── Helpers ───────────────────────────────────────────────

    private JToggleButton roleToggleBtn(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFont(Theme.FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private void styleRoleBtn(JToggleButton b, boolean selected) {
        b.setBackground(selected ? Theme.INK   : Theme.PAPER);
        b.setForeground(selected ? Theme.PAPER : Theme.MUTED);
    }

    private void showError(String msg) {
        errorLabel.setText("⚠  " + msg);
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }

    /**
     * منطق تسجيل الدخول المربوط بملفات JSON
     */
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        boolean isAdmin = adminToggle.isSelected();

        // فحص الحقول الفارغة
        if (user.isEmpty() || pass.isEmpty() || user.equals("e.g. j.doe")) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            if (isAdmin) {
                // منطق المسؤول: بحث فقط في ملف admins.json
                List<Administrator> admins = JsonHandler.loadList("admins.json", Administrator.class);
                Administrator found = admins.stream()
                        .filter(a -> a.getEmail().equalsIgnoreCase(user))
                        .findFirst().orElse(null);

                if (found != null) {
                    if (found.getPassword().equals(pass)) {
                        listener.onLogin(user, true);
                    } else {
                        showError("Incorrect password for Administrator!");
                    }
                } else {
                    showError("Administrator account not found.");
                }
            } else {
                // منطق المستخدم: بحث في ملف users.json
                List<User> users = JsonHandler.loadList("users.json", User.class);
                User found = users.stream()
                        .filter(u -> u.getEmail().equalsIgnoreCase(user))
                        .findFirst().orElse(null);

                if (found != null) {
                    if (found.getPassword().equals(pass)) {
                        listener.onLogin(user, false);
                    } else {
                        showError("Wrong password for this user!");
                    }
                } else {
                    // مستخدم جديد: يتم إنشاؤه وحفظه تلقائياً
                    users.add(new User(user, pass));
                    JsonHandler.saveList(users, "users.json");
                    listener.onLogin(user, false);
                }
            }
        } catch (Exception ex) {
            showError("System error: Unable to load database.");
            ex.printStackTrace();
        }
    }
}