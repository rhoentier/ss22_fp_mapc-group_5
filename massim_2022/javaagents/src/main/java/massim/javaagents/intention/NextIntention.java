package massim.javaagents.intention;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.AgentUtil;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.agents.NextPlanWrapper;
import massim.javaagents.agents.NextSimulationStatus;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.general.NextConstants.EPathFinding;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.PathfindingConfig;
import massim.javaagents.percept.NextTask;

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
            	if(action != null) {
	                if (NextConstants.PriorityMap.get(action.getName()) < NextConstants.PriorityMap.get(nextAction.getName())) {
	                    nextAction = action;
	                }
            	}
            }
        }
        return nextAction;
    }

    public void GeneratePossibleActions() {

        // Localises the distance to the next target:  "dispenser", "goal", "role"
        //possibleActions.add(NextAgentUtil.GenerateSurveyThingAction("dispenser"));

        // Survey a specific field with an agent. Get Name, Role, Energy
        // Attributes x-Position, y-Position relative to the Agent
        //possibleActions.add(NextAgentUtil.GenerateSurveyAgentAction(0, 0));

        //Special case: Interaction with an adjacent element.
        for (NextMapTile visibleThing : nextAgent.getStatus().GetVision()) {

            Point position = visibleThing.getPoint();

            if (NextAgentUtil.NextTo(position, nextAgent.getStatus()) && nextAgent.GetActiveTask() != null ) {

            	// Block für den aktiven Task überhaupt tragbar?
            	if(NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask(), visibleThing.getThingType())) 
            	{
	                if (visibleThing.getThingType().contains("dispenser")) {
	
	                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 1) {
	                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request, NextAgentUtil.GetDirection(position)));
	                        //nextAgent.pathMemory.clear();
	                    }
	                }
	
	                if (visibleThing.getThingType().contains("block") && this.nextAgent.GetActiveTask() != null) {
	                	if (nextAgent.getStatus().GetAttachedElementsAmount() < 1) {
	                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach, NextAgentUtil.GetDirection(position)));
	                    }
	                }
            	}
            }
            // submit block, if its in the right direction
            if(nextAgent.getStatus().GetAttachedElementsAmount() > 0 && visibleThing.getThingType().contains("entity-5")
            		&& NextAgentUtil.IsAgentInGoalZone(nextAgent.getStatus().GetGoalZones()) )
            {
            	if(NextAgentUtil.IsBlockInCorrectPosition(nextAgent)) 
            	{
            		possibleActions.add(NextActionWrapper.CreateAction(EActions.submit, new Identifier(nextAgent.GetActiveTask().GetName())));   
            	} 
            	else 
            	{
            		// TODO miri schlauer rotieren, auch merhere Blöcke - ISSUE
            		possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw")));
            		this.nextAgent.SetPathMemory(new ArrayList<>());
            	}
            }
        }
    }
    
    Action generateDefaultAction()
    {
    	Action nextMove = AgentUtil.GenerateMove(PathfindingConfig.GetAlgorithm());
    	return nextMove;
    }
    
    
    public void GeneratePathMemory()
    {
		NextAgentStatus nextAgentStatus = nextAgent.getStatus();
		NextSimulationStatus nextSimulationStatus = nextAgent.getSimulationStatus();
		EAgentTask oldTask = nextAgent.GetAgentTask();
        
        // Task is active
 		if(nextAgent.GetActiveTask() != null && NextAgentUtil.IsTaskActive(nextAgent.GetActiveTask(), nextSimulationStatus.GetActualStep()))
        {
 			this.nextAgent.SetAgentTask(NextPlanWrapper.GenerateNewPlan(this.nextAgent));
	    } 
	    else 
	    {
	    	// TODO miri: Methode um nach einem inaktiven Task die Dinge zurückzusezen
	    		    	
	    	this.nextAgent.SetActiveTask(null);
	    	possibleActions.clear();
	    	
	    	// dispenser available for task?
	    	if(nextAgentStatus.GetDispenser().size() > 0)
	    	{        	
		   		 ArrayList<NextTask> selectedTask = NextAgentUtil.EvaluatePossibleTask(nextSimulationStatus.GetTasksList(), 
		   				nextAgentStatus.GetDispenser(), nextSimulationStatus.GetActualStep());
		   		 this.nextAgent.SetActiveTask(selectedTask != null && selectedTask.size() > 0 ? selectedTask.get(0) : null);
	    	}
	    	this.nextAgent.SetAgentTask(EAgentTask.exploreMap);
	    	
	    	// TODO miri: Mehrere Blöcke fallen lassen
	    	if(nextAgentStatus.GetAttachedElementsAmount() > 0)
	    	{
	    		possibleActions.add(NextActionWrapper.CreateAction(EActions.detach, 
	    				NextAgentUtil.GetDirection(nextAgentStatus.GetAttachedElements().iterator().next().getLocation())));
	    	}
	    }
 		
        // Status changed - Clear pathMemory
        if(this.nextAgent.GetAgentTask() != oldTask)
        {
        	this.nextAgent.SetPathMemory(new ArrayList<>());
        }
        
        // Move to..
        switch(this.nextAgent.GetAgentTask()) {
	        case exploreMap:
	        	//this.nextAgent.pathMemory = manhattanPath.calculatePath(NextAgentUtil.GenerateRandomNumber(21)-10,NextAgentUtil.GenerateRandomNumber(21)-10);
	        	
	        	possibleActions.add(generateDefaultAction());
	        	break;
	        case goToDispenser:
	        	
	        	/***
	        	 * TODO miri
	        	 *  Hier noch schauen, welchen typ block ich noch nicht hab bzw suchen will. können mehere in einem Task sein
	        	 *  Derzeit nehm ich nur den ersten Task
	        	 */
	        	Iterator<NextMapTile> requiredBlockIterator = this.nextAgent.GetActiveTask().GetRequiredBlocks().iterator();
	        	
	        	Vector2D foundDispenser = NextAgentUtil.GetDispenserFromType(
	        			nextAgentStatus.GetDispenser(), //TODO miri->hier noch von der MAOP holen
	        			requiredBlockIterator.next().getThingType()
	        		);
	        	
	        	// Only new pathMemory, if the current Path is empty
	        	if (this.nextAgent.GetPathMemory().isEmpty()) {
//	        		this.nextAgent.SetPathMemory(this.nextAgent.calculatePath(foundDispenser));
	                this.nextAgent.SetPathMemory(manhattanPath.calculatePath((int)foundDispenser.x, (int)foundDispenser.y));
                    if(this.nextAgent.GetPathMemory().size() == 0) 
                    {
                    	possibleActions.add(generateDefaultAction()); //fallback
                    }
	            }
	            break;
	        case goToEndzone:
	        	// Route zur Endzone
	        	if (this.nextAgent.GetPathMemory().isEmpty()) {
//	        		this.nextAgent.SetPathMemory(
//	        				this.nextAgent.calculatePath(NextAgentUtil.GetNearestGoalZone(nextAgentStatus.GetGoalZones()).getPosition())
//	        		);
	                this.nextAgent.SetPathMemory(NextAgentUtil.GetNearestGoalZone(nextAgentStatus.GetGoalZones()));
                    if(this.nextAgent.GetPathMemory().size() == 0)
                    {
                    	possibleActions.add(generateDefaultAction()); //fallback
                    }
	            }
	        	break;
			default:
				break;
        }
    }
    
    public void ClearPossibleActions()
    {
    	possibleActions.clear();
    }
}
