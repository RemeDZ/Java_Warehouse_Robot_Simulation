package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SimulationState {
    public enum RobotStatus {
        IDLE,
        WAITING,
        ASSIGNED,
        IN_PROGRESS,
        LOW_BATTERY
    }

    private final int currentStep;
    private final char[][] grid;
    private final List<Position> pickupPoints;
    private final List<Position> dropoffPoints;
    private final List<Position> chargers;
    private final List<RobotState> robots;
    private final List<TaskState> tasks;

    public SimulationState(int currentStep, char[][] grid, List<Position> pickupPoints,
            List<Position> dropoffPoints, List<Position> chargers,
            List<RobotState> robots, List<TaskState> tasks) {
        this.currentStep = currentStep;
        this.grid = copyGrid(grid);
        this.pickupPoints = copyPositions(pickupPoints);
        this.dropoffPoints = copyPositions(dropoffPoints);
        this.chargers = copyPositions(chargers);
        this.robots = Collections.unmodifiableList(new ArrayList<>(robots));
        this.tasks = Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    public int getCurrentStep() { return currentStep; }
    public int getRows() { return grid.length; }
    public int getCols() { return grid.length == 0 ? 0 : grid[0].length; }
    public char getCell(int row, int col) { return grid[row][col]; }

    public char[][] getGrid() { return copyGrid(grid); }
    public List<Position> getPickupPoints() { return pickupPoints; }
    public List<Position> getDropoffPoints() { return dropoffPoints; }
    public List<Position> getChargers() { return chargers; }
    public List<RobotState> getRobots() { return robots; }
    public List<TaskState> getTasks() { return tasks; }

    private static char[][] copyGrid(char[][] source) {
        char[][] copy = new char[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    private static List<Position> copyPositions(List<Position> positions) {
        List<Position> copy = new ArrayList<>();
        for (Position position : positions) copy.add(copyPosition(position));
        return Collections.unmodifiableList(copy);
    }

    private static Position copyPosition(Position position) {
        return position == null ? null : new Position(position.getRow(), position.getCol());
    }

    public static final class RobotState {
        private final int id;
        private final String type;
        private final Position position;
        private final int battery;
        private final RobotStatus status;
        private final String currentTaskId;

        public RobotState(int id, String type, Position position, int battery,
                RobotStatus status, String currentTaskId) {
            this.id = id;
            this.type = type;
            this.position = copyPosition(position);
            this.battery = battery;
            this.status = status;
            this.currentTaskId = currentTaskId;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public Position getPosition() { return position; }
        public int getBattery() { return battery; }
        public RobotStatus getStatus() { return status; }
        public String getCurrentTaskId() { return currentTaskId; }
    }

    public static final class TaskState {
        private final String id;
        private final Position pickupPosition;
        private final Position deliveryPosition;
        private final String assignedRobotId;
        private final Task.Status status;

        public TaskState(String id, Position pickupPosition, Position deliveryPosition,
                String assignedRobotId, Task.Status status) {
            this.id = id;
            this.pickupPosition = copyPosition(pickupPosition);
            this.deliveryPosition = copyPosition(deliveryPosition);
            this.assignedRobotId = assignedRobotId;
            this.status = status;
        }

        public String getId() { return id; }
        public Position getPickupPosition() { return pickupPosition; }
        public Position getDeliveryPosition() { return deliveryPosition; }
        public String getAssignedRobotId() { return assignedRobotId; }
        public Task.Status getStatus() { return status; }
    }
}
