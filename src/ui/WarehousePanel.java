package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

//row, col
public class WarehousePanel extends JPanel {

    private static final int ROWS = 16;
    private static final int COLS = 16;
    private static final Color FLOOR = Color.WHITE;
    private static final Color OBSTACLE = Color.GRAY;

    private static final Color PICKUP = new Color(190, 255, 190);
    private static final Color DELIVERY = new Color(255, 255, 190);
    private static final Color CHARGER = new Color(190, 220, 255);

    private static final int[][] ROBOT_POSITIONS = {{1, 1}, {7, 5}, {13, 12}};//TMP

    public WarehousePanel() {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel title = new JLabel("Warehouse");
        add(title, BorderLayout.NORTH);

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintWarehouse(g, getWidth(), getHeight());
            }
        };
        canvas.setBackground(Color.WHITE);
        canvas.setPreferredSize(new Dimension(480, 480));
        add(canvas, BorderLayout.CENTER);

        JPanel legend = new JPanel(new GridLayout(2, 1, 5, 5));
        legend.add(new JLabel("Pn: Pickup   Dn: Delivery   n: Task ID   C: Charger   Gray: Wall Blue: Robot"));
        add(legend, BorderLayout.SOUTH);
    }

    private void paintWarehouse(Graphics graphics, int width, int height) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            int cell = Math.min((width - 36) / COLS, (height - 36) / ROWS);

            if (cell < 1)
                return;

            int left = 28 + (width - 36 - cell * COLS) / 2;
            int top = 28 + (height - 36 - cell * ROWS) / 2;

            g.setFont(getFont());
            g.setColor(Color.BLACK);

            for (int i = 0; i < ROWS; i++) {
                drawCentered(g, String.valueOf(i), left - 25, top + i * cell, 20, cell);
                drawCentered(g, String.valueOf(i), left + i * cell, top - 25, cell, 20);
            }

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    char tile = sampleTile(row, col);
                    int x = left + col * cell;
                    int y = top + row * cell;
                    g.setColor(tileColor(tile));
                    g.fillRect(x, y, cell, cell);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, cell, cell);
                    if (tile == 'P' || tile == 'D' || tile == 'C') {
                        g.setColor(Color.BLACK);
                        String label = tile == 'C' ? "C" : StatusPanel.taskLabelAt(row, col);
                        drawCentered(g, label, x, y, cell, cell);
                    }
                }
            }

            for (int i = 0; i < ROBOT_POSITIONS.length; i++) {
                int x = left + ROBOT_POSITIONS[i][1] * cell;
                int y = top + ROBOT_POSITIONS[i][0] * cell;
                int inset = Math.max(2, cell / 10);
                g.setColor(Color.BLUE);
                g.fillOval(x + inset, y + inset, cell - inset * 2, cell - inset * 2);
                g.setColor(Color.WHITE);
                drawCentered(g, "R" + (i + 1), x, y, cell, cell);
            }

        } finally {
            g.dispose();
        }
    }

    private char sampleTile(int row, int col) {

        if ((row == 5 && col >= 4 && col <= 10)
                || (row == 10 && col >= 3 && col <= 8)) {
            return '#';
        }

        String taskLabel = StatusPanel.taskLabelAt(row, col);

        if (!taskLabel.isEmpty())
            return taskLabel.charAt(0);

        if (row == 1 && col == 14)
            return 'C';

        return '_';
    }

    private Color tileColor(char tile) {
        switch (tile) {
            case '#': return OBSTACLE;
            case 'P': return PICKUP;
            case 'D': return DELIVERY;
            case 'C': return CHARGER;
            default: return FLOOR;
        }
    }

    private void drawCentered(Graphics2D g, String text, int x, int y, int width, int height) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x + (width - metrics.stringWidth(text)) / 2,
                y + (height - metrics.getHeight()) / 2 + metrics.getAscent());
    }
}
