package studypulse.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Today's Study Load Index and burnout risk with the reasons behind every point,
 * recommendations and the last 14 days. Opened on top of another frame (DISPOSE_ON_CLOSE).
 * pnlLoadGauge / pnlRiskGauge are plain JPanels; the gauge parts are added in showGauge().
 */
public class RiskReportFrame extends javax.swing.JFrame {

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color NAVY = new Color(34, 48, 63);
    private static final Color TEXT_MUTED = new Color(107, 119, 133);
    private static final Color SUCCESS = new Color(46, 158, 98);
    private static final Color WARNING = new Color(230, 145, 56);
    private static final Color DANGER = new Color(214, 69, 69);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.ENGLISH);

    /**
     * Creates new form RiskReportFrame
     */
    public RiskReportFrame() {
        initComponents();
        applyColors();
        loadReport();
        loadHistory();
        setLocationRelativeTo(null);
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        txtRecommendations.setMargin(new java.awt.Insets(8, 8, 8, 8));
        lblSubtitle.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH))
                + "  ·  updated now");
        lblDisclaimer.setText("<html><div style='width:310px'>StudyPulse is an early-warning tool, not a medical "
                + "diagnosis. If you feel overwhelmed, talk to a counsellor.</div></html>");
        styleTable(tblReasons, false);
        styleTable(tblRiskHistory, true);
        tblReasons.getColumnModel().getColumn(0).setPreferredWidth(370);
    }

    private static void styleTable(JTable table, boolean hideId) {
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(230, 234, 238));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        if (hideId) {
            table.removeColumn(table.getColumnModel().getColumn(0));
        }
    }

    /** Title on top, big number in the middle, progress bar at the bottom. */
    private static void showGauge(JPanel panel, String title, int value, int max, String level, Color color) {
        panel.removeAll();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, color),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel lblGaugeTitle = new JLabel(title, SwingConstants.CENTER);
        lblGaugeTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGaugeTitle.setForeground(TEXT_MUTED);
        panel.add(lblGaugeTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel("<html><center>" + value + "<br><span style='font-size:12px'>" + level
                + "</span></center></html>", SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblValue.setForeground(color);
        panel.add(lblValue, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(value);
        bar.setForeground(color);
        bar.setBackground(new Color(230, 234, 238));
        bar.setBorderPainted(false);
        panel.add(bar, BorderLayout.SOUTH);
        panel.revalidate();
    }

    private static Color levelColor(String level) {
        if ("Low".equals(level)) {
            return SUCCESS;
        }
        return "Medium".equals(level) ? WARNING : DANGER;
    }

    /** Sample report for today. */
    private void loadReport() {
        showGauge(pnlLoadGauge, "Study Load Index", 46, 100, "Moderate", WARNING);
        showGauge(pnlRiskGauge, "Burnout Risk", 38, 100, "Medium", WARNING);

        DefaultTableModel reasonsModel = (DefaultTableModel) tblReasons.getModel();
        reasonsModel.setRowCount(0);
        reasonsModel.addRow(new Object[]{"4 tasks due in the next 7 days", "+12"});
        reasonsModel.addRow(new Object[]{"Average sleep 5.8 h (below 7 h)", "+10"});
        reasonsModel.addRow(new Object[]{"Stress 4 / 5 on 3 of the last 5 days", "+9"});
        reasonsModel.addRow(new Object[]{"2 study blocks skipped this week", "+4"});
        reasonsModel.addRow(new Object[]{"3 h of study without a break yesterday", "+3"});
        lblTotal.setText("Total: 38 / 100  ·  Medium");
        lblTotal.setForeground(WARNING);

        txtRecommendations.setText("• Start the Database Assignment today – it is due in 2 days.\n"
                + "• Aim for at least 7 hours of sleep for the next few nights.\n"
                + "• Take a 5-minute break after every 25-minute focus round.\n"
                + "• Move one Statistics block to the weekend to spread the load.");
        txtRecommendations.setCaretPosition(0);
    }

    private void loadHistory() {
        int[] load = {30, 32, 35, 33, 38, 40, 36, 34, 39, 42, 44, 41, 45, 46};
        int[] risk = {18, 20, 22, 25, 24, 28, 26, 22, 28, 31, 35, 30, 41, 38};
        DefaultTableModel historyModel = (DefaultTableModel) tblRiskHistory.getModel();
        historyModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < risk.length; i++) {
            int score = risk[risk.length - 1 - i];
            String level = score < 25 ? "Low" : (score < 60 ? "Medium" : "High");
            historyModel.addRow(new Object[]{i + 1, today.minusDays(i).format(DATE),
                load[load.length - 1 - i], score, level});
        }
        tblRiskHistory.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                super.setValue(value);
                setForeground(levelColor(String.valueOf(value)));
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        pnlLoadGauge = new javax.swing.JPanel();
        pnlRiskGauge = new javax.swing.JPanel();
        lblWhyTitle = new javax.swing.JLabel();
        scrReasons = new javax.swing.JScrollPane();
        tblReasons = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        lblRecTitle = new javax.swing.JLabel();
        scrRecommendations = new javax.swing.JScrollPane();
        txtRecommendations = new javax.swing.JTextArea();
        lblDisclaimer = new javax.swing.JLabel();
        lblHistoryTitle = new javax.swing.JLabel();
        scrRiskHistory = new javax.swing.JScrollPane();
        tblRiskHistory = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("StudyPulse – Risk Report");

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblTitle.setText("Risk Report");

        lblSubtitle.setForeground(new java.awt.Color(107, 119, 133));
        lblSubtitle.setText("Today");

        javax.swing.GroupLayout pnlLoadGaugeLayout = new javax.swing.GroupLayout(pnlLoadGauge);
        pnlLoadGauge.setLayout(pnlLoadGaugeLayout);
        pnlLoadGaugeLayout.setHorizontalGroup(
            pnlLoadGaugeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        pnlLoadGaugeLayout.setVerticalGroup(
            pnlLoadGaugeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 220, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlRiskGaugeLayout = new javax.swing.GroupLayout(pnlRiskGauge);
        pnlRiskGauge.setLayout(pnlRiskGaugeLayout);
        pnlRiskGaugeLayout.setHorizontalGroup(
            pnlRiskGaugeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        pnlRiskGaugeLayout.setVerticalGroup(
            pnlRiskGaugeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 220, Short.MAX_VALUE)
        );

        lblWhyTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblWhyTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblWhyTitle.setText("Why this score?");

        tblReasons.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Reason", "Points"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrReasons.setViewportView(tblReasons);

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTotal.setText("Total: 0 / 100");

        lblRecTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblRecTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblRecTitle.setText("Recommendations");

        txtRecommendations.setColumns(20);
        txtRecommendations.setEditable(false);
        txtRecommendations.setLineWrap(true);
        txtRecommendations.setRows(5);
        txtRecommendations.setWrapStyleWord(true);
        scrRecommendations.setViewportView(txtRecommendations);

        lblDisclaimer.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblDisclaimer.setForeground(new java.awt.Color(107, 119, 133));
        lblDisclaimer.setText("Disclaimer");

        lblHistoryTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblHistoryTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblHistoryTitle.setText("Last 14 days");

        tblRiskHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Date", "Load", "Risk", "Level"
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
        scrRiskHistory.setViewportView(tblRiskHistory);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblSubtitle)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlLoadGauge, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(pnlRiskGauge, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblWhyTitle)
                            .addComponent(scrReasons, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                            .addComponent(lblTotal)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRecTitle)
                            .addComponent(scrRecommendations, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                            .addComponent(lblDisclaimer))
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHistoryTitle)
                            .addComponent(scrRiskHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTitle)
                .addGap(2, 2, 2)
                .addComponent(lblSubtitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlLoadGauge, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlRiskGauge, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblWhyTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrReasons, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotal)))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblRecTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrRecommendations, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDisclaimer))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblHistoryTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrRiskHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))
                .addGap(16, 16, 16))
        );

        setSize(new java.awt.Dimension(900, 640));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblDisclaimer;
    private javax.swing.JLabel lblHistoryTitle;
    private javax.swing.JLabel lblRecTitle;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblWhyTitle;
    private javax.swing.JPanel pnlLoadGauge;
    private javax.swing.JPanel pnlRiskGauge;
    private javax.swing.JScrollPane scrReasons;
    private javax.swing.JScrollPane scrRecommendations;
    private javax.swing.JScrollPane scrRiskHistory;
    private javax.swing.JTable tblReasons;
    private javax.swing.JTable tblRiskHistory;
    private javax.swing.JTextArea txtRecommendations;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new RiskReportFrame().setVisible(true);
        });
    }
}
