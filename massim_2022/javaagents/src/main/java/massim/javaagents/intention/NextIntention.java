package massim.javaagents.intention;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.agents.NextMessage;
import massim.javaagents.agents.NextMessageUtil;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentActivity;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextPlanConnect;
import massim.javaagents.plans.NextPlanDispenser;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextSurveyedThing;
import massim.javaagents.plans.NextPlanRoleZone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class NextIntention {

    private final NextAgent nextAgent;
    private boolean positionHasBeenCorrected = false;
    ArrayList<Action> possibleActions;
    Action nextPossibleAction;
    private final NextAgentStatus nextAgentStatus;

    private int lastSurveyedDistance = 0;
    private String lastDirection = "n";
    private int surveySteps = 0;
    private int surveyOutOfSteps = 0;
    private int countWaitForDispenser = 0;

    private Vector2D lastDetachPosition = new Vector2D(0, 0);

    public NextIntention(NextAgent nextAgent) {
        this.nextAgent = nextAgent;
        possibleActions = new ArrayList<>();
        nextAgentStatus = nextAgent.GetAgentStatus();
        nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);
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
                    if (NextConstants.PriorityMap.get(action.getName()) < NextConstants.PriorityMap.get(
                            nextAction.getName())) {
                        nextAction = action;
                    }
                }
            }
        }
        return nextAction;
    }

    /**
     * Generiert anhand der lokalen Sicht die nächste mögliche Aktion, die Sinnvoll ist
     */
    public Action GeneratePossibleAction() {
        nextPossibleAction = null;
        // ----- Rollenwechsel
        if (nextAgent.IsAgentActivity(EAgentActivity.goToRolezone) && changeRoleAction()) return nextPossibleAction;

        // ----- detach Blocks
        if (nextAgentStatus.GetAttachedElementsAmount() > 0 && detachUnusedBlocksAction()) return nextPossibleAction;

        // ----- clear lastDetachBlock
        if (clearDetachedBlockAction()) return nextPossibleAction;

        // ----- goToDispenser
        if (nextAgent.IsAgentActivity(EAgentActivity.goToDispenser) && nextAgent.GetActiveTask() != null && goToDispenserAction())
            return nextPossibleAction;

        // ----- goToGoalZone
        if (nextAgent.IsAgentActivity(EAgentActivity.goToGoalzone) && goToGoalZoneAction()) return nextPossibleAction;

        // ----- connectToAgent
        if (nextAgent.IsAgentActivity(EAgentActivity.connectToAgent) && connectToAgentAction())
            return nextPossibleAction;

        return nextPossibleAction;
    }

    private boolean changeRoleAction() {
        // in RoleZone
        if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetRoleZones())
        ) {
            nextPossibleAction = NextAgentUtil.GenerateRoleChangeAction(((NextPlanRoleZone) nextAgent.GetAgentPlan()).GetRole());
            //nextAgent.ClearPathMemory();
            return true;
        }
        return false;
    }

    private boolean detachUnusedBlocksAction() {
        Boolean blocksNeeded = true;
        // Blöcke loswerden, die nicht zu meinem aktuellen Task passen
        for (NextMapTile attachedElement : nextAgentStatus.GetAttachedElementsNextMapTiles()) {
            if (attachedElement.getThingType().contains(nextAgent.GetTaskHandler().GetRequiredBlockType())) {
                blocksNeeded = true;
                break;
            } else {
                blocksNeeded = false;
                lastDetachPosition = attachedElement.GetPosition();
            }
        }
        if (!blocksNeeded) {
            nextPossibleAction = NextActionWrapper.CreateAction(EActions.detach, new Identifier(
                    NextAgentUtil.ConvertVector2DToECardinals(lastDetachPosition).toString()));
            return true;
            //this.nextAgent.ClearPathMemory();
        }
        return false;
    }

    private boolean clearDetachedBlockAction() {
        if (nextAgentStatus.GetLastActionResult().contains("success") && this.nextAgentStatus.GetLastAction().contains("detach")) {
            possibleActions.add(NextActionWrapper.CreateAction(
                    EActions.clear, new Identifier("" + lastDetachPosition.x), new Identifier("" + lastDetachPosition.y))
            );
            return true;
        }
        return false;
    }

    private boolean goToDispenserAction() {
        // ----- Aktuelle Sicht des Agenten
        for (NextMapTile visibleThing : nextAgentStatus.GetVisibleThings()) {

            Vector2D position = visibleThing.GetPosition();

            if (NextAgentUtil.IsCorrectBlockType(nextAgent.GetActiveTask().GetRequiredBlocks(), visibleThing.getThingType())
                    && NextAgentUtil.HasFreeSlots(nextAgentStatus)
                    && NextAgentUtil.NextToUsingLocalView(position, nextAgent)
            ) {
            	NextMessage nextMessage = NextMessageUtil.getMessageFromAgent(this.nextAgent.getName(), "gehweg");
                if (nextMessage != null) {
                	if(countWaitForDispenser < 2) // wait for 2 steps
                	{
                		nextPossibleAction = generateDefaultAction();
                		countWaitForDispenser++;
                		return true;
                	}
                    // Nachricht betrifft mich ich setz eine Runde aus--
                    NextMessageUtil.removeFromMessageStore(nextMessage);
                    countWaitForDispenser = 0;
                    return false;
                } else {
                    if (nextAgentStatus.GetAttachedElementsAmount() == 0 &&
                            NextAgentUtil.IsAnotherAgentInNearOfBlock(position, this.nextAgentStatus.GetFullLocalView())) {

                    	nextMessage = NextMessageUtil.getMessageFromAgent(this.nextAgent.getName(), "gehweg");
                    	if(nextMessage == null)
                    	{
	                    	HashSet<NextAgent> agentSet = NextAgentUtil.getAgentsInFrontOfBlock(nextAgent.GetPosition(), nextAgent.GetAgentGroup().GetAgents(), position);
	                        for (NextAgent agent : agentSet) {
	                        	NextMessageUtil.addSpecificMessageToStore("gehweg", this.nextAgent.getName(), agent.getName());
	                        }
                    	}
                    	else
                    	{
                    		return false;
                    	}
                    }

                    if (visibleThing.IsDispenser() && !this.nextAgentStatus.GetLastAction().contains("request")) {
                        nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.request,
                                NextAgentUtil.ChangeVector2DToIdentifier(position));
                        return true;
                    }
                    if (visibleThing.IsBlock()) {
                        nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.attach,
                                NextAgentUtil.ChangeVector2DToIdentifier(position));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean goToGoalZoneAction() {
        Vector2D requiredBlockPosition = new Vector2D(0, 0);
        if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetGoalZones())) { // Stehe in der Goalzone
            requiredBlockPosition = nextAgent.GetActiveTask().GetRequiredBlocks().iterator().next().GetPosition();

            // -- Blockposition passt
            if (NextAgentUtil.IsBlockInPosition(requiredBlockPosition, nextAgentStatus.GetAttachedElementsVector2D())) {
                // -- ("Action - Submit");
                nextPossibleAction = NextActionWrapper.CreateAction(EActions.submit, new Identifier(nextAgent.GetActiveTask().GetName()));
                this.ClearPossibleActions();
                return true;
            } else // -- Blockposition passt nicht
            {
                Vector2D blockPosition = nextAgentStatus.GetAttachedElementsVector2D().iterator().next();
                Vector2D oppositeBlockPosition = NextAgentUtil.GetOppositeVector(blockPosition);
                String direction = NextAgentUtil.RotateInWhichDirection(
                        nextAgentStatus.GetAttachedElementsVector2D(),
                        nextAgent.GetActiveTask().GetRequiredBlocks());

                Action action = wrongBlockPositionAction(blockPosition, direction);
                if (action != null) {
                    nextPossibleAction = action;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean connectToAgentAction() {
        // correct the own position if necessary
        if (nextAgent.GetCorrectPosition()) {
            if (nextAgent.GetPathMemory().isEmpty())
                nextAgent.SetCorrectPosition(false);
            else return false;
        }
        // agent is already connected and ready to submit
        if (this.nextAgent.GetConnectedToAgent()) {
            // -- ("Action - Submit");
            nextPossibleAction = NextActionWrapper.CreateAction(EActions.submit,
                    new Identifier(nextAgent.GetActiveTask().GetName()));
            positionHasBeenCorrected = false;
            return true;
        }
        // Verbindung zweier Agenten
        NextPlanConnect nextPlanConnect = ((NextPlanConnect) this.nextAgent.GetAgentPlan());
        if (nextPlanConnect != null && NextAgentUtil.CheckIfAgentInZoneUsingLocalView(nextAgentStatus.GetGoalZones())) {
            Vector2D requiredBlockPosition = new Vector2D(0, 0);
            requiredBlockPosition = nextPlanConnect.GetTargetBlockPosition();
            if (!nextPlanConnect.IsAgentMain()) {
                requiredBlockPosition = new Vector2D(0, -1);
            }
            String direction = NextAgentUtil.RotateInWhichDirection(
                    nextAgentStatus.GetAttachedElementsVector2D(),
                    nextAgent.GetActiveTask().GetRequiredBlocks());

            // -- Blockposition passt
            if (NextAgentUtil.IsBlockInPosition(requiredBlockPosition, nextAgentStatus.GetAttachedElementsVector2D())) {
                if (nextPlanConnect.IsAgentMain()) {
                    // Say other agent to connect
                    // connect to Agent
                    nextPossibleAction = NextActionWrapper.CreateAction(EActions.connect,
                            new Identifier(nextPlanConnect.GetInvolvedAgents().iterator().next().getName()),
                            new Identifier("" + nextAgentStatus.GetAttachedElementsVector2D().iterator().next().x),
                            new Identifier("" + nextAgentStatus.GetAttachedElementsVector2D().iterator().next().y));
                    NextMessageUtil.addSpecificMessageToStore("connect",
                            nextAgent.getName(),
                            nextPlanConnect.GetInvolvedAgents().iterator().next().getName()
                    );
                } else {
                	NextMessage nextMessage = NextMessageUtil.getMessageFromAgent(this.nextAgent.getName(), "connect");
                    if (nextMessage != null) {
                        nextPossibleAction = NextActionWrapper.CreateAction(EActions.connect,
                                new Identifier(nextMessage.getSenderAgent()),
                                new Identifier("" + nextAgentStatus.GetAttachedElementsVector2D().iterator().next().x),
                                new Identifier("" + nextAgentStatus.GetAttachedElementsVector2D().iterator().next().y));
                        return true;
                    } else {
                        // Warten
                        nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);
                        return true;
                    }
                }
            } else {
                Action action = wrongBlockPositionAction(requiredBlockPosition, direction);
                if (action != null) {
                    nextPossibleAction = action;
                    return true;
                }
                return false;
            }

        }
        return false;
    }

    private Action wrongBlockPositionAction(Vector2D requiredBlockPosition, String direction) {
        Vector2D oppositeBlockPosition = NextAgentUtil.GetOppositeVector(requiredBlockPosition);
        if (NextAgentUtil.IsRotationPossible(nextAgentStatus, direction)) {
            // -- ("Action - Rotate");
            return NextActionWrapper.CreateAction(EActions.rotate, new Identifier(direction));
        }

        // Clear in BlockPosition möglich
        if (NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(),
                requiredBlockPosition)) {
            // -- ("Action - Clear");
            return NextActionWrapper.CreateAction(EActions.clear,
                    new Identifier("" + requiredBlockPosition.x),
                    new Identifier("" + requiredBlockPosition.y));
        }

        // Step nach oben möglich
        if (NextAgentUtil.IsNextStepPossible(ECardinals.n,
                this.nextAgentStatus.GetAttachedElementsVector2D(), this.nextAgentStatus.GetObstacles())) {
            // -- ("Action - Move n");
            return NextAgentUtil.GenerateNorthMove();
        }

        // Clear nach oben möglich?
        if (NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(),
                new Vector2D(0, -1))) {
            // -- ("Action - Clear");
            return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + 0), new Identifier("" + -1));
        }

        // Move to other side
        ECardinals oppositeCardinal = NextAgentUtil.ConvertVector2DToECardinals(oppositeBlockPosition);
        if (NextAgentUtil.IsNextStepPossible(oppositeCardinal,
                this.nextAgentStatus.GetAttachedElementsVector2D(), this.nextAgentStatus.GetObstacles())) {
            return NextActionWrapper.CreateAction(EActions.move, new Identifier(oppositeCardinal.toString()));
        }

        // Clear the other side
        if (NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(),
                oppositeBlockPosition)) {
            // -- ("Action - Clear");
            return NextActionWrapper.CreateAction(EActions.clear,
                    new Identifier("" + oppositeBlockPosition.x),
                    new Identifier("" + oppositeBlockPosition.y));
        }
        return null;
    }
    
    private Action generateDefaultAction() {
        return NextAgentUtil.GenerateRandomMove();
    }

    public Action GeneratePathMemory() {
        NextPlan plan = nextAgent.GetAgentPlan();
        NextMap map = this.nextAgent.GetMap();

        if (plan == null) return null;

        nextAgent.SetAgentTask(plan.GetAgentTask());

        // Survey failed 2 times -> RandomStep
        if (surveyOutOfSteps == 2) {
            possibleActions.add(generateDefaultAction()); // fallback
            surveyOutOfSteps = 0;
        }

        System.out.println("[ " + nextAgent.getName() + " ]" + " ---------------------- " + plan.GetAgentTask().toString());

        // Move to..
        int vision = this.nextAgent.GetAgentStatus().GetCurrentRole().GetVision();

        switch (plan.GetAgentTask()) {
            case surveyRandom:
                if (this.nextAgent.GetAgentGroup() != null && this.nextAgent.GetPathMemory().isEmpty()) {
                    this.nextAgent.SetPathMemory(this.nextAgent.CalculatePath(this.nextAgent.GetPosition()
                            .getAdded(vision * NextAgentUtil.GenerateRandomNumber(4) - vision * 2,
                                    vision * NextAgentUtil.GenerateRandomNumber(4) - vision * 2)));
                }
                return null;
            case surveyDispenser:
                if (survey("dispenser")) return null;
                return nextPossibleAction;
            case surveyGoalZone:
                if (survey("goal")) return null;
                return nextPossibleAction;
            case surveyRoleZone:
                if (survey("role")) return null;
                return nextPossibleAction;
            case goToDispenser:
                // Only new pathMemory, if the current Path is empty
                if (this.nextAgent.GetPathMemory().isEmpty()) {
                    NextMapTile nextMapTile = NextAgentUtil.GetNearestDispenserFromType(map.GetDispensers(),
                            ((NextPlanDispenser) plan).GetDispenser().getThingType(), this.nextAgent.GetPosition());
                    Vector2D foundDispenser = nextMapTile == null ? new Vector2D(0, 0) : nextMapTile.GetPosition();
                    this.nextAgent.SetPathMemory(this.nextAgent.CalculatePathNextToTarget(foundDispenser));
                }
                return null;
            case goToGoalzone:
                NextMessage message = NextMessageUtil.getMessageFromAgent(this.nextAgent.getName(), "readyToConnect");
                if (message != null) nextAgent.GetTaskHandler().SetReadyToConnect();
                if (this.nextAgent.GetPathMemory().isEmpty() && map.IsGoalZoneAvailable()) {
                    this.nextAgent.SetPathMemory(this.nextAgent.CalculatePath(
                            NextAgentUtil.GetNearestZone(this.nextAgent.GetPosition(), map.GetGoalZones())));
                }
                return null;
            case goToRolezone:
                if (this.nextAgent.GetPathMemory().isEmpty() && map.IsRoleZoneAvailable()) {
                    this.nextAgent.SetPathMemory(this.nextAgent.CalculatePath(
                            NextAgentUtil.GetNearestZone(this.nextAgent.GetPosition(), map.GetRoleZones())));
                }
                return null;
            case connectToAgent:
                NextPlanConnect nextPlanConnect = ((NextPlanConnect) this.nextAgent.GetAgentPlan());
                if (nextPlanConnect.IsAgentMain()) {
                    calcBestPosForMainAgent();
                } else {
                    calcWayToConnectPosition();
                }
                return null;
            case cleanMap:
                // Geht zur Goalzone
                if (this.nextAgent.GetPathMemory()
                        .isEmpty() && map.IsGoalZoneAvailable() && !NextAgentUtil.CheckIfAgentInZoneUsingLocalView(
                        nextAgent.GetAgentStatus().GetGoalZones())) {
                    this.nextAgent.SetPathMemory(nextAgent.CalculatePath(
                            NextAgentUtil.GetNearestZone(nextAgent.GetPosition(), map.GetGoalZones())));
                }
                if (nextAgent.GetPathMemory().isEmpty()) {
                    nextAgent.SetPathMemory(nextAgent.CalculatePath(nextAgent.GetPosition()
                            .getAdded(NextAgentUtil.GenerateRandomNumber(vision),
                                    NextAgentUtil.GenerateRandomNumber(vision))));
                }
                return null;
            default:
                return null;
        }
    }

    private void calcBestPosForMainAgent() {
        if (!nextAgent.GetPathMemory().isEmpty()) return;

        //move agent to middle of goalZone
        if (positionHasBeenCorrected) {
            positionHasBeenCorrected = true;
            HashSet<Vector2D> goalPositions = nextAgent.GetAgentStatus().GetGoalZones().stream().map(NextMapTile::GetPosition).collect(Collectors.toCollection(HashSet::new));
            Vector2D target = new Vector2D(0, 0);
            if (!goalPositions.contains(new Vector2D(0, 2))) target.getAdded(0, -2);
            if (!goalPositions.contains(new Vector2D(0, -2))) target.getAdded(0, 2);
            if (!goalPositions.contains(new Vector2D(2, 0))) target.getAdded(-2, 0);
            if (!goalPositions.contains(new Vector2D(-2, 0))) target.getAdded(2, 0);

            nextAgent.SetPathMemory(nextAgent.CalculatePath(target.getAdded(this.nextAgent.GetPosition())));
        }
        // Position to secondAgent
        NextAgent involvedAgent = ((NextPlanConnect) nextAgent.GetAgentPlan()).GetInvolvedAgents().iterator().next();
        NextMessageUtil.addSpecificMessageToStore("readyToConnect", this.nextAgent.getName(), involvedAgent.getName());
        if (involvedAgent.GetAgentTask().equals(EAgentActivity.connectToAgent)) {
            Vector2D blockPos = ((NextPlanConnect) involvedAgent.GetAgentPlan()).GetTargetBlockPosition();
            Vector2D targetPos = this.nextAgent.GetPosition().getAdded(blockPos).getAdded(new Vector2D(0, 1));

            NextMessageUtil.addSpecificMessageToStore("position", this.nextAgent.getName(), involvedAgent.getName(),
                    targetPos);
        }
    }


    private void calcWayToConnectPosition() {
        if (!nextAgent.GetPathMemory().isEmpty()) return;
        NextMessage nextMessage = NextMessageUtil.getMessageFromAgent(this.nextAgent.getName(), "position");
        if (nextMessage != null && !nextMessage.getPosition().equals(this.nextAgent.GetPosition())) {
            nextAgent.SetCorrectPosition(true);
            this.nextAgent.SetPathMemory(this.nextAgent.CalculatePath(nextMessage.getPosition()));
            NextMessageUtil.removeFromMessageStore(nextMessage);
        }
    }

    private boolean survey(String type) {
        boolean result = true;
        if (this.nextAgent.GetPathMemory().isEmpty()) {
            if (this.nextAgentStatus.IsLastSpecificActionSuccess(type, "survey")) {
                for (NextSurveyedThing nextSurveyedThings : this.nextAgentStatus.GetSurveyedThings()) {
                    int distance = nextSurveyedThings.GetDistance();
                    if (surveySteps == 0) {
                        // erstes Abtasten
                        nextPossibleAction = NextAgentUtil.GenerateNorthMove();
                        lastDirection = "n";
                        surveySteps++;
                        result = false;
                    } else if (surveySteps == 1) {
                        // north or south
                        if (distance > lastSurveyedDistance) {
                            lastDirection = "s";
                        }
                        nextPossibleAction = NextAgentUtil.GenerateEastMove();
                        surveySteps++;
                        result = false;
                    } else if (surveySteps == 2) {
                        // north or south
                        if (distance > lastSurveyedDistance) {
                            lastDirection += "e";
                        } else {
                            lastDirection += "w";
                        }
                        // Weg in Richtung des Dispensers
                        this.nextAgent.SetPathMemory(this.nextAgent.CalculatePath(
                                NextAgentUtil.RandomPointInDirection(lastDirection, this.nextAgent.GetPosition(),
                                        distance)));
                        surveySteps = 0;
                        result = true;
                    }
                    lastSurveyedDistance = distance;
                }
            } else {
                nextPossibleAction = NextAgentUtil.GenerateSurveyThingAction(type);
                result = false;
            }
        }
        return result;
    }


    public void ResetAfterTaskChange() {
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
