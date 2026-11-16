
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simulation {

    // Warehouse and simulation data
    private final Warehouse warehouse;
    private final List<Robot> robots;
    private final List<Task> tasks;
    private final PathPlanner planner;

    private int currentStep;

    //================ Simulation() ================
    // Description: Initializes the simulation.
    // Input: A warehouse object.
    // Output: Creates an empty robot and task list.
    //==============================================
    public Simulation(Warehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException(
                "Warehouse cannot be null."
            );
        }

        this.warehouse = warehouse;
        this.robots = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.planner = new PathPlanner(warehouse);
        this.currentStep = 0;
    }


    
    //================ addRobot() =====================
    // Description: Adds a robot to the simulation.
    // Input: A Robot object.
    // Output: Adds the robot if it is valid and unique.
    //================================================
    public void addRobot(Robot robot) {

        // Check for null robot.
        if (robot == null) {
            throw new IllegalArgumentException("Robot cannot be null.");
        }

        // Check for duplicate robot IDs.
        for (Robot existingRobot : robots) {
            if (existingRobot.getId() == robot.getId()) {
                throw new IllegalArgumentException("Robot ID " + robot.getId() + " already exists.");
            }
        }

        // Add the robot.
        robots.add(robot);
    }



    //================ addTask() ======================
    // Description: Adds a valid task to the simulation.
    // Input: A Task object.
    // Output: Adds the task if valid and unique.
    //================================================
    public void addTask(Task task) {

        // Check for null task.
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        // Check for missing task ID.
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be empty.");
        }

        // Check for duplicate task IDs.
        for (Task existingTask : tasks) {
            if (existingTask.getId().equals(task.getId())) {
                throw new IllegalArgumentException( "Task ID " + task.getId() + " already exists.");
            }
        }

        // Create pickup and delivery positions.
        Position pickup = getPickupPosition(task);
        Position delivery = getDeliveryPosition(task);

        char[][] grid = warehouse.getGrid();

        // Check warehouse boundaries.
        if (!isWithinBounds(pickup, grid) || !isWithinBounds(delivery, grid)) {
            throw new IllegalArgumentException("Task location is outside the warehouse.");
        }

        // Check whether locations are usable.
        if (!isValidTaskLocation(pickup) ||
            !isValidTaskLocation(delivery)) {
            throw new IllegalArgumentException("Task location is blocked.");
        }

        tasks.add(task);
    }

    //================ isWithinBounds() ================
    // Description: Checks whether a position is on the map.
    // Input: Position and warehouse grid.
    // Output: True if position is inside boundaries.
    //==================================================
    private boolean isWithinBounds(Position pos, char[][] grid) {
        return pos.getRow() >= 0 &&
               pos.getRow() < grid.length &&
               pos.getCol() >= 0 &&
               pos.getCol() < grid[0].length;
    }

    //================ isValidTaskLocation() ============
    // Description: Checks task pickup/delivery locations.
    // Input: A Position object.
    // Output: True if the location is usable.
    //==================================================
    private boolean isValidTaskLocation(Position pos) {

        char cell = warehouse.getCell(
            pos.getRow(), pos.getCol()
        );

        return (cell == '_' || cell == 'P' || cell == 'D');
    }



    //================ getWarehouse() ==============
    // Description: Returns the warehouse.
    // Input: None.
    // Output: Warehouse object.
    //==============================================
    public Warehouse getWarehouse() {
        return warehouse;
    }



    //================ getRobots() ==================
    // Description: Returns the robot list.
    // Input: None.
    // Output: Read-only list of robots.
    //==============================================
    public List<Robot> getRobots() {
        return Collections.unmodifiableList(robots);
    }



    //================ getTasks() ===================
    // Description: Returns the task list.
    // Input: None.
    // Output: Read-only list of tasks.
    //==============================================
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }



    //================ getCurrentStep() =============
    // Description: Returns current step number.
    // Input: None.
    // Output: Current simulation step.
    //==============================================
    public int getCurrentStep() {
        return currentStep;
    }



    //================ moveRobotOneStep() ================
    // Description: Moves a robot one step toward a goal.
    // Input: A robot and its destination.
    // Output: True if the robot moves successfully.
    //====================================================
    private boolean moveRobotOneStep(Robot robot, Position goal) {

        // Find a path from the current position to the goal.
        List<Position> path =
                planner.findPath(robot.getPosition(), goal);

        // No movement if the destination is unreachable
        // or the robot is already at the destination.
        if (path.isEmpty()) {
                System.out.println("Robot " + robot.getId() + ": No available path to " + goal);
                return false;
        }

        if (path.size() == 1) {
                return false;
        }

        // Do not move if the battery is too low.
        if (robot.batteryLow()) {
                System.out.println("Robot " + robot.getId() + ": Low battery. Movement stopped.");
                return false;
        }

        // Move to the next position in the path.
        try {
            robot.moveTo(path.get(1));
            return true;
        }
        catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }



    //================ getPickupPosition() ================
    // Description: Gets the pickup location of a task.
    // Input: A Task object.
    // Output: Pickup position.
    //=====================================================
    private Position getPickupPosition(Task task) {
        return new Position(
            task.getPickupX(),
            task.getPickupY()
        );
    }

    //================ getDeliveryPosition() ==============
    // Description: Gets the delivery location of a task.
    // Input: A Task object.
    // Output: Delivery position.
    //=====================================================
    private Position getDeliveryPosition(Task task) {
        return new Position(
            task.getDeliveryX(),
            task.getDeliveryY()
        );
    }

    //================ processRobot() =====================
    // Description: Processes one robot's assigned task.
    // Input: A Robot object.
    // Output: Updates robot position and task status.
    //=====================================================
    private void processRobot(Robot robot) {

        Task task = robot.getCurrentTask();

        // Skip robots without a task.
        if (task == null) {
            return;
        }

        // Skip completed or unassigned tasks.
        if (task.getStatus() == Task.Status.COMPLETED ||
            task.getStatus() == Task.Status.PENDING) {
            return;
        }

        Position goal;

        // Determine the next destination.
        if (task.getStatus() == Task.Status.ASSIGNED) {
            goal = getPickupPosition(task);
        }
        else {
            goal = getDeliveryPosition(task);
        }

        // Move one square if not already at the goal.
        if (!robot.getPosition().equals(goal)) {
            moveRobotOneStep(robot, goal);
        }

        // Check whether the robot reached the goal.
        if (robot.getPosition().equals(goal)) {

            if (task.getStatus() == Task.Status.ASSIGNED) {
                task.setStatus(Task.Status.IN_PROGRESS);

                System.out.println("Robot " + robot.getId() + " picked up task " + task.getId());
            }
            else if (task.getStatus() == Task.Status.IN_PROGRESS) {
                task.setStatus(Task.Status.COMPLETED);
                robot.completeTask();

                System.out.println("Task " + task.getId() + " completed.");
            }
        }
    }



    //================ step() =============================
    // Description: Advances the simulation by one step.
    // Input: None.
    // Output: Updates robots and simulation step count.
    //=====================================================
    public void step() {

        currentStep++;

        System.out.println(
            "Simulation Step: " + currentStep
        );

        // Process each robot once per step.
        for (Robot robot : robots) {
            processRobot(robot);
        }
    }



    //================ displayStatus() =================
    // Description: Displays the current simulation
    //              status, robots, and tasks.
    // Input: None.
    // Output: Prints simulation information.
    //=================================================
    public void displayStatus() {

        System.out.println("\n===== SIMULATION STATUS =====");

        System.out.println("Current Step: " + currentStep);

        System.out.println("\nRobots:");
        if (robots.isEmpty()) {
            System.out.println("No robots available.");
        }
        else {
            for (Robot robot : robots) {
                System.out.println(robot);
            }
        }

        System.out.println("\nTasks:");
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
        }
        else {
            for (Task task : tasks) {
                System.out.println(task);
            }
        }

        System.out.println("=============================");
    }

}
