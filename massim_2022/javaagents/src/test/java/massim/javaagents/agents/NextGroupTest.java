/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package massim.javaagents.agents;

import java.util.HashSet;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Tests for NextGroup methods
 *
 * @author Alexander Lorenz
 */
@Ignore
public class NextGroupTest {

    public NextGroupTest() {

    }

    /**
     * Test of AddAgent method, of class NextGroup.
     */
    @Test
    public void testAddAgent() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextAgent agent = new NextAgent("second", null);
        NextGroup instance = defaultGroup;
        assertEquals(instance.GetAgents().size(), 1);
        instance.addAgent(agent);
        assertEquals(instance.GetAgents().size(), 2);
        assertTrue(instance.GetAgents().contains(agent));
    }

    /**
     * Test of removeAgent method, of class NextGroup.
     */
    @Test
    public void testRemoveAgent() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextAgent agent = new NextAgent("second", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextGroup instance = defaultGroup;
        instance.addAgent(agent);
        assertEquals(instance.GetAgents().size(), 2);
        instance.removeAgent(agent);
        assertEquals(instance.GetAgents().size(), 1);
        assertTrue(!instance.GetAgents().contains(agent));
    }

    /**
     * Test of CountAgents method, of class NextGroup.
     */
    @Test
    public void testCountAgents() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextAgent agent = new NextAgent("second", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextGroup instance = defaultGroup;
        instance.addAgent(agent);
        int expResult = 2;
        int result = instance.countAgents();
        assertEquals(expResult, result);
    }

    /**
     * Test of GetAgents method, of class NextGroup.
     */
    @Test
    public void testGetAgents() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextGroup instance = defaultGroup;
        HashSet<NextAgent> result = instance.GetAgents();
        assertTrue(result.contains(defaultAgent));
    }

    /**
     * Test of GetGroupID method, of class NextGroup.
     */
    @Test
    public void testGetGroupID() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextGroup instance = new NextGroup(defaultAgent, 12);
        int expResult = 12;
        int result = instance.getGroupID();
        assertEquals(expResult, result);
    }

    /**
     * Test of RemovePositionsOfKnownAgents method, of class NextGroup.
     */
    @Test
    public void testRemovePositionsOfKnownAgents() {
        NextAgent agent1 = new NextAgent("first", null);
        NextAgent agent2 = new NextAgent("second", null);
        NextAgent agent3 = new NextAgent("third", null);

        NextGroup instance = new NextGroup(agent1, 0);
        instance.addAgent(agent1);
        instance.SetAgentPosition(agent1, new Vector2D(10, 10));
        instance.addAgent(agent2);
        instance.SetAgentPosition(agent2, new Vector2D(14, 12));
        instance.addAgent(agent3);
        instance.SetAgentPosition(agent3, new Vector2D(8, 11));

        Vector2D centerPosition = new Vector2D(10, 10);
        HashSet<NextMapTile> positions = new HashSet<>();
        positions.add(new NextMapTile(4, 2, 0));
        positions.add(new NextMapTile(1, 1, 0));
        positions.add(new NextMapTile(0, 0, 0));

        HashSet<NextMapTile> expResult = new HashSet<>();
        expResult.add(new NextMapTile(1, 1, 0));
        HashSet<NextMapTile> result = instance.removePositionsOfKnownAgents(centerPosition, positions);
        assertEquals(expResult, result);
    }

    /**
     * Test of GetAgentPosition method, of class NextGroup.
     */
    @Test
    public void testGetAgentPosition() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextAgent agent = defaultAgent;
        NextGroup instance = defaultGroup;
        instance.addAgent(agent);
        instance.SetAgentPosition(agent, new Vector2D(10, 10));

        Vector2D expResult = new Vector2D(10, 10);
        Vector2D result = instance.GetAgentPosition(agent);
        assertEquals(expResult, result);
    }

    /**
     * Test of SetAgentPosition method, of class NextGroup.
     */
    @Test
    public void testSetAgentPosition() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextAgent agent = defaultAgent;
        NextGroup instance = defaultGroup;
        instance.addAgent(agent);
        instance.SetAgentPosition(agent, new Vector2D(10, 10));

        assertTrue(instance.GetAgentPositions().contains(new Vector2D(10, 10)));
    }

    /**
     * Test of GetGroupMap method, of class NextGroup.
     */
    @Test
    public void testGetGroupMap() {
        NextAgent defaultAgent = new NextAgent("first", null);
        NextGroup defaultGroup = new NextGroup(defaultAgent, 0);
        NextGroup instance = defaultGroup;
        NextMap result = instance.GetGroupMap();
        assertTrue(result != null);
        assertTrue(result instanceof NextMap);
    }

    /**
     * Test of AddGroup method, of class NextGroup.
     */
    @Test
    public void testAddGroup() {
        NextAgent agent1 = new NextAgent("first", null);
        NextAgent agent2 = new NextAgent("second", null);
        NextAgent agent3 = new NextAgent("third", null);

        NextGroup group1 = new NextGroup(agent1, 0);
        NextGroup group2 = new NextGroup(agent2, 0);
        group2.addAgent(agent3);
        
        group1.SetAgentPosition(agent1,new Vector2D(2, 2));
        group2.SetAgentPosition(agent2,new Vector2D(0, 0));
        group2.SetAgentPosition(agent3,new Vector2D(1, 2));

        Vector2D offset = new Vector2D(-5, -5);
        group1.AddGroup(group2, offset);

        assertTrue(group1.GetAgents().contains(agent2));
        assertTrue(group1.GetAgents().contains(agent3));
        assertEquals(new Vector2D(5,5), group1.GetAgentPosition(agent2));
        assertEquals(new Vector2D(6,7), group1.GetAgentPosition(agent3));
        
        assertFalse(group2.GetAgents().contains(agent2));
        assertFalse(group2.GetAgents().contains(agent3));
        
    }

    /**
     * Test of TellGroup method, of class NextGroup.
     */
    @Test
    public void testTellGroup() {

        NextAgent agent1 = new NextAgent("1", null);
        NextAgent agent2 = new NextAgent("2", null);
        NextAgent agent3 = new NextAgent("3", null);
        NextGroup instance = new NextGroup(agent1, 0);
        instance.addAgent(agent2);
        instance.addAgent(agent3);
        agent1.GetAgentStatus().SetName("1");
        agent2.GetAgentStatus().SetName("2");
        agent3.GetAgentStatus().SetName("3");

        String Message = "JUNIT TEST";
        NextAgent sourceAgent = agent1;
        NextAgent targetAgent = agent2;

        instance.TellGroupAgent(Message, targetAgent, sourceAgent);

        assertNotEquals("JUNIT TEST", agent1.GetAgentStatus().GetName());
        assertEquals("JUNIT TEST", agent2.GetAgentStatus().GetName());
        assertEquals("JUNIT TEST", agent3.GetAgentStatus().GetName());

    }

    /**
     * Test of TellGroupAgent method, of class NextGroup.
     */
    @Test
    public void testTellGroupAgent() {

        NextAgent agent1 = new NextAgent("1", null);
        NextAgent agent2 = new NextAgent("2", null);
        NextAgent agent3 = new NextAgent("3", null);
        NextGroup instance = new NextGroup(agent1, 0);
        instance.addAgent(agent2);
        instance.addAgent(agent3);
        agent1.GetAgentStatus().SetName("1");
        agent2.GetAgentStatus().SetName("2");
        agent3.GetAgentStatus().SetName("3");

        String Message = "JUNIT TEST";
        NextAgent sourceAgent = agent1;
        NextAgent targetAgent = agent2;

        instance.TellGroupAgent(Message, targetAgent, sourceAgent);

        assertNotEquals("JUNIT TEST", agent1.GetAgentStatus().GetName());
        assertEquals("JUNIT TEST", agent2.GetAgentStatus().GetName());
        assertNotEquals("JUNIT TEST", agent3.GetAgentStatus().GetName());

    }

    /**
     * Test of GetAgentPositions method, of class NextGroup.
     */
    @Test
    public void testGetAgentPositions() {
        NextAgent agent1 = new NextAgent("1", null);
        NextAgent agent2 = new NextAgent("2", null);
        NextAgent agent3 = new NextAgent("3", null);
        NextGroup instance = new NextGroup(agent1, 0);
        instance.addAgent(agent2);
        instance.addAgent(agent3);
        instance.SetAgentPosition(agent1, new Vector2D(10, 10));
        instance.SetAgentPosition(agent2, new Vector2D(15, 10));
        instance.SetAgentPosition(agent3, new Vector2D(10, 15));

        HashSet<Vector2D> expResult = new HashSet<>();
        expResult.add(new Vector2D(10, 10));
        expResult.add(new Vector2D(15, 10));
        expResult.add(new Vector2D(10, 15));
        HashSet<Vector2D> result = instance.GetAgentPositions();
        assertEquals(expResult, result);
    }

}
