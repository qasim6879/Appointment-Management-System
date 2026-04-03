package org.example;
import java.util.Collections;
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

    private static final Color ADMIN_BG    = new Color(0x1A, 0x0E, 0x0A);
    private static final Color SLOT_BOOKED = Theme.CREAM;
    private static final Color SLOT_AVAIL  = Theme.PAPER;
    private static final Color SLOT_SEL    = Theme.ACCENT;

    private final String         adminName;
    private final LogoutListener logoutListener;

    // Table fields
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

    private JPanel statsPanel;
    // Add-appointment form fields
    private JTextField          addUserField;
    private JComboBox<String>   addTypeBox;
    private JPanel              addSlotGrid   = new JPanel(new GridLayout(3, 4, 8, 8));
    private List<JToggleButton> addSlotBtns   = new ArrayList<>();
    private ButtonGroup         addSlotGroup  = new ButtonGroup();
    private JToggleButton       addDur30;
    private JToggleButton       addDur60;

    // Live data & booking state
    private Administrator        adminObj;
    private final List<Object[]>    liveData  = new ArrayList<>();
    private final List<Appointment> liveAppts = new ArrayList<>();
    private LocalDate            pickedAddDate      = null;
    private int                  pickedAddDuration  = 30;

    /**
     * @param adminName      admin's display name
     * @param logoutListener callback for logout
     */
    public AdminDashboard(String adminName, LogoutListener logoutListener) {
        this.adminName      = adminName;
        this.logoutListener = logoutListener;
        this.adminObj       = Administrator.getAdministratorObject(adminName);
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

        loadTableData();
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

        btnReservations.addActionListener(e -> { loadTableData(); switchTo(CARD_RESERVATIONS); });
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

    private void switchTo(String card) {
        contentCards.show(contentPanel, card);
        switch (card) {
            case CARD_RESERVATIONS: setActive(btnReservations); break;
            case CARD_ADD:          setActive(btnAdd);          break;
            case CARD_NOTIFS:       setActive(btnNotifs);       break;
            case CARD_REPORTS:      setActive(btnReports);      break;
        }
    }

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
        statsPanel = buildStats();
        view.add(statsPanel);
        view.add(Box.createVerticalStrut(20));
        view.add(buildReservationsCard());
        view.add(Box.createVerticalStrut(24));
        return view;
    }

    private JPanel buildStats() {
        int total = liveAppts.size();

        int completed = 0;
        int todayReminders = 0;
        int cancelled = 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        for (Appointment a : liveAppts) {
            LocalDateTime apptTime = LocalDateTime.of(a.getDate(), a.getStartTime());
            String status = a.getStatus().toString().toUpperCase();

            // Completed
            if (apptTime.isBefore(now) && status.equals("CONFIRMED"))
                completed++;

            // Today reminders
            if (a.getDate().equals(today) &&
                    apptTime.isAfter(now) &&
                    !status.equals("CANCELLED"))
                todayReminders++;

            // Cancelled
            if (status.equals("CANCELLED"))
                cancelled++;
        }

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        row.add(Components.statCard(String.valueOf(total), "Total", Theme.ACCENT2));
        row.add(Components.statCard(String.valueOf(completed), "Completed", Theme.SUCCESS));
        row.add(Components.statCard(String.valueOf(todayReminders), "Today Reminders", Theme.ACCENT));
        row.add(Components.statCard(String.valueOf(cancelled), "Cancelled Appointments", Theme.WARNING));

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
        tableModel = new DefaultTableModel(new Object[0][], cols) {
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
            slot.setEnabled(false);
            slot.setBackground(SLOT_BOOKED);
            slot.setForeground(Theme.MUTED);
            slot.setBorder(new LineBorder(Theme.BORDER, 1, true));
            slot.setPreferredSize(new Dimension(0, 52));
            slot.addActionListener(e -> {
                for (JToggleButton s : addSlotBtns)
                    if (!s.isSelected()) { s.setBackground(SLOT_AVAIL); s.setForeground(Theme.INK); }
                slot.setBackground(SLOT_SEL);
                slot.setForeground(Theme.WHITE);
            });
            addSlotGroup.add(slot);
            addSlotBtns.add(slot);
            addSlotGrid.add(slot);
        }
        card.add(addSlotGrid, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.add(addLegendDot(SLOT_AVAIL,  "Available"));
        legend.add(addLegendDot(SLOT_BOOKED, "Booked"));
        legend.add(addLegendDot(SLOT_SEL,    "Selected"));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildAddFormCard() {
        JPanel card = Components.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("APPOINTMENT DETAILS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        addUserField = Components.textField("Enter username");
        addUserField.setAlignmentX(LEFT_ALIGNMENT);
        addUserField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        addTypeBox = Components.comboBox(new String[]{
                "— Appointment Type —", "Urgent", "Follow-up", "Assessment",
                "Virtual", "In-person", "Individual", "Group"
        });
        addTypeBox.setAlignmentX(LEFT_ALIGNMENT);
        addTypeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        addDur30 = new JToggleButton("30 min");
        addDur60 = new JToggleButton("60 min");
        new ButtonGroup() {{ add(addDur30); add(addDur60); }};
        for (JToggleButton db : new JToggleButton[]{addDur30, addDur60}) {
            db.setFont(new Font("SansSerif", Font.BOLD, 13));
            db.setFocusPainted(false);
            db.setBackground(SLOT_AVAIL);
            db.setForeground(Theme.INK);
            db.setBorder(new LineBorder(Theme.BORDER, 1, true));
            db.setPreferredSize(new Dimension(0, 60));
            db.addActionListener(e -> {
                addDur30.setBackground(addDur30.isSelected() ? SLOT_SEL : SLOT_AVAIL);
                addDur30.setForeground(addDur30.isSelected() ? Theme.WHITE : Theme.INK);
                addDur60.setBackground(addDur60.isSelected() ? SLOT_SEL : SLOT_AVAIL);
                addDur60.setForeground(addDur60.isSelected() ? Theme.WHITE : Theme.INK);
                pickedAddDuration = addDur30.isSelected() ? 30 : 60;
                refreshAddSlots();
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
                boolean unavail = dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY
                        || !date.isAfter(today)
                        || date.isAfter(today.plusDays(30));
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
                    pickedAddDate = date;
                    refreshAddSlots();
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

    private void refreshAddSlots() {
        addSlotGroup.clearSelection();
        for (JToggleButton slot : addSlotBtns) {
            slot.setEnabled(false);
            slot.setBackground(SLOT_BOOKED);
            slot.setForeground(Theme.MUTED);
        }
        if (pickedAddDate == null)
            return;
        boolean[] available = Appointment.availableTimeSlots(pickedAddDate, adminName, pickedAddDuration);
        for (int i = 0; i < addSlotBtns.size(); i++) {
            addSlotBtns.get(i).setEnabled(available[i]);
            addSlotBtns.get(i).setBackground(available[i] ? SLOT_AVAIL : SLOT_BOOKED);
            addSlotBtns.get(i).setForeground(available[i] ? Theme.INK : Theme.MUTED);
        }
    }
    
    private void showEditDialog(Appointment appt) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Edit Appointment", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        // ── State ──────────────────────────────────────────────
        final LocalDate[] editDate = { appt.getDate() };
        final int[] editDuration = { appt.getDuration() };

        // ── Root panel ─────────────────────────────────────────
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Theme.CARD);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Title
        JLabel titleLbl = new JLabel("EDIT APPOINTMENT");
        titleLbl.setFont(Theme.FONT_LABEL);
        titleLbl.setForeground(Theme.MUTED);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel userLbl = new JLabel("User: " + appt.getUser().getUsername());
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLbl.setForeground(Theme.INK);
        userLbl.setAlignmentX(LEFT_ALIGNMENT);

        // ── Date picker row ────────────────────────────────────
        String initMonth = appt.getDate().getMonth().toString();
        String initMon = initMonth.charAt(0) + initMonth.substring(1, 3).toLowerCase();
        JLabel dateLbl = new JLabel("Date:  " + initMon + " " + appt.getDate().getDayOfMonth());
        dateLbl.setFont(Theme.FONT_BODY);
        dateLbl.setForeground(Theme.INK);

        JLabel changeDateLink = new JLabel("Change ›");
        changeDateLink.setFont(Theme.FONT_SMALL);
        changeDateLink.setForeground(Theme.ACCENT);
        changeDateLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setOpaque(false);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        dateRow.setAlignmentX(LEFT_ALIGNMENT);
        dateRow.add(dateLbl, BorderLayout.WEST);
        dateRow.add(changeDateLink, BorderLayout.EAST);

        // ── Type combo ─────────────────────────────────────────
        String[] typeOptions = { "Urgent", "Follow-up", "Assessment", "Virtual", "In-person", "Individual", "Group" };
        JComboBox<String> typeBox = Components.comboBox(typeOptions);
        typeBox.setAlignmentX(LEFT_ALIGNMENT);
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        typeBox.setSelectedItem(formatType(appt.getType()));

        // ── Duration combo ─────────────────────────────────────
        JComboBox<String> durBox = Components.comboBox(new String[] { "30 min", "60 min" });
        durBox.setAlignmentX(LEFT_ALIGNMENT);
        durBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        durBox.setSelectedItem(appt.getDuration() + " min");

        // ── Time combo (dynamic) ───────────────────────────────
        JComboBox<String> timeBox = Components.comboBox(new String[] { "— pick date first —" });
        timeBox.setAlignmentX(LEFT_ALIGNMENT);
        timeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Helper: refresh time combo based on current editDate + editDuration
        Runnable refreshTimes = () -> {
            String currentSel = timeBox.getSelectedItem() != null ? timeBox.getSelectedItem().toString() : "";
            timeBox.removeAllItems();
            String[] slots = { "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                    "12:00", "12:30", "13:00", "13:30", "14:00", "14:30" };
            boolean[] available = Appointment.availableTimeSlots(editDate[0], adminName, editDuration[0]);
            // Always include the appointment's own current slot so it's selectable
            String currentApptTime = String.format("%02d:%02d",
                    appt.getStartTime().getHour(), appt.getStartTime().getMinute());
            boolean anyAdded = false;
            for (int i = 0; i < slots.length; i++) {
                if (available[i] || slots[i].equals(currentApptTime)) {
                    timeBox.addItem(slots[i]);
                    anyAdded = true;
                }
            }
            if (!anyAdded)
                timeBox.addItem("— no slots available —");
            // Restore previous selection if still available, else default to current appt time
            if (timeBox.getItemCount() > 0) {
                boolean restored = false;
                for (int i = 0; i < timeBox.getItemCount(); i++) {
                    if (timeBox.getItemAt(i).equals(currentSel)) {
                        timeBox.setSelectedIndex(i);
                        restored = true;
                        break;
                    }
                }
                if (!restored) {
                    for (int i = 0; i < timeBox.getItemCount(); i++) {
                        if (timeBox.getItemAt(i).equals(currentApptTime)) {
                            timeBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        };

        refreshTimes.run();

        // Wire duration changes → refresh times
        durBox.addActionListener(e -> {
            String sel = (String) durBox.getSelectedItem();
            editDuration[0] = sel != null && sel.startsWith("60") ? 60 : 30;
            refreshTimes.run();
        });

        // Wire date change link → calendar picker → refresh times
        changeDateLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JDialog cal = new JDialog(dialog, "Pick a Date", true);
                cal.setUndecorated(true);
                cal.setSize(300, 320);
                cal.setLocationRelativeTo(dialog);

                final LocalDate[] cursor = { editDate[0].withDayOfMonth(1) };

                JPanel calRoot = new JPanel(new BorderLayout(0, 8));
                calRoot.setBackground(Theme.CARD);
                calRoot.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER, 1),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)));

                JPanel nav = new JPanel(new BorderLayout());
                nav.setOpaque(false);
                JLabel monthLbl = new JLabel("", SwingConstants.CENTER);
                monthLbl.setFont(new Font("Serif", Font.BOLD, 14));
                monthLbl.setForeground(Theme.INK);
                JButton prev = Components.outlineBtn("‹");
                JButton next = Components.outlineBtn("›");
                JButton close = Components.dangerBtn("✕");
                close.addActionListener(ev -> cal.dispose());
                JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                navRight.setOpaque(false);
                navRight.add(next);
                navRight.add(close);
                nav.add(prev, BorderLayout.WEST);
                nav.add(monthLbl, BorderLayout.CENTER);
                nav.add(navRight, BorderLayout.EAST);
                calRoot.add(nav, BorderLayout.NORTH);

                JPanel calGrid = new JPanel(new GridLayout(0, 7, 4, 4));
                calGrid.setOpaque(false);
                for (String d : new String[] { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" }) {
                    JLabel h = new JLabel(d, SwingConstants.CENTER);
                    h.setFont(Theme.FONT_LABEL);
                    h.setForeground(d.equals("Fr") || d.equals("Sa") ? Theme.MUTED : Theme.INK);
                    calGrid.add(h);
                }
                calRoot.add(calGrid, BorderLayout.CENTER);

                JLabel hint = new JLabel("Weekends & past dates are unavailable", SwingConstants.CENTER);
                hint.setFont(Theme.FONT_SMALL);
                hint.setForeground(Theme.MUTED);
                calRoot.add(hint, BorderLayout.SOUTH);

                Runnable[] renderCal = new Runnable[1];
                renderCal[0] = () -> {
                    while (calGrid.getComponentCount() > 7)
                        calGrid.remove(calGrid.getComponentCount() - 1);
                    String mo = cursor[0].getMonth().toString();
                    monthLbl.setText(mo.charAt(0) + mo.substring(1).toLowerCase() + " " + cursor[0].getYear());
                    int firstDow = cursor[0].withDayOfMonth(1).getDayOfWeek().getValue() % 7;
                    for (int i = 0; i < firstDow; i++)
                        calGrid.add(new JLabel());
                    LocalDate today = LocalDate.now();
                    for (int dd = 1; dd <= cursor[0].lengthOfMonth(); dd++) {
                        LocalDate date = cursor[0].withDayOfMonth(dd);
                        DayOfWeek dow = date.getDayOfWeek();
                        boolean unavail = dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY
                                || !date.isAfter(today) || date.isAfter(today.plusDays(30));
                        JButton btn = new JButton(String.valueOf(dd));
                        btn.setFont(Theme.FONT_SMALL);
                        btn.setFocusPainted(false);
                        btn.setEnabled(!unavail);
                        btn.setBackground(unavail ? Theme.CREAM : Theme.PAPER);
                        btn.setForeground(unavail ? Theme.MUTED : Theme.INK);
                        btn.setBorder(new LineBorder(Theme.BORDER, 1, true));
                        btn.addActionListener(ev -> {
                            editDate[0] = date;
                            String m = date.getMonth().toString();
                            dateLbl.setText("Date:  "
                                    + (m.charAt(0) + m.substring(1, 3).toLowerCase())
                                    + " " + date.getDayOfMonth());
                            refreshTimes.run();
                            cal.dispose();
                        });
                        calGrid.add(btn);
                    }
                    calGrid.revalidate();
                    calGrid.repaint();
                };

                prev.addActionListener(ev -> {
                    cursor[0] = cursor[0].minusMonths(1);
                    renderCal[0].run();
                });
                next.addActionListener(ev -> {
                    cursor[0] = cursor[0].plusMonths(1);
                    renderCal[0].run();
                });
                renderCal[0].run();

                cal.setContentPane(calRoot);
                cal.setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                changeDateLink.setForeground(Theme.ACCENT2);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeDateLink.setForeground(Theme.ACCENT);
            }
        });

        // ── Confirm button ─────────────────────────────────────
        JButton confirmBtn = Components.primaryBtn("Save Changes  →");
        confirmBtn.setAlignmentX(LEFT_ALIGNMENT);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBtn.addActionListener(e -> {
            String selTime = (String) timeBox.getSelectedItem();
            if (selTime == null || selTime.startsWith("—")) {
                JOptionPane.showMessageDialog(dialog, "Please select a valid time slot.", "Missing Field", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalTime newTime = LocalTime.parse(selTime);
            int newDur = editDuration[0];
            AppointmentType newTp = AppointmentType.valueOf(typeBox.getSelectedItem().toString().toUpperCase().replace("-", "_").replace(" ", "_"));
            
            adminObj.editAppointment(appt, editDate[0], newTime, newDur, newTp);
            dialog.dispose();
            loadTableData();
        });

        // ── Cancel button ──────────────────────────────────────
        JButton cancelBtn = Components.dangerBtn("Discard");
        cancelBtn.setAlignmentX(LEFT_ALIGNMENT);
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        // ── Assemble ───────────────────────────────────────────
        root.add(titleLbl);
        root.add(Box.createVerticalStrut(4));
        root.add(userLbl);
        root.add(Box.createVerticalStrut(20));
        root.add(mkEditLabel("Date"));
        root.add(Box.createVerticalStrut(5));
        root.add(dateRow);
        root.add(Box.createVerticalStrut(16));
        root.add(mkEditLabel("Appointment Type"));
        root.add(Box.createVerticalStrut(5));
        root.add(typeBox);
        root.add(Box.createVerticalStrut(16));
        root.add(mkEditLabel("Duration"));
        root.add(Box.createVerticalStrut(5));
        root.add(durBox);
        root.add(Box.createVerticalStrut(16));
        root.add(mkEditLabel("Start Time"));
        root.add(Box.createVerticalStrut(5));
        root.add(timeBox);
        root.add(Box.createVerticalStrut(24));
        root.add(btnRow);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void refreshStats() {
        if (statsPanel == null)
            return;

        statsPanel.removeAll();

        int total = liveAppts.size();
        int completed = 0;
        int todayReminders = 0;
        int cancelled = 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        for (Appointment a : liveAppts) {
            LocalDateTime apptTime = LocalDateTime.of(a.getDate(), a.getStartTime());
            String status = a.getStatus().toString().toUpperCase();

            if (apptTime.isBefore(now) && status.equals("CONFIRMED"))
                completed++;

            if (a.getDate().equals(today) &&
                    apptTime.isAfter(now) &&
                    !status.equals("CANCELLED"))
                todayReminders++;

            if (status.equals("CANCELLED"))
                cancelled++;
        }

        statsPanel.setLayout(new GridLayout(1, 4, 12, 0));
        statsPanel.add(Components.statCard(String.valueOf(total), "Total", Theme.ACCENT2));
        statsPanel.add(Components.statCard(String.valueOf(completed), "Completed", Theme.SUCCESS));
        statsPanel.add(Components.statCard(String.valueOf(todayReminders), "Today Reminders", Theme.ACCENT));
        statsPanel.add(Components.statCard(String.valueOf(cancelled), "Cancelled Appointments", Theme.WARNING));

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JLabel mkEditLabel(String text) {
        JLabel l = Components.sectionLabel(text);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
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
        String targetUser = addUserField.getText().trim();
        String type       = (String) addTypeBox.getSelectedItem();
        boolean slotSelected = addSlotBtns.stream().anyMatch(JToggleButton::isSelected);
        boolean durSelected  = addDur30.isSelected() || addDur60.isSelected();

        String missing = "";
        if (targetUser.isEmpty())  missing += "• Enter a username\n";
        if (pickedAddDate == null) missing += "• Pick a date\n";
        if (!durSelected)          missing += "• Choose a duration\n";
        if (type.startsWith("—")) missing += "• Choose appointment type\n";
        if (!slotSelected)         missing += "• Select a time slot\n";

        if (!missing.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete the following before confirming:\n\n" + missing,
                    "Incomplete Booking", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User targetUserObj = User.getUserObject(targetUser);
        if (targetUserObj == null) {
            JOptionPane.showMessageDialog(this,
                    "No user found with username: \"" + targetUser + "\".\nPlease check and try again.",
                    "User Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JToggleButton selected = addSlotBtns.stream().filter(JToggleButton::isSelected).findFirst().get();
        LocalTime startTime = LocalTime.parse(selected.getText());
        AppointmentType apptType = AppointmentType.valueOf(
                type.toUpperCase().replace("-", "_").replace(" ", "_")
        );

     // التعديل هنا: نجعل الإدمن هو من يقوم بفعل الحجز وليس اليوزر
        adminObj.bookAppointment(targetUser, pickedAddDate, startTime, pickedAddDuration, apptType);
        refreshAddSlots();

        JOptionPane.showMessageDialog(this,
                "Appointment booked successfully!\n\n"
                        + "User:     " + targetUser + "\n"
                        + "Type:     " + type + "\n"
                        + "Date:     " + pickedAddDate + "\n"
                        + "Slot:     " + selected.getText() + "\n"
                        + "Duration: " + pickedAddDuration + " min",
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

        // Reset form
        addUserField.setText("");
        addTypeBox.setSelectedIndex(0);
        addSlotGroup.clearSelection();
        pickedAddDate     = null;
        pickedAddDuration = 30;
        addDur30.setSelected(false); addDur30.setBackground(SLOT_AVAIL); addDur30.setForeground(Theme.INK);
        addDur60.setSelected(false); addDur60.setBackground(SLOT_AVAIL); addDur60.setForeground(Theme.INK);
        for (JToggleButton s : addSlotBtns) { s.setEnabled(false); s.setBackground(SLOT_BOOKED); s.setForeground(Theme.MUTED); }

        loadTableData();
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
                Notification.deleteAllNotifications(adminName); // حذف نهائي
                adminNotifsList.removeAll();
                adminNotifsList.revalidate();
                adminNotifsList.repaint();
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
        // جلب الإشعارات الحقيقية الخاصة بهذا الإدمن من ملف JSON
        List<Notification> realNotifs = Notification.getNotifications(adminName);

        if (realNotifs == null || realNotifs.isEmpty()) {
            JLabel empty = new JLabel("No new notifications.");
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            adminNotifsList.add(empty);
        } else {
            // ترتيب التنبيهات (الأحدث فوق)
            Collections.reverse(realNotifs);
            for (Notification n : realNotifs) {
            	if (n.isActive()) {
                    String icon = (n.getType() == NotificationType.CANCELLATION) ? "❌" : "🔔";
                    Color accent = (n.getType() == NotificationType.CONFIRMATION) ? Theme.SUCCESS : Theme.ACCENT;
                    
                    // التعديل هنا: نمرر n كأول باراميتر
                    adminNotifsList.add(buildAdminNotifRow(n, icon, accent)); 
            	}}
        }
        adminNotifsList.revalidate();
        adminNotifsList.repaint();
    }

 // تعديل دالة buildAdminNotifRow (Existing Function - Changed Parameters)
    private JPanel buildAdminNotifRow(Notification n, String icon, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Theme.PAPER);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createCompoundBorder(new LineBorder(Theme.BORDER, 1), BorderFactory.createEmptyBorder(10, 12, 10, 12))
        ));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel text = new JPanel(new GridLayout(3, 1, 0, 1));
        text.setOpaque(false);
        text.add(new JLabel(n.getType().toString()) {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        text.add(new JLabel(n.getMessage()) {{ setFont(Theme.FONT_BODY); }});
        text.add(new JLabel(n.getDateSent()) {{ setFont(Theme.FONT_SMALL); setForeground(Theme.MUTED); }});

        JButton x = new JButton("✖");
        x.setFont(new Font("SansSerif", Font.PLAIN, 20));
        x.setForeground(Color.RED);
        x.setBorderPainted(false);      // سطر جديد: لإزالة الإطار (المربع)
        x.setFocusPainted(false);       // سطر جديد: لإزالة مربع التنقيط عند الضغط
        x.setContentAreaFilled(false);  // لجعل الخلفية شفافة
        x.setOpaque(false);             // لجعل الزر شفافاً تمام
        x.setContentAreaFilled(false);
        x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // الأكشن الجديد للحذف النهائي من JSON عند الضغط على X
        x.addActionListener(e -> {
            Notification.deleteNotification(n); // حذف نهائي من JSON
            adminNotifsList.remove(row);        // حذف من الواجهة
            adminNotifsList.revalidate();
            adminNotifsList.repaint();
        });

        row.add(ico, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(x, BorderLayout.EAST);
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

    // ── Data loading ──────────────────────────────────────────

    private void loadTableData() {
        liveData.clear();
        liveAppts.clear();
        if (adminObj != null) {
            ArrayList<Appointment> appts = adminObj.getUserAppointments();
            int i = 1;
            for (Appointment appt : appts) {
                String id           = String.format("#%03d", i++);
                String user         = appt.getUser() != null ? appt.getUser().getUsername() : "Unknown";
                String type         = formatType(appt.getType());
                String dateTime     = formatDateTime(appt.getDate(), appt.getStartTime());
                String duration     = appt.getDuration() + " min";
                String participants = String.valueOf(appt.getMaxParticipants());
                String status       = formatStatus(appt.getStatus());
                liveData.add(new Object[]{id, user, type, dateTime, duration, participants, status});
                liveAppts.add(appt);
           }
        }
        if (tableModel != null) {
            tableModel.setRowCount(0);
            for (Object[] row : liveData)
                tableModel.addRow(row);
        }
        
        refreshStats();
    }

    private String formatType(AppointmentType type) {
        if (type == null) return "";
        switch (type) {
            case URGENT:     return "Urgent";
            case FOLLOW_UP:  return "Follow-up";
            case ASSESSMENT: return "Assessment";
            case VIRTUAL:    return "Virtual";
            case IN_PERSON:  return "In-person";
            case INDIVIDUAL: return "Individual";
            case GROUP:      return "Group";
            default:         return type.toString();
        }
    }

    private String formatStatus(AppointmentStatus status) {
        if (status == null) return "";
        switch (status) {
            case CONFIRMED: return "Confirmed";
            case PENDING:   return "Pending";
            case CANCELLED: return "Cancelled";
            default:        return status.toString();
        }
    }

    private String formatDateTime(LocalDate date, LocalTime time) {
        if (date == null) return "";
        String month = date.getMonth().toString();
        String mon   = month.charAt(0) + month.substring(1, 3).toLowerCase();
        String t     = time != null
                ? String.format(" · %02d:%02d", time.getHour(), time.getMinute())
                : "";
        return mon + " " + date.getDayOfMonth() + t;
    }

    // ── Table helpers ─────────────────────────────────────────

    private void filterTable(String query) {
        tableModel.setRowCount(0);
        String q = query.toLowerCase();
        for (Object[] row : liveData) {
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
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            p.setBackground(sel ? Theme.CREAM : (row % 2 == 0 ? Theme.CARD : Theme.PAPER));
            String status = t.getValueAt(row, 6) != null ? t.getValueAt(row, 6).toString() : "";
            if (!status.equalsIgnoreCase("Cancelled")) {
                p.add(Components.outlineBtn("Edit"));
                if (!status.equalsIgnoreCase("Confirmed")) {
                    JButton confirm = Components.outlineBtn("Confirm");
                    ((Components.RoundButton) confirm).setBorderColor(Theme.SUCCESS);
                    confirm.setForeground(Theme.SUCCESS);
                    p.add(confirm);
                }
                p.add(Components.dangerBtn("Cancel"));
            }
            return p;
        }
    }

    private class AdminActionEditor extends DefaultCellEditor {
        public AdminActionEditor(JCheckBox c) { super(c); }

        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                                                               boolean sel, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            p.setBackground(Theme.CREAM);
            String status = tableModel.getValueAt(row, 6) != null ? tableModel.getValueAt(row, 6).toString() : "";
            if (!status.equalsIgnoreCase("Cancelled")) {
                String user = tableModel.getValueAt(row, 1).toString();

                JButton edit = Components.outlineBtn("Edit");
                edit.addActionListener(e -> {
                    Appointment appt = liveAppts.get(row);
                    stopCellEditing();
                    showEditDialog(appt);
                });
                p.add(edit);

                if (!status.equalsIgnoreCase("Confirmed")) {
                    JButton confirm = Components.outlineBtn("Confirm");
                    ((Components.RoundButton) confirm).setBorderColor(Theme.SUCCESS);
                    confirm.setForeground(Theme.SUCCESS);

                    confirm.addActionListener(e -> {
                        int r = JOptionPane.showConfirmDialog(t,
                                "Confirm this reservation for " + user + "?",
                                "Admin Confirm", JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.YES_OPTION) {
                            Appointment appt = liveAppts.get(row);
                            Administrator.confirmAppointment(appt);
                            stopCellEditing();
                            loadTableData();
                        } else {
                            stopCellEditing();
                        }
                    });
                    p.add(confirm);
                }

                JButton can = Components.dangerBtn("Cancel");
                can.addActionListener(e -> {
                    int r = JOptionPane.showConfirmDialog(t,
                            "Cancel reservation for " + user + "?\nSlot will become available again.",
                            "Admin Cancel", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.YES_OPTION) {
                        Appointment appt = liveAppts.get(row);
                        adminObj.cancelAppointment(appt);
                        stopCellEditing();
                        loadTableData();
                    } else {
                        stopCellEditing();
                    }
                });
                p.add(can);
            }
            //SwingUtilities.invokeLater(this::stopCellEditing);
            return p;
        }
    }
}