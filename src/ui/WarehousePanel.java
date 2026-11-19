package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Position;
import model.SimulationState;
import model.Task;

//row, col
public class WarehousePanel extends JPanel {

    private static final Color FLOOR = Color.WHITE;
    private static final Color OBSTACLE = Color.GRAY;

    private static final Color PICKUP = new Color(190, 255, 190);
    private static final Color DELIVERY = new Color(255, 255, 190);
    private static final Color CHARGER = new Color(190, 220, 255);

    private final JPanel canvas;
    private SimulationState state;

    public WarehousePanel() {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel title = new JLabel("Warehouse");
        add(title, BorderLayout.NORTH);

        canvas = new JPanel() {
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
        legend.add(new JLabel("Pn: Pickup   Dn: Delivery   n: Task ID   C: Charger   Gray: Wall"));
        legend.add(new JLabel("Blue: Robot   Coordinates: (row, col), start at 0"));
        add(legend, BorderLayout.SOUTH);
    }

    public void render(SimulationState state) {
        this.state = state;
        canvas.repaint();
    }

    private void paintWarehouse(Graphics graphics, int width, int height) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            SimulationState scene = state;
            g.setFont(getFont());
            g.setColor(Color.BLACK);
            if (scene == null || scene.getRows() == 0 || scene.getCols() == 0) {
                drawCentered(g, scene == null ? "No simulation loaded" : "Empty warehouse",
                        0, 0, width, height);
                return;
            }
            int rows = scene.getRows();
            int cols = scene.getCols();
            int cell = Math.min((width - 36) / cols, (height - 36) / rows);

            if (cell < 1)
                return;

            int left = 28 + (width - 36 - cell * cols) / 2;
            int top = 28 + (height - 36 - cell * rows) / 2;

            g.setFont(getFont());
            g.setColor(Color.BLACK);

            for (int i = 0; i < rows; i++) {
                drawCentered(g, String.valueOf(i), left - 25, top + i * cell, 20, cell);
            }
            for (int i = 0; i < cols; i++) {
                drawCentered(g, String.valueOf(i), left + i * cell, top - 25, cell, 20);
            }

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    char tile = scene.getCell(row, col);
                    String taskLabel = taskLabelAt(scene, row, col);
                    if (!taskLabel.isEmpty() && (tile == '_' || tile == 'P' || tile == 'D')) {
                        tile = taskLabel.charAt(0);
                    }
                    int x = left + col * cell;
                    int y = top + row * cell;
                    g.setColor(tileColor(tile));
                    g.fillRect(x, y, cell, cell);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, cell, cell);
                    if (tile == 'P' || tile == 'D' || tile == 'C') {
                        g.setColor(Color.BLACK);
                        String label = tile == 'C' || taskLabel.isEmpty()
                                ? String.valueOf(tile) : taskLabel;
                        drawCentered(g, label, x, y, cell, cell);
                    }
                }
            }

            for (SimulationState.RobotState robot : scene.getRobots()) {
                Position position = robot.getPosition();
                if (position == null || position.getRow() < 0 || position.getRow() >= rows
                        || position.getCol() < 0 || position.getCol() >= cols) continue;
                int x = left + position.getCol() * cell;
                int y = top + position.getRow() * cell;
                int inset = Math.max(2, cell / 10);
                g.setColor(Color.BLUE);
                g.fillOval(x + inset, y + inset, cell - inset * 2, cell - inset * 2);
                g.setColor(Color.WHITE);
                drawCentered(g, "R" + robot.getId(), x, y, cell, cell);
            }

        } finally {
            g.dispose();
        }
    }

    private String taskLabelAt(SimulationState scene, int row, int col) {
        for (SimulationState.TaskState task : scene.getTasks()) {
            if (task.getStatus() == Task.Status.COMPLETED) continue;
            String id = task.getId();
            String number = id.startsWith("T") ? id.substring(1) : id;
            if (matches(task.getPickupPosition(), row, col)) return "P" + number;
            if (matches(task.getDeliveryPosition(), row, col)) return "D" + number;
        }
        return "";
    }

    private boolean matches(Position position, int row, int col) {
        return position != null && position.getRow() == row && position.getCol() == col;
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
        Font original = g.getFont();
        FontMetrics metrics = g.getFontMetrics();
        while ((metrics.stringWidth(text) > width - 2 || metrics.getHeight() > height - 2)
                && g.getFont().getSize2D() > 1f) {
            g.setFont(g.getFont().deriveFont(g.getFont().getSize2D() - 1f));
            metrics = g.getFontMetrics();
        }
        g.drawString(text, x + (width - metrics.stringWidth(text)) / 2,
                y + (height - metrics.getHeight()) / 2 + metrics.getAscent());
        g.setFont(original);
    }
}
