package com.example.demo;

import javafx.application.Application;
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
import java.util.*;

public class PathfindingApplication extends Application {
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
    AStarPathfinder aStarPathfinder;

    @Override
    public void start(Stage primaryStage) {
        grid = new int[HEIGHT][WIDTH];
        initializeGrid();

        canvas = new Canvas(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
        canvas.setOnMousePressed(this::handleMouseClick);

        Button findPathButton = new Button("Найти путь");
        findPathButton.setOnAction(e -> {
            path = aStarSearch();
            drawGrid();
        });

        pathLengthLabel = new Label("Длина пути: ");
        VBox buttonBox = new VBox(findPathButton, pathLengthLabel);
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Поиск кратчайшего пути А*");
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
        }
        aStarPathfinder = new AStarPathfinder(start, end, grid);
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
        if (start != null) drawPoint(gc, start, Color.ORANGE);
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
            gc.setLineWidth(10);
            for (int i = 0; i < path.size() - 1; i++) {
                Point2D p1 = path.get(i);
                Point2D p2 = path.get(i + 1);
                gc.strokeLine(
                        p1.getX() * CELL_SIZE + CELL_SIZE / 2,
                        p1.getY() * CELL_SIZE + CELL_SIZE / 2,
                        p2.getX() * CELL_SIZE + CELL_SIZE / 2,
                        p2.getY() * CELL_SIZE + CELL_SIZE / 2);
            }
        }
    }

    private void drawPoint(GraphicsContext gc, Point2D point, Color color) {
        gc.setFill(color);
        gc.fillOval(
                point.getX() * CELL_SIZE + CELL_SIZE / 4,
                point.getY() * CELL_SIZE + CELL_SIZE / 4,
                CELL_SIZE / 2,
                CELL_SIZE / 2);
    }

    private List<Point2D> aStarSearch() {
        path = aStarPathfinder.findPath();
        if(path != null){
            updatePathLengthLabel();
        } else {
            pathLengthLabel.setText("Path Length: Not Found");
        }
        return path;
    }


    private void updatePathLengthLabel() {
        int pathLength = aStarPathfinder.calculatePathLength();
        pathLengthLabel.setText("Path Length: " + pathLength);
    }

    public static void main(String[] args) {
        launch(args);
    }
}