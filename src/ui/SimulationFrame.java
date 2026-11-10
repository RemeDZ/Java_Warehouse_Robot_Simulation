package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimulationFrame extends JFrame {
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
        center.add(new WarehousePanel(), BorderLayout.CENTER);
        center.add(new StatusPanel(), BorderLayout.EAST);
        content.add(center, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        for (String text : new String[] {"Add Task", "Next Step","Reset" }) {
            JButton button = new JButton(text);
            toolbar.add(button);
        }
        return toolbar;
    }
}
