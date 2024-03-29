package massim.javaagents.agents;

import java.util.HashMap;
import java.util.HashSet;

import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.groupPlans.NextTaskPlanner;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextPathfindingUtil;
import massim.javaagents.percept.NextTask;

/**
 * Grouping of Agents and managing of a common map.
 *
 * @author Alexander Lorenz, Sebastian Loder, Jan Steffen Jendrny
 */
public class NextGroup {

    /*
     * ########## region fields
     */
    
    private final int groupID;                              // Group identification ID
    private int lastStep;                                   // last simulation step processed inside the Group

    // a collection of agents having joined the group
    private final HashSet<NextAgent> agentSet = new HashSet<>();            

    // mapping of current position for each agent
    private final HashMap<NextAgent, Vector2D> agentPositionMap = new HashMap<>(); 

    private NextMap groupMap = new NextMap(this);           // environment map shared by all agents
    private final NextTaskPlanner taskPlanner;              // task developing class

    /*
     * ##################### endregion fields
     */
    
    /**
     * ########## region constructor.
     */
    
    /**
     * General constructor -  to be called inside the NextAgent
     *
     * @param agent NextAgent - the initial agent to populate the group
     * @param id int - group name
     */
    public NextGroup(NextAgent agent, int id) {
        this.groupID = id;
        this.AddAgent(agent);
        this.SetAgentPosition(agent, new Vector2D(0, 0));

        this.lastStep = -1;
        this.taskPlanner = new NextTaskPlanner(this);
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
    final public void AddAgent(NextAgent agent) {
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
     * Count the agents in the group
     *
     * @return int the current amount of agents in the group
     */
    public int CountAgents() {
        return this.agentSet.size();
    }

    /**
     * Retrieve a Set containing all agents forming this group
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
     * @return stripped Set containing only unknown agents
     */
    public HashSet<NextMapTile> RemovePositionsOfKnownAgents(Vector2D centerPosition, HashSet<NextMapTile> positions) {
        HashSet<NextMapTile> removeSet = new HashSet<>();
        for (NextMapTile agentPosition : positions) {

            for (NextAgent agent : this.GetAgents()) {
                if (agentPosition.GetPosition().clone().GetAdded(centerPosition).equals(this.GetAgentPosition(agent))) {
                    removeSet.add(agentPosition);
                }
            }
        }
        positions.removeAll(removeSet);

        return positions;
    }

    /**
     * Retrieve the position of an agent
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
    final public void SetAgentPosition(NextAgent agent, Vector2D position) {
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

        for (NextAgent agentToAdd : newGroup.agentSet) {
            System.out.println(" " + agentToAdd + " " + agentToAdd.GetPosition().GetSubtracted(offset));
            this.agentPositionMap.put(agentToAdd, agentToAdd.GetPosition().GetSubtracted(offset));
            this.AddAgent(agentToAdd);
        }

        joinGroupMap(newGroup.groupMap, offset);

        for (NextAgent agentToAdd : this.agentSet) {
            newGroup.RemoveAgent(agentToAdd);
        }

        NextAgent.RemoveEmptyGroup(newGroup);

    }

    /**
     * Move the position of a single agent of this group
     *
     * @param agent Agent to be moved
     * @param offset move by offset
     */
    
    public void MoveSingleAgent(NextAgent agent, Vector2D offset) {
        agentPositionMap.put(agent, agentPositionMap.get(agent).GetAdded(offset));
    }

    /**
     * Performs a modulus operation on the position of an agent. 
     * 
     * @param agent NextAgent to perform the mod operation on
     */
    public void ModSingleAgent(NextAgent agent) {
        Vector2D pos = new Vector2D(agentPositionMap.get(agent).GetMod(groupMap.GetSimulationMapSize()));
        agentPositionMap.put(agent, pos);
    }

    /**
     * Move the position of all agents of this group
     *
     * @param offset move by offset
     */
    public void MoveAllAgents(Vector2D offset) {

        for (NextAgent agent : agentPositionMap.keySet()) {
            agentPositionMap.put(agent, agentPositionMap.get(agent).GetAdded(offset));
        }
    }

    
    /**
     * Performs a modulus operation on the position of all agents. 
     * 
     * @param mod Vector2D input value
     */
    public void ModAllAgents(Vector2D mod) {
        for (NextAgent agent : agentPositionMap.keySet()) {
            agentPositionMap.put(agent, agentPositionMap.get(agent).GetMod(mod));
        }
    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases. Is called from within the agent
     *
     * @param Message - String based message
     * @param sourceAgent NextAgent sending the message
     *
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
     * @param targetAgent String recieving the message
     * @param sourceAgent NextAgent sending the message
     */
    public void TellGroupAgent(String Message, String targetAgent, NextAgent sourceAgent) {
        for (NextAgent agent : agentSet) {
            if (agent.getName().equals(targetAgent)) {
                agent.HandleGroupMessage(Message, sourceAgent.getName());
            }
        }
    }

    /**
     * Retrieve a Set of positions of all agents in the group
     *
     * @return HashSet - Set of agent´s positions
     */
    public HashSet<Vector2D> GetAgentPositions() {
        HashSet<Vector2D> werte = new HashSet<>();
        werte.addAll(this.agentPositionMap.values());
        return werte;
    }

    /**
     * Updates all tasks and plans for the group.
     * 
     * @param newTasks NextTask HashSet to update
     * @param currentStep int timestamp
     */
    public void UpdateTasks(HashSet<NextTask> newTasks, int currentStep) {
        if (currentStep > lastStep) {
            this.lastStep = currentStep;
            taskPlanner.UpdateTasksAndAgents(newTasks);
        }
    }

    /**
     * Retrieves a plan for an agent.
     * 
     * @param agent NextAgent to retrieve a plan for
     * @return NextAgentPlan
     */
    public NextAgentPlan GetPlan(NextAgent agent) {
        return taskPlanner.GetPlan(agent);
    }

    /**
     * Retrieves the last step stored in the group.
     * 
     * @return int timestamp of the last newest percept
     */
    public int GetLastStep() {
        return lastStep;
    }

    /**
     * Sets the maximal attemps to reached.
     * 
     * @param task NextTask to assign the value to. 
     */
    public void SetMaxAttemptsAreReached(NextTask task) {
        taskPlanner.SetMaxAttemptsAreReached(task);
    }

    /**
     * Checks if the deadline for a task is reached 
     * 
     * @param activeTask NextTask to be checked
     * @return boolean true if reached
     */
    public boolean IsDeadlineReached(NextTask activeTask) {
        return taskPlanner.IsDeadlineReached(activeTask);
    }
    
    /**
     * This implementation returns the GroupID and number of agents as a string
     * 
     * @return String formatted for representation
     */
    @Override
    public String toString() {
        return "NextGroup{" + "groupID=" + groupID + ", agentCount=" + agentSet.size() + '}';
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
     * @return int Distance between the points using Manhattan or A*
     */
    private int calculateDistance(Vector2D startPosition, Vector2D targetPosition) {
        return NextPathfindingUtil.CalculateDistance(this.groupMap, startPosition, targetPosition);
    }
    
    /*
     * ##################### endregion private methods
     */
}
