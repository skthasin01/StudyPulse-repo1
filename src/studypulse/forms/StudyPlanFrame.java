package studypulse.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Week view of the study plan (Mon..Sun) with plan generation.
 * tblWeek has one row; each cell summarises one day. The bottom table lists the blocks
 * of the selected day. Plans are sample data kept per week while the app runs.
 */
public class StudyPlanFrame extends javax.swing.JFrame {

    private static final String[] DAY_COLUMNS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final int COL_STATUS = 4;
    private static final String PLANNED = "Planned";
    private static final String DONE = "Done";
    private static final String SKIPPED = "Skipped";
    private static final DateTimeFormatter RANGE_DAY = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter RANGE_END = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_TITLE_FORMAT = DateTimeFormatter.ofPattern("EEEE dd MMM", Locale.ENGLISH);

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color NAVY = new Color(34, 48, 63);
    private static final Color ACCENT = new Color(46, 125, 154);
    private static final Color SUCCESS = new Color(46, 158, 98);
    private static final Color TODAY = new Color(232, 243, 247);

    /** Monday -> 7 lists of blocks {id, task, difficulty, minutes, status}. */
    private static final Map<LocalDate, List<List<Object[]>>> PLANS = new HashMap<LocalDate, List<List<Object[]>>>();

    private LocalDate monday = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
    private DefaultTableModel weekModel;
    private DefaultTableModel blocksModel;
    private int selectedDay = LocalDate.now().getDayOfWeek().getValue() - 1;

    /**
     * Creates new form StudyPlanFrame
     */
    public StudyPlanFrame() {
        initComponents();
        Sidebar.attach(this, pnlSidebar, Sidebar.STUDY_PLAN);
        applyColors();
        setupWeekTable();
        setupBlocksTable();
        if (!PLANS.containsKey(monday)) {
            PLANS.put(monday, samplePlan());
        }
        loadWeek();
        tblWeek.setRowHeight(Math.max(120, scrWeek.getViewport().getHeight()));
        setLocationRelativeTo(null);
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        btnGeneratePlan.setBackground(ACCENT);
        btnGeneratePlan.setForeground(Color.WHITE);
        btnMarkBlockDone.setBackground(SUCCESS);
        btnMarkBlockDone.setForeground(Color.WHITE);
        for (javax.swing.JButton b : new javax.swing.JButton[]{btnGeneratePlan, btnMarkBlockDone, btnSkipBlock,
            btnMyFreeTime, btnPrevWeek, btnNextWeek}) {
            b.setFocusPainted(false);
        }
        btnPrevWeek.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnNextWeek.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPrevWeek.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        btnNextWeek.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
    }

    private void setupWeekTable() {
        weekModel = new DefaultTableModel(DAY_COLUMNS, 1) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblWeek.setModel(weekModel);
        tblWeek.setCellSelectionEnabled(true);
        tblWeek.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblWeek.getTableHeader().setReorderingAllowed(false);
        tblWeek.getTableHeader().setBackground(NAVY);
        tblWeek.getTableHeader().setForeground(Color.WHITE);
        tblWeek.setGridColor(new Color(220, 226, 232));
        tblWeek.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setVerticalAlignment(SwingConstants.TOP);
                if (!isSelected) {
                    setBackground(monday.plusDays(column).equals(LocalDate.now()) ? TODAY : Color.WHITE);
                    setForeground(NAVY);
                }
                return this;
            }
        });
        // one row that fills the visible height
        scrWeek.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                tblWeek.setRowHeight(Math.max(120, scrWeek.getViewport().getHeight()));
            }
        });
        ListSelectionListener dayListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && tblWeek.getSelectedColumn() >= 0) {
                    selectedDay = tblWeek.getSelectedColumn();
                    showDayBlocks();
                }
            }
        };
        tblWeek.getColumnModel().getSelectionModel().addListSelectionListener(dayListener);
    }

    private void setupBlocksTable() {
        blocksModel = (DefaultTableModel) tblDayBlocks.getModel();
        tblDayBlocks.setRowHeight(26);
        tblDayBlocks.setFillsViewportHeight(true);
        tblDayBlocks.setGridColor(new Color(230, 234, 238));
        tblDayBlocks.getTableHeader().setBackground(NAVY);
        tblDayBlocks.getTableHeader().setForeground(Color.WHITE);
        tblDayBlocks.removeColumn(tblDayBlocks.getColumnModel().getColumn(0));
        tblDayBlocks.getColumnModel().getColumn(0).setPreferredWidth(260);
        tblDayBlocks.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateBlockButtons();
            }
        });
    }

    private static List<List<Object[]>> samplePlan() {
        String[][] tasks = {
            {"Database Assignment", "Hard"}, {"Java Project – GUI", "Medium"},
            {"Statistics Exam Prep", "Hard"}, {"Networking Lab Report", "Easy"},
        };
        int[][] dayTasks = {{0, 1}, {2, 0}, {1, 3}, {2}, {0, 2}, {1, 3}, {}};
        int[][] dayMinutes = {{90, 60}, {60, 45}, {90, 30}, {120}, {60, 45}, {90, 60}, {}};
        List<List<Object[]>> week = new ArrayList<List<Object[]>>();
        int id = 1;
        for (int d = 0; d < 7; d++) {
            List<Object[]> blocks = new ArrayList<Object[]>();
            for (int i = 0; i < dayTasks[d].length; i++) {
                String[] task = tasks[dayTasks[d][i]];
                blocks.add(new Object[]{id++, task[0], task[1], dayMinutes[d][i], PLANNED});
            }
            week.add(blocks);
        }
        return week;
    }

    private void loadWeek() {
        lblWeekRange.setText(monday.format(RANGE_DAY) + " – " + monday.plusDays(6).format(RANGE_END));
        List<List<Object[]>> week = PLANS.get(monday);
        for (int d = 0; d < 7; d++) {
            weekModel.setValueAt(daySummary(monday.plusDays(d), week == null ? null : week.get(d)), 0, d);
        }
        if (tblWeek.getRowCount() > 0) {
            tblWeek.changeSelection(0, selectedDay, false, false);
        }
        showDayBlocks();
    }

    private static String daySummary(LocalDate date, List<Object[]> blocks) {
        StringBuilder html = new StringBuilder("<html><div style='padding:4px'><b>")
                .append(date.getDayOfMonth()).append("</b><br><br>");
        if (blocks == null || blocks.isEmpty()) {
            html.append("<span style='color:#6b7785'>").append(blocks == null ? "No plan" : "Rest day").append("</span>");
        } else {
            int minutes = 0;
            for (Object[] b : blocks) {
                minutes += (Integer) b[3];
                String color = DONE.equals(b[COL_STATUS]) ? "#2e9e62" : (SKIPPED.equals(b[COL_STATUS]) ? "#d64545" : "#2e7d9a");
                html.append("<span style='color:").append(color).append("'>• ").append(b[1]).append("</span><br>");
            }
            html.append("<br><span style='color:#6b7785'>").append(formatMinutes(minutes)).append("</span>");
        }
        return html.append("</div></html>").toString();
    }

    private void showDayBlocks() {
        blocksModel.setRowCount(0);
        LocalDate day = monday.plusDays(selectedDay);
        lblDayTitle.setText("Blocks on " + day.format(DAY_TITLE_FORMAT));
        List<List<Object[]>> week = PLANS.get(monday);
        if (week != null) {
            for (Object[] b : week.get(selectedDay)) {
                blocksModel.addRow(new Object[]{b[0], b[1], b[2], formatMinutes((Integer) b[3]), b[COL_STATUS]});
            }
        }
        updateBlockButtons();
    }

    private void updateBlockButtons() {
        Object[] block = selectedBlock();
        boolean planned = block != null && PLANNED.equals(block[COL_STATUS]);
        btnMarkBlockDone.setEnabled(planned);
        btnSkipBlock.setEnabled(planned);
    }

    private Object[] selectedBlock() {
        int row = tblDayBlocks.getSelectedRow();
        List<List<Object[]>> week = PLANS.get(monday);
        if (row < 0 || week == null) {
            return null;
        }
        return week.get(selectedDay).get(tblDayBlocks.convertRowIndexToModel(row));
    }

    private void changeWeek(int weeks) {
        monday = monday.plusWeeks(weeks);
        loadWeek();
    }

    private void openFreeTime() {
        AvailabilityFrame frame = new AvailabilityFrame();
        frame.setOnSaved(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(StudyPlanFrame.this, "Free time saved. Generate the plan again to use it.",
                        "Study plan", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        frame.setVisible(true);
    }

    private void generatePlan() {
        if (ChronoUnit.DAYS.between(LocalDate.now(), monday.plusDays(6)) < 0) {
            showWarning("This week is over. Plans can only be generated for this week or later.");
            return;
        }
        if (PLANS.containsKey(monday)) {
            int choice = JOptionPane.showConfirmDialog(this, "Replace the plan for this week?", "Study plan",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        PLANS.put(monday, samplePlan());
        loadWeek();
        ToastFrame.showToast("Study plan ready", "Your plan for " + lblWeekRange.getText() + " was generated.", ACCENT);
    }

    private void markBlockDone() {
        setBlockStatus(DONE);
    }

    private void skipBlock() {
        setBlockStatus(SKIPPED);
    }

    private void setBlockStatus(String status) {
        Object[] block = selectedBlock();
        if (block == null) {
            showWarning("Select a block first.");
            return;
        }
        block[COL_STATUS] = status;
        int row = tblDayBlocks.getSelectedRow();
        loadWeek();
        if (row >= 0 && row < tblDayBlocks.getRowCount()) {
            tblDayBlocks.setRowSelectionInterval(row, row);
        }
    }

    private static String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        return minutes / 60 + " h" + (minutes % 60 == 0 ? "" : " " + minutes % 60 + " min");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Study plan", JOptionPane.WARNING_MESSAGE);
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
        lblTitle = new javax.swing.JLabel();
        btnPrevWeek = new javax.swing.JButton();
        lblWeekRange = new javax.swing.JLabel();
        btnNextWeek = new javax.swing.JButton();
        btnMyFreeTime = new javax.swing.JButton();
        btnGeneratePlan = new javax.swing.JButton();
        scrWeek = new javax.swing.JScrollPane();
        tblWeek = new javax.swing.JTable();
        lblDayTitle = new javax.swing.JLabel();
        btnSkipBlock = new javax.swing.JButton();
        btnMarkBlockDone = new javax.swing.JButton();
        scrDayBlocks = new javax.swing.JScrollPane();
        tblDayBlocks = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StudyPulse – Study Plan");

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

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblTitle.setText("Study Plan");

        btnPrevWeek.setText("<");
        btnPrevWeek.setToolTipText("Previous week");
        btnPrevWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevWeekActionPerformed(evt);
            }
        });

        lblWeekRange.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblWeekRange.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWeekRange.setText("02 Nov – 08 Nov 2026");

        btnNextWeek.setText(">");
        btnNextWeek.setToolTipText("Next week");
        btnNextWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextWeekActionPerformed(evt);
            }
        });

        btnMyFreeTime.setText("My Free Time");
        btnMyFreeTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMyFreeTimeActionPerformed(evt);
            }
        });

        btnGeneratePlan.setText("Generate Plan");
        btnGeneratePlan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGeneratePlanActionPerformed(evt);
            }
        });

        tblWeek.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrWeek.setViewportView(tblWeek);

        lblDayTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDayTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblDayTitle.setText("Blocks on");

        btnSkipBlock.setText("Skip Block");
        btnSkipBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkipBlockActionPerformed(evt);
            }
        });

        btnMarkBlockDone.setText("Mark Block Done");
        btnMarkBlockDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkBlockDoneActionPerformed(evt);
            }
        });

        tblDayBlocks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Task", "Difficulty", "Planned", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrDayBlocks.setViewportView(tblDayBlocks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(24, 24, 24)
                        .addComponent(btnPrevWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblWeekRange, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNextWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMyFreeTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGeneratePlan))
                    .addComponent(scrWeek, javax.swing.GroupLayout.DEFAULT_SIZE, 1008, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDayTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSkipBlock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkBlockDone))
                    .addComponent(scrDayBlocks, javax.swing.GroupLayout.DEFAULT_SIZE, 1008, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(btnPrevWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWeekRange)
                    .addComponent(btnNextWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMyFreeTime, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGeneratePlan, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrWeek, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDayTitle)
                    .addComponent(btnSkipBlock, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMarkBlockDone, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrDayBlocks, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        setSize(new java.awt.Dimension(1200, 720));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void btnPrevWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevWeekActionPerformed
        changeWeek(-1);
    }//GEN-LAST:event_btnPrevWeekActionPerformed

    private void btnNextWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextWeekActionPerformed
        changeWeek(1);
    }//GEN-LAST:event_btnNextWeekActionPerformed

    private void btnMyFreeTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMyFreeTimeActionPerformed
        openFreeTime();
    }//GEN-LAST:event_btnMyFreeTimeActionPerformed

    private void btnGeneratePlanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGeneratePlanActionPerformed
        generatePlan();
    }//GEN-LAST:event_btnGeneratePlanActionPerformed

    private void btnSkipBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkipBlockActionPerformed
        skipBlock();
    }//GEN-LAST:event_btnSkipBlockActionPerformed

    private void btnMarkBlockDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkBlockDoneActionPerformed
        markBlockDone();
    }//GEN-LAST:event_btnMarkBlockDoneActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGeneratePlan;
    private javax.swing.JButton btnMarkBlockDone;
    private javax.swing.JButton btnMyFreeTime;
    private javax.swing.JButton btnNextWeek;
    private javax.swing.JButton btnPrevWeek;
    private javax.swing.JButton btnSkipBlock;
    private javax.swing.JLabel lblDayTitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblWeekRange;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrDayBlocks;
    private javax.swing.JScrollPane scrWeek;
    private javax.swing.JTable tblDayBlocks;
    private javax.swing.JTable tblWeek;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new StudyPlanFrame().setVisible(true);
        });
    }
}
