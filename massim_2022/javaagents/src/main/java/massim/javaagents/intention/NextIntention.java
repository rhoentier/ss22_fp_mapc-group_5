package massim.javaagents.intention;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextPlanWrapper;
import massim.javaagents.agents.NextSimulationStatus;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.general.NextConstants.EPathFinding;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.NextPathfindingUtil;
import massim.javaagents.pathfinding.PathfindingConfig;
import massim.javaagents.percept.NextTask;
import massim.javaagents.percept.NextRole;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class NextIntention {

    private NextAgent nextAgent;
    ArrayList<Action> possibleActions;
    private NextManhattanPath manhattanPath = new NextManhattanPath();
    private NextAgentStatus nextAgentStatus;

    public NextIntention(NextAgent nextAgent){
        this.nextAgent = nextAgent;
        possibleActions = new ArrayList<Action>();
        nextAgentStatus = nextAgent.getAgentStatus();
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
        
        //Example for an Rolechange action
        exampleRoleChangeAction();

        //Special case: Interaction with an adjacent element.
        for (NextMapTile visibleThing : nextAgentStatus.GetVisibleThings()) {

            Vector2D position = visibleThing.getPoint();

            if (NextAgentUtil.NextTo(position, nextAgentStatus) && nextAgent.GetActiveTask() != null ) {

            	// Block für den aktiven Task überhaupt tragbar?
            	if(NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask(), visibleThing.getThingType())) 
            	{
	                if (visibleThing.getThingType().contains("dispenser")) {
	
	                    if (nextAgentStatus.GetAttachedElementsAmount() < 1) {
	                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request, NextAgentUtil.GetDirection(position)));
	                        this.nextAgent.ClearPathMemory();
	                    }
	                }
	
	                if (visibleThing.getThingType().contains("block") && this.nextAgent.GetActiveTask() != null) {
	                	if (nextAgentStatus.GetAttachedElementsAmount() < 1) {
	                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach, NextAgentUtil.GetDirection(position)));
	                        this.nextAgent.ClearPathMemory();
	                	}
	                }
            	}
            }
            // submit block, if its in the right direction
            // original //if(nextAgentStatus.GetAttachedElementsAmount() > 0 && visibleThing.getThingType().contains("entity-5")
            if(nextAgentStatus.GetAttachedElementsAmount() > 0 && visibleThing.getThingType().contains(this.nextAgentStatus.GetTeamName())
                    && NextAgentUtil.IsAgentInGoalZone(nextAgentStatus.GetGoalZones()) )
            {
            	if(NextAgentUtil.IsBlockInCorrectPosition(nextAgent)) 
            	{
            		possibleActions.add(NextActionWrapper.CreateAction(EActions.submit, new Identifier(nextAgent.GetActiveTask().GetName())));   
            	} 
            	else 
            	{
            		// TODO miri schlauer rotieren, auch mehreren Blöcke - ISSUE
//            		if(this.nextAgent.GetMap().IsRotationPossible(new Identifier("cw"), this.nextAgent.GetPosition(), nextAgentStatus.GetAttachedElements()))
//            		{            		
            		if(nextAgentStatus.GetLastAction().contains("rotate") && !nextAgentStatus.GetLastActionResult().contains("success")
            				&& nextAgentStatus.GetLastActionParams().contains("cw"))
    				{
//            			possibleActions.add(
//            				new Action(EActions.clear.toString(), new Identifier("" + visibleThing.getPositionX()),new Identifier("" + visibleThing.getPositionY()+1))
//            			);
                 		possibleActions.add(generateDefaultAction());
    				}
        			possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw")));
//            		}
            	}
            	this.nextAgent.ClearPathMemory();
            }
        }
    }
    
    private Action generateDefaultAction()
    {
    	Action nextMove = NextAgentUtil.GenerateRandomMove();
    	return nextMove;
    }
    
    public void GeneratePathMemory()
    {
    	//evaluateLastStep();
		NextSimulationStatus nextSimulationStatus = nextAgent.getSimulationStatus();
		EAgentTask oldTask = nextAgent.GetAgentTask();

        // Task is active
 		if(nextAgent.GetActiveTask() != null && NextAgentUtil.IsTaskActive(nextAgent, nextSimulationStatus.GetActualStep()))
        {
 			this.nextAgent.SetAgentTask(NextPlanWrapper.GenerateNewPlan(this.nextAgent));
	    } 
	    else 
	    {
	    	resetAfterInactiveTask();
	    	
	    	// dispenser available for task?
	    	HashSet<NextMapTile> dispatcherLst = nextAgent.GetMap().GetDispensers();
	    	if(!dispatcherLst.isEmpty())
	    	{
	    		ArrayList<NextTask> selectedTask = NextAgentUtil.EvaluatePossibleTask(nextSimulationStatus.GetTasksList(), 
	    				dispatcherLst, nextSimulationStatus.GetActualStep());
		   		 this.nextAgent.SetActiveTask(selectedTask != null && selectedTask.size() > 0 ? selectedTask.get(0) : null);
	    	}
	    }
 		
        // Status changed - Clear pathMemory
        if(this.nextAgent.GetAgentTask() != oldTask)
        {
        	this.nextAgent.ClearPathMemory();
        }

    	NextMap map = this.nextAgent.GetMap();
    	
        // Move to..
        switch(this.nextAgent.GetAgentTask()) {
	        case exploreMap:
	        	if(this.nextAgent.GetPathMemory().isEmpty()) {
	        		//this.nextAgent.SetPathMemory(NextPathfindingUtil.GenerateExploreActions());
	        		this.nextAgent.SetPathMemory(
                                        this.nextAgent.CalculatePath(
                                                new Vector2D(NextAgentUtil.GenerateRandomNumber(21)-10 + this.nextAgent.GetPosition().x,NextAgentUtil.GenerateRandomNumber(21)-10 + this.nextAgent.GetPosition().y)
                                        )
	        		
	        		// original Version		// this.nextAgent.CalculatePath(new Vector2D(NextAgentUtil.GenerateRandomNumber(21)-10,NextAgentUtil.GenerateRandomNumber(21)-10))
	        		);
	        	}
	        	break;
	        case goToDispenser:
	        	
	        	/***
	        	 * TODO 
	        	 *  Hier noch schauen, welchen typ block ich noch nicht hab bzw suchen will. können mehere in einem Task sein
	        	 *  Derzeit nehm ich nur den ersten Task
	        	 */	        	
	        	// Only new pathMemory, if the current Path is empty
	        	if (this.nextAgent.GetPathMemory().isEmpty()) {
		        	Iterator<NextMapTile> requiredBlockIterator = this.nextAgent.GetActiveTask().GetRequiredBlocks().iterator();
		        	
		        	Vector2D foundDispenser = NextAgentUtil.GetDispenserFromType(
		        			map.GetDispensers(),
		        			requiredBlockIterator.next().getThingType()
		        		);
                                        System.out.println("\n \n \n \n foundDispenser -> " + foundDispenser);
		        		this.nextAgent.SetPathMemory(this.nextAgent.CalculatePathNextToTarget(foundDispenser));
		                //this.nextAgent.SetPathMemory(manhattanPath.calculatePath((int)foundDispenser.x, (int)foundDispenser.y));
	                    if(this.nextAgent.GetPathMemory().size() == 0) 
	                    {
	                    	possibleActions.add(generateDefaultAction()); //fallback
	                    }
	            }
	            break;
	        case goToEndzone:
	        	// Route zur Endzone
	        	if (this.nextAgent.GetPathMemory().isEmpty() && map.IsGoalZoneAvailable()) {
	        		this.nextAgent.SetPathMemory(
	        				this.nextAgent.CalculatePath(
	        						NextAgentUtil.GetNearestGoalZone(map.GetGoalZones())
	        				)
	        		);
//	                this.nextAgent.SetPathMemory(
//	                		NextAgentUtil.GetNearestGoalZone(map.GetMapTiles("goalZone", this.nextAgent.GetPosition()))
//	                );
                    if(this.nextAgent.GetPathMemory().size() == 0)
                    {
                    	possibleActions.add(generateDefaultAction()); //fallback
                    }
	            }
	        	break;
	        case goToRolezone: //ungetestet
	        	if (this.nextAgent.GetPathMemory().isEmpty() && map.IsRoleZoneAvailable()) {
	        		this.nextAgent.SetPathMemory(
	        				this.nextAgent.CalculatePath(
	        						NextAgentUtil.GetNearestRoleZone(map.GetRoleZones())
	        				)
	        		);
//	                this.nextAgent.SetPathMemory(
//	                		NextAgentUtil.GetNearestRoleZone(map.GetMapTiles("roleZone", this.nextAgent.GetPosition()))
//	                );
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
    
    private void resetAfterInactiveTask() {
    	this.nextAgent.SetActiveTask(null);
    	possibleActions.clear();
    	//this.nextAgent.ClearPathMemory();
    	this.nextAgent.SetAgentTask(EAgentTask.exploreMap);
    	
    	// TODO miri: Mehrere Blöcke fallen lassen
    	// Erst schauen, ob es gerade einen Task gibt, den ich sonst abgeben könnte
//    	if(nextAgentStatus.GetAttachedElementsAmount() > 0)
//    	{
//    		possibleActions.add(NextActionWrapper.CreateAction(EActions.detach, 
//    				NextAgentUtil.GetDirection(nextAgentStatus.GetAttachedElements().iterator().next().getLocation())));
//    	}
	}
    
    /**
     *  Beispiel für Rollenwechsel
     */
    private void exampleRoleChangeAction() {
        NextRole roleToChangeTo = null;
        
        // Initialise a RoleChange if in RoleZone
        if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(this.nextAgentStatus.GetRoleZones())) {
            // desiredActions - have to be filled in Desires processing
            // roleToChangeTo - should be selected in Desires processing
                    HashSet<NextConstants.EActions> desiredActions = new HashSet<>();
                    desiredActions.add(NextConstants.EActions.attach);
                    desiredActions.add(NextConstants.EActions.request);
                    desiredActions.add(NextConstants.EActions.submit);
                    try {
                        roleToChangeTo = NextAgentUtil.FindNextRoleToAdapt(desiredActions, nextAgent.getSimulationStatus().GetRolesList());
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

            if (roleToChangeTo != null) {
                if (!roleToChangeTo.GetName().equals(nextAgentStatus.GetRole())) {
                    possibleActions.add(NextAgentUtil.GenerateRoleChangeAction(roleToChangeTo));
                }
            }
        }
    }
    
    private Boolean evaluateLastStep() {
		String lastAction = this.nextAgentStatus.GetLastAction(); // e.g. move
		String lastActionParams = this.nextAgentStatus.GetLastActionParams(); // e.g. success, fail..
		String lastActionResult = this.nextAgentStatus.GetLastActionResult(); // e.g. ["n"]
		
		if(!lastActionParams.contains("success") && lastAction.contains("move")) {
			if(this.nextAgentStatus.GetAttachedElementsAmount() > 0)
			{
				// TODO miri Block schlauer drehen, derweil einfach im Uhrzeigersinn
				possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw")));
				
				// Add Old step to PathMemory				
				ArrayList<Action> actions = new ArrayList<Action>();
				switch(lastActionResult)
				{
                case "[n]":
                	actions.add(NextAgentUtil.GenerateNorthMove());
                    break;
                case "[e]":
                	actions.add(NextAgentUtil.GenerateEastMove());
                    break;
                case "[s]":
                	actions.add(NextAgentUtil.GenerateSouthMove());
                    break;
                case "[w]":
                	actions.add(NextAgentUtil.GenerateWestMove());
                    break;
				}
				actions.addAll(this.nextAgent.GetPathMemory());
				this.nextAgent.SetPathMemory(actions);
			}
			return false;
		}
		return true;
	}

	public void ClearPossibleActions()
    {
    	possibleActions.clear();
    }
}
