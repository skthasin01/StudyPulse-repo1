package studypulse.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import studypulse.Main;

/**
 * Home screen: summary cards, today's plan, upcoming deadlines and the 7-day risk chart.
 * The numbers are sample data; there is no database behind this UI.
 */
public class DashboardFrame extends javax.swing.JFrame {

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color NAVY = new Color(34, 48, 63);
    private static final Color ACCENT = new Color(46, 125, 154);
    private static final Color SUCCESS = new Color(46, 158, 98);
    private static final Color WARNING = new Color(230, 145, 56);
    private static final Color DANGER = new Color(214, 69, 69);
    private static final Color ROW_DONE = new Color(232, 245, 237);
    private static final Color ROW_URGENT = new Color(252, 234, 234);
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DEADLINE = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

    private DefaultTableModel planModel;
    private DefaultTableModel upcomingModel;

    /**
     * Creates new form DashboardFrame
     */
    public DashboardFrame() {
        initComponents();
        Sidebar.attach(this, pnlSidebar, Sidebar.DASHBOARD);
        applyColors();
        setupTables();
        loadDashboard();
        setLocationRelativeTo(null);
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        btnBell.setBackground(Color.WHITE);
        btnBell.setForeground(NAVY);
        btnBell.setFocusPainted(false);
        btnBell.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnBell.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        LoginFrame.styleLink(btnRiskDetails, ACCENT);
        btnStartPomodoro.setBackground(ACCENT);
        btnStartPomodoro.setForeground(Color.WHITE);
        btnStartPomodoro.setFocusPainted(false);
        styleCard(pnlCardLoad, WARNING);
        styleCard(pnlCardRisk, WARNING);
        styleCard(pnlCardDue, ACCENT);
        styleCard(pnlCardHours, SUCCESS);
    }

    private static void styleCard(JPanel card, Color accent) {
        card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, accent));
    }

    private void setupTables() {
        planModel = (DefaultTableModel) tblTodayPlan.getModel();
        upcomingModel = (DefaultTableModel) tblUpcoming.getModel();
        styleTable(tblTodayPlan);
        styleTable(tblUpcoming);
        tblTodayPlan.getColumnModel().getColumn(0).setPreferredWidth(260);
        tblUpcoming.getColumnModel().getColumn(0).setPreferredWidth(220);

        // Done blocks green, deadlines within 3 days red
        tblTodayPlan.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    boolean done = "Done".equals(planModel.getValueAt(table.convertRowIndexToModel(row), 3));
                    setBackground(done ? ROW_DONE : Color.WHITE);
                }
                return this;
            }
        });
        tblUpcoming.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String left = (String) upcomingModel.getValueAt(table.convertRowIndexToModel(row), 3);
                    setBackground(left.startsWith("1 ") || left.startsWith("2 ") ? ROW_URGENT : Color.WHITE);
                }
                return this;
            }
        });
    }

    /** Row height, header colours and the hidden ID column (model column 0). */
    private static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(230, 234, 238));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.removeColumn(table.getColumnModel().getColumn(0));
    }

    /** Fills the screen with sample data. */
    private void loadDashboard() {
        LocalDate today = LocalDate.now();
        lblWelcome.setText("Welcome, " + firstName(Main.userName));
        lblToday.setText(today.format(LONG_DATE));
        refreshBell();

        lblLoadValue.setText("46");
        lblLoadValue.setForeground(WARNING);
        lblLoadLevel.setText("Moderate · next 7 days");
        lblLoadLevel.setForeground(WARNING);

        lblRiskValue.setText("38");
        lblRiskValue.setForeground(WARNING);
        lblRiskLevel.setText("Medium");
        lblRiskLevel.setForeground(WARNING);

        lblDueValue.setText("4");
        lblDueNext.setText("Next: Database Assignment (2 days)");

        lblHoursValue.setText("12.5 h");
        lblHoursDiff.setText("+2.0 h vs last week");
        lblHoursDiff.setForeground(SUCCESS);

        planModel.setRowCount(0);
        planModel.addRow(new Object[]{1, "Database Assignment", "1 h 30 min", "Planned"});
        planModel.addRow(new Object[]{2, "Java Project – GUI", "1 h", "Done"});
        planModel.addRow(new Object[]{3, "Statistics Exam Prep", "45 min", "Planned"});

        upcomingModel.setRowCount(0);
        upcomingModel.addRow(new Object[]{1, "Database Assignment", today.plusDays(2).format(DEADLINE) + " 23:59", "2 days"});
        upcomingModel.addRow(new Object[]{4, "Statistics Exam", today.plusDays(6).format(DEADLINE) + " 09:00", "6 days"});
        upcomingModel.addRow(new Object[]{5, "Java Project", today.plusDays(10).format(DEADLINE) + " 23:59", "10 days"});
        upcomingModel.addRow(new Object[]{6, "Networking Lab Report", today.plusDays(14).format(DEADLINE) + " 17:00", "14 days"});

        String[] days = new String[7];
        for (int i = 0; i < 7; i++) {
            days[i] = today.minusDays(6 - i).format(DAY);
        }
        pnlRiskChart.removeAll();
        pnlRiskChart.add(new SimpleChart("Burnout risk – last 7 days", days, true)
                .addSeries("Risk score", new double[]{22, 28, 31, 35, 30, 41, 38}, DANGER), BorderLayout.CENTER);
        pnlRiskChart.revalidate();
        pnlRiskChart.repaint();
    }

    private void openRiskReport() {
        new RiskReportFrame().setVisible(true);
    }

    /** Opens the Pomodoro timer with the selected (or first planned) task of today's plan. */
    private void startPomodoro() {
        int row = tblTodayPlan.getSelectedRow();
        String task = row >= 0 ? (String) planModel.getValueAt(tblTodayPlan.convertRowIndexToModel(row), 1)
                : (String) planModel.getValueAt(0, 1);
        PomodoroFrame.showWindow(task);
    }

    private void refreshBell() {
        int unread = NotificationFrame.unreadCount;
        btnBell.setText(String.valueOf(unread));
        btnBell.setToolTipText(unread + " unread notification" + (unread == 1 ? "" : "s"));
        btnBell.setForeground(unread > 0 ? DANGER : NAVY);
    }

    private static String firstName(String fullName) {
        String name = fullName == null ? "" : fullName.trim();
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        lblToday = new javax.swing.JLabel();
        btnBell = new javax.swing.JButton();
        pnlCardLoad = new javax.swing.JPanel();
        lblLoadTitle = new javax.swing.JLabel();
        lblLoadValue = new javax.swing.JLabel();
        lblLoadLevel = new javax.swing.JLabel();
        pnlCardRisk = new javax.swing.JPanel();
        lblRiskTitle = new javax.swing.JLabel();
        lblRiskValue = new javax.swing.JLabel();
        lblRiskLevel = new javax.swing.JLabel();
        btnRiskDetails = new javax.swing.JButton();
        pnlCardDue = new javax.swing.JPanel();
        lblDueTitle = new javax.swing.JLabel();
        lblDueValue = new javax.swing.JLabel();
        lblDueNext = new javax.swing.JLabel();
        pnlCardHours = new javax.swing.JPanel();
        lblHoursTitle = new javax.swing.JLabel();
        lblHoursValue = new javax.swing.JLabel();
        lblHoursDiff = new javax.swing.JLabel();
        lblTodayPlanTitle = new javax.swing.JLabel();
        btnStartPomodoro = new javax.swing.JButton();
        lblUpcomingTitle = new javax.swing.JLabel();
        scrTodayPlan = new javax.swing.JScrollPane();
        tblTodayPlan = new javax.swing.JTable();
        scrUpcoming = new javax.swing.JScrollPane();
        tblUpcoming = new javax.swing.JTable();
        pnlRiskChart = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StudyPulse – Dashboard");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        pnlSidebar.setBackground(new java.awt.Color(34, 48, 63));

        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 720, Short.MAX_VALUE)
        );

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(34, 48, 63));
        lblWelcome.setText("Welcome, Student");

        lblToday.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblToday.setForeground(new java.awt.Color(107, 119, 133));
        lblToday.setText("Today");

        btnBell.setToolTipText("Notifications");
        btnBell.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBellActionPerformed(evt);
            }
        });

        pnlCardLoad.setBackground(new java.awt.Color(255, 255, 255));

        lblLoadTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoadTitle.setForeground(new java.awt.Color(107, 119, 133));
        lblLoadTitle.setText("Study Load Index");

        lblLoadValue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblLoadValue.setForeground(new java.awt.Color(34, 48, 63));
        lblLoadValue.setText("0");

        lblLoadLevel.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLoadLevel.setText("Light");

        javax.swing.GroupLayout pnlCardLoadLayout = new javax.swing.GroupLayout(pnlCardLoad);
        pnlCardLoad.setLayout(pnlCardLoadLayout);
        pnlCardLoadLayout.setHorizontalGroup(
            pnlCardLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardLoadLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlCardLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLoadTitle)
                    .addComponent(lblLoadValue)
                    .addComponent(lblLoadLevel, 0, 204, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        pnlCardLoadLayout.setVerticalGroup(
            pnlCardLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardLoadLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblLoadTitle)
                .addGap(2, 2, 2)
                .addComponent(lblLoadValue)
                .addGap(2, 2, 2)
                .addComponent(lblLoadLevel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCardRisk.setBackground(new java.awt.Color(255, 255, 255));

        lblRiskTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRiskTitle.setForeground(new java.awt.Color(107, 119, 133));
        lblRiskTitle.setText("Burnout Risk");

        lblRiskValue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblRiskValue.setForeground(new java.awt.Color(34, 48, 63));
        lblRiskValue.setText("0");

        lblRiskLevel.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblRiskLevel.setText("Low");

        btnRiskDetails.setText("View details");
        btnRiskDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRiskDetailsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlCardRiskLayout = new javax.swing.GroupLayout(pnlCardRisk);
        pnlCardRisk.setLayout(pnlCardRiskLayout);
        pnlCardRiskLayout.setHorizontalGroup(
            pnlCardRiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardRiskLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlCardRiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRiskTitle)
                    .addComponent(lblRiskValue)
                    .addGroup(pnlCardRiskLayout.createSequentialGroup()
                        .addComponent(lblRiskLevel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRiskDetails)))
                .addGap(16, 16, 16))
        );
        pnlCardRiskLayout.setVerticalGroup(
            pnlCardRiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardRiskLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblRiskTitle)
                .addGap(2, 2, 2)
                .addComponent(lblRiskValue)
                .addGap(2, 2, 2)
                .addGroup(pnlCardRiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRiskLevel)
                    .addComponent(btnRiskDetails))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCardDue.setBackground(new java.awt.Color(255, 255, 255));

        lblDueTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDueTitle.setForeground(new java.awt.Color(107, 119, 133));
        lblDueTitle.setText("Due this week");

        lblDueValue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblDueValue.setForeground(new java.awt.Color(34, 48, 63));
        lblDueValue.setText("0");

        lblDueNext.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDueNext.setText("No open tasks");

        javax.swing.GroupLayout pnlCardDueLayout = new javax.swing.GroupLayout(pnlCardDue);
        pnlCardDue.setLayout(pnlCardDueLayout);
        pnlCardDueLayout.setHorizontalGroup(
            pnlCardDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDueLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlCardDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDueTitle)
                    .addComponent(lblDueValue)
                    .addComponent(lblDueNext, 0, 204, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        pnlCardDueLayout.setVerticalGroup(
            pnlCardDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDueLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblDueTitle)
                .addGap(2, 2, 2)
                .addComponent(lblDueValue)
                .addGap(2, 2, 2)
                .addComponent(lblDueNext)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCardHours.setBackground(new java.awt.Color(255, 255, 255));

        lblHoursTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblHoursTitle.setForeground(new java.awt.Color(107, 119, 133));
        lblHoursTitle.setText("Study hours this week");

        lblHoursValue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblHoursValue.setForeground(new java.awt.Color(34, 48, 63));
        lblHoursValue.setText("0.0 h");

        lblHoursDiff.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblHoursDiff.setText("+0.0 h vs last week");

        javax.swing.GroupLayout pnlCardHoursLayout = new javax.swing.GroupLayout(pnlCardHours);
        pnlCardHours.setLayout(pnlCardHoursLayout);
        pnlCardHoursLayout.setHorizontalGroup(
            pnlCardHoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardHoursLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlCardHoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHoursTitle)
                    .addComponent(lblHoursValue)
                    .addComponent(lblHoursDiff, 0, 204, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        pnlCardHoursLayout.setVerticalGroup(
            pnlCardHoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardHoursLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblHoursTitle)
                .addGap(2, 2, 2)
                .addComponent(lblHoursValue)
                .addGap(2, 2, 2)
                .addComponent(lblHoursDiff)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblTodayPlanTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTodayPlanTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblTodayPlanTitle.setText("Today's plan");

        btnStartPomodoro.setText("Start Pomodoro");
        btnStartPomodoro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartPomodoroActionPerformed(evt);
            }
        });

        lblUpcomingTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblUpcomingTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblUpcomingTitle.setText("Upcoming deadlines");

        tblTodayPlan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Task", "Planned", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrTodayPlan.setViewportView(tblTodayPlan);

        tblUpcoming.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Task", "Deadline", "Left"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrUpcoming.setViewportView(tblUpcoming);

        pnlRiskChart.setBackground(new java.awt.Color(255, 255, 255));
        pnlRiskChart.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblWelcome)
                            .addComponent(lblToday))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBell, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlCardLoad, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlCardRisk, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlCardDue, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlCardHours, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblTodayPlanTitle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnStartPomodoro))
                            .addComponent(scrTodayPlan, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE))
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUpcomingTitle)
                            .addComponent(scrUpcoming, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)))
                    .addComponent(pnlRiskChart, javax.swing.GroupLayout.DEFAULT_SIZE, 992, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblWelcome)
                        .addGap(2, 2, 2)
                        .addComponent(lblToday))
                    .addComponent(btnBell, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCardLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlCardRisk, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlCardDue, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlCardHours, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTodayPlanTitle)
                    .addComponent(btnStartPomodoro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpcomingTitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrTodayPlan, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(scrUpcoming, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addComponent(pnlRiskChart, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        setSize(new java.awt.Dimension(1200, 720));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        refreshBell();
    }//GEN-LAST:event_formWindowActivated

    private void btnBellActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBellActionPerformed
        Sidebar.navigate(this, Sidebar.NOTIFICATIONS);
    }//GEN-LAST:event_btnBellActionPerformed

    private void btnRiskDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRiskDetailsActionPerformed
        openRiskReport();
    }//GEN-LAST:event_btnRiskDetailsActionPerformed

    private void btnStartPomodoroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartPomodoroActionPerformed
        startPomodoro();
    }//GEN-LAST:event_btnStartPomodoroActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBell;
    private javax.swing.JButton btnRiskDetails;
    private javax.swing.JButton btnStartPomodoro;
    private javax.swing.JLabel lblDueNext;
    private javax.swing.JLabel lblDueTitle;
    private javax.swing.JLabel lblDueValue;
    private javax.swing.JLabel lblHoursDiff;
    private javax.swing.JLabel lblHoursTitle;
    private javax.swing.JLabel lblHoursValue;
    private javax.swing.JLabel lblLoadLevel;
    private javax.swing.JLabel lblLoadTitle;
    private javax.swing.JLabel lblLoadValue;
    private javax.swing.JLabel lblRiskLevel;
    private javax.swing.JLabel lblRiskTitle;
    private javax.swing.JLabel lblRiskValue;
    private javax.swing.JLabel lblToday;
    private javax.swing.JLabel lblTodayPlanTitle;
    private javax.swing.JLabel lblUpcomingTitle;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlCardDue;
    private javax.swing.JPanel pnlCardHours;
    private javax.swing.JPanel pnlCardLoad;
    private javax.swing.JPanel pnlCardRisk;
    private javax.swing.JPanel pnlRiskChart;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrTodayPlan;
    private javax.swing.JScrollPane scrUpcoming;
    private javax.swing.JTable tblTodayPlan;
    private javax.swing.JTable tblUpcoming;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}
