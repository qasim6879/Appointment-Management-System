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

public class UserDashboard extends JPanel {

    public interface LogoutListener {
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
    private JPanel     notifsList;

    private JButton btnAppts;
    private JButton btnBook;
    private JButton btnNotifs;

    private DefaultTableModel   apptModel;
    private ButtonGroup         slotGroup      = new ButtonGroup();
    private JPanel              slotGrid       = new JPanel(new GridLayout(3, 4, 8, 8));
    private List<JToggleButton> slotBtns       = new ArrayList<>();
    private LocalDate           pickedDate     = null;
    private String              pickedAdmin    = null;
    private int                 pickedDuration = 30;

    public UserDashboard(String username, LogoutListener logoutListener) {
        this.username = username;
        this.logoutListener = logoutListener;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);

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
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(BorderFactory.createEmptyBorder(28, 0, 28, 0));

        JLabel logo = new JLabel("  AppointEase");
        logo.setFont(new Font("Serif", Font.BOLD, 18));
        logo.setForeground(Theme.PAPER);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 20, 6, 16));
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel userChip = new JLabel("  " + capitalize(username));
        userChip.setFont(Theme.FONT_SMALL);
        userChip.setForeground(new Color(0x88, 0x82, 0x78));
        userChip.setBorder(BorderFactory.createEmptyBorder(0, 20, 24, 16));
        userChip.setAlignmentX(LEFT_ALIGNMENT);

        side.add(logo);
        side.add(userChip);

        btnAppts  = Components.sidebarItem("📅", "My Appointments");
        btnBook   = Components.sidebarItem("➕", "Book Appointment");
        btnNotifs = Components.sidebarItem("🔔", "Notifications");

        btnAppts.addActionListener(e  -> { switchTo(CARD_APPOINTMENTS); refreshAppointmentsTable(); });
        btnBook.addActionListener(e   -> switchTo(CARD_BOOK));
        btnNotifs.addActionListener(e -> { switchTo(CARD_NOTIFS); rebuildNotifs(); });

        side.add(btnAppts);
        side.add(btnBook);
        side.add(btnNotifs);

        JButton logout = Components.sidebarItem("🚪", "Log out");
        logout.setForeground(Theme.ACCENT);
        logout.addActionListener(e -> logoutListener.onLogout());

        side.add(Box.createVerticalGlue());
        side.add(Components.divider());
        side.add(Box.createVerticalStrut(8));
        side.add(logout);

        setActive(btnAppts);
        return side;
    }

    private void switchTo(String card) {
        contentCards.show(contentPanel, card);
        switch (card) {
            case CARD_APPOINTMENTS: setActive(btnAppts);  break;
            case CARD_BOOK:         setActive(btnBook);   break;
            case CARD_NOTIFS:       setActive(btnNotifs); break;
        }
    }

    private void setActive(JButton active) {
        for (JButton b : new JButton[]{btnAppts, btnBook, btnNotifs}) {
            boolean on = (b == active);
            b.setBackground(on ? Theme.ACCENT : Theme.SIDEBAR_BG);
            b.setForeground(on ? Theme.WHITE  : new Color(0xBB, 0xB4, 0xA8));
        }
    }

    // ── Main shell ────────────────────────────────────────────

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
                BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        left.add(Components.sectionLabel("User Dashboard"));
        JLabel greeting = new JLabel("Good morning, " + capitalize(username) + ".");
        greeting.setFont(new Font("Serif", Font.BOLD, 22));
        greeting.setForeground(Theme.INK);
        left.add(greeting);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setForeground(Theme.WHITE);
        avatar.setFont(new Font("Serif", Font.BOLD, 16));
        JPanel nameCol = new JPanel(new GridLayout(2, 1));
        nameCol.setOpaque(false);
        JLabel nameL = new JLabel(capitalize(username));
        nameL.setFont(new Font("SansSerif", Font.BOLD, 13)); nameL.setForeground(Theme.INK);
        JLabel roleL = new JLabel("user");
        roleL.setFont(Theme.FONT_SMALL); roleL.setForeground(Theme.MUTED);
        nameCol.add(nameL); nameCol.add(roleL);
        chip.add(avatar); chip.add(nameCol);

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

    // ── VIEW 1 — MY APPOINTMENTS ──────────────────────────────

    private JPanel buildAppointmentsView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        view.add(buildStats());
        view.add(Box.createVerticalStrut(24));
        view.add(buildAppointmentsCard());
        view.add(Box.createVerticalStrut(28));
        return view;
    }

    private JPanel buildStats() {
        ArrayList<Appointment> appts = User.getUserObject(username).getUserAppointments();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        int upcoming = 0, completed = 0, pending = 0, reminders = 0;
        for (Appointment a : appts) {
            LocalDateTime apptTime = LocalDateTime.of(a.getDate(), a.getStartTime());
            boolean isFuture = apptTime.isAfter(now);
            boolean isPast = apptTime.isBefore(now);
            boolean isToday = a.getDate().equals(today);
            String status = a.getStatus().toString().toUpperCase();

            if (isFuture && !status.equals("CANCELLED"))
                upcoming++;
            if (isPast && status.equals("CONFIRMED"))
                completed++;
            if (isFuture && status.equals("PENDING"))
                pending++;
            if (isToday && isFuture && !status.equals("CANCELLED"))
                reminders++;
        }

        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.add(Components.statCard(String.valueOf(upcoming), "Upcoming appointments", Theme.ACCENT2));
        row.add(Components.statCard(String.valueOf(completed), "Completed", Theme.SUCCESS));
        row.add(Components.statCard(String.valueOf(pending), "Pending confirmation", Theme.ACCENT));
        row.add(Components.statCard(String.valueOf(reminders), "Reminders today", Theme.WARNING));
        return row;
    }

    private JPanel buildAppointmentsCard() {
        JPanel card = Components.card();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("MY UPCOMING APPOINTMENTS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        JButton addBtn = Components.primaryBtn("+ Book New");
        addBtn.addActionListener(e -> switchTo(CARD_BOOK));
        header.add(title,  BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        String[] cols = {"Date", "Administrator", "Time", "Duration", "Type", "Status", "Actions"};
        ArrayList<Appointment> appts = User.getUserObject(username).getUserAppointments();
        Object[][] data = new Object[appts.size()][7];
        for (int i = 0; i < appts.size(); i++) {
            Appointment a = appts.get(i);
            data[i][0] = a.getDate().toString();
            data[i][1] = a.getAdmin().getUsername();
            data[i][2] = a.getStartTime().toString();
            data[i][3] = a.getDuration() + " min";
            data[i][4] = a.getType().toString();
            data[i][5] = a.getStatus().toString();
            data[i][6] = "";
        }

        apptModel = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) {
                if (c != 6) return false;
                try {
                    String status = (String) getValueAt(r, 5);
                    if (status.equalsIgnoreCase("CANCELLED")) return false;
                    LocalDateTime apptTime = LocalDateTime.of(
                            LocalDate.parse((String) getValueAt(r, 0)),
                            LocalTime.parse((String) getValueAt(r, 2))
                    );
                    return apptTime.isAfter(LocalDateTime.now());
                } catch (Exception e) { return false; }
            }
        };
        JTable table = new JTable(apptModel);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(48);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE8, 0xE2, 0xD8));
        table.setSelectionForeground(Theme.INK);
        table.setFillsViewportHeight(true);
        table.setBackground(Theme.CARD);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setBackground(Theme.ACCENT);
        th.setForeground(Theme.WHITE);
        th.setBorder(null);
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                     boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBackground(sel ? new Color(0xE8,0xE2,0xD8) : (row % 2 == 0 ? Theme.CARD : new Color(0xF2,0xEE,0xE6)));
                setForeground(Theme.INK);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                setFont(Theme.FONT_BODY);
                return this;
            }
        });

        table.getColumn("Type").setCellRenderer(new TypeTagRenderer());
        table.getColumn("Status").setCellRenderer(new StatusTagRenderer());
        table.getColumn("Actions").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Actions").setCellEditor(new ActionCellEditor(new JCheckBox()));

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(Theme.BORDER, 1, true));
        sp.getViewport().setBackground(Theme.CARD);
        card.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("  ⚠  Only future appointments can be cancelled.");
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(Theme.MUTED);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    private void refreshAppointmentsTable() {
        if (apptModel == null)
            return;

        // Auto-cancel past pending appointments
        User user = User.getUserObject(username);
        for (Appointment a : user.getUserAppointments()) {
            LocalDateTime apptTime = LocalDateTime.of(a.getDate(), a.getStartTime());
            if (apptTime.isBefore(LocalDateTime.now())
                    && a.getStatus().toString().toUpperCase().equals("PENDING")) {
                user.cancelAppointment(a);
            }
        }

        apptModel.setRowCount(0);
        for (Appointment a : user.getUserAppointments()) {
            apptModel.addRow(new Object[] {
                    a.getDate().toString(),
                    a.getAdmin().getUsername(),
                    a.getStartTime().toString(),
                    a.getDuration() + " min",
                    a.getType().toString(),
                    a.getStatus().toString(),
                    ""
            });
        }
    }

    // ── VIEW 2 — BOOK APPOINTMENT ─────────────────────────────

    private JPanel buildBookView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Book an Appointment");
        heading.setFont(new Font("Serif", Font.BOLD, 24));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = Components.subtitle("Select an administrator, pick a date and duration, then choose a free slot.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        view.add(heading);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(24));

        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row.add(buildSlotsCard());
        row.add(buildBookingFormCard());
        view.add(row);
        view.add(Box.createVerticalStrut(28));
        return view;
    }

    private void showDatePicker(JLabel slotHeader) {
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

        JLabel hint = new JLabel("Weekends, past or too far dates are unavailable", SwingConstants.CENTER);
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
                    pickedDate = date;
                    refreshSlots();
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

    private JPanel buildSlotsCard() {
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
            @Override public void mouseClicked(MouseEvent e) { showDatePicker(t); }
            @Override public void mouseEntered(MouseEvent e) { ch.setForeground(Theme.ACCENT2); }
            @Override public void mouseExited(MouseEvent e)  { ch.setForeground(Theme.ACCENT); }
        });
        header.add(t, BorderLayout.WEST); header.add(ch, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        String[] times = {"09:00","09:30","10:00","10:30","11:00","11:30",
                "12:00","12:30","13:00","13:30","14:00","14:30"};
        slotGrid.setOpaque(false);
        slotBtns.clear();
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
                for (JToggleButton s : slotBtns)
                    if (!s.isSelected()) { s.setBackground(SLOT_AVAILABLE); s.setForeground(Theme.INK); }
                slot.setBackground(SLOT_SELECTED);
                slot.setForeground(Theme.WHITE);
            });
            slotGroup.add(slot);
            slotBtns.add(slot);
            slotGrid.add(slot);
        }
        card.add(slotGrid, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.add(legendDot(Theme.PAPER,  "Available"));
        legend.add(legendDot(Theme.CREAM,  "Booked"));
        legend.add(legendDot(Theme.ACCENT, "Selected"));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private void refreshSlots() {
        slotGroup.clearSelection();
        for (JToggleButton slot : slotBtns) {
            slot.setEnabled(false);
            slot.setBackground(SLOT_BOOKED);
            slot.setForeground(Theme.MUTED);
        }
        if (pickedDate == null || pickedAdmin == null) return;
        boolean[] available = Appointment.availableTimeSlots(pickedDate, pickedAdmin, pickedDuration);
        for (int i = 0; i < slotBtns.size(); i++) {
            slotBtns.get(i).setEnabled(available[i]);
            slotBtns.get(i).setBackground(available[i] ? SLOT_AVAILABLE : SLOT_BOOKED);
            slotBtns.get(i).setForeground(available[i] ? Theme.INK      : Theme.MUTED);
        }
    }

    private JPanel buildBookingFormCard() {
        JPanel card = Components.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("APPOINTMENT DETAILS");
        title.setFont(Theme.FONT_LABEL); title.setForeground(Theme.MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        List<Administrator> admins = JsonHandler.loadList("admins.json", Administrator.class);
        String[] adminNames = new String[admins.size() + 1];
        adminNames[0] = "— Select Administrator —";
        for (int i = 0; i < admins.size(); i++)
            adminNames[i + 1] = admins.get(i).username;
        JComboBox<String> adminBox = Components.comboBox(adminNames);
        adminBox.setAlignmentX(LEFT_ALIGNMENT);
        adminBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        adminBox.addActionListener(e -> {
            int idx = adminBox.getSelectedIndex();
            pickedAdmin = idx == 0 ? null : (String) adminBox.getSelectedItem();
            refreshSlots();
        });

        String[] types = {"— Appointment Type —","Urgent","Follow-up","Assessment",
                "Virtual","In-person","Individual","Group"};
        JComboBox<String> typeBox = Components.comboBox(types);
        typeBox.setAlignmentX(LEFT_ALIGNMENT);
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JToggleButton dur30 = new JToggleButton("30 min");
        JToggleButton dur60 = new JToggleButton("60 min");
        new ButtonGroup() {{ add(dur30); add(dur60); }};
        for (JToggleButton db : new JToggleButton[]{dur30, dur60}) {
            db.setFont(new Font("SansSerif", Font.BOLD, 13));
            db.setFocusPainted(false);
            db.setBackground(SLOT_AVAILABLE);
            db.setForeground(Theme.INK);
            db.setBorder(new LineBorder(Theme.BORDER, 1, true));
            db.setPreferredSize(new Dimension(0, 60));
            db.addActionListener(e -> {
                dur30.setBackground(dur30.isSelected() ? SLOT_SELECTED : SLOT_AVAILABLE);
                dur30.setForeground(dur30.isSelected() ? Theme.WHITE   : Theme.INK);
                dur60.setBackground(dur60.isSelected() ? SLOT_SELECTED : SLOT_AVAILABLE);
                dur60.setForeground(dur60.isSelected() ? Theme.WHITE   : Theme.INK);
                pickedDuration = dur30.isSelected() ? 30 : 60;
                refreshSlots();
            });
        }
        JPanel durRow = new JPanel(new GridLayout(1, 2, 8, 0));
        durRow.setOpaque(false);
        durRow.setAlignmentX(LEFT_ALIGNMENT);
        durRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        durRow.add(dur30); durRow.add(dur60);

        JButton confirm = Components.primaryBtn("Request Appointment  →");
        confirm.setAlignmentX(LEFT_ALIGNMENT);
        confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirm.addActionListener(e -> {
            String missing = "";
            if (pickedAdmin == null)                        missing += "• Select an administrator\n";
            if (pickedDate == null)                         missing += "• Pick a date\n";
            if (!dur30.isSelected() && !dur60.isSelected()) missing += "• Choose a duration\n";
            if (typeBox.getSelectedIndex() == 0)            missing += "• Choose appointment type\n";
            boolean slotSelected = slotBtns.stream().anyMatch(JToggleButton::isSelected);
            if (!slotSelected)                              missing += "• Select a time slot\n";

            if (!missing.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please complete the following before confirming:\n\n" + missing,
                        "Incomplete Booking", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JToggleButton selected = slotBtns.stream().filter(JToggleButton::isSelected).findFirst().get();
            LocalTime startTime = LocalTime.parse(selected.getText());
            AppointmentType type = AppointmentType.valueOf(
                    typeBox.getSelectedItem().toString().toUpperCase().replace("-", "_").replace(" ", "_")
            );

            User.getUserObject(username).bookAppointment(pickedAdmin, pickedDate, startTime, pickedDuration, type);
            refreshSlots();
            JOptionPane.showMessageDialog(this,
                    "Appointment requested successfully!\nStatus: Pending.",
                    "Request Sent", JOptionPane.INFORMATION_MESSAGE);
            switchTo(CARD_APPOINTMENTS);
            refreshAppointmentsTable();
        });

        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(mkLabel("Administrator"));
        card.add(Box.createVerticalStrut(5));
        card.add(adminBox);
        card.add(Box.createVerticalStrut(16));
        card.add(mkLabel("Appointment Type"));
        card.add(Box.createVerticalStrut(5));
        card.add(typeBox);
        card.add(Box.createVerticalStrut(16));
        card.add(mkLabel("Duration"));
        card.add(Box.createVerticalStrut(5));
        card.add(durRow);
        card.add(Box.createVerticalStrut(20));
        card.add(confirm);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JLabel mkLabel(String text) {
        JLabel l = Components.sectionLabel(text);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // ── VIEW 3 — NOTIFICATIONS ────────────────────────────────

    private JPanel buildNotificationsView() {
        JPanel view = new JPanel();
        view.setBackground(Theme.PAPER);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Notifications & Reminders");
        heading.setFont(new Font("Serif", Font.BOLD, 24));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerRow.add(heading, BorderLayout.WEST);
        JLabel mark = new JLabel("Mark all read ›");
        mark.setFont(Theme.FONT_SMALL); mark.setForeground(Theme.ACCENT);
        mark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        mark.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // حذف نهائي من الـ JSON
                Notification.deleteAllNotifications(username);
                // تنظيف الواجهة
                notifsList.removeAll();
                notifsList.revalidate();
                notifsList.repaint();
            }
        });
        headerRow.add(mark, BorderLayout.EAST);

        JLabel sub = Components.subtitle("Reminders, confirmations, and slot updates appear here.");
        sub.setAlignmentX(LEFT_ALIGNMENT);

        notifsList = new JPanel();
        notifsList.setOpaque(false);
        notifsList.setLayout(new GridLayout(0, 1, 0, 10));
        rebuildNotifs();

        view.add(headerRow);
        view.add(Box.createVerticalStrut(4));
        view.add(sub);
        view.add(Box.createVerticalStrut(24));
        view.add(notifsList);
        view.add(Box.createVerticalStrut(28));
        return view;
    }

    private void rebuildNotifs() {
        notifsList.removeAll();
        List<Notification> notifs =Notification.getNotifications(username);
        if (notifs == null || notifs.isEmpty()) {
            JLabel empty = new JLabel("No notifications yet.");
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            notifsList.add(empty);
        } else {
        	Collections.reverse(notifs);
            for (Notification n : notifs)
                if (n.isActive())
                    notifsList.add(buildNotifRow(n));
        }
        notifsList.revalidate();
        notifsList.repaint();
    }
    
    private JPanel buildNotifRow(Notification notif) {
        String icon;
        Color accent;
        switch (notif.getType()) {
            case REMINDER:     icon = "🔔"; accent = Theme.WARNING; break;
            case CONFIRMATION: icon = "✅"; accent = Theme.SUCCESS;  break;
            case CANCELLATION: icon = "❌"; accent = Theme.ACCENT;   break;
            default:           icon = "🔔"; accent = Theme.MUTED;    break;
        }

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
        JLabel t  = new JLabel(notif.getType().toString());
        t.setFont(new Font("SansSerif", Font.BOLD, 12)); t.setForeground(Theme.INK);
        JLabel b  = new JLabel(notif.getMessage());
        b.setFont(Theme.FONT_BODY); b.setForeground(Theme.INK);
        JLabel ts = new JLabel(notif.getDateSent());
        ts.setFont(Theme.FONT_SMALL); ts.setForeground(Theme.MUTED);
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
            Notification.deleteNotification(notif);   // ← removes from JSON
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
    
    
    
    
    
    
    private JPanel buildNotifRow(String icon, String title, String body, String time, Color accent) {
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
        //x.setPreferredSize(new Dimension(36, 36));
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

    // ── Helpers ───────────────────────────────────────────────

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
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

    private static Color rowBg(boolean sel, int row) {
        return sel ? new Color(0xE8,0xE2,0xD8) : (row % 2 == 0 ? Theme.CARD : new Color(0xF2,0xEE,0xE6));
    }

    private static class TypeTagRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean focus, int row, int col) {
            String val = v == null ? "" : v.toString();
            JLabel tag;
            switch (val) {
                case "URGENT":     case "Urgent":     tag = Components.tagUrgent();  break;
                case "VIRTUAL":    case "Virtual":    tag = Components.tagVirtual(); break;
                case "GROUP":      case "Group":      tag = Components.tagGroup();   break;
                case "IN_PERSON":  case "In-person":  case "In_Person":
                    tag = Components.tag("In-Person",  new Color(0xD2,0xE8,0xFA), Theme.ACCENT2); break;
                case "FOLLOW_UP":  case "Follow-up":  case "Follow_Up":
                    tag = Components.tag("Follow-Up",  new Color(0xFA,0xEE,0xD2), Theme.WARNING); break;
                case "ASSESSMENT": case "Assessment":
                    tag = Components.tag("Assessment", new Color(0xEC,0xE3,0xFA), new Color(0x6B,0x3F,0xC8)); break;
                case "INDIVIDUAL": case "Individual":
                    tag = Components.tag("Individual", new Color(0xD4,0xED,0xDF), Theme.SUCCESS); break;
                default: tag = new JLabel(val);
            }
            tag.setBackground(rowBg(sel, row));
            tag.setOpaque(true);
            return tag;
        }
    }

    private static class StatusTagRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean focus, int row, int col) {
            String val = v == null ? "" : v.toString();
            JLabel tag;
            switch (val) {
                case "CONFIRMED": case "Confirmed": tag = Components.tagConfirmed(); break;
                case "PENDING":   case "Pending":   tag = Components.tagPending();   break;
                case "URGENT":    case "Urgent":    tag = Components.tagUrgent();    break;
                case "CANCELLED": case "Cancelled":
                    tag = Components.tag("Cancelled", new Color(0xF0,0xD8,0xD8), Theme.ACCENT); break;
                default: tag = new JLabel(val);
            }
            tag.setBackground(rowBg(sel, row));
            tag.setOpaque(true);
            return tag;
        }
    }

    private class ActionCellRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean focus, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
            p.setBackground(rowBg(sel, row));
            try {
                String dateStr = (String) apptModel.getValueAt(row, 0);
                String timeStr = (String) apptModel.getValueAt(row, 2);
                String status  = (String) apptModel.getValueAt(row, 5);
                LocalDateTime apptTime = LocalDateTime.of(LocalDate.parse(dateStr), LocalTime.parse(timeStr));
                if (apptTime.isAfter(LocalDateTime.now()) && !status.equalsIgnoreCase("CANCELLED"))
                    p.add(Components.dangerBtn("Cancel"));
            } catch (Exception ignored) {}
            return p;
        }
    }

    private class ActionCellEditor extends DefaultCellEditor {
        public ActionCellEditor(JCheckBox c) { super(c); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                                                               boolean sel, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
            p.setBackground(new Color(0xE8, 0xE2, 0xD8));
            try {
                String dateStr = (String) apptModel.getValueAt(row, 0);
                String timeStr = (String) apptModel.getValueAt(row, 2);
                LocalDateTime apptTime = LocalDateTime.of(LocalDate.parse(dateStr), LocalTime.parse(timeStr));
                if (apptTime.isAfter(LocalDateTime.now())) {
                    JButton can = Components.dangerBtn("Cancel");
                    can.addActionListener(e -> {
                        int r = JOptionPane.showConfirmDialog(t,
                                "Cancel this appointment?\nThe slot will become available again.",
                                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.YES_OPTION) {
                            // Cancel the appointment in the model
                            Appointment appt = User.getUserObject(username).getUserAppointments().get(row);
                            User.getUserObject(username).cancelAppointment(appt);

                            stopCellEditing();                  // stop editing first

                            // Tell the table to redraw just this cell
                            apptModel.fireTableCellUpdated(row, 6);

                            // Optional: refresh entire table if needed
                            refreshAppointmentsTable();
                        } else {
                            stopCellEditing();
                        }
                    });
                    p.add(can);
                }
            } catch (Exception ignored) {}
            return p;
        }
    }
}