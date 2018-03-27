package obstacleVisualizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import json.pojo.CSpace;
import json.pojo.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Json CSpace parser
 * Parse json file and generate {@link CSpace}, used for visualization
 * CSpaceParser generate {@link javafx.scene.shape.Sphere} for each point(start and finish)
 * {@link javafx.scene.shape.Box} for vertices and {@link Obstacle} for each obstacle
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see Node
 */
public class CSpaceParser {

    private List<Node> obstacles;
    private List<Node> points;
    private List<Node> vertices;

    /**
     * Default constructor. Initialize internal variable collections
     */
    public CSpaceParser(){
        obstacles = new ArrayList<>();
        points = new ArrayList<>();
        vertices = new ArrayList<>();
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
     * Return list of vertices (as box object)
     * @return List of {@link Box}
     */
    public List<Node> getVertices(){
       return vertices;
    }

    /**
     * Parse toParse json file to generate CSpace.
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
            CSpace p = mapper.readValue(toParse, CSpace.class);
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
            vertices.add(box);

            // generate obstacles
            for (json.pojo.Obstacle o: p.getObstacles()){
                Color obstacleColor = Color.color(r.nextFloat(), r.nextFloat(), r.nextFloat());
                Obstacle obstShape = new Obstacle(o.getVertices(),o.getFacets(), new PhongMaterial(obstacleColor));
                obstacles.add(obstShape);
            }

        }
        catch (IOException e){
            System.err.print(e.getMessage());
            throw new Error(e.getMessage());
        }
    }

}
