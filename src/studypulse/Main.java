package studypulse;

import studypulse.forms.LoginFrame;

/**
 * Starts StudyPulse on the login screen. Plain Swing: no look-and-feel is installed,
 * every colour comes from the frames themselves.
 */
public class Main {

    /** Name and email of the logged-in student, shown on the dashboard and in Settings. */
    public static String userName = "Student";
    public static String userEmail = "";

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
