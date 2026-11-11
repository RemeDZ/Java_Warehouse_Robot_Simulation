
package model;

public class SimulationTest {

    public static void main(String[] args) {

        // Create a warehouse.
        Warehouse warehouse = new Warehouse(5, 5);

        // Mark pickup and delivery locations.
        warehouse.addPickup(new Position(1, 3));
        warehouse.addDropoff(new Position(3, 3));

        // Create the simulation.
        Simulation simulation = new Simulation(warehouse);

        // Create a robot at (1, 1).
        StandardRobot robot = new StandardRobot(new Position(1, 1));

        simulation.addRobot(robot);

        // Create a delivery task.
        Task task = new Task("T1", 1, 3, 3, 3);
        simulation.addTask(task);

        // Assign the task to the robot manually.
        robot.setTask(task);
        task.setAssignedRobotId(String.valueOf(robot.getId()));
        task.setStatus(Task.Status.ASSIGNED);

        // Run five simulation steps.
        for (int i = 0; i < 5; i++) {

            simulation.step();

            System.out.println(robot);
            System.out.println(task.getStatus());
            System.out.println();
        }

        // Check final results.
        System.out.println("Final position: " + robot.getPosition());

        System.out.println("Final battery: " + robot.getBattery());

        System.out.println("Final task status: " + task.getStatus());

         // Display the complete simulation status
        simulation.displayStatus();
    }
}
