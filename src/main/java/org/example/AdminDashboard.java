package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Administrator Dashboard panel for AppointEase.
 * Shows all reservations in a searchable/filterable table.
 * Admins can modify or cancel ANY appointment regardless of owner.
 *
 * @author AppointEase
 * @version 1.0
 */
public class AdminDashboard extends JPanel {

    /** Callback for logout. */
    public interface LogoutListener {
        /** Called when admin clicks Log Out. */
        void onLogout();
    }

    private final String adminName;
    private final LogoutListener logoutListener;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // Raw data (replace with real persistence layer)
    private final Object[][] allData = {
        {"#001", "Jana Doe",         "In-person",  "Mar 14 · 10:00", "30 min", "1", "Confirmed"},
        {"#002", "Omar Khalil",       "Urgent",     "Mar 14 · 11:00", "30 min", "1", "Urgent"},
        {"#003", "Lina Nasser",       "Virtual",    "Mar 15 · 14:00", "60 min", "1", "Pending"},
        {"#004", "Group – Assessment","Group",      "Mar 22 · 09:00", "30 min", "4", "Pending"},
        {"#005", "Karim Saleh",       "Follow-up",  "Mar 23 · 09:30", "30 min", "1", "Confirmed"},
        {"#006", "Reem Haddad",       "Individual", "Mar 24 · 15:00", "60 min", "1", "Confirmed"},
    };

    /**
     * @param adminName      admin's display name
     * @param logoutListener callback for logout
     */
    public AdminDashboard(String adminName, LogoutListener logoutListener) {
        this.adminName = adminName;
        this.logoutListener = logoutListener;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        Color adminBg = new Color(0x1A, 0x0E, 0x0A);
        JPanel side = new JPanel();
        side.setBackground(adminBg);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(210, 0));
        side.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel logo = new JLabel("  AppointEase");
        logo.setFont(new Font("Serif", Font.BOLD, 16));
        logo.setForeground(Theme.PAPER);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));

        JLabel roleChip = new JLabel("  ADMINISTRATOR");
        roleChip.setFont(Theme.FONT_LABEL);
        roleChip.setForeground(Theme.ACCENT);
        roleChip.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        side.add(logo);
        side.add(roleChip);
        side.add(mkSideLabel("MANAGE"));

        JButton reservations = sideBtn(adminBg, "📋", "All Reservations");
        JButton schedule     = sideBtn(adminBg, "🗓", "Schedule View");
        JButton users        = sideBtn(adminBg, "👥", "Users");
        Components.setSidebarActive(reservations);

        side.add(reservations); side.add(schedule); side.add(users);
        side.add(mkSideLabel("CONFIG"));

        JButton settings = sideBtn(adminBg, "⚙", "Slot Settings");
        JButton reports  = sideBtn(adminBg, "📊", "Reports");
        JButton logout   = sideBtn(adminBg, "🚪", "Log out");
        logout.setForeground(Theme.ACCENT);
        logout.addActionListener(e -> logoutListener.onLogout());

        side.add(settings); side.add(reports);
        side.add(Box.createVerticalGlue());
        side.add(Components.divider());
        side.add(Box.createVerticalStrut(8));
        side.add(logout);
        return side;
    }

    // ── Main Content ──────────────────────────────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.PAPER);
        main.add(buildTopBar(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(Theme.PAPER);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        content.add(buildStats());
        content.add(Box.createVerticalStrut(20));
        content.add(buildReservationsCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PAPER);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(Components.sectionLabel("Administrator Panel"));
        left.add(new JLabel("Reservation Management") {{
            setFont(new Font("Serif", Font.BOLD, 20)); setForeground(Theme.INK);
        }});

        // Admin avatar chip
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chip.setOpaque(false);
        JLabel avatar = new JLabel(String.valueOf(adminName.charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setForeground(Theme.WHITE);
        avatar.setFont(new Font("Serif", Font.BOLD, 14));
        JPanel nameCol = new JPanel(new GridLayout(2, 1));
        nameCol.setOpaque(false);
        nameCol.add(new JLabel(adminName) {{ setFont(new Font("SansSerif", Font.BOLD, 12)); setForeground(Theme.INK); }});
        nameCol.add(new JLabel("Administrator") {{ setFont(Theme.FONT_SMALL); setForeground(Theme.MUTED); }});
        chip.add(avatar); chip.add(nameCol);

        bar.add(left, BorderLayout.WEST);
        bar.add(chip, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.add(Components.statCard("" + allData.length, "Total today",      Theme.ACCENT2));
        row.add(Components.statCard("4",                  "Confirmed",        Theme.SUCCESS));
        row.add(Components.statCard("1",                  "Urgent cases",     Theme.ACCENT));
        row.add(Components.statCard("3",                  "Slots remaining",  Theme.WARNING));
        return row;
    }

    private JPanel buildReservationsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 12));

        // Card header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("ALL APPOINTMENTS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        JButton addBtn = Components.primaryBtn("+ Add Manually");
        addBtn.setFont(new Font("Monospaced", Font.BOLD, 10));
        addBtn.addActionListener(e -> showAddDialog());
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Search + filter
        JPanel controls = new JPanel(new BorderLayout(0, 8));
        controls.setOpaque(false);

        // Search bar
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(Theme.CARD);
        searchRow.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JLabel searchIcon = new JLabel("🔍");
        searchField = new JTextField("Search by user, date, type, status…");
        searchField.setFont(Theme.FONT_BODY);
        searchField.setForeground(Theme.MUTED);
        searchField.setBorder(null);
        searchField.setBackground(Theme.CARD);
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().startsWith("Search")) {
                    searchField.setText(""); searchField.setForeground(Theme.INK);
                }
            }
        });
        searchField.addActionListener(e -> filterTable(searchField.getText()));
        searchRow.add(searchIcon, BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        // Filter pills
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pills.setOpaque(false);
        String[] filters = {"All","Confirmed","Pending","Urgent","Virtual","Group","Today","This week"};
        ButtonGroup fg = new ButtonGroup();
        for (String f : filters) {
            JToggleButton pill = pillBtn(f);
            if (f.equals("All")) { pill.setSelected(true); stylePill(pill, true); }
            pill.addActionListener(e -> {
                for (Component c : pills.getComponents())
                    if (c instanceof JToggleButton) stylePill((JToggleButton) c, false);
                stylePill(pill, true);
                filterTable(f.equals("All") ? "" : f);
            });
            fg.add(pill); pills.add(pill);
        }

        controls.add(searchRow, BorderLayout.NORTH);
        controls.add(pills, BorderLayout.SOUTH);
        card.add(controls, BorderLayout.CENTER);

        // Table
        String[] cols = {"#", "User", "Type", "Date & Time", "Duration", "Participants", "Status", "Admin Actions"};
        tableModel = new DefaultTableModel(allData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        table = new JTable(tableModel);
        Components.styleTable(table);
        table.getColumn("Status").setCellRenderer(new StatusRenderer());
        table.getColumn("Type").setCellRenderer(new TypeRenderer());
        table.getColumn("Admin Actions").setCellRenderer(new AdminActionRenderer());
        table.getColumn("Admin Actions").setCellEditor(new AdminActionEditor(new JCheckBox()));
        table.getColumn("#").setPreferredWidth(40);
        table.getColumn("Admin Actions").setPreferredWidth(160);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(Theme.BORDER, 1, true));
        sp.getViewport().setBackground(Theme.CARD);
        sp.setPreferredSize(new Dimension(0, 240));
        card.add(sp, BorderLayout.SOUTH);

        // Footer note
        JLabel note = new JLabel("  ⚠  Only administrators can modify or cancel any reservation. Past appointments are read-only.");
        note.setFont(Theme.FONT_SMALL); note.setForeground(Theme.MUTED);
        // Add below sp
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.add(note, BorderLayout.SOUTH);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return wrap;
    }

    // ── Helpers ───────────────────────────────────────────────

    private void filterTable(String query) {
        tableModel.setRowCount(0);
        String q = query.toLowerCase();
        for (Object[] row : allData) {
            boolean match = q.isEmpty();
            for (Object cell : row) {
                if (cell.toString().toLowerCase().contains(q)) { match = true; break; }
            }
            if (match) tableModel.addRow(row);
        }
    }

    private void showAddDialog() {
        JOptionPane.showMessageDialog(this,
            "Manual booking dialog — wire to your AppointmentService here.",
            "Add Appointment", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel mkSideLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(new Color(0x55, 0x4E, 0x44));
        l.setBorder(BorderFactory.createEmptyBorder(16, 16, 4, 16));
        return l;
    }

    private JButton sideBtn(Color bg, String icon, String label) {
        JButton b = Components.sidebarItem(icon, label);
        b.setBackground(bg);
        return b;
    }

    private JToggleButton pillBtn(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFont(Theme.FONT_LABEL);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        b.setForeground(Theme.MUTED);
        return b;
    }

    private void stylePill(JToggleButton b, boolean active) {
        if (active) {
            b.setBackground(Theme.INK);
            b.setForeground(Theme.PAPER);
            b.setOpaque(true);
        } else {
            b.setBackground(null);
            b.setForeground(Theme.MUTED);
            b.setOpaque(false);
        }
    }

    // ── Cell Renderers ────────────────────────────────────────

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            String val = v == null ? "" : v.toString();
            JLabel tag;
            switch (val) {
                case "Confirmed": tag = Components.tagConfirmed(); break;
                case "Pending":   tag = Components.tagPending();   break;
                case "Urgent":    tag = Components.tagUrgent();    break;
                case "Virtual":   tag = Components.tagVirtual();   break;
                default:          tag = new JLabel(val);
            }
            tag.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            tag.setOpaque(true);
            return tag;
        }
    }

    private static class TypeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            String val = v == null ? "" : v.toString();
            JLabel tag;
            switch (val) {
                case "Urgent":    tag = Components.tagUrgent();    break;
                case "Virtual":   tag = Components.tagVirtual();   break;
                case "Group":     tag = Components.tagGroup();     break;
                case "In-person": tag = Components.tag("In-person", new Color(0xE3,0xF0,0xFA), Theme.ACCENT2); break;
                case "Follow-up": tag = Components.tag("Follow-up", new Color(0xFA,0xEE,0xE3), Theme.WARNING); break;
                default:          tag = new JLabel(val);
            }
            tag.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            tag.setOpaque(true);
            return tag;
        }
    }

    private static class AdminActionRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            p.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            p.add(Components.outlineBtn("Edit"));
            p.add(Components.dangerBtn("Cancel"));
            return p;
        }
    }

    private class AdminActionEditor extends DefaultCellEditor {
        public AdminActionEditor(JCheckBox c) { super(c); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            p.setBackground(Theme.CREAM);
            JButton edit = Components.outlineBtn("Edit");
            JButton can  = Components.dangerBtn("Cancel");
            String user  = tableModel.getValueAt(row, 1).toString();
            edit.addActionListener(e -> {
                JOptionPane.showMessageDialog(t,
                    "Edit dialog for " + user + " — connect to your AppointmentService.");
                stopCellEditing();
            });
            can.addActionListener(e -> {
                int r = JOptionPane.showConfirmDialog(t,
                    "Cancel reservation for " + user + "?\nSlot will become available again.",
                    "Admin Cancel", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(row);
                    JOptionPane.showMessageDialog(t, "Reservation cancelled. Slot freed.");
                }
                stopCellEditing();
            });
            p.add(edit); p.add(can);
            return p;
        }
    }
}
