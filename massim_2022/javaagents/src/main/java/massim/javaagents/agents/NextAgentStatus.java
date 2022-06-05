package massim.javaagents.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextSurveyedAgent;
import massim.javaagents.percept.NextSurveyedThing;

/**
 *
 * @author AVL
 */
public class NextAgentStatus {

    private String name;
    private String teamName;
    private String lastAction;
    private String lastActionResult;
    private String lastActionParams;

    private int energy;
    private boolean deactivated;
    private String role;

    private HashSet<Point> attachedElements;

    private HashSet<NextMapTile> visibleThings;

    private HashSet<NextMapTile> obstacles;
    private HashSet<NextMapTile> roleZones;
    private HashSet<NextMapTile> goalZones;
    private HashSet<NextMapTile> hits;
    
    private HashSet<NextSurveyedAgent> surveyedAgents; 
    private HashSet<NextSurveyedThing> surveyedThings;
    
    private HashSet<NextMapTile> dispenser;

    private Vector2D position; // Position on the map relative to the starting point
    private NextMap map;

    public NextAgentStatus() {
        name = null;
        teamName = null;
        lastAction = null;
        lastActionResult = null;
        lastActionParams = null;
        energy = -1;
        deactivated = false;
        role = null;
        attachedElements = new HashSet<>();
        position = new Vector2D();
        map = new NextMap();
        dispenser = new HashSet<NextMapTile>();
    }

    public void SetTeam(String teamName) {
        this.teamName = teamName;
    }

    public String GetTeamName() {
        return this.teamName;
    }

    public void SetLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public String GetLastAction() {
        return this.lastAction;
    }

    public void SetDeactivatedFlag(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public boolean GetDeactivatedFlag() {
        return this.deactivated;
    }

    public void SetLastActionResult(String lastActionResult) {
        this.lastActionResult = lastActionResult;
    }

    public String GetLastActionResult() {
        return this.lastActionResult;
    }

    public void SetEnergy(int energy) {
        this.energy = energy;
    }

    public int GetEnergy() {
        return this.energy;
    }

    public void SetRole(String role) {
        this.role = role;
    }

    public String GetRole() {
        return this.role;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public String GetName() {
        return this.name;
    }

    public void SetLastActionParams(String lastActionParams) {
        this.lastActionParams = lastActionParams;
    }

    public String GetLastActionParams() {
        return this.lastActionParams;
    }

    // compare attached elements to NextConstants directions and convert to array ?
    public void SetAttachedElements(HashSet<Point> attachedElements) {
        this.attachedElements = new HashSet();
        for (Point attached : attachedElements) {
            if (attached.equals(NextConstants.WestPoint)
                    || attached.equals(NextConstants.NorthPoint)
                    || attached.equals(NextConstants.EastPoint)
                    || attached.equals(NextConstants.SouthPoint)) {
                this.attachedElements.add(attached);
            }
        }
    }

    public HashSet<Point> GetAttachedElements() {
        return this.attachedElements;
    }

    public Integer GetAttachedElementsAmount() {
        return this.attachedElements.size();
    }

    public void DropAttachedElements() {
        this.attachedElements = new HashSet<>();
    }

    public void SetVision(HashSet<NextMapTile> visionElements) {
        this.visibleThings = visionElements;
    }

    public HashSet<NextMapTile> GetVision() {
        return this.visibleThings;
    }

    public HashSet<NextMapTile> GetObstacles() {
        return obstacles;
    }

    public void SetObstacles(HashSet<NextMapTile> obstacles) {
        this.obstacles = obstacles;
    }

    public HashSet<NextMapTile> GetRoleZones() {
        return roleZones;
    }

    public void SetRoleZones(HashSet<NextMapTile> roleZones) {
        this.roleZones = roleZones;
    }

    public HashSet<NextMapTile> GetGoalZones() {
        return goalZones;
    }

    public void SetGoalZones(HashSet<NextMapTile> goalZones) {
        this.goalZones = goalZones;
    }

    public HashSet<NextMapTile> GetHits() {
        return hits;
    }

    public void SetHits(HashSet<NextMapTile> hits) {
        this.hits = hits;
    }
    
    
    public HashSet<NextSurveyedAgent> getSurveyedAgents() {
        return surveyedAgents;
    }

    public void SetSurveyedAgents(HashSet<NextSurveyedAgent> surveyedAgents) {
        this.surveyedAgents = surveyedAgents;
    }

    public HashSet<NextSurveyedThing> getSurveyedThings() {
        return surveyedThings;
    }

    public void SetSurveyedThings(HashSet<NextSurveyedThing> surveyedThings) {
        this.surveyedThings = surveyedThings;
    }

    public void UpdateMap() {
        if (lastAction.equals("move") && lastActionResult.equals("success")) {
            Vector2D currentStep = new Vector2D(0, 0);

            switch (lastActionParams) {
                case "[n]":
                    currentStep = new Vector2D(0, -1);
                    break;
                case "[e]":
                    currentStep = new Vector2D(1, 0);
                    break;
                case "[s]":
                    currentStep = new Vector2D(0, 1);
                    break;
                case "[w]":
                    currentStep = new Vector2D(-1, 0);
                    break;
            }

            position.add(currentStep);
            HashSet<NextMapTile> visibleNotAttachedThings = new HashSet<>();

            // Only add visible things which are not attached to the agent
            for (NextMapTile thing: visibleThings) {
                if (!attachedElements.contains(thing.getPoint())) {
                    visibleNotAttachedThings.add(thing);
                }
            }
            map.AddPercept(position, visibleNotAttachedThings);
            map.AddPercept(position, obstacles);

            //map.WriteToFile("map.txt");
        }
    }

    /**
     * Returns the position of the Agent on the Map, based on absolute origin
     * @return  Vector2D 
     */
    public Vector2D GetPosition() {
        return map.RelativeToAbsolute(position);
    }

    /**
     * Returns the mapArray stored n the local NextMap instance.
     * Is used in pathfinding algorithms
     * 
     * @return Array of MapTiles
     */
    
    public NextMapTile[][] GetMapArray() {
        return map.GetMap();
    }
    
    /**
     * Returns the size of the NextMap instance
     * @return Vector2D with the X Y Dimensions
     */
    public Vector2D GetSizeOfMap() {
        return map.GetSizeOfMap();
    }
    
    @Override
    public String toString() {

        return "[ " + this.name + " ] \n \n Attached: \n" + attachedElements + " \n \n"
                + "visible Things: \n" + this.visibleThings + " \n"
                + "obstacles: \n" + this.obstacles + " \n"
                + "goalZones: \n" + this.goalZones + " \n"
                + "roleZones: \n" + this.roleZones + " \n"
                + "--------------------------------- \n";

    }
    
    public HashSet<NextMapTile> GetDispenser() {
        return dispenser;
    }

    public void SetDispenser(HashSet<NextMapTile> dispenser) {
        this.dispenser = dispenser;
    }
    
    public Boolean IsObstacleInNextStep(Vector2D position, ECardinals direction) {
    	Vector2D newAgentPosition = new Vector2D(); 
    	switch(direction) {
    	case n:
    		newAgentPosition = position.getAdded(1, 0);
    		break;
    	case e:
    		newAgentPosition = position.getAdded(0, 1);
    		break;
		case s:
    		newAgentPosition = position.getAdded(-1, 0);
    		break;
    	case w:
    		newAgentPosition = position.getAdded(0, -1);
        	break;
    	}
    	
    	Iterator<NextMapTile> it = GetObstacles().iterator();
    	
    	while(it.hasNext())
    	{
    		NextMapTile next = it.next();
    		//Vector2D nextPosition = map.RelativeToAbsolute(next.getPosition());
    		Vector2D zero = new Vector2D();
    		if(zero.equals(newAgentPosition)) {
    			return true;
    		}
    	}
    	return false;
    }
}
