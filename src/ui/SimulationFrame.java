package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.SimulationState;
import model.Task;

public class SimulationFrame extends JFrame {
    private final WarehousePanel warehousePanel = new WarehousePanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final JButton addTaskButton = new JButton("Add Task");
    private final JButton nextStepButton = new JButton("Next Step");
    private final JButton resetButton = new JButton("Reset");
    private final JLabel stepLabel = new JLabel("Step: -");
    private final JLabel taskSummary = new JLabel("No tasks");
    private final JLabel completionMessage = new JLabel();
    private Runnable nextStepAction;
    private Runnable addTaskAction;
    private Runnable resetAction;
    private boolean hasState;
    private int sceneRows;
    private int sceneCols;

    /** 输入数据只包含行列坐标，任务编号由 Application 生成。 */
    public static final class TaskInput {
        private final int pickupRow;
        private final int pickupCol;
        private final int deliveryRow;
        private final int deliveryCol;

        public TaskInput(int pickupRow, int pickupCol, int deliveryRow, int deliveryCol) {
            this.pickupRow = pickupRow;
            this.pickupCol = pickupCol;
            this.deliveryRow = deliveryRow;
            this.deliveryCol = deliveryCol;
        }

        public int getPickupRow() { return pickupRow; }
        public int getPickupCol() { return pickupCol; }
        public int getDeliveryRow() { return deliveryRow; }
        public int getDeliveryCol() { return deliveryCol; }
    }

    public SimulationFrame() {
        super("Robot Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1000, 720);

        JPanel content = new JPanel(new BorderLayout(5, 5));
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(content);
        content.add(createToolbar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(warehousePanel, BorderLayout.CENTER);
        center.add(statusPanel, BorderLayout.EAST);
        content.add(center, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(5, 5));
        footer.add(taskSummary, BorderLayout.CENTER);
        footer.add(completionMessage, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);
        statusPanel.onSelectionChanged(warehousePanel::setSelection);

        setLocationRelativeTo(null);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        addTaskButton.setEnabled(false);
        addTaskButton.addActionListener(event -> {
            if (addTaskAction != null) addTaskAction.run();
        });
        toolbar.add(addTaskButton);
        nextStepButton.setEnabled(false);
        nextStepButton.addActionListener(event -> {
            if (nextStepAction != null) nextStepAction.run();
        });
        toolbar.add(nextStepButton);
        resetButton.setEnabled(false);
        resetButton.addActionListener(event -> {
            if (resetAction != null) resetAction.run();
        });
        toolbar.add(resetButton);
        toolbar.add(stepLabel);
        return toolbar;
    }

    public void render(SimulationState state) {
        warehousePanel.render(state);
        statusPanel.render(state);
        hasState = state != null;
        sceneRows = hasState ? state.getRows() : 0;
        sceneCols = hasState ? state.getCols() : 0;
        stepLabel.setText(hasState ? "Step: " + state.getCurrentStep() : "Step: -");
        updateControls();
        int pending = 0;
        int assigned = 0;
        int delivering = 0;
        int completed = 0;
        if (state != null) {
            for (SimulationState.TaskState task : state.getTasks()) {
                if (task.getStatus() == Task.Status.PENDING) pending++;
                else if (task.getStatus() == Task.Status.ASSIGNED) assigned++;
                else if (task.getStatus() == Task.Status.IN_PROGRESS) delivering++;
                else if (task.getStatus() == Task.Status.COMPLETED) completed++;
            }
        }
        taskSummary.setText("Pending: " + pending + "   Assigned: " + assigned
                + "   Delivering: " + delivering + "   Completed: " + completed);
        boolean allCompleted = state != null && !state.getTasks().isEmpty()
                && completed == state.getTasks().size();
        completionMessage.setText(allCompleted ? "All tasks completed" : "");
        completionMessage.setForeground(StatusColors.COMPLETED);
    }

    public void onNextStep(Runnable action) {
        nextStepAction = action;
        updateControls();
    }

    public void onAddTask(Runnable action) {
        addTaskAction = action;
        updateControls();
    }

    public void onReset(Runnable action) {
        resetAction = action;
        updateControls();
    }

    private void updateControls() {
        addTaskButton.setEnabled(hasState && sceneRows > 0 && sceneCols > 0 && addTaskAction != null);
        nextStepButton.setEnabled(hasState && nextStepAction != null);
        resetButton.setEnabled(hasState && resetAction != null);
    }

    /** 取消返回 null；格式错误时保留原文本，业务校验由 Application 处理。 */
    public TaskInput requestTaskInput(TaskInput initialValues) {
        if (!hasState || sceneRows == 0 || sceneCols == 0) return null;
        TaskInput values = initialValues == null ? new TaskInput(0, 0, 0, 1) : initialValues;
        JTextField pickupRow = new JTextField(String.valueOf(values.getPickupRow()), 6);
        JTextField pickupCol = new JTextField(String.valueOf(values.getPickupCol()), 6);
        JTextField deliveryRow = new JTextField(String.valueOf(values.getDeliveryRow()), 6);
        JTextField deliveryCol = new JTextField(String.valueOf(values.getDeliveryCol()), 6);
        JPanel fields = new JPanel(new GridLayout(4, 2, 5, 5));
        fields.add(new JLabel("Pickup row"));
        fields.add(pickupRow);
        fields.add(new JLabel("Pickup column"));
        fields.add(pickupCol);
        fields.add(new JLabel("Delivery row"));
        fields.add(deliveryRow);
        fields.add(new JLabel("Delivery column"));
        fields.add(deliveryCol);
        JPanel form = new JPanel(new BorderLayout(5, 5));
        form.add(new JLabel("Rows: 0-" + (sceneRows - 1) + ", columns: 0-" + (sceneCols - 1)
                + ". Task ID: automatic."), BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        while (JOptionPane.showConfirmDialog(this, form, "Add Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                return new TaskInput(Integer.parseInt(pickupRow.getText().trim()),
                        Integer.parseInt(pickupCol.getText().trim()),
                        Integer.parseInt(deliveryRow.getText().trim()),
                        Integer.parseInt(deliveryCol.getText().trim()));
            } catch (NumberFormatException exception) {
                showError("Enter whole numbers for all four coordinates.");
            }
        }
        return null;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Simulation error", JOptionPane.ERROR_MESSAGE);
    }
}
