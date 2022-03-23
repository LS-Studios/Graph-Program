package com.example.graphenprogramm.graphLogic;

import com.example.graphenprogramm.graphLogic.Algorithm.Way;
import com.example.graphenprogramm.graphUI.Position;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    private String name;
    private Position position = new Position(0, 0);
    private ArrayList<Edge> edges = new ArrayList<>();
    private int ID;

    private static int count;

    private Node shortestNode;
    private double distance = 0;

    public Node(String name) {
        this.name = name;
        ID = count++;
    }

    /**
     * Get all child nodes who are connected to this node
     */
    public List<Way> getChildNodes() {
        List<Way> childNodes = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.getNode1().equals(this)) {
                if (edge.isPointToNode2())
                    childNodes.add(new Way(edge.getNode2(), edge));
            }
            else if (edge.getNode2().equals(this)) {
                if (edge.isPointToNode1())
                    childNodes.add(new Way(edge.getNode1(), edge));
            }
        }

        return childNodes;
    }

    /**
     * Do remove an edge from the node
     */
    public void removeEdge(Edge edgeToRemove) {
        edges.remove(edgeToRemove);
    }

    /**
     * Checks if there is a connection to the given node
     */
    public boolean isConnectedTo(Node node) {
        boolean isConnected = false;

        for (Edge edge : edges) {
            if (edge.getNode1().equals(node) || edge.getNode2().equals(node)) {
                isConnected = true;
                break;
            }
        }

        return isConnected;
    };

    //region Getter and setter

    public static int getCount() {
        return count;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public void addEdge(Edge... edgesToAdd) {
        for (Edge edge : edgesToAdd) {
            this.edges.add(edge);
        }
    }

    public Node getShortestNode() {
        return shortestNode;
    }

    public void setShortestNode(Node shortestNode) {
        this.shortestNode = shortestNode;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    //endregion
}