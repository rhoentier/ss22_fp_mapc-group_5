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
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextSurveyedAgent;
import massim.javaagents.percept.NextSurveyedThing;

/**
 * Part of Agent Belief System
 * 
 * Agent related status values
 * 
 * @author AVL
 */
public class NextAgentStatus {

    private NextAgent nextAgent;
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
    private NextRole currentRole;

    public NextAgentStatus(NextAgent nextAgent) {
        this.nextAgent = nextAgent;
        name = null;
        teamName = null;
        lastAction = null;
        lastActionResult = null;
        lastActionParams = null;
        energy = -1;
        deactivated = false;
        role = null;
        attachedElements = new HashSet<>();
        dispenser = new HashSet<NextMapTile>();
        currentRole = new NextRole("dummy", 0, null, null, 0, 0);
    }

    public NextRole GetCurrentRole() {
        return currentRole;
    }

    public void SetCurrentRole(NextRole currentRole) {
        this.currentRole = currentRole;
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

    public HashSet<NextMapTile> GetVisibleThings() {
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
    
    public ArrayList<NextMapTile> IsObstacleInNextStep(ECardinals direction) {
    	ArrayList<Vector2D> newAgentPositionLst = new ArrayList<Vector2D>(); 
    	HashSet<Point> attachedElements = this.nextAgent.getAgentStatus().GetAttachedElements();
    	ArrayList<NextMapTile> result = new ArrayList<NextMapTile>();
    	Vector2D newDirection = new Vector2D(0,0);
    	switch(direction) {
    	case n:
    		// **O****
			// **XA***
			// *******
    		newDirection = new Vector2D(0 ,-1);
    		newAgentPositionLst.add(newDirection); // no block			
			if(!attachedElements.isEmpty()) {
				Iterator<Point> attachedElementsIt = attachedElements.iterator();
				while(attachedElementsIt.hasNext()) {
					Point next = attachedElementsIt.next();
					newAgentPositionLst.add(new Vector2D(next.getLocation().x + newDirection.x, next.getLocation().y + newDirection.y)); 		
				}
			}
    		//newAgentPosition = new Vector2D(0, -1);
    		break;
    	case e:
    		// *******
			// **XAO**
			// *******
    		newDirection = new Vector2D(1, 0);
    		newAgentPositionLst.add(newDirection); // no block				
			if(!attachedElements.isEmpty())
			{
				Iterator<Point> attachedElementsIt = attachedElements.iterator();
				while(attachedElementsIt.hasNext()) {
					Point next = attachedElementsIt.next();
					newAgentPositionLst.add(new Vector2D(next.getLocation().x + newDirection.x, next.getLocation().y + newDirection.y)); 
				}
			}
    		//newAgentPosition = new Vector2D(1, 0);
    		break;
		case s:
			// *******
			// **XA***
			// **OO***
    		newDirection = new Vector2D(0 ,1);
    		newAgentPositionLst.add(newDirection); // no block		
			if(!attachedElements.isEmpty()) {
				Iterator<Point> attachedElementsIt = attachedElements.iterator();
				while(attachedElementsIt.hasNext()) {
					Point next = attachedElementsIt.next();
					newAgentPositionLst.add(new Vector2D(next.getLocation().x + newDirection.x, next.getLocation().y + newDirection.y)); 	
				}
			}
    		//newAgentPosition = new Vector2D(0, 1);
    		break;
    	case w:
    		// *******
			// *ODA***
			// *******
    		newDirection = new Vector2D(-1 ,0);
    		newAgentPositionLst.add(newDirection); // no block					
			if(!attachedElements.isEmpty()) {
				Iterator<Point> attachedElementsIt = attachedElements.iterator();
				while(attachedElementsIt.hasNext()) {
					Point next = attachedElementsIt.next();
					newAgentPositionLst.add(new Vector2D(next.getLocation().x + newDirection.x, next.getLocation().y + newDirection.y)); 	
				}
			}
    		//newAgentPosition = new Vector2D(-1, 0);
        	break;
    	}
    	
//		newAgentPositionLstArrayList.add(new Vector2D());
    	Iterator<NextMapTile> it = GetObstacles().iterator();
    	while(it.hasNext())
    	{
    		NextMapTile next = it.next();
    		Vector2D nextPosition = next.getPosition();
    		
    		for (Iterator<Vector2D> iterator = newAgentPositionLst.iterator(); iterator.hasNext();) {
    			Vector2D point = iterator.next();
				if(point.equals(nextPosition)) {
    				result.add(next);
    			}
			}
//    		if(newAgentPositionLstArrayList.equals(nextPosition)) {
//    			return next;
//    		}
    	}
    	return result;
    }
    
}
