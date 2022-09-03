package massim.javaagents.agents;

import java.util.HashMap;
import java.util.HashSet;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;

import massim.javaagents.pathfinding.NextPathfindingUtil;

/**
 * Functions: Grouping of Agents, handling of a common map and higher level
 * reasoning.
 *
 * Done: registering of agents, joining maps, Group Based Communication
 *
 * @author Alexander Lorenz, Sebastian Loder
 */
public class NextGroup {

    /*
     * ########## region fields
     */
    private int groupID;

    private HashSet<NextAgent> agentSet = new HashSet<>();  // a collection of agents having joined the group
    private HashMap<NextAgent, Vector2D> agentPositionMap = new HashMap<>(); // maping of current position for each agent

    private NextMap groupMap = new NextMap(this); // environment map shared by all agents


    /*
     * ##################### endregion fields
     */
    /**
     * ########## region constructor.
     */
    /**
     * General constructor - supposed to be called within the Nextagent
     *
     * @param agent NextAgent - the initial agent to populate the group
     * @param id int - groupname
     */
    public NextGroup(NextAgent agent, int id) {
        this.groupID = id;
        this.AddAgent(agent);
        this.SetAgentPosition(agent, new Vector2D(0, 0));
    }

    /*
     * ##################### endregion constructor
     */

 /*
     * ########## region public methods
     */
    /**
     * Add agent to the group
     *
     * @param agent NextAgent to be added
     */
    public void AddAgent(NextAgent agent) {
        this.agentSet.add(agent);
        agent.SetAgentGroup(this);
    }

    /**
     * remove agent from the group
     *
     * @param agent NextAgent to be removed
     */
    public void RemoveAgent(NextAgent agent) {
        this.agentSet.remove(agent);
    }

    /**
     * Add agent to the group
     *
     * @return int the current amount of agents in the group
     */
    public int CountAgents() {
        return this.agentSet.size();
    }

    /**
     * Retrieve a set containing all agents forming this group
     *
     * @return HashSet agents forming the group
     */
    public HashSet<NextAgent> GetAgents() {
        return this.agentSet;
    }

    /**
     * Retrieve the group ID
     *
     * @return int group identificator
     */
    public int GetGroupID() {
        return groupID;
    }

    /**
     * Checks for known agents in the environment of a specific agent
     *
     * @param centerPosition position of the agent sending a request
     * @param positions positions of possible groupagents
     * @return stripped set containing only unknown agents
     */
    public HashSet<NextMapTile> RemovePositionsOfKnownAgents(Vector2D centerPosition, HashSet<NextMapTile> positions) {
        HashSet<NextMapTile> removeSet = new HashSet<>();
        for (NextMapTile agentPosition : positions) {

            for (NextAgent agent : this.GetAgents()) {
                if (agentPosition.GetPosition().clone().getAdded(centerPosition).equals(this.GetAgentPosition(agent))) {
                    removeSet.add(agentPosition);
                }
            }
        }
        positions.removeAll(removeSet);

        return positions;
    }

    /**
     * Retrieve position of an agent
     *
     * @param agent NextAgent agent to retrieve position from
     * @return Vector2D agent's position
     */
    public Vector2D GetAgentPosition(NextAgent agent) {
        if (agentSet.contains(agent)) {
            return agentPositionMap.get(agent);
        }
        return null;
    }

    /**
     * Set the position for a specific agent
     *
     * @param agent NextAgent agent to define position to
     * @param position Vector2D agent´s new position
     */
    public void SetAgentPosition(NextAgent agent, Vector2D position) {
        agentPositionMap.put(agent, position);
    }

    /**
     * Retrieve the common map shared by the group
     *
     * @return NextMap group´s map
     */
    public NextMap GetGroupMap() {
        return groupMap;
    }

    /**
     * Join a second group to the current group, retrieve the agents, positions
     * and map
     *
     * @param newGroup NextGroup - Group to join to current group
     * @param offset Vector2D - offset between position [0,0] of the two groups
     */
    public void AddGroup(NextGroup newGroup, Vector2D offset) {

        /**
         * System.out.println("MAP to Keep
         * ______________________________________ \n" +
         * NextMap.MapToStringBuilder(GetGroupMap().GetMapArray(),
         * GetAgentPositions(), GetGroupMap().GetDispenserPositions()));
         *
         * for (NextAgent agent : this.agentSet) {
         * agent.say(agent.GetPosition().toString()); }
         *
         * System.out.println("Dispenser: " + this.groupMap.GetDispensers());
         *
         * System.out.println("MAP to Join______________________________________
         * \n" +
         * NextMap.MapToStringBuilder(newGroup.GetGroupMap().GetMapArray(),
         * newGroup.GetAgentPositions(),
         * newGroup.GetGroupMap().GetDispenserPositions()));
         *
         *
         * for (NextAgent agent : newGroup.agentSet) {
         * agent.say(agent.GetPosition().toString()); }
         *
         */
        for (NextAgent agentToAdd : newGroup.agentSet) {
            System.out.println(" " + agentToAdd + " " + agentToAdd.GetPosition().getSubtracted(offset));
            this.agentPositionMap.put(agentToAdd, agentToAdd.GetPosition().getSubtracted(offset));
            this.AddAgent(agentToAdd);
        }

        joinGroupMap(newGroup.groupMap, offset);

        for (NextAgent agentToAdd : this.agentSet) {
            newGroup.RemoveAgent(agentToAdd);
        }

        NextAgent.RemoveEmptyGroup(newGroup);

        /*
        System.out.println("----------------------------------------- joined ----------------------");
        System.out.println("MAP ______________________________________ \n"
                + NextMap.MapToStringBuilder(GetGroupMap().GetMapArray(), GetAgentPositions(), GetGroupMap().GetDispenserPositions()));

        for (NextAgent agent : this.agentSet) {
            agent.say(agent.GetPosition().toString());
        }

        System.out.println("Dispenser: " + this.groupMap.GetDispensers());
         */
    }

    /**
     * Move the position of a single agent of this group
     *
     * @param agent Agent to be moved
     * @param offset move by offset
     */
    public void MoveSingleAgent(NextAgent agent, Vector2D offset) {
        //agentPositionMap.get(agent).add(offset);
        agentPositionMap.put(agent, agentPositionMap.get(agent).getAdded(offset));
    }

    public void ModSingleAgent(NextAgent agent) {
        Vector2D pos = new Vector2D(agentPositionMap.get(agent).getMod(groupMap.GetSimulationMapSize()));
        agentPositionMap.put(agent, pos);
    }

    /**
     * Move the position of all agents of this group
     *
     * @param offset move by offset
     */
    public void MoveAllAgents(Vector2D offset) {

        for (NextAgent agent : agentPositionMap.keySet()) {
            agentPositionMap.put(agent, agentPositionMap.get(agent).getAdded(offset));
        }
    }

    public void ModAllAgents(Vector2D mod) {
        for (NextAgent agent : agentPositionMap.keySet()) {
            agentPositionMap.put(agent, agentPositionMap.get(agent).getMod(mod));
        }
    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases. Is called from within the agent
     *
     * @param Message - String based message
     */
    public void TellGroup(String Message, NextAgent sourceAgent) {
        for (NextAgent agent : agentSet) {
            if (!agent.equals(sourceAgent)) {
                agent.HandleGroupMessage(Message, sourceAgent.getName());
            }
        }
    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases. Is called from within the agent
     *
     * @param Message - String based message
     */
    public void TellGroupAgent(String Message, String targetAgent, NextAgent sourceAgent) {
        for (NextAgent agent : agentSet) {
            if (agent.getName().equals(targetAgent)) {
                agent.HandleGroupMessage(Message, sourceAgent.getName());
            }
        }
    }

    /**
     * Retrieve a set of positions of all agents in the group
     *
     * @return HashSet<Vector2D> - Set of agent´s positions
     */
    public HashSet<Vector2D> GetAgentPositions() {
        HashSet<Vector2D> werte = new HashSet<>();
        werte.addAll(this.agentPositionMap.values());
        return werte;
    }

    /*
     * ##################### endregion public methods
     */
    // ------------------------------------------------------------------------
    /*
     * ########## region private methods
     */
    /**
     * Combine two maps and assign to the group map
     *
     * @param newMap NextMap new map to join to current map
     * @param offset Vector2D offset between position [0,0] of the two maps
     */
    private void joinGroupMap(NextMap newMap, Vector2D offset) {
        this.groupMap = NextMap.JoinMap(this.groupMap, newMap, offset);
    }

    /**
     * CalculateDistance between two cells using Manhattan or A*JPS if
     * applicable
     *
     * @param startPosition Vector2D Start of calculation
     * @param targetPosition Vector2D Targetof calculation
     * @return int distance between the points using Manhattan or A*
     */
    private int calculateDistance(Vector2D startPosition, Vector2D targetPosition) {
        return NextPathfindingUtil.calculateDistance(this.groupMap, startPosition, targetPosition);
    }

    @Override
    public String toString() {
        return "NextGroup{" + "groupID=" + groupID + ", agentCount=" + agentSet.size() + '}';
    }

    /*
     * ##################### endregion private methods
     */
}
