package com.example.graphenprogramm.graphUI;

import com.example.graphenprogramm.Controller;
import com.example.graphenprogramm.Main;
import com.example.graphenprogramm.graphLogic.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;

import static com.example.graphenprogramm.Controller.*;

import java.util.ArrayList;
import java.util.Arrays;

public class NodeUI extends Button {
    public Node NODE;

    public static int count = 0;
    private int ID;

    public ObservableList<EdgeUI> edges = FXCollections.observableArrayList();

    private boolean removeText = true;

    public static ArrayList<NodeUI> selectedNodes = new ArrayList<>();
    public double startDragX;
    public double startDragY;

    public static NodeUI startNode, endNode, dragedNode;

    public NodeUI(double x, double y) {
        //Place node
        setLayoutX(x);
        setLayoutY(y);

        //Set translation to the middle of node
        translateXProperty().bind(widthProperty().divide(-2));
        translateYProperty().bind(heightProperty().divide(-2));

        //region Set ID

        //Raise id
        ID = count++;

        boolean needNewID = false;

        //Check if the current id is already gives away or need to be smaller
        for (NodeUI node : Controller.nodes) {
            if (!isNumeric(node.getText()))
                needNewID = true;
            else if (ID == Integer.parseInt(node.getText())) {
                needNewID = true;
            }
        }

        //Set the new id
        if (needNewID) {
            int id = 0;

            //Find the right id
            while (true) {
                boolean isContained = true;

                //Check if the current checked id is already existing
                for (NodeUI node : Controller.nodes) {
                    if (isNumeric(node.getText()) && id == Integer.parseInt(node.getText())) {
                        isContained = true;
                        break;
                    } else
                        isContained = false;
                }

                //If id already exist increase it or break
                if (isContained)
                    id++;
                else
                    break;
            }

            //Set the id to the found one
            ID = id;
        }

        //Set the text to the id and add the node style
        setText("" + ID);
        getStyleClass().add("nodeStyle");
        //endregion

        //Set node events
        setOnMousePressed(mouseEvent -> onNodePressed(mouseEvent));
        setOnDragDetected(mouseEvent -> onNodeDragDetected(mouseEvent));
        setOnMouseDragged(mouseEvent -> onNodeDragged(mouseEvent));
        setOnMouseReleased(mouseEvent -> onNodeReleased(mouseEvent));
        setOnMouseDragEntered(mouseDragEvent -> onNodeDragEntered(mouseDragEvent));
        setOnMouseDragExited(mouseDragEvent -> onNodeDragExited(mouseDragEvent));
        setOnMouseMoved(mouseEvent -> { if (contextMenu != null) contextMenu.hide();});

        //Set position variables
        startDragX = getLayoutX();
        startDragY = getLayoutY();
    }

    /**
     * Checks if the given String is a number
     */
    private boolean isNumeric(String text) {
        try {
            double d = Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    //region Node events

    private void onNodePressed(MouseEvent mouseEvent) {
        //Select node if left mouse button was pressed
        if (mouseEvent.isPrimaryButtonDown())
            selectNode(this);

        //Set drag positions
        startDragX = getLayoutX();
        startDragY = getLayoutY();

        //Set drag position of all selected nodes if the pressed one is selected
        if (selectedNodes.contains(this)) {
            selectedNodes.forEach(node -> {
                if (node != this) {
                    node.startDragX = node.getLayoutX();
                    node.startDragY = node.getLayoutY();
                }
            });
        }
    }

    private void onNodeReleased(MouseEvent mouseEvent) {
        if (node2 != null) {
            node2.toFront();
            node2.setVisible(true);
        }

        if (dragedNode != null) {
            dragedNode.toFront();
            dragedNode.setVisible(true);
            dragedNode.edges.forEach(edge -> {
                edge.toFront();
            });
        }

        //Add edge between dragged from and dragged over node
        if (node2 != null && mouseEvent.getButton() == MouseButton.PRIMARY && dragOverNode != null) {
            removeNode(node2);
            dragOverNode.getStyleClass().remove("draggedOver");
            edgeUI.removeEdge();
            createEdge(dragOverNode, this);
            dragOverNode = null;
        }

        if (dragedNode != null) {
            System.out.println();
            dragedNode.edges.forEach(edge -> {
                System.out.println(edge.getNode1() + " " + edge.getNode2());
            });
        }

        //Join dragged node worth drag over node and move the edges
        if (dragedNode != null && mouseEvent.getButton() == MouseButton.SECONDARY && dragOverNode != null) {
            dragOverNode.getStyleClass().remove("draggedOver");

            dragedNode.edges.forEach(edge -> {
                if (!edge.getNode1().equals(dragedNode)) {
                    if (!edge.getNode1().NODE.isConnectedTo(dragOverNode.NODE)) {
                        createEdge(edge.getNode1(), dragOverNode);
                    }
                } else if (!edge.getNode2().equals(dragedNode)) {
                    if (!edge.getNode2().NODE.isConnectedTo(dragOverNode.NODE)) {
                        createEdge(edge.getNode2(), dragOverNode);
                    }
                }
            });
            removeNode(dragedNode);
            dragedNode = null;
        }

        //Remove all states like rename and select on right mouse click
        if (mouseEvent.getButton() == MouseButton.SECONDARY && mouseEvent.isStillSincePress()) {
            nodes.forEach(node -> {
                node.setOnKeyPressed(null);
                node.getStyleClass().remove("rename");

                //Do it for all edges as well
                node.edges.forEach(edge -> {
                    edge.contentBtn.setOnKeyPressed(null);
                    edge.contentBtn.getStyleClass().remove("rename");
                });
            });

            //Create context menu

            //region Rename menu

            Menu renameMenu = new Menu("Rename");
            MenuItem subRename1 = new MenuItem("Rename current");
            MenuItem subRename2 = new MenuItem("Rename all selected nodes");

            //Rename current node
            subRename1.setOnAction(actionEvent -> {
                getStyleClass().add("rename");
                removeText = true;
                setOnKeyPressed(keyEvent -> setNodeText(keyEvent, false));
            });

            //Rename all selected nodes
            subRename2.setOnAction(actionEvent -> {
                selectedNodes.forEach(node -> {
                    node.getStyleClass().add("rename");
                    removeText = true;
                    node.setOnKeyPressed(keyEvent -> setNodeText(keyEvent, true));
                });
            });

            renameMenu.getItems().addAll(subRename1, subRename2);

            //endregion

            //region Delete menu
            Menu deleteMenu = new Menu("Delete");
            MenuItem subDelete1 = new MenuItem("Delete current");
            MenuItem subDelete2 = new MenuItem("Delete all selected");

            //Delete current node
            subDelete1.setOnAction(actionEvent -> removeNode());

            //Delete all selected nodes and edges
            subDelete2.setOnAction(actionEvent -> {
                while (selectedNodes.size() > 0) {
                    selectedNodes.get(selectedNodes.size()-1).removeNode();
                }
                while (EdgeUI.selectedEdges.size() > 0) {
                    EdgeUI.selectedEdges.get(EdgeUI.selectedEdges.size()-1).removeEdge();
                }
            });

            deleteMenu.getItems().addAll(subDelete1, subDelete2);
            //endregion

            MenuItem duplicateItem = new MenuItem("Duplicate selected nodes");

            //region Algorithm menu
            Menu algorithmMenu = new Menu("Algorithm");
            MenuItem subAlgorithm1 = new MenuItem("Set as start node");
            MenuItem subAlgorithm2 = new MenuItem("Set as end node");

            //Set start node
            subAlgorithm1.setOnAction(actionEvent -> {
                if (startNode != null) {
                    startNode.getStyleClass().remove("startNode");

                    if (getStyleClass().contains("endNode"))
                        startNode.getStyleClass().remove("endNode");
                }

                if (!getStyleClass().contains("startNode"))
                    getStyleClass().add("startNode");
                startNode = this;
            });

            //Set end node
            subAlgorithm2.setOnAction(actionEvent -> {
                if (endNode != null) {
                    endNode.getStyleClass().remove("endNode");

                    if (getStyleClass().contains("startNode"))
                        startNode.getStyleClass().remove("startNode");
                }

                if (!getStyleClass().contains("endNode"))
                    getStyleClass().add("endNode");
                endNode = this;
            });

            algorithmMenu.getItems().addAll(subAlgorithm1, subAlgorithm2);
            //endregion

            //Multiple items are selected
            if (selectedNodes.size() > 1 || EdgeUI.selectedEdges.size() > 0 && selectedNodes.contains(this)) {
                //Create menu for multiple items to rename and delete
                if (selectedNodes.size() > 1)
                    createContextMenu(Arrays.asList(renameMenu, deleteMenu, algorithmMenu), mouseEvent.getScreenX() - 10, mouseEvent.getScreenY() - 10);

                //Create menu for multiple items to delete but just rename the current one
                else
                    createContextMenu(Arrays.asList(subRename1, deleteMenu, algorithmMenu), mouseEvent.getScreenX() - 10, mouseEvent.getScreenY() - 10);
            }

            //Just one or not enough items are selected
            else {
                //Create item to rename and delete just the current item
                createContextMenu(Arrays.asList(subRename1, subDelete1, algorithmMenu), mouseEvent.getScreenX() - 10, mouseEvent.getScreenY() - 10);
            }
        }

        node2 = null;
        dragedNode = null;
    }

    private void onNodeDragDetected(MouseEvent e) {
        //Create edge and drag it if drag on node is happening
        if (e.isPrimaryButtonDown()) {
            node1 = this;
            node2 = createNode(getLayoutX() + e.getX() + getTranslateX(),
                               getLayoutY() + e.getY() + getTranslateY());
            node2.toBack();
            edgeUI = createEdge(this, node2);
        }
        //Prepare for joining
        else if (e.isSecondaryButtonDown()) {
            dragedNode = this;
            dragedNode.toBack();
            dragedNode.edges.forEach(edge -> {
                edge.toBack();
            });
        }

        startFullDrag();
    }

    private void onNodeDragEntered(MouseDragEvent dragEvent) {
        //Connect nodes
        if (node2 != null && node2 != this) {
            node2.setVisible(false);

            //Check if node that is tried to connect is already connected to the other node
            boolean canConnect = true;
            for (EdgeUI edge : edges) {
                if (edge.getNode1().equals(node1)
                        || edge.getNode2().equals(node1)) {
                    canConnect = false;
                }
            }

            //Set the style depending on whether its is able to connect
            if (canConnect) {
                getStyleClass().add("draggedOver");
                dragOverNode = this;
            } else {
                dragOverNode = null;
                getStyleClass().add("draggedOverError");
            }
        }
        //Join nodes
        if (dragedNode != null && dragedNode != this) {
            dragedNode.setVisible(false);
            getStyleClass().add("draggedOver");
            dragOverNode = this;
        }
    }

    private void onNodeDragExited(MouseDragEvent dragEvent) {
        if (node2 != null)
            node2.setVisible(true);

        if (dragedNode != null)
            dragedNode.setVisible(true);

        dragOverNode = null;

        getStyleClass().remove("draggedOver");
        getStyleClass().remove("draggedOverError");
    }

    private void onNodeDragged(MouseEvent event) {
        //Set the position of the dragged node to the mouse
        if (node2 != null) {
            if (event.getSceneX() > 0 && event.getSceneX() < Main.mainStage.getWidth()-16)
                node2.setLayoutX(getLayoutX() + event.getX() + getTranslateX());

            if (event.getSceneY() > 0 && event.getSceneY() < Main.mainStage.getHeight()-39)
                node2.setLayoutY(getLayoutY() + event.getY() + getTranslateY());
        }

        if (event.isSecondaryButtonDown()) {
            dragedNode = this;
            dragedNode.toBack();
            dragedNode.edges.forEach(edge -> {
                edge.toBack();
            });
        }

        //Move all selected nodes together with current
        if (event.isSecondaryButtonDown()) {
            //Move the current one
            if (event.getSceneX() > 0 && event.getSceneX() < Main.mainStage.getWidth()-16)
                setLayoutX(startDragX + (event.getSceneX() - startDragX));

            if (event.getSceneY() > 0 && event.getSceneY() < Main.mainStage.getHeight()-39)
                setLayoutY(startDragY + (event.getSceneY() - startDragY));

            //Move all selected notes according the current node
            if (selectedNodes.contains(this)) {
                selectedNodes.forEach(node -> {
                    if (node != this) {
                        double x = node.startDragX + (event.getSceneX() - startDragX);
                        double y = node.startDragY + (event.getSceneY() - startDragY);

                        if (x > 0 && x < Main.mainStage.getWidth()-16)
                            node.setLayoutX(x);

                        if (y > 0 && y < Main.mainStage.getHeight()-39)
                            node.setLayoutY(y);
                    }
                });
            }
        }
    }

    //endregion

    //region Selecting
    /**
     * Do select the node
     */
    public static void selectNode(NodeUI nodeToSelect) {
        if (!controlPressed) {
            //Deselect all the other nodes and edges if shift is not pressed
            if (!shiftPressed) {
                deselectSelectedNodes();
                EdgeUI.deselectSelectedEdges();
            }

            //Select the given node
            if (!nodeToSelect.getStyleClass().contains("selected"))
                nodeToSelect.getStyleClass().add("selected");

            selectedNodes.add(nodeToSelect);
        }

        //Deselect if control is pressed
        else {
            nodeToSelect.removeAllStates();
        }
    }

    /**
     * Deselecting all selected nodes
     */
    public static void deselectSelectedNodes() {
        //Reset all nodes to their base state
        nodes.forEach(node_ -> {
            node_.removeAllStates("startNode", "endNode");
        });

        selectedNodes.clear();
    }

    //endregion

    public void setNewStyle(String newStyle, String... exceptions) {
        removeAllStates(exceptions);

        if (!getStyleClass().contains(newStyle))
            getStyleClass().add(newStyle);
    }

    public void removeAllStates(String... exceptions) {
        for (int i = 0; i < getStyleClass().size(); i++) {
            if (!getStyleClass().get(i).equals("nodeStyle") && !getStyleClass().get(i).equals("button")) {
                boolean isExcepting = false;
                for (String excepting : exceptions) {
                    if (getStyleClass().get(i).equals(excepting)) {
                        isExcepting = true;
                    }
                }

                if (!isExcepting)
                    getStyleClass().remove(i);
            }
        }

        setOnKeyPressed(null);

        selectedNodes.remove(this);
    }

    /**
     * Sets the text of the node
     */
    public void setNodeText(KeyEvent keyEvent, boolean setAll) {
        String text = getText();

        //Remove the text if the user start renaming
        if (removeText && keyEvent.getCode() != KeyCode.SHIFT) {
            setText("");
            removeText = false;
            setNodeText(keyEvent, setAll);
            return;
        }

        //Do different action depending on the key that's pressed
        switch (keyEvent.getCode()) {
            case BACK_SPACE -> text = deleteChar(text);
            case SPACE -> text += " ";
            case ENTER -> {
                //Complete renaming
                nodes.forEach(node -> {
                    node.setOnKeyPressed(null);
                    node.getStyleClass().remove("rename");
                });
            }
            default -> {
                //Only add char if it's a letter or number and write it upper case when shift is pressed
                if (keyEvent.getCode().isLetterKey() || keyEvent.getCode().isDigitKey()) {
                    if (keyEvent.isShiftDown())
                        text += keyEvent.getCode().getChar().toUpperCase();
                    else
                        text += keyEvent.getCode().getChar().toLowerCase();
                }
            }
        }

        //Rename all selected nodes the same, if true
        final String btnText = text;
        if (setAll) {
            selectedNodes.forEach(node -> {
                node.NODE.setName(btnText);
                node.setText(btnText);

                //Update the edge position in case the node expands
                node.edges.forEach(edge -> edge.update());
            });
        }
        else {
            NODE.setName(text);
            setText(text);

            //Update the edge position in case the node expands
            edges.forEach(edge -> edge.update());
        }
    }

    /**
     * Delete the last char of the node name
     */
    private String deleteChar(String text) {
        String newText = text;

        if (text.length() > 0) {
            newText = newText.substring(0, text.length()-1);
        }

        return newText;
    }

    /**
     * Removes this node from the graph and screen
     */
    public void removeNode() {
        paneReference.getChildren().remove(this);

        //Remove all edges the node is connected to
        for (EdgeUI edge : edges) {
            paneReference.getChildren().remove(edge);
            graph.removeEdge(edge.EDGE);

            //Remove edge also in connected node
            if (edge.getNode1().equals(this)) {
                edge.getNode2().edges.remove(edge);
                edge.getNode2().NODE.getEdges().remove(edge.EDGE);
            }
            else if (edge.getNode2().equals(this)) {
                edge.getNode1().edges.remove(edge);
                edge.getNode1().NODE.getEdges().remove(edge.EDGE);
            }
            EdgeUI.deselectEdge(edge);
        }

        //Remove the node as well from the data graph
        graph.removeNode(NODE);

        removeAllStates();
        edges.clear();
        nodes.remove(this);

        //Decrease the count
        NodeUI.count--;
    }

    /**
     * Removes the given node from the graph and screen
     */
    public void removeNode(NodeUI nodeToRemove) {
        paneReference.getChildren().remove(nodeToRemove);

        //Remove all edges the given node is connected to
        for (int i = 0; i < nodeToRemove.edges.size(); i++) {
            EdgeUI edge = nodeToRemove.edges.get(i);

            paneReference.getChildren().remove(edge);
            graph.removeEdge(edge.EDGE);

            //Remove edge also in connected node
            if (edge.getNode1().equals(this)) {
                edge.getNode2().edges.remove(edge);
                edge.getNode2().NODE.getEdges().remove(edge.EDGE);
            }
            else if (edge.getNode2().equals(this)) {
                edge.getNode1().edges.remove(edge);
                edge.getNode1().NODE.getEdges().remove(edge.EDGE);
            }

            EdgeUI.deselectEdge(edge);
        }

        //Remove the given node as well from the data graph
        graph.removeNode(nodeToRemove.NODE);


        nodeToRemove.removeAllStates();
        nodeToRemove.edges.clear();
        nodes.remove(nodeToRemove);

        //Decrease the count
        NodeUI.count--;
    }

    //endregion
}