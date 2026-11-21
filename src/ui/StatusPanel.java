package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.function.BiConsumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.SimulationState;
import model.Task;

public class StatusPanel extends JPanel {
    private final DefaultTableModel robotModel = readOnlyModel(
            new String[] {"ID", "Position", "Battery", "Status", "Task"});
    private final DefaultTableModel taskModel = readOnlyModel(
            new String[] {"ID", "Pickup", "Delivery", "Status", "Robot"});
    private final JTable robotTable;
    private final JTable taskTable;
    private SimulationState state;
    private Integer primaryRobotId;
    private String primaryTaskId;
    private boolean updating;
    private BiConsumer<Integer, String> selectionChanged = (robotId, taskId) -> { };

    public StatusPanel() {
        super(new GridLayout(2, 1, 5, 5));
        setPreferredSize(new Dimension(360, 480));

        robotTable = createTable(robotModel, new int[] {35, 80, 60, 90, 40}, true);
        taskTable = createTable(taskModel, new int[] {35, 65, 75, 75, 45}, false);
        add(createSection("Robots", robotTable));
        add(createSection("Tasks", taskTable));
        robotTable.getSelectionModel().addListSelectionListener(event -> {
            if (updating || event.getValueIsAdjusting() || state == null) return;
            int row = robotTable.getSelectedRow();
            primaryRobotId = row < 0 ? null : state.getRobots().get(
                    robotTable.convertRowIndexToModel(row)).getId();
            primaryTaskId = null;
            updateSelection();
        });
        taskTable.getSelectionModel().addListSelectionListener(event -> {
            if (updating || event.getValueIsAdjusting() || state == null) return;
            int row = taskTable.getSelectedRow();
            primaryTaskId = row < 0 ? null : state.getTasks().get(
                    taskTable.convertRowIndexToModel(row)).getId();
            primaryRobotId = null;
            updateSelection();
        });
        followClickedRow(robotTable, true);
        followClickedRow(taskTable, false);
    }

    private void followClickedRow(JTable table, boolean robots) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                if (updating || state == null || event.getButton() != MouseEvent.BUTTON1) return;
                int row = table.rowAtPoint(event.getPoint());
                if (row < 0 || table.getSelectedRow() != row) return;
                int modelRow = table.convertRowIndexToModel(row);
                primaryRobotId = robots ? state.getRobots().get(modelRow).getId() : null;
                primaryTaskId = robots ? null : state.getTasks().get(modelRow).getId();
                updateSelection();
            }
        });
    }

    public void render(SimulationState state) {
        Point robotScroll = scrollPosition(robotTable);
        Point taskScroll = scrollPosition(taskTable);
        updating = true;
        try {
            this.state = state;
            robotModel.setRowCount(0);
            taskModel.setRowCount(0);
            if (state != null) fillRows(state);
            updateSelection();
        } finally {
            updating = false;
        }
        restoreScroll(robotTable, robotScroll);
        restoreScroll(taskTable, taskScroll);
    }

    private void fillRows(SimulationState state) {
        for (SimulationState.RobotState robot : state.getRobots()) {
            robotModel.addRow(new Object[] {
                "R" + robot.getId(), text(robot.getPosition()), robot.getBattery() + "%",
                statusText(robot.getStatus()), text(robot.getCurrentTaskId())
            });
        }
        for (SimulationState.TaskState task : state.getTasks()) {
            taskModel.addRow(new Object[] {
                task.getId(), text(task.getPickupPosition()), text(task.getDeliveryPosition()),
                statusText(task.getStatus()),
                task.getAssignedRobotId() == null ? "-" : "R" + task.getAssignedRobotId()
            });
        }
    }

    public void onSelectionChanged(BiConsumer<Integer, String> callback) {
        selectionChanged = callback == null ? (robotId, taskId) -> { } : callback;
        updateSelection();
    }

    private void updateSelection() {
        boolean wasUpdating = updating;
        updating = true;
        int robotRow = -1;
        int taskRow = -1;
        try {
            if (state != null && primaryRobotId != null) {
                robotRow = robotRow(primaryRobotId);
                if (robotRow >= 0) taskRow = taskRow(state.getRobots().get(robotRow).getCurrentTaskId());
                else primaryRobotId = null;
            } else if (state != null && primaryTaskId != null) {
                taskRow = taskRow(primaryTaskId);
                if (taskRow >= 0) {
                    String assignedId = state.getTasks().get(taskRow).getAssignedRobotId();
                    for (int i = 0; i < state.getRobots().size(); i++) {
                        if (String.valueOf(state.getRobots().get(i).getId()).equals(assignedId)) {
                            robotRow = i;
                            break;
                        }
                    }
                } else primaryTaskId = null;
            } else if (state == null) {
                primaryRobotId = null;
                primaryTaskId = null;
            }
            selectRow(robotTable, robotRow);
            selectRow(taskTable, taskRow);
        } finally {
            updating = wasUpdating;
        }
        selectionChanged.accept(robotRow < 0 ? null : state.getRobots().get(robotRow).getId(),
                taskRow < 0 ? null : state.getTasks().get(taskRow).getId());
    }

    private int robotRow(int id) {
        for (int i = 0; i < state.getRobots().size(); i++) {
            if (state.getRobots().get(i).getId() == id) return i;
        }
        return -1;
    }

    private int taskRow(String id) {
        if (id == null) return -1;
        for (int i = 0; i < state.getTasks().size(); i++) {
            if (id.equals(state.getTasks().get(i).getId())) return i;
        }
        return -1;
    }

    private static void selectRow(JTable table, int modelRow) {
        if (modelRow < 0) table.clearSelection();
        else {
            int row = table.convertRowIndexToView(modelRow);
            table.setRowSelectionInterval(row, row);
        }
    }

    private static Point scrollPosition(JTable table) {
        return table.getParent() instanceof JViewport
                ? ((JViewport) table.getParent()).getViewPosition() : null;
    }

    private static void restoreScroll(JTable table, Point position) {
        if (position == null || !(table.getParent() instanceof JViewport)) return;
        JViewport viewport = (JViewport) table.getParent();
        Dimension extent = viewport.getExtentSize();
        Dimension size = table.getPreferredSize();
        viewport.setViewPosition(new Point(Math.min(position.x, Math.max(0, size.width - extent.width)),
                Math.min(position.y, Math.max(0, size.height - extent.height))));
    }

    private static String text(Object value) {
        return value == null ? "-" : value.toString();
    }

    private static String statusText(Enum<?> status) {
        if (status == null) return "-";
        String value = status.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createSection(String title, JTable table) {
        JPanel section = new JPanel(new BorderLayout(5, 5));
        JLabel heading = new JLabel(title);
        section.add(heading, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private JTable createTable(DefaultTableModel model, int[] widths, boolean robots) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component cell = super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                if (selected || state == null) return cell;
                cell.setBackground(Color.WHITE);
                cell.setForeground(source.getForeground());
                int modelRow = source.convertRowIndexToModel(row);
                int modelColumn = source.convertColumnIndexToModel(column);
                Color color;
                if (robots) {
                    SimulationState.RobotState robot = state.getRobots().get(modelRow);
                    color = StatusColors.robot(robot.getStatus());
                    if (modelColumn == 2 && robot.getStatus() == SimulationState.RobotStatus.LOW_BATTERY) {
                        cell.setForeground(StatusColors.LOW_BATTERY);
                        cell.setBackground(StatusColors.background(StatusColors.LOW_BATTERY));
                    }
                } else {
                    Task.Status status = state.getTasks().get(modelRow).getStatus();
                    color = StatusColors.task(status);
                    if (status == Task.Status.COMPLETED) cell.setForeground(Color.GRAY);
                }
                if (modelColumn == 3) {
                    cell.setForeground(color);
                    cell.setBackground(StatusColors.background(color));
                }
                return cell;
            }
        });
        return table;
    }
}
