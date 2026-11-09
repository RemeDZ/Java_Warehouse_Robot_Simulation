package model;

//StandardRobot: the normal robot, carries light items (up to 20)
//and uses 1 battery per step.
//It inherits from Robot and fills in the two abstract methods.
public class StandardRobot extends Robot
{
    public static final int MAX_LOAD = 20;   // heaviest item it can carry
    public static final int STEP_COST = 1;   // battery used per square moved

    //just passes the start square up to Robot's constructor
    public StandardRobot(Position startPosition)
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

    //prints like StandardRobot #1 at (0, 0), battery 100
    @Override
    public String toString()
    {
        return "StandardRobot #" + getId() + " at " + getPosition() + ", battery " + getBattery();
    }
}