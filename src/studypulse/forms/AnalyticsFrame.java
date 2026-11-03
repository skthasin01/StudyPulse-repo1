package studypulse.forms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Four charts for the last 7 days or 4 weeks, an insight line and PNG export.
 * pnlCharts has a 2x2 GridLayout in the form; the charts are added in loadCharts().
 */
public class AnalyticsFrame extends javax.swing.JFrame {

    private static final String[] FILE_NAMES = {"study-hours", "load-vs-risk", "tasks-by-status", "sleep-vs-stress"};
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEE dd", Locale.ENGLISH);

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color ACCENT = new Color(46, 125, 154);
    private static final Color TEAL = new Color(38, 166, 154);
    private static final Color SUCCESS = new Color(46, 158, 98);
    private static final Color WARNING = new Color(230, 145, 56);
    private static final Color DANGER = new Color(214, 69, 69);
    private static final Color NAVY = new Color(34, 48, 63);

    private final List<SimpleChart> charts = new ArrayList<SimpleChart>();

    /**
     * Creates new form AnalyticsFrame
     */
    public AnalyticsFrame() {
        initComponents();
        Sidebar.attach(this, pnlSidebar, Sidebar.ANALYTICS);
        applyColors();
        loadCharts();
        setLocationRelativeTo(null);
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        pnlCharts.setOpaque(false);
        btnExportPng.setBackground(ACCENT);
        btnExportPng.setForeground(Color.WHITE);
        btnExportPng.setFocusPainted(false);
    }

    private int selectedDays() {
        return cmbRange.getSelectedIndex() == 1 ? 28 : 7;
    }

    /** Sample values: 7 daily points or 4 weekly points. */
    private void loadCharts() {
        int days = selectedDays();
        boolean weekly = days > 7;
        String[] labels = new String[weekly ? 4 : 7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < labels.length; i++) {
            labels[i] = dayLabel(weekly ? today.minusWeeks(labels.length - 1 - i) : today.minusDays(labels.length - 1 - i), days);
        }

        charts.clear();
        if (weekly) {
            charts.add(new SimpleChart("Study hours per week", labels, false)
                    .addSeries("Hours", new double[]{9.5, 11, 10.5, 12.5}, ACCENT));
            charts.add(new SimpleChart("Study load vs burnout risk", labels, true)
                    .addSeries("Load", new double[]{34, 38, 41, 46}, WARNING)
                    .addSeries("Risk", new double[]{22, 26, 30, 38}, DANGER));
            charts.add(new SimpleChart("Tasks by status", new String[]{"Pending", "In progress", "Done", "Missed"}, false)
                    .addSeries("Tasks", new double[]{2, 2, 9, 2}, TEAL));
            charts.add(new SimpleChart("Sleep (h) vs stress (1-5)", labels, true)
                    .addSeries("Sleep", new double[]{7.1, 6.8, 6.4, 6.2}, ACCENT)
                    .addSeries("Stress", new double[]{2.6, 3.0, 3.3, 3.6}, DANGER));
            lblInsight.setText("<html><div>Your study hours grew by <b>3 h</b> over 4 weeks, but average sleep "
                    + "dropped to <b>6.2 h</b>. Risk follows the load – spread the next big deadline.</div></html>");
        } else {
            charts.add(new SimpleChart("Study hours per day", labels, false)
                    .addSeries("Hours", new double[]{1.5, 2, 2.5, 0.5, 2, 3, 1}, ACCENT));
            charts.add(new SimpleChart("Study load vs burnout risk", labels, true)
                    .addSeries("Load", new double[]{36, 34, 39, 42, 44, 41, 46}, WARNING)
                    .addSeries("Risk", new double[]{22, 28, 31, 35, 30, 41, 38}, DANGER));
            charts.add(new SimpleChart("Tasks by status", new String[]{"Pending", "In progress", "Done", "Missed"}, false)
                    .addSeries("Tasks", new double[]{2, 2, 1, 1}, TEAL));
            charts.add(new SimpleChart("Sleep (h) vs stress (1-5)", labels, true)
                    .addSeries("Sleep", new double[]{7, 6.5, 8, 6, 5.5, 6, 7}, ACCENT)
                    .addSeries("Stress", new double[]{3, 3, 2, 4, 4, 3, 3}, DANGER));
            lblInsight.setText("<html><div>On days with less than <b>6 h</b> of sleep your stress was "
                    + "<b>4 / 5</b>. Your best study day was " + labels[5] + " with <b>3 h</b>.</div></html>");
        }
        lblInsight.setForeground(NAVY);

        pnlCharts.removeAll();
        for (SimpleChart chart : charts) {
            chart.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(225, 230, 235)));
            pnlCharts.add(chart);
        }
        pnlCharts.revalidate();
        pnlCharts.repaint();
    }

    private static String dayLabel(LocalDate date, int days) {
        return days > 7 ? date.format(SHORT_DAY) : date.format(WEEKDAY);
    }

    /** Saves every chart as a PNG file in the chosen folder. */
    private void exportPng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose a folder for the PNG files");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File folder = chooser.getSelectedFile();
        try {
            for (int i = 0; i < charts.size(); i++) {
                SimpleChart chart = charts.get(i);
                int width = Math.max(600, chart.getWidth());
                int height = Math.max(400, chart.getHeight());
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                chart.setSize(width, height);
                chart.paint(g);
                g.dispose();
                ImageIO.write(image, "png", new File(folder, FILE_NAMES[i] + ".png"));
            }
            pnlCharts.revalidate();
            JOptionPane.showMessageDialog(this, charts.size() + " charts saved to " + folder.getAbsolutePath(),
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the charts: " + ex.getMessage(), "Export",
                    JOptionPane.ERROR_MESSAGE);
        }
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
        cmbRange = new javax.swing.JComboBox<>();
        btnExportPng = new javax.swing.JButton();
        pnlCharts = new javax.swing.JPanel();
        lblInsight = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StudyPulse – Analytics");

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
        lblTitle.setText("Analytics");

        cmbRange.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Last 7 days", "Last 4 weeks" }));
        cmbRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbRangeActionPerformed(evt);
            }
        });

        btnExportPng.setText("Export PNG");
        btnExportPng.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportPngActionPerformed(evt);
            }
        });

        pnlCharts.setLayout(new java.awt.GridLayout(2, 2, 16, 16));

        lblInsight.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblInsight.setForeground(new java.awt.Color(34, 48, 63));
        lblInsight.setText("Insight");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbRange, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportPng))
                    .addComponent(pnlCharts, javax.swing.GroupLayout.DEFAULT_SIZE, 1008, Short.MAX_VALUE)
                    .addComponent(lblInsight, javax.swing.GroupLayout.DEFAULT_SIZE, 1008, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(cmbRange, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExportPng, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlCharts, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblInsight)
                .addGap(16, 16, 16))
        );

        setSize(new java.awt.Dimension(1200, 720));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void cmbRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRangeActionPerformed
        loadCharts();
    }//GEN-LAST:event_cmbRangeActionPerformed

    private void btnExportPngActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportPngActionPerformed
        exportPng();
    }//GEN-LAST:event_btnExportPngActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportPng;
    private javax.swing.JComboBox<String> cmbRange;
    private javax.swing.JLabel lblInsight;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlCharts;
    private javax.swing.JPanel pnlSidebar;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new AnalyticsFrame().setVisible(true);
        });
    }
}
