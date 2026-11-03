package studypulse.forms;

import java.awt.Color;
import java.awt.Component;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Add, edit, delete, search and complete study tasks.
 * The filter combos have "All ..." at index 0. Tasks are sample data kept while the app runs.
 */
public class TaskFrame extends javax.swing.JFrame {

    private static final int COL_DIFFICULTY = 4;
    private static final int COL_LEFT = 9;
    private static final String[] STATUSES = {"Pending", "In Progress", "Done", "Missed"};
    private static final LocalTime DEFAULT_DEADLINE_TIME = LocalTime.of(23, 59);
    private static final DateTimeFormatter DEADLINE = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.ENGLISH);

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color NAVY = new Color(34, 48, 63);
    private static final Color ACCENT = new Color(46, 125, 154);
    private static final Color TEXT_MUTED = new Color(107, 119, 133);
    private static final Color SUCCESS = new Color(46, 158, 98);
    private static final Color WARNING = new Color(230, 145, 56);
    private static final Color DANGER = new Color(214, 69, 69);

    /** One study task. */
    private static class Task {
        int id;
        String title;
        String course;
        String type;
        String difficulty;
        int estimatedMinutes;
        LocalDateTime deadline;
        String priority;
        String status;
        String description;

        Task(int id, String title, String course, String type, String difficulty, int estimatedMinutes,
                LocalDateTime deadline, String priority, String status) {
            this.id = id;
            this.title = title;
            this.course = course;
            this.type = type;
            this.difficulty = difficulty;
            this.estimatedMinutes = estimatedMinutes;
            this.deadline = deadline;
            this.priority = priority;
            this.status = status;
            this.description = "";
        }
    }

    private static final List<Task> ALL = new ArrayList<Task>();
    private static int nextId = 1;

    static {
        LocalDateTime today = LocalDate.now().atTime(DEFAULT_DEADLINE_TIME);
        add(new Task(0, "Database Assignment", "CSE 311", "Assignment", "Hard", 360, today.plusDays(2), "High", "In Progress"));
        add(new Task(0, "Java Project – GUI", "CSE 215", "Project", "Medium", 600, today.plusDays(10), "High", "In Progress"));
        add(new Task(0, "Statistics Exam", "MAT 361", "Exam", "Hard", 480, today.plusDays(6).withHour(9).withMinute(0), "High", "Pending"));
        add(new Task(0, "Networking Lab Report", "CSE 338", "Assignment", "Easy", 120, today.plusDays(14).withHour(17).withMinute(0), "Medium", "Pending"));
        add(new Task(0, "Read Chapter 5 – OS", "CSE 323", "Preparation", "Medium", 90, today.minusDays(1), "Low", "Done"));
        add(new Task(0, "Physics Quiz", "PHY 108", "Exam", "Easy", 60, today.minusDays(3), "Medium", "Missed"));
    }

    private static void add(Task task) {
        task.id = nextId++;
        ALL.add(task);
    }

    private final List<Task> shownTasks = new ArrayList<Task>();
    private DefaultTableModel tableModel;
    private int selectedTaskId = -1;
    private boolean fillingTable;

    /**
     * Creates new form TaskFrame
     */
    public TaskFrame() {
        initComponents();
        Sidebar.attach(this, pnlSidebar, Sidebar.TASKS);
        applyColors();
        setupFields();
        setupTable();
        clearForm();
        applyFilter();
        setLocationRelativeTo(null);
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        btnSave.setBackground(ACCENT);
        btnSave.setForeground(Color.WHITE);
        btnMarkDone.setBackground(SUCCESS);
        btnMarkDone.setForeground(Color.WHITE);
        btnDelete.setForeground(DANGER);
        for (javax.swing.JButton b : new javax.swing.JButton[]{btnSave, btnUpdate, btnDelete, btnClear, btnMarkDone}) {
            b.setFocusPainted(false);
        }
        lblEstimateHint.setForeground(TEXT_MUTED);
        lblSummary.setForeground(TEXT_MUTED);
    }

    private void setupFields() {
        dateDeadline.setModel(new SpinnerDateModel());
        dateDeadline.setEditor(new JSpinner.DateEditor(dateDeadline, "yyyy-MM-dd"));
        spnDeadlineTime.setModel(new SpinnerDateModel(toDate(LocalDate.now().atTime(DEFAULT_DEADLINE_TIME)),
                null, null, Calendar.MINUTE));
        spnDeadlineTime.setEditor(new JSpinner.DateEditor(spnDeadlineTime, "HH:mm"));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
    }

    private void setupTable() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Course", "Type", "Difficulty", "Est.",
            "Deadline", "Priority", "Status", "Left"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTasks.setModel(tableModel);
        tblTasks.setRowHeight(26);
        tblTasks.setFillsViewportHeight(true);
        tblTasks.setGridColor(new Color(230, 234, 238));
        tblTasks.getTableHeader().setBackground(NAVY);
        tblTasks.getTableHeader().setForeground(Color.WHITE);
        tblTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTasks.removeColumn(tblTasks.getColumnModel().getColumn(0));
        tblTasks.getColumnModel().getColumn(0).setPreferredWidth(180);
        tblTasks.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    int modelColumn = table.convertColumnIndexToModel(column);
                    Task task = shownTasks.get(table.convertRowIndexToModel(row));
                    if (modelColumn == COL_DIFFICULTY) {
                        setForeground(difficultyColor(task.difficulty));
                    } else if (modelColumn == COL_LEFT) {
                        setForeground(leftColor(task));
                    } else {
                        setForeground(NAVY);
                    }
                }
                return this;
            }
        });
        tblTasks.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !fillingTable) {
                    loadSelectedTask();
                }
            }
        });
    }

    private static Color difficultyColor(String difficulty) {
        if ("Hard".equals(difficulty)) {
            return DANGER;
        }
        return "Medium".equals(difficulty) ? WARNING : SUCCESS;
    }

    private static Color leftColor(Task task) {
        if (isClosed(task)) {
            return TEXT_MUTED;
        }
        long hours = Duration.between(LocalDateTime.now(), task.deadline).toHours();
        return hours < 72 ? DANGER : NAVY;
    }

    private static boolean isClosed(Task task) {
        return "Done".equals(task.status) || "Missed".equals(task.status);
    }

    /** Search text + status + difficulty filter. */
    private void applyFilter() {
        String search = txtSearch.getText().trim().toLowerCase(Locale.ENGLISH);
        String status = cmbFilterStatus.getSelectedIndex() <= 0 ? null : STATUSES[cmbFilterStatus.getSelectedIndex() - 1];
        String difficulty = cmbFilterDifficulty.getSelectedIndex() <= 0 ? null : (String) cmbFilterDifficulty.getSelectedItem();

        fillingTable = true;
        tableModel.setRowCount(0);
        shownTasks.clear();
        for (Task t : ALL) {
            boolean matches = search.isEmpty() || t.title.toLowerCase(Locale.ENGLISH).contains(search)
                    || t.course.toLowerCase(Locale.ENGLISH).contains(search);
            if (matches && (status == null || status.equals(t.status))
                    && (difficulty == null || difficulty.equals(t.difficulty))) {
                shownTasks.add(t);
                tableModel.addRow(new Object[]{t.id, t.title, t.course, t.type, t.difficulty,
                    formatMinutes(t.estimatedMinutes), t.deadline.format(DEADLINE), t.priority, t.status, timeLeft(t)});
            }
        }
        for (int i = 0; i < shownTasks.size(); i++) {
            if (shownTasks.get(i).id == selectedTaskId) {
                tblTasks.setRowSelectionInterval(i, i);
            }
        }
        fillingTable = false;
        updateSummary();
        updateButtons();
    }

    private void updateSummary() {
        int[] counts = new int[STATUSES.length];
        for (Task t : ALL) {
            for (int i = 0; i < STATUSES.length; i++) {
                if (STATUSES[i].equals(t.status)) {
                    counts[i]++;
                }
            }
        }
        lblSummary.setText("Total: " + ALL.size() + " | Pending: " + counts[0] + " | In progress: " + counts[1]
                + " | Done: " + counts[2] + " | Missed: " + counts[3]);
    }

    private void loadSelectedTask() {
        int row = tblTasks.getSelectedRow();
        if (row < 0) {
            return;
        }
        Task task = shownTasks.get(tblTasks.convertRowIndexToModel(row));
        selectedTaskId = task.id;
        lblFormTitle.setText("Edit task");
        txtTitle.setText(task.title);
        txtCourse.setText(task.course);
        cmbType.setSelectedItem(task.type);
        cmbDifficulty.setSelectedItem(task.difficulty);
        cmbPriority.setSelectedItem(task.priority);
        spnHours.setValue(task.estimatedMinutes / 60);
        spnMinutes.setValue(task.estimatedMinutes % 60 / 5 * 5);
        dateDeadline.setValue(toDate(task.deadline));
        spnDeadlineTime.setValue(toDate(task.deadline));
        txtDescription.setText(task.description);
        updateButtons();
    }

    /** Copies the form into 'task'; false (with a message) when a field is invalid. */
    private boolean readForm(Task task) {
        commitSpinners();
        String title = txtTitle.getText().trim();
        String course = txtCourse.getText().trim();
        int minutes = (Integer) spnHours.getValue() * 60 + (Integer) spnMinutes.getValue();
        if (title.isEmpty() || course.isEmpty()) {
            showWarning("Enter a title and a course.");
            return false;
        }
        if (minutes <= 0) {
            showWarning("The estimate must be more than 0 minutes.");
            return false;
        }
        LocalDate date = toLocalDateTime((Date) dateDeadline.getValue()).toLocalDate();
        LocalTime time = toLocalDateTime((Date) spnDeadlineTime.getValue()).toLocalTime().withSecond(0).withNano(0);
        task.title = title;
        task.course = course;
        task.type = (String) cmbType.getSelectedItem();
        task.difficulty = (String) cmbDifficulty.getSelectedItem();
        task.priority = (String) cmbPriority.getSelectedItem();
        task.estimatedMinutes = minutes;
        task.deadline = date.atTime(time);
        task.description = txtDescription.getText().trim();
        return true;
    }

    private void saveTask() {
        Task task = new Task(0, "", "", "", "", 0, null, "", "Pending");
        if (!readForm(task)) {
            return;
        }
        add(task);
        selectedTaskId = task.id;
        applyFilter();
        loadSelectedTask();
    }

    private void updateTask() {
        Task task = requireSelection();
        if (task != null && readForm(task)) {
            applyFilter();
            JOptionPane.showMessageDialog(this, "Task updated.", "Tasks", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteTask() {
        Task task = requireSelection();
        if (task == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete \"" + task.title + "\"?", "Tasks",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            ALL.remove(task);
            clearForm();
            applyFilter();
        }
    }

    private void markDone() {
        Task task = requireSelection();
        if (task == null) {
            return;
        }
        Integer actual = askActualMinutes(task.estimatedMinutes);
        if (actual == null) {
            return;
        }
        task.status = "Done";
        applyFilter();
        ToastFrame.showToast("Task done", task.title + " (" + formatMinutes(actual) + ")", SUCCESS);
    }

    private Integer askActualMinutes(int estimated) {
        String answer = JOptionPane.showInputDialog(this, "How many minutes did it actually take?", estimated);
        if (answer == null) {
            return null;
        }
        try {
            int minutes = Integer.parseInt(answer.trim());
            if (minutes > 0) {
                return minutes;
            }
        } catch (NumberFormatException ex) {
            // falls through to the warning
        }
        showWarning("Enter a whole number of minutes greater than 0.");
        return null;
    }

    private void clearForm() {
        selectedTaskId = -1;
        tblTasks.clearSelection();
        lblFormTitle.setText("New task");
        txtTitle.setText("");
        txtCourse.setText("");
        cmbType.setSelectedIndex(0);
        cmbDifficulty.setSelectedIndex(1);
        cmbPriority.setSelectedIndex(1);
        spnHours.setValue(1);
        spnMinutes.setValue(0);
        dateDeadline.setValue(toDate(LocalDate.now().plusDays(7).atTime(DEFAULT_DEADLINE_TIME)));
        spnDeadlineTime.setValue(toDate(LocalDate.now().atTime(DEFAULT_DEADLINE_TIME)));
        txtDescription.setText("");
        updateButtons();
        updateEstimateHint();
    }

    private void updateButtons() {
        boolean selected = selectedTaskId >= 0;
        Task task = selected ? findLoaded(selectedTaskId) : null;
        btnSave.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnDelete.setEnabled(selected);
        btnMarkDone.setEnabled(task != null && !isClosed(task));
    }

    /** Typical effort for the chosen type and difficulty. */
    private void updateEstimateHint() {
        String type = (String) cmbType.getSelectedItem();
        String difficulty = (String) cmbDifficulty.getSelectedItem();
        int hours;
        if ("Exam".equals(type)) {
            hours = "Hard".equals(difficulty) ? 10 : ("Medium".equals(difficulty) ? 6 : 3);
        } else if ("Project".equals(type)) {
            hours = "Hard".equals(difficulty) ? 15 : ("Medium".equals(difficulty) ? 10 : 5);
        } else {
            hours = "Hard".equals(difficulty) ? 6 : ("Medium".equals(difficulty) ? 3 : 1);
        }
        lblEstimateHint.setText("<html><div style='width:175px'>Typical for a " + String.valueOf(difficulty).toLowerCase(Locale.ENGLISH)
                + " " + String.valueOf(type).toLowerCase(Locale.ENGLISH) + ": about " + hours + " h</div></html>");
    }

    private Task requireSelection() {
        Task task = findLoaded(selectedTaskId);
        if (task == null) {
            showWarning("Select a task in the table first.");
        }
        return task;
    }

    private Task findLoaded(int id) {
        for (Task t : ALL) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    /** Takes the value that is typed but not yet committed in the spinners. */
    private void commitSpinners() {
        for (JSpinner spinner : new JSpinner[]{spnHours, spnMinutes, dateDeadline, spnDeadlineTime}) {
            try {
                spinner.commitEdit();
            } catch (java.text.ParseException ex) {
                // keep the last valid value
            }
        }
    }

    private static String timeLeft(Task task) {
        if (isClosed(task)) {
            return "–";
        }
        Duration left = Duration.between(LocalDateTime.now(), task.deadline);
        if (left.isNegative()) {
            return "Overdue";
        }
        long days = left.toDays();
        return days >= 1 ? days + (days == 1 ? " day" : " days") : left.toHours() + " h";
    }

    private static String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        return minutes / 60 + " h" + (minutes % 60 == 0 ? "" : " " + minutes % 60 + " min");
    }

    private static Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Tasks", JOptionPane.WARNING_MESSAGE);
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
        pnlForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblTaskTitle = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        lblCourse = new javax.swing.JLabel();
        txtCourse = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        cmbType = new javax.swing.JComboBox<>();
        lblDifficulty = new javax.swing.JLabel();
        cmbDifficulty = new javax.swing.JComboBox<>();
        lblEstimate = new javax.swing.JLabel();
        spnHours = new javax.swing.JSpinner();
        lblHours = new javax.swing.JLabel();
        spnMinutes = new javax.swing.JSpinner();
        lblMinutes = new javax.swing.JLabel();
        lblEstimateHint = new javax.swing.JLabel();
        lblDeadline = new javax.swing.JLabel();
        dateDeadline = new javax.swing.JSpinner();
        lblDeadlineTime = new javax.swing.JLabel();
        spnDeadlineTime = new javax.swing.JSpinner();
        lblPriority = new javax.swing.JLabel();
        cmbPriority = new javax.swing.JComboBox<>();
        lblDescription = new javax.swing.JLabel();
        scrDescription = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnMarkDone = new javax.swing.JButton();
        lblTitle = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        cmbFilterStatus = new javax.swing.JComboBox<>();
        cmbFilterDifficulty = new javax.swing.JComboBox<>();
        scrTasks = new javax.swing.JScrollPane();
        tblTasks = new javax.swing.JTable();
        lblSummary = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StudyPulse – Tasks");

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

        pnlForm.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblFormTitle.setText("Add / Edit Task");

        lblTaskTitle.setText("Title");

        lblCourse.setText("Course");

        lblType.setText("Type");

        cmbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Assignment", "Exam", "Project", "Preparation" }));
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        lblDifficulty.setText("Difficulty");

        cmbDifficulty.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Easy", "Medium", "Hard" }));
        cmbDifficulty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDifficultyActionPerformed(evt);
            }
        });

        lblEstimate.setText("Estimated time");

        spnHours.setModel(new javax.swing.SpinnerNumberModel(1, 0, 100, 1));

        lblHours.setText("h");

        spnMinutes.setModel(new javax.swing.SpinnerNumberModel(0, 0, 55, 5));

        lblMinutes.setText("min");

        lblEstimateHint.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblEstimateHint.setForeground(new java.awt.Color(46, 125, 154));
        lblEstimateHint.setText("Estimate hint");

        lblDeadline.setText("Deadline");

        lblDeadlineTime.setText("Time");

        lblPriority.setText("Priority");

        cmbPriority.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Low", "Medium", "High" }));

        lblDescription.setText("Description");

        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(3);
        txtDescription.setWrapStyleWord(true);
        scrDescription.setViewportView(txtDescription);

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnMarkDone.setText("Mark as Done");
        btnMarkDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkDoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFormTitle)
                    .addComponent(lblTaskTitle)
                    .addComponent(txtTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                    .addComponent(lblCourse)
                    .addComponent(txtCourse, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblType)
                            .addComponent(cmbType, 0, 110, Short.MAX_VALUE))
                        .addGap(8, 8, 8)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDifficulty)
                            .addComponent(cmbDifficulty, 0, 110, Short.MAX_VALUE)))
                    .addComponent(lblEstimate)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addComponent(spnHours, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHours)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnMinutes, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMinutes))
                    .addComponent(lblEstimateHint, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDeadline)
                            .addComponent(dateDeadline, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                        .addGap(8, 8, 8)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDeadlineTime)
                            .addComponent(spnDeadlineTime, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)))
                    .addComponent(lblPriority)
                    .addComponent(cmbPriority, 0, 228, Short.MAX_VALUE)
                    .addComponent(lblDescription)
                    .addComponent(scrDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                    .addComponent(btnMarkDone, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblFormTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTaskTitle)
                .addGap(4, 4, 4)
                .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCourse)
                .addGap(4, 4, 4)
                .addComponent(txtCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(lblDifficulty))
                .addGap(4, 4, 4)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEstimate)
                .addGap(4, 4, 4)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnHours, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHours)
                    .addComponent(spnMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMinutes))
                .addGap(4, 4, 4)
                .addComponent(lblEstimateHint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDeadline)
                    .addComponent(lblDeadlineTime))
                .addGap(4, 4, 4)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateDeadline, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnDeadlineTime, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPriority)
                .addGap(4, 4, 4)
                .addComponent(cmbPriority, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDescription)
                .addGap(4, 4, 4)
                .addComponent(scrDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMarkDone, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblTitle.setText("Tasks");

        cmbFilterStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All status", "Pending", "In Progress", "Done", "Missed" }));
        cmbFilterStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterStatusActionPerformed(evt);
            }
        });

        cmbFilterDifficulty.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All difficulty", "Easy", "Medium", "Hard" }));
        cmbFilterDifficulty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterDifficultyActionPerformed(evt);
            }
        });

        tblTasks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Title", "Course", "Type", "Difficulty", "Est.", "Deadline", "Priority", "Status", "Left"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrTasks.setViewportView(tblTasks);

        lblSummary.setForeground(new java.awt.Color(107, 119, 133));
        lblSummary.setText("Total: 0 | Pending: 0 | In progress: 0 | Done: 0 | Missed: 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFilterStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFilterDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE)
                    .addComponent(lblSummary, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(pnlForm, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                .addGap(16, 16, 16))
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbFilterStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbFilterDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSummary)
                .addGap(16, 16, 16))
        );

        setSize(new java.awt.Dimension(1200, 720));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
        updateEstimateHint();
    }//GEN-LAST:event_cmbTypeActionPerformed

    private void cmbDifficultyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDifficultyActionPerformed
        updateEstimateHint();
    }//GEN-LAST:event_cmbDifficultyActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveTask();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateTask();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteTask();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnMarkDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkDoneActionPerformed
        markDone();
    }//GEN-LAST:event_btnMarkDoneActionPerformed

    private void cmbFilterStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterStatusActionPerformed
        applyFilter();
    }//GEN-LAST:event_cmbFilterStatusActionPerformed

    private void cmbFilterDifficultyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterDifficultyActionPerformed
        applyFilter();
    }//GEN-LAST:event_cmbFilterDifficultyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnMarkDone;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbDifficulty;
    private javax.swing.JComboBox<String> cmbFilterDifficulty;
    private javax.swing.JComboBox<String> cmbFilterStatus;
    private javax.swing.JComboBox<String> cmbPriority;
    private javax.swing.JComboBox<String> cmbType;
    private javax.swing.JSpinner dateDeadline;
    private javax.swing.JLabel lblCourse;
    private javax.swing.JLabel lblDeadline;
    private javax.swing.JLabel lblDeadlineTime;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblDifficulty;
    private javax.swing.JLabel lblEstimate;
    private javax.swing.JLabel lblEstimateHint;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHours;
    private javax.swing.JLabel lblMinutes;
    private javax.swing.JLabel lblPriority;
    private javax.swing.JLabel lblSummary;
    private javax.swing.JLabel lblTaskTitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrDescription;
    private javax.swing.JScrollPane scrTasks;
    private javax.swing.JSpinner spnDeadlineTime;
    private javax.swing.JSpinner spnHours;
    private javax.swing.JSpinner spnMinutes;
    private javax.swing.JTable tblTasks;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtCourse;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new TaskFrame().setVisible(true);
        });
    }
}
