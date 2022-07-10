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

	// TODO Properties
	private int lastSurveyedDistance = 0;
	private String lastDirection = "n";
	private int surveySteps = 0;

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
			// Dispenser && Agent steht neben einem Ding && Agent hat aktiven Task &&
			// Block-Typ stimmt
			if (visibleThing.getThingType().contains("dispenser") 
					&& this.nextAgent.GetActiveTask() != null
					&& NextAgentUtil.NextToUsingLocalView(position, nextAgent)
					&& !NextAgentUtil.IsAnotherAgentInFrontOfBlock(position, this.nextAgentStatus.GetFullLocalView(), this.nextAgent.GetPosition())
					&& NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask().GetRequiredBlocks(), visibleThing.getThingType())
					&& this.nextAgent.GetAgentTask() == EAgentActivity.goToDispenser) {

				// Wenn Agent noch keinen Block trägt: nehme Block, lösche PathMemory
				if (nextAgentStatus.GetAttachedElementsAmount() < 1) {
					possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request,
							NextAgentUtil.ChangeVector2DToIdentifier(position)));
					this.nextAgent.ClearPathMemory();
				}
			}

			// Block && Aktiver Task && Agent hat freie Slots
			if (visibleThing.getThingType().contains("block") 
					&& this.nextAgent.GetActiveTask() != null
					&& NextAgentUtil.NextToUsingLocalView(position, this.nextAgent)
					&& NextAgentUtil.HasFreeSlots(nextAgentStatus)
					&& !NextAgentUtil.IsAnotherAgentInFrontOfBlock(position, this.nextAgentStatus.GetFullLocalView(), this.nextAgent.GetPosition())
					&& NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask().GetRequiredBlocks(), visibleThing.getThingType())
					&& nextAgent.GetAgentTask() == EAgentActivity.goToDispenser) {
//                System.out.println("Action - Attach");
				possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach,
						NextAgentUtil.ChangeVector2DToIdentifier(position)));
				this.nextAgent.ClearPathMemory();
			}

			// Agent hat Block && ist in GoalZone
			if (nextAgentStatus.GetAttachedElementsAmount() > 0
					&& NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetGoalZones())
					&& nextAgent.GetAgentTask() == EAgentActivity.goToGoalzone) {
				// Block korrekt gedreht
				if (NextAgentUtil.IsBlockInCorrectPosition(nextAgent)) {
					possibleActions.add(NextActionWrapper.CreateAction(EActions.submit,
							new Identifier(nextAgent.GetActiveTask().GetName())));
				} else {
					// Block position besetzt? -> Schritt in die Entgegengesetzte Richtung machen
                	if(nextAgentStatus.GetAttachedElementsAmount() == 1)
                	{
                		Vector2D requiredBlockPosition = nextAgent.GetActiveTask().GetRequiredBlocks().iterator().next().GetPosition();
                		Vector2D blockPosition = nextAgentStatus.GetAttachedElementsVector2D().iterator().next();
                		Vector2D oppositeBlockPosition = NextAgentUtil.GetOppositeVector(blockPosition);
                		if(NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(), requiredBlockPosition))
                		{
                			possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, 
        					new Identifier("" + oppositeBlockPosition.x),
        					new Identifier("" + oppositeBlockPosition.y))
        					);                  			
                		}
                		else 
                		{
        					String direction = NextAgentUtil.RotateInWhichDirection(nextAgentStatus.GetAttachedElementsVector2D(),
        							nextAgent.GetActiveTask().GetRequiredBlocks());
        					// Block nicht korrekt gedreht Prüfen zu rotieren
        					if (NextAgentUtil.IsRotationPossible(nextAgentStatus, direction)) {
        						possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier(direction)));
        					} else if (NextAgentUtil.IsRotationPossible(nextAgentStatus,
        							NextAgentUtil.GetOtherRotation(direction))) {
        						possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate,
        								new Identifier(NextAgentUtil.GetOtherRotation(direction))));
        					} else {
        						
        						// TODO Methode, um zu prüfen wo meine Blöcke sind und wo ich clearen muss
        						Vector2D nextRotateDirection = NextAgentUtil.GetNextToRotateDirection(blockPosition, direction);
        						possibleActions.add(NextActionWrapper.CreateAction(EActions.clear,
        								new Identifier("" + nextRotateDirection.x),
        								new Identifier("" + nextRotateDirection.y)));

        						// Clear nicht erfolgreich, randomstep

        						if (this.nextAgentStatus.GetLastAction().contains("clear")
        								&& this.nextAgentStatus.GetLastActionResult().contains("fail")) {
        							Iterator<Action> posIt = possibleActions.iterator();
        							if (posIt.hasNext()) {
        								possibleActions.add(NextAgentUtil.GenerateMoveWithDirection(posIt.next().toString()));
        							} else {
        								possibleActions.add(generateDefaultAction());
        							}
        						}
        					}
                		}
                	}
                	else {
                		// mehrere Blöcke
                	}					

				}
				this.nextAgent.ClearPathMemory();
			}
		}
	}

	private void detachUnusedBlocks() {
		NextTask nextTask = nextAgent.GetActiveTask();
		// Blöcke loswerden, die nicht zu meinem aktuellen Task passen
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

//        System.out.println("-------------------------Aktueller Weg: " + plan.GetAgentTask());
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
						if (distance < lastSurveyedDistance) {
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
