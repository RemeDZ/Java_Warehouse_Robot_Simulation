package model;

import java.util.List;

import javax.swing.text.Position;

import java.util.ArrayList;

public class Warehouse {
    
    private char[][] grid;
    private int rows = 16;
    private int cols = 16;

    // DEPENDS ON HOW POSITION IS IMPLEMENTED
    private final List<Position> dropoffPoints = new ArrayList<>();
    private final List<Position> pickupPoints = new ArrayList<>();
    private final List<Position> chargers = new ArrayList<>();

    public Warehouse() {

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                grid[x][y] = '_';
            }
        }

    }

    public Warehouse(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                grid[x][y] = '_';
            }
        }
    }


    public char[][] getGrid() { return grid; }

    public boolean isWalkable(int row, int col) { return grid[row][col] == '_'; }

    public void display() {
        for (char[] row : grid) {
            for (char cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }

    public char getCell(int row, int col) { return grid[row][col]; }

    public void setCell(int row, int col, char value) { 
        grid[row][col] = value;
    }

    // DEPENDS ON HOW POSITION IS IMPLEMENTED
    public List<Position> getDropoffPoints() { return dropoffPoints; }
    public List<Position> getPickupPoints() { return pickupPoints; }
    public List<Position> getChargers() { return chargers; }

    // DEPENDS ON HOW POSITION IS IMPLEMENTED
    public void addDropoff(Position pos) { setCell(pos, 'D'); }
    public void addPickup(Position pos) { setCell(pos, 'P'); }
    public void addCharger(Position pos) { setCell(pos, 'C'); }

}