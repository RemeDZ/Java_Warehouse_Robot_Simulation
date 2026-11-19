package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.SimulationState;

public class StatusPanel extends JPanel {
    private final DefaultTableModel robotModel = readOnlyModel(
            new String[] {"ID", "Position", "Battery", "Status", "Task"});
    private final DefaultTableModel taskModel = readOnlyModel(
            new String[] {"ID", "Pickup", "Delivery", "Status", "Robot"});

    public StatusPanel() {
        super(new GridLayout(2, 1, 5, 5));
        setPreferredSize(new Dimension(360, 480));

        add(createSection("Robots", robotModel,
                new int[] {35, 80, 60, 90, 40}));
        add(createSection("Tasks", taskModel,
                new int[] {35, 65, 75, 75, 45}));
    }

    public void render(SimulationState state) {
        robotModel.setRowCount(0);
        taskModel.setRowCount(0);
        if (state == null) return;
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

    private JPanel createSection(String title, DefaultTableModel model, int[] widths) {
        JPanel section = new JPanel(new BorderLayout(5, 5));
        JLabel heading = new JLabel(title);
        section.add(heading, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane scroll = new JScrollPane(table);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }
}
