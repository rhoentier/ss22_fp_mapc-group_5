package massim.javaagents.pathfinding;

import eis.iilang.Action;
import eis.iilang.IILObjectVisitor;
import eis.iilang.IILVisitor;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import java.util.ArrayList;
import massim.javaagents.map.Vector2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for NextManhattanPath
 *
 * @author Alexander Lorenz
 */
public class NextManhattanPathTest {

    public NextManhattanPathTest() {
    }

    /**
     * Test of calculatePath method, of class NextManhattanPath.
     */
    @Test
    public void testCalculatePath_int_int() {
        System.out.println("calculatePath");
        int X = 1;
        int Y = 2;
        NextManhattanPath instance = new NextManhattanPath();
        ArrayList<Action> result = instance.calculatePath(X, Y);
        assertEquals(3, result.size());

    }

    /**
     * Test direction generation
     */
    @Test
    public void testCalculatePath_int_int_NorthMovement() {
        System.out.println("calculatePathNorth");
        int X = 0;
        int Y = -1;
        NextManhattanPath instance = new NextManhattanPath();
        ArrayList<Action> result = instance.calculatePath(X, Y);
        assertEquals(new Action("move", new Identifier("n")), result.get(0));
    }

    @Test
    public void testCalculatePath_int_int_SouthMovement() {
        System.out.println("calculatePathSouth");
        int X = 0;
        int Y = 1;
        NextManhattanPath instance = new NextManhattanPath();
        ArrayList<Action> result = instance.calculatePath(X, Y);
        assertEquals(new Action("move", new Identifier("s")), result.get(0));
    }

    @Test
    public void testCalculatePath_int_int_EastMovement() {
        System.out.println("calculatePathEast");
        int X = 1;
        int Y = 0;
        NextManhattanPath instance = new NextManhattanPath();
        ArrayList<Action> result = instance.calculatePath(X, Y);
        assertEquals(new Action("move", new Identifier("e")), result.get(0));
    }

    @Test
    public void testCalculatePath_int_int_WestMovement() {
        System.out.println("calculatePathWest");
        int X = -1;
        int Y = 0;
        NextManhattanPath instance = new NextManhattanPath();
        ArrayList<Action> result = instance.calculatePath(X, Y);
        assertEquals(new Action("move", new Identifier("w")), result.get(0));
    }

    /**
     * Test of CalculatePath method, of class NextManhattanPath.
     */
    @Test
    public void testCalculatePath_Vector2D_Vector2D() {
        System.out.println("CalculatePath");
        Vector2D startPosition = new Vector2D(10, 20);
        Vector2D endPosition = new Vector2D(12, 18);
        ArrayList<Action> result = NextManhattanPath.CalculatePath(startPosition, endPosition);
        assertEquals(4, result.size());
    }

    /**
     * Test direction generation with Vector2D
     */
    @Test
    public void testCalculatePath_Vector2D_Vector2D_NorthMovement() {
        System.out.println("calculatePathNorth");
        Vector2D S = new Vector2D(10, 10);
        Vector2D E = new Vector2D(10, 9);
        ArrayList<Action> result = NextManhattanPath.CalculatePath(S, E);
        assertEquals(new Action("move", new Identifier("n")), result.get(0));
    }

    @Test
    public void testCalculatePath_Vector2D_Vector2D_SouthMovement() {
        System.out.println("calculatePathSouth");
        Vector2D S = new Vector2D(10, 10);
        Vector2D E = new Vector2D(10, 11);
        ArrayList<Action> result = NextManhattanPath.CalculatePath(S, E);
        assertEquals(new Action("move", new Identifier("s")), result.get(0));
    }

    @Test
    public void testCalculatePath_Vector2D_Vector2D_EastMovement() {
        System.out.println("calculatePathEast");
        Vector2D S = new Vector2D(10, 10);
        Vector2D E = new Vector2D(11, 10);
        ArrayList<Action> result = NextManhattanPath.CalculatePath(S, E);
        assertEquals(new Action("move", new Identifier("e")), result.get(0));
    }

    @Test
    public void testCalculatePath_Vector2D_Vector2D_WestMovement() {
        System.out.println("calculatePathWest");
        Vector2D S = new Vector2D(10, 10);
        Vector2D E = new Vector2D(9, 10);
        ArrayList<Action> result = NextManhattanPath.CalculatePath(S, E);
        assertEquals(new Action("move", new Identifier("w")), result.get(0));
    }
}
