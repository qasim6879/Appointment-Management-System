package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Factory class for styled, reusable Swing components.
 * All widgets follow the AppointEase design system (Theme).
 *
 * @author AppointEase
 * @version 1.0
 */
public final class Components {

    private Components() {}

    

    /**
     * Creates a section micro-label (ALL CAPS, accent color).
     * @param text label text
     * @return styled JLabel
     */
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.ACCENT);
        return l;
    }

    /**
     * Creates a large serif page title.
     * @param text title text
     * @return styled JLabel
     */
    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_TITLE);
        l.setForeground(Theme.INK);
        return l;
    }

    /**
     * Creates a muted subtitle / helper text label.
     * @param text subtitle text
     * @return styled JLabel
     */
    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.MUTED);
        return l;
    }

    

    /**
     * Primary CTA button (red background, white text).
     * @param text button label
     * @return styled JButton
     */
    public static JButton primaryBtn(String text) {
        JButton b = new RoundButton(text, Theme.ACCENT, Theme.WHITE);
        b.setFont(Theme.FONT_BUTTON);
        return b;
    }

    /**
     * Secondary outline button (ink border, ink text).
     * @param text button label
     * @return styled JButton
     */
    public static JButton outlineBtn(String text) {
        JButton b = new RoundButton(text, Theme.CARD, Theme.INK);
        b.setFont(Theme.FONT_BUTTON);
        ((RoundButton) b).setBorderColor(Theme.INK);
        return b;
    }

    /**
     * Danger / cancel outline button (red border, red text).
     * @param text button label
     * @return styled JButton
     */
    public static JButton dangerBtn(String text) {
        JButton b = new RoundButton(text, Theme.CARD, Theme.ACCENT);
        b.setFont(Theme.FONT_BUTTON);
        ((RoundButton) b).setBorderColor(Theme.ACCENT);
        return b;
    }

    /**
     * Sidebar navigation item button.
     * @param icon  emoji icon prefix
     * @param label item label
     * @return styled JButton
     */
    public static JButton sidebarItem(String icon, String label) {
        JButton b = new JButton(icon + "  " + label);
        b.setFont(Theme.FONT_BODY);
        b.setForeground(new Color(0xBB, 0xB4, 0xA8));
        b.setBackground(Theme.SIDEBAR_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setForeground(Theme.WHITE);
                b.setBackground(new Color(0x2A, 0x22, 0x1A));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!Boolean.TRUE.equals(b.getClientProperty("active"))) {
                    b.setForeground(new Color(0xBB, 0xB4, 0xA8));
                    b.setBackground(Theme.SIDEBAR_BG);
                }
            }
        });
        return b;
    }

    /**
     * Marks a sidebar item as active (highlighted with accent color).
     * @param b sidebar button to activate
     */
    public static void setSidebarActive(JButton b) {
        b.putClientProperty("active", true);
        b.setBackground(Theme.ACCENT);
        b.setForeground(Theme.WHITE);
    }

    

    /**
     * Creates a styled text field with placeholder hint.
     * @param placeholder placeholder hint text
     * @return styled JTextField
     */
    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Theme.MUTED);
                    g2.setFont(getFont());
                    Insets i = getInsets();
                    g2.drawString(placeholder, i.left,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                }
            }
        };
        tf.setFont(Theme.FONT_BODY);
        tf.setForeground(Theme.INK);
        tf.setBackground(Theme.PAPER);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        tf.setCaretColor(Theme.INK);
        return tf;
    }

    /**
     * Creates a styled password field.
     * @return styled JPasswordField
     */
    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_BODY);
        pf.setForeground(Theme.INK);
        pf.setBackground(Theme.PAPER);
        pf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        pf.setCaretColor(Theme.INK);
        return pf;
    }

    /**
     * Creates a styled combo box (dropdown).
     * @param items array of option strings
     * @return styled JComboBox
     */
    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_BODY);
        cb.setForeground(Theme.INK);
        cb.setBackground(Theme.PAPER);
        cb.setBorder(new LineBorder(Theme.BORDER, 1, true));
        return cb;
    }

    

    /**
     * Creates a card panel with border and light background.
     * @return styled JPanel
     */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(Theme.PAD_MD, Theme.PAD_MD, Theme.PAD_MD, Theme.PAD_MD)
        ));
        return p;
    }

    /**
     * Creates a stat card displaying a large number and a description.
     * @param number   the big number string
     * @param desc     description below
     * @param numColor color for the number
     * @return styled JPanel
     */
    public static JPanel statCard(String number, String desc, Color numColor) {
        JPanel p = card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel num = new JLabel(number);
        num.setFont(new Font("Serif", Font.BOLD, 28));
        num.setForeground(numColor);
        JLabel d = new JLabel(desc);
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.MUTED);
        p.add(num);
        p.add(Box.createVerticalStrut(4));
        p.add(d);
        return p;
    }

    

    /**
     * Creates a colored status tag label.
     * @param text tag text
     * @param bg   background color
     * @param fg   foreground color
     * @return styled JLabel as tag
     */
    public static JLabel tag(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text.toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return l;
    }

    /** @return a "Confirmed" tag */
    public static JLabel tagConfirmed() { return tag("Confirmed", Theme.TAG_CONFIRMED_BG, Theme.SUCCESS); }
    /** @return a "Pending" tag */
    public static JLabel tagPending()   { return tag("Pending",   Theme.TAG_PENDING_BG,   Theme.WARNING); }
    /** @return an "Urgent" tag */
    public static JLabel tagUrgent()    { return tag("Urgent",    Theme.TAG_URGENT_BG,    Theme.ACCENT);  }
    /** @return a "Virtual" tag */
    public static JLabel tagVirtual()   { return tag("Virtual",   Theme.TAG_VIRTUAL_BG,   Theme.ACCENT2); }
    /** @return a "Group" tag */
    public static JLabel tagGroup()     { return tag("Group",     Theme.TAG_GROUP_BG,     Theme.TAG_GROUP_FG); }

    

    /**
     * Applies AppointEase styling to a JTable.
     * @param table the table to style
     */
    public static void styleTable(JTable table) {
        table.setFont(Theme.FONT_BODY);
        table.setBackground(Theme.CARD);
        table.setForeground(Theme.INK);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.CREAM);
        table.setSelectionForeground(Theme.INK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_LABEL);
        header.setBackground(Theme.CREAM);
        header.setForeground(Theme.MUTED);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
                setForeground(Theme.INK);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setFont(Theme.FONT_BODY);
                return this;
            }
        });
    }

    

    /**
     * Creates a thin horizontal separator line.
     * @return styled JSeparator
     */
    public static JSeparator divider() {
        JSeparator s = new JSeparator();
        s.setForeground(Theme.BORDER);
        s.setBackground(Theme.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    

    /**
     * Creates a notification row panel with left accent border.
     * @param icon   emoji icon
     * @param title  bold title text
     * @param body   message body text
     * @param time   timestamp string
     * @param accent left-border accent color
     * @return styled JPanel
     */
    public static JPanel notifRow(String icon, String title, String body, String time, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Theme.PAPER);
        row.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            )
        ));
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JPanel text = new JPanel(new GridLayout(3, 1, 0, 1));
        text.setOpaque(false);
        JLabel t  = new JLabel(title); t.setFont(new Font("SansSerif", Font.BOLD, 12)); t.setForeground(Theme.INK);
        JLabel b  = new JLabel(body);  b.setFont(Theme.FONT_BODY);  b.setForeground(Theme.INK);
        JLabel ts = new JLabel(time);  ts.setFont(Theme.FONT_SMALL); ts.setForeground(Theme.MUTED);
        text.add(t); text.add(b); text.add(ts);
        row.add(ico,  BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        return row;
    }

    

    /**
     * A custom JButton with rounded corners and hover darkening effect.
     */
    public static class RoundButton extends JButton {
        private final Color bg, fg;
        private Color   borderColor;
        private boolean hovered = false;

        /**
         * @param text button label
         * @param bg   background color
         * @param fg   foreground (text) color
         */
        public RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg; this.borderColor = bg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        /** @param c border color */
        public void setBorderColor(Color c) { this.borderColor = c; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? bg.darker() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth() - 1.5f, getHeight() - 1.5f, 6, 6));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
