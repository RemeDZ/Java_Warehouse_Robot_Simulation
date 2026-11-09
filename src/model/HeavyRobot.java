package model;

//HeavyRobot: carries big items (up to 50)
//but uses twice the battery per step (2 instead of 1).
//It inherits from Robot directly, NOT from StandardRobot:
//the two are separate kinds of robot.
public class HeavyRobot extends Robot
{
    public static final int MAX_LOAD = 50;   // heaviest item it can carry
    public static final int STEP_COST = 2;   // battery used per square moved

    //just passes the start square up to Robot's constructor
    public HeavyRobot(Position startPosition)
    {
        super(startPosition);
    }

    //true if the item isn't too heavy for this robot
    @Override
    public boolean canCarry(int weight)
    {
        return weight > 0 && weight <= MAX_LOAD;
    }

    //how much battery one step costs for this robot
    @Override
    protected int getMoveCost()
    {
        return STEP_COST;
    }

    //prints like HeavyRobot #2 at (5, 5), battery 100
    @Override
    public String toString()
    {
        return "HeavyRobot #" + getId() + " at " + getPosition() + ", battery " + getBattery();
    }
}