package model;
public class Task {
    public enum Status {
        PENDING,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED
    }

    private String id;
    private int pickupX;
    private int pickupY;
    private int deliveryX;
    private int deliveryY;
    private String assignedRobotId;
    private Status status;

    public Task(String id, int pickupX, int pickupY, int deliveryX, int deliveryY) {
        this.id = id;
        this.pickupX = pickupX;
        this.pickupY = pickupY;
        this.deliveryX = deliveryX;
        this.deliveryY = deliveryY;
        this.assignedRobotId = null;
        this.status = Status.PENDING;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPickupX() {
        return pickupX;
    }

    public void setPickupX(int pickupX) {
        this.pickupX = pickupX;
    }

    public int getPickupY() {
        return pickupY;
    }

    public void setPickupY(int pickupY) {
        this.pickupY = pickupY;
    }

    public int getDeliveryX() {
        return deliveryX;
    }

    public void setDeliveryX(int deliveryX) {
        this.deliveryX = deliveryX;
    }

    public int getDeliveryY() {
        return deliveryY;
    }

    public void setDeliveryY(int deliveryY) {
        this.deliveryY = deliveryY;
    }

    public String getAssignedRobotId() {
        return assignedRobotId;
    }

    public void setAssignedRobotId(String assignedRobotId) {
        this.assignedRobotId = assignedRobotId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", pickup=(" + pickupX + ", " + pickupY + ")" +
                ", delivery=(" + deliveryX + ", " + deliveryY + ")" +
                ", assignedRobotId=" + assignedRobotId +
                ", status=" + status +
                '}';
    }
}
