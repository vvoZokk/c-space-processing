package json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.geometry.Point3D;

/**
 * Class for json Points parsing.
 * Stores three value for x,y,z Point coordinates
 * Presents a point in 3D space
 *
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see <a href="https://github.com/FasterXML/jackson">Jackkson JSON Java parser</a>
 */

public class Point {

    private float x;
    private float y;
    private float z;

    /**
     * Default constructor for json Point parsing
     * @param x float value of x coordinate
     * @param y float value of y coordinate
     * @param z float value of z coordinate
     */
    @JsonCreator
    public Point(@JsonProperty(value = "X", required = true) float x,
                 @JsonProperty(value = "Y", required = true) float y,
                 @JsonProperty(value = "Z", required = true) float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Additional constructor for creation Point from {@link Point3D}
     * @param p javafx Point3D point
     */
    public Point(Point3D p){
        this.x = (float) p.getX();
        this.y = (float) p.getY();
        this.z = (float) p.getZ();
    }

    @Override
    public String toString() {
        return "{ x: " + x + "; y: " + y + "z: " + z + "}";
    }

    /**
     * Return x coordinate of a Point
     * @return x
     */
    public float x() {
        return x;
    }

    /**
     * Return y coordinate of a Point
     * @return y
     */
    public float y() {
        return y;
    }

    /**
     * Return z coordinate of a Point
     * @return z
     */
    public float z() {
        return z;
    }

    /**
     * Return {@link Point3D} created from Point
     * @return new Point3D
     */
    public Point3D getPoint3D(){
        return new Point3D(x,y,z);
    }
}
