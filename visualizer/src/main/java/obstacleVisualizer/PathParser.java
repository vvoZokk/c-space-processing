package obstacleVisualizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import json.pojo.Path;
import json.pojo.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Json Path parser
 * Parse json file and generate {@link Path}, used for visualization
 * PathParser generate {@link javafx.scene.shape.Sphere} for each point in path and {@link javafx.scene.shape.Cylinder}
 * as line between points.
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see Path
 * @see Point
 * @see Node
 */
public class PathParser {

    private static float POINT_CLOSING_RADIUS = 0.2f;
    private static float POINT_RADIUS = 0.1f;
    private List<Node> nodes = new ArrayList<>();
    private List<Point> points = new ArrayList<>();

    /**
     * Parse toParse json file to generate Path.
     * Stores generated List of {@link Point} and List of {@link Node} in internal variables
     * @param toParse reference to json file
     * @throws Error throw Error for IOException
     * @see IOException
     * @see Error
     */
    public void parse(File toParse) throws Error {
        try {
            nodes = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            Path path = mapper.readValue(toParse, Path.class);
            List<Point> pointsList = path.getPoints();
            for(int i = 0; i < pointsList.size(); ++i){
                if(i == 0 || i == pointsList.size() -1)
                    nodes.add(ObstacleVisualizer.createSphere(pointsList.get(i), POINT_CLOSING_RADIUS,Color.YELLOW));
                else
                    nodes.add(ObstacleVisualizer.createSphere(pointsList.get(i), POINT_RADIUS,Color.YELLOW));


               if(i != pointsList.size() - 1)
                   nodes.add(ObstacleVisualizer.createLine(pointsList.get(i),pointsList.get(i+1),Color.LIGHTGREEN));

            }
            points.addAll(pointsList);
        } catch (IOException e) {
            System.err.print(e.getMessage());
            throw new Error(e.getMessage());
        }
    }

    /**
     * Return list of parsed {@link Node}
     * Used for visualization
     * @return List of {@link Node}
     */
    public List<Node> getNodes(){
        return nodes;
    }

    /**
     * Return list of parsed {@link Point}
     * Used for path validation
     * @return List of {@link Point}
     */
    public List<Point> getPoints(){
        return points;
    }
}
