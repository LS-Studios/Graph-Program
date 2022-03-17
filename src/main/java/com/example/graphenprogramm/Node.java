package com.example.graphenprogramm;

import java.util.ArrayList;

public class Node {
    public String name;
    public ArrayList<Edge> edges = new ArrayList<>();

    public Node(String name) {
        this.name = name;
    }

    public Edge addEdge(Edge edgeToAdd) {
        edges.add(edgeToAdd);
        return edgeToAdd;
    }
}
