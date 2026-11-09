package model;

import java.util.Objects;

//Position: one square on the warehouse grid (row, col).
//It can't be changed after it's made (immutable),
//so to move a robot we give it a NEW Position.
//That keeps it safe to store in a HashMap/HashSet (PathPlanner does this).
public class Position
{
    private final int row;   // up/down, 0 = top row
    private final int col;   // left/right, 0 = left column

    //makes a position from a row and a column
    public Position(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

    //getters only, no setters since it can't change
    public int getRow() { return row; }
    public int getCol() { return col; }

    //gives back the square one step away.
    //up = (-1, 0), down = (1, 0), left = (0, -1), right = (0, 1)
    //it does NOT check the warehouse edges, PathPlanner does that.
    public Position offset(int dRow, int dCol)
    {
        return new Position(row + dRow, col + dCol);
    }

    //two Positions are equal if they have the same row and col,
    //even if they are different objects in memory.
    //without this, new Position(2,3).equals(new Position(2,3)) is false.
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;                   // same exact object
        if (!(o instanceof Position)) return false;   // null or not a Position
        Position other = (Position) o;
        return row == other.row && col == other.col;
    }

    //has to be overridden together with equals.
    //HashMap/HashSet use this to find the right bucket,
    //so equal Positions need the same hash code.
    @Override
    public int hashCode()
    {
        return Objects.hash(row, col);
    }

    //prints like (2, 3), useful for debugging and the console display
    @Override
    public String toString()
    {
        return "(" + row + ", " + col + ")";
    }
}