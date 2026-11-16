
package model;

public class LowBatteryTest {

    public static void main(String[] args) {

        // Create a warehouse.
        Warehouse warehouse = new Warehouse(5, 5);

        // Set pickup and delivery locations.
        warehouse.addPickup(new Position(1, 3));
        warehouse.addDropoff(new Position(3, 3));

        // Create the simulation and robot.
        Simulation simulation = new Simulation(warehouse);

        StandardRobot robot = new StandardRobot(new Position(1, 1));

        simulation.addRobot(robot);

        // Reduce battery to 19 using the robot's
        // existing movement method.
        for (int i = 0; i < 81; i++) {
            robot.moveTo(new Position(1, 1));
        }

        System.out.println("Initial battery: " + robot.getBattery());

        // Create and manually assign a task.
        Task task = new Task("T3", 1, 3, 3, 3);

        simulation.addTask(task);

        robot.setTask(task);

        task.setAssignedRobotId(String.valueOf(robot.getId()));

        task.setStatus(Task.Status.ASSIGNED);

        // Try moving with low battery.
        simulation.step();

        // Display final results.
        simulation.displayStatus();

        System.out.println("Robot stayed at start: " + robot.getPosition().equals(new Position(1, 1)));

        System.out.println("Battery unchanged: " + (robot.getBattery() == 19));

        System.out.println("Task still assigned: " + (task.getStatus() == Task.Status.ASSIGNED));
    }
}
