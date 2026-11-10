package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StatusPanel extends JPanel {

    //For Testing
    private static final Object[][] SAMPLE_TASKS = {
        {"T1", "(2, 3)", "(13, 13)", "Pending", "-"},
        {"T2", "(4, 12)", "(12, 3)", "Assigned", "R2"}
    };

    static {
        //check
        Map<Object, Object> taskAtPosition = new HashMap<>();
        for (Object[] task : SAMPLE_TASKS) {
            if ("Completed".equals(task[3])) continue;
            for (int column = 1; column <= 2; column++) {
                Object previous = taskAtPosition.putIfAbsent(task[column], task[0]);
                if (previous != null && !previous.equals(task[0])) {
                    throw new IllegalStateException("Preview tasks share a position: " + task[column]);
                }
            }
        }
    }

    public StatusPanel() {
        super(new GridLayout(2, 1, 5, 5));
        setPreferredSize(new Dimension(360, 480));

        //test
        Object[][] robots = {
            {"R1", "(1, 1)", "100%", "Idle", "-"},
            {"R2", "(7, 5)", "80%", "Assigned", "T2"},
            {"R3", "(13, 12)", "60%", "Idle", "-"}
        };

        //test
        add(createSection("Robots", robots,
                new String[] {"ID", "Position", "Battery", "Status", "Task"},
                new int[] {35, 80, 60, 80, 40}));

        //test
        add(createSection("Tasks", SAMPLE_TASKS,
                new String[] {"ID", "Pickup", "Delivery", "Status", "Robot"},
                new int[] {35, 65, 75, 75, 45}));
    }

    
    //tmp
    static String taskLabelAt(int row, int col) {
        String position = "(" + row + ", " + col + ")";
        for (Object[] task : SAMPLE_TASKS) {
            if ("Completed".equals(task[3])) continue;
            String id = (String) task[0];
            String number = id.startsWith("T") ? id.substring(1) : id;
            if (position.equals(task[1])) return "P" + number;
            if (position.equals(task[2])) return "D" + number;
        }
        return "";
    }

    private JPanel createSection(String title, Object[][] rows, String[] columns, int[] widths) {
        JPanel section = new JPanel(new BorderLayout(5, 5));
        JLabel heading = new JLabel(title);
        section.add(heading, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane scroll = new JScrollPane(table);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }
}
