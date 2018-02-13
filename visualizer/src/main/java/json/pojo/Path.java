package json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class for json Path parsing.
 * Includes list of points represents path
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see <a href="https://github.com/FasterXML/jackson">Jackkson JSON Java parser</a>
 * @see Point
 */

public class Path {
        private List<Point> points;

    /**
     * Default constructor for path creation from json
     * @param points List of {@link Point}
     */
    @JsonCreator
        public Path(@JsonProperty(value = "Path", required = true) List<Point> points){
            this.points = points;
        }

        @Override
        public String toString() {
            return points.toString();
        }

    /**
     * Return list of points represented path
     * @return List of {@link json.pojo.Point}
     */
    public List<Point> getPoints(){
            return points;
        }

}

