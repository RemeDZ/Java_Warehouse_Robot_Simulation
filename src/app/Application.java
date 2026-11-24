package app;

import javax.swing.SwingUtilities;
import model.Position;
import model.SimulationState;
import model.StandardRobot;
import model.Task;
import model.Warehouse;
import service.Simulation;
import ui.SimulationFrame;

public class Application {
    private Simulation simulation;
    private SimulationFrame window;
    private int nextTaskNumber;

    public void init() {
        Warehouse warehouse = new Warehouse(16, 16);

        for (int col = 4; col <= 10; col++)
            warehouse.setCell(5, col, '#');
        for (int col = 3; col <= 8; col++)
            warehouse.setCell(10, col, '#');

        warehouse.addCharger(new Position(1, 14));
        warehouse.addPickup(new Position(2, 3));
        warehouse.addDropoff(new Position(13, 13));
        warehouse.addPickup(new Position(4, 12));
        warehouse.addDropoff(new Position(12, 3));

        Simulation initial = new Simulation(warehouse);
        initial.addRobot(new StandardRobot(1, new Position(1, 1)));
        initial.addRobot(new StandardRobot(2, new Position(7, 5)));
        initial.addRobot(new StandardRobot(3, new Position(13, 12)));
        initial.addTask(new Task("T1", 2, 3, 13, 13));
        initial.addTask(new Task("T2", 4, 12, 12, 3));
        simulation = initial;
        nextTaskNumber = 3;
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            if (simulation == null)
                init();
            window = new SimulationFrame();
            window.onNextStep(this::advanceSimulation);
            window.onAddTask(this::addTask);
            window.onReset(this::resetSimulation);
            window.render(simulation.getState());
            window.setVisible(true);
        });

    }

    private void addTask() {
        SimulationFrame.TaskInput input = null;
        while ((input = window.requestTaskInput(input)) != null) {
            try {
                SimulationState state = simulation.getState();
                validateTaskPositions(input, state);
                int number = nextTaskNumber;
                while (hasTaskId(state, "T" + number)) number++;
                simulation.addTask(new Task("T" + number, input.getPickupRow(), input.getPickupCol(),
                        input.getDeliveryRow(), input.getDeliveryCol()));
                nextTaskNumber = number + 1;
                window.render(simulation.getState());
                return;
            } catch (IllegalArgumentException exception) {
                window.showError(exception.getMessage());
            }
        }
    }

    private void validateTaskPositions(SimulationFrame.TaskInput input, SimulationState state) {
        Position pickup = new Position(input.getPickupRow(), input.getPickupCol());
        Position delivery = new Position(input.getDeliveryRow(), input.getDeliveryCol());
        if (pickup.equals(delivery)) {
            throw new IllegalArgumentException("Pickup and delivery positions must be different.");
        }
        for (SimulationState.TaskState task : state.getTasks()) {
            if (task.getStatus() == Task.Status.COMPLETED) continue;
            if (pickup.equals(task.getPickupPosition()) || pickup.equals(task.getDeliveryPosition())
                    || delivery.equals(task.getPickupPosition()) || delivery.equals(task.getDeliveryPosition())) {
                throw new IllegalArgumentException("Pickup or delivery position is already used by task "
                        + task.getId() + ".");
            }
        }
    }

    private boolean hasTaskId(SimulationState state, String id) {
        for (SimulationState.TaskState task : state.getTasks()) {
            if (id.equals(task.getId())) return true;
        }
        return false;
    }

    private void resetSimulation() {
        try {
            init();
            window.render(null);
            window.render(simulation.getState());
        } catch (RuntimeException exception) {
            window.showError(exception.getMessage());
        }
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
