package ui;

import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
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
    private Integer selectedRobotId;
    private String selectedTaskId;

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

        JPanel legend = new JPanel(new GridLayout(4, 1, 5, 3));
        legend.add(new JLabel("Pn: Pickup   Dn: Delivery   C: Charger   Gray cell: Wall"));
        legend.add(new JLabel("Status: Gray pending/idle | Orange assigned | Blue delivering"));
        legend.add(new JLabel("Green: Completed   Red: Low battery   Purple outline: Selected"));
        legend.add(new JLabel("R: Robot   Coordinates: (row, col), start at 0"));
        add(legend, BorderLayout.SOUTH);
    }

    public void render(SimulationState state) {
        this.state = state;
        canvas.repaint();
    }

    public void setSelection(Integer robotId, String taskId) {
        selectedRobotId = robotId;
        selectedTaskId = taskId;
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
                    SimulationState.TaskState task = taskAt(scene, row, col);
                    boolean pickup = task != null && matches(task.getPickupPosition(), row, col);
                    if (task != null && (tile == '_' || tile == 'P' || tile == 'D')) {
                        tile = pickup ? 'P' : 'D';
                    }
                    boolean visited = task != null && (task.getStatus() == Task.Status.COMPLETED
                            || (pickup && task.getStatus() == Task.Status.IN_PROGRESS));
                    int x = left + col * cell;
                    int y = top + row * cell;
                    Color background = tileColor(tile);
                    g.setColor(visited ? StatusColors.background(background) : background);
                    g.fillRect(x, y, cell, cell);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, cell, cell);
                    if (task != null && task.getStatus() != Task.Status.COMPLETED) {
                        outline(g, visited ? Color.LIGHT_GRAY : StatusColors.task(task.getStatus()),
                                x + 3, y + 3, cell - 7, cell - 7, 2f);
                    }
                    if (task != null && task.getId().equals(selectedTaskId)) {
                        outline(g, StatusColors.SELECTED, x + 1, y + 1, cell - 3, cell - 3, 2f);
                    }
                    if (tile == 'P' || tile == 'D' || tile == 'C') {
                        g.setColor(visited ? Color.GRAY : task == null ? Color.BLACK
                                : StatusColors.task(task.getStatus()));
                        String label = stationLabel(tile, task);
                        drawCentered(g, label, x + 3, y + 2, cell - 6, cell - 4);
                    }
                }
            }

            // 机器人统一最后绘制，直接覆盖下方的站点标记。
            for (SimulationState.RobotState robot : scene.getRobots()) {
                Position position = robot.getPosition();
                if (position == null || position.getRow() < 0 || position.getRow() >= rows
                        || position.getCol() < 0 || position.getCol() >= cols) continue;
                int x = left + position.getCol() * cell;
                int y = top + position.getRow() * cell;
                int robotX = x + 4;
                int robotY = y + 4;
                int robotWidth = cell - 8;
                int robotHeight = cell - 8;
                if (robotWidth <= 0 || robotHeight <= 0) continue;
                g.setColor(StatusColors.robot(robot.getStatus()));
                g.fillOval(robotX, robotY, robotWidth, robotHeight);
                if (selectedRobotId != null && robot.getId() == selectedRobotId) {
                    Stroke oldStroke = g.getStroke();
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(StatusColors.SELECTED);
                    g.drawOval(robotX, robotY, robotWidth, robotHeight);
                    g.setStroke(oldStroke);
                }
                g.setColor(Color.WHITE);
                drawCentered(g, "R" + robot.getId(), robotX, robotY, robotWidth, robotHeight);
            }

        } finally {
            g.dispose();
        }
    }

    private SimulationState.TaskState taskAt(SimulationState scene, int row, int col) {
        SimulationState.TaskState completed = null;
        for (SimulationState.TaskState task : scene.getTasks()) {
            if (!matches(task.getPickupPosition(), row, col)
                    && !matches(task.getDeliveryPosition(), row, col)) continue;
            if (task.getStatus() != Task.Status.COMPLETED) return task;
            completed = task;
        }
        return completed;
    }

    private String stationLabel(char tile, SimulationState.TaskState task) {
        if (tile == 'C' || task == null || task.getStatus() == Task.Status.COMPLETED) {
            return String.valueOf(tile);
        }
        String id = task.getId();
        return tile + (id.startsWith("T") ? id.substring(1) : id);
    }

    private void outline(Graphics2D g, Color color, int x, int y, int width, int height, float thickness) {
        if (width <= 0 || height <= 0) return;
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(thickness));
        g.setColor(color);
        g.drawRect(x, y, width, height);
        g.setStroke(oldStroke);
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
