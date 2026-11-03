package studypulse.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * All notifications, newest first. Unread rows are bold; the type is coloured.
 * cmbFilterType: index 0 = all types, then the type names.
 */
public class NotificationFrame extends javax.swing.JFrame {

    private static final int COL_TYPE = 1;
    private static final int COL_STATUS = 4;
    private static final String UNREAD = "Unread";
    private static final String READ = "Read";

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color NAVY = new Color(34, 48, 63);
    private static final Color ACCENT = new Color(46, 125, 154);
    private static final Color SUCCESS = new Color(46, 158, 98);
    private static final Color WARNING = new Color(230, 145, 56);
    private static final Color DANGER = new Color(214, 69, 69);

    /** Rows: ID, Type, Message, Time, Status. Kept while the app runs; read by the dashboard bell. */
    private static final List<Object[]> ALL = new ArrayList<Object[]>();
    static int unreadCount;

    static {
        ALL.add(new Object[]{1, "Deadline", "Database Assignment is due in 2 days.", "Today 08:00", UNREAD});
        ALL.add(new Object[]{2, "Burnout", "Burnout risk rose to 38 (Medium).", "Today 07:30", UNREAD});
        ALL.add(new Object[]{3, "Break", "You studied 3 h without a break.", "Yesterday 21:10", UNREAD});
        ALL.add(new Object[]{4, "Workload", "Next week's load is Moderate (46).", "Yesterday 09:00", READ});
        ALL.add(new Object[]{5, "Info", "Your study plan for this week was generated.", "Mon 08:15", READ});
        countUnread();
    }

    private DefaultTableModel tableModel;

    /**
     * Creates new form NotificationFrame
     */
    public NotificationFrame() {
        initComponents();
        Sidebar.attach(this, pnlSidebar, Sidebar.NOTIFICATIONS);
        applyColors();
        setupTable();
        loadNotifications();
        setLocationRelativeTo(null);
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        btnMarkRead.setBackground(ACCENT);
        btnMarkRead.setForeground(Color.WHITE);
        btnMarkAllRead.setFocusPainted(false);
        btnMarkRead.setFocusPainted(false);
        btnDelete.setForeground(DANGER);
        btnDelete.setFocusPainted(false);
    }

    private void setupTable() {
        tableModel = (DefaultTableModel) tblNotifications.getModel();
        tblNotifications.setRowHeight(28);
        tblNotifications.setFillsViewportHeight(true);
        tblNotifications.setGridColor(new Color(230, 234, 238));
        tblNotifications.getTableHeader().setBackground(NAVY);
        tblNotifications.getTableHeader().setForeground(Color.WHITE);
        tblNotifications.removeColumn(tblNotifications.getColumnModel().getColumn(0));
        tblNotifications.getColumnModel().getColumn(1).setPreferredWidth(380);
        tblNotifications.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                boolean unread = UNREAD.equals(tableModel.getValueAt(modelRow, COL_STATUS));
                setFont(getFont().deriveFont(unread ? Font.BOLD : Font.PLAIN));
                if (!isSelected) {
                    setForeground(table.convertColumnIndexToModel(column) == COL_TYPE
                            ? typeColor(String.valueOf(value)) : NAVY);
                }
                return this;
            }
        });
    }

    private static Color typeColor(String type) {
        if ("Burnout".equals(type) || "Deadline".equals(type)) {
            return DANGER;
        }
        if ("Workload".equals(type)) {
            return WARNING;
        }
        return "Break".equals(type) ? SUCCESS : ACCENT;
    }

    private void loadNotifications() {
        String filter = cmbFilterType.getSelectedIndex() <= 0 ? null : (String) cmbFilterType.getSelectedItem();
        tableModel.setRowCount(0);
        for (Object[] row : ALL) {
            if (filter == null || filter.equals(row[COL_TYPE])) {
                tableModel.addRow(row.clone());
            }
        }
        countUnread();
        lblUnread.setText(unreadCount + " unread");
        lblUnread.setForeground(unreadCount > 0 ? DANGER : NAVY);
    }

    private static void countUnread() {
        int count = 0;
        for (Object[] row : ALL) {
            if (UNREAD.equals(row[COL_STATUS])) {
                count++;
            }
        }
        unreadCount = count;
    }

    private List<Integer> selectedIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (int row : tblNotifications.getSelectedRows()) {
            ids.add((Integer) tableModel.getValueAt(tblNotifications.convertRowIndexToModel(row), 0));
        }
        return ids;
    }

    private void markRead() {
        List<Integer> ids = selectedIds();
        if (ids.isEmpty()) {
            showWarning("Select one or more notifications first.");
            return;
        }
        for (Object[] row : ALL) {
            if (ids.contains(row[0])) {
                row[COL_STATUS] = READ;
            }
        }
        loadNotifications();
    }

    private void markAllRead() {
        for (Object[] row : ALL) {
            row[COL_STATUS] = READ;
        }
        loadNotifications();
    }

    private void delete() {
        List<Integer> ids = selectedIds();
        if (ids.isEmpty()) {
            showWarning("Select one or more notifications first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete " + ids.size() + " notification"
                + (ids.size() == 1 ? "" : "s") + "?", "Notifications", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        for (int i = ALL.size() - 1; i >= 0; i--) {
            if (ids.contains(ALL.get(i)[0])) {
                ALL.remove(i);
            }
        }
        loadNotifications();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Notifications", JOptionPane.WARNING_MESSAGE);
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
        lblUnread = new javax.swing.JLabel();
        cmbFilterType = new javax.swing.JComboBox<>();
        btnMarkRead = new javax.swing.JButton();
        btnMarkAllRead = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        scrNotifications = new javax.swing.JScrollPane();
        tblNotifications = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StudyPulse – Notifications");

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
        lblTitle.setText("Notifications");

        lblUnread.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblUnread.setForeground(new java.awt.Color(214, 69, 69));
        lblUnread.setText("0 unread");

        cmbFilterType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All types", "Workload", "Burnout", "Break", "Deadline", "Info" }));
        cmbFilterType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterTypeActionPerformed(evt);
            }
        });

        btnMarkRead.setText("Mark as read");
        btnMarkRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkReadActionPerformed(evt);
            }
        });

        btnMarkAllRead.setText("Mark all as read");
        btnMarkAllRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkAllReadActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        tblNotifications.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Type", "Message", "Time", "Status"
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
        scrNotifications.setViewportView(tblNotifications);

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
                        .addGap(12, 12, 12)
                        .addComponent(lblUnread)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbFilterType, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkRead)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkAllRead)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete))
                    .addComponent(scrNotifications, javax.swing.GroupLayout.DEFAULT_SIZE, 1008, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(lblUnread)
                    .addComponent(cmbFilterType, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMarkRead, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMarkAllRead, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrNotifications, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        setSize(new java.awt.Dimension(1200, 720));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void cmbFilterTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterTypeActionPerformed
        loadNotifications();
    }//GEN-LAST:event_cmbFilterTypeActionPerformed

    private void btnMarkReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkReadActionPerformed
        markRead();
    }//GEN-LAST:event_btnMarkReadActionPerformed

    private void btnMarkAllReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkAllReadActionPerformed
        markAllRead();
    }//GEN-LAST:event_btnMarkAllReadActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        delete();
    }//GEN-LAST:event_btnDeleteActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnMarkAllRead;
    private javax.swing.JButton btnMarkRead;
    private javax.swing.JComboBox<String> cmbFilterType;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUnread;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrNotifications;
    private javax.swing.JTable tblNotifications;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new NotificationFrame().setVisible(true);
        });
    }
}
