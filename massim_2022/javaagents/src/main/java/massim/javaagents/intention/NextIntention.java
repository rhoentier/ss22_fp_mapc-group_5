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
import massim.javaagents.general.NextConstants.EAgentActivity;
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
import massim.javaagents.plans.NextPlanDispenser;
import massim.javaagents.plans.NextTaskPlanner;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextSurveyedThing;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class NextIntention {

	private NextAgent nextAgent;
	ArrayList<Action> possibleActions;
	private NextAgentStatus nextAgentStatus;

	private int lastSurveyedDistance = 0;
	private String lastDirection = "n";
	private int surveySteps = 0;
	private int surveyOutOfSteps = 0;

	public NextIntention(NextAgent nextAgent) {
		this.nextAgent = nextAgent;
		possibleActions = new ArrayList<Action>();
		nextAgentStatus = nextAgent.GetAgentStatus();
	}

	/**
	 * Selects the next Action based on the priorityMap
	 *
	 * @return Action
	 */
	public Action SelectNextAction() {

		Action nextAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);

		// System.out.println("Actions : ------" + possibleActions.toString());

		// Compares each action based on the value
		if (possibleActions != null) {
			for (Action action : possibleActions) {
				if (action != null) {
					if (NextConstants.PriorityMap.get(action.getName()) < NextConstants.PriorityMap
							.get(nextAction.getName())) {
						nextAction = action;
					}
				}
			}
		}
		return nextAction;
	}

	public void GeneratePossibleActions() {

		// Special case: Interaction with an adjacent element.

		// TODO Rollenwechsel implementieren
		// Wenn Agent in einer RoleZone und noch nicht worker ist
		if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetRoleZones())
				&& !nextAgent.GetAgentStatus().GetRole().equals("worker")) {
			possibleActions.add(NextAgentUtil.GenerateRoleChangeAction("worker"));
			nextAgent.ClearPathMemory();
		}
		
		detachUnusedBlocks();
		
		// Aktuelle Sicht des Agenten
		for (NextMapTile visibleThing : nextAgentStatus.GetVisibleThings()) {

			Vector2D position = visibleThing.GetPosition();

			// Nur wenn Task aktiv ist, sind Dispenser und Blocks relevant
			// Blocktyp passt und goToDispenser
			if(this.nextAgent.GetActiveTask() != null
				&& NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask().GetRequiredBlocks(), visibleThing.getThingType())
				&& NextAgentUtil.HasFreeSlots(nextAgentStatus)
				&& this.nextAgent.GetAgentTask() == EAgentActivity.goToDispenser
			)
			{				
				// Stehe direkt nebenan && Blocktyp passt
				// TODO hier die kleinere ID nehmen?
				if(NextAgentUtil.NextToUsingLocalView(position, nextAgent)
					&& !NextAgentUtil.IsAnotherAgentInNearOfBlock(position, this.nextAgentStatus.GetFullLocalView())
				) 
				{
					if(NextAgentUtil.HasFreeSlots(nextAgentStatus))
						{
						// ********************** Dispenser					
						if (visibleThing.IsDispenser())
						{
							// -- ("Action - Request");
							possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request,
									NextAgentUtil.ChangeVector2DToIdentifier(position)));
							this.nextAgent.ClearPathMemory();
						}
	
						// ********************** Block
						if (visibleThing.IsBlock())
						{
							// -- ("Action - Attach");
							possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach,
									NextAgentUtil.ChangeVector2DToIdentifier(position)));
							this.nextAgent.ClearPathMemory();
						}
					}
				}
			}

			// ********************** GoalZone (1Block)
			if(this.nextAgent.GetAgentTask() == EAgentActivity.goToGoalzone
				&& nextAgentStatus.GetAttachedElementsAmount() > 0
				&& NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetGoalZones())
			)
			{
				if (NextAgentUtil.IsBlockInCorrectPosition(nextAgent)) 
				{
					// Korrekte Blockposition
					// -- ("Action - Submit");
					possibleActions.add(NextActionWrapper.CreateAction(EActions.submit,
							new Identifier(nextAgent.GetActiveTask().GetName())));
				} 
				else
				{
					// Blockposition falsch
					Vector2D requiredBlockPosition = nextAgent.GetActiveTask().GetRequiredBlocks().iterator().next().GetPosition();
            		Vector2D blockPosition = nextAgentStatus.GetAttachedElementsVector2D().iterator().next();
            		Vector2D oppositeBlockPosition = NextAgentUtil.GetOppositeVector(blockPosition);
            		String direction = NextAgentUtil.RotateInWhichDirection(nextAgentStatus.GetAttachedElementsVector2D(),
							nextAgent.GetActiveTask().GetRequiredBlocks());
            		
            		if(NextAgentUtil.IsRotationPossible(nextAgentStatus, direction))
            		{
            			// -- ("Action - Rotate");
            			possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier(direction)));
            			break;
            		}
            		
        			// Clear in BlockPosition moeglich
            		if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(), requiredBlockPosition))
            		{
            			// -- ("Action - Clear");
            			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, 
            					new Identifier("" + requiredBlockPosition.x),
            					new Identifier("" + requiredBlockPosition.y))
            			);  
            			break;
            		}
            		
            		// Step nach oben moeglich
            		if(NextAgentUtil.IsNextStepPossible(ECardinals.n, this.nextAgentStatus.GetAttachedElementsVector2D(), this.nextAgentStatus.GetObstacles()))
            		{
            			// -- ("Action - Move n");
            			possibleActions.add(NextAgentUtil.GenerateNorthMove());
            			break;
            		}
            		
            		// Clear nach oben m??glich?
            		if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(), new Vector2D(0, -1)))
            		{
            			// -- ("Action - Clear");
            			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, 
            					new Identifier("" + 0),
            					new Identifier("" + -1))
            			);  
            			break;
            		}
            		
            		// Move to other side
            		ECardinals opposideCardinal = NextAgentUtil.ConvertVector2DToECardinals(oppositeBlockPosition);
            		if(NextAgentUtil.IsNextStepPossible(opposideCardinal, this.nextAgentStatus.GetAttachedElementsVector2D(), this.nextAgentStatus.GetObstacles()))
            		{
            			possibleActions.add(NextActionWrapper.CreateAction(EActions.move, new Identifier(opposideCardinal.toString())));
            			break;
            		}
            		
            		// Clear the other side
            		if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(), oppositeBlockPosition))
            		{
            			// -- ("Action - Clear");
            			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, 
            					new Identifier("" + oppositeBlockPosition.x),
            					new Identifier("" + oppositeBlockPosition.y))
            			);  
            			break;
            		}
            		
            		// Fallback
            		possibleActions.add(generateDefaultAction());
				}
				this.nextAgent.ClearPathMemory();
			}
		}
	}

	private void detachUnusedBlocks() {
		NextTask nextTask = nextAgent.GetActiveTask();
		// Bl??cke loswerden, die nicht zu meinem aktuellen Task passen
		if(nextTask != null && nextAgentStatus.GetAttachedElementsAmount() > 0)
    	{
			for(NextMapTile attachedElement : nextAgentStatus.GetAttachedElementsNextMapTiles())
			{
				for(NextMapTile block : nextTask.GetRequiredBlocks())
				{
					if(!attachedElement.getThingType().contains(block.getThingType()))
    				{
    					possibleActions.add(NextActionWrapper.CreateAction(EActions.detach, new Identifier(NextAgentUtil.ConvertVector2DToECardinals(attachedElement.GetPosition()).toString())));
    					this.nextAgent.ClearPathMemory();
    					break;
    				} 
				}
			}
    	}
	}

	private Action generateDefaultAction() {
		Action nextMove = NextAgentUtil.GenerateRandomMove();
		return nextMove;
	}

	public void GeneratePathMemory() {
		NextPlan plan = nextAgent.GetAgentPlan();
		NextMap map = this.nextAgent.GetMap();

		nextAgent.SetAgentTask(plan.GetAgentTask());

		// Survey failed 2 times -> Randomstep
		if(surveyOutOfSteps == 2)
		{
			possibleActions.add(generateDefaultAction()); // fallback
			surveyOutOfSteps = 0;
		}
		
		System.out.println("---------------------- " + plan.GetAgentTask().toString());
		
		// Move to..
		switch (plan.GetAgentTask()) {
		case surveyDispenser:
			survey("dispenser");
			break;
		case surveyGoalZone:
			survey("goal");
			break;
		case surveyRoleZone:
			survey("role");
			break;
		case goToDispenser:
			// Only new pathMemory, if the current Path is empty
			if (this.nextAgent.GetPathMemory().isEmpty()) {
				Vector2D foundDispenser = NextAgentUtil.GetDispenserFromType(map.GetDispensers(),
						((NextPlanDispenser) plan).GetDispenser().getThingType());
				this.nextAgent.SetPathMemory(this.nextAgent.CalculatePathNextToTarget(foundDispenser));
				if (this.nextAgent.GetPathMemory().size() == 0) {
					possibleActions.add(generateDefaultAction()); // fallback
				}
			}
			break;
		case goToGoalzone:
			if (this.nextAgent.GetPathMemory().isEmpty() && map.IsGoalZoneAvailable()) {
				this.nextAgent.SetPathMemory(this.nextAgent
						.CalculatePath(NextAgentUtil.GetNearestZone(this.nextAgent.GetPosition(), map.GetGoalZones())));
				if (this.nextAgent.GetPathMemory().size() == 0) {
					possibleActions.add(generateDefaultAction()); // fallback
				}
			}
			break;
		case goToRolezone:
			if (this.nextAgent.GetPathMemory().isEmpty() && map.IsRoleZoneAvailable()) {
				this.nextAgent.SetPathMemory(this.nextAgent
						.CalculatePath(NextAgentUtil.GetNearestZone(this.nextAgent.GetPosition(), map.GetRoleZones())));
				if (this.nextAgent.GetPathMemory().size() == 0) {
					possibleActions.add(generateDefaultAction()); // fallback
				}
			}
			break;
		default:
			break;
		}
	}

	private void survey(String type) {
		if (this.nextAgent.GetPathMemory().isEmpty()) {
			if (this.nextAgentStatus.IsLastSpecificActionSuccess(type, "survey")) {
				for(NextSurveyedThing nextSurveyedThings: this.nextAgentStatus.GetSurveyedThings())
				{
					int distance = nextSurveyedThings.GetDistance();
					if (surveySteps == 0) {
						// erstes Abtasten
						possibleActions.add(NextAgentUtil.GenerateNorthMove());
						lastDirection = "n";
						surveySteps++;
					} else if (surveySteps == 1) {
						// north or south
						if (distance > lastSurveyedDistance) {
							lastDirection = "s";
						}
						possibleActions.add(NextAgentUtil.GenerateEastMove());
						surveySteps++;
					} else if (surveySteps == 2) {
						// north or south
						if (distance > lastSurveyedDistance) {
							lastDirection += "e";
						} else {
							lastDirection += "w";
						}
						// Weg in Richtung des Dispensers
						this.nextAgent.SetPathMemory(this.nextAgent.CalculatePath(NextAgentUtil
								.RandomPointInDirection(lastDirection, this.nextAgent.GetPosition(), distance)));
						surveySteps = 0;
					}
					lastSurveyedDistance = distance;
				}
			} else {
				possibleActions.add(NextAgentUtil.GenerateSurveyThingAction(type));
			}
		}
	}

	public void ResetAfterTaskChange(NextTask newTask) {
		possibleActions.clear();
		lastSurveyedDistance = 0;
		nextAgent.ClearPathMemory();
	}

	/**
	 * Beispiel f??r Rollenwechsel
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
				roleToChangeTo = NextAgentUtil.FindNextRoleToAdapt(desiredActions,
						nextAgent.GetSimulationStatus().GetRolesList());
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			if (roleToChangeTo != null) {
				if (!roleToChangeTo.GetName().equals(nextAgentStatus.GetRole())) {
					possibleActions.add(NextAgentUtil.GenerateRoleChangeAction(roleToChangeTo.GetName()));
				}
			}
		}
	}

	public void ClearPossibleActions() {
		possibleActions.clear();
	}
}
