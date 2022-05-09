package massim.javaagents.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import massim.javaagents.general.Constants;

/**
 *
 * @author AVL
 */
public class AgentStatus {

    private String name;
    private String teamName;
    private String lastAction;
    private String lastActionResult;
    private String lastActionParams;

    private int energy;
    private boolean deactivated;
    private String role;

    private HashSet<Point> attachedElements;

    private HashSet<MapTile> vision;

    public AgentStatus() {
        name = null;
        teamName = null;
        lastAction = null;
        lastActionResult = null;
        lastActionParams = null;
        energy = -1;
        deactivated = false;
        role = null;
        attachedElements = new HashSet<>();
    };
    
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

    // compare attached elements to Constants directions and convert to array ?
    public void SetAttachedElements(HashSet<Point> attachedElements) {
        this.attachedElements = new HashSet();
        for (Point attached : attachedElements) {
            if (attached.equals(Constants.WestPoint)
                    || attached.equals(Constants.NorthPoint)
                    || attached.equals(Constants.EastPoint)
                    || attached.equals(Constants.SouthPoint)) {
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

    public void SetVision(HashSet<MapTile> visionElements) {
        this.vision = visionElements;
    }

    public HashSet<MapTile> GetVision() {
        return this.vision;
    }

    public void SetGoalZones(HashSet<MapTile> goalZones){
    }
            
    public void SetRoleZones(HashSet<MapTile> roleZones){
        
    }  
    
    public void SetHits(HashSet<MapTile> Hits){
        
    }
    
    public void SetObstacles(HashSet<MapTile> Obstacles) {
        
    }
    
    @Override
    public String toString() {

        return "[ " + this.name + " ] \n \n Attached: \n" + attachedElements + " \n \n"
                + "vision: \n" + this.vision + " \n  --------------------------------- \n";

    }
}
