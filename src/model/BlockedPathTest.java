
package model;

public class BlockedPathTest {

    public static void main(String[] args) {

        // Create a 5x5 warehouse
        Warehouse warehouse = new Warehouse(5, 5);

        // Place obstacles around the pickup location
        warehouse.setCell(1, 2, '#');
        warehouse.setCell(2, 1, '#');
        warehouse.setCell(2, 3, '#');
        warehouse.setCell(3, 2, '#');

        // Pickup is surrounded by obstacles
        warehouse.addPickup(new Position(2, 2));
        warehouse.addDropoff(new Position(4, 4));

        Simulation simulation = new Simulation(warehouse);

        StandardRobot robot = new StandardRobot(new Position(0, 0));

        simulation.addRobot(robot);

        Task task = new Task("T2", 2, 2, 4, 4);
        simulation.addTask(task);

        robot.setTask(task);
        task.setAssignedRobotId(String.valueOf(robot.getId()));
        task.setStatus(Task.Status.ASSIGNED);

        // Display warehouse
        warehouse.display();

        // Try to advance the simulation
        for (int i = 0; i < 3; i++) {
            simulation.step();
        }

        simulation.displayStatus();

        // Verify that the robot did not move
        System.out.println("Robot stayed at start: " + robot.getPosition().equals(new Position(0, 0)));

        System.out.println("Task still assigned: " + (task.getStatus() == Task.Status.ASSIGNED));
    }
}
