/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package massim.javaagents.agents;

import eis.iilang.Action;
import eis.iilang.Percept;
import java.util.ArrayList;
import java.util.List;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 * Tests for NextAgent methods
 *
 * @author Alexander Lorenz
 */
public class NextAgentTest {

    public NextAgentTest() {
    }

    
    /**
     * Test of handleMessage method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testHandleMessage() {
        System.out.println("handleMessage");
        Percept message = null;
        String sender = "";
        NextAgent instance = null;
        instance.handleMessage(message, sender);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of step method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testStep() {
        System.out.println("step");
        NextAgent instance = new NextAgent("1", null);
        Action expResult = null;
        Action result = instance.step();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of GetAgentStatus method, of class NextAgent.
     */
    @Test
    public void testGetAgentStatus() {
        NextAgent instance = new NextAgent("1", null);
        NextAgentStatus result = instance.GetAgentStatus();
        assertFalse(result == null);
        assertTrue(result instanceof NextAgentStatus);
    }

    /**
     * Test of GetSimulationStatus method, of class NextAgent.
     */
    @Test
    public void testGetSimulationStatus() {
        NextAgent instance = new NextAgent("1", null);
        NextSimulationStatus result = instance.GetSimulationStatus();
        assertFalse(result == null);
        assertTrue(result instanceof NextSimulationStatus);
    }

    /**
     * Test of GetAgentGroup method, of class NextAgent.
     */
    @Test
    public void testGetAgentGroup() {
        NextAgent instance = new NextAgent("1", null);
        NextGroup group1 = new NextGroup(instance, 0);
        NextGroup expResult = group1;
        NextGroup result = instance.GetAgentGroup();
        assertEquals(expResult, result);
    }

    /**
     * Test of SetAgentGroup method, of class NextAgent.
     */
    @Test
    public void testSetAgentGroup() {
        System.out.println("SetAgentGroup");
        NextAgent instance = new NextAgent("1", null);
        NextAgent instance2 = new NextAgent("1", null);
        NextGroup group1 = new NextGroup(instance, 0);
        NextGroup group2 = new NextGroup(instance2, 0);

        assertEquals(group2, instance2.GetAgentGroup());
        instance2.SetAgentGroup(group1);
        assertEquals(group1, instance2.GetAgentGroup());
    }

    /**
     * Test of GetPathMemory method, of class NextAgent.
     */
    @Test
    public void testGetPathMemory() {

        NextAgent instance = new NextAgent("1", null);
        List<Action> expResult = new ArrayList<>();
        expResult.add(NextAgentUtil.GenerateEastMove());
        expResult.add(NextAgentUtil.GenerateNorthMove());
        expResult.add(NextAgentUtil.GenerateNorthMove());
        instance.SetPathMemory(expResult);
        List<Action> result = instance.GetPathMemory();
        assertEquals(expResult, result);
        assertFalse(result.isEmpty());
    }

    /**
     * Test of SetPathMemory method, of class NextAgent.
     */
    @Test
    public void testSetPathMemory() {
        NextAgent instance = new NextAgent("1", null);
        List<Action> pathMemory = new ArrayList<>();
        pathMemory.add(NextAgentUtil.GenerateEastMove());
        pathMemory.add(NextAgentUtil.GenerateNorthMove());
        pathMemory.add(NextAgentUtil.GenerateNorthMove());

        assertTrue(instance.GetPathMemory().isEmpty());
        instance.SetPathMemory(pathMemory);
        assertFalse(instance.GetPathMemory().isEmpty());
        assertEquals(pathMemory, instance.GetPathMemory());
    }

    /**
     * Test of ClearPathMemory method, of class NextAgent.
     */
    @Test
    public void testClearPathMemory() {
        NextAgent instance = new NextAgent("1", null);
        List<Action> pathMemory = new ArrayList<>();
        pathMemory.add(NextAgentUtil.GenerateEastMove());
        pathMemory.add(NextAgentUtil.GenerateNorthMove());
        pathMemory.add(NextAgentUtil.GenerateNorthMove());

        instance.SetPathMemory(pathMemory);
        assertFalse(instance.GetPathMemory().isEmpty());
        assertEquals(pathMemory, instance.GetPathMemory());
        
        instance.ClearPathMemory();
        assertTrue(instance.GetPathMemory().isEmpty());
    }

    /**
     * Test of GetPosition method, of class NextAgent.
     */
    @Test
    public void testGetPosition() {
        NextAgent instance = new NextAgent("first", null);
        NextGroup group = new NextGroup(instance, 0);
        group.SetAgentPosition(instance, new Vector2D(10, 10));
        
        Vector2D expResult = new Vector2D(10, 10);
        Vector2D result = instance.GetPosition();
        assertEquals(expResult, result);
    }

    /**
     * Test of GetPositionRef method, of class NextAgent.
     */
    @Test
    public void testGetPositionRef() {
        NextAgent instance = new NextAgent("first", null);
        NextGroup group = new NextGroup(instance, 0);
        group.SetAgentPosition(instance, new Vector2D(10, 10));
        
        Vector2D expResult = new Vector2D(10, 10);
        Vector2D result = instance.GetPositionRef();
        assertEquals(expResult, result);
        result.add(2, 2);
        assertEquals(instance.GetPositionRef(), result);
    }

    /**
     * Test of GetMap method, of class NextAgent.
     */
    @Test
    public void testGetMap() {
        NextAgent instance = new NextAgent("first", null);
        NextGroup group = new NextGroup(instance, 0);
        NextMap expResult = new NextMap(group);
        NextMap result = instance.GetMap();
        assertTrue(result instanceof NextMap);
        assertFalse(result == null);
    }

    /**
     * Test of GetGroup method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testGetGroup() {
        System.out.println("GetGroup");
        NextAgent instance = null;
        NextGroup expResult = null;
        NextGroup result = instance.GetAgentGroup();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    
    /**
     * Test of CalculatePath method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testCalculatePath() {
        System.out.println("CalculatePath");
        Vector2D target = null;
        NextAgent instance = null;
        List<Action> expResult = null;
        List<Action> result = instance.CalculatePath(target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of CalculatePathNextToTarget method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testCalculatePathNextToTarget() {
        System.out.println("CalculatePathNextToTarget");
        Vector2D target = null;
        NextAgent instance = null;
        List<Action> expResult = null;
        List<Action> result = instance.CalculatePathNextToTarget(target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of TellGroup method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testTellGroup() {
        System.out.println("TellGroup");
        String message = "";
        NextAgent instance = null;
        //instance.TellGroupAgent(message, instance.getName());
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of TellGroupAgent method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testTellGroupAgent() {
        System.out.println("TellGroupAgent");
        String message = "";
        String agentName = "";
        NextAgent instance = null;
        //instance.TellGroupAgent(message, agentName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of HandleGroupMessage method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testHandleGroupMessage() {
        System.out.println("HandleGroupMessage");
        String message = "";
        String agent = "";
        NextAgent instance = null;
        //instance.HandleGroupMessage(message, agent);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearAgentStepMemory method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testClearAgentStepMemory() {
        System.out.println("clearAgentStepMemory");
        NextAgent instance = null;
        //instance.clearAgentStepMemory();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of RemoveEmptyGroup method, of class NextAgent.
     */
    @Test
    @Ignore
    public void testRemoveEmptyGroup() {
        System.out.println("RemoveEmptyGroup");
        NextGroup groupToRemove = null;
        NextAgent.RemoveEmptyGroup(groupToRemove);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
