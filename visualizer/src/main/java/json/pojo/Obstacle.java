package json.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class for json Obstacle parsing.
 * Includes list of edges and list of facets
 * Presents 3D obstacle with custom geometry
 *
 * @author      Vladislav Khakin
 * @version     %I%, %G%
 * @see <a href="https://github.com/FasterXML/jackson">Jackkson JSON Java parser</a>
 * @see Point
 * @see Facet
 */
public class Obstacle {

    private int id;
    private List<Point> edges;
    private List<Facet> facets;

    /**
     * Default constructor for obstacle creation from json
     * @param id obstacle identifier number
     * @param edges list of {@link Point}
     * @param facets list of {@link Facet}
     */
    public Obstacle(@JsonProperty(value = "Id", required = true) int id,
                    @JsonProperty(value = "Edge", required = true) List<Point> edges,
                    @JsonProperty(value = "Facet", required = true) List<Facet> facets){
        this.id = id;
        this.edges = edges;
        this.facets = facets;
    }

    /**
     * Return String in format close to json
     */
    @Override
    public String toString(){
        return "id: " + id + "\n" +
                "edges" + edges + "\n" +
                "facets" + facets;
    }

    /**
     * Return list of obstacle points
     * @return List of {@link Point}
     */
    public List<Point> getEdges(){
        return edges;
    }

    /**
     * Return list of facets
     * @return List of {@link Facet}
     */
    public List<Facet> getFacets(){
        return facets;
    }
}
