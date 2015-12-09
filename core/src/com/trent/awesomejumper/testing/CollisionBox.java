package com.trent.awesomejumper.testing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.trent.awesomejumper.utils.Utilities.getNormal;
import static com.trent.awesomejumper.utils.Utilities.subVec;

/**
 * Created by Sinthu on 03.07.2015.
 */
public class CollisionBox {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------


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

    private float width, height;
    private Vector2 position, min, max, a, b, c, d;
    private Vector2 offset;
    private Array<Vector2> edges, vertices, normals;

    // DEBUG DRAWING CONSTANTS

    private final float VSIZE = 0.05f;          // size of a vertex for drawing purposes
    private final float VHALF = VSIZE / 2;      // half vertex size
    private final float NORMAL_LENGTH = 0.125f;  // length of a normal

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public CollisionBox(Vector2 position, float width, float height) {

        this.position = position;
        this.offset = new Vector2(0f,0f);
        this.min = position.cpy();
        this.max = new Vector2(position.cpy().x + width, position.cpy().y + height);
        this.width = width;
        this.height = height;

        // VERTICES
        // -----------------------------------------------------------------------------------------

        this.a = new Vector2(min.x, min.y);
        this.b = new Vector2(min.x, max.y);
        this.c = new Vector2(max.x, max.y);
        this.d = new Vector2(max.x, min.y);
        this.vertices = new Array<>();
        // ADD ALL VERTICES TO THE VERTEX ARRAY
        vertices.add(a);
        vertices.add(b);
        vertices.add(c);
        vertices.add(d);

        // EDGES & NORMALS
        // -----------------------------------------------------------------------------------------

        this.edges = new Array<>();
        this.normals = new Array<>();

        // LOOP TROUGH ALL VERTICES AND CALCULATE EDGES BETWEEN THEM
        for (int i = 0; i < vertices.size; i++) {
            edges.add(subVec(vertices.get(i), vertices.get((i + 1) % 4)));
            normals.add(getNormal(edges.get(i)));
        }

    }


    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------


    // DRAW
    // ----------------------------------------------------------------------------------------------
    /**
     * @param renderer Instance of the libGdx ShapeRenderer Class used to draw the CollisionBox
     */
    public void draw(ShapeRenderer renderer) {

        // DRAW OUTER BOUNDING BOX
        renderer.set(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.BLUE);
        renderer.rect(position.x, position.y, width, height);

        // DRAW VERTICES
        renderer.setAutoShapeType(true);


        // DRAW NORMALS & EDGES
        renderer.setColor(Color.PINK);
        for (int i = 0; i < edges.size; i++) {
            // GRAB CURRENT VERTEX, EDGE, NORMAL
            Vector2 edge = edges.get(i);
            Vector2 vertex = vertices.get(i);
            Vector2 normal = normals.get(i);

            // DRAW EDGES
            renderer.setColor(Color.RED);
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


        renderer.setColor(Color.GREEN);
    }

    // UPDATE POSITION OF BOUNDS AND VERTICES
    // ---------------------------------------------------------------------------------------------
    /**
     * @param velocity  velocity of the entity that owns this very CollisionBox
     */
    public void update(Vector2 newPosition) {
        // UPDATE GENERAL POSITION AND POSITION OF ALL VERTICES
        /**
         * O : lower left corner = position,
         * 1 : upper left corner
         * 2 : upper right corner
         * 3 : lower right corner
         */

        position.set(newPosition);
        vertices.get(0).set(position).add(offset);
        vertices.get(1).set(position.x, position.y + height).add(offset);
        vertices.get(2).set(position.x + width, position.y + height).add(offset);
        vertices.get(3).set(position.x + width, position.y).add(offset);


    }

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Vector2 getPosition() {
        return position;
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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Array<Vector2> getVertices() {
        return vertices;
    }

    public Array<Vector2> getEdges() {
        return edges;
    }

    public Array<Vector2> getNormals() {
        return normals;
    }


}
