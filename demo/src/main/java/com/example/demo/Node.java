package com.example.demo;

import javafx.geometry.Point2D;

public class Node {
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
