package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class HelloApplication extends Application {
    private static final int CELL_SIZE = 80;
    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;

    private int[][] grid;
    private Point2D start;
    private Point2D end;
    private List<Point2D> path;
    private Canvas canvas;
    private boolean startAndEndSelected = false;
    private Label pathLengthLabel;

    private enum CellType {
        OBSTACLE(0, 0), ROAD(1, 1), GRASS(2, 5), SAND(3, 10);

        public final int id;
        public final int cost;

        CellType(int id, int cost) {
            this.id = id;
            this.cost = cost;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        grid = new int[HEIGHT][WIDTH];
        initializeGrid();

        canvas = new Canvas(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
        canvas.setOnMousePressed(this::handleMouseClick);

        Button findPathButton = new Button("Find Path");
        findPathButton.setOnAction(e -> {
            path = aStarSearch();
            drawGrid();
        });

        pathLengthLabel = new Label("Path Length: ");
        VBox buttonBox = new VBox(findPathButton, pathLengthLabel);
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pathfinding JavaFX");
        primaryStage.show();
        drawGrid();
    }

    private void handleMouseClick(javafx.scene.input.MouseEvent e) {
        int x = (int) (e.getX() / CELL_SIZE);
        int y = (int) (e.getY() / CELL_SIZE);

        if (e.isPrimaryButtonDown()) {
            if (start == null) {
                start = new Point2D(x, y);
            } else if (end == null) {
                end = new Point2D(x, y);
                startAndEndSelected = true;
            } else if (startAndEndSelected) {
                grid[y][x] = (grid[y][x] + 1) % 4;
            }
        } else if (e.isSecondaryButtonDown()) {
            grid[y][x] = CellType.OBSTACLE.id;
        }
        drawGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < HEIGHT; i++) {
            Arrays.fill(grid[i], CellType.GRASS.id);
        }
    }

    private void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                CellType type = CellType.values()[grid[i][j]];
                gc.setFill(getColor(type));
                gc.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                gc.setStroke(Color.BLACK);
                gc.strokeRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        drawPath(gc);
        if (start != null) drawPoint(gc, start, Color.PINK);
        if (end != null) drawPoint(gc, end, Color.RED);
    }

    private Color getColor(CellType type) {
        switch (type) {
            case OBSTACLE:
                return Color.BLACK;
            case ROAD:
                return Color.GRAY;
            case GRASS:
                return Color.GREEN;
            case SAND:
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }

    private void drawPath(GraphicsContext gc) {
        if (path != null) {
            gc.setStroke(Color.MAGENTA);
            gc.setLineWidth(5);
            for (int i = 0; i < path.size() - 1; i++) {
                Point2D p1 = path.get(i);
                Point2D p2 = path.get(i + 1);
                gc.strokeLine(p1.getX() * CELL_SIZE + CELL_SIZE / 2, p1.getY() * CELL_SIZE + CELL_SIZE / 2,
                        p2.getX() * CELL_SIZE + CELL_SIZE / 2, p2.getY() * CELL_SIZE + CELL_SIZE / 2);
            }
        }
    }

    private void drawPoint(GraphicsContext gc, Point2D point, Color color) {
        gc.setFill(color);
        gc.fillOval(point.getX() * CELL_SIZE + CELL_SIZE / 4, point.getY() * CELL_SIZE + CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
    }

    private List<Point2D> aStarSearch() {
        path = aStarSearchHelper();
        if(path != null){
            updatePathLengthLabel();
        } else {
            pathLengthLabel.setText("Path Length: Not Found");
        }
        return path;
    }


    private List<Point2D> aStarSearchHelper() {
        if (start == null || end == null) return null;

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

            for (Point2D neighbor : getNeighbors(current.point)) {
                if (closedSet.contains(neighbor)) continue;

                int tentativeGScore = current.g + getCost(neighbor);
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
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return null;
    }

    private double heuristic(Point2D p1, Point2D p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    private int getCost(Point2D point) {
        return CellType.values()[grid[(int) point.getY()][(int) point.getX()]].cost;
    }

    private List<Point2D> getNeighbors(Point2D point) {
        List<Point2D> neighbors = new ArrayList<>();
        int x = (int) point.getX();
        int y = (int) point.getY();

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && grid[ny][nx] != CellType.OBSTACLE.id) {
                neighbors.add(new Point2D(nx, ny));
            }
        }
        return neighbors;
    }

    private List<Point2D> reconstructPath(Node node) {
        List<Point2D> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.point);
            node = node.parent;
        }
        return path;
    }

    private int calculatePathLength() {
        if (path == null || path.isEmpty()) return 0;
        int totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Point2D p = path.get(i);
            int x = (int) p.getX();
            int y = (int) p.getY();
            totalCost += CellType.values()[grid[y][x]].cost; // Исправлено: используем координаты для получения стоимости
        }
        return totalCost;
    }

    private void updatePathLengthLabel() {
        int pathLength = calculatePathLength();
        pathLengthLabel.setText("Path Length: " + pathLength);
    }

    private static class Node {
        Point2D point;
        Node parent;
        int g;
        int h;
        int f;

        Node(Point2D point, Node parent, int g, int h) {
            this.point = point;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return point.equals(node.point);
        }

        @Override
        public int hashCode() {
            return point.hashCode();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}