import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cyber Incident Reporting & Management System
 * SINGLE FILE VERSION - GUI + JDBC + MySQL CRUD + Reports.
 *
 * Database expected:
 *   cyber_incident_management
 *
 * Tables expected:
 *   Incident
 *   Incident_Type
 *   Severity
 *   Status
 *   Personnel
 *   Affected_System
 *   Investigation
 *   Resolution
 *
 * Existing DB objects used by Reports:
 *   View: Incident_Report
 *   Procedure: GetIncidentsBySeverity(IN severity_input VARCHAR(50))
 *
 * No DAO classes are required by this file.
 */
public class CyberIncidentManagementGUI extends JFrame {

    // ===================== DATABASE =====================
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/cyber_incident_management";
    private static final String DB_USER = "root";
    private Connection conn;

    // ===================== THEME =====================
    private static final Color BG = new Color(12, 17, 23);
    private static final Color SIDEBAR = new Color(9, 13, 18);
    private static final Color PANEL = new Color(20, 27, 35);
    private static final Color PANEL2 = new Color(27, 36, 46);
    private static final Color BORDER = new Color(48, 62, 77);
    private static final Color TEXT = new Color(235, 241, 247);
    private static final Color MUTED = new Color(147, 163, 179);
    private static final Color ACCENT = new Color(48, 175, 255);
    private static final Color SUCCESS = new Color(65, 214, 139);
    private static final Color WARNING = new Color(244, 191, 67);
    private static final Color DANGER = new Color(240, 82, 82);

    // ===================== NAVIGATION =====================
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final List<JButton> navButtons = new ArrayList<>();

    // ===================== DASHBOARD =====================
    private JLabel totalLabel, highLabel, openLabel, criticalLabel;
    private DefaultTableModel recentModel;

    // ===================== INCIDENTS =====================
    private DefaultTableModel incidentModel;
    private JTable incidentTable;
    private TableRowSorter<DefaultTableModel> incidentSorter;
    private JTextField incidentSearch;
    private JComboBox<String> severityFilter, statusFilter;
    private int selectedIncidentId = -1;

    // ===================== SYSTEMS =====================
    private DefaultTableModel systemModel;
    private JTable systemTable;

    // ===================== PERSONNEL =====================
    private DefaultTableModel personnelModel;
    private JTable personnelTable;

    public CyberIncidentManagementGUI(Connection conn) {
        this.conn = conn;
        setTitle("Cyber Incident Reporting & Management System");
        setSize(1380, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        loadAllTables();
    }

    // =============================================================
    // UI BUILD
    // =============================================================
    private void buildUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(BG);
        content.add(buildDashboard(), "Dashboard");
        content.add(buildIncidents(), "Incidents");
        content.add(buildSystems(), "Affected Systems");
        content.add(buildPersonnel(), "Personnel");
        content.add(buildReports(), "Reports");
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(235, 0));
        side.setBackground(SIDEBAR);
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(28, 23, 24, 18));

        JLabel logo = new JLabel("◈");
        logo.setForeground(ACCENT);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        JLabel title = new JLabel("CYBER INCIDENT");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel subtitle = new JLabel("MANAGEMENT SYSTEM");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brand.add(logo);
        brand.add(Box.createVerticalStrut(4));
        brand.add(title);
        brand.add(subtitle);
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(5, 12, 10, 12));
        addNav(nav, "▣", "Dashboard", "Dashboard");
        addNav(nav, "⚠", "Incidents", "Incidents");
        addNav(nav, "▤", "Affected Systems", "Affected Systems");
        addNav(nav, "♟", "Personnel", "Personnel");
        addNav(nav, "▥", "Reports", "Reports");
        side.add(nav, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(15, 20, 20, 20));
        JLabel connected = new JLabel("●  MYSQL CONNECTED");
        connected.setForeground(SUCCESS);
        connected.setFont(new Font("Segoe UI", Font.BOLD, 11));
        JLabel db = new JLabel("cyber_incident_management");
        db.setForeground(MUTED);
        db.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        bottom.add(connected);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(db);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private void addNav(JPanel parent, String icon, String text, String card) {
        JButton b = new JButton("  " + icon + "    " + text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(MUTED);
        b.setBackground(SIDEBAR);
        b.setBorder(new EmptyBorder(13, 12, 13, 12));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            cards.show(content, card);
            setActiveNav(b);
        });
        navButtons.add(b);
        parent.add(b);
        parent.add(Box.createVerticalStrut(4));
        if (navButtons.size() == 1) setActiveNav(b);
    }

    private void setActiveNav(JButton active) {
        for (JButton b : navButtons) {
            b.setBackground(SIDEBAR);
            b.setForeground(MUTED);
        }
        active.setBackground(new Color(25, 67, 92));
        active.setForeground(TEXT);
    }

    // =============================================================
    // DASHBOARD
    // =============================================================
    private JPanel buildDashboard() {
        JPanel page = page("Security Dashboard",
                "Live overview of the cyber incident management database");

        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsPanel.setOpaque(false);
        totalLabel = new JLabel("0");
        highLabel = new JLabel("0");
        openLabel = new JLabel("0");
        criticalLabel = new JLabel("0");
        cardsPanel.add(statCard("TOTAL INCIDENTS", totalLabel, ACCENT));
        cardsPanel.add(statCard("HIGH SEVERITY", highLabel, WARNING));
        cardsPanel.add(statCard("OPEN / INVESTIGATING", openLabel, ACCENT));
        cardsPanel.add(statCard("CRITICAL", criticalLabel, DANGER));
        center.add(cardsPanel, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 14, 0));
        lower.setOpaque(false);
        lower.add(buildRecentCard());
        lower.add(buildStatusCard());
        center.add(lower, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(18, 20, 18, 20)));
        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        value.setForeground(accent);
        value.setFont(new Font("Segoe UI", Font.BOLD, 32));
        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(t, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildRecentCard() {
        JPanel p = titledPanel("Recent Incidents");
        recentModel = new DefaultTableModel(
                new String[]{"ID", "Incident", "Severity", "Status", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(recentModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStatusCard() {
        JPanel p = titledPanel("System Status");
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        statusRow(box, "MySQL Connection", "CONNECTED", SUCCESS);
        statusRow(box, "Incident CRUD", "ACTIVE", SUCCESS);
        statusRow(box, "Search / Filters", "ACTIVE", SUCCESS);
        statusRow(box, "Incident_Report View", "READY", SUCCESS);
        statusRow(box, "GetIncidentsBySeverity", "READY", SUCCESS);
        box.add(Box.createVerticalStrut(18));
        JLabel note = new JLabel("<html><div style='width:360px'>"
                + "This screen is connected directly to your MySQL database."
                + "</div></html>");
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        box.add(note);
        p.add(box, BorderLayout.CENTER);
        return p;
    }

    private void statusRow(JPanel parent, String name, String state, Color c) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 2, 9, 2));
        JLabel left = new JLabel("●  " + name);
        left.setForeground(TEXT);
        JLabel right = new JLabel(state);
        right.setForeground(c);
        right.setFont(new Font("Segoe UI", Font.BOLD, 11));
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        parent.add(row);
    }

    // =============================================================
    // INCIDENTS
    // =============================================================
    private JPanel buildIncidents() {
        JPanel page = page("Incident Management",
                "Create, search, update and delete incidents in MySQL");

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        incidentSearch = new JTextField();
        styleTextField(incidentSearch);
        incidentSearch.setToolTipText("Search title, description, type, person or system");
        JButton search = accentButton("Search");
        JButton reset = secondaryButton("Reset");
        JButton add = accentButton("+  New Incident");

        JPanel searchButtons = new JPanel(new BorderLayout(8, 0));
        searchButtons.setOpaque(false);
        searchButtons.add(search, BorderLayout.CENTER);
        searchButtons.add(reset, BorderLayout.EAST);
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.add(incidentSearch, BorderLayout.CENTER);
        searchBox.add(searchButtons, BorderLayout.EAST);
        toolbar.add(searchBox, BorderLayout.CENTER);
        toolbar.add(add, BorderLayout.EAST);
        main.add(toolbar, BorderLayout.NORTH);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setOpaque(false);
        filter.add(label("Severity"));
        severityFilter = combo(new String[]{"All", "Low", "Medium", "High", "Critical"});
        filter.add(severityFilter);
        filter.add(label("Status"));
        statusFilter = combo(new String[]{"All", "Reported", "Under Investigation", "Resolved", "Closed"});
        filter.add(statusFilter);
        JButton edit = secondaryButton("Edit Selected");
        JButton delete = dangerButton("Delete Selected");
        filter.add(edit);
        filter.add(delete);
        main.add(filter, BorderLayout.SOUTH);

        incidentModel = new DefaultTableModel(new String[]{
                "ID", "Title", "Attack Type", "Severity", "Status",
                "Personnel", "Affected System", "Reported Date"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        incidentTable = styledTable(incidentModel);
        incidentTable.setRowHeight(33);
        incidentSorter = new TableRowSorter<>(incidentModel);
        incidentTable.setRowSorter(incidentSorter);
        incidentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelectedIncident();
            }
        });

        JPanel tablePanel = titledPanel("Incident Records  •  Double-click to edit");
        tablePanel.add(new JScrollPane(incidentTable), BorderLayout.CENTER);
        main.add(tablePanel, BorderLayout.CENTER);

        search.addActionListener(e -> filterIncidents());
        reset.addActionListener(e -> resetIncidentFilters());
        severityFilter.addActionListener(e -> filterIncidents());
        statusFilter.addActionListener(e -> filterIncidents());
        add.addActionListener(e -> showIncidentEditor(-1));
        edit.addActionListener(e -> editSelectedIncident());
        delete.addActionListener(e -> deleteIncident());

        page.add(main, BorderLayout.CENTER);
        return page;
    }

    private void filterIncidents() {
        String q = incidentSearch.getText().trim();
        String sev = String.valueOf(severityFilter.getSelectedItem());
        String status = String.valueOf(statusFilter.getSelectedItem());
        List<RowFilter<Object,Object>> filters = new ArrayList<>();
        if (!q.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
        }
        if (!"All".equals(sev)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(sev) + "$", 3));
        }
        if (!"All".equals(status)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 4));
        }
        incidentSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void resetIncidentFilters() {
        incidentSearch.setText("");
        severityFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        incidentSorter.setRowFilter(null);
    }

    private void editSelectedIncident() {
        int viewRow = incidentTable.getSelectedRow();
        if (viewRow < 0) {
            showInfo(this, "Select an incident first.");
            return;
        }
        int modelRow = incidentTable.convertRowIndexToModel(viewRow);
        int id = (int) incidentModel.getValueAt(modelRow, 0);
        showIncidentEditor(id);
    }

    private void showIncidentEditor(int incidentId) {
        JDialog d = new JDialog(this,
                incidentId > 0 ? "Edit Incident" : "Report New Incident", true);
        d.setSize(720, 670);
        d.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL);
        form.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        JTextField title = new JTextField();
        JTextArea description = new JTextArea(4, 25);
        JTextField date = new JTextField(LocalDate.now().toString());
        JComboBox<IdName> type = new JComboBox<>();
        JComboBox<IdName> severity = new JComboBox<>();
        JComboBox<IdName> status = new JComboBox<>();
        JComboBox<IdName> personnel = new JComboBox<>();
        JComboBox<IdName> system = new JComboBox<>();

        styleTextField(title); styleTextArea(description); styleTextField(date);
        styleCombo(type); styleCombo(severity); styleCombo(status);
        styleCombo(personnel); styleCombo(system);

        loadCombo(type, "SELECT type_id, type_name FROM Incident_Type ORDER BY type_name");
        loadCombo(severity, "SELECT severity_id, severity_name FROM Severity ORDER BY severity_id");
        loadCombo(status, "SELECT status_id, status_name FROM Status ORDER BY status_id");
        loadCombo(personnel, "SELECT personnel_id, name FROM Personnel ORDER BY name");
        loadCombo(system, "SELECT system_id, system_name FROM Affected_System ORDER BY system_name");

        addFormRow(form, g, 0, "Title", title);
        addFormRow(form, g, 1, "Description", new JScrollPane(description));
        addFormRow(form, g, 2, "Attack Type", type);
        addFormRow(form, g, 3, "Severity", severity);
        addFormRow(form, g, 4, "Status", status);
        addFormRow(form, g, 5, "Assigned Personnel", personnel);
        addFormRow(form, g, 6, "Affected System", system);
        addFormRow(form, g, 7, "Reported Date", date);

        if (incidentId > 0) {
            loadIncidentForEdit(incidentId, title, description, date,
                    type, severity, status, personnel, system);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = secondaryButton("Cancel");
        JButton save = accentButton(incidentId > 0 ? "Save Changes" : "Add Incident");
        buttons.add(cancel); buttons.add(save);
        cancel.addActionListener(e -> d.dispose());

        save.addActionListener(e -> {
            IdName t = (IdName) type.getSelectedItem();
            IdName s = (IdName) severity.getSelectedItem();
            IdName st = (IdName) status.getSelectedItem();
            IdName p = (IdName) personnel.getSelectedItem();
            IdName a = (IdName) system.getSelectedItem();
            if (title.getText().trim().isEmpty()) {
                showError(d, "Incident title is required."); return;
            }
            if (t == null || s == null || st == null || p == null || a == null) {
                showError(d, "Type, severity, status, personnel and system are required."); return;
            }
            if (!validDate(date.getText().trim())) {
                showError(d, "Reported date must use YYYY-MM-DD."); return;
            }
            if (incidentId > 0) {
                updateIncident(incidentId, title.getText(), description.getText(),
                        date.getText(), t.id, s.id, st.id, p.id, a.id);
            } else {
                insertIncident(title.getText(), description.getText(),
                        date.getText(), t.id, s.id, st.id, p.id, a.id);
            }
            d.dispose();
        });

        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);
        d.add(main);
        d.setVisible(true);
    }

    private void loadIncidentForEdit(int id, JTextField title, JTextArea desc,
                                     JTextField date, JComboBox<IdName> type,
                                     JComboBox<IdName> severity, JComboBox<IdName> status,
                                     JComboBox<IdName> personnel, JComboBox<IdName> system) {
        String sql = "SELECT title, description, reported_date, type_id, severity_id, "
                + "status_id, personnel_id, system_id FROM Incident WHERE incident_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    title.setText(rs.getString("title"));
                    desc.setText(rs.getString("description"));
                    date.setText(rs.getString("reported_date"));
                    selectId(type, rs.getInt("type_id"));
                    selectId(severity, rs.getInt("severity_id"));
                    selectId(status, rs.getInt("status_id"));
                    selectId(personnel, rs.getInt("personnel_id"));
                    selectId(system, rs.getInt("system_id"));
                }
            }
        } catch (SQLException ex) {
            showError(this, "Could not load incident:\n" + ex.getMessage());
        }
    }

    private void insertIncident(String title, String desc, String date,
                                int type, int severity, int status,
                                int personnel, int system) {
        String sql = "INSERT INTO Incident "
                + "(title,description,reported_date,type_id,severity_id,status_id,personnel_id,system_id) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, desc.trim());
            ps.setDate(3, Date.valueOf(date.trim()));
            ps.setInt(4, type); ps.setInt(5, severity); ps.setInt(6, status);
            ps.setInt(7, personnel); ps.setInt(8, system);
            ps.executeUpdate();
            loadAllTables();
            showInfo(this, "Incident added successfully.");
        } catch (SQLException ex) {
            showError(this, "Could not add incident:\n" + ex.getMessage());
        }
    }

    private void updateIncident(int id, String title, String desc, String date,
                                int type, int severity, int status,
                                int personnel, int system) {
        String sql = "UPDATE Incident SET title=?,description=?,reported_date=?,"
                + "type_id=?,severity_id=?,status_id=?,personnel_id=?,system_id=? "
                + "WHERE incident_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim()); ps.setString(2, desc.trim());
            ps.setDate(3, Date.valueOf(date.trim()));
            ps.setInt(4, type); ps.setInt(5, severity); ps.setInt(6, status);
            ps.setInt(7, personnel); ps.setInt(8, system); ps.setInt(9, id);
            ps.executeUpdate();
            loadAllTables();
            showInfo(this, "Incident updated successfully.");
        } catch (SQLException ex) {
            showError(this, "Could not update incident:\n" + ex.getMessage());
        }
    }

    private void deleteIncident() {
        int viewRow = incidentTable.getSelectedRow();
        if (viewRow < 0) { showInfo(this, "Select an incident first."); return; }
        int modelRow = incidentTable.convertRowIndexToModel(viewRow);
        int id = (int) incidentModel.getValueAt(modelRow, 0);
        String title = String.valueOf(incidentModel.getValueAt(modelRow, 1));
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete incident #" + id + "?\n\n" + title,
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(
                    "DELETE FROM Resolution WHERE incident_id=?");
                 PreparedStatement p2 = conn.prepareStatement(
                    "DELETE FROM Investigation WHERE incident_id=?");
                 PreparedStatement p3 = conn.prepareStatement(
                    "DELETE FROM Incident WHERE incident_id=?")) {
                p1.setInt(1, id); p1.executeUpdate();
                p2.setInt(1, id); p2.executeUpdate();
                p3.setInt(1, id); p3.executeUpdate();
            }
            conn.commit();
            loadAllTables();
            showInfo(this, "Incident deleted successfully.");
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            showError(this, "Could not delete incident:\n" + ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // =============================================================
    // SYSTEMS
    // =============================================================
    private JPanel buildSystems() {
        JPanel page = page("Affected Systems",
                "Manage infrastructure referenced by incident records");
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        top.setOpaque(false);
        JButton add = accentButton("+  Add System");
        JButton edit = secondaryButton("Edit Selected");
        JButton delete = dangerButton("Delete Selected");
        top.add(add); top.add(edit); top.add(delete);
        main.add(top, BorderLayout.NORTH);

        systemModel = new DefaultTableModel(
                new String[]{"ID", "System Name", "System Type", "Location", "Description"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        systemTable = styledTable(systemModel);
        systemTable.setRowHeight(33);

        JPanel table = titledPanel("Registered Systems");
        table.add(new JScrollPane(systemTable), BorderLayout.CENTER);
        main.add(table, BorderLayout.CENTER);

        add.addActionListener(e -> systemEditor(-1));
        edit.addActionListener(e -> {
            int r = systemTable.getSelectedRow();
            if (r < 0) { showInfo(this, "Select a system first."); return; }
            int mr = systemTable.convertRowIndexToModel(r);
            systemEditor((int) systemModel.getValueAt(mr, 0));
        });
        delete.addActionListener(e -> deleteSystem());
        systemTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = systemTable.getSelectedRow();
                    if (r >= 0) {
                        int mr = systemTable.convertRowIndexToModel(r);
                        systemEditor((int) systemModel.getValueAt(mr, 0));
                    }
                }
            }
        });

        page.add(main, BorderLayout.CENTER);
        return page;
    }

    private void systemEditor(int id) {
        JDialog d = new JDialog(this, id > 0 ? "Edit Affected System" : "Add Affected System", true);
        d.setSize(620, 500); d.setLocationRelativeTo(this);
        JPanel main = dialogPanel();
        JPanel form = formPanel();
        GridBagConstraints g = formConstraints();

        JTextField name = new JTextField();
        JTextField type = new JTextField();
        JTextField location = new JTextField();
        JTextArea desc = new JTextArea(5, 22);
        styleTextField(name); styleTextField(type); styleTextField(location); styleTextArea(desc);

        if (id > 0) loadSystemEdit(id, name, type, location, desc);

        addFormRow(form, g, 0, "System Name", name);
        addFormRow(form, g, 1, "System Type", type);
        addFormRow(form, g, 2, "Location", location);
        addFormRow(form, g, 3, "Description", new JScrollPane(desc));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = secondaryButton("Cancel");
        JButton save = accentButton("Save");
        buttons.add(cancel); buttons.add(save);
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            if (name.getText().trim().isEmpty()) { showError(d, "System name is required."); return; }
            if (id > 0) updateSystem(id, name.getText(), type.getText(), location.getText(), desc.getText());
            else insertSystem(name.getText(), type.getText(), location.getText(), desc.getText());
            d.dispose();
        });
        main.add(form, BorderLayout.CENTER); main.add(buttons, BorderLayout.SOUTH);
        d.add(main); d.setVisible(true);
    }

    private void loadSystems() {
        systemModel.setRowCount(0);
        String sql = "SELECT system_id,system_name,system_type,location,description "
                + "FROM Affected_System ORDER BY system_id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                systemModel.addRow(new Object[]{
                        rs.getInt("system_id"), rs.getString("system_name"),
                        rs.getString("system_type"), rs.getString("location"), rs.getString("description")});
            }
        } catch (SQLException ex) { showError(this, "Could not load systems:\n" + ex.getMessage()); }
    }

    private void loadSystemEdit(int id, JTextField name, JTextField type,
                                JTextField location, JTextArea desc) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT system_name,system_type,location,description FROM Affected_System WHERE system_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name.setText(rs.getString("system_name"));
                    type.setText(rs.getString("system_type"));
                    location.setText(rs.getString("location"));
                    desc.setText(rs.getString("description"));
                }
            }
        } catch (SQLException ex) { showError(this, "Could not load system:\n" + ex.getMessage()); }
    }

    private void insertSystem(String name, String type, String location, String desc) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Affected_System(system_name,system_type,location,description) VALUES(?,?,?,?)")) {
            ps.setString(1, name.trim()); ps.setString(2, type.trim());
            ps.setString(3, location.trim()); ps.setString(4, desc.trim());
            ps.executeUpdate(); loadSystems(); showInfo(this, "System added successfully.");
        } catch (SQLException ex) { showError(this, "Could not add system:\n" + ex.getMessage()); }
    }

    private void updateSystem(int id, String name, String type, String location, String desc) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Affected_System SET system_name=?,system_type=?,location=?,description=? WHERE system_id=?")) {
            ps.setString(1, name.trim()); ps.setString(2, type.trim());
            ps.setString(3, location.trim()); ps.setString(4, desc.trim()); ps.setInt(5, id);
            ps.executeUpdate(); loadSystems(); loadIncidents(); showInfo(this, "System updated successfully.");
        } catch (SQLException ex) { showError(this, "Could not update system:\n" + ex.getMessage()); }
    }

    private void deleteSystem() {
        int r = systemTable.getSelectedRow();
        if (r < 0) { showInfo(this, "Select a system first."); return; }
        int mr = systemTable.convertRowIndexToModel(r);
        int id = (int) systemModel.getValueAt(mr, 0);
        int references = countReferences("SELECT COUNT(*) FROM Incident WHERE system_id=?", id);
        if (references > 0) {
            showError(this, "This system is referenced by " + references + " incident(s).\n"
                    + "Update those incidents before deleting it.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete the selected system?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Affected_System WHERE system_id=?")) {
            ps.setInt(1, id); ps.executeUpdate(); loadSystems(); showInfo(this, "System deleted successfully.");
        } catch (SQLException ex) { showError(this, "Could not delete system:\n" + ex.getMessage()); }
    }

    // =============================================================
    // PERSONNEL
    // =============================================================
    private JPanel buildPersonnel() {
        JPanel page = page("Personnel Management",
                "Manage security staff assigned to incidents");
        JPanel main = new JPanel(new BorderLayout(0, 12)); main.setOpaque(false);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); top.setOpaque(false);
        JButton add = accentButton("+  Add Personnel");
        JButton edit = secondaryButton("Edit Selected");
        JButton delete = dangerButton("Delete Selected");
        top.add(add); top.add(edit); top.add(delete); main.add(top, BorderLayout.NORTH);

        personnelModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Role", "Department", "Contact"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        personnelTable = styledTable(personnelModel); personnelTable.setRowHeight(33);
        JPanel table = titledPanel("Security Personnel");
        table.add(new JScrollPane(personnelTable), BorderLayout.CENTER);
        main.add(table, BorderLayout.CENTER);

        add.addActionListener(e -> personnelEditor(-1));
        edit.addActionListener(e -> {
            int r = personnelTable.getSelectedRow();
            if (r < 0) { showInfo(this, "Select personnel first."); return; }
            int mr = personnelTable.convertRowIndexToModel(r);
            personnelEditor((int) personnelModel.getValueAt(mr, 0));
        });
        delete.addActionListener(e -> deletePersonnel());
        personnelTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = personnelTable.getSelectedRow();
                    if (r >= 0) {
                        int mr = personnelTable.convertRowIndexToModel(r);
                        personnelEditor((int) personnelModel.getValueAt(mr, 0));
                    }
                }
            }
        });
        page.add(main, BorderLayout.CENTER); return page;
    }

    private void personnelEditor(int id) {
        JDialog d = new JDialog(this, id > 0 ? "Edit Personnel" : "Add Personnel", true);
        d.setSize(620, 500); d.setLocationRelativeTo(this);
        JPanel main = dialogPanel(); JPanel form = formPanel(); GridBagConstraints g = formConstraints();
        JTextField name = new JTextField(), role = new JTextField(), dept = new JTextField(), contact = new JTextField();
        styleTextField(name); styleTextField(role); styleTextField(dept); styleTextField(contact);
        if (id > 0) loadPersonnelEdit(id, name, role, dept, contact);
        addFormRow(form, g, 0, "Name", name); addFormRow(form, g, 1, "Role", role);
        addFormRow(form, g, 2, "Department", dept); addFormRow(form, g, 3, "Contact", contact);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); buttons.setOpaque(false);
        JButton cancel = secondaryButton("Cancel"), save = accentButton("Save"); buttons.add(cancel); buttons.add(save);
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            if (name.getText().trim().isEmpty()) { showError(d, "Personnel name is required."); return; }
            if (id > 0) updatePersonnel(id, name.getText(), role.getText(), dept.getText(), contact.getText());
            else insertPersonnel(name.getText(), role.getText(), dept.getText(), contact.getText());
            d.dispose();
        });
        main.add(form, BorderLayout.CENTER); main.add(buttons, BorderLayout.SOUTH); d.add(main); d.setVisible(true);
    }

    private void loadPersonnel() {
        personnelModel.setRowCount(0);
        String sql = "SELECT personnel_id,name,role,department,contact_info FROM Personnel ORDER BY personnel_id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) personnelModel.addRow(new Object[]{
                    rs.getInt("personnel_id"), rs.getString("name"), rs.getString("role"),
                    rs.getString("department"), rs.getString("contact_info")});
        } catch (SQLException ex) { showError(this, "Could not load personnel:\n" + ex.getMessage()); }
    }

    private void loadPersonnelEdit(int id, JTextField name, JTextField role,
                                   JTextField dept, JTextField contact) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT name,role,department,contact_info FROM Personnel WHERE personnel_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name.setText(rs.getString("name")); role.setText(rs.getString("role"));
                    dept.setText(rs.getString("department")); contact.setText(rs.getString("contact_info"));
                }
            }
        } catch (SQLException ex) { showError(this, "Could not load personnel:\n" + ex.getMessage()); }
    }

    private void insertPersonnel(String name, String role, String dept, String contact) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Personnel(name,role,department,contact_info) VALUES(?,?,?,?)")) {
            ps.setString(1, name.trim()); ps.setString(2, role.trim()); ps.setString(3, dept.trim()); ps.setString(4, contact.trim());
            ps.executeUpdate(); loadPersonnel(); showInfo(this, "Personnel added successfully.");
        } catch (SQLException ex) { showError(this, "Could not add personnel:\n" + ex.getMessage()); }
    }

    private void updatePersonnel(int id, String name, String role, String dept, String contact) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Personnel SET name=?,role=?,department=?,contact_info=? WHERE personnel_id=?")) {
            ps.setString(1, name.trim()); ps.setString(2, role.trim()); ps.setString(3, dept.trim()); ps.setString(4, contact.trim()); ps.setInt(5, id);
            ps.executeUpdate(); loadPersonnel(); loadIncidents(); showInfo(this, "Personnel updated successfully.");
        } catch (SQLException ex) { showError(this, "Could not update personnel:\n" + ex.getMessage()); }
    }

    private void deletePersonnel() {
        int r = personnelTable.getSelectedRow();
        if (r < 0) { showInfo(this, "Select personnel first."); return; }
        int mr = personnelTable.convertRowIndexToModel(r);
        int id = (int) personnelModel.getValueAt(mr, 0);
        int references = countReferences("SELECT COUNT(*) FROM Incident WHERE personnel_id=?", id);
        if (references > 0) {
            showError(this, "This person is assigned to " + references + " incident(s).\n"
                    + "Update those incidents before deleting them."); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete the selected personnel?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Personnel WHERE personnel_id=?")) {
            ps.setInt(1, id); ps.executeUpdate(); loadPersonnel(); showInfo(this, "Personnel deleted successfully.");
        } catch (SQLException ex) { showError(this, "Could not delete personnel:\n" + ex.getMessage()); }
    }

    // =============================================================
    // REPORTS
    // =============================================================
    private JPanel buildReports() {
        JPanel page = page("Report Center",
                "Use your MySQL View, Stored Procedure and reporting queries");
        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15)); grid.setOpaque(false);
        grid.add(reportCard("INCIDENTS BY SEVERITY",
                "Runs the existing GetIncidentsBySeverity stored procedure.", this::severityReport));
        grid.add(reportCard("UNRESOLVED INCIDENTS",
                "Shows Reported and Under Investigation incidents.", this::unresolvedReport));
        grid.add(reportCard("COMPLETE INCIDENT REPORT",
                "Reads the existing Incident_Report view.", this::completeReport));
        grid.add(reportCard("SYSTEM INCIDENT SUMMARY",
                "Groups incident counts by affected system.", this::systemSummaryReport));
        page.add(grid, BorderLayout.CENTER); return page;
    }

    private JPanel reportCard(String title, String description, Runnable action) {
        JPanel p = new JPanel(new BorderLayout(0, 12)); p.setBackground(PANEL);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(20, 20, 20, 20)));
        JLabel t = new JLabel(title); t.setForeground(ACCENT); t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel d = new JLabel("<html><div style='width:310px'>" + description + "</div></html>");
        d.setForeground(MUTED); d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JButton b = accentButton("View Report"); b.addActionListener(e -> action.run());
        p.add(t, BorderLayout.NORTH); p.add(d, BorderLayout.CENTER); p.add(b, BorderLayout.SOUTH); return p;
    }

    private void severityReport() {
        JComboBox<String> box = combo(new String[]{"Low", "Medium", "High", "Critical"});
        JPanel p = new JPanel(new BorderLayout(8, 8)); p.setBackground(BG);
        JLabel l = new JLabel("Select severity:"); l.setForeground(TEXT); p.add(l, BorderLayout.WEST); p.add(box, BorderLayout.CENTER);
        int ok = JOptionPane.showConfirmDialog(this, p, "Incidents by Severity",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        try (CallableStatement cs = conn.prepareCall("{CALL GetIncidentsBySeverity(?)}")) {
            cs.setString(1, String.valueOf(box.getSelectedItem()));
            try (ResultSet rs = cs.executeQuery()) {
                showResultSet("Incidents by Severity: " + box.getSelectedItem(), rs);
            }
        } catch (SQLException ex) { showError(this, "Could not run stored procedure:\n" + ex.getMessage()); }
    }

    private void unresolvedReport() {
        runReport("Unresolved Incidents",
                "SELECT incident_id,title,attack_type,severity,status,assigned_personnel,affected_system,reported_date "
                + "FROM Incident_Report WHERE status IN ('Reported','Under Investigation') "
                + "ORDER BY reported_date DESC");
    }

    private void completeReport() {
        runReport("Complete Incident Report",
                "SELECT incident_id,title,description,reported_date,attack_type,severity,status,"
                + "assigned_personnel,affected_system FROM Incident_Report ORDER BY incident_id DESC");
    }

    private void systemSummaryReport() {
        runReport("System Incident Summary",
                "SELECT a.system_name AS affected_system, COUNT(i.incident_id) AS incident_count "
                + "FROM Affected_System a LEFT JOIN Incident i ON a.system_id=i.system_id "
                + "GROUP BY a.system_id,a.system_name ORDER BY incident_count DESC,a.system_name");
    }

    private void runReport(String title, String sql) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            showResultSet(title, rs);
        } catch (SQLException ex) { showError(this, "Could not run report:\n" + ex.getMessage()); }
    }

    private void showResultSet(String title, ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData(); int n = md.getColumnCount();
        String[] cols = new String[n];
        for (int i = 1; i <= n; i++) cols[i - 1] = md.getColumnLabel(i);
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        while (rs.next()) {
            Object[] row = new Object[n];
            for (int i = 1; i <= n; i++) row[i - 1] = rs.getObject(i);
            model.addRow(row);
        }
        JTable table = styledTable(model); table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JDialog d = new JDialog(this, title, true); d.setSize(1200, 620); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(BG); p.setBorder(new EmptyBorder(15,15,15,15));
        JLabel heading = new JLabel(title + "  •  " + model.getRowCount() + " record(s)");
        heading.setForeground(TEXT); heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JButton close = secondaryButton("Close"); close.addActionListener(e -> d.dispose());
        p.add(heading, BorderLayout.NORTH); p.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setOpaque(false); bottom.add(close); p.add(bottom, BorderLayout.SOUTH);
        d.add(p); d.setVisible(true);
    }

    // =============================================================
    // DATA LOADING
    // =============================================================
    private void loadAllTables() {
        loadIncidents();
        loadSystems();
        loadPersonnel();
        loadDashboard();
    }

    private void loadIncidents() {
        if (incidentModel == null) return;
        incidentModel.setRowCount(0);
        String sql = "SELECT i.incident_id,i.title,it.type_name AS attack_type,s.severity_name AS severity,"
                + "st.status_name AS status,p.name AS personnel,a.system_name AS affected_system,i.reported_date "
                + "FROM Incident i "
                + "LEFT JOIN Incident_Type it ON i.type_id=it.type_id "
                + "LEFT JOIN Severity s ON i.severity_id=s.severity_id "
                + "LEFT JOIN Status st ON i.status_id=st.status_id "
                + "LEFT JOIN Personnel p ON i.personnel_id=p.personnel_id "
                + "LEFT JOIN Affected_System a ON i.system_id=a.system_id "
                + "ORDER BY i.incident_id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) incidentModel.addRow(new Object[]{
                    rs.getInt("incident_id"), rs.getString("title"), rs.getString("attack_type"),
                    rs.getString("severity"), rs.getString("status"), rs.getString("personnel"),
                    rs.getString("affected_system"), rs.getString("reported_date")});
        } catch (SQLException ex) { showError(this, "Could not load incidents:\n" + ex.getMessage()); }
        if (incidentSorter != null) incidentSorter.setRowFilter(null);
    }

    private void loadDashboard() {
        if (conn == null || totalLabel == null) return;
        String sql = "SELECT COUNT(*) AS total_count, "
                + "SUM(CASE WHEN s.severity_name='High' THEN 1 ELSE 0 END) AS high_count, "
                + "SUM(CASE WHEN s.severity_name='Critical' THEN 1 ELSE 0 END) AS critical_count, "
                + "SUM(CASE WHEN st.status_name IN ('Reported','Under Investigation') THEN 1 ELSE 0 END) AS open_count "
                + "FROM Incident i LEFT JOIN Severity s ON i.severity_id=s.severity_id "
                + "LEFT JOIN Status st ON i.status_id=st.status_id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                totalLabel.setText(String.valueOf(rs.getInt("total_count")));
                highLabel.setText(String.valueOf(rs.getInt("high_count")));
                criticalLabel.setText(String.valueOf(rs.getInt("critical_count")));
                openLabel.setText(String.valueOf(rs.getInt("open_count")));
            }
        } catch (SQLException ex) { showError(this, "Could not load dashboard:\n" + ex.getMessage()); }

        recentModel.setRowCount(0);
        String recent = "SELECT i.incident_id,i.title,s.severity_name,st.status_name,i.reported_date "
                + "FROM Incident i LEFT JOIN Severity s ON i.severity_id=s.severity_id "
                + "LEFT JOIN Status st ON i.status_id=st.status_id "
                + "ORDER BY i.reported_date DESC,i.incident_id DESC LIMIT 8";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(recent)) {
            while (rs.next()) recentModel.addRow(new Object[]{
                    rs.getInt("incident_id"), rs.getString("title"), rs.getString("severity_name"),
                    rs.getString("status_name"), rs.getString("reported_date")});
        } catch (SQLException ex) { showError(this, "Could not load recent incidents:\n" + ex.getMessage()); }
    }

    private void loadCombo(JComboBox<IdName> combo, String sql) {
        combo.removeAllItems();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) combo.addItem(new IdName(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ex) { showError(this, "Could not load dropdown data:\n" + ex.getMessage()); }
    }

    private void selectId(JComboBox<IdName> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private int countReferences(String sql, int id) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            showError(this, "Could not check references:\n" + ex.getMessage()); return -1;
        }
    }

    // =============================================================
    // COMMON UI HELPERS
    // =============================================================
    private JPanel page(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG); p.setBorder(new EmptyBorder(25, 28, 25, 28));
        JPanel heading = new JPanel(); heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setForeground(TEXT); t.setFont(new Font("Segoe UI", Font.BOLD, 27));
        JLabel s = new JLabel(subtitle); s.setForeground(MUTED); s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heading.add(t); heading.add(Box.createVerticalStrut(4)); heading.add(s); p.add(heading, BorderLayout.NORTH); return p;
    }

    private JPanel titledPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(PANEL);
        p.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(BORDER), title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), TEXT),
                new EmptyBorder(10,10,10,10)));
        return p;
    }

    private JPanel dialogPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 14)); p.setBackground(BG); p.setBorder(new EmptyBorder(22,25,22,25)); return p;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(PANEL);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(18,18,18,18))); return p;
    }

    private GridBagConstraints formConstraints() {
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7,7,7,7); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; return g;
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row, String text, Component c) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(text); l.setForeground(MUTED); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); p.add(l, g);
        g.gridx = 1; g.weightx = 1; p.add(c, g);
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model); t.setBackground(PANEL); t.setForeground(TEXT); t.setGridColor(BORDER);
        t.setSelectionBackground(new Color(35,83,111)); t.setSelectionForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12)); t.setRowHeight(30); t.setShowVerticalLines(false);
        JTableHeader h = t.getTableHeader(); h.setBackground(PANEL2); h.setForeground(TEXT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 11)); h.setPreferredSize(new Dimension(0,37)); return t;
    }

    private JLabel label(String text) { JLabel l = new JLabel(text); l.setForeground(MUTED); l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); return l; }

    private JComboBox<String> combo(String[] items) { JComboBox<String> c = new JComboBox<>(items); styleCombo(c); return c; }

    private void styleTextField(JTextField f) {
        f.setBackground(PANEL2); f.setForeground(TEXT); f.setCaretColor(TEXT); f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(8,10,8,10)));
    }

    private void styleTextArea(JTextArea a) {
        a.setBackground(PANEL2); a.setForeground(TEXT); a.setCaretColor(TEXT); a.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        a.setLineWrap(true); a.setWrapStyleWord(true); a.setBorder(new EmptyBorder(8,10,8,10));
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(PANEL2);
        c.setForeground(TEXT);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setFocusable(false);
        c.setOpaque(true);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(2, 6, 2, 6)));
        c.setRenderer(new DarkComboRenderer());
    }

    private JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10,15,10,15));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PANEL2);
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(8,13,8,13)));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(103,38,41));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10,13,10,13));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void showError(Component parent, String message) { JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showInfo(Component parent, String message) { JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE); }
    private boolean validDate(String s) { try { Date.valueOf(s); return true; } catch (IllegalArgumentException e) { return false; } }

    // =============================================================
    // STARTUP / JDBC
    // =============================================================
    private static String askPassword() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(260, 30));
        JPanel p = new JPanel(new BorderLayout(8,8));
        JLabel l = new JLabel("MySQL password for user 'root':"); l.setForeground(TEXT);
        p.add(l, BorderLayout.NORTH); p.add(field, BorderLayout.CENTER);
        int result = JOptionPane.showConfirmDialog(null, p, "MySQL Database Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION ? new String(field.getPassword()) : null;
    }

    private static Connection connect(String password) throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector/J not found. Keep the connector .jar in the project's lib folder.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, password);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", BG);
            UIManager.put("info", PANEL);
            UIManager.put("nimbusBase", new Color(35, 45, 57));
            UIManager.put("nimbusBlueGrey", new Color(55, 68, 83));
            UIManager.put("nimbusLightBackground", PANEL2);
            UIManager.put("text", TEXT);
            UIManager.put("ComboBox.background", PANEL2);
            UIManager.put("ComboBox.foreground", TEXT);
            UIManager.put("ComboBox.selectionBackground", new Color(35, 83, 111));
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("Button.foreground", TEXT);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            String password = askPassword();
            if (password == null) return;
            try {
                Connection c = connect(password);
                CyberIncidentManagementGUI app = new CyberIncidentManagementGUI(c);
                app.setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "MySQL connection failed.\n\n" + ex.getMessage()
                                + "\n\nCheck that MySQL is running, the database exists, and your password is correct.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static class DarkComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(new EmptyBorder(6, 8, 6, 8));
            setBackground(isSelected ? new Color(35, 83, 111) : PANEL2);
            setForeground(TEXT);
            return this;
        }
    }

    private static class IdName {
        final int id;
        final String name;
        IdName(int id, String name) { this.id = id; this.name = name; }
        public String toString() { return name; }
    }
}
