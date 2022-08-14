/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package massim.javaagents.pathfinding;

import eis.iilang.Action;
import eis.iilang.Identifier;
import java.util.ArrayList;
import java.util.List;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * Tests for NextManhattanPath
 *
 * @author Alexander Lorenz
 */
public class NextAStarPathTest {

    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 4Args (originalMap, startpoint, target, currentStep)
     */
    @Test
    public void testCalculatePath_4args() {
        System.out.println("calculatePath_with_4Args(originalMap, startpoint, target, currentStep)");
        NextMapTile[][] originalMap = createEmptyMap(3, 3);
        Vector2D startpoint = new Vector2D(0, 1);
        Vector2D target = new Vector2D(2,1);
        int currentStep = -1;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("e")));
        expResult.add(new Action("move", new Identifier("e")));
        List<Action> result = instance.calculatePath(originalMap, startpoint, target, currentStep);
        assertEquals(2, result.size());
        assertEquals(expResult, result);
    }

    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 5Args (originalMap, startpoint, target, centerTheMap, currentStep)
     * with centerTheMap active
     */
    @Test
    public void testCalculatePath_5args_1() {
        System.out.println("calculatePath_with_5Args(originalMap, startpoint, target, currentStep)");
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][1] = new NextMapTile(2,1, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = -1;
        Boolean centerTheMap = true;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        List<Action> result = instance.calculatePath(originalMap, startpoint, target, centerTheMap, currentStep);
        assertEquals(2, result.size());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 5Args (originalMap, startpoint, target, centerTheMap, currentStep)
     * with centerTheMap disabled
     */
    @Test
    public void testCalculatePath_5args_2() {
        System.out.println("calculatePath_with_5Args(originalMap, startpoint, target, currentStep)");
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][1] = new NextMapTile(2,1, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = -1;
        Boolean centerTheMap = false;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        List<Action> result = instance.calculatePath(originalMap, startpoint, target, centerTheMap, currentStep);
        assertEquals(0, result.size());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 5Args (aStarJps, originalMap, startpoint, target, currentStep)
     * with JPS active closed path
     */
    @Test
    public void testCalculatePath_5args_3() {
        System.out.println("calculatePath_with_5Args(aStarJps, originalMap, startpoint, target, currentStep)");
        Boolean aStarJps = true;
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][1] = new NextMapTile(2,1, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = -1;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        List<Action> result = instance.calculatePath(aStarJps, originalMap, startpoint, target, currentStep);
        assertEquals(0, result.size());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 5Args (aStarJps, originalMap, startpoint, target, currentStep)
     * with JPS active open path
     */
    @Test
    public void testCalculatePath_5args_4() {
        System.out.println("calculatePath_with_5Args(aStarJps, originalMap, startpoint, target, currentStep)");
        Boolean aStarJps = true;
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = 0;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("n")));
        expResult.add(new Action("move", new Identifier("e")));
        expResult.add(new Action("move", new Identifier("e")));
        expResult.add(new Action("move", new Identifier("e")));
        expResult.add(new Action("move", new Identifier("s")));
        List<Action> result = instance.calculatePath(aStarJps, originalMap, startpoint, target, currentStep);
        assertEquals(5, result.size());
        assertEquals(expResult, result);
    }

    
    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 6Args (originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep);
     * with centerTheMap disabled block in the path
     */
    
    @Test
    public void testCalculatePath_6args_1() {
        System.out.println("calculatePath_with_6Args(originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep)");
        NextMapTile[][] originalMap = createEmptyMap(4, 4);
        originalMap[1][0] = new NextMapTile(1,0, -1, "obstacle");        
        originalMap[1][2] = new NextMapTile(1,2, -1, "obstacle");        
        originalMap[1][3] = new NextMapTile(1,3, -1, "obstacle");        
        originalMap[3][2] = new NextMapTile(3,2, -1, "block");
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(2,2);
        int currentStep = 0;
        Boolean centerTheMap = true;
        Boolean strictWalkable = true;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("n")));
        List<Action> result = instance.calculatePath(originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep);
        assertEquals(4, result.size());
        assertEquals(expResult, result);
    }



    /**
     * Test of calculatePath method, of class NextAStarPath.
     * using 6Args (aStarJps, originalMap, startpoint, target, centerTheMap, currentStep)
     * with JPS active, centerTheMap active, open path
     */
    
    @Test
    public void testCalculatePath_6args_2() {
        System.out.println("calculatePath_with_6Args(aStarJps, originalMap, startpoint, target, centerTheMap, currentStep)");
        Boolean aStarJps = true;
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        Boolean centerTheMap = true;
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = 0;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        List<Action> result = instance.calculatePath(aStarJps, originalMap, startpoint, target, centerTheMap, currentStep);
        assertEquals(2, result.size());
        assertEquals(expResult, result);
    }

    /**Test of calculatePath method, of class NextAStarPath.
     * using 7Args (aStarJps, originalMap, startpoint, target, currentStep)
     * with JPS active , centerTheMap active and strictWalkable active
     * open path
     */
    @Test
    public void testCalculatePath_7args() {
        System.out.println("calculatePath_with_5Args((aStarJps, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep)");
        Boolean aStarJps = true;
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        Boolean centerTheMap = true;
        Boolean strictWalkable = true;
        
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = 0;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        List<Action> result = instance.calculatePath(aStarJps, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep);
        assertEquals(2, result.size());
        assertEquals(expResult, result);
        
    }

     /**Test of calculatePath method, of class NextAStarPath.
     * using 7Args (aStarJps, originalMap, startpoint, target, currentStep)
     * with JPS active , centerTheMap active and strictWalkable active
     * block in the path
     */
    @Test
    public void testCalculatePath_7args_1() {
        System.out.println("calculatePath_with_7Args((aStarJps, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep)");
        Boolean aStarJps = true;
        NextMapTile[][] originalMap = createEmptyMap(5, 5);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        originalMap[4][2] = new NextMapTile(4,2, -1, "block"); 
        Boolean centerTheMap = true;
        Boolean strictWalkable = true;
        
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = 0;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("n")));
        List<Action> result = instance.calculatePath(aStarJps, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep);
        assertEquals(4, result.size());
        assertEquals(expResult, result);
    }

 /**Test of calculatePath method, of class NextAStarPath.
     * using 7Args (aStarJps, originalMap, startpoint, target, currentStep)
     * with JPS disabled , centerTheMap active and strictWalkable active
     * block in the path
     */
    @Test
    public void testCalculatePath_7args_2() {
        System.out.println("calculatePath_with_7Args((aStarJps, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep)");
        Boolean aStarJps = false;
        NextMapTile[][] originalMap = createEmptyMap(6, 6);
        originalMap[2][0] = new NextMapTile(2,0, -1, "obstacle");        
        originalMap[2][2] = new NextMapTile(2,2, -1, "obstacle");        
        originalMap[2][3] = new NextMapTile(2,3, -1, "obstacle");        
        originalMap[2][4] = new NextMapTile(2,4, -1, "obstacle");
        originalMap[2][5] = new NextMapTile(2,5, -1, "obstacle");
        originalMap[4][2] = new NextMapTile(4,2, -1, "block"); 
        Boolean centerTheMap = true;
        Boolean strictWalkable = true;
        
        Vector2D startpoint = new Vector2D(0, 2);
        Vector2D target = new Vector2D(3,2);
        int currentStep = 0;
        NextAStarPath instance = new NextAStarPath();
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("n")));
        List<Action> result = instance.calculatePath(aStarJps, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep);
        assertEquals(5, result.size());
        assertEquals(expResult, result);
    }

    private NextMapTile[][] createEmptyMap(int xDimension, int yDimension) {

        NextMapTile[][] map = new NextMapTile[xDimension][yDimension];
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                map[x][y] = new NextMapTile(x, y, -1);
            }
        }
        return map;
    }

}
