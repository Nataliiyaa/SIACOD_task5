package com.example.demo;

public enum CellType {
    GRASS(0, 5), ROAD(1, 1), SAND(2, 10), OBSTACLE(3, 0);

    public final int id;
    public final int cost;

    CellType(int id, int cost) {
        this.id = id;
        this.cost = cost;
    }
}
