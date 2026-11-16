
package model;

public class MultipleRobotsTest {

    public static void main(String[] args) {

        // Create the warehouse and simulation.
        Warehouse warehouse = new Warehouse(8, 8);
        Simulation simulation = new Simulation(warehouse);

        // Create three robots.
        StandardRobot robot1 = new StandardRobot(new Position(0, 0));

        StandardRobot robot2 = new StandardRobot(new Position(3, 0));

        StandardRobot robot3 = new StandardRobot(new Position(6, 0));

        // Add robots to the simulation.
        simulation.addRobot(robot1);
        simulation.addRobot(robot2);
        simulation.addRobot(robot3);

        // Create three pickup and delivery tasks.
        Task task1 = new Task("T1", 0, 2, 0, 4);
        Task task2 = new Task("T2", 3, 2, 3, 4);
        Task task3 = new Task("T3", 6, 2, 6, 4);

        simulation.addTask(task1);
        simulation.addTask(task2);
        simulation.addTask(task3);

        // Set pickup locations.
        warehouse.addPickup(new Position(0, 2));
        warehouse.addPickup(new Position(3, 2));
        warehouse.addPickup(new Position(6, 2));

        // Set delivery locations.
        warehouse.addDropoff(new Position(0, 4));
        warehouse.addDropoff(new Position(3, 4));
        warehouse.addDropoff(new Position(6, 4));

        // Manually assign one task to each robot.
        Robot[] robots = {robot1, robot2, robot3};
        Task[] tasks = {task1, task2, task3};

        for (int i = 0; i < robots.length; i++) {
            robots[i].setTask(tasks[i]);

            tasks[i].setAssignedRobotId(String.valueOf(robots[i].getId()));

            tasks[i].setStatus(Task.Status.ASSIGNED);
        }

        // Run five simulation steps.
        for (int i = 0; i < 5; i++) {
            simulation.step();
        }

        // Display final simulation status.
        simulation.displayStatus();

        // Check results.
        System.out.println("\n===== TEST RESULTS =====");

        for (int i = 0; i < robots.length; i++) {

            Position delivery = new Position(tasks[i].getDeliveryX(),tasks[i].getDeliveryY());

            boolean reachedDestination = robots[i].getPosition().equals(delivery);

            boolean taskCompleted = tasks[i].getStatus() == Task.Status.COMPLETED;

            boolean robotAvailable = robots[i].getCurrentTask() == null;

            System.out.println("Robot " + robots[i].getId() + " reached delivery: " + reachedDestination);

            System.out.println("Task " + tasks[i].getId() + " completed: " + taskCompleted);

            System.out.println("Robot available: " + robotAvailable);
        }
    }
}
