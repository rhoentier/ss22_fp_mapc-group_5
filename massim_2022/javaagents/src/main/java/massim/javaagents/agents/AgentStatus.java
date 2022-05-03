package massim.javaagents.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        teamName= null;    
        lastAction= null;
        lastActionResult= null;
        lastActionParams= null;    
        energy = -1;
        deactivated = false;
        role= null;
        attachedElements = new HashSet<>();
    };
    
    public void setTeam( String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setlastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setDeactivatedFlag(boolean deactivated) {
        this.deactivated = deactivated;}

    public boolean getDeactivatedFlag() {
        return this.deactivated;
    }

    public void setLastActionResult(String lastActionResult) {
        this.lastActionResult = lastActionResult;
    }

    public String getLastActionResult() {
        return lastActionResult;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getEnergy() {
        return energy;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLastActionParams(String lastActionParams) {
        this.lastActionParams = lastActionParams;
    }

    
    public String getLastActionParams() {
        return lastActionParams;
    }

    // compare attached elements to Constants directions and convert to array ?
    public void setAttachedElements(HashSet<Point> attachedElements) {
        for(Point attached:attachedElements) {
            this.attachedElements.add(attached);
        }
    }

    public HashSet<Point> getAttachedElements() {
        return this.attachedElements;
    }
    
    public Integer getAttachedElementsAmount() {
        return this.attachedElements.size();
    }

    public void dropAttachedElements() {
        this.attachedElements = new HashSet<>();
    }
    
    public void setVision(HashSet<MapTile> visionElements) {
        this.vision = visionElements;
    }

    public HashSet<MapTile> getVision() {
        return vision;
    }
    
}
