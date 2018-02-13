package obstacleVisualizer;

import com.sun.javafx.sg.prism.NGNode;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Translate;
import json.pojo.Facet;
import json.pojo.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for description 3D obstacle in space.
 * Represents a Shape3D with custom Mesh and Material.
 * Realize methods for to check intersection.
 */
public class Obstacle extends Shape3D {
    private TriangleMesh mesh;
    private Material material;
    private MeshView meshView;
    private List<Point> points;
    private List<Facet> facets;

    /**
     * Default constructor for obstacle creation.
     * Create mesh by points and facets and set
     * filled material yellow color.
     * @param points list with {@link Point}
     * @param facets list with {@link Facet}
     */
    Obstacle(List<Point> points, List<Facet> facets){
        this.points = points;
        this.facets = facets;
        float[] floatPoints = parseList(this.points);
        mesh = createMesh(floatPoints, facets);
        this.material = new PhongMaterial(Color.YELLOW);
        this.meshView = new MeshView(mesh);
        meshView.setMaterial(material);

    }

    /**
     * Constructor for obstacle creation defined material by argument
     * @param points list with {@link Point}
     * @param facets list with {@link Facet}
     * @param material material to apply for created obstacle
     */
    Obstacle(List<Point> points, List<Facet> facets, Material material){
        this.points = points;
        this.facets = facets;
        float[] floatPoints = parseList(this.points);
        mesh = createMesh(floatPoints, this.facets);
        this.material = material;
        meshView = new MeshView(mesh);
        meshView.setMaterial(material);

    }

    private float[] parseList(List<Point> points){
        float[] floatPoints = new float[points.size() * 3];
        int i = 0;
        for(Point p: points){
           floatPoints[i] = p.x();
            floatPoints[i + 1] = p.y();
            floatPoints[i + 2] = p.z();
            i += 3;
        }

        return floatPoints;
    }

    /**
     * Return MeshView object, used to add it on 3D scene.
     * @return {@link MeshView} of created obstacle mesh
     */
    public MeshView getMeshView(){
        return meshView;
    }

    /**
     * Create mesh, defined by list of faces and points.
     * Don't use texture coordinates
     * @param points list with {@link Point} for mesh creation
     * @param facets list with {@link Facet} for mesh creation
     * @return created {@link TriangleMesh}
     */
    TriangleMesh createMesh(float[] points, List<Facet> facets){
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);

        float[] textures = new float[points.length * 2];

        for(int i = 0 ; i < textures.length; ++i)
            textures[i] = 0.0f;

        mesh.getTexCoords().addAll(textures);
        int faces[] = new int [facets.size() * 6];

        int i = 0;
        for(Facet f: facets){
            // point position
            faces[i] = f.getFirst();
            // texture
            faces[i + 1] = 0;
            faces[i + 2] = f.getSecond();
            faces[i + 3] = 0;
            faces[i + 4] = f.getThird();
            faces[i + 5] = 0;
            i += 6;
        }

       mesh.getFaces().addAll(faces);
       return mesh;
       }

    /**
     * Return list of obstacle Facets
     * @return List of {@link Facet}
     */
    public List<Facet> getFacets(){
        return facets;
    }

    /**
     * Return list of obstacle Points
     * @return List of {@link Point}
     */
    public List<Point> getPoints(){
        return points;
    }

    /**
     * Return list of Points, represents normals for each of obstacle face
     * @return List of {@link Point3D}
     */
    public List<Point3D> getNormals(){
        List<Point3D> normals = new ArrayList<>();
        //for(Facet f: facets){
        Facet f = facets.get(1);
            Point3D first = points.get(f.getFirst()).getPoint3D();
            Point3D second = points.get(f.getSecond()).getPoint3D();
            Point3D secondMinusFirst = second.subtract(first);
            Point3D third = points.get(f.getThird()).getPoint3D();
            Point3D thirdMinusFirst = third.subtract(first);
            normals.add(secondMinusFirst.crossProduct(thirdMinusFirst));
        //}
        return normals;
    }
    @Override
    protected NGNode impl_createPeer() {
        return null;
    }

    /**
     * Return rectangular bound box in local coordinates, represented simple obstacle bounds.
     * Use min and max points for each of coordinates to build bound box
     * @return {@link Bounds}
     */
    public Bounds getMeshBoundsInLocal(){
        return meshView.getBoundsInLocal();
    }

    /**
     * Return rectangular bound box in global coordinates, represented simple obstacle bounds.
     * Use min and max points for each of coordinates to build bound box
     * @return {@link Bounds}
     */
    public Bounds getMeshBoundsInGlobal(){
       return meshView.localToScene(getMeshBoundsInLocal());
    }

    /**
     * Return {@link Box} 3D object for obstacle bound box visualization.
     * Returned box drawn with line drawn mode and blueviolet color
     * @return obstacle bound box
     */
    public Box getBoundBox(){
        Bounds global = getMeshBoundsInGlobal();
        Box box = new Box(global.getWidth(),global.getHeight(),global.getDepth());
        box.setDrawMode(DrawMode.LINE);
        box.setMaterial(new PhongMaterial(Color.BLUEVIOLET));
        box.getTransforms().add(new Translate(global.getMinX() + global.getWidth() / 2d,
                global.getMinY() + global.getHeight() / 2d, global.getMinZ() + global.getDepth() / 2d));
        return box;
    }

    /**
     * Return list of {@link Facet} with which origin (in direction) intersected
     * @param origin point in local coordinates
     * @param direction point in local coordinates
     * @return list of intersected faces
     */
    public List<Facet> getIntersections(Point3D origin, Point3D direction){
        final double EPS = 0.000001f;
        return facets.parallelStream().filter(f->{
            // vertices indices
            int p0=(int)f.getFirst();
            int p1=(int)f.getSecond();
            int p2=(int)f.getThird();

            // vertices 3D coordinates
            Point3D a = points.get(p0).getPoint3D();
            Point3D b = points.get(p1).getPoint3D();
            Point3D c = points.get(p2).getPoint3D();

            Point3D edge1 = b.subtract(a);
            Point3D edge2 = c.subtract(a);
            Point3D pvec=direction.crossProduct(edge2);
            float det =(float) edge1.dotProduct(pvec);

            if(det<=-EPS || det>=EPS){
                float inv_det=1f/det;
                Point3D tvec=origin.subtract(a);
                float u = (float) tvec.dotProduct(pvec)*inv_det;
                if(u>=0f && u<=1f){
                    Point3D qvec=tvec.crossProduct(edge1);
                    float v = (float) direction.dotProduct(qvec)*inv_det;
                    if(v>=0 && u+v<=1f){
                        float t = (float) c.dotProduct(qvec)*inv_det;
                        return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }
}
