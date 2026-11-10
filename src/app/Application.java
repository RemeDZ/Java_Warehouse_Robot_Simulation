package app;

import javax.swing.SwingUtilities;
import ui.SimulationFrame;

public class Application {

    public void init() {

    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            SimulationFrame window = new SimulationFrame();
            window.setVisible(true);
        });

    }
}
