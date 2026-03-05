package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * User Dashboard panel for AppointEase.
 * Shows upcoming appointments, available slots, quick-book form,
 * and a notification feed. Users can modify and cancel their own
 * future appointments only.
 *
 * @author AppointEase
 * @version 1.0
 */
public class UserDashboard extends JPanel {

    /** Callback for logout action. */
    public interface LogoutListener {
        /** Called when user clicks Log Out. */
        void onLogout();
    }

    private final String username;
    private final LogoutListener logoutListener;

    // Slot colors
    private static final Color SLOT_AVAILABLE = Theme.PAPER;
    private static final Color SLOT_BOOKED    = Theme.CREAM;
    private static final Color SLOT_SELECTED  = Theme.ACCENT;

    /**
     * @param username       logged-in user's name
     * @param logoutListener callback for logout
     */
    public UserDashboard(String username, LogoutListener logoutListener) {
        this.username = username;
        this.logoutListener = logoutListener;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.SIDEBAR_BG);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(210, 0));
        side.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        // Logo
        JLabel logo = new JLabel("  AppointEase");
        logo.setFont(new Font("Serif", Font.BOLD, 16));
        logo.setForeground(Theme.PAPER);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        logo.setAlignmentX(LEFT_ALIGNMENT);
        side.add(logo);
        side.add(mkSideLabel("MAIN"));

        JButton appts  = Components.sidebarItem("📅", "My Appointments");
        JButton slots  = Components.sidebarItem("🔍", "Browse Slots");
        JButton book   = Components.sidebarItem("➕", "Book Appointment");
        Components.setSidebarActive(appts);

        side.add(appts); side.add(slots); side.add(book);
        side.add(mkSideLabel("ACCOUNT"));

        JButton notifs  = Components.sidebarItem("🔔", "Notifications");
        JButton profile = Components.sidebarItem("👤", "My Profile");
        JButton logout  = Components.sidebarItem("🚪", "Log out");
        logout.setForeground(Theme.ACCENT);
        logout.addActionListener(e -> logoutListener.onLogout());

        side.add(notifs); side.add(profile);
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

        // Top bar
        main.add(buildTopBar(), BorderLayout.NORTH);

        // Scrollable content
        JPanel content = new JPanel();
        content.setBackground(Theme.PAPER);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        content.add(buildStats());
        content.add(Box.createVerticalStrut(20));
        content.add(buildAppointmentsCard());
        content.add(Box.createVerticalStrut(16));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        row2.add(buildSlotsCard());
        row2.add(buildQuickBookCard());
        content.add(row2);
        content.add(Box.createVerticalStrut(16));
        content.add(buildNotificationsCard());
        content.add(Box.createVerticalStrut(24));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Theme.PAPER);
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
        left.add(Components.sectionLabel("User Dashboard"));
        left.add(new JLabel("Good morning, " + capitalize(username) + ".") {{
            setFont(new Font("Serif", Font.BOLD, 20));
            setForeground(Theme.INK);
        }});

        // Avatar chip
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chip.setOpaque(false);
        JLabel avatar = new JLabel(String.valueOf(username.charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT2);
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
        nameCol.add(new JLabel(capitalize(username)) {{ setFont(new Font("SansSerif", Font.BOLD, 12)); setForeground(Theme.INK); }});
        nameCol.add(new JLabel("Patient · User") {{ setFont(Theme.FONT_SMALL); setForeground(Theme.MUTED); }});
        chip.add(avatar); chip.add(nameCol);

        bar.add(left, BorderLayout.WEST);
        bar.add(chip, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.add(Components.statCard("3", "Upcoming appointments", Theme.ACCENT2));
        row.add(Components.statCard("7", "Completed",             Theme.SUCCESS));
        row.add(Components.statCard("1", "Pending confirmation",  Theme.ACCENT));
        row.add(Components.statCard("2", "Reminders today",       Theme.WARNING));
        return row;
    }

    private JPanel buildAppointmentsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("MY UPCOMING APPOINTMENTS");
        title.setFont(Theme.FONT_LABEL);
        title.setForeground(Theme.MUTED);
        JButton addBtn = Components.primaryBtn("+ Book New");
        addBtn.setFont(new Font("Monospaced", Font.BOLD, 10));
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Date", "Appointment", "Time", "Type", "Participants", "Status", "Actions"};
        Object[][] data = {
            {"Mar 14", "General Check-up",       "10:00–10:30", "In-person", "1", "Confirmed", ""},
            {"Mar 19", "Follow-up Consultation",  "14:00–14:30", "Virtual",   "1", "Virtual",   ""},
            {"Mar 22", "Assessment Session",      "09:00–09:30", "Group",     "4", "Pending",   ""}
        };
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Components.styleTable(table);

        // Actions column with buttons
        table.getColumn("Actions").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Actions").setCellEditor(new ActionCellEditor(new JCheckBox()));
        table.getColumn("Status").setCellRenderer(new StatusTagRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(Theme.BORDER, 1, true));
        sp.getViewport().setBackground(Theme.CARD);
        card.add(sp, BorderLayout.CENTER);

        // Hint
        JLabel hint = new JLabel("  ⚠  Only future appointments can be modified or cancelled.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.MUTED);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildSlotsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("AVAILABLE SLOTS — MAR 14");
        t.setFont(Theme.FONT_LABEL); t.setForeground(Theme.MUTED);
        JLabel ch = new JLabel("Change date ›");
        ch.setFont(Theme.FONT_SMALL); ch.setForeground(Theme.ACCENT);
        ch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(t, BorderLayout.WEST); header.add(ch, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Slot grid
        JPanel grid = new JPanel(new GridLayout(3, 4, 6, 6));
        grid.setOpaque(false);
        String[] times = {"09:00","09:30","10:00","10:30","11:00","11:30","13:00","13:30","14:00","14:30","15:00","15:30"};
        boolean[] booked = {false,false,true,false,true,true,false,false,false,true,false,false};
        ButtonGroup slotGroup = new ButtonGroup();
        for (int i = 0; i < times.length; i++) {
            final boolean bk = booked[i];
            JToggleButton slot = new JToggleButton(times[i]);
            slot.setFont(Theme.FONT_BUTTON);
            slot.setFocusPainted(false);
            slot.setEnabled(!bk);
            slot.setBackground(bk ? SLOT_BOOKED : SLOT_AVAILABLE);
            slot.setForeground(bk ? Theme.MUTED  : Theme.INK);
            if (bk) slot.setBorder(new LineBorder(Theme.BORDER, 1, true));
            else {
                slot.setBorder(new LineBorder(Theme.BORDER, 1, true));
                slot.addActionListener(e -> {
                    slot.setBackground(slot.isSelected() ? SLOT_SELECTED : SLOT_AVAILABLE);
                    slot.setForeground(slot.isSelected() ? Theme.WHITE    : Theme.INK);
                });
            }
            slotGroup.add(slot);
            grid.add(slot);
        }
        card.add(grid, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(Theme.PAPER, "Available"));
        legend.add(legendDot(Theme.CREAM, "Booked"));
        legend.add(legendDot(Theme.ACCENT, "Selected"));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildQuickBookCard() {
        JPanel card = Components.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("QUICK BOOK");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        String[] types = {"— Appointment Type —","Urgent","Follow-up","Assessment","Virtual","In-person","Individual","Group"};
        String[] parts = {"— Participants —","1","2","3","4 (max)"};
        String[] durs  = {"— Duration —","30 min (½ hr)","60 min (1 hr)"};

        JComboBox<String> typeBox = Components.comboBox(types);
        JComboBox<String> partBox = Components.comboBox(parts);
        JComboBox<String> durBox  = Components.comboBox(durs);
        for (JComboBox<String> cb : new JComboBox[]{typeBox, partBox, durBox}) {
            cb.setAlignmentX(LEFT_ALIGNMENT);
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        }

        JLabel warn = new JLabel("⚠  Max duration: 1 hr · Participants vary by type");
        warn.setFont(Theme.FONT_SMALL); warn.setForeground(Theme.MUTED);
        warn.setBackground(Theme.CREAM); warn.setOpaque(true);
        warn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        warn.setAlignmentX(LEFT_ALIGNMENT);
        warn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton confirm = Components.primaryBtn("Confirm Booking  →");
        confirm.setAlignmentX(LEFT_ALIGNMENT);
        confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        confirm.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Appointment booked! Status: Confirmed.", "Booking Confirmed",
            JOptionPane.INFORMATION_MESSAGE));

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(typeBox);
        card.add(Box.createVerticalStrut(8));
        card.add(partBox);
        card.add(Box.createVerticalStrut(8));
        card.add(durBox);
        card.add(Box.createVerticalStrut(8));
        card.add(warn);
        card.add(Box.createVerticalStrut(10));
        card.add(confirm);
        return card;
    }

    private JPanel buildNotificationsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("NOTIFICATIONS & REMINDERS");
        t.setFont(Theme.FONT_LABEL); t.setForeground(Theme.MUTED);
        JLabel mark = new JLabel("Mark all read ›");
        mark.setFont(Theme.FONT_SMALL); mark.setForeground(Theme.ACCENT);
        mark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(t, BorderLayout.WEST); header.add(mark, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(Components.notifRow("🔔","Reminder","Your appt with Dr. Mahmoud is tomorrow at 10:00.","Today · 08:00", Theme.WARNING));
        list.add(Box.createVerticalStrut(6));
        list.add(Components.notifRow("✅","Confirmed","Follow-up on Mar 19 has been confirmed.","Yesterday · 14:32", Theme.SUCCESS));
        list.add(Box.createVerticalStrut(6));
        list.add(Components.notifRow("ℹ","Slot Freed","An earlier slot (Mar 14, 09:00) is now available.","Yesterday · 11:05", Theme.ACCENT2));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────

    private JLabel mkSideLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(new Color(0x55, 0x4E, 0x44));
        l.setBorder(BorderFactory.createEmptyBorder(16, 16, 4, 16));
        return l;
    }

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setForeground(color);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JLabel txt = new JLabel(label);
        txt.setFont(Theme.FONT_SMALL);
        txt.setForeground(Theme.MUTED);
        p.add(dot); p.add(txt);
        return p;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Table Cell Renderers ──────────────────────────────────

    /** Renders status column as colored tag labels. */
    private static class StatusTagRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            String val = v == null ? "" : v.toString();
            JLabel tag;
            switch (val) {
                case "Confirmed": tag = Components.tagConfirmed(); break;
                case "Virtual":   tag = Components.tagVirtual();   break;
                case "Pending":   tag = Components.tagPending();   break;
                case "Urgent":    tag = Components.tagUrgent();    break;
                default:          tag = new JLabel(val);
            }
            tag.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            tag.setOpaque(true);
            return tag;
        }
    }

    /** Renders action buttons in the Actions column. */
    private static class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            p.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            p.add(Components.outlineBtn("Modify"));
            p.add(Components.dangerBtn("Cancel"));
            return p;
        }
    }

    /** Dummy editor so action buttons row doesn't freeze on click. */
    private static class ActionCellEditor extends DefaultCellEditor {
        public ActionCellEditor(JCheckBox c) { super(c); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            p.setBackground(Theme.CREAM);
            JButton mod = Components.outlineBtn("Modify");
            JButton can = Components.dangerBtn("Cancel");
            can.addActionListener(e -> {
                int r = JOptionPane.showConfirmDialog(t,
                    "Cancel this appointment?", "Confirm Cancel",
                    JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION)
                    JOptionPane.showMessageDialog(t, "Appointment cancelled. Slot is now available.");
                stopCellEditing();
            });
            mod.addActionListener(e -> {
                JOptionPane.showMessageDialog(t, "Modify dialog — connect to your booking form.");
                stopCellEditing();
            });
            p.add(mod); p.add(can);
            return p;
        }
    }
}
