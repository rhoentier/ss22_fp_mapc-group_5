/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package massim.javaagents.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import java.util.ArrayList;
import java.util.List;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextTaskPlanner;
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
    public void testHandleMessage() {
        NextAgent instance = new NextAgent("first", null);
        Percept message = new Percept("MapSizeDiscoveryHasStarted");
        String sender = "testAgent";
        instance.handleMessage(message, sender);
        assertTrue(instance.GetSimulationStatus().HasMapSizeDiscoveryStarted());
    }

    /**
     * Test of step method, of class NextAgent.
     */
    @Test
    public void testStepGroupBuilding() {
        NextAgent instance = new NextAgent("1", null);
        Action expResult = null;
        Action result = instance.step();
        assertEquals(expResult, result);
        NextGroup expGroupResult = new NextGroup(instance, 0);
        NextGroup groupResult = instance.GetAgentGroup();
        assertFalse(groupResult == null);
        assertTrue(groupResult instanceof NextGroup);
        assertEquals(expGroupResult, groupResult);
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

        assertEquals(group2, instance2.GetGroup());
        instance2.SetAgentGroup(group1);
        assertEquals(group1, instance2.GetGroup());
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
    public void testGetGroup() {
        NextAgent instance = new NextAgent("1", null);
        NextGroup group1 = new NextGroup(instance, 0);
        NextGroup expResult = group1;
        NextGroup result = instance.GetAgentGroup();
        assertEquals(expResult, result);

    }

    /**
     * Test of CalculatePath method, of class NextAgent.
     */
    @Test
    public void testCalculatePath() {
        Vector2D target = new Vector2D(-2, 4);
        NextAgent instance = new NextAgent("1", null);
        NextGroup group1 = new NextGroup(instance, 0);
        List<Action> expResult = new ArrayList<>();
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("s")));
        List<Action> result = instance.CalculatePath(target);
        assertEquals(expResult, result);
        assertTrue(result.size() == 6);
    }

    /**
     * Test of CalculatePathNextToTarget method, of class NextAgent. Method
     * returns default path when adjasent tiles are not reachable
     */
    @Test
    public void testCalculatePathNextToTarget() {
        Vector2D target = new Vector2D(-2, 4);
        NextAgent instance = new NextAgent("1", null);
        NextGroup group1 = new NextGroup(instance, 0);
        List<Action> expResult = new ArrayList<>();

        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("w")));
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("s")));
        expResult.add(new Action("move", new Identifier("s")));

        List<Action> result = instance.CalculatePathNextToTarget(target);
        assertEquals(expResult, result);

    }

    /**
     * Test of TellGroup method, of class NextAgent.
     */
    @Test
    public void testTellGroup() {
        NextAgent instance1 = new NextAgent("1", null);
        NextAgent instance2 = new NextAgent("2", null);
        NextAgent instance3 = new NextAgent("3", null);
        NextAgent instance4 = new NextAgent("4", null);

        NextGroup group1 = new NextGroup(instance1, 0);
        group1.AddAgent(instance2);
        group1.AddAgent(instance3);
        group1.AddAgent(instance4);

        String message = "JUNIT TEST";
        instance1.TellGroup(message);

        for (NextAgent agent : group1.GetAgents()) {
            if (!agent.getName().equals("1")) {
                assertEquals("JUNIT TEST", agent.GetAgentStatus().GetName());
            }
        }
    }

    /**
     * Test of TellGroupAgent method, of class NextAgent.
     */
    @Test
    public void testTellGroupAgent() {
        NextAgent instance1 = new NextAgent("1", null);
        instance1.GetAgentStatus().SetName("1");
        NextAgent instance2 = new NextAgent("2", null);
        instance2.GetAgentStatus().SetName("2");
        NextAgent instance3 = new NextAgent("3", null);
        instance3.GetAgentStatus().SetName("3");
        NextAgent instance4 = new NextAgent("4", null);
        instance4.GetAgentStatus().SetName("4");

        NextGroup group1 = new NextGroup(instance1, 0);
        group1.AddAgent(instance2);
        group1.AddAgent(instance3);
        group1.AddAgent(instance4);

        for (NextAgent agent : group1.GetAgents()) {
            assertFalse(agent.GetAgentStatus().GetName().equals("JUNIT TEST"));
        }

        String message = "JUNIT TEST";
        instance1.TellGroupAgent(message, instance3.GetAgentStatus().GetName());

        for (NextAgent agent : group1.GetAgents()) {
            if (agent.getName().equals("3")) {
                assertEquals("JUNIT TEST", agent.GetAgentStatus().GetName());
            } else {
                assertFalse(agent.GetAgentStatus().GetName().equals("JUNIT TEST"));
            }
        }
    }

    /**
     * Test of HandleGroupMessage method, of class NextAgent.
     */
    @Test
    public void testHandleGroupMessage() {
        NextAgent instance = new NextAgent("1", null);
        instance.GetAgentStatus().SetName("1");
        String message = "JUNIT TEST";
        String agent = "random_Agent";
        assertEquals("1", instance.GetAgentStatus().GetName());
        instance.HandleGroupMessage(message, agent);
        assertEquals("JUNIT TEST", instance.GetAgentStatus().GetName());
    }

    /**
     * Test of clearAgentStepMemory method, of class NextAgent.
     */
    @Test
    
    public void testClearAgentStepMemory() {

        NextAgent instance = new NextAgent("1", null);
        NextGroup group = new NextGroup(instance, 0);
        // grow map
        group.SetAgentPosition(instance, new Vector2D(2,2));
        group.SetAgentPosition(instance, new Vector2D(0,0));
        // adjust simulation
        instance.GetSimulationStatus().SetCurrentStep(1);
        // create map
        group.GetGroupMap().setMapTile(new NextMapTile(2,2,1, "free"));
        group.GetGroupMap().setMapTile(new NextMapTile(2,1,1, "free"));        
        group.GetGroupMap().setMapTile(new NextMapTile(2,0,1, "free"));
        
        group.GetGroupMap().setMapTile(new NextMapTile(1,2,1, "free"));
        group.GetGroupMap().setMapTile(new NextMapTile(1,1,1, "free"));        
        group.GetGroupMap().setMapTile(new NextMapTile(1,0,1, "free"));
        
        group.GetGroupMap().setMapTile(new NextMapTile(0,2,1, "free"));
        group.GetGroupMap().setMapTile(new NextMapTile(0,1,1, "free"));        
        group.GetGroupMap().setMapTile(new NextMapTile(0,0,1, "free"));
        
        //Calculate path and block tiles
        instance.SetPathMemory(instance.CalculatePath(new Vector2D(2,2)));
        instance.GetAgentStatus().SetLastActionParams("");
        assertFalse(instance.GetPathMemory().isEmpty());
        
        System.out.println("pathmemory" + instance.GetPathMemory().toString());
    
        assertTrue(group.GetGroupMap().GetMapTile(new Vector2D(0, 1)).CheckAtStep(2));
        assertTrue(group.GetGroupMap().GetMapTile(new Vector2D(0, 2)).CheckAtStep(3));
        assertTrue(group.GetGroupMap().GetMapTile(new Vector2D(1, 2)).CheckAtStep(4));
        assertTrue(group.GetGroupMap().GetMapTile(new Vector2D(2, 2)).CheckAtStep(5));
    
        instance.clearAgentStepMemory();
        
        assertFalse(group.GetGroupMap().GetMapTile(new Vector2D(0, 1)).CheckAtStep(2));
        assertFalse(group.GetGroupMap().GetMapTile(new Vector2D(0, 2)).CheckAtStep(3));
        assertFalse(group.GetGroupMap().GetMapTile(new Vector2D(1, 2)).CheckAtStep(4));
        assertFalse(group.GetGroupMap().GetMapTile(new Vector2D(2, 2)).CheckAtStep(5));
    }

    /**
     * Test of RemoveEmptyGroup method, of class NextAgent.
     */
    @Test
    public void testRemoveEmptyGroup() {
        NextAgent instance = new NextAgent("1", null);
        NextGroup group = new NextGroup(instance, 0);
        
        assertTrue(instance.GetGroup() == group);
        assertTrue(instance.CountAllGroups() == 1);
        
        instance.SetAgentGroup(null);
        group.RemoveAgent(instance);
        
        NextAgent.RemoveEmptyGroup(group);
        
        assertTrue(instance.CountAllGroups() == 0);
    }

}
