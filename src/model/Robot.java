package model;


public abstract class Robot {

    private static int idCounter = 1;
    private int id;
    private Position position;
    private Task currentTask;
    private int battery;
    private final int MAX_BATTERY = 100;

    protected Robot(Position startPosition) {
        this.id = idCounter++;
        this.battery = MAX_BATTERY;
        this.position = startPosition;
    }

    // abstract methods
    public abstract boolean canCarry(int weight);
    protected abstract int getMoveCost();

    
    public void setTask(Task task) {
        this.currentTask = task;
    }

    public void completeTask() {
        currentTask = null;
    }

    public void moveTo(Position newPosition) {
        // add battery threshold check later to see if it needs to move to charger
        this.position = newPosition;
    }

    public boolean batteryLow() {
       return battery<20;
    }


    // getters
    public int getId() { return id; }
    public Position getPosition() { return position; }
    public Task getCurrentTask() { return currentTask; }
    public int getBattery() { return battery; }


}
