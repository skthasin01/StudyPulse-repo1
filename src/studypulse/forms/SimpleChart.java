package studypulse.forms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Small bar / line chart painted with Graphics2D, used on the dashboard and the analytics screen.
 */
public class SimpleChart extends JPanel {

    private static final Color TITLE = new Color(34, 48, 63);
    private static final Color AXIS = new Color(107, 119, 133);
    private static final Color GRID = new Color(230, 234, 238);
    private static final int GRID_LINES = 4;

    private final String title;
    private final String[] labels;
    private final boolean line;
    private final List<String> seriesNames = new ArrayList<String>();
    private final List<double[]> seriesValues = new ArrayList<double[]>();
    private final List<Color> seriesColors = new ArrayList<Color>();

    /** 'line' = true draws a line chart, false a bar chart. */
    public SimpleChart(String title, String[] labels, boolean line) {
        this.title = title;
        this.labels = labels;
        this.line = line;
        setBackground(Color.WHITE);
    }

    public SimpleChart addSeries(String name, double[] values, Color color) {
        seriesNames.add(name);
        seriesValues.add(values);
        seriesColors.add(color);
        repaint();
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(TITLE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(title, 14, 22);
        paintLegend(g2);

        int left = 44;
        int right = getWidth() - 16;
        int top = 40;
        int bottom = getHeight() - 28;
        if (right <= left || bottom <= top || labels.length == 0) {
            g2.dispose();
            return;
        }

        double max = 1;
        for (double[] values : seriesValues) {
            for (double v : values) {
                max = Math.max(max, v);
            }
        }
        // round up so every grid line gets a whole number
        max = Math.ceil(max * 1.1 / GRID_LINES) * GRID_LINES;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= GRID_LINES; i++) {
            int y = bottom - (bottom - top) * i / GRID_LINES;
            g2.setColor(GRID);
            g2.drawLine(left, y, right, y);
            g2.setColor(AXIS);
            String text = format(max * i / GRID_LINES);
            g2.drawString(text, left - 6 - fm.stringWidth(text), y + 4);
        }

        double slot = (right - left) / (double) labels.length;
        for (int i = 0; i < labels.length; i++) {
            int x = (int) (left + slot * i + slot / 2);
            g2.setColor(AXIS);
            g2.drawString(labels[i], x - fm.stringWidth(labels[i]) / 2, bottom + 16);
        }

        for (int s = 0; s < seriesValues.size(); s++) {
            double[] values = seriesValues.get(s);
            g2.setColor(seriesColors.get(s));
            if (line) {
                g2.setStroke(new BasicStroke(2.5f));
                int prevX = -1;
                int prevY = -1;
                for (int i = 0; i < values.length && i < labels.length; i++) {
                    int x = (int) (left + slot * i + slot / 2);
                    int y = (int) (bottom - (bottom - top) * values[i] / max);
                    if (prevX >= 0) {
                        g2.drawLine(prevX, prevY, x, y);
                    }
                    g2.fillOval(x - 4, y - 4, 8, 8);
                    prevX = x;
                    prevY = y;
                }
            } else {
                int groupWidth = (int) (slot * 0.6);
                int barWidth = Math.max(4, groupWidth / seriesValues.size());
                for (int i = 0; i < values.length && i < labels.length; i++) {
                    int x = (int) (left + slot * i + (slot - groupWidth) / 2) + s * barWidth;
                    int h = (int) ((bottom - top) * values[i] / max);
                    g2.fillRect(x, bottom - h, barWidth - 2, h);
                }
            }
        }
        g2.dispose();
    }

    private void paintLegend(Graphics2D g2) {
        if (seriesNames.size() < 2) {
            return;
        }
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int x = getWidth() - 14;
        for (int s = seriesNames.size() - 1; s >= 0; s--) {
            String name = seriesNames.get(s);
            x -= fm.stringWidth(name);
            g2.setColor(AXIS);
            g2.drawString(name, x, 22);
            x -= 14;
            g2.setColor(seriesColors.get(s));
            g2.fillRect(x, 13, 10, 10);
            x -= 12;
        }
    }

    private static String format(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }
}
