package studypulse.forms;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Pomodoro timer (one window at a time, opened on top of other screens with
 * {@link #showWindow}). Focus 25 / short break 5 / long break 15 minutes by default,
 * long break after 4 focus rounds.
 */
public class PomodoroFrame extends javax.swing.JFrame {

    private static final int ROUNDS_BEFORE_LONG_BREAK = 4;
    private static final String FOCUS = "Focus";
    private static final String SHORT_BREAK = "Short break";
    private static final String LONG_BREAK = "Long break";
    private static final String[] TODAY_TASKS = {"Database Assignment", "Java Project – GUI", "Statistics Exam Prep"};

    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color ACCENT = new Color(46, 125, 154);
    private static final Color TEAL = new Color(38, 166, 154);
    private static final Color TEXT_MUTED = new Color(107, 119, 133);

    private static PomodoroFrame instance;

    private final Timer ticker;
    private String phase = FOCUS;
    private int round = 1;
    private int phaseSeconds;
    private int remainingSeconds;
    private boolean running;

    /**
     * Creates new form PomodoroFrame
     */
    public PomodoroFrame() {
        initComponents();
        // formWindowClosing decides whether to close
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        applyColors();
        loadTasks();
        ticker = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        preparePhase();
        setLocationRelativeTo(null);
    }

    /** Shows the timer window (creating it once) and selects 'task' when given. */
    public static void showWindow(String task) {
        if (instance == null) {
            instance = new PomodoroFrame();
        }
        if (task != null) {
            instance.cmbTask.setSelectedItem(task);
        }
        instance.setVisible(true);
        instance.toFront();
    }

    public static void closeWindow() {
        if (instance != null) {
            instance.dispose();
        }
    }

    private void applyColors() {
        getContentPane().setBackground(BACKGROUND);
        btnStart.setBackground(ACCENT);
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnReset.setFocusPainted(false);
        btnSkip.setFocusPainted(false);
        prgTime.setBackground(Color.WHITE);
        prgTime.setBorderPainted(false);
        chkOnTop.setOpaque(false);
    }

    private void loadTasks() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        model.addElement("No task");
        for (String task : TODAY_TASKS) {
            model.addElement(task);
        }
        cmbTask.setModel(model);
    }

    private void preparePhase() {
        phaseSeconds = minutesFor(phase) * 60;
        remainingSeconds = phaseSeconds;
        updateDisplay();
    }

    private int minutesFor(String p) {
        if (FOCUS.equals(p)) {
            return (Integer) spnFocus.getValue();
        }
        return SHORT_BREAK.equals(p) ? (Integer) spnShortBreak.getValue() : (Integer) spnLongBreak.getValue();
    }

    private boolean phaseStarted() {
        return running || remainingSeconds != phaseSeconds;
    }

    private void updateDisplay() {
        boolean isBreak = !FOCUS.equals(phase);
        lblPhase.setText(phase);
        lblPhase.setForeground(isBreak ? TEAL : ACCENT);
        lblTimer.setText(String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60));
        prgTime.setMaximum(Math.max(1, phaseSeconds));
        prgTime.setValue(phaseSeconds - remainingSeconds);
        prgTime.setForeground(isBreak ? TEAL : ACCENT);
        lblRounds.setText("Round " + round + " of " + ROUNDS_BEFORE_LONG_BREAK);
        lblRounds.setForeground(TEXT_MUTED);
        btnStart.setText(running ? "Pause" : (phaseStarted() ? "Resume" : "Start"));
    }

    private void startOrPause() {
        running = !running;
        if (running) {
            ticker.start();
        } else {
            ticker.stop();
        }
        updateDisplay();
    }

    private void tick() {
        remainingSeconds--;
        if (remainingSeconds <= 0) {
            finishPhase(false);
        } else {
            updateDisplay();
        }
    }

    /** Moves to the next phase: focus -> short break (or long break after round 4) -> focus. */
    private void finishPhase(boolean skipped) {
        ticker.stop();
        running = false;
        String finished = phase;
        if (FOCUS.equals(phase)) {
            phase = round >= ROUNDS_BEFORE_LONG_BREAK ? LONG_BREAK : SHORT_BREAK;
        } else {
            round = LONG_BREAK.equals(phase) ? 1 : round + 1;
            phase = FOCUS;
        }
        preparePhase();
        if (!skipped) {
            Toolkit.getDefaultToolkit().beep();
            ToastFrame.showToast(finished + " finished", "Next: " + phase + " (" + minutesFor(phase) + " min)",
                    FOCUS.equals(phase) ? ACCENT : TEAL);
        }
    }

    private void resetPhase() {
        ticker.stop();
        running = false;
        preparePhase();
    }

    private void askToSaveRunningFocus() {
        if (running && FOCUS.equals(phase)) {
            int choice = JOptionPane.showConfirmDialog(this, "A focus round is running. Stop the timer and close?",
                    "Pomodoro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }

    @Override
    public void dispose() {
        if (ticker != null) {
            ticker.stop();
        }
        if (instance == this) {
            instance = null;
        }
        super.dispose();
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
        lblTaskCaption = new javax.swing.JLabel();
        cmbTask = new javax.swing.JComboBox<>();
        lblPhase = new javax.swing.JLabel();
        lblTimer = new javax.swing.JLabel();
        prgTime = new javax.swing.JProgressBar();
        lblRounds = new javax.swing.JLabel();
        btnStart = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        btnSkip = new javax.swing.JButton();
        lblFocus = new javax.swing.JLabel();
        lblShortBreak = new javax.swing.JLabel();
        lblLongBreak = new javax.swing.JLabel();
        spnFocus = new javax.swing.JSpinner();
        spnShortBreak = new javax.swing.JSpinner();
        spnLongBreak = new javax.swing.JSpinner();
        chkOnTop = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("StudyPulse – Pomodoro");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(34, 48, 63));
        lblTitle.setText("Pomodoro");

        lblTaskCaption.setText("Task");

        cmbTask.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No task" }));

        lblPhase.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblPhase.setForeground(new java.awt.Color(46, 125, 154));
        lblPhase.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPhase.setText("Focus");

        lblTimer.setFont(new java.awt.Font("Segoe UI", 1, 56)); // NOI18N
        lblTimer.setForeground(new java.awt.Color(34, 48, 63));
        lblTimer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTimer.setText("25:00");

        lblRounds.setForeground(new java.awt.Color(107, 119, 133));
        lblRounds.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRounds.setText("Round 1 of 4");

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnSkip.setText("Skip");
        btnSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkipActionPerformed(evt);
            }
        });

        lblFocus.setText("Focus (min)");

        lblShortBreak.setText("Short break");

        lblLongBreak.setText("Long break");

        spnFocus.setModel(new javax.swing.SpinnerNumberModel(25, 1, 90, 1));
        spnFocus.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFocusStateChanged(evt);
            }
        });

        spnShortBreak.setModel(new javax.swing.SpinnerNumberModel(5, 1, 30, 1));
        spnShortBreak.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnShortBreakStateChanged(evt);
            }
        });

        spnLongBreak.setModel(new javax.swing.SpinnerNumberModel(15, 5, 60, 1));
        spnLongBreak.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnLongBreakStateChanged(evt);
            }
        });

        chkOnTop.setText("Keep window on top");
        chkOnTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOnTopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblTaskCaption)
                    .addComponent(cmbTask, 0, 304, Short.MAX_VALUE)
                    .addComponent(lblPhase, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(lblTimer, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(prgTime, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(lblRounds, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReset, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSkip, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFocus)
                            .addComponent(spnFocus, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblShortBreak)
                            .addComponent(spnShortBreak, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblLongBreak)
                            .addComponent(spnLongBreak, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)))
                    .addComponent(chkOnTop))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTaskCaption)
                .addGap(4, 4, 4)
                .addComponent(cmbTask, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblPhase)
                .addGap(2, 2, 2)
                .addComponent(lblTimer)
                .addGap(6, 6, 6)
                .addComponent(prgTime, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblRounds)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSkip, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFocus)
                    .addComponent(lblShortBreak)
                    .addComponent(lblLongBreak))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnFocus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnShortBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnLongBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkOnTop)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(360, 560));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        askToSaveRunningFocus();
    }//GEN-LAST:event_formWindowClosing

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        startOrPause();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        resetPhase();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkipActionPerformed
        finishPhase(true);
    }//GEN-LAST:event_btnSkipActionPerformed

    private void spnFocusStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFocusStateChanged
        if (!phaseStarted()) {
            preparePhase();
        }
    }//GEN-LAST:event_spnFocusStateChanged

    private void spnShortBreakStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnShortBreakStateChanged
        if (!phaseStarted()) {
            preparePhase();
        }
    }//GEN-LAST:event_spnShortBreakStateChanged

    private void spnLongBreakStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnLongBreakStateChanged
        if (!phaseStarted()) {
            preparePhase();
        }
    }//GEN-LAST:event_spnLongBreakStateChanged

    private void chkOnTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOnTopActionPerformed
        setAlwaysOnTop(chkOnTop.isSelected());
    }//GEN-LAST:event_chkOnTopActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSkip;
    private javax.swing.JButton btnStart;
    private javax.swing.JCheckBox chkOnTop;
    private javax.swing.JComboBox<String> cmbTask;
    private javax.swing.JLabel lblFocus;
    private javax.swing.JLabel lblLongBreak;
    private javax.swing.JLabel lblPhase;
    private javax.swing.JLabel lblRounds;
    private javax.swing.JLabel lblShortBreak;
    private javax.swing.JLabel lblTaskCaption;
    private javax.swing.JLabel lblTimer;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JProgressBar prgTime;
    private javax.swing.JSpinner spnFocus;
    private javax.swing.JSpinner spnLongBreak;
    private javax.swing.JSpinner spnShortBreak;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new PomodoroFrame().setVisible(true);
        });
    }
}
