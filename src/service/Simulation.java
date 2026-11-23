
package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Position;
import model.Robot;
import model.SimulationState;
import model.Task;
import model.Warehouse;

public class Simulation {

    // Warehouse and simulation data
    private final Warehouse warehouse;
    private final List<Robot> robots;
    private final TaskManager taskManager;
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
        this.taskManager = new TaskManager(warehouse);
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
        taskManager.addTask(task);
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
        return taskManager.getTasks();
    }



    //================ getCurrentStep() =============
    // Description: Returns current step number.
    // Input: None.
    // Output: Current simulation step.
    //==============================================
    public int getCurrentStep() {
        return currentStep;
    }

    public SimulationState getState() {
        List<SimulationState.RobotState> robotStates = new ArrayList<>();
        for (Robot robot : robots) {
            Task currentTask = robot.getCurrentTask();
            robotStates.add(new SimulationState.RobotState(
                    robot.getId(), robot.getClass().getSimpleName(), robot.getPosition(),
                    robot.getBattery(), getRobotStatus(robot),
                    currentTask == null ? null : currentTask.getId()));
        }

        List<SimulationState.TaskState> taskStates = new ArrayList<>();
        for (Task task : getTasks()) {
            taskStates.add(new SimulationState.TaskState(
                    task.getId(), getPickupPosition(task), getDeliveryPosition(task),
                    task.getAssignedRobotId(), task.getStatus()));
        }

        return new SimulationState(currentStep, warehouse.getGrid(),
                warehouse.getPickupPoints(), warehouse.getDropoffPoints(), warehouse.getChargers(),
                robotStates, taskStates);
    }

    private SimulationState.RobotStatus getRobotStatus(Robot robot) {
        if (robot.batteryLow()) return SimulationState.RobotStatus.LOW_BATTERY;
        Task task = robot.getCurrentTask();
        if (task == null || task.getStatus() == Task.Status.COMPLETED) {
            return SimulationState.RobotStatus.IDLE;
        }
        if (task.getStatus() == Task.Status.ASSIGNED) return SimulationState.RobotStatus.ASSIGNED;
        if (task.getStatus() == Task.Status.IN_PROGRESS) return SimulationState.RobotStatus.IN_PROGRESS;
        return SimulationState.RobotStatus.WAITING;
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

        // 在移动前分配一次；本步完成任务的机器人从下一步开始接新任务。
        taskManager.assignPendingTasks(robots);

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
        if (getTasks().isEmpty()) {
            System.out.println("No tasks available.");
        }
        else {
            for (Task task : getTasks()) {
                System.out.println(task);
            }
        }

        System.out.println("=============================");
    }

}
