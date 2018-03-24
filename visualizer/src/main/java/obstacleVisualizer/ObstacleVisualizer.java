package obstacleVisualizer;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.PickResult;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import json.pojo.Facet;
import json.pojo.Point;

import java.io.File;
import java.util.*;
/**
 * Main class for obstacle visualizer
 * Represent scene with 3D scene and toolbar with controls
 *
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @since       1.0
 */

public class ObstacleVisualizer extends Application {

    private static final double SHIFT_MULTIPLIER = 30.0;
    private static final double ROTATION_SPEED = 0.1;

    final private Group root = new Group();
    final private XForm world = new XForm();

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;


    private double dx;
    private double dy;

    private Camera camera = new Camera();
    private List<Node> pathNodes;
    private List<Node> obstacles;
    private List<Node> points;
    private List<Node> bounds;
    private List<Node> objects;
    private List<Point> pathPoints;
    private Map<Node, Material> defaultColors = new HashMap<>();
    private Map<Node, Material> errorColors = new HashMap<>();

    private ToolBar toolBar;
    private Button pathButton;
    private Button clearButton;
    private CheckBox showErrorState;
    private Label errorLabel;


    /**
     * add created {@link Camera} object to scene
     */
    private void buildCamera() {
        root.getChildren().add(camera.getXForm());
        //cameraXform.setPivot(100,100,100);
    }

    /**
     * Create mouse handlers
     * @param scene
     */
    private void handleMouse(SubScene scene){

        scene.setOnMousePressed(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            PickResult pick = event.getPickResult();
            // picking up obstacle and changed draw mode to opposite
            if(pick != null) {
                Node pickedNode = pick.getIntersectedNode();
                if(pickedNode instanceof MeshView){
                    MeshView pickedMeshView = (MeshView) pickedNode;
                    if(pickedMeshView.getDrawMode() == DrawMode.FILL)
                        pickedMeshView.setDrawMode(DrawMode.LINE);
                    else if(pickedMeshView.getDrawMode() == DrawMode.LINE)
                        pickedMeshView.setDrawMode(DrawMode.FILL);
                }
            }
        });

        scene.setOnMouseDragged(event -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            dx = mousePosX - mouseOldX;
            dy = mousePosY - mouseOldY;

            camera.rotateX(dy * ROTATION_SPEED);
            camera.rotateY(dx * ROTATION_SPEED);
            ;
            // }
        });

    }

    /**
     * Create keyboard handlers
     * @param scene
     */
    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if(!event.isShiftDown()) {
                switch (event.getCode()) {
                    case W:
                        camera.moveForward();
                        break;
                    case S:
                        camera.moveBackward();
                        break;
                    case A:
                        camera.moveLeft();
                        break;
                    case D:
                        camera.moveRight();
                        break;
                    case SPACE:
                        camera.moveUp();
                        break;
                    case CONTROL:
                        camera.moveDown();
                        break;
                    case ESCAPE:
                        System.exit(0);
                        break;
                }
            }
            else if(event.isShiftDown()){

                if(event.isShiftDown() && event.getCode() == KeyCode.W){
                    camera.moveForward(SHIFT_MULTIPLIER);
                }
                else if(event.isShiftDown() && event.getCode() == KeyCode.S){
                    camera.moveBackward(SHIFT_MULTIPLIER);
                }
                else if(event.isShiftDown() && event.getCode() == KeyCode.A){
                    camera.moveLeft(SHIFT_MULTIPLIER);
                }
                else if(event.isShiftDown() && event.getCode() == KeyCode.D){
                    camera.moveRight(SHIFT_MULTIPLIER);
                }
                else if(event.isShiftDown() && event.getCode() == KeyCode.SPACE){
                    camera.moveUp(SHIFT_MULTIPLIER);
                }
                else if(event.isShiftDown() && event.getCode() == KeyCode.CONTROL){
                    camera.moveDown(20);
                }
            }
        });
    }


    /**
     * Set 3D subscene params, created parser and parse entered json file with field.
     * Add parsed obstacles points to scene.
     * If file has wrong format, show error message and close application.
     * @param selectedFile loaded file with field
     * @param subScene created subscene
     */
    private void build3DSubscene(File selectedFile, SubScene subScene){

        root.getChildren().add(world);
        root.setDepthTest(DepthTest.ENABLE);
        subScene.setFill(Color.color(0.3, 0.4, 0.6));
        FieldParser fieldParser = new FieldParser();
        try {
            if (selectedFile != null) {
                fieldParser.parse(selectedFile);
                obstacles = fieldParser.getObstacles();
                obstacles.forEach(o->{defaultColors.put(o,((Obstacle) o).getMeshView().getMaterial());});
                points = fieldParser.getPoints();
                bounds = fieldParser.getBounds();
            } else
                System.exit(0);
        }
        catch (Error e){
            System.out.println(e.getMessage());
            Alert errorDialog = new Alert(Alert.AlertType.ERROR);
            errorDialog.setTitle("Ошибка");
            errorDialog.setHeaderText("Неверный формат файла");
            errorDialog.setContentText(e.getMessage());
            Optional<ButtonType> result = errorDialog.showAndWait();
            if (result.get() == ButtonType.OK){
                System.exit(-1);
            }
        }

        objects = new ArrayList<>();
        objects.addAll(obstacles);
        objects.addAll(points);
        objects.addAll(bounds);
        for(Node o: objects){
            if(o instanceof Obstacle) {
                Obstacle obsticle = (Obstacle) o;
                world.getChildren().add(obsticle.getMeshView());
            }
            else
                world.getChildren().add(o);
        }

    }

    // P1 is start line segment point
    // P2 is end line segment points
    // n2 is an Obsticle

    /**
     * Compare if obstacle intersects with line (presented by two points)
     * Create a Ray from start to end point and check, if it intersects with bound box of obstacle.
     * If intersects additionally check intersection with obstacle faces.
     * @param p1 start line point
     * @param p2 end line points
     * @param n2 obstacle object
     * @return true if obstacle intersects with line
     * @throws Exception if argument is not obstacle instance
     */
    private boolean isIntersects(Point p1, Point p2, Node n2)throws Exception{
        Obstacle obstacle = null;
        if(n2 instanceof Obstacle) {
            obstacle = (Obstacle) n2;
        }
        else
            throw new Exception("Second argument should be instance of Obstacle class, but has" +
                    n2.getClass() + " class");

        if(obstacle != null) {
                /* FIRST STEP: Check the ray crosses the bounding box of the shape at any of
                   its 6 faces
                */
            Point3D source = p1.getPoint3D();
            Point3D dest = p2.getPoint3D();
            Point3D direction = dest.subtract(source).normalize();
            Point3D sourceInLocal = obstacle.getMeshView().sceneToLocal(source);

            Bounds locBounds = obstacle.getMeshBoundsInLocal();
            List<Point3D> normals = Arrays.asList(new Point3D(-1, 0, 0), new Point3D(1, 0, 0), new Point3D(0, -1, 0),
                    new Point3D(0, 1, 0), new Point3D(0, 0, -1), new Point3D(0, 0, 1));
            List<Point3D> positions = Arrays.asList(new Point3D(locBounds.getMinX(), 0, 0), new Point3D(locBounds.getMaxX(), 0, 0),
                    new Point3D(0, locBounds.getMinY(), 0), new Point3D(0, locBounds.getMaxY(), 0),
                    new Point3D(0, 0, locBounds.getMinZ()), new Point3D(0, 0, locBounds.getMaxZ()));
            int intersectionCount = 0;
            // For each of 6 bound box faces
            for(int i =0; i < 6; ++i){
                double d = -normals.get(i).dotProduct(positions.get(i));
                double t = -(sourceInLocal.dotProduct(normals.get(i)) + d) / (direction.dotProduct(normals.get(i)));

                // Stop ray, when it exceed (dest - source)
                if(t > source.distance(dest) || t <0)
                    continue;

                Point3D locInter = sourceInLocal.add(direction.multiply(t));
                if (locBounds.contains(locInter)) {
                    Point3D gloInter = obstacle.getMeshView().localToScene(locInter);

                    // DEBUG Show Intersection points with bound box
                    /*
                    Sphere s2 = new Sphere(0.1d);
                    s2.getTransforms().add(new Translate(gloInter.getX(), gloInter.getY(), gloInter.getZ()));
                    s2.setMaterial(new PhongMaterial(Color.GOLD));
                    pathNodes.add(s2);
                    world.getChildren().add(s2);
                    */
                    intersectionCount++;
                }
               // if(intersectionCount > 0){
                 /*
                     SECOND STEP: Check if the ray crosses any of the triangles of the mesh
                 */
                    // triangle mesh
                    Point3D sourceInLocal1 = new Point3D((float) sourceInLocal.getX(), (float) sourceInLocal.getY(), (float) sourceInLocal.getZ());
                    Point3D direction1 = new Point3D((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());

                    List<Facet> intersections = obstacle.getIntersections(sourceInLocal1, direction1);
                    if(intersections.size() > 0){
                        // DEBUG Intersection count with faces
                        /*
                        System.out.println("Intersections = \n");
                        intersections.forEach(System.out::println);
                        */

                        return true;
                    //}
                }
            }

        }

                return false;
    }

    /**
     * Validate path on intersections with obstacles.
     * For each line in path check every obstacle if it intersects with line.
     * Change color of intersected obstacle to red.
     * @param path - parsed path to validate
     * @param nodes - list of obstacles
     */
    private void validatePath(List<Point> path, List<Node> nodes) {
        obstacles.forEach(o ->{
           errorColors.put(o, new PhongMaterial(Color.WHITE));
        });
        Set<Node> intersections = new HashSet<>();
        for (int i = 0; i < path.size(); ++i) {
            for (Node n : obstacles) {

                // Debug - visualize mesh bound box
                //world.getChildren().add(((Obstacle) n).getBoundBox());

                if(i != path.size() - 1) {
                    Point p1 = path.get(i);
                    Point p2 = path.get(i + 1);
                    try {
                        if (isIntersects(p1, p2, n)) {

                            Material m = new PhongMaterial(Color.RED);
                            //c.setMaterial(m);
                            errorColors.put(n, m);
                            System.out.println("Intersecton with " + n);
                            intersections.add(n);
                        }
                    }
                    catch (Exception e){
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
        if(intersections.size() != 0){
            errorLabel.setDisable(false);
            errorLabel.setText("Ошибка.Обнаружено пересечений с препятствием: " + intersections.size());
            errorLabel.setTextFill(Color.RED);
            showErrorState.setDisable(false);
        }
    }

    /**
     * Build path for visualization.
     * Create path parser and parse file.
     * Show error message on parse exception.
     * Additionally validate path on intersections with obstacles
     * @param file path file to parse
     */
    private void buildPath(File file){
        try{
            PathParser pathParser = new PathParser();
            pathParser.parse(file);
            pathNodes = pathParser.getNodes();
            pathPoints = pathParser.getPoints();
            validatePath(pathPoints, pathNodes);
            for(Node n: pathNodes){
                if(n != null)
                    world.getChildren().add(n);
            }

        }
        catch (Error e){
            System.err.print(e.getMessage());
            Alert errorDialog = new Alert(Alert.AlertType.ERROR);
            errorDialog.setTitle("Ошибка");
            errorDialog.setHeaderText("Неверный формат файла");
            errorDialog.setContentText(e.getMessage());
            Optional<ButtonType> result = errorDialog.showAndWait();
            pathButton.setDisable(false);
        }
    }

    /**
     * Build toolbar panel with number of buttons and other controls.
     * Set handler for button and checkbox presses
     * @param pane - pane on which toolbar will be added
     * @param stage - main application stage
     */
    private void buildToolbar(BorderPane pane, Stage stage){
        pathButton = new Button("Проверить путь");
        pathButton.setFocusTraversable(false);
        clearButton = new Button("Очистить путь");
        clearButton.setDisable(true);
        clearButton.setFocusTraversable(false);
        showErrorState = new CheckBox("Показать пересечения");
        showErrorState.setDisable(true);
        showErrorState.setFocusTraversable(false);
        errorLabel = new Label("Ошибка! Обнаружено");
        errorLabel.setDisable(true);
        pathButton.setOnMousePressed(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                fileChooser.setTitle("Open Path File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Json files", "*.json"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(stage);
                buildPath(selectedFile);
                clearButton.setDisable(false);
                pathButton.setDisable(true);
            }
            catch (Exception e) {
                System.err.print(e.getMessage());
                System.exit(-1);
            }
        });
        clearButton.setOnMousePressed(event -> {
            try {
                if (!pathNodes.isEmpty()) {
                    for (Node n : pathNodes) {
                        ObservableList<Node> worldElems = world.getChildren();
                        if (worldElems.contains(n)) {
                            worldElems.remove(n);
                        }
                    }
                    pathPoints.clear();
                    pathNodes.clear();
                }
                pathButton.setDisable(false);
                clearButton.setDisable(true);
                showErrorState.setDisable(true);
                showErrorState.setSelected(false);
                errorLabel.setDisable(true);
                obstacles.forEach(o->{((Obstacle) o).getMeshView().setMaterial(defaultColors.get(o));});            } catch (Exception e) {
                System.err.print(e.getMessage());
                throw new NoSuchElementException(e.getMessage());
            }
        });
        showErrorState.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val) {
                    if(new_val){
                        obstacles.forEach(elem->{
                                ((Obstacle) elem).getMeshView().setMaterial(errorColors.get(elem));
                        });
                    }
                    else
                        obstacles.forEach(elem->{
                                ((Obstacle) elem).getMeshView().setMaterial(defaultColors.get(elem));
                        });
            }

            });
        toolBar = new ToolBar(pathButton);
        toolBar.getItems().add(clearButton);
        toolBar.getItems().add(showErrorState);
        toolBar.getItems().add(errorLabel);
        pane.setTop(toolBar);
    }

    /**
     * Init application loading and file chooser for loading field.
     * Build subscene, camera, toolbar and create keyboard and mouse handlers
     * @param primaryStage internal javafx param
     */
        @Override
        public void start(Stage primaryStage) {

        /*
        Parameters params = getParameters();
        String argumentName = "";
        if(params != null) {
            List<String> list = params.getRaw();
            if (list.size() != 0)
                argumentName = list.get(0);
        }
        File selectedFile = new File("./"+argumentName);
        if(selectedFile == null) {
        */
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Json files", "*.json"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            //}

            SubScene subScene = new SubScene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
            build3DSubscene(selectedFile, subScene);

            try {
                BorderPane pane = new BorderPane();
                pane.setPrefHeight(300);
                pane.setCenter(subScene);
                buildToolbar(pane, primaryStage);
                Scene scene = new Scene(pane);

                buildCamera();
                handleKeyboard(scene);
                handleMouse(subScene);

                subScene.setCamera(camera.getCamera());

                primaryStage.setScene(scene);
                primaryStage.show();
            }
            catch (Exception e){
                System.exit(-1);
            }
        }
    /**
     * Special helper method for sphere creation.
     * Create {@link Sphere} with center in point and defined radius and color
     * @param p1 - first point
     * @param radius - radius of created sphere
     * @param color - color of created line
     * @return created {@link Sphere}
     */
    public static Node createSphere(Point p1, float radius, Color color){
        Random r = new Random();
        Sphere sphere = new Sphere(radius);
        sphere.getTransforms().add(new Translate(p1.x(), p1.y(), p1.z()));
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Special helper method for line creation.
     * Create {@link Cylinder} between two point with defined color.
     * @param p1 - first point
     * @param p2 - second point
     * @param color - color of created line
     * @return created {@link Cylinder}
     */
    public static Node createLine(Point p1, Point p2, Color color){
        float CYLINDER_RADIUS = 0.02f;
        Random r = new Random();
        Point3D yAxis = new Point3D(0.0,1.0,0.0);
        Point3D origin = new Point3D(p1.x(), p1.y(), p1.z());
        Point3D target = new Point3D(p2.x(), p2.y(), p2.z());

        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder cylinder = new Cylinder(CYLINDER_RADIUS, height - CYLINDER_RADIUS);

        cylinder.getTransforms().add(moveToMidpoint);
        cylinder.getTransforms().addAll(rotateAroundCenter);

        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        cylinder.setMaterial(material);
        return cylinder;
    }

        /**
         * The main() method is ignored in correctly deployed JavaFX application.
         * main() serves only as fallback in case the application can not be
         * launched through deployment artifacts, e.g., in IDEs with limited FX
         * support. NetBeans ignores main().
         *
         * @param args the command line arguments
         */
    public static void main(String[] args) {
        launch(args);
    }
}
