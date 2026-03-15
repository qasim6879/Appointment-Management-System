package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.time.*;

/**
 * Administrator Dashboard panel for AppointEase.
 * Sidebar navigates between four views via CardLayout:
 *   - All Reservations  (default) — searchable/filterable table of every appointment
 *   - Add Appointment             — form to manually book an appointment for any user
 *   - Notifications               — system alerts and booking updates
 *   - Reports                     — placeholder for future statistics
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

    private static final String CARD_RESERVATIONS = "RESERVATIONS";
    private static final String CARD_ADD          = "ADD_APPOINTMENT";
    private static final String CARD_NOTIFS       = "NOTIFICATIONS";
    private static final String CARD_REPORTS      = "REPORTS";

    private static final Color ADMIN_BG = new Color(0x1A, 0x0E, 0x0A);

    private final String         adminName;
    private final LogoutListener logoutListener;

    // Kept as fields so filterTable() can reach them
    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;

    private CardLayout contentCards;
    private JPanel     contentPanel;
    private JPanel     adminNotifsList;

    private JButton btnReservations;
    private JButton btnAdd;
    private JButton btnNotifs;
    private JButton btnReports;

    // Add-appointment form fields
    private JTextField        addUserField;
    private JComboBox<String> addTypeBox;
    private JPanel            addSlotGrid    = new JPanel(new GridLayout(3, 4, 8, 8));
    private List<JToggleButton> addSlotBtns  = new ArrayList<>();
    private ButtonGroup       addSlotGroup   = new ButtonGroup();
    private JToggleButton     addDur30;
    private JToggleButton     addDur60;

    // Demo data — replace with real persistence layer
    private final Object[][] allData = {
            {"#001", "Jana Doe",          "In-person",  "Mar 14 · 10:00", "30 min", "1", "Confirmed"},
            {"#002", "Omar Khalil",        "Urgent",     "Mar 14 · 11:00", "30 min", "1", "Urgent"   },
            {"#003", "Lina Nasser",        "Virtual",    "Mar 15 · 14:00", "60 min", "1", "Pending"  },
            {"#004", "Group – Assessment", "Group",      "Mar 22 · 09:00", "30 min", "4", "Pending"  },
            {"#005", "Karim Saleh",        "Follow-up",  "Mar 23 · 09:30", "30 min", "1", "Confirmed"},
            {"#006", "Reem Haddad",        "Individual", "Mar 24 · 15:00", "60 min", "1", "Confirmed"},
    };

    /**
     * @param adminName      admin's display name
     * @param logoutListener callback for logout
     */
    public AdminDashboard(String adminName, LogoutListener logoutListener) {
        this.adminName      = adminName;
        this.logoutListener = logoutListener;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);

        contentCards = new CardLayout();
        contentPanel = new JPanel(contentCards);
        contentPanel.setBackground(Theme.PAPER);
        contentPanel.add(wrapScrollable(buildReservationsView()),   CARD_RESERVATIONS);
        contentPanel.add(wrapScrollable(buildAddAppointmentView()), CARD_ADD);
        contentPanel.add(wrapScrollable(buildNotificationsView()),  CARD_NOTIFS);
        contentPanel.add(wrapScrollable(buildReportsView()),        CARD_REPORTS);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(ADMIN_BG);
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
        roleChip.setBorder(BorderFactory.createEmptyBorder(0, 16, 20, 16));

        side.add(logo);
        side.add(roleChip);

        btnReservations = mkSideBtn("📋", "All Reservations");
        btnAdd          = mkSideBtn("➕", "Add Appointment");
        btnNotifs       = mkSideBtn("🔔", "Notifications");
        btnReports      = mkSideBtn("📊", "Reports");

        btnReservations.addActionListener(e -> switchTo(CARD_RESERVATIONS));
        btnAdd.addActionListener(e          -> switchTo(CARD_ADD));
        btnNotifs.addActionListener(e -> { switchTo(CARD_NOTIFS); rebuildAdminNotifs(); });
        btnReports.addActionListener(e      -> switchTo(CARD_REPORTS));

        side.add(btnReservations);
        side.add(btnAdd);
        side.add(btnNotifs);
        side.add(btnReports);

        JButton logout = mkSideBtn("🚪", "Log out");
        logout.setForeground(Theme.ACCENT);
        logout.addActionListener(e -> logoutListener.onLogout());

        side.add(Box.createVerticalGlue());
        side.add(Components.divider());
        side.add(Box.createVerticalStrut(8));
        side.add(logout);

        setActive(btnReservations);
        return side;
    }

    /**
     * Switches the content card and updates the sidebar highlight.
     * @param card one of the CARD_* constants
     */
    private void switchTo(String card) {
        contentCards.show(contentPanel, card);
        switch (card) {
            case CARD_RESERVATIONS: setActive(btnReservations); break;
            case CARD_ADD:          setActive(btnAdd);          break;
            case CARD_NOTIFS:       setActive(btnNotifs);       break;
            case CARD_REPORTS:      setActive(btnReports);      break;
        }
    }

    /** Highlights one sidebar button and resets the others. */
    private void setActive(JButton active) {
        for (JButton b : new JButton[]{btnReservations, btnAdd, btnNotifs, btnReports}) {
            boolean on = (b == active);
            b.putClientProperty("active", on);
            b.setBackground(on ? Theme.ACCENT : ADMIN_BG);
            b.setForeground(on ? Theme.WHITE  : new Color(0xBB, 0xB4, 0xA8));
        }
    }

    // ── Outer shell ───────────────────────────────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.PAPER);
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(contentPanel,  BorderLayout.CENTER);
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
        JLabel title = new JLabel("Reservation Management");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(Theme.INK);
        left.add(title);

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
        JLabel nameL = new JLabel(adminName);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(Theme.INK);
        JLabel roleL = new JLabel("Administrator");
        roleL.setFont(Theme.FONT_SMALL);
        roleL.setForeground(Theme.MUTED);
        nameCol.add(nameL);
        nameCol.add(roleL);

        chip.add(avatar);
        chip.add(nameCol);
        bar.add(left, BorderLayout.WEST);
        bar.add(chip, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane wrapScrollable(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.PAPER);
        wrapper.add(content, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBackground(Theme.PAPER);
        return sp;
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW 1 — ALL RESERVATIONS
    // ══════════════════════════════════════════════════════════

    private JPanel buildReservationsView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        view.add(buildStats());
        view.add(Box.createVerticalStrut(20));
        view.add(buildReservationsCard());
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.add(Components.statCard("" + allData.length, "Total today",     Theme.ACCENT2));
        row.add(Components.statCard("4",                  "Confirmed",       Theme.SUCCESS));
        row.add(Components.statCard("1",                  "Urgent cases",    Theme.ACCENT));
        row.add(Components.statCard("3",                  "Slots remaining", Theme.WARNING));
        return row;
    }

    private JPanel buildReservationsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 12));

        JLabel cardTitle = new JLabel("ALL APPOINTMENTS");
        cardTitle.setFont(Theme.FONT_LABEL);
        cardTitle.setForeground(Theme.MUTED);
        card.add(cardTitle, BorderLayout.NORTH);

        // Search bar
        JPanel controls = new JPanel(new BorderLayout(0, 8));
        controls.setOpaque(false);

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
                    searchField.setText("");
                    searchField.setForeground(Theme.INK);
                }
            }
        });
        searchField.addActionListener(e -> filterTable(searchField.getText()));
        searchRow.add(searchIcon,  BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        // Filter pills
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pills.setOpaque(false);
        String[] filters = {"All", "Confirmed", "Pending", "Urgent", "Virtual", "Group", "Today", "This week"};
        ButtonGroup filterGroup = new ButtonGroup();
        for (String f : filters) {
            JToggleButton pill = mkPillBtn(f);
            if (f.equals("All")) { pill.setSelected(true); stylePill(pill, true); }
            pill.addActionListener(e -> {
                for (Component c : pills.getComponents())
                    if (c instanceof JToggleButton) stylePill((JToggleButton) c, false);
                stylePill(pill, true);
                filterTable(f.equals("All") ? "" : f);
            });
            filterGroup.add(pill);
            pills.add(pill);
        }

        controls.add(searchRow, BorderLayout.NORTH);
        controls.add(pills,     BorderLayout.SOUTH);
        card.add(controls, BorderLayout.CENTER);

        // Table
        String[] cols = {"#", "User", "Type", "Date & Time", "Duration", "Participants", "Status", "Admin Actions"};
        tableModel = new DefaultTableModel(allData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        table = new JTable(tableModel);
        Components.styleTable(table);
        table.setRowHeight(46);
        JTableHeader th = table.getTableHeader();
        th.setBackground(Theme.ACCENT);
        th.setForeground(Theme.WHITE);
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(0, 38));
        table.getColumn("Status").setCellRenderer(new StatusRenderer());
        table.getColumn("Type").setCellRenderer(new TypeRenderer());
        table.getColumn("Admin Actions").setCellRenderer(new AdminActionRenderer());
        table.getColumn("Admin Actions").setCellEditor(new AdminActionEditor(new JCheckBox()));
        table.getColumn("#").setPreferredWidth(40);
        table.getColumn("Admin Actions").setPreferredWidth(160);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(Theme.BORDER, 1, true));
        sp.getViewport().setBackground(Theme.CARD);
        sp.setPreferredSize(new Dimension(0, 460));
        card.add(sp, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel note = new JLabel("  ⚠  Only administrators can modify or cancel any reservation. Past appointments are read-only.");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.MUTED);
        wrap.add(card, BorderLayout.CENTER);
        wrap.add(note, BorderLayout.SOUTH);
        return wrap;
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW 2 — ADD APPOINTMENT
    // ══════════════════════════════════════════════════════════

    private JPanel buildAddAppointmentView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Add Appointment");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Components.subtitle("Pick a date and slot, fill in the details, then confirm.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        view.add(heading);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(20));

        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row.add(buildAddSlotsCard());
        row.add(buildAddFormCard());
        view.add(row);
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    private JPanel buildAddSlotsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("AVAILABLE SLOTS — pick a date");
        t.setFont(Theme.FONT_LABEL); t.setForeground(Theme.MUTED);
        JLabel ch = new JLabel("Change date ›");
        ch.setFont(Theme.FONT_SMALL); ch.setForeground(Theme.ACCENT);
        ch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ch.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showAddDatePicker(t); }
            @Override public void mouseEntered(MouseEvent e) { ch.setForeground(Theme.ACCENT2); }
            @Override public void mouseExited(MouseEvent e)  { ch.setForeground(Theme.ACCENT); }
        });
        header.add(t, BorderLayout.WEST); header.add(ch, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        String[] times = {"09:00","09:30","10:00","10:30","11:00","11:30",
                "12:00","12:30","13:00","13:30","14:00","14:30"};
        addSlotGrid.setOpaque(false);
        addSlotBtns.clear();
        for (String time : times) {
            JToggleButton slot = new JToggleButton(time);
            slot.setFont(Theme.FONT_BUTTON);
            slot.setFocusPainted(false);
            slot.setEnabled(true);
            slot.setBackground(Theme.PAPER);
            slot.setForeground(Theme.INK);
            slot.setBorder(new LineBorder(Theme.BORDER, 1, true));
            slot.setPreferredSize(new Dimension(0, 52));
            slot.addActionListener(e -> {
                for (JToggleButton s : addSlotBtns)
                    if (!s.isSelected()) { s.setBackground(Theme.PAPER); s.setForeground(Theme.INK); }
                slot.setBackground(Theme.ACCENT);
                slot.setForeground(Theme.WHITE);
            });
            addSlotGroup.add(slot);
            addSlotBtns.add(slot);
            addSlotGrid.add(slot);
        }
        card.add(addSlotGrid, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.add(addLegendDot(Theme.PAPER,  "Available"));
        legend.add(addLegendDot(Theme.CREAM,  "Booked"));
        legend.add(addLegendDot(Theme.ACCENT, "Selected"));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildAddFormCard() {
        JPanel card = Components.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("APPOINTMENT DETAILS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // Username field
        addUserField = Components.textField("Enter username");
        addUserField.setAlignmentX(LEFT_ALIGNMENT);
        addUserField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Type
        addTypeBox = Components.comboBox(new String[]{
                "— Appointment Type —", "Urgent", "Follow-up", "Assessment",
                "Virtual", "In-person", "Individual", "Group"
        });
        addTypeBox.setAlignmentX(LEFT_ALIGNMENT);
        addTypeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Duration toggle buttons
        addDur30 = new JToggleButton("30 min");
        addDur60 = new JToggleButton("60 min");
        new ButtonGroup() {{ add(addDur30); add(addDur60); }};
        for (JToggleButton db : new JToggleButton[]{addDur30, addDur60}) {
            db.setFont(new Font("SansSerif", Font.BOLD, 13));
            db.setFocusPainted(false);
            db.setBackground(Theme.PAPER);
            db.setForeground(Theme.INK);
            db.setBorder(new LineBorder(Theme.BORDER, 1, true));
            db.setPreferredSize(new Dimension(0, 60));
            db.addActionListener(e -> {
                addDur30.setBackground(addDur30.isSelected() ? Theme.ACCENT : Theme.PAPER);
                addDur30.setForeground(addDur30.isSelected() ? Theme.WHITE  : Theme.INK);
                addDur60.setBackground(addDur60.isSelected() ? Theme.ACCENT : Theme.PAPER);
                addDur60.setForeground(addDur60.isSelected() ? Theme.WHITE  : Theme.INK);
            });
        }
        JPanel durRow = new JPanel(new GridLayout(1, 2, 8, 0));
        durRow.setOpaque(false);
        durRow.setAlignmentX(LEFT_ALIGNMENT);
        durRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        durRow.add(addDur30); durRow.add(addDur60);

        JButton confirmBtn = Components.primaryBtn("Confirm Booking  →");
        confirmBtn.setAlignmentX(LEFT_ALIGNMENT);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBtn.addActionListener(e -> handleAddBooking());

        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(mkAddLabel("Username"));
        card.add(Box.createVerticalStrut(5));
        card.add(addUserField);
        card.add(Box.createVerticalStrut(16));
        card.add(mkAddLabel("Appointment Type"));
        card.add(Box.createVerticalStrut(5));
        card.add(addTypeBox);
        card.add(Box.createVerticalStrut(16));
        card.add(mkAddLabel("Duration"));
        card.add(Box.createVerticalStrut(5));
        card.add(durRow);
        card.add(Box.createVerticalStrut(20));
        card.add(confirmBtn);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private void showAddDatePicker(JLabel slotHeader) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Pick a Date", true);
        dialog.setUndecorated(true);
        dialog.setSize(300, 320);
        dialog.setLocationRelativeTo(this);

        final LocalDate[] cursor = { LocalDate.now().withDayOfMonth(1) };

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(Theme.CARD);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Serif", Font.BOLD, 14));
        monthLabel.setForeground(Theme.INK);
        JButton prev  = Components.outlineBtn("‹");
        JButton next  = Components.outlineBtn("›");
        JButton close = Components.dangerBtn("✕");
        close.addActionListener(e -> dialog.dispose());
        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navRight.setOpaque(false);
        navRight.add(next); navRight.add(close);
        nav.add(prev, BorderLayout.WEST);
        nav.add(monthLabel, BorderLayout.CENTER);
        nav.add(navRight, BorderLayout.EAST);
        root.add(nav, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setOpaque(false);
        for (String d : new String[]{"Su","Mo","Tu","We","Th","Fr","Sa"}) {
            JLabel h = new JLabel(d, SwingConstants.CENTER);
            h.setFont(Theme.FONT_LABEL);
            h.setForeground(d.equals("Fr") || d.equals("Sa") ? Theme.MUTED : Theme.INK);
            grid.add(h);
        }
        root.add(grid, BorderLayout.CENTER);

        JLabel hint = new JLabel("Weekends & past dates are unavailable", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(Theme.MUTED);
        root.add(hint, BorderLayout.SOUTH);

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            while (grid.getComponentCount() > 7) grid.remove(grid.getComponentCount() - 1);
            String month = cursor[0].getMonth().toString();
            monthLabel.setText(month.charAt(0) + month.substring(1).toLowerCase() + " " + cursor[0].getYear());
            int firstDow = cursor[0].withDayOfMonth(1).getDayOfWeek().getValue() % 7;
            for (int i = 0; i < firstDow; i++) grid.add(new JLabel());
            LocalDate today = LocalDate.now();
            for (int d = 1; d <= cursor[0].lengthOfMonth(); d++) {
                LocalDate date = cursor[0].withDayOfMonth(d);
                DayOfWeek dow  = date.getDayOfWeek();
                boolean unavail = dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY || !date.isAfter(today);
                JButton btn = new JButton(String.valueOf(d));
                btn.setFont(Theme.FONT_SMALL);
                btn.setFocusPainted(false);
                btn.setEnabled(!unavail);
                btn.setBackground(unavail ? Theme.CREAM : Theme.PAPER);
                btn.setForeground(unavail ? Theme.MUTED : Theme.INK);
                btn.setBorder(new LineBorder(Theme.BORDER, 1, true));
                btn.addActionListener(e -> {
                    String m = date.getMonth().toString();
                    slotHeader.setText("AVAILABLE SLOTS — "
                            + (m.charAt(0) + m.substring(1).toLowerCase().substring(0, 2))
                            + " " + date.getDayOfMonth());
                    dialog.dispose();
                });
                grid.add(btn);
            }
            grid.revalidate(); grid.repaint();
        };

        prev.addActionListener(e -> { cursor[0] = cursor[0].minusMonths(1); render[0].run(); });
        next.addActionListener(e -> { cursor[0] = cursor[0].plusMonths(1);  render[0].run(); });
        render[0].run();

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel addLegendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■"); dot.setForeground(color); dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JLabel txt = new JLabel(label); txt.setFont(Theme.FONT_SMALL); txt.setForeground(Theme.MUTED);
        p.add(dot); p.add(txt);
        return p;
    }

    private JLabel mkAddLabel(String text) {
        JLabel l = Components.sectionLabel(text);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void handleAddBooking() {
        String user = addUserField.getText().trim();
        String type = (String) addTypeBox.getSelectedItem();
        boolean slotSelected = addSlotBtns.stream().anyMatch(JToggleButton::isSelected);
        boolean durSelected  = addDur30.isSelected() || addDur60.isSelected();

        if (user.isEmpty() || type.startsWith("—") || !slotSelected || !durSelected) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields before confirming.",
                    "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String slot = addSlotBtns.stream().filter(JToggleButton::isSelected).findFirst().get().getText();
        String dur  = addDur30.isSelected() ? "30 min" : "60 min";

        JOptionPane.showMessageDialog(this,
                "Appointment booked successfully!\n\n"
                        + "User:     " + user + "\n"
                        + "Type:     " + type + "\n"
                        + "Slot:     " + slot + "\n"
                        + "Duration: " + dur,
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

        // reset
        addUserField.setText("");
        addTypeBox.setSelectedIndex(0);
        addSlotGroup.clearSelection();
        for (JToggleButton s : addSlotBtns) { s.setBackground(Theme.PAPER); s.setForeground(Theme.INK); }
        new ButtonGroup() {{ add(addDur30); add(addDur60); }};
        addDur30.setSelected(false); addDur30.setBackground(Theme.PAPER); addDur30.setForeground(Theme.INK);
        addDur60.setSelected(false); addDur60.setBackground(Theme.PAPER); addDur60.setForeground(Theme.INK);

        switchTo(CARD_RESERVATIONS);
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
        mark.setFont(Theme.FONT_SMALL);
        mark.setForeground(Theme.ACCENT);
        mark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerRow.add(mark, BorderLayout.EAST);

        JLabel sub = Components.subtitle("System alerts, booking updates, and admin notifications.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        adminNotifsList = new JPanel(new GridLayout(0, 1, 0, 10));
        adminNotifsList.setOpaque(false);
        JPanel list = adminNotifsList;

        mark.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                list.removeAll(); list.revalidate(); list.repaint();
            }
        });

        rebuildAdminNotifs();

        view.add(headerRow);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(20));
        view.add(list);
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    private void rebuildAdminNotifs() {
        adminNotifsList.removeAll();
        Object[][] notifs = {
                {"⚠",  "Capacity Warning",     "Group – Assessment on Mar 22 has reached max participants (4).", "Today · 09:15",     Theme.ACCENT},
                {"✅", "Booking Confirmed",     "Reservation #006 for Reem Haddad was confirmed successfully.",  "Today · 08:40",     Theme.SUCCESS},
                {"🔔", "Upcoming Appointments", "4 appointments scheduled for tomorrow.",                         "Yesterday · 17:00", Theme.WARNING},
                {"ℹ",  "Slot Freed",            "Reservation #003 cancelled. Slot Mar 15 · 14:00 is now free.",  "Yesterday · 13:22", Theme.ACCENT2}
        };
        for (Object[] n : notifs)
            adminNotifsList.add(buildAdminNotifRow((String)n[0], (String)n[1], (String)n[2], (String)n[3], (Color)n[4]));
        adminNotifsList.revalidate();
        adminNotifsList.repaint();
    }

    private JPanel buildAdminNotifRow(String icon, String title, String body, String time, Color accent) {
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

        JButton x = new JButton("✖");
        x.setFont(new Font("SansSerif", Font.PLAIN, 20));
        x.setForeground(Color.RED);
        x.setBackground(Theme.PAPER);
        x.setBorderPainted(false);
        x.setFocusPainted(false);
        x.setContentAreaFilled(false);
        x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        x.addActionListener(e -> {
            Container parent = row.getParent();
            parent.remove(row);
            parent.revalidate();
            parent.repaint();
        });

        row.add(ico,  BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(x,    BorderLayout.EAST);
        return row;
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW 4 — REPORTS (placeholder)
    // ══════════════════════════════════════════════════════════

    private JPanel buildReportsView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Reports");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Components.subtitle("Appointment statistics and summaries will be available here.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        view.add(heading);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(24));

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        stats.add(Components.statCard("42",  "Total appointments this month", Theme.ACCENT2));
        stats.add(Components.statCard("87%", "Confirmed rate",                Theme.SUCCESS));
        stats.add(Components.statCard("3",   "Cancellations this week",       Theme.ACCENT));
        view.add(stats);
        view.add(Box.createVerticalStrut(20));

        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel cardTitle = new JLabel("DETAILED REPORTS");
        cardTitle.setFont(Theme.FONT_LABEL);
        cardTitle.setForeground(Theme.MUTED);
        card.add(cardTitle, BorderLayout.NORTH);

        JLabel placeholder = new JLabel(
                "<html><div style='text-align:center;padding:40px'>"
                        + "<div style='font-size:32px'>📊</div><br><br>"
                        + "<b>Coming soon</b><br><br>"
                        + "Charts, export options, and detailed breakdowns<br>"
                        + "by appointment type, user, and time period<br>"
                        + "will be designed after project completion."
                        + "</div></html>"
        );
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        placeholder.setFont(Theme.FONT_BODY);
        placeholder.setForeground(Theme.MUTED);
        card.add(placeholder, BorderLayout.CENTER);

        view.add(card);
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    // ── Form helpers ──────────────────────────────────────────

    /**
     * Builds a labelled single-field form row (label above, field below).
     * @param labelText uppercase field label
     * @param field     the input component
     * @return assembled JPanel
     */
    private JPanel mkFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = Components.sectionLabel(labelText);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.add(lbl);
        row.add(Box.createVerticalStrut(5));
        row.add(field);
        return row;
    }

    /**
     * Builds a two-field form row with each field taking half the width.
     * @param label1 label for the left field
     * @param field1 left input component
     * @param label2 label for the right field
     * @param field2 right input component
     * @return assembled JPanel
     */
    private JPanel mkFormRowTwo(String label1, JComponent field1,
                                String label2, JComponent field2) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.add(mkFormRow(label1, field1));
        row.add(mkFormRow(label2, field2));
        return row;
    }

    // ── Table helpers ─────────────────────────────────────────

    /**
     * Filters the reservations table to rows containing the query string.
     * @param query text to search across all columns (case-insensitive)
     */
    private void filterTable(String query) {
        tableModel.setRowCount(0);
        String q = query.toLowerCase();
        for (Object[] row : allData) {
            boolean match = q.isEmpty();
            for (Object cell : row)
                if (cell.toString().toLowerCase().contains(q)) { match = true; break; }
            if (match) tableModel.addRow(row);
        }
    }

    // ── Widget helpers ────────────────────────────────────────

    private JButton mkSideBtn(String icon, String label) {
        JButton b = Components.sidebarItem(icon, label);
        b.setBackground(ADMIN_BG);
        return b;
    }

    private JToggleButton mkPillBtn(String text) {
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
                case "In-person": tag = Components.tag("In-person", new Color(0xE3, 0xF0, 0xFA), Theme.ACCENT2); break;
                case "Follow-up": tag = Components.tag("Follow-up", new Color(0xFA, 0xEE, 0xE3), Theme.WARNING);  break;
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
            p.add(edit);
            p.add(can);
            return p;
        }
    }
}