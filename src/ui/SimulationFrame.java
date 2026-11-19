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

public class SimulationFrame extends JFrame {
    private final WarehousePanel warehousePanel = new WarehousePanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final JButton nextStepButton = new JButton("Next Step");
    private final JLabel stepLabel = new JLabel("Step: -");
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
    }

    public void onNextStep(Runnable action) {
        nextStepAction = action;
        nextStepButton.setEnabled(hasState && nextStepAction != null);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Simulation error", JOptionPane.ERROR_MESSAGE);
    }
}
