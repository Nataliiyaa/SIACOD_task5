package com.example.demo;

import javafx.geometry.Point2D;
import java.util.*;

public class AStarPathfinder {
    public Point2D start;
    public Point2D end;
    public int[][] grid;
    private List<Point2D> path;


    public AStarPathfinder(Point2D start, Point2D end, int[][]grid) {
        this.start = start;
        this.end = end;
        this.grid = grid;
    }

    public List<Point2D> findPath() {


        if (start == null || end == null) {
            return null;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<Point2D> closedSet = new HashSet<>();
        Map<Point2D, Node> nodeMap = new HashMap<>();

        Node startNode = new Node(start, null, 0, (int) heuristic(start, end));
        openSet.add(startNode);
        nodeMap.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.point.equals(end)) {
                return reconstructPath(current);
            }

            closedSet.add(current.point);

            for (Point2D neighbor : getNeighbors(current.point, grid)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGScore = current.g + getCost(neighbor, grid);
                Node neighborNode = nodeMap.get(neighbor);

                if (neighborNode == null || tentativeGScore < neighborNode.g) {
                    if (neighborNode == null) {
                        neighborNode = new Node(neighbor, current, tentativeGScore, (int) heuristic(neighbor, end));
                        nodeMap.put(neighbor, neighborNode);
                    } else {
                        neighborNode.parent = current;
                        neighborNode.g = tentativeGScore;
                        neighborNode.f = tentativeGScore + neighborNode.h;
                    }

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    } else {
                        openSet.remove(neighborNode);
                        openSet.add(neighborNode); // Reinsert to update priority
                    }
                }
            }
        }

        return null; // Path not found
    }

    private double heuristic(Point2D p1, Point2D p2) {
        // Manhattan distance
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    private int getCost(Point2D point, int[][] grid) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        return CellType.values()[grid[y][x]].cost;
    }

    private List<Point2D> getNeighbors(Point2D point, int[][] grid) {
        List<Point2D> neighbors = new ArrayList<>();
        int x = (int) point.getX();
        int y = (int) point.getY();

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < grid.length && ny >= 0 && ny < grid.length) {
                if (grid[ny][nx] != CellType.OBSTACLE.id) {
                    neighbors.add(new Point2D(nx, ny));
                }
            }
        }
        return neighbors;
    }

    private List<Point2D> reconstructPath(Node node) {
        path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.point);
            node = node.parent;
        }
        return path;
    }

     public int calculatePathLength() {
        if (path == null || path.isEmpty()) return 0;
        int totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Point2D p = path.get(i);
            int x = (int) p.getX();
            int y = (int) p.getY();
            totalCost += CellType.values()[grid[y][x]].cost;
        }
        return totalCost;
    }
}
