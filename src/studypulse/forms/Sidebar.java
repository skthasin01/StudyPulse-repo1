package studypulse.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Left navigation bar of the main screens. pnlSidebar is an empty JPanel in every form;
 * the buttons are added here at runtime. A click opens that screen and closes the current one.
 */
public final class Sidebar {

    public static final String DASHBOARD = "Dashboard";
    public static final String TASKS = "Tasks";
    public static final String STUDY_PLAN = "Study Plan";
    public static final String CHECKIN = "Check-in";
    public static final String ANALYTICS = "Analytics";
    public static final String NOTIFICATIONS = "Notifications";
    public static final String SETTINGS = "Settings";
    public static final String LOGOUT = "Log out";

    private static final String[] ITEMS = {DASHBOARD, TASKS, STUDY_PLAN, CHECKIN, ANALYTICS, NOTIFICATIONS, SETTINGS};

    private static final Color BACKGROUND = new Color(34, 48, 63);
    private static final Color ACTIVE = new Color(46, 125, 154);
    private static final Color TEXT = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(190, 200, 210);

    private Sidebar() {
    }

    public static void attach(JFrame frame, JPanel sidebar, String current) {
        sidebar.removeAll();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BACKGROUND);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 16, 0));

        JLabel logo = new JLabel("StudyPulse");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(TEXT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 18, 24, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        for (String item : ITEMS) {
            sidebar.add(button(frame, item, item.equals(current)));
        }
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(button(frame, LOGOUT, false));
        sidebar.revalidate();
    }

    private static JButton button(final JFrame frame, final String target, boolean active) {
        JButton button = new JButton(target);
        button.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
        button.setForeground(active ? TEXT : TEXT_MUTED);
        button.setBackground(active ? ACTIVE : BACKGROUND);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 10));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (!active) {
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    navigate(frame, target);
                }
            });
        }
        return button;
    }

    /** Opens the screen named by 'target' (one of the constants) and closes 'from'. */
    public static void navigate(JFrame from, String target) {
        JFrame next;
        if (DASHBOARD.equals(target)) {
            next = new DashboardFrame();
        } else if (TASKS.equals(target)) {
            next = new TaskFrame();
        } else if (STUDY_PLAN.equals(target)) {
            next = new StudyPlanFrame();
        } else if (CHECKIN.equals(target)) {
            next = new CheckinFrame();
        } else if (ANALYTICS.equals(target)) {
            next = new AnalyticsFrame();
        } else if (NOTIFICATIONS.equals(target)) {
            next = new NotificationFrame();
        } else if (SETTINGS.equals(target)) {
            next = new SettingsFrame();
        } else {
            PomodoroFrame.closeWindow();
            next = new LoginFrame(studypulse.Main.userEmail);
        }
        next.setVisible(true);
        from.dispose();
    }
}
