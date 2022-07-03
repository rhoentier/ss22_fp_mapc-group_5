package massim.javaagents.agents;

import java.util.HashMap;
import java.util.HashSet;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;

/**
 * Funkctions: Grouping of Agents, handling of common map and higher level reasoning.
 * 
 * Todo: register of agents, joining maps, ...
 * 
 * Done:
 * 
 * @author Alexander Lorenz
 */
public class NextGroup {
    
    /*
     * ########## region fields
     */
    
    private int groupID;
    
    private HashSet<NextAgent> agentSet = new HashSet<>();
    private HashMap<NextAgent, Vector2D> agentPositionMap = new HashMap<>();
    
    private NextMap groupMap = new NextMap(this);
    
    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * @param agent the initial agent
     */
    
    public NextGroup(NextAgent agent, int id){
        this.groupID = id;
        this.agentSet.add(agent);
        this.agentPositionMap.put(agent, new Vector2D (0,0));
    }

    /*
     * ##################### endregion constructor
     */

    /*
     * ########## region public methods
     */
    
    public void addAgent(NextAgent agent) {
        this.agentSet.add(agent);        
    }

    public void removeAgent(NextAgent agent) {
        this.agentSet.remove(agent);
    }
    
    public int countAgents() {
        return this.agentSet.size();
    }

    public int getGroupID() {
        return groupID;
    }
    
    public HashSet<NextMapTile> removePositionsOfKnownAgents(HashSet<NextMapTile> positions) {
        HashSet<NextMapTile> returnSet = new HashSet<>();
        for(NextMapTile agentPosition : positions ) {
            for(NextAgent agent : agentSet) {
                if(!agentPosition.GetPosition().equals(agent.GetPosition())) {
                    returnSet.add(agentPosition);
                }
            }
        }
        return returnSet;
    }
    
    public Vector2D GetAgentPosition (NextAgent agent){
        return agentPositionMap.get(agent);
    }

    public void SetAgentPosition (NextAgent agent, Vector2D position) {
        agentPositionMap.put(agent, position);
    }
    
    public NextMap GetGroupMap() {
        return groupMap;
    }
    
    public void AddGroup(NextGroup newGroup, Vector2D offset){
        
        joinGroupMap ( newGroup.groupMap, offset);
        
        for(NextAgent agentToAdd: newGroup.agentSet){
            this.addAgent(agentToAdd);
            this.agentPositionMap.put(agentToAdd,agentToAdd.GetPosition().getAdded(offset));
            newGroup.removeAgent(agentToAdd);
            NextAgent.RemoveEmptyGroup(newGroup);
            agentToAdd.SetAgentGroup(this);
            agentToAdd.say("NewPosition: " + agentToAdd.GetPosition());
        }
        
    }

    /**
     * Move the position of a single agent of this group
     * @param agent Agent to be moved
     * @param offset move by offset
     */
    public void MoveSingleAgent(NextAgent agent, Vector2D offset) {
        agentPositionMap.get(agent).add(offset);
    }

    /**
     * Move the position of all agents of this group
     * @param offset move by offset
     */
    public void MoveAllAgents(Vector2D offset) {
        for (Vector2D position : agentPositionMap.values()) {
            position.add(offset);
        }
    }

    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */


    private void joinGroupMap ( NextMap newMap, Vector2D offset) {
        this.groupMap = NextMap.JoinMap ( this.groupMap, newMap, offset);
    }
       
    @Override
    public String toString() {
        return "NextGroup{" + "groupID=" + groupID + ", agentCount=" + agentSet.size() + '}';
    }

    /*
     * ##################### endregion private methods
     */

   
}
