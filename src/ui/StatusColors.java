package ui;

import java.awt.Color;
import model.SimulationState;
import model.Task;

final class StatusColors {
    static final Color PENDING = new Color(110, 110, 110);
    static final Color ASSIGNED = new Color(180, 100, 0);
    static final Color IN_PROGRESS = new Color(35, 105, 200);
    static final Color COMPLETED = new Color(40, 125, 60);
    static final Color LOW_BATTERY = new Color(185, 35, 35);
    static final Color SELECTED = new Color(125, 40, 150);

    private StatusColors() { }

    static Color task(Task.Status status) {
        if (status == Task.Status.ASSIGNED) return ASSIGNED;
        if (status == Task.Status.IN_PROGRESS) return IN_PROGRESS;
        if (status == Task.Status.COMPLETED) return COMPLETED;
        return PENDING;
    }

    static Color robot(SimulationState.RobotStatus status) {
        if (status == SimulationState.RobotStatus.LOW_BATTERY) return LOW_BATTERY;
        if (status == SimulationState.RobotStatus.ASSIGNED) return ASSIGNED;
        if (status == SimulationState.RobotStatus.IN_PROGRESS) return IN_PROGRESS;
        return PENDING;
    }

    static Color background(Color color) {
        return new Color((color.getRed() + 255 * 4) / 5,
                (color.getGreen() + 255 * 4) / 5, (color.getBlue() + 255 * 4) / 5);
    }
}
