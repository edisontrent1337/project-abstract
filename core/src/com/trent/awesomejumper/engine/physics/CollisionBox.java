package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.trent.awesomejumper.utils.Utilities.getNormal;
import static com.trent.awesomejumper.utils.Utilities.sub;

    /**
     * min = lower left corner
     * max = upper right corner
     * a,b,c,d = vertices
     * center = center of bounding box
     * vertices = array that holds all vertices of the shape
     * edges = array that holds all edges of the shape
     * normals = array that holds all normals of the shape
     *
     *
     *             n1
     *             e1
     *       b *-------* c
     *         |       |
     *   n0 e0 |       | e2 n2
     *         |       |
     *      a  *-------* d
     *             e3
     *             n3
     */
/**
 * CollisionBox class representing hit boxes for every entity with physical attributes or the need
 * to resolve collisions.
 * Created by Sinthu on 03.07.2015.
 */
public class CollisionBox {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public enum BoxType {
        REGULAR(0),
        TRIANGLE(3),
        RECTANGLE(4),
        PENTAGON(5),
        HEXAGON(6),
        RANDOM(7);
        private final int VERTICES;
        BoxType(int value) {
            this.VERTICES = value;
        }
    }

    private float width, height;
    private Vector2 position, min, max, offset, center;
    private Array<Vector2> edges, vertices, normals;

    public BoxType type;
    private Array<Vector2>vertexData;
    // DEBUG DRAWING CONSTANTS

    private final float VSIZE = 0.05f;          // size of a vertex for drawing purposes
    private final float VHALF = VSIZE / 2;      // half vertex size
    private final float NORMAL_LENGTH = 0.125f;  // length of a normal

    // TESTING
    private float damageCoefficient;

    // CONSTRUCTORS
    // ---------------------------------------------------------------------------------------------

    // contains collision boxes.
    // TODO: Add a method which creates regular polygonal collision boxes. It could be useful ;)
    // TODO: Implement a collision interface and support <<collision circles>> :D

    // DEFAULT CONSTRUCTOR FOR RECTANGULAR OBJECTS
    public CollisionBox(Vector2 position, float width, float height) {

        this.position = position;
        this.offset = new Vector2(0f,0f);
        this.min = position.cpy();
        this.max = new Vector2(position.cpy().x + width, position.cpy().y + height);
        this.width = width;
        this.height = height;
        this.type = BoxType.RECTANGLE;
        this.center = new Vector2(position.x + width/2f, position.y + height/2f);

        // VERTICES
        // -----------------------------------------------------------------------------------------

        /**
         * Adding all vertices starting from the bottom left corner.
         */
        this.vertices = new Array<>(type.VERTICES);
        this.vertexData = new Array<>(type.VERTICES);
        vertices.add(min);
        vertices.add(new Vector2(min.x, max.y));
        vertices.add(max);
        vertices.add(new Vector2(max.x, min.y));
        vertexData.addAll(vertices);

        addEdgesAndNormals();

    }

    // CONSTRUCTOR FOR VARIABLE SHAPES DEFINED BY VERTEX DATA
    public CollisionBox(Vector2 position, float width, float height, BoxType type, float[]vertexData) {
        /**
         * Throw an IllegalArgumentException because the vertex array data is the only critical
         * parameter.
         */
        if(vertexData.length % 2 != 0) {
            throw new IllegalArgumentException("The vertexData is invalid. Its length is: " + vertexData.length +
                    "Some values are missing.");
        }
        this.position = position;
        this.width = width;
        this.height = height;
        this.min = new Vector2(0f,0f);
        this.max = new Vector2(0f,0f);
        this.offset = new Vector2(0f,0f);
        this.type = type;

        this.vertices = new Array<>(type.VERTICES);

        for(int i = 0; i < vertexData.length; i+=2) {
            // The values in the vertexData array should come in (x,y) tuples.
            Gdx.app.log("SIZE", Integer.toString(vertices.size));
            float x = vertexData[i];
            float y = vertexData[i + 1];

            /**
             * Get the last vertex in vertices and add the new data on top of it to create the next
             * vertex. Finally add the next vertex to the vertices array.
             */
            Vector2 temp = new Vector2(x, y).add(position);

            Gdx.app.log("LAST VERTEX", temp.toString());
            vertices.add(temp);

            if(temp.x < min.x)
                min.x = temp.x;
            if(temp.y < min.y)
                min.y = temp.y;
            if(temp.x > max.x)
                max.x = temp.x;
            if(temp.y > max.y)
                max.y = temp.y;


        }

        Vector2 centroid = new Vector2();
        for(Vector2 v : vertices) {
            centroid.add(v);
        }
        centroid.scl(1f/vertices.size);
        this.center = centroid;

        addEdgesAndNormals();

    }




    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /**
     * Adds edges and normals to the corresponding collections.
     */
    private void addEdgesAndNormals() {
        this.edges = new Array<>();
        this.normals = new Array<>();
        for (int i = 0; i < vertices.size; i++) {
            edges.add(sub(vertices.get(i), vertices.get((i + 1) % vertices.size)));
            normals.add(getNormal(edges.get(i)));
        }


    }

    // DRAW
    // ----------------------------------------------------------------------------------------------
    /** Draws the outline, the vertices and the normals of the collisionBox
     * @param renderer Instance of the libGdx ShapeRenderer Class used to draw the CollisionBox
     */
    public void draw(ShapeRenderer renderer) {
        Color color = renderer.getColor();
        // DRAW OUTER BOUNDING BOX
        renderer.set(ShapeRenderer.ShapeType.Line);
        renderer.setAutoShapeType(true);

        // DRAW NORMALS & EDGES
        for (int i = 0; i < edges.size; i++) {
            // GRAB CURRENT VERTEX, EDGE, NORMAL
            Vector2 edge = edges.get(i);
            Vector2 vertex = vertices.get(i);
            Vector2 normal = normals.get(i);

            // DRAW EDGES
            // VERTEX + EDGE INFORMATION
            renderer.line(vertex, vertex.cpy().add(edge));

            // DRAW NORMALS
            /**
             *  This method uses vector addition to calculate the start and end of
             *  the normal to be drawn.
             *  start = v + 0.5*e
             *  end = v + 0.5*e + 0.25*n
             */

            Vector2 nStart = vertex.cpy().add(edge.cpy().scl(0.5f));
            Vector2 nEnd = vertex.cpy().add(edge.cpy().scl(0.5f)).add(normal.cpy().scl(NORMAL_LENGTH));
            renderer.line(nStart.x, nStart.y, nEnd.x, nEnd.y, Color.BLUE, Color.RED);

        }

        renderer.set(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.GREEN);
        for (Vector2 v : vertices) {
            renderer.rect(v.x - VHALF, v.y - VHALF, VSIZE, VSIZE);
        }

        renderer.rect(center.x- VHALF,center.y - VHALF, VSIZE,VSIZE);

        renderer.setColor(color);
    }

    // UPDATE POSITION OF BOUNDS AND VERTICES
    // ---------------------------------------------------------------------------------------------
    /**
     * Updates the position of the collisionBox in the world. Position is set to the position of the
     * object that holds this collisionBox. An offset is added.
     * @param newPosition  position of the entity that owns this very CollisionBox
     */
    public void update(Vector2 newPosition) {

        position.set(newPosition);
        center.set(newPosition.cpy().add((width + offset.cpy().x )/2f, (offset.cpy().y + height)/2f));
        /**
         * Iterate over all vertices, set their base position to position and add the "path" created
         * by all edges combined to the new position of the vertex.
         */

        for(int i = 0; i < vertices.size; i++) {
            vertices.get(i).set(position.cpy());
            for(int y = 0; y < i; y++) {
                vertices.get(i).add(edges.get(y));
            }
            /**
             * Finally add the global collisionBox offset to each vertex.
             */
            vertices.get(i).add(offset);
        }

    }

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getPositionAndOffset() {
        return new Vector2(position.x + offset.x, position.y + offset.y);
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setOffset(float x, float y) {
        offset.x = x;
        offset.y = y;
    }

    public Vector2 getOffset() {
        return offset;
    }
    public void setOffset(Vector2 offset) {
        this.offset = offset;
    }


    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float width) {
        this.width = width;
    }
    public void setHeight(float height) {
        this.height = height;
    }

    public Array<Vector2> getVertices() {
        return vertices;
    }

    public Array<Vector2> getEdges() {
        return edges;
    }

    public Vector2 getCenter() {
        return center;
    }

    public Array<Vector2> getNormals() {
        return normals;
    }

    public void setDamageCoefficient(float damageCoefficient) {
        this.damageCoefficient = damageCoefficient;
    }
    public float getDamageCoefficient() {
        return damageCoefficient;
    }

}
