# Java Warehouse Robot Fleet Simulation

A Java simulation of a small automated warehouse. Robots move around a 16 x 16 grid, pick up items at pickup points, and deliver them to drop-off points. The simulation runs in discrete steps and shows robot positions, battery levels, and task progress in a Swing window.

## Features

- **Warehouse layout:** 16 x 16 grid with walls, pickup points, delivery points, and a charger, drawn in a window with row and column numbers.
- **Robot fleet:** 3 robots, each with an ID, a position, a battery level, and a status (Idle, Assigned, In progress, Low battery).
- **Tasks:** pickup-and-delivery tasks move through `PENDING → ASSIGNED → IN_PROGRESS → COMPLETED`. You can add new tasks while the simulation is running.
- **Task assignment:** each step, every pending task is given to the closest free robot that isn't low on battery.
- **Movement:** robots move one square per step along a shortest path (breadth-first search). They stay inside the warehouse and never walk through walls.
- **Battery:** every move uses battery (1 per step for a StandardRobot, 2 for a HeavyRobot). A robot below 20% battery stops moving.
- **Controls:** Add Task, Next Step, and Reset buttons, plus live robot and task tables.
- **Error handling:** bad input is rejected with a message instead of crashing the program (see [Edge cases](#edge-cases-handled)).

## Requirements

- Java JDK 8 or newer (we developed and tested with JDK 21)
- No external libraries

Check that Java is installed:

```
java -version
javac -version
```

## Build and run

Run these commands from the project's root folder (the folder that contains `src` and `test`).

### Windows (PowerShell)

```
javac -d out (Get-ChildItem -Recurse src,test -Filter *.java).FullName
java -cp out app.Main
```

### macOS / Linux

```
javac -d out $(find src test -name "*.java")
java -cp out app.Main
```

The compiled `.class` files go into `out/`, which is ignored by Git.

## How to use the simulation

When the window opens, the warehouse starts with 3 robots and 2 tasks.

| Button | What it does |
|---|---|
| **Next Step** | Advances the simulation one step: assigns pending tasks, then moves each busy robot one square. |
| **Add Task** | Asks for a pickup row/column and a delivery row/column, then adds a new task with an automatic ID (T3, T4, ...). |
| **Reset** | Restores the starting scenario. |

Clicking a robot or task in the side tables highlights it on the grid. The bar at the bottom counts pending, assigned, delivering, and completed tasks, and shows "All tasks completed" when everything is delivered.

### Map legend

| Symbol | Meaning |
|---|---|
| Gray cell | Wall (robots can't enter) |
| `Pn` | Pickup point for task n |
| `Dn` | Delivery point for task n |
| `C` | Charger |
| `Rn` | Robot n |

Coordinates are written as `(row, col)` and start at 0 in the top-left corner.

**Robot and task colors:** gray = pending/idle, orange = assigned, blue = delivering, green = completed, red = low battery, purple outline = selected.

## How the simulation works

Each time **Next Step** is pressed, `Simulation.step()`:

1. Asks `TaskManager` to assign every pending task to the closest free robot (by grid distance to the pickup) that isn't low on battery.
2. For every robot with a task, asks `PathPlanner` for a shortest path to its current goal (the pickup first, then the delivery point) and moves the robot one square along it.
3. When a robot reaches the pickup, the task becomes `IN_PROGRESS`. When it reaches the delivery point, the task becomes `COMPLETED` and the robot is free for a new task.

### Robot types

| Type | Max load | Battery per step |
|---|---|---|
| `StandardRobot` | 20 | 1 |
| `HeavyRobot` | 50 | 2 |

Every robot starts at 100% battery.

### Battery rule (recovery action)

When a robot's battery drops below 20%, it **stops in place and keeps its task**. It is not given new tasks, and its status shows as Low battery (red). Automatic charging is not part of the simulation. `Robot.recharge()` exists to restore a robot to 100% if charging is added later.

### Pathfinding

`PathPlanner` uses breadth-first search (BFS) on the grid. It only moves up, down, left, and right, so every step costs the same and BFS always finds a shortest route. It walks around walls, and it only steps onto a pickup, delivery, or charger square when that square is the robot's destination. If the goal is blocked in or off the map, it returns an empty path and the robot waits instead of crashing.

## Edge cases handled

| Situation | What happens |
|---|---|
| Task location outside the warehouse | Rejected: "Task location is outside the warehouse." |
| Task location on a wall | Rejected: "Task location is blocked." |
| Duplicate task or robot ID | Rejected with an error message |
| Pickup and delivery at the same square | Rejected |
| Task using a square already used by another active task | Rejected |
| Non-number typed in the Add Task box | "Enter whole numbers for all four coordinates." |
| Destination that can't be reached | Robot waits and the console prints "No available path" |
| Battery below 20% | Robot stops and is not assigned new tasks |
| No free robots | Task stays pending until a robot is available |

## Running the tests

The tests are plain Java programs in `test/service/`. Each one builds a small scenario, runs the simulation, and prints its results (`true` / `PASS` means the check passed).

Build first (see [Build and run](#build-and-run)), then:

```
java -cp out service.SimulationTest
java -cp out service.MultipleRobotsTest
java -cp out service.BlockedPathTest
java -cp out service.LowBatteryTest
```

| Test | What it checks |
|---|---|
| `SimulationTest` | One robot completes a full pickup and delivery; duplicate IDs and out-of-bounds tasks are rejected |
| `MultipleRobotsTest` | Three robots each complete their own task |
| `BlockedPathTest` | A robot whose pickup is walled in stays put instead of crashing |
| `LowBatteryTest`