package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

//PathPlanner: finds a route for a robot from one square to another.
//It uses BFS (breadth-first search), which checks squares in rings
//going outward from the start, so the first time it reaches the goal
//it's a shortest path (fewest steps).
//Robots only move up, down, left, right (no diagonals).
public class PathPlanner
{
    private final Warehouse warehouse;   // the grid we are planning on

    //the 4 directions a robot can step: up, down, left, right
    private static final int[][] DIRECTIONS = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

    //the planner needs the warehouse so it knows where the walls are
    public PathPlanner(Warehouse warehouse)
    {
        this.warehouse = warehouse;
    }

    //finds a path from start to goal.
    //the list INCLUDES start and goal, so:
    //  empty list      -> no path (blocked, or start/goal off the map)
    //  just [start]    -> already at the goal
    //  size() - 1      -> number of steps the robot has to take
    public List<Position> findPath(Position start, Position goal)
    {
        List<Position> path = new ArrayList<>();

        //can't plan from or to a square that's off the map
        if (!inBounds(start) || !inBounds(goal))
        {
            return path;   // empty = no path
        }

        //already there, nothing to do
        if (start.equals(goal))
        {
            path.add(start);
            return path;
        }

        //queue = squares we still need to look at (first in, first out)
        Queue<Position> queue = new ArrayDeque<>();

        //cameFrom remembers how we got to each square: square -> the square before it.
        //it also works as our "visited" list (if it's a key, we've been there).
        Map<Position, Position> cameFrom = new HashMap<>();

        queue.add(start);
        cameFrom.put(start, null);   // start has no square before it

        while (!queue.isEmpty())
        {
            Position current = queue.poll();   // take the next square off the front

            if (current.equals(goal))
            {
                return buildPath(cameFrom, goal);   // found it
            }

            //look at the 4 neighbors of current
            for (int[] d : DIRECTIONS)
            {
                Position next = current.offset(d[0], d[1]);

                //skip it if we can't step there or we already visited it
                if (canEnter(next, goal) && !cameFrom.containsKey(next))
                {
                    cameFrom.put(next, current);   // we reached next from current
                    queue.add(next);
                }
            }
        }

        return path;   // queue ran out and never hit the goal = unreachable, empty list
    }

    //how many steps from start to goal, or -1 if it can't get there.
    //useful for checking battery before a robot takes a task.
    public int stepsNeeded(Position start, Position goal)
    {
        List<Position> path = findPath(start, goal);
        if (path.isEmpty())
        {
            return -1;
        }
        return path.size() - 1;
    }

    //walks backwards from the goal using cameFrom until it hits start,
    //then flips the list so it goes start -> goal.
    private List<Position> buildPath(Map<Position, Position> cameFrom, Position goal)
    {
        List<Position> path = new ArrayList<>();
        Position step = goal;
        while (step != null)
        {
            path.add(step);
            step = cameFrom.get(step);   // the square before this one (null at start)
        }
        Collections.reverse(path);
        return path;
    }

    //true if the square is inside the warehouse grid
    private boolean inBounds(Position p)
    {
        char[][] grid = warehouse.getGrid();
        return p.getRow() >= 0 && p.getRow() < grid.length
            && p.getCol() >= 0 && p.getCol() < grid[0].length;
    }

    //true if a robot is allowed to step on this square.
    //open floor is always ok. pickup (P), drop-off (D), and charger (C)
    //squares are only ok if it's the square we're going to,
    //so robots don't cut through other stations on the way.
    private boolean canEnter(Position p, Position goal)
    {
        if (!inBounds(p))
        {
            return false;
        }
        if (warehouse.isWalkable(p.getRow(), p.getCol()))
        {
            return true;
        }
        char cell = warehouse.getCell(p.getRow(), p.getCol());
        boolean isStation = cell == 'P' || cell == 'D' || cell == 'C';
        return isStation && p.equals(goal);
    }
}