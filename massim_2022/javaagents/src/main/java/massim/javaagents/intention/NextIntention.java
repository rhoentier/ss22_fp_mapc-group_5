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
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextTaskPlanner;
import massim.javaagents.percept.NextRole;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class NextIntention {

    private NextAgent nextAgent;
    ArrayList<Action> possibleActions;
    private NextAgentStatus nextAgentStatus;
    
    // TODO Properties
    private int lastSurveyedDistance = 0;
    private String lastDirection = "n";
    private int surveySteps = 0;

    public NextIntention(NextAgent nextAgent) {
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

        //System.out.println("Actions : ------" + possibleActions.toString());

        //Compares each action based on the value
        if (possibleActions != null) {
            for (Action action : possibleActions) {
                if (action != null) {
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
        // ->  exampleRoleChangeAction();
        //Special case: Interaction with an adjacent element.
        for (NextMapTile visibleThing : nextAgentStatus.GetVisibleThings()) {

            Vector2D position = visibleThing.GetPosition();

            // Wenn Dispenser sichtbar && Agent ist neben irgendwas && Agent hat aktiven Task && Block-Typ stimmt
            if (visibleThing.getThingType().contains("dispenser")
                    && NextAgentUtil.NextToUsingLocalView(position, nextAgent) && nextAgent.GetActiveTask() != null
                    && NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask(), visibleThing.getThingType())) {
                // Block für den aktiven Task überhaupt tragbar?
                if (visibleThing.getThingType().contains("dispenser")) {

                    // Wenn Agent noch keinen Block trägt: nehme Block, lösche PathMemory
                    if (nextAgentStatus.GetAttachedElementsAmount() < 1) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request, NextAgentUtil.GetDirection(position)));
                        this.nextAgent.ClearPathMemory();
                    }
                }
            }

            // Wenn Block sichtbar && Agent hat noch keinen Block: aufnehmen, lösche PathMemory
            if (visibleThing.getThingType().contains("block") && this.nextAgent.GetActiveTask() != null && nextAgentStatus.GetAttachedElementsAmount() < 1) {
                System.out.println("Action - Attach");
                possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach, NextAgentUtil.GetDirection(position)));
                this.nextAgent.ClearPathMemory();
            }

            // submit block, if its in the right direction
            // Wenn Agent einen Block trägt && Agent in Goal Zone
            if (nextAgentStatus.GetAttachedElementsAmount() > 0
                   && NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetGoalZones())) 
            {
                if (NextAgentUtil.IsBlockInCorrectPosition(nextAgent)) {
                    possibleActions.add(NextActionWrapper.CreateAction(EActions.submit, new Identifier(nextAgent.GetActiveTask().GetName())));
                } 
                else 
                {
                	if(NextAgentUtil.IsRotationPossible(nextAgent, "cw"))
                	{
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw")));
                	} 
                	else if(NextAgentUtil.IsRotationPossible(nextAgent, "ccw"))
                	{
                		possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier("ccw")));
                	}
                	else
                	{
                		//rotieren nicht moeglich - Clear neben mir
                		Vector2D blockPosition = this.nextAgentStatus.GetAttachedElements().iterator().next();
                		                		
                		if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetVisibleThings(), new Vector2D(1,0))) // e
                		{
                			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + 1),new Identifier("" + 0)));             			
                		}
                		else if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetVisibleThings(), new Vector2D(0,1))) // s
                		{
                			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + 0),new Identifier("" + 1)));             			
                		}
                		else if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetVisibleThings(), new Vector2D(-1,0))) // w
                		{
                			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + -1),new Identifier("" + 0)));             			
                		}
                		else if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetVisibleThings(), new Vector2D(0,-1))) // n
                		{
                			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + 0),new Identifier("" + -1)));             			
                		}
                	}
                }
                this.nextAgent.ClearPathMemory();
            }
        }
    }

    private Action generateDefaultAction() {
        Action nextMove = NextAgentUtil.GenerateRandomMove();
        return nextMove;
    }

    public void GeneratePathMemory() {
        NextSimulationStatus nextSimulationStatus = nextAgent.getSimulationStatus();
        EAgentTask oldTask = nextAgent.GetAgentTask();

        /// >> Hier das auskommentieren, dass die Taskverarbeitung getestet werden kann
        if (nextAgent.GetActiveTask() != null && NextAgentUtil.IsTaskActive(nextAgent, nextSimulationStatus.GetCurrentStep())) {
            this.nextAgent.SetAgentTask(NextPlanWrapper.GenerateNewPlan(this.nextAgent));
        } else {
            resetAfterInactiveTask();

            // dispenser available for task?
            HashSet<NextMapTile> dispatcherLst = nextAgent.GetMap().GetDispensers();
            if (!dispatcherLst.isEmpty()) {
                ArrayList<NextTask> selectedTask = NextAgentUtil.EvaluatePossibleTask(nextSimulationStatus.GetTasksList(),
                        dispatcherLst, nextSimulationStatus.GetCurrentStep());
                //System.out.println(" \n \n \n \n" + dispatcherLst +"\n \n" + selectedTask +" \n \n \n \n");
                this.nextAgent.SetActiveTask(selectedTask != null && selectedTask.size() > 0 ? selectedTask.get(0) : null);
            }
        }

        // Status changed - Clear pathMemory
        if (this.nextAgent.GetAgentTask() != oldTask) {
            this.nextAgent.ClearPathMemory();
            lastSurveyedDistance = 0;
        }

        NextMap map = this.nextAgent.GetMap();

        System.out.println("-------------------------Aktueller Weg: " + this.nextAgent.GetAgentTask().toString());
        // Move to..
        switch (this.nextAgent.GetAgentTask()) {
	        case surveyDispenser:
                survey("dispenser");
	        	break;
	        case surveyGoalZone:
	        	survey("goal");
	        	break;
	        case surveyRoleZone:
	        	survey("role"); // nicht getestet 27.07
	        	break;
            case exploreMap:     
                if (this.nextAgent.GetPathMemory().isEmpty()) {
                    this.nextAgent.SetPathMemory(
                            this.nextAgent.CalculatePath(
                                    new Vector2D(NextAgentUtil.GenerateRandomNumber(21) - 10 + this.nextAgent.GetPosition().x, NextAgentUtil.GenerateRandomNumber(21) - 10 + this.nextAgent.GetPosition().y)
                            )
                    );
                }
                break;
            case goToDispenser:
            	// TODO Zu welchem Dispenser will ich denn? (Warten auf Path)
                // Only new pathMemory, if the current Path is empty
                if (this.nextAgent.GetPathMemory().isEmpty()) {
                    Iterator<NextMapTile> requiredBlockIterator = this.nextAgent.GetActiveTask().GetRequiredBlocks().iterator();

                    Vector2D foundDispenser = NextAgentUtil.GetDispenserFromType(
                            map.GetDispensers(),
                            requiredBlockIterator.next().getThingType()
                    );
                    this.nextAgent.SetPathMemory(this.nextAgent.CalculatePathNextToTarget(foundDispenser));
                    if (this.nextAgent.GetPathMemory().size() == 0) {
                        possibleActions.add(generateDefaultAction()); //fallback
                    }
                }
                break;
            case goToGoalzone:
                if (this.nextAgent.GetPathMemory().isEmpty() && map.IsGoalZoneAvailable()) {
                    this.nextAgent.SetPathMemory(
                            this.nextAgent.CalculatePath(
                                    NextAgentUtil.GetNearestZone(this.nextAgent.GetPosition(), map.GetGoalZones())
                            )
                    );
                    if (this.nextAgent.GetPathMemory().size() == 0) {
                        possibleActions.add(generateDefaultAction()); //fallback
                    }
                }
                break;
            case goToRolezone:
                if (this.nextAgent.GetPathMemory().isEmpty() && map.IsRoleZoneAvailable()) {
                    this.nextAgent.SetPathMemory(
                            this.nextAgent.CalculatePath(
                                    NextAgentUtil.GetNearestZone(this.nextAgent.GetPosition(), map.GetRoleZones())
                            )
                    );
                    if (this.nextAgent.GetPathMemory().size() == 0) {
                        possibleActions.add(generateDefaultAction()); //fallback
                    }
                }
                break;
            default:
                break;
        }
    }
    
    private void survey(String type)
    {
    	if (this.nextAgent.GetPathMemory().isEmpty()) {
        	if(this.nextAgentStatus.IsLastSpecificActionSucess(type, "survey"))
        	{
        		if(this.nextAgentStatus.GetSurveyedThings().iterator().hasNext())
        		{
        			int distance = this.nextAgentStatus.GetSurveyedThings().iterator().next().GetDistance();
        			if(surveySteps == 0)
        			{
        				//erstes Abtasten
        				possibleActions.add(NextAgentUtil.GenerateNorthMove());
        				lastDirection = "n";	
	        			surveySteps++;
        			}
        			else if(surveySteps == 1)
        			{
        				// north or south
        				if(distance > lastSurveyedDistance)
	        			{
	        				lastDirection = "s";
	        			}
        				possibleActions.add(NextAgentUtil.GenerateEastMove());	
	        			surveySteps++;	        				
        			} else if(surveySteps == 2)
        			{
        				// north or south
        				if(distance < lastSurveyedDistance)
	        			{
	        				lastDirection += "e";
	        			}
        				else {
        					lastDirection += "w";
        				}
        				// Weg in Richtung des Dispensers
    	                this.nextAgent.SetPathMemory(
    	                		this.nextAgent.CalculatePath(NextAgentUtil.RandomPointInDirection(lastDirection, this.nextAgent.GetPosition(), distance))
    	                );
	        			surveySteps = 0;
        			}
        			lastSurveyedDistance = distance;
        		}
        	}
        	else 
        	{            		
        		possibleActions.add(NextAgentUtil.GenerateSurveyThingAction(type));
        	}
        }
    }

    private void resetAfterInactiveTask() {
        this.nextAgent.SetActiveTask(null);
        if(this.nextAgent.GetAgentTask() != EAgentTask.surveyDispenser && this.nextAgent.GetAgentTask() != EAgentTask.surveyGoalZone)
        {        	
        	possibleActions.clear();
        	this.nextAgent.SetAgentTask(EAgentTask.surveyDispenser);
        }

        // TODO miri: Mehrere Blöcke fallen lassen
        // Erst schauen, ob es gerade einen Task gibt, den ich sonst abgeben könnte
    	if(nextAgentStatus.GetAttachedElementsAmount() > 0)
    	{
    		possibleActions.add(NextActionWrapper.CreateAction(EActions.detach, 
    				NextAgentUtil.GetDirection(new Vector2D(nextAgentStatus.GetAttachedElements().iterator().next().x, nextAgentStatus.GetAttachedElements().iterator().next().y)
    		)));
    	}
    }

    /**
     * Beispiel für Rollenwechsel
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

    public void ClearPossibleActions() {
        possibleActions.clear();
    }
}
