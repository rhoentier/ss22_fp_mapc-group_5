package massim.javaagents.agents;

import java.util.HashMap;
import java.util.HashSet;

import massim.javaagents.groupPlans.NextGroupTaskPlanner;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;

/**
 * Funkctions: Grouping of Agents, handling of common map and higher level
 * reasoning.
 * <p>
 * Todo: register of agents, joining maps, ...
 * <p>
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

    private NextGroupTaskPlanner taskPlanner;

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * @param agent the initial agent
     */
    public NextGroup(NextAgent agent, int id) {
        this.groupID = id;
        this.agentSet.add(agent);
        this.agentPositionMap.put(agent, new Vector2D(0, 0));

        this.taskPlanner = new NextGroupTaskPlanner(this);
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

    public HashSet<NextMapTile> removePositionsOfKnownAgents(Vector2D centerPosition, HashSet<NextMapTile> positions) {
        HashSet<NextMapTile> returnSet = new HashSet<>();
        for (NextMapTile agentPosition : positions) {
            for (NextAgent agent : agentSet) {
                if (!agentPosition.GetPosition().clone().getAdded(centerPosition).equals(agent.GetPosition())) {
                    returnSet.add(agentPosition);
                }
            }
        }
        return returnSet;
    }

    public Vector2D GetAgentPosition(NextAgent agent) {
        return agentPositionMap.get(agent);
    }

    public void SetAgentPosition(NextAgent agent, Vector2D position) {
        agentPositionMap.put(agent, position);
    }

    public NextMap GetGroupMap() {
        return groupMap;
    }

    public void AddGroup(NextGroup newGroup, Vector2D offset) {

        System.out.println("MAP to Keep ______________________________________ \n" + NextMap.MapToStringBuilder(GetGroupMap().GetMapArray()));

        for (NextAgent agent : this.agentSet) {
            agent.say(agent.GetPosition().toString());
        }

        System.out.println("MAP to Join______________________________________ \n" + NextMap.MapToStringBuilder(newGroup.GetGroupMap().GetMapArray()));

        for (NextAgent agent : newGroup.agentSet) {
            agent.say(agent.GetPosition().toString());
        }

        for (NextAgent agentToAdd : newGroup.agentSet) {
            this.addAgent(agentToAdd);
            agentToAdd.say("OldPosition: " + agentToAdd.GetPosition());
            agentToAdd.say("Offset: " + offset);
            this.agentPositionMap.put(agentToAdd, agentToAdd.GetPosition().getSubtracted(offset));
            //this.agentPositionMap.put(agentToAdd, agentToAdd.GetPosition().getAdded(offset));
            agentToAdd.SetAgentGroup(this);
            agentToAdd.say("NewPosition: " + agentToAdd.GetPosition());
        }

        joinGroupMap(newGroup.groupMap, offset);

        for (NextAgent agentToAdd : this.agentSet) {
            newGroup.removeAgent(agentToAdd);
        }

        NextAgent.RemoveEmptyGroup(newGroup);

        System.out.println("----------------------------------------- joined ----------------------");
        System.out.println("MAP ______________________________________ \n" + NextMap.MapToStringBuilder(this.GetGroupMap().GetMapArray()));

        for (NextAgent agent : this.agentSet) {
            agent.say(agent.GetPosition().toString());
        }

    }

    /**
     * Move the position of a single agent of this group
     *
     * @param agent  Agent to be moved
     * @param offset move by offset
     */
    public void MoveSingleAgent(NextAgent agent, Vector2D offset) {
        //agentPositionMap.get(agent).add(offset);
        agentPositionMap.put(agent, agentPositionMap.get(agent).getAdded(offset));
    }

    /**
     * Move the position of all agents of this group
     *
     * @param offset move by offset
     */
    public void MoveAllAgents(Vector2D offset) {

        for (NextAgent agent : agentPositionMap.keySet()) {
            //    agentPositionMap.get(agent).add(offset);

            agentPositionMap.put(agent, agentPositionMap.get(agent).getAdded(offset));
        }
    }

    public int GetLastSimulationStep(){
        int latestSimulationStep = -1;
        for (NextAgent agent : agentSet){
            int simulationStep = agent.GetSimulationStatus().GetCurrentStep();
            latestSimulationStep =  simulationStep > latestSimulationStep ? simulationStep: latestSimulationStep;
        }
        return latestSimulationStep;
    }

    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */
    private void joinGroupMap(NextMap newMap, Vector2D offset) {
        return;
        //this.groupMap = NextMap.JoinMap(this.groupMap, newMap, offset);
    }

    @Override
    public String toString() {
        return "NextGroup{" + "groupID=" + groupID + ", agentCount=" + agentSet.size() + '}';
    }

    /*
     * ##################### endregion private methods
     */
}
