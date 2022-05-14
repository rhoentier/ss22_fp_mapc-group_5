package massim.javaagents.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

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
    
    // Let us discuss the proper target format for surveyed elements -> AVL
    // The conversion should happen in NextPerceptReader
    private HashSet<String[]> surveyedAgents; 
    private HashSet<String[]> surveyedThings;

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
    
    
    public HashSet<String[]> getSurveyedAgents() {
        return surveyedAgents;
    }

    public void setSurveyedAgents(HashSet<String[]> surveyedAgents) {
        this.surveyedAgents = surveyedAgents;
    }

    public HashSet<String[]> getSurveyedThings() {
        return surveyedThings;
    }

    public void setSurveyedThings(HashSet<String[]> surveyedThings) {
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
}
