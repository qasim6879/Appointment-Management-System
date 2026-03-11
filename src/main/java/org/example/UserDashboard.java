package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * User Dashboard panel for AppointEase.
 * Sidebar navigates between three views via CardLayout:
 *   - My Appointments  (default)
 *   - Book Appointment (slots picker + booking form)
 *   - Notifications    (reminder / confirmation feed)
 * Only "Log out" is always visible at the bottom of the sidebar.
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

    private static final String CARD_APPOINTMENTS = "APPOINTMENTS";
    private static final String CARD_BOOK         = "BOOK";
    private static final String CARD_NOTIFS       = "NOTIFICATIONS";

    private static final Color SLOT_AVAILABLE = Theme.PAPER;
    private static final Color SLOT_BOOKED    = Theme.CREAM;
    private static final Color SLOT_SELECTED  = Theme.ACCENT;

    private final String         username;
    private final LogoutListener logoutListener;

    private CardLayout contentCards;
    private JPanel     contentPanel;

    // Sidebar nav buttons kept as fields to manage active state
    private JButton btnAppts;
    private JButton btnBook;
    private JButton btnNotifs;

    /**
     * @param username       logged-in user's name
     * @param logoutListener callback for logout
     */
    public UserDashboard(String username, LogoutListener logoutListener) {
        this.username = username;
        this.logoutListener = logoutListener;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);

        // Build card panel first so sidebar buttons can reference switchTo()
        contentCards = new CardLayout();
        contentPanel = new JPanel(contentCards);
        contentPanel.setBackground(Theme.PAPER);
        contentPanel.add(wrapScrollable(buildAppointmentsView()), CARD_APPOINTMENTS);
        contentPanel.add(wrapScrollable(buildBookView()),         CARD_BOOK);
        contentPanel.add(wrapScrollable(buildNotificationsView()),CARD_NOTIFS);

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

        JLabel logo = new JLabel("  AppointEase");
        logo.setFont(new Font("Serif", Font.BOLD, 16));
        logo.setForeground(Theme.PAPER);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 16, 20, 16));
        logo.setAlignmentX(LEFT_ALIGNMENT);
        side.add(logo);

        btnAppts  = Components.sidebarItem("📅", "My Appointments");
        btnBook   = Components.sidebarItem("➕", "Book Appointment");
        btnNotifs = Components.sidebarItem("🔔", "Notifications");

        btnAppts.addActionListener(e  -> switchTo(CARD_APPOINTMENTS));
        btnBook.addActionListener(e   -> switchTo(CARD_BOOK));
        btnNotifs.addActionListener(e -> switchTo(CARD_NOTIFS));

        side.add(btnAppts);
        side.add(btnBook);
        side.add(btnNotifs);

        // Logout pinned at bottom
        JButton logout = Components.sidebarItem("🚪", "Log out");
        logout.setForeground(Theme.ACCENT);
        logout.addActionListener(e -> logoutListener.onLogout());

        side.add(Box.createVerticalGlue());
        side.add(Components.divider());
        side.add(Box.createVerticalStrut(8));
        side.add(logout);

        setActive(btnAppts); // default
        return side;
    }

    /**
     * Shows the given card and updates the active sidebar highlight.
     * @param card one of CARD_APPOINTMENTS, CARD_BOOK, CARD_NOTIFS
     */
    private void switchTo(String card) {
        contentCards.show(contentPanel, card);
        switch (card) {
            case CARD_APPOINTMENTS: setActive(btnAppts);  break;
            case CARD_BOOK:         setActive(btnBook);   break;
            case CARD_NOTIFS:       setActive(btnNotifs); break;
        }
    }

    /** Highlights one sidebar button and resets the others. */
    private void setActive(JButton active) {
        for (JButton b : new JButton[]{btnAppts, btnBook, btnNotifs}) {
            boolean on = (b == active);
            b.putClientProperty("active", on);
            b.setBackground(on ? Theme.ACCENT    : Theme.SIDEBAR_BG);
            b.setForeground(on ? Theme.WHITE     : new Color(0xBB, 0xB4, 0xA8));
        }
    }

    // ── Outer shell: top bar + card switcher ──────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.PAPER);
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);
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
        JLabel greeting = new JLabel("Good morning, " + capitalize(username) + ".");
        greeting.setFont(new Font("Serif", Font.BOLD, 20));
        greeting.setForeground(Theme.INK);
        left.add(greeting);

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
        JLabel nameL = new JLabel(capitalize(username));
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12)); nameL.setForeground(Theme.INK);
        JLabel roleL = new JLabel(" Mr.User");
        roleL.setFont(Theme.FONT_SMALL); roleL.setForeground(Theme.MUTED);
        nameCol.add(nameL); nameCol.add(roleL);
        chip.add(avatar); chip.add(nameCol);

        bar.add(left, BorderLayout.WEST);
        bar.add(chip, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane wrapScrollable(JPanel content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBackground(Theme.PAPER);
        return sp;
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW 1 — MY APPOINTMENTS
    // ══════════════════════════════════════════════════════════

    private JPanel buildAppointmentsView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        view.add(buildStats());
        view.add(Box.createVerticalStrut(20));
        view.add(buildAppointmentsCard());
        view.add(Box.createVerticalStrut(24));
        return view;
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("MY UPCOMING APPOINTMENTS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        // "Book New" jumps straight to the Book view
        JButton addBtn = Components.primaryBtn("+ Book New");
        addBtn.setFont(new Font("Monospaced", Font.BOLD, 10));
        addBtn.addActionListener(e -> switchTo(CARD_BOOK));
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        String[] cols = {"Date", "Appointment", "Time", "Type", "Participants", "Status", "Actions"};
        Object[][] data = {
            {"Mar 14", "General Check-up",      "10:00–10:30", "In-person", "1", "Confirmed", ""},
            {"Mar 19", "Follow-up Consultation", "14:00–14:30", "Virtual",   "1", "Virtual",   ""},
            {"Mar 22", "Assessment Session",     "09:00–09:30", "Group",     "4", "Pending",   ""}
        };
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        JTable table = new JTable(model);
        Components.styleTable(table);
        table.getColumn("Status").setCellRenderer(new StatusTagRenderer());
        table.getColumn("Actions").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Actions").setCellEditor(new ActionCellEditor(new JCheckBox(), model));
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(Theme.BORDER, 1, true));
        sp.getViewport().setBackground(Theme.CARD);
        card.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("  ⚠  Only future appointments can be modified or cancelled.");
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(Theme.MUTED);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW 2 — BOOK APPOINTMENT
    // ══════════════════════════════════════════════════════════

    private JPanel buildBookView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Book an Appointment");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Components.subtitle("Pick a free slot, choose the appointment details, then confirm.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        view.add(heading);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(20));

        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        row.add(buildSlotsCard());
        row.add(buildBookingFormCard());
        view.add(row);
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    private JPanel buildSlotsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("AVAILABLE SLOTS — MAR 14");
        t.setFont(Theme.FONT_LABEL); t.setForeground(Theme.MUTED);
        JLabel ch = new JLabel("Change date ›");
        ch.setFont(Theme.FONT_SMALL); ch.setForeground(Theme.ACCENT);
        ch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(t, BorderLayout.WEST); header.add(ch, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 4, 6, 6));
        grid.setOpaque(false);
        String[]  times  = {"09:00","09:30","10:00","10:30","11:00","11:30",
                             "13:00","13:30","14:00","14:30","15:00","15:30"};
        boolean[] booked = { false,  false,  true,   false,  true,   true,
                              false,  false,  false,  true,   false,  false };
        ButtonGroup slotGroup = new ButtonGroup();
        for (int i = 0; i < times.length; i++) {
            final boolean bk = booked[i];
            JToggleButton slot = new JToggleButton(times[i]);
            slot.setFont(Theme.FONT_BUTTON);
            slot.setFocusPainted(false);
            slot.setEnabled(!bk);
            slot.setBackground(bk ? SLOT_BOOKED : SLOT_AVAILABLE);
            slot.setForeground(bk ? Theme.MUTED  : Theme.INK);
            slot.setBorder(new LineBorder(Theme.BORDER, 1, true));
            if (!bk) {
                slot.addActionListener(e -> {
                    slot.setBackground(slot.isSelected() ? SLOT_SELECTED : SLOT_AVAILABLE);
                    slot.setForeground(slot.isSelected() ? Theme.WHITE    : Theme.INK);
                });
            }
            slotGroup.add(slot);
            grid.add(slot);
        }
        card.add(grid, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(Theme.PAPER,  "Available"));
        legend.add(legendDot(Theme.CREAM,  "Booked"));
        legend.add(legendDot(Theme.ACCENT, "Selected"));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBookingFormCard() {
        JPanel card = Components.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("APPOINTMENT DETAILS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        String[] types = {"— Appointment Type —","Urgent","Follow-up","Assessment",
                          "Virtual","In-person","Individual","Group"};
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
        confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirm.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Appointment booked successfully!\nStatus: Confirmed.",
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            switchTo(CARD_APPOINTMENTS); // return to appointments view after booking
        });

        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(typeBox);
        card.add(Box.createVerticalStrut(8));
        card.add(partBox);
        card.add(Box.createVerticalStrut(8));
        card.add(durBox);
        card.add(Box.createVerticalStrut(10));
        card.add(warn);
        card.add(Box.createVerticalStrut(12));
        card.add(confirm);
        card.add(Box.createVerticalGlue());
        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW 3 — NOTIFICATIONS
    // ══════════════════════════════════════════════════════════

    private JPanel buildNotificationsView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Notifications & Reminders");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        headerRow.add(heading, BorderLayout.WEST);
        JLabel mark = new JLabel("Mark all read ›");
        mark.setFont(Theme.FONT_SMALL); mark.setForeground(Theme.ACCENT);
        mark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerRow.add(mark, BorderLayout.EAST);

        JLabel sub = Components.subtitle("Reminders, confirmations, and slot updates appear here.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        view.add(headerRow);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(20));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        list.add(Components.notifRow("🔔","Reminder",
            "Your appointment with Dr. Mahmoud is tomorrow at 10:00.",
            "Today · 08:00", Theme.WARNING));
        list.add(Box.createVerticalStrut(8));
        list.add(Components.notifRow("✅","Confirmed",
            "Your Follow-up on Mar 19 has been confirmed.",
            "Yesterday · 14:32", Theme.SUCCESS));
        list.add(Box.createVerticalStrut(8));
        list.add(Components.notifRow("ℹ","Slot Freed",
            "An earlier slot (Mar 14, 09:00) is now available.",
            "Yesterday · 11:05", Theme.ACCENT2));
        list.add(Box.createVerticalStrut(8));
        list.add(Components.notifRow("🔔","Reminder",
            "Assessment Session on Mar 22 starts in 3 days.",
            "Mar 6 · 09:00", Theme.WARNING));

        view.add(list);
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    // ── Helpers ───────────────────────────────────────────────

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setForeground(color);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JLabel txt = new JLabel(label);
        txt.setFont(Theme.FONT_SMALL); txt.setForeground(Theme.MUTED);
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
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
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

    /** Renders Modify + Cancel buttons in the Actions column. */
    private static class ActionCellRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            p.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            p.add(Components.dangerBtn("Cancel"));
            return p;
        }
    }

    /** Editor so Modify / Cancel buttons are interactive when clicked. */
    private static class ActionCellEditor extends DefaultCellEditor {
        private final DefaultTableModel model;

        /**
         * @param c     dummy checkbox required by DefaultCellEditor
         * @param model table model used to remove cancelled rows
         */
        public ActionCellEditor(JCheckBox c, DefaultTableModel model) {
            super(c);
            this.model = model;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                                                     boolean sel, int row, int col) {

            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            p.setBackground(Theme.CREAM);

            JButton can = Components.dangerBtn("Cancel");

            can.addActionListener(e -> {
                int r = JOptionPane.showConfirmDialog(t,
                        "Cancel this appointment?\nThe slot will become available again.",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

                if (r == JOptionPane.YES_OPTION) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(t,
                            "Appointment cancelled. Slot is now free.");
                }

                stopCellEditing();
            });

            p.add(can);
            return p;
        }
    }
}
