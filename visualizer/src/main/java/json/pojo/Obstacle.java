package json.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class for json Obstacle parsing.
 * Includes list of vertices and list of facets
 * Presents 3D obstacle with custom geometry
 *
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see <a href="https://github.com/FasterXML/jackson">Jackkson JSON Java parser</a>
 * @see Point
 * @see Facet
 */
public class Obstacle {

    private List<Point> vertices;
    private List<Facet> facets;

    /**
     * Default constructor for obstacle creation from json
     * @param vertices list of {@link Point}
     * @param facets list of {@link Facet}
     */
    public Obstacle(@JsonProperty(value = "Vertex", required = true) List<Point> vertices,
                    @JsonProperty(value = "Facet", required = true) List<Facet> facets){
        this.vertices = vertices;
        this.facets = facets;
    }

    /**
     * Return String in format close to json
     */
    @Override
    public String toString(){
        return "vertices" + vertices + "\n" +
                "facets" + facets;
    }

    /**
     * Return list of obstacle points
     * @return List of {@link Point}
     */
    public List<Point> getVertices(){
        return vertices;
    }

    /**
     * Return list of facets
     * @return List of {@link Facet}
     */
    public List<Facet> getFacets(){
        return facets;
    }
}
