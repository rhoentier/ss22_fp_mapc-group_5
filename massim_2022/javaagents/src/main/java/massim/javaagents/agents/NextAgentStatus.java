package massim.javaagents.agents;

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
 * @author Alexander Lorenz
 */
public class NextAgentStatus {
    
     /*
     * ########## region fields
     */

    private final NextAgent nextAgent;      // instance of parent agent
    private String name;                    // Agents name
    private String teamName;                // Name of the agents team
    private String lastAction;              // Type of agents action in last step
    private String lastActionResult;        // Result of agents last action
    private String lastActionParams;        // List of parameters for last action 

    private int energy;                     // Agents current energy level
    private boolean deactivated;            // Flag if agent is deactivated
    private String role;                    // Name of the current role
    private NextRole currentRole;           // Full NextRole instance with attributes 
    
    private HashSet<Vector2D> attachedElements;            // Unsorted unfiltered list of elements, attached to the agent
    private HashSet<Vector2D> visibleAttachedElements;     // Unsorted unfiltered list of all visible attached elements
    private HashSet<NextMapTile> attachedElementsNextMapTile;

    private HashSet<NextMapTile> fullLocalView;         // Unsorted list containing visible elements and obstacles as NextMapTile: contains - entity, block, dispenser, marker,...
    private HashSet<NextMapTile> visibleThings;         // Unsorted list of visible elements as NextMapTile: contains - entity, block, dispenser...
    private HashSet<NextMapTile> markers;               // Unsorted list of visible Markers as NextMapTile
    private HashSet<NextMapTile> obstacles;             // Unsorted list of visible obstacles as NextMapTile
    private HashSet<NextMapTile> roleZones;             // Unsorted list of visible zones for rolechange
    private HashSet<NextMapTile> goalZones;             // Unsorted list of visible zones for submitting a task
    private HashSet<NextMapTile> hits;                  // Unsorted list of hit origin - the position where the damage came from (might be off if the agent moved during the previous step)
    
    private HashSet<NextSurveyedAgent> surveyedAgents;  // Unsorted list of NextSurveyedAgent Instances containing information about the last surveyed action
    private HashSet<NextSurveyedThing> surveyedThings;  // Unsorted list of NextSurveyedThing Instances, storing Distance to the targeted elements.

    private HashSet<NextMapTile> dispenser;
    
    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     * 
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
        attachedElementsNextMapTile = new HashSet<>();
        dispenser = new HashSet<>();
        currentRole = new NextRole("dummy", 0, null, null, 0, 0);
    }
    
    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

    /**
     * Retrieve the current role defined by server
     * @return NextRole element
     */
    public NextRole GetCurrentRole() {
        return currentRole;
    }

    /**
     * Sets the current Role
     * 
     * @param currentRole as NextRole element
     */
    public void SetCurrentRole(NextRole currentRole) {
        this.currentRole = currentRole;
    }

    /**
     * Sets the name of the team, as defined by server
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param teamName String value 
     */
    public void SetTeam(String teamName) {
        this.teamName = teamName;
    }

    /**
     * Retrieve the name of the team, as defined by server
     * 
     * @return String name of the team
     */
    public String GetTeamName() {
        return this.teamName;
    }

    /**
     * Sets the type of the last action
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param lastAction String type of action 
     */
    public void SetLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    /**
     * Retrieve the type of the last action
     * @return String type of the action
     */
    public String GetLastAction() {
        return this.lastAction;
    }

    /**
     * Set the the agent to deactivated
     * 
     * @param deactivated Boolean true if disabled
     */
    public void SetDeactivatedFlag(boolean deactivated) {
        this.deactivated = deactivated;
    }

    /**
     * Retrieve the activation status
     * 
     * @return Boolean true if disabled
     */
    public boolean GetDeactivatedFlag() {
        return this.deactivated;
    }

    /**
     * Status of last action, e.g. "failed", "successfull"
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param lastActionResult String type of the status
     */
    public void SetLastActionResult(String lastActionResult) {
        this.lastActionResult = lastActionResult;
    }

    /**
     * Retrieve the status of last action, e.g. "failed", "successfull"
     * 
     * @return String type of the status
     */
    public String GetLastActionResult() {
        return this.lastActionResult;
    }

    /**
     * Set the energy level of the agent
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param energy int energyvalue
     */
    public void SetEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * Retrieve the energy level of the agent
     * 
     * @return int value of the energy
     */
    public int GetEnergy() {
        return this.energy;
    }

    /**
     * Name of current role selected by agent
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param role String name of the current role
     */
    public void SetRole(String role) {
        this.role = role;
    }

    /**
     * Retrieve the current role selected by agent
     * 
     * @return String name of the current role
     */
    public String GetRole() {
        return this.role;
    }

    /**
     * Specifies the name of the entity
     * value is provided by NextPerceptReader, not to be adjusted manually
     * 
     * @param name String specific name of the entity
     */
    public void SetName(String name) {
        this.name = name;
    }

    /**
     * Retrieve the specific name of the entity
     * 
     * @return String specific name of the entity
     */
    public String GetName() {
        return this.name;
    }

    /**
     * Extracts the id from the name
     * 
     * @return int value with Agents ID
     */
    public int GetId() {
        return Integer.parseInt(name.substring(5, name.length()-1));
    }

    /**
     * Sets the parameters provided to the server with the last action call
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param lastActionParams String representation of the last parameters
     */
    public void SetLastActionParams(String lastActionParams) {
        this.lastActionParams = lastActionParams;
    }

    /**
     * Retrieves the parameters provided to the server with the last action call
     * 
     * @return String representation of the last parameters
     */
    public String GetLastActionParams() {
        return this.lastActionParams;
    }

    /**
     * Retrieve positions of all visible attached elements as Vector2D
     * can be carried by other agents, e.g. opponent agents
     * 
     * @return Vector2D HashSet with position values
     */
    public HashSet<Vector2D> GetVisibleAttachedElements() {
        return visibleAttachedElements;
    }

    /**
     * Specifies positions of all visible attached elements as Vector2D
     * value is provided by NextPerceptReader, not to be adjusted manually
     * 
     * @param visibleAttachedElements Vector2D HashSet with position values
     */
    public void SetVisibleAttachedElements(HashSet<Vector2D> visibleAttachedElements) {
        this.visibleAttachedElements = visibleAttachedElements;
        SetAttachedElements(visibleAttachedElements);
    }

    /**
     * Retrieve the attached blocks in agent environment
     * 
     * important: only checks for adjacent position. 
     * Real information about attachement to the agent is not proided by the server.
     * 
     * @return Vector2D with block position
     */
    public HashSet<Vector2D> GetAttachedElementsVector2D() {
        return this.attachedElements;
    }

    /**
     * Retrieve the amount of attached elements
     * important: only checks for adjacent position. 
     * 
     * @return int amount of attached blocks
     */
    public Integer GetAttachedElementsAmount() {
        return this.attachedElements.size();
    }
    
    /**
     * Retrieve the attached elements as nextmaptile
     *  
     * @return NextMapTile HashSet containing attached elements, sorted by type
     */
    public HashSet<NextMapTile> GetAttachedElementsNextMapTiles(){
    	return this.attachedElementsNextMapTile;
    }

    /**
     * Specifies all visible elements without obstacles in local Vision
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param visionElements NextMapTile HashSet containing all visible elements.
     */
    public void SetVision(HashSet<NextMapTile> visionElements) {
        this.visibleThings = visionElements;
    }

    /**
     * Retrieve all visible elements without obstacles in local Vision
     * 
     * @return NextMapTile HashSet containing all visible elements.
     */
    public HashSet<NextMapTile> GetVisibleThings() {
        return this.visibleThings;
    }

    /**
     * Retrieve the marker tiles in local Vision
     * 
     * @return NextMapTile HashSet containing all visible marker.
     */
    public HashSet<NextMapTile> GetMarkers() {
        return markers;
    }

    /**
     * Specifies the markers in local Vision submitted by server
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param markers NextMapTile HashSet containing all visible elements.
     */
    public void SetMarkers(HashSet<NextMapTile> markers) {
        this.markers = markers;
    }

    /**
     * Retrieve the obstacles visible in local vision
     * 
     * @return NextMapTile HashSet containing all visible obstacles.
     */
    public HashSet<NextMapTile> GetObstacles() {
        return obstacles;
    }

    /**
     * Specifies the obstacles visible in local vision
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param obstacles NextMapTile HashSet containing all visible obstacles.
     */
    public void SetObstacles(HashSet<NextMapTile> obstacles) {
        this.obstacles = obstacles;
    }

    /**
     * Retrieve the RoleZone Tiles in local Vision
     * 
     * @return NextMapTile HashSet containing local RoleZones  
    */
    public HashSet<NextMapTile> GetRoleZones() {
        return roleZones;
    }

    /**
     * Specifies the Role Zones visible in local vision
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param roleZones NextMapTile HashSet containing all visible RoleZone tiles.
     */
    public void SetRoleZones(HashSet<NextMapTile> roleZones) {
        this.roleZones = roleZones;
    }

    /**
     * Retrieve the GoalZone Tiles in local Vision
     * 
     * @return NextMapTile HashSet containing local GoalZones
    */
    public HashSet<NextMapTile> GetGoalZones() {
        return goalZones;
    }

    /**
     * 
     * Specifies the GoalZones visible in local vision
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param goalZones NextMapTile HashSet containing local GoalZones.
     */
    public void SetGoalZones(HashSet<NextMapTile> goalZones) {
        this.goalZones = goalZones;
    }

    /**
     * Retrieve the source position for suffered damage
     * @return NextMapTile HashSet containing damage sources
     */
    public HashSet<NextMapTile> GetHits() {
        return hits;
    }

    /**
     * Specifies the  source position for suffered damage
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param hits NextMapTile HashSet containing damage sources
     */
    public void SetHits(HashSet<NextMapTile> hits) {
        this.hits = hits;
    }

    /**
     * Retrieve information about an agent after an surveyed action 
     * 
     * @return NextSurveyedAgent HashSet with supplied information
     */
    public HashSet<NextSurveyedAgent> GetSurveyedAgents() {
        return surveyedAgents;
    }

    /** 
     * Specifies information about an agent after an surveyed action 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param surveyedAgents NextSurveyedAgent HashSet with supplied information 
     */
    public void SetSurveyedAgents(HashSet<NextSurveyedAgent> surveyedAgents) {
        this.surveyedAgents = surveyedAgents;
    }

    /**
     * Specifies information about a surveyed thing (other than an agent) after an surveyed action 
     * @return NextSurveyedThing HashSet with supplied information 
     */
    public HashSet<NextSurveyedThing> GetSurveyedThings() {
        return surveyedThings;
    }

    /**
     * Specifies information about a surveyed thing (other than an agent) after an surveyed action 
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param surveyedThings NextSurveyedThing HashSet with supplied information 
     */
    public void SetSurveyedThings(HashSet<NextSurveyedThing> surveyedThings) {
        this.surveyedThings = surveyedThings;
    }
    
    /**
     * Retrieve the Dispenser Tiles in local Vision
     * 
     * @return NextMapTile HashSet containing local Dispensers
    */
    public HashSet<NextMapTile> GetDispenser() {
        return dispenser;
    }

    /**
     * Specifies the Dispenser Tiles in local Vision
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param dispenser NextMapTile HashSet containing local Dispensers
     */
    public void SetDispenser(HashSet<NextMapTile> dispenser) {
        this.dispenser = dispenser;
    }

    /**
     * Retrieve all visible elements with obstacles in local Vision 
     * 
     * @return NextMapTile with visible things 
     */
    public HashSet<NextMapTile> GetFullLocalView() {
        return fullLocalView;
    }

    /**
     * Specifies all visible elements with obstacles in local Vision
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param fullLocalView NextMapTile HashSet with visible things
     */
    public void SetFullLocalView(HashSet<NextMapTile> fullLocalView) {
        this.fullLocalView = fullLocalView;
    }
    
    /**
     * This implementation returns a small part of the stored values as string
     * 
     * @return String formatted for representation
     */
    @Override
    public String toString() {

        return "[ " + this.name + " ] \n \n Attached: \n" + attachedElements + " \n \n"
                + "visible Things: \n" + this.visibleThings + " \n"
                + "obstacles: \n" + this.obstacles + " \n"
                + "goalZones: \n" + this.goalZones + " \n"
                + "roleZones: \n" + this.roleZones + " \n"
                + "--------------------------------- \n";
    }
    
    /**
     * Check for sucessfull execution of last action
     * 
     * @param lastActionParams e.g. dispenser, goal, role
     * @param lastAction e.g. survey, clear, move
     * @return boolean true if successful
     */
    
    public boolean IsLastSpecificActionSuccess(String lastActionParams, String lastAction)
    {
    	return this.lastActionResult.contains("success") 
    			&& this.lastActionParams.contains(lastActionParams)
    			&& this.lastAction.contains(lastAction);
    }
    
    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */

    /**
     * Convert attachedElements to NextMapTile
     * 
     * @param attachedElements Vector2D HashSet 
     */
    private void SetAttachedElementsNextMapTile(HashSet<Vector2D> attachedElements)
    {
        HashSet<NextMapTile> processedAttachedSet = new HashSet<>();
        
        for(Vector2D position : attachedElements){
            for(NextMapTile tile : visibleThings){
                if(tile.GetPosition().equals(position)){
                    processedAttachedSet.add(tile);
                }
            }
        }
    	this.attachedElementsNextMapTile = processedAttachedSet;
    }
    
    /**
     * SetAttachedElements
     * compare attached elements to NextConstants directions and save in local HashSet
     * 
     * @param attachedElements visibly by the agent
     */
    private void SetAttachedElements(HashSet<Vector2D> attachedElements) {
            this.attachedElements = new HashSet();
            for (Vector2D attached : attachedElements) {
                if (attached.equals(NextConstants.WestPoint)
                        || attached.equals(NextConstants.NorthPoint)
                        || attached.equals(NextConstants.EastPoint)
                        || attached.equals(NextConstants.SouthPoint)) {
                    this.attachedElements.add(attached);
                }
            }
            SetAttachedElementsNextMapTile(attachedElements);
    }
    
    /*
     * ##################### endregion private methods
     */
}
