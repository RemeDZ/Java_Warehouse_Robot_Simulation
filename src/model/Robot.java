package model;


public abstract class Robot {

    private static int idCounter = 1;
    private int id;
    private Position position;
    private Task currentTask;
    private int battery;
    private final int MAX_BATTERY = 100;

    protected Robot(Position startPosition) {
        this(idCounter, startPosition);
    }

    // 场景重建可指定稳定编号，同时保留其他调用方的自动编号方式。
    protected Robot(int id, Position startPosition) {
        if (id <= 0 || id == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Robot ID must be positive and below Integer.MAX_VALUE.");
        }
        this.id = id;
        this.battery = MAX_BATTERY;
        this.position = startPosition;
        idCounter = Math.max(idCounter, id + 1);
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

        if (battery < getMoveCost()) {
            throw new IllegalStateException("Robot " + id + " has no battery left to move.");
        }

        // add battery threshold check later to see if it needs to move to charger
        this.position = newPosition;
        this.battery = Math.max(0, this.battery - getMoveCost());
    }

    public boolean batteryLow() {
       return battery<20;
    }

    public void recharge() {
        this.battery = MAX_BATTERY;
    }


    // getters
    public int getId() { return id; }
    public Position getPosition() { return position; }
    public Task getCurrentTask() { return currentTask; }
    public int getBattery() { return battery; }


}
