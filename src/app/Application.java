package app;

import javax.swing.SwingUtilities;
import model.Position;
import model.StandardRobot;
import model.Task;
import model.Warehouse;
import service.Simulation;
import ui.SimulationFrame;

public class Application {
    private Simulation simulation;
    private SimulationFrame window;

    public void init() {
        Warehouse warehouse = new Warehouse(16, 16);
        for (int col = 4; col <= 10; col++) warehouse.setCell(5, col, '#');
        for (int col = 3; col <= 8; col++) warehouse.setCell(10, col, '#');
        warehouse.addCharger(new Position(1, 14));
        warehouse.addPickup(new Position(2, 3));
        warehouse.addDropoff(new Position(13, 13));
        warehouse.addPickup(new Position(4, 12));
        warehouse.addDropoff(new Position(12, 3));

        simulation = new Simulation(warehouse);
        simulation.addRobot(new StandardRobot(new Position(1, 1)));
        simulation.addRobot(new StandardRobot(new Position(7, 5)));
        simulation.addRobot(new StandardRobot(new Position(13, 12)));
        simulation.addTask(new Task("T1", 2, 3, 13, 13));
        simulation.addTask(new Task("T2", 4, 12, 12, 3));
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            if (simulation == null) init();
            window = new SimulationFrame();
            window.onNextStep(this::advanceSimulation);
            window.render(simulation.getState());
            window.setVisible(true);
        });

    }

    private void advanceSimulation() {
        RuntimeException failure = null;
        try {
            simulation.step();
        } catch (RuntimeException exception) {
            failure = exception;
        }
        window.render(simulation.getState());
        if (failure != null) {
            String message = failure.getMessage();
            window.showError(message == null ? "Unable to advance the simulation." : message);
        }
    }
}
