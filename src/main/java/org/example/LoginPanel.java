package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {

    public interface LoginListener {
        void onLogin(String username, boolean isAdmin);
    }

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JToggleButton  userToggle;
    private JToggleButton  adminToggle;
    private JLabel         errorLabel;
    private final LoginListener listener;

    public LoginPanel(LoginListener listener) {
        this.listener = listener;
        setLayout(new GridLayout(1, 2));
        setBackground(Theme.INK);
        add(buildLeft());
        add(buildRight());
    }

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
        JLabel brand = new JLabel("<html><span style='color:#f5f0e8'>Appoint</span><span style='color:#c84b2f'><i>Ease.</i></span></html>");
        brand.setFont(new Font("Serif", Font.BOLD, 36));
        JLabel tag = new JLabel("<html><span style='color:rgba(245,240,232,0.5)'>A structured appointment scheduling<br>system for your institution.</span></html>");
        tag.setFont(Theme.FONT_BODY);
        top.add(brand);
        top.add(Box.createVerticalStrut(12));
        top.add(tag);

        JPanel features = new JPanel();
        features.setOpaque(false);
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        for (String f : new String[]{
                "Book, modify & cancel appointments",
                "Multiple types: urgent, virtual, group…",
                "Automatic reminders & notifications",
                "Admin-level override & full schedule control"}) {
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

    private JPanel buildRight() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.CARD);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 480));

        JLabel heading = new JLabel("Sign in");
        heading.setFont(new Font("Serif", Font.BOLD, 28));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Components.subtitle("Enter your credentials to continue.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup roleGroup = new ButtonGroup();
        userToggle  = roleToggleBtn("👤  User");
        adminToggle = roleToggleBtn("🔧  Administrator");
        userToggle.setSelected(true);
        styleRoleBtn(userToggle, true);
        styleRoleBtn(adminToggle, false);
        roleGroup.add(userToggle);
        roleGroup.add(adminToggle);
        userToggle.addActionListener(e  -> { styleRoleBtn(userToggle, true);  styleRoleBtn(adminToggle, false); });
        adminToggle.addActionListener(e -> { styleRoleBtn(adminToggle, true); styleRoleBtn(userToggle, false);  });

        JPanel roleRow = new JPanel(new GridLayout(1, 2));
        roleRow.setOpaque(false);
        roleRow.setBorder(new LineBorder(Theme.BORDER, 1, true));
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        roleRow.setAlignmentX(LEFT_ALIGNMENT);
        roleRow.add(userToggle);
        roleRow.add(adminToggle);

        usernameField = Components.textField("e.g. j.doe");
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        passwordField = Components.passwordField();
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.ACCENT);
        errorLabel.setOpaque(false);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton signIn = Components.primaryBtn("Sign In  →");
        signIn.setAlignmentX(LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        signIn.addActionListener(e -> handleSignIn());
        usernameField.addActionListener(e -> handleSignIn());
        passwordField.addActionListener(e -> handleSignIn());

        // "Don't have an account? Sign up" link
        JPanel signUpRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        signUpRow.setOpaque(false);
        signUpRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel noAccount = Components.subtitle("Don't have an account?  ");
        JLabel signUpLink = new JLabel("<html><u>Sign up</u></html>");
        signUpLink.setFont(Theme.FONT_BODY);
        signUpLink.setForeground(Theme.ACCENT2);
        signUpLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openSignUpDialog(); }
            @Override public void mouseEntered(MouseEvent e) { signUpLink.setForeground(Theme.ACCENT2.darker()); }
            @Override public void mouseExited(MouseEvent e)  { signUpLink.setForeground(Theme.ACCENT2); }
        });
        signUpRow.add(noAccount);
        signUpRow.add(signUpLink);

        form.add(heading);                              form.add(Box.createVerticalStrut(4));
        form.add(sub);                                  form.add(Box.createVerticalStrut(20));
        form.add(Components.sectionLabel("I am a:"));  form.add(Box.createVerticalStrut(6));
        form.add(roleRow);                              form.add(Box.createVerticalStrut(18));
        form.add(Components.sectionLabel("Username")); form.add(Box.createVerticalStrut(5));
        form.add(usernameField);                        form.add(Box.createVerticalStrut(14));
        form.add(Components.sectionLabel("Password")); form.add(Box.createVerticalStrut(5));
        form.add(passwordField);                        form.add(Box.createVerticalStrut(10));
        form.add(errorLabel);                           form.add(Box.createVerticalStrut(4));
        form.add(signIn);                               form.add(Box.createVerticalStrut(12));
        form.add(signUpRow);

        outer.add(form);
        return outer;
    }

    private void handleSignIn() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        boolean isAdmin = adminToggle.isSelected();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("⚠  Please enter both fields."); return;
        }

        String file = isAdmin ? "admins.json" : "users.json";

        if (User.signIn(user, pass, file)) {
            hideError();
            listener.onLogin(user, isAdmin);
        } else {
            showError("⚠  Wrong username or password.");
        }
    }

    private void openSignUpDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Create Account", true);
        dialog.setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.CARD);
        root.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setPreferredSize(new Dimension(320, 440));

        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Serif", Font.BOLD, 24));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JTextField     unField  = Components.textField("Choose a unique username");
        JTextField     emField  = Components.textField("e.g. name@example.com");
        JPasswordField pwField  = Components.passwordField();
        JPasswordField pw2Field = Components.passwordField();
        for (JComponent c : new JComponent[]{unField, emField, pwField, pw2Field}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }

        JLabel diagError = new JLabel(" ");
        diagError.setFont(Theme.FONT_SMALL);
        diagError.setForeground(Theme.ACCENT);
        diagError.setOpaque(false);
        diagError.setAlignmentX(LEFT_ALIGNMENT);
        diagError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton createBtn = Components.primaryBtn("Create Account  →");
        createBtn.setAlignmentX(LEFT_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        createBtn.addActionListener(e -> {
            String username = unField.getText().trim();
            String email    = emField.getText().trim();
            String pass     = new String(pwField.getPassword()).trim();
            String pass2    = new String(pw2Field.getPassword()).trim();

            if (!pass.equals(pass2)) {
                showDialogError(diagError, "⚠  Passwords do not match."); return;
            }
            String fileName = adminToggle.isSelected() ? "admins.json" : "users.json";
            int result = User.signUp(username, email, pass, fileName);
            if      (result == 1) showDialogError(diagError, "⚠  Username is invalid or already taken.");
            else if (result == 2) showDialogError(diagError, "⚠  Email is invalid or already taken.");
            else {
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Account created! You can now sign in.", "Welcome!", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(username);
                userToggle.setSelected(true);
                styleRoleBtn(userToggle, true);
                styleRoleBtn(adminToggle, false);
            }
        });
        pw2Field.addActionListener(e -> createBtn.doClick());

        JLabel backLink = new JLabel("← Back to sign in");
        backLink.setFont(Theme.FONT_SMALL);
        backLink.setForeground(Theme.MUTED);
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLink.setAlignmentX(LEFT_ALIGNMENT);
        backLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dialog.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { backLink.setForeground(Theme.INK); }
            @Override public void mouseExited(MouseEvent e)  { backLink.setForeground(Theme.MUTED); }
        });

        content.add(heading);                                                content.add(Box.createVerticalStrut(4));
        content.add(Components.subtitle("Sign-up creates a User account.")); content.add(Box.createVerticalStrut(20));
        content.add(Components.sectionLabel("Username"));                    content.add(Box.createVerticalStrut(5));
        content.add(unField);                                                content.add(Box.createVerticalStrut(14));
        content.add(Components.sectionLabel("Email Address"));               content.add(Box.createVerticalStrut(5));
        content.add(emField);                                                content.add(Box.createVerticalStrut(14));
        content.add(Components.sectionLabel("Password"));                    content.add(Box.createVerticalStrut(5));
        content.add(pwField);                                                content.add(Box.createVerticalStrut(14));
        content.add(Components.sectionLabel("Confirm Password"));            content.add(Box.createVerticalStrut(5));
        content.add(pw2Field);                                               content.add(Box.createVerticalStrut(12));
        content.add(diagError);                                              content.add(Box.createVerticalStrut(4));
        content.add(createBtn);                                              content.add(Box.createVerticalStrut(12));
        content.add(backLink);

        root.add(content);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setOpaque(true);
        errorLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.ACCENT, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        revalidate(); repaint();
    }

    private void hideError() {
        errorLabel.setText(" ");
        errorLabel.setOpaque(false);
        errorLabel.setBorder(null);
        revalidate(); repaint();
    }

    private void showDialogError(JLabel label, String msg) {
        label.setText(msg);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.ACCENT, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        label.revalidate(); label.repaint();
    }

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
        b.setBackground(selected ? Theme.INK : Theme.PAPER);
        b.setForeground(selected ? Theme.PAPER : Theme.MUTED);
    }
}