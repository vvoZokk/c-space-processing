package obstacleVisualizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import json.pojo.FieldMap;
import json.pojo.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Json Field parser
 * Parse json file and generate {@link FieldMap}, used for visualization
 * FieldParser generate {@link javafx.scene.shape.Sphere} for each point(start and finish)
 * {@link javafx.scene.shape.Box} for bounds and {@link Obstacle} for each obstacle
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see Node
 */
public class FieldParser {

    private List<Node> obstacles;
    private List<Node> points;
    private List<Node> bounds;

    /**
     * Default constructor. Initialize internal variable collections
     */
    public FieldParser(){
        obstacles = new ArrayList<>();
        points = new ArrayList<>();
        bounds = new ArrayList<>();
    }

    /**
     * Return list with parsed obstacles
     * @return List of {@link Obstacle}
     */
    public List<Node> getObstacles() {
       return obstacles;
    }

    /**
     * Return list with parsed points (as sphere node objects)
     * @return List of {@link javafx.scene.shape.Sphere}
     */
    public List<Node> getPoints(){
       return points;
    }

    /**
     * Return list of bounds (as box object)
     * @return List of {@link Box}
     */
    public List<Node> getBounds(){
       return bounds;
    }

    /**
     * Parse toParse json file to generate FieldMap.
     * Stores generated List of {@link Point}, List of {@link Box} and
     * List of {@link Obstacle} in internal variables
     * @param toParse reference to json file
     * @throws Error throw Error for IOException
     * @see IOException
     * @see Error
     */
    public void parse(File toParse) throws Error {
        Random r = new Random();
        try {
            ObjectMapper mapper = new ObjectMapper();
            FieldMap p = mapper.readValue(toParse, FieldMap.class);
            // generate start point
            Point start = p.getStart();
            points.add(ObstacleVisualizer.createSphere(start,0.2f,Color.YELLOW));
            // generate finish point
            Point finish = p.getFinish();
            points.add(ObstacleVisualizer.createSphere(finish,0.2f,Color.YELLOW));

            // generate borders
            Box box = new Box(10.0,10.0,10.0);
            box.setTranslateX(5.0);
            box.setTranslateY(5.0);
            box.setTranslateZ(5.0);
            //box.setDrawMode(DrawMode.LINE);
            box.setDrawMode(DrawMode.LINE);
            box.setDisable(true);
            bounds.add(box);

            // generate obstacles
            for (json.pojo.Obstacle o: p.getObstacles()){
                Color obstacleColor = Color.color(r.nextFloat(), r.nextFloat(), r.nextFloat());
                Obstacle obstShape = new Obstacle(o.getEdges(),o.getFacets(), new PhongMaterial(obstacleColor));
                obstacles.add(obstShape);
            }

        }
        catch (IOException e){
            System.err.print(e.getMessage());
            throw new Error(e.getMessage());
        }
    }

}
