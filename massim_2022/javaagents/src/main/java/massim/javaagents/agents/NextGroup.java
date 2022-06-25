package massim.javaagents.agents;

import java.util.HashSet;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;

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
    
    private NextMap groupMap = new NextMap();
    
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
                if(!agentPosition.getPosition().equals(agent.GetPosition())) {
                    returnSet.add(agentPosition);
                }
            }
        }
        return returnSet;
    }
    
    

    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */


    @Override
    public String toString() {
        return "NextGroup{" + "groupID=" + groupID + ", agentCount=" + agentSet.size() + '}';
    }

    /*
     * ##################### endregion private methods
     */
}
