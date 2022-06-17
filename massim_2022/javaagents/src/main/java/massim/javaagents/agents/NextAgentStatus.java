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
 * Agent related status values
 * 
 * @author AVL
 */
public class NextAgentStatus {
    
     /*
     * ########## region fields
     */

    private final NextAgent nextAgent;      //instance of parent agent
    private String name;                    // Agent Name
    private String teamName;                // Name of the agents team
    private String lastAction;              // Type of agents action in last step
    private String lastActionResult;        // Result of agents last action
    private String lastActionParams;        // List of parameters for last action 

    private int energy;                     // Agents current energy level
    private boolean deactivated;            // Flag if agent is deactivated
    private String role;                    // Name of current role
    private NextRole currentRole;           // Full NextRole instance with attributes 
    
    private HashSet<Point> attachedElements;            // Unsorted unfiltered list of elements, attached to the agent
    private HashSet<Point> visibleAttachedElements;     // Unsorted unfiltered list of all visible attached elements

    private HashSet<NextMapTile> visibleThings;         // Unsorted list of visible elements as NextMapTile: contains - entity, block, dispenser, marker,...
    private HashSet<NextMapTile> obstacles;             // Unsorted list of visible obstacles as NextMapTile
    private HashSet<NextMapTile> roleZones;             // Unsorted list of visible zones for rolechange
    private HashSet<NextMapTile> goalZones;             // Unsorted list of visible zones for submitting a task
    private HashSet<NextMapTile> hits;                  // Unsorted list of hit origin - the position where the damage came from (might be off if the agent moved during the previous step)
    
    private HashSet<NextSurveyedAgent> surveyedAgents;  // Unsorted list of NextSurveyedAgent Instances containing information about the last surveyed action
    private HashSet<NextSurveyedThing> surveyedThings;  // Unsorted list of NextSurveyedThing Instances, storing distance to the targeted elements.

    private HashSet<NextMapTile> dispenser;
    private NextRole currentRole;

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     * @param nextAgent - instance of parent agent
     */
    

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
    
     /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

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

    public HashSet<Point> GetVisibleAttachedElements() {
        return visibleAttachedElements;
    }

    public void SetVisibleAttachedElements(HashSet<Point> visibleAttachedElements) {
        this.visibleAttachedElements = visibleAttachedElements;
        SetAttachedElements(visibleAttachedElements);
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

    public HashSet<NextSurveyedThing> GetSurveyedThings() {
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
    
    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */


    /*
     * ##################### endregion private methods
     */
}
