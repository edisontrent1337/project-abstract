package com.trent.awesomejumper.testing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.trent.awesomejumper.utils.Utilites.getNormal;
import static com.trent.awesomejumper.utils.Utilites.subVec;

/**
 * Created by Sinthu on 03.07.2015.
 */
public class CollisionBox {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------


    /*
     * min = lower left corner
     * max = upper right corner
     * a,b,c,d = vertices
     * vertices = array that holds all vertices of the shape
     * edges = array that holds all edges of the shape
     * normals = array that holds all normals of the shape
     */

    private float width, height;
    private Vector2 position,min,max,a,b,c,d,center;
    private Array<Vector2> edges, vertices, normals;

    // DEBUG DRAWING CONSTANTS

    private final float VSIZE = 0.05f;
    private final float VHALF = VSIZE / 2;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public CollisionBox(Vector2 position, float width, float height) {
        this.position = position;
        // LOWER LEFT CORNER
        this.min = position;
        // UPPER RIGHT CORNER;
        this.max = new Vector2(position.x + width, position.y + height);

        this.width = width;
        this.height = height;

        // VERTICES
        // -----------------------------------------------------------------------------------------

        this.a = new Vector2(min.x, min.y);
        this.b = new Vector2(min.x, max.y);
        this.c = new Vector2(max.x, max.y);
        this.d = new Vector2(max.x, min.y);
        this.center = new Vector2(max.x/2, max.y/2);
        this.vertices = new Array<>();
        vertices.add(a);
        vertices.add(b);
        vertices.add(c);
        vertices.add(d);


        // EDGES & NORMALS
        // -----------------------------------------------------------------------------------------

        this.edges = new Array<>();
        this.normals = new Array<>();

        // LOOP TROUGH ALL VERTICES AND CALCULATE EDGES BETWEEN THEM
        for(int i = 0 ; i< vertices.size; i++) {
            edges.add(subVec(vertices.get(i), vertices.get((i + 1) % 4)));
            normals.add(getNormal(edges.get(i)));
        }

    }


    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------



    // DRAW
    //----------------------------------------------------------------------------------------------
    /*
     * @param renderer Instance of the libGdx ShapRenderer Class used to draw the CollisionBox
     */
    public void draw(ShapeRenderer renderer) {

        // OUTER BOUNDING BOX
        renderer.setColor(Color.BLUE);
        renderer.rect(position.x, position.y, width, height);

        // VERTICES
        renderer.setAutoShapeType(true);
        renderer.set(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.GREEN);
        for(Vector2 v : vertices) {
            renderer.rect(v.x - VHALF, v.y - VHALF, VSIZE, VSIZE);
        }

        // NORMALS & EDGES
        renderer.set(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.PINK);
        for(int i = 0; i< edges.size; i++) {
            // GRAB CURRENT VERTEX, EDGE, NORMAL
            Vector2 edge = edges.get(i);
            Vector2 vertex = vertices.get(i);
            Vector2 normal = normals.get(i);

            // DRAW EDGES
            renderer.setColor(Color.RED);
            // VERTEX + EDGE INFORMATION
            renderer.line(vertex, vertex.cpy().add(edge));

            // DRAW NORMALS

            Vector2 nStart = vertex.cpy().add(edge.cpy().scl(0.5f));
            Vector2 nEnd = vertex.cpy().add(edge.cpy().scl(0.5f)).add(normal.cpy().scl(0.25f));
            renderer.line(nStart.x, nStart.y, nEnd.x, nEnd.y, Color.BLUE, Color.RED);
        }
        renderer.setColor(Color.GREEN);
    }

    // UPDATE POSITION OF BOUNDS AND VERTICES
    // ---------------------------------------------------------------------------------------------
    /*
     * @param delta     time elapsed since last update
     * @param velocity  Velocity of the entity that owns this very CollisionBox
     */
    public void update(float delta, Vector2 velocity) {
        // UPDATE GENERAL POSITION
        position.add(velocity.cpy().scl(delta));
        // UPDATE VERTICES POSITIONS
        for(Vector2 v: vertices) {
            v.add(velocity.cpy().scl(delta));
        }

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
