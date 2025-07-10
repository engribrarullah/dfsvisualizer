// File: Main.java

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends Application {
    Graph graph = new Graph();
    Pane graphPane = new Pane();
    TextArea output = new TextArea();
    Map<String, Circle> nodeCircles = new HashMap<>();
    Map<String, Text> nodeLabels = new HashMap<>();
    Map<String, Color> nodeColors = new HashMap<>();
    Set<Color> usedColors = new HashSet<>();
    List<Node> edgeVisuals = new ArrayList<>();
    Label errorLabel = new Label();
    FlowPane nodeListFlow = new FlowPane();
    Random rand = new Random();

    private final List<Color> colorPalette = new ArrayList<>(Arrays.asList(
            Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.PURPLE,
            Color.BROWN, Color.DARKCYAN, Color.GOLD, Color.HOTPINK, Color.DARKORCHID
    ));

    class DirectedEdge {
        String from;
        String to;
        DirectedEdge(String from, String to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DirectedEdge)) return false;
            DirectedEdge e = (DirectedEdge) o;
            return from.equals(e.from) && to.equals(e.to);
        }
        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

    Set<DirectedEdge> currentEdges = new HashSet<>();

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20px; -fx-background-color: #ffffff; -fx-font-family: 'Poppins';");

        TextField nodeField = new TextField();
        nodeField.setPromptText("Node name");
        nodeField.setStyle(textFieldStyle());

        ColorPicker colorPicker = new ColorPicker(Color.DODGERBLUE);

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.setStyle(buttonStyle("#2196F3"));
        addNodeBtn.setOnAction(e -> {
            String node = nodeField.getText().trim();
            Color color = colorPicker.getValue();
            if (!node.isEmpty()) {
                if (nodeCircles.containsKey(node)) {
                    showError("\u274C Node '" + node + "' already exists.");
                } else {
                    while (usedColors.contains(color)) {
                        color = getNextAvailableColor();
                    }
                    graph.addNode(node);
                    nodeColors.put(node, color);
                    usedColors.add(color);
                    drawNode(node, color);
                    nodeListFlow.getChildren().add(new Label(node));
                    nodeField.clear();
                    colorPicker.setValue(getNextAvailableColor());
                    errorLabel.setText("");
                    output.appendText("Node added: " + node + "\n");
                }
            } else {
                showError("\u274C Node name cannot be empty.");
            }
        });

        TextField fromField = new TextField();
        fromField.setPromptText("From");
        fromField.setStyle(textFieldStyle());

        TextField toField = new TextField();
        toField.setPromptText("To");
        toField.setStyle(textFieldStyle());

        Button addEdgeBtn = new Button("Add Edge");
        addEdgeBtn.setStyle(buttonStyle("#4CAF50"));
        addEdgeBtn.setOnAction(e -> {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            if (!from.isEmpty() && !to.isEmpty()) {
                if (!graph.hasNode(from)) {
                    showError("\u274C Node '" + from + "' does not exist.");
                } else if (!graph.hasNode(to)) {
                    showError("\u274C Node '" + to + "' does not exist.");
                } else {
                    DirectedEdge edge = new DirectedEdge(from, to);
                    if (currentEdges.contains(edge)) {
                        showError("\u274C Edge already exists.");
                        return;
                    }
                    graph.addEdge(from, to);
                    currentEdges.add(edge);
                    drawEdge(from, to);
                    fromField.clear();
                    toField.clear();
                    errorLabel.setText("");
                    output.appendText("Edge added: " + from + " → " + to + "\n");
                }
            }
        });

        Button resetBtn = new Button("Reset Graph");
        resetBtn.setStyle(buttonStyle("#f44336"));
        resetBtn.setOnAction(e -> resetGraph());

        Button saveImageBtn = new Button("Save as Image");
        saveImageBtn.setStyle(buttonStyle("#607D8B"));
        saveImageBtn.setOnAction(e -> saveGraphAsImage());

        TextField startNodeField = new TextField();
        startNodeField.setPromptText("Start Node for DFS");
        startNodeField.setStyle(textFieldStyle());

        Button dfsBtn = new Button("Run DFS");
        dfsBtn.setStyle(buttonStyle("#9C27B0"));
        dfsBtn.setOnAction(e -> {
            String start = startNodeField.getText().trim();
            if (!start.isEmpty() && graph.hasNode(start)) {
                Set<String> visited = new LinkedHashSet<>();
                List<String> result = new ArrayList<>();
                graph.dfs(start, visited, result);
                animatePath(result);
                output.appendText("DFS from " + start + ": " + String.join(" → ", result) + "\n");
            }
        });

        ScrollPane nodeScrollPane = new ScrollPane(nodeListFlow);
        nodeScrollPane.setPrefHeight(50);
        nodeScrollPane.setFitToWidth(true);
        nodeListFlow.setHgap(10);
        nodeListFlow.setVgap(10);

        HBox addNodeBox = new HBox(10, nodeField, colorPicker, addNodeBtn);
        HBox addEdgeBox = new HBox(10, fromField, toField, addEdgeBtn);
        HBox dfsBox = new HBox(10, startNodeField, dfsBtn);
        HBox topRow = new HBox(10);
        topRow.getChildren().addAll(new Label("Nodes in Graph:"), nodeScrollPane, resetBtn, saveImageBtn);

        StackPane graphContainer = new StackPane(graphPane);
        graphContainer.setPrefHeight(500);
        graphContainer.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 2px;");
        graphContainer.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            graphPane.setScaleX(graphPane.getScaleX() * zoomFactor);
            graphPane.setScaleY(graphPane.getScaleY() * zoomFactor);
        });

        graphPane.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                Node target = e.getPickResult().getIntersectedNode();
                if (target instanceof Circle) {
                    Circle circle = (Circle) target;
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Delete Node");
                    deleteItem.setOnAction(event -> {
                        String toRemove = null;
                        for (Map.Entry<String, Circle> entry : nodeCircles.entrySet()) {
                            if (entry.getValue() == circle) {
                                toRemove = entry.getKey();
                                break;
                            }
                        }
                        if (toRemove != null) {
                            removeNode(toRemove);
                        }
                    });
                    contextMenu.getItems().add(deleteItem);
                    contextMenu.show(graphPane, e.getScreenX(), e.getScreenY());
                }
            }
        });

        VBox.setVgrow(graphContainer, Priority.ALWAYS);

        root.getChildren().addAll(
                addNodeBox,
                addEdgeBox,
                topRow,
                dfsBox,
                graphContainer,
                errorLabel,
                output
        );

        stage.setScene(new Scene(root, 1100, 900));
        stage.setTitle("\u26A1 DFS Visualizer");
        stage.show();
    }

    private void removeNode(String name) {
        Circle circle = nodeCircles.remove(name);
        Text label = nodeLabels.remove(name);
        graphPane.getChildren().removeAll(circle, label);

        for (Node node : nodeListFlow.getChildren()) {
            if (node instanceof Label lbl && lbl.getText().equals(name)) {
                nodeListFlow.getChildren().remove(lbl);
                break;
            }
        }

        nodeColors.remove(name);
        graph.removeNode(name);
        usedColors.remove(circle.getFill());
        currentEdges.removeIf(edge -> edge.from.equals(name) || edge.to.equals(name));
        redrawEdges();
        output.appendText("Node removed: " + name + "\n");
    }

    private void saveGraphAsImage() {
        WritableImage image = graphPane.snapshot(new SnapshotParameters(), null);
        File file = new File("graph_snapshot.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            output.appendText("Graph saved as graph_snapshot.png\n");
        } catch (IOException e) {
            showError("Failed to save image.");
        }
    }

    private void resetGraph() {
        graph = new Graph();
        nodeCircles.clear();
        nodeLabels.clear();
        nodeColors.clear();
        usedColors.clear();
        edgeVisuals.clear();
        currentEdges.clear();
        graphPane.getChildren().clear();
        nodeListFlow.getChildren().clear();
        output.clear();
        errorLabel.setText("");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private String textFieldStyle() {
        return "-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 8px; -fx-font-size: 14px; -fx-font-family: 'Poppins';";
    }

    private String buttonStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8px; -fx-font-size: 14px; -fx-font-family: 'Poppins';";
    }

    private Color getNextAvailableColor() {
        for (Color color : colorPalette) {
            if (!usedColors.contains(color)) {
                return color;
            }
        }
        return Color.GRAY;
    }

    private void drawNode(String name, Color color) {
        double x = 100 + rand.nextInt(800);
        double y = 100 + rand.nextInt(300);

        Circle circle = new Circle(x, y, 25);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);

        Text label = new Text(name);
        label.setX(x - 5);
        label.setY(y + 5);

        Delta dragDelta = new Delta();
        circle.setOnMousePressed(e -> {
            dragDelta.x = circle.getCenterX() - e.getX();
            dragDelta.y = circle.getCenterY() - e.getY();
        });
        circle.setOnMouseDragged(e -> {
            circle.setCenterX(e.getX() + dragDelta.x);
            circle.setCenterY(e.getY() + dragDelta.y);
            label.setX(circle.getCenterX() - 5);
            label.setY(circle.getCenterY() + 5);
            redrawEdges();
        });

        graphPane.getChildren().addAll(circle, label);
        nodeCircles.put(name, circle);
        nodeLabels.put(name, label);
    }

    private void redrawEdges() {
        graphPane.getChildren().removeAll(edgeVisuals);
        edgeVisuals.clear();
        for (DirectedEdge edge : currentEdges) {
            drawEdge(edge.from, edge.to);
        }
    }

    private void drawEdge(String from, String to) {
        if (!currentEdges.contains(new DirectedEdge(from, to))) return;

        Circle start = nodeCircles.get(from);
        Circle end = nodeCircles.get(to);
        if (start == null || end == null) return;

        double sx = start.getCenterX();
        double sy = start.getCenterY();
        double ex = end.getCenterX();
        double ey = end.getCenterY();

        double dx = ex - sx;
        double dy = ey - sy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double offsetX = dx * 25 / dist;
        double offsetY = dy * 25 / dist;

        Line line = new Line(sx + offsetX, sy + offsetY, ex - offsetX, ey - offsetY);
        line.setStrokeWidth(2);

        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(0.0, 0.0, -10.0, -5.0, -10.0, 5.0);
        arrow.setFill(Color.BLACK);

        double angle = Math.atan2(dy, dx);
        arrow.setTranslateX(ex - offsetX);
        arrow.setTranslateY(ey - offsetY);
        arrow.setRotate(Math.toDegrees(angle));
        arrow.setViewOrder(-1);

        ContextMenu edgeMenu = new ContextMenu();
        MenuItem deleteEdgeItem = new MenuItem("Delete Edge: " + from + " → " + to);
        deleteEdgeItem.setOnAction(event -> {
            currentEdges.remove(new DirectedEdge(from, to));
            graph.removeEdge(from, to);
            redrawEdges();
            output.appendText("Edge removed: " + from + " → " + to + "\n");
        });
        edgeMenu.getItems().add(deleteEdgeItem);

        line.setOnContextMenuRequested(e -> edgeMenu.show(line, e.getScreenX(), e.getScreenY()));
        arrow.setOnContextMenuRequested(e -> edgeMenu.show(arrow, e.getScreenX(), e.getScreenY()));

        graphPane.getChildren().addAll(line, arrow);
        edgeVisuals.add(line);
        edgeVisuals.add(arrow);
    }

    private void animatePath(List<String> path) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < path.size() - 1; i++) {
            final int idx = i;
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(i), e -> {
                drawEdge(path.get(idx), path.get(idx + 1));
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    }

    private static class Delta {
        double x, y;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
