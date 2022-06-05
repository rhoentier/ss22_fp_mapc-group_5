package massim.javaagents.intention;

import eis.iilang.Action;
import massim.javaagents.agents.AgentUtil;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.agents.NextSimulationStatus;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.general.NextConstants.EPathFinding;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.PathfindingConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class NextIntention {

    private NextAgent nextAgent;
    ArrayList<Action> possibleActions;
    private NextManhattanPath manhattanPath = new NextManhattanPath();

    public NextIntention(NextAgent nextAgent){
        this.nextAgent = nextAgent;
        possibleActions = new ArrayList<Action>();
    }

    /**
     * Selects the next Action based on the priorityMap
     *
     * @return Action
     */
    public Action SelectNextAction() {

        Action nextAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);

        //Compares each action based on the value
        if (possibleActions != null) {
            for (Action action : possibleActions) {
                if (NextConstants.PriorityMap.get(action.getName()) < NextConstants.PriorityMap.get(nextAction.getName())) {
                    nextAction = action;
                }
            }
        }
        return nextAction;
    }

    public void GeneratePossibleActions() {
        possibleActions.clear();

        NextSimulationStatus agentSimStatus = this.nextAgent.getSimulationStatus();
        NextAgentStatus agentStatus = this.nextAgent.getStatus();
        
        // Evaluate possible Task
        if(this.nextAgent.isActiveTask == null)
        {
        	this.nextAgent.isActiveTask = NextAgentUtil.EvaluatePossibleTask(agentSimStatus.GetTasksList(), 
        			agentStatus.GetDispenser()).get(0); // Ersten Task nehmen
        }
        else 
        {
        	// hab ich schon einen Block?
            if(agentStatus.GetAttachedElementsAmount() > 0) // Block vorhanden
            {
            	// Kenn ich schon die Endzone?
            	if(!agentStatus.GetGoalZones().isEmpty()) // Kenne die Endozone
            	{
            		this.nextAgent.agentTask = EAgentTask.goToEndzone;
            	} 
            	else // Kenne Endzone nicht
            	{
            		this.nextAgent.agentTask = EAgentTask.exploreMap;
            	}
            } 
            else 
            {
            	// Kenn ich einen Dispenser vom aktuellen Tasktyp
            	if(!agentStatus.GetDispenser().isEmpty())
            	{
            		this.nextAgent.agentTask = EAgentTask.goToDispenser;
            	} else 
            	{
            		this.nextAgent.agentTask = EAgentTask.exploreMap;
            	}
            }
        }
        
        // Move to..
        switch(this.nextAgent.agentTask) {
        case exploreMap:
        	Action nextMove = AgentUtil.GenerateMove(PathfindingConfig.GetAlgorithm());
        	if(nextMove != null) 
        	{
        		possibleActions.add(nextMove);
            }
        	break;
        case goToDispenser:
        	// Route dahin und dahin laufen
        	Iterator<NextMapTile> tmp = this.nextAgent.isActiveTask.GetRequiredBlocks().iterator();
        	// TODO miri: Hier noch schauen, welchen typ block ich noch nicht hab bzw suchen will. können mehere in einem Task sein
        	
        	Vector2D foundDispenser = NextAgentUtil.GetDispenserFromType(agentStatus.GetDispenser(), 
        			tmp.next().getThingType());
        	
        	if (this.nextAgent.pathMemory.isEmpty()) {
                try {
                    //this.say(agentStatus.GetPosition().toString());
                    /*
                        try{
                        this.say(" " + agentStatus.getMap().MapToStringBuilder());
                        }catch( Exception e){} finally{}
                     */
                    //this.say(" " + agentStatus.GetSizeOfMap());
                    //pathMemory = aStar.calculatePath(agentStatus.GetMapArray(), agentStatus.GetPosition(), agentStatus.GetPosition().getAdded(2, 4) );
                    this.nextAgent.pathMemory = manhattanPath.calculatePath((int)foundDispenser.x, (int)foundDispenser.y);

                    //this.nextAgent.say(this.nextAgent.pathMemory.toString());
                } catch (Exception e) {
                    //this.nextAgent.say("Path generation failed: " + e);
                }
            }
            break;
        case goToEndzone:
        	// Route zur Endzone
        	if (this.nextAgent.pathMemory.isEmpty()) {
                try {
                    //this.say(agentStatus.GetPosition().toString());
                    /*
                        try{
                        this.say(" " + agentStatus.getMap().MapToStringBuilder());
                        }catch( Exception e){} finally{}
                     */
                    //this.say(" " + agentStatus.GetSizeOfMap());
                    //pathMemory = aStar.calculatePath(agentStatus.GetMapArray(), agentStatus.GetPosition(), agentStatus.GetPosition().getAdded(2, 4) );
                    this.nextAgent.pathMemory = NextAgentUtil.GetNearestGoalZone(agentStatus.GetGoalZones());

                    //this.nextAgent.say(this.nextAgent.pathMemory.toString());
                } catch (Exception e) {
                    //this.nextAgent.say("Path generation failed: " + e);
                }
            }
        	break;
		default:
			break;
        }
        
    	//Action nextMove = AgentUtil.GenerateMove(PathfindingConfig.GetAlgorithm());
//    	if(nextMove != null) 
//    	{
//    		possibleActions.add(nextMove);
//        }
//
        // Localises the distance to the next target:  "dispenser", "goal", "role"
        //possibleActions.add(NextAgentUtil.GenerateSurveyThingAction("dispenser"));

        // Survey a specific field with an agent. Get Name, Role, Energy
        // Attributes x-Position, y-Position relative to the Agent
        //possibleActions.add(NextAgentUtil.GenerateSurveyAgentAction(0, 0));

        //Special case: Interaction with an adjacent element.
        for (NextMapTile visibleThing : nextAgent.getStatus().GetVision()) {

            Point position = visibleThing.getPoint();

            if (NextAgentUtil.NextTo(position, nextAgent.getStatus())) {

                if (visibleThing.getThingType().contains("dispenser")) {

                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 2) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request, NextAgentUtil.GetDirection(position)));
                        nextAgent.pathMemory.clear();
                    }
                }

                if (visibleThing.getThingType().contains("block")) {
                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 2) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach, NextAgentUtil.GetDirection(position)));
                    }
                }
            }
        }
    }
}
