package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Position;
import model.Robot;
import model.Task;
import model.Warehouse;

public class TaskManager {
    private final Warehouse warehouse;
    private final List<Task> tasks = new ArrayList<>();

    public TaskManager(Warehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("Warehouse cannot be null.");
        }
        this.warehouse = warehouse;
    }

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be empty.");
        }
        for (Task existingTask : tasks) {
            if (existingTask.getId().equals(task.getId())) {
                throw new IllegalArgumentException("Task ID " + task.getId() + " already exists.");
            }
        }

        Position pickup = new Position(task.getPickupX(), task.getPickupY());
        Position delivery = new Position(task.getDeliveryX(), task.getDeliveryY());
        if (!isWithinBounds(pickup) || !isWithinBounds(delivery)) {
            throw new IllegalArgumentException("Task location is outside the warehouse.");
        }
        if (!isValidTaskLocation(pickup) || !isValidTaskLocation(delivery)) {
            throw new IllegalArgumentException("Task location is blocked.");
        }
        tasks.add(task);
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public int assignPendingTasks(List<Robot> robots) {
        if (robots == null) {
            throw new IllegalArgumentException("Robot list and its entries cannot be null.");
        }
        for (Robot robot : robots) {
            if (robot == null) {
                throw new IllegalArgumentException("Robot list and its entries cannot be null.");
            }
        }

        int assignedCount = 0;
        for (Task task : tasks) {
            if (task.getStatus() != Task.Status.PENDING || task.getAssignedRobotId() != null) {
                continue;
            }
            for (Robot robot : robots) {
                if (robot.getCurrentTask() == null && !robot.batteryLow()) {
                    robot.setTask(task);
                    task.setAssignedRobotId(String.valueOf(robot.getId()));
                    task.setStatus(Task.Status.ASSIGNED);
                    assignedCount++;
                    break;
                }
            }
        }
        return assignedCount;
    }

    private boolean isWithinBounds(Position position) {
        char[][] grid = warehouse.getGrid();
        return position.getRow() >= 0 && position.getRow() < grid.length
                && position.getCol() >= 0 && position.getCol() < grid[0].length;
    }

    private boolean isValidTaskLocation(Position position) {
        char cell = warehouse.getCell(position.getRow(), position.getCol());
        return cell == '_' || cell == 'P' || cell == 'D';
    }
}
