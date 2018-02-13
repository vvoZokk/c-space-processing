package json.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.shape.TriangleMesh;

/**
 * Class for json Facet parsing.
 * Presents triangle face with three vertices indexes.
 * For example json structure:
 * <pre>
 *     "Facet": [
 *               {
 *                   "First": 0,
 *                   "Second": 3,
 *                   "Third": 8
 *              }]
 * </pre>
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see <a href="https://github.com/FasterXML/jackson">Jackkson JSON Java parser</a>
 * @see TriangleMesh
 * @see Obstacle
 * @see obstacleVisualizer.Obstacle
 */
public class Facet {

    /**
     * index of first point in facet
     */
    private int first;

    /**
     * index of second point in facet
     */
    private int second;

    /**
     * index of third point in facet
     */
    private int third;

    /**
     * Default constructor for json data parsing.
     * Collect data from "First" "Second" and "Third" json tags
     * into variables with same name
     *
     * @param first index of the first vertex
     * @param second index of the second vertex
     * @param third index of the third vertex
     */
    public Facet(@JsonProperty(value = "First", required = true) int first,
                 @JsonProperty(value = "Second", required = true) int second,
                 @JsonProperty(value = "Third", required = true) int third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Return String in format close to json
     * For example:
     * <pre>
     * first: <code>1</code>
     * second: <code>3</code>
     * third: <code>2</code>
     * </pre>
     * @return String
     */
    public String toString(){
        return "first: " + first + "\n" +
                "second: " + second + "\n" +
                "third: " + third + "\n";
    }

    /**
     * Return the first vertex index in the facet
     * @return int - vertex index
     * @see Facet
     */
    public int getFirst(){
        return first;
    }

    /**
     * Return the second vertex index in the facet
     * @return int - vertex index
     * @see Facet
     */
    public int getSecond(){
        return second;
    }

    /**
     * Return the third vertex index in the facet
     * @return int - vertex index
     * @see Facet
     */
    public int getThird(){
       return third;
    }

}
