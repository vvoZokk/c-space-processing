package json.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class for json Field parsing.
 * Includes description, list of points (borders),
 * start and end point and list of obstacles
 * Presents field map, used for visualization
 *
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see <a href="https://github.com/FasterXML/jackson">Jackkson JSON Java parser</a>
 * @see javafx.scene.shape.TriangleMesh
 * @see Obstacle
 * @see Point
 * @see obstacleVisualizer.Obstacle
 */

public class FieldMap {
    private String description;
    private List<Point> border;
    private Point start;
    private Point finish;
    private List<Obstacle> obstacles;

    /** Constructor for field map creation from json file
     *
     * @param text - value of "Description" tag in json file
     * @param border - list of {@link json.pojo.Point} represents border
     * @param start - start {@link json.pojo.Point} of the path
     * @param finish - end {@link json.pojo.Point} of the path
     * @param obstacles list of {@link Obstacle} on the field
     */
    public FieldMap(@JsonProperty(value = "Description", required = true) String text,
                    @JsonProperty(value = "Border", required = true) List<Point> border,
                    @JsonProperty(value = "Start",required = true) Point start,
                    @JsonProperty(value = "Finish",required = true) Point finish,
                    @JsonProperty(value = "Obstacle", required = true) List<Obstacle> obstacles)
    {
       this.description = text;
       this.border = border;
       this.start = start;
       this.finish = finish;
       this.obstacles = obstacles;
    }

    /**
     * Return String in format close to json
     */
    @Override
    public String toString() {
        return "{ description: " + description + "\n" +
        "border: " + border + "\n" +
        "start: " + start + "\n" +
        "finish: " + finish + "\n" +
        "obstacles" + obstacles + "\n";
    }

    /**
     * Return start point of the field
     * @return {@link json.pojo.Point}
     */
    public Point getStart(){
        return start;
    }

    /**
     * Return finish point of the field
     * @return {@link json.pojo.Point}
     */
    public Point getFinish(){
        return finish;
    }

    /**
     * Returns list of obstacles
     * @return List {@link json.pojo.Obstacle}
     */
    public List<Obstacle> getObstacles(){
        return obstacles;
    }

    /**
     * Returns borders as list of Points
     * @return List {@link json.pojo.Point}
     */
    public List<Point> getBorder(){
        return border;
    }
}
