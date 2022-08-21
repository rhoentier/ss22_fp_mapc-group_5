package massim.javaagents.agents;

import java.util.HashMap;
import java.util.HashSet;

import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.groupPlans.NextTaskPlanner;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;

/**
 * Funkctions: Grouping of Agents, handling of common map and higher level
 * reasoning.
 *
 * Done: registering of agents, joining maps, Group Based Communication
 *
 * @author Alexander, Sebastian
 */
public class NextGroup {

    /*
     * ########## region fields
     */
    private int groupID;
    private int lastStep;

    private HashSet<NextAgent> agentSet = new HashSet<>();
    private HashMap<NextAgent, Vector2D> agentPositionMap = new HashMap<>();

    private NextMap groupMap = new NextMap(this);

    private NextTaskPlanner taskPlanner;

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
        this.lastStep = -1;

        this.taskPlanner = new NextTaskPlanner(this);
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

    public HashSet<NextAgent> GetAgents() {return this.agentSet;}

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

         System.out.println("MAP to Keep ______________________________________ \n" + 
                NextMap.MapToStringBuilder(GetGroupMap().GetMapArray(),GetAgentPositions(),GetGroupMap().GetDispenserPositions()));
        
        for (NextAgent agent : this.agentSet) {
            agent.say(agent.GetPosition().toString());
        }
        
        System.out.println("Dispenser: " + this.groupMap.GetDispensers());
        
        

         System.out.println("MAP to Join______________________________________ \n" + 
                NextMap.MapToStringBuilder(newGroup.GetGroupMap().GetMapArray(),newGroup.GetAgentPositions(),newGroup.GetGroupMap().GetDispenserPositions()));
        
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
        
        System.out.println("Dispenser: " + newGroup.groupMap.GetDispensers());

        joinGroupMap(newGroup.groupMap, offset);

        for (NextAgent agentToAdd : this.agentSet) {
            newGroup.removeAgent(agentToAdd);
        }

        NextAgent.RemoveEmptyGroup(newGroup);

        System.out.println("----------------------------------------- joined ----------------------");
        System.out.println("MAP ______________________________________ \n" + 
                NextMap.MapToStringBuilder(GetGroupMap().GetMapArray(),GetAgentPositions(),GetGroupMap().GetDispenserPositions()));
        
        for (NextAgent agent : this.agentSet) {
            agent.say(agent.GetPosition().toString());
        }

        System.out.println("Dispenser: " + this.groupMap.GetDispensers());
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

    /**
     * String-based communication with groupagents
     * to be extended for further usecases.
     * to be called from agent
     *
     * @param Message - String based message
     */
    public void TellGroup (String Message, NextAgent sourceAgent) {
        for (NextAgent agent : agentSet){
            if(!agent.equals(sourceAgent)){
                agent.HandleGroupMessage(Message, sourceAgent.getName());
            }
        }
    }

    /**
     * String-based communication with groupagents
     * to be extended for further usecases.
     * to be called from agent
     *
     * @param Message - String based message
     */
    public void TellGroupAgent (String Message, String targetAgent, NextAgent sourceAgent) {
        for (NextAgent agent : agentSet){
            if(agent.getName().equals(targetAgent)){
                agent.HandleGroupMessage(Message, sourceAgent.getName());
            }
        }
    }

    public void ModAllAgents(Vector2D mod) {
        for (NextAgent agent : agentPositionMap.keySet()) {
            agentPositionMap.put(agent, agentPositionMap.get(agent).getMod(mod));
        }
    }
    
    public HashSet<Vector2D> GetAgentPositions(){
        HashSet <Vector2D> werte = new HashSet<>();
        werte.addAll(this.agentPositionMap.values());
        return werte;
    }

    /**
     * updates all tasks and plans for the group
     */
    public void UpdateTasks(HashSet<NextTask> newTasks, int currentStep) {
        if(currentStep > lastStep){
            this.lastStep = currentStep;
            taskPlanner.UpdateTasksAndAgents(newTasks);
        }
    }

    public NextAgentPlan GetPlan(NextAgent agent){
        return taskPlanner.GetPlan(agent);
    }

    public int GetLastStep() {
        return lastStep;
    }

    public void SetMaxAttemptsAreReached(NextTask task){
        taskPlanner.SetMaxAttemptsAreReached(task);
    }

    /*
     * ##################### endregion public methods
     */

    // ------------------------------------------------------------------------

    /*
     * ########## region private methods
     */
    private void joinGroupMap(NextMap newMap, Vector2D offset) {
        this.groupMap = NextMap.JoinMap(this.groupMap, newMap, offset);
    }

    @Override
    public String toString() {
        return "NextGroup{" + "groupID=" + groupID + ", agentCount=" + agentSet.size() + '}';
    }

    /*
     * ##################### endregion private methods
     */
}
