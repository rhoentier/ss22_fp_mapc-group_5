package massim.javaagents.intention;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.AgentUtil;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.agents.NextSimulationStatus;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.general.NextConstants.EPathFinding;
import massim.javaagents.general.NextConstants.EAgentTask;
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
        possibleActions.clear();

        NextSimulationStatus agentSimStatus = this.nextAgent.getSimulationStatus();
        NextAgentStatus agentStatus = this.nextAgent.getStatus();
        EAgentTask oldState = this.nextAgent.GetAgentTask();
        
        if(this.nextAgent.GetActiveTask() != null
        		&& NextAgentUtil.IsTaskActive(nextAgent))
//        		 && this.nextAgent.GetActiveTask().GetDeadline() < this.nextAgent.getSimulationStatus().GetDeadline())
        {
        	// hab ich schon einen Block?
            if(agentStatus.GetAttachedElementsAmount() > 0) // Block vorhanden
            {
            	// Kenn ich schon die Endzone?
            	if(!agentStatus.GetGoalZones().isEmpty()) // Kenne die Endozone
            	{
            		this.nextAgent.SetAgentTask(EAgentTask.goToEndzone);
            	} 
            	else // Kenne Endzone nicht
            	{
            		this.nextAgent.SetAgentTask(EAgentTask.exploreMap);
            	}
            } 
            else 
            {
            	// Kenn ich einen Dispenser vom aktuellen Tasktyp
            	if(!agentStatus.GetDispenser().isEmpty())
            	{
            		this.nextAgent.SetAgentTask(EAgentTask.goToDispenser);
            	} else 
            	{
            		this.nextAgent.SetAgentTask(EAgentTask.exploreMap);
            	}
            }
        } 
        else 
        {
        	this.nextAgent.SetActiveTask(null);
        	if(agentStatus.GetDispenser().size() > 0)
        	{        	
       		 ArrayList<NextTask> selectedTask = NextAgentUtil.EvaluatePossibleTask(agentSimStatus.GetTasksList(), 
       			agentStatus.GetDispenser(), agentSimStatus.GetActualStep());
       		 this.nextAgent.SetActiveTask(selectedTask != null && selectedTask.size() > 0 ? selectedTask.get(0) : null);
        	}
        	this.nextAgent.SetAgentTask(EAgentTask.exploreMap);
        	possibleActions.clear();
        	agentStatus.DropAttachedElements();
        }
        
        // Status changed
        if(this.nextAgent.GetAgentTask() != oldState)
        {
        	nextAgent.pathMemory.clear();
        }
        
        // Move to..
        switch(this.nextAgent.GetAgentTask()) {
        case exploreMap:
        	//this.nextAgent.pathMemory = manhattanPath.calculatePath(NextAgentUtil.GenerateRandomNumber(21)-10,NextAgentUtil.GenerateRandomNumber(21)-10);
        	
        	possibleActions.add(generateDefaultAction());
        	break;
        case goToDispenser:
        	// Route dahin und dahin laufen
        	Iterator<NextMapTile> tmp = this.nextAgent.GetActiveTask().GetRequiredBlocks().iterator();
        	
        	// TODO miri: Hier noch schauen, welchen typ block ich noch nicht hab bzw suchen will. können mehere in einem Task sein
        	
        	Vector2D foundDispenser = NextAgentUtil.GetDispenserFromType(agentStatus.GetDispenser(), 
        			tmp.next().getThingType());
        	
        	if (this.nextAgent.pathMemory.isEmpty()) {
                try {this.nextAgent.pathMemory = manhattanPath.calculatePath((int)foundDispenser.x, (int)foundDispenser.y);
                    if(this.nextAgent.pathMemory.size() == 0)
                    {
                    	possibleActions.add(generateDefaultAction());
                    }
                } catch (Exception e) {
                    //this.nextAgent.say("Path generation failed: " + e);
                }
            }
            break;
        case goToEndzone:
        	// Route zur Endzone
        	if (this.nextAgent.pathMemory.isEmpty()) {
                try {this.nextAgent.pathMemory = NextAgentUtil.GetNearestGoalZone(agentStatus.GetGoalZones());
                    if(this.nextAgent.pathMemory.size() == 0)
                    {
                    	possibleActions.add(generateDefaultAction());
                    }
                } catch (Exception e) {
                    //this.nextAgent.say("Path generation failed: " + e);
                }
            }
        	break;
		default:
			break;
        }

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

                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 1) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request, NextAgentUtil.GetDirection(position)));
                        nextAgent.pathMemory.clear();
                    }
                }

                if (visibleThing.getThingType().contains("block") && this.nextAgent.GetActiveTask() != null) {
                	// Nur einen Block zum passenden Task aufnehmen     
                	//if(NextAgentUtil.IsCorrectBlockType(this.nextAgent.GetActiveTask(), visibleThing.getThingType())) {
	                	if (nextAgent.getStatus().GetAttachedElementsAmount() < 1) {
	                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach, NextAgentUtil.GetDirection(position)));
	                    }
                	//}
                }
            }
            // Block abgeben
            if(nextAgent.getStatus().GetAttachedElementsAmount() > 0 && visibleThing.getThingType().contains("entity-5")
            		&& NextAgentUtil.AgentInGoalZone(agentStatus.GetGoalZones()) )
            {
            	// Block korrekt gedreht?
            	if(NextAgentUtil.IsBlockInCorrectPosition(nextAgent)) 
            	{
            		possibleActions.add(NextActionWrapper.CreateAction(EActions.submit, new Identifier(nextAgent.GetActiveTask().GetName())));   
            	} else 
            	{
            		possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw")));
            		this.nextAgent.pathMemory.clear();
            	}
            }
        }
    }
    
    Action generateDefaultAction()
    {
    	Action nextMove = AgentUtil.GenerateMove(PathfindingConfig.GetAlgorithm());
    	return nextMove;
    }
}
