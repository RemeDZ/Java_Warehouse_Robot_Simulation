package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.SimulationState;
import model.Task;

public class SimulationFrame extends JFrame {
    private final WarehousePanel warehousePanel = new WarehousePanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final JButton nextStepButton = new JButton("Next Step");
    private final JLabel stepLabel = new JLabel("Step: -");
    private final JLabel taskSummary = new JLabel("No tasks");
    private final JLabel completionMessage = new JLabel();
    private Runnable nextStepAction;
    private boolean hasState;

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
        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.setEnabled(false);
        toolbar.add(addTaskButton);
        nextStepButton.setEnabled(false);
        nextStepButton.addActionListener(event -> {
            if (nextStepAction != null) nextStepAction.run();
        });
        toolbar.add(nextStepButton);
        JButton resetButton = new JButton("Reset");
        resetButton.setEnabled(false);
        toolbar.add(resetButton);
        toolbar.add(stepLabel);
        return toolbar;
    }

    public void render(SimulationState state) {
        warehousePanel.render(state);
        statusPanel.render(state);
        hasState = state != null;
        stepLabel.setText(hasState ? "Step: " + state.getCurrentStep() : "Step: -");
        nextStepButton.setEnabled(hasState && nextStepAction != null);
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
        nextStepButton.setEnabled(hasState && nextStepAction != null);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Simulation error", JOptionPane.ERROR_MESSAGE);
    }
}
