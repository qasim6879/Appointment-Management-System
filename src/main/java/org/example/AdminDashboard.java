package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

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

    private JButton btnReservations;
    private JButton btnAdd;
    private JButton btnNotifs;
    private JButton btnReports;

    // Add-appointment form fields — kept as fields so resetAddForm() can clear them
    private JTextField        addUserField;
    private JComboBox<String> addTypeBox;
    private JTextField        addDateField;
    private JComboBox<String> addTimeBox;
    private JComboBox<String> addDurationBox;
    private JComboBox<String> addParticipantsBox;
    private JTextArea         addNotesArea;

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
        btnNotifs.addActionListener(e       -> switchTo(CARD_NOTIFS));
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
        JScrollPane sp = new JScrollPane(content);
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

        JLabel sub = Components.subtitle("Manually book an appointment on behalf of any user.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        view.add(heading);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(20));

        // Form card
        JPanel card = Components.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(680, Integer.MAX_VALUE));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // User
        card.add(mkFormRow("USER / PATIENT NAME",
            addUserField = Components.textField("e.g. Jana Doe")));
        card.add(Box.createVerticalStrut(14));

        // Type
        addTypeBox = Components.comboBox(new String[]{
            "— Appointment Type —", "Urgent", "Follow-up", "Assessment",
            "Virtual", "In-person", "Individual", "Group"
        });
        card.add(mkFormRow("APPOINTMENT TYPE", addTypeBox));
        card.add(Box.createVerticalStrut(14));

        // Date + Time
        addDateField = Components.textField("e.g. Mar 14");
        addTimeBox   = Components.comboBox(new String[]{
            "— Time Slot —",
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30"
        });
        card.add(mkFormRowTwo("DATE", addDateField, "TIME SLOT", addTimeBox));
        card.add(Box.createVerticalStrut(14));

        // Duration + Participants
        addDurationBox = Components.comboBox(new String[]{
            "— Duration —", "30 min (½ hr)", "60 min (1 hr)"
        });
        addParticipantsBox = Components.comboBox(new String[]{
            "— Participants —", "1", "2", "3", "4 (max)"
        });
        card.add(mkFormRowTwo("DURATION", addDurationBox, "PARTICIPANTS", addParticipantsBox));
        card.add(Box.createVerticalStrut(14));

        // Rule hint
        JLabel ruleHint = new JLabel("⚠  Max duration: 1 hr · Max participants vary by appointment type");
        ruleHint.setFont(Theme.FONT_SMALL);
        ruleHint.setForeground(Theme.MUTED);
        ruleHint.setBackground(Theme.CREAM);
        ruleHint.setOpaque(true);
        ruleHint.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        ruleHint.setAlignmentX(LEFT_ALIGNMENT);
        ruleHint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(ruleHint);
        card.add(Box.createVerticalStrut(14));

        // Notes
        JLabel notesLabel = Components.sectionLabel("Notes (optional)");
        notesLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(notesLabel);
        card.add(Box.createVerticalStrut(5));

        addNotesArea = new JTextArea(3, 0);
        addNotesArea.setFont(Theme.FONT_BODY);
        addNotesArea.setForeground(Theme.INK);
        addNotesArea.setBackground(Theme.PAPER);
        addNotesArea.setLineWrap(true);
        addNotesArea.setWrapStyleWord(true);
        addNotesArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane notesSp = new JScrollPane(addNotesArea);
        notesSp.setBorder(null);
        notesSp.setAlignmentX(LEFT_ALIGNMENT);
        notesSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.add(notesSp);
        card.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton confirmBtn = Components.primaryBtn("Confirm Booking  →");
        confirmBtn.addActionListener(e -> handleAddBooking());
        JButton resetBtn = Components.outlineBtn("Reset Form");
        resetBtn.addActionListener(e -> resetAddForm());
        btnRow.add(confirmBtn);
        btnRow.add(resetBtn);
        card.add(btnRow);

        view.add(card);
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    /**
     * Validates the add-appointment form and shows a confirmation summary.
     * Wire the collected field values to your AppointmentService in the service layer.
     */
    private void handleAddBooking() {
        String user = addUserField.getText().trim();
        String type = (String) addTypeBox.getSelectedItem();
        String date = addDateField.getText().trim();
        String time = (String) addTimeBox.getSelectedItem();
        String dur  = (String) addDurationBox.getSelectedItem();
        String part = (String) addParticipantsBox.getSelectedItem();

        if (user.isEmpty() || type.startsWith("—") || date.isEmpty()
                || time.startsWith("—") || dur.startsWith("—") || part.startsWith("—")) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all required fields before confirming.",
                "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Appointment booked successfully!\n\n"
            + "User:         " + user + "\n"
            + "Type:         " + type + "\n"
            + "Date:         " + date + "  " + time + "\n"
            + "Duration:     " + dur  + "\n"
            + "Participants: " + part,
            "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

        resetAddForm();
        switchTo(CARD_RESERVATIONS);
    }

    /** Clears all add-appointment form fields back to their defaults. */
    private void resetAddForm() {
        addUserField.setText("");
        addTypeBox.setSelectedIndex(0);
        addDateField.setText("");
        addTimeBox.setSelectedIndex(0);
        addDurationBox.setSelectedIndex(0);
        addParticipantsBox.setSelectedIndex(0);
        addNotesArea.setText("");
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

        view.add(headerRow);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(20));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        list.add(Components.notifRow("⚠", "Capacity Warning",
            "Group – Assessment on Mar 22 has reached max participants (4).",
            "Today · 09:15", Theme.ACCENT));
        list.add(Box.createVerticalStrut(8));
        list.add(Components.notifRow("✅", "Booking Confirmed",
            "Reservation #006 for Reem Haddad was confirmed successfully.",
            "Today · 08:40", Theme.SUCCESS));
        list.add(Box.createVerticalStrut(8));
        list.add(Components.notifRow("🔔", "Upcoming Appointments",
            "4 appointments scheduled for tomorrow (Mar 14).",
            "Yesterday · 17:00", Theme.WARNING));
        list.add(Box.createVerticalStrut(8));
        list.add(Components.notifRow("ℹ", "Slot Freed",
            "Reservation #003 was cancelled by user. Slot Mar 15 · 14:00 is now free.",
            "Yesterday · 13:22", Theme.ACCENT2));

        view.add(list);
        view.add(Box.createVerticalStrut(24));
        return view;
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
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        stats.add(Components.statCard("42",  "Total appointments this month", Theme.ACCENT2));
        stats.add(Components.statCard("87%", "Confirmed rate",                Theme.SUCCESS));
        stats.add(Components.statCard("3",   "Cancellations this week",       Theme.ACCENT));
        view.add(stats);
        view.add(Box.createVerticalStrut(20));

        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 12));
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
