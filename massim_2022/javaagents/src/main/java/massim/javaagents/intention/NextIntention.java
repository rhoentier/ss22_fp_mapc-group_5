package massim.javaagents.intention;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentActivity;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;
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
    ArrayList<Action> possibleActions;
    Action nextPossibleAction;
    private final NextAgentStatus nextAgentStatus;

    private int lastSurveyedDistance = 0;
    private String lastDirection = "n";
    private int surveySteps = 0;
    private int surveyOutOfSteps = 0;

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
            if (attachedElement.getThingType().contains(nextAgent.GetTaskPlanner().GetRequiredBlockType())) {
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
                if (nextAgent.nextMessage.hasMessage() && nextAgent.nextMessage.getTargetAgent().contains(nextAgent.getName())) {
                    // Nachricht betrifft mich ich setz eine Runde aus--
                    nextAgent.nextMessage.clearMessage();
                    return false;
                } else {
                    if (nextAgentStatus.GetAttachedElementsAmount() == 0 &&
                            NextAgentUtil.IsAnotherAgentInNearOfBlock(position, this.nextAgentStatus.GetFullLocalView())) {
                        HashSet<NextAgent> agentSet = NextAgentUtil.getAgentsInFrontOfBlock(nextAgent.GetPosition(), nextAgent.GetAgentGroup().GetAgents(), position);
                        for (NextAgent agent : agentSet) {
                            nextAgent.nextMessage.newMessage("Gehweg", this.nextAgent.getName(), agent.getName());
                        }
                        return false;
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
                            new Identifier("" + nextPlanConnect.GetTargetBlockPosition().x),
                            new Identifier("" + nextPlanConnect.GetTargetBlockPosition().y));
                    nextAgent.nextMessage.newMessage("connect",
                            nextAgent.getName(),
                            nextPlanConnect.GetInvolvedAgents().iterator().next().getName()
                            // position nextPlanConnect.get
                    );
                } else {
                    if (nextAgent.nextMessage.hasMessage()
                            && nextAgent.nextMessage.getTargetAgent().contains(nextAgent.getName())
                            && nextAgent.nextMessage.getMessage().contains("connect")) {
                        nextPossibleAction = NextActionWrapper.CreateAction(EActions.connect,
                                new Identifier(nextAgent.nextMessage.getSenderAgent()),
                                new Identifier("" + nextPlanConnect.GetTargetBlockPosition().x),
                                new Identifier("" + nextPlanConnect.GetTargetBlockPosition().y));
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


    // ALTE FUNKTION
    public void GeneratePossibleActions() {

        // Special case: Interaction with an adjacent element.
        // TODO Rollenwechsel implementieren
        // Wenn Agent in einer RoleZone und noch nicht worker ist
        if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(
                nextAgentStatus.GetRoleZones()) && nextAgent.GetAgentPlan() instanceof NextPlanRoleZone) {
            possibleActions.add(
                    NextAgentUtil.GenerateRoleChangeAction(((NextPlanRoleZone) nextAgent.GetAgentPlan()).GetRole()));
            nextAgent.ClearPathMemory();
        }

        //detachUnusedBlocks();

        // Aktuelle Sicht des Agenten
        for (NextMapTile visibleThing : nextAgentStatus.GetVisibleThings()) {

            Vector2D position = visibleThing.GetPosition();

            // Nur wenn Task aktiv ist, sind Dispenser und Blocks relevant
            // Blocktyp passt und goToDispenser
            if (this.nextAgent.GetActiveTask() != null && NextAgentUtil.IsCorrectBlockType(
                    nextAgent.GetActiveTask().GetRequiredBlocks(),
                    visibleThing.getThingType()) && NextAgentUtil.HasFreeSlots(
                    nextAgentStatus) && this.nextAgent.GetAgentTask() == EAgentActivity.goToDispenser) {
                // Stehe direkt nebenan && Blocktyp passt
                // TODO hier die kleinere ID nehmen?
                if (NextAgentUtil.NextToUsingLocalView(position,
                        nextAgent) && !NextAgentUtil.IsAnotherAgentInNearOfBlock(position,
                        this.nextAgentStatus.GetFullLocalView())) {
                    if (NextAgentUtil.HasFreeSlots(nextAgentStatus)) {
                        // ********************** Dispenser
                        if (visibleThing.IsDispenser()) {
                            // -- ("Action - Request");
                            possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request,
                                    NextAgentUtil.ChangeVector2DToIdentifier(position)));
                            this.nextAgent.ClearPathMemory();
                        }

                        // ********************** Block
                        if (visibleThing.IsBlock()) {
                            // -- ("Action - Attach");
                            possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach,
                                    NextAgentUtil.ChangeVector2DToIdentifier(position)));
                            this.nextAgent.ClearPathMemory();
                        }
                    }
                }
            }

            Vector2D requiredBlockPosition = new Vector2D(0, 0);
            // ********************** GoalZone (1Block)
            if (nextAgentStatus.GetAttachedElementsAmount() > 0 && NextAgentUtil.CheckIfAgentInZoneUsingLocalView(
                    nextAgentStatus.GetGoalZones()) &&
                    (this.nextAgent.GetAgentTask() == EAgentActivity.goToGoalzone || this.nextAgent.GetAgentTask() == EAgentActivity.connectToAgent)) {
                NextPlanConnect nextPlanConnect = null;
                if (this.nextAgent.GetAgentTask() == EAgentActivity.connectToAgent) {
                    // TODO aendern bei 3er blocks
                    nextPlanConnect = ((NextPlanConnect) this.nextAgent.GetAgentPlan());
                    requiredBlockPosition = nextPlanConnect.GetTargetBlockPosition();
                    if (!nextPlanConnect.IsAgentMain()) {
                        requiredBlockPosition = new Vector2D(0, -1);
                    }
                } else {
                    requiredBlockPosition = nextAgent.GetActiveTask().GetRequiredBlocks().iterator().next()
                            .GetPosition();
                }

                if (NextAgentUtil.IsBlockInPosition(requiredBlockPosition,
                        nextAgentStatus.GetAttachedElementsVector2D())) {
                    if (nextPlanConnect != null) {
//                        if (this.nextAgentStatus.GetLastActionResult()
//                                .contains("fail") && this.nextAgentStatus.GetLastAction().contains("connect")) {
//                            nextPlanConnect.SetAgentConnection(false);
//                        }

                        if (nextPlanConnect.GetInvolvedAgents().size() == 0 || nextPlanConnect.IsAgentConnected()) {
                            // Korrekte Blockposition
                            // -- ("Action - Submit");
                            possibleActions.add(NextActionWrapper.CreateAction(EActions.submit,
                                    new Identifier(nextAgent.GetActiveTask().GetName())));
                            break;
                        }

                        // connect to Agent
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.connect,
                                new Identifier(nextPlanConnect.GetInvolvedAgents().iterator().next().getName()),
                                new Identifier("" + nextPlanConnect.GetTargetBlockPosition().x),
                                new Identifier("" + nextPlanConnect.GetTargetBlockPosition().y)));
//                        nextPlanConnect.SetAgentConnection(true);
                    } else {
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.submit,
                                new Identifier(nextAgent.GetActiveTask().GetName())));
                    }

                } else {
                    // Blockposition falsch
                    // requiredBlockPosition = nextAgent.GetActiveTask().GetRequiredBlocks().iterator().next().GetPosition();
                    Vector2D blockPosition = nextAgentStatus.GetAttachedElementsVector2D().iterator().next();
                    Vector2D oppositeBlockPosition = NextAgentUtil.GetOppositeVector(blockPosition);
                    String direction = NextAgentUtil.RotateInWhichDirection(
                            nextAgentStatus.GetAttachedElementsVector2D(),
                            nextAgent.GetActiveTask().GetRequiredBlocks());

                    if (NextAgentUtil.IsRotationPossible(nextAgentStatus, direction)) {
                        // -- ("Action - Rotate");
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.rotate, new Identifier(direction)));
                        break;
                    }

                    // Clear in BlockPosition möglich
                    if (NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(),
                            requiredBlockPosition)) {
                        // -- ("Action - Clear");
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.clear,
                                new Identifier("" + requiredBlockPosition.x),
                                new Identifier("" + requiredBlockPosition.y)));
                        break;
                    }

                    // Step nach oben möglich
                    if (NextAgentUtil.IsNextStepPossible(ECardinals.n,
                            this.nextAgentStatus.GetAttachedElementsVector2D(), this.nextAgentStatus.GetObstacles())) {
                        // -- ("Action - Move n");
                        possibleActions.add(NextAgentUtil.GenerateNorthMove());
                        break;
                    }

                    // Clear nach oben möglich?
                    if (NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(),
                            new Vector2D(0, -1))) {
                        // -- ("Action - Clear");
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + 0),
                                new Identifier("" + -1)));
                        break;
                    }

                    // Move to other side
                    ECardinals oppositeCardinal = NextAgentUtil.ConvertVector2DToECardinals(oppositeBlockPosition);
                    if (NextAgentUtil.IsNextStepPossible(oppositeCardinal,
                            this.nextAgentStatus.GetAttachedElementsVector2D(), this.nextAgentStatus.GetObstacles())) {
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.move,
                                new Identifier(oppositeCardinal.toString())));
                        break;
                    }

                    // Clear the other side
                    if (NextAgentUtil.IsObstacleInPosition(this.nextAgentStatus.GetFullLocalView(),
                            oppositeBlockPosition)) {
                        // -- ("Action - Clear");
                        possibleActions.add(NextActionWrapper.CreateAction(EActions.clear,
                                new Identifier("" + oppositeBlockPosition.x),
                                new Identifier("" + oppositeBlockPosition.y)));
                        break;
                    }

                    // Fallback
                    possibleActions.add(generateDefaultAction());
                }
                this.nextAgent.ClearPathMemory();
            }

        }
        if (nextAgentStatus.GetLastActionResult().contains("success") && this.nextAgentStatus.GetLastAction()
                .contains("detach")) {
            possibleActions.clear();
            //clearDetachedBlock();
        }
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
                if (this.nextAgent.GetPathMemory()
                        .isEmpty() && this.nextAgent.GetAgentPlan() != null && map.IsGoalZoneAvailable()) {
                    NextPlanConnect nextPlanConnect = ((NextPlanConnect) this.nextAgent.GetAgentPlan());

                    // TODO hier weitern, damit die 2er Task in der Mitte der Goalzone abgegeben werden können
                    if (nextPlanConnect.IsAgentMain()) {
                        checkIfConnectPositionIsEmpty();
                    } else {
                        calcWayToConnectPosition();
                    }
                }
                return null;
            default:
                return null;
        }
    }

    private void checkIfConnectPositionIsEmpty() {
        // get only the Vector2D position of the required blocks
        HashSet<Vector2D> connectPositions = nextAgent.GetActiveTask().GetRequiredBlocks().stream()
                .map(NextMapTile::GetPosition).collect(
                        Collectors.toCollection(HashSet::new));
        // filter the positions that are not free
        connectPositions = connectPositions.stream()
                .filter(pos -> NextAgentUtil.IsPositionFreeUsingLocalView(pos, nextAgentStatus.GetFullLocalView()))
                .filter(pos -> pos.equals(new Vector2D(0, 1)))
                .collect(Collectors.toCollection(HashSet::new));
        // if all positions are free, then the agent should wait
        if (connectPositions.isEmpty()) {
            nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);
            return;
        }
        // create a move that the connectPos is free
        //NextAgentUtil.GenerateNorthMove();
    }


    private void calcWayToConnectPosition() {
        // Handling to get easy access to involved agents
        HashSet<NextAgent> involvedAgentSet = ((NextPlanConnect) nextAgent.GetAgentPlan()).GetInvolvedAgents();
        NextAgent[] involvedAgents = new NextAgent[involvedAgentSet.size()];
        involvedAgents = involvedAgentSet.toArray(involvedAgents);
        if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(involvedAgents[0].GetAgentStatus().GetGoalZones())) {
            //TODO: Wenn Task mit mehr Blöcken abgearbeitet wird, dann hier verbessern
            Vector2D involvedAgentPos = nextAgent.GetAgentGroup().GetAgentPosition(involvedAgents[0]);
            // Calculate target position for the agent (one field below the block has to be)
            Vector2D blockPos = ((NextPlanConnect) nextAgent.GetAgentPlan()).GetTargetBlockPosition();
            Vector2D targetPos = involvedAgentPos.getAdded(blockPos).getAdded(new Vector2D(0, 1));
            if (!targetPos.equals(this.nextAgent.GetPosition())) {
                this.nextAgent.SetPathMemory(this.nextAgent.CalculatePathNextToTarget(targetPos));
            } else {
                nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);
            }
        } else {
            nextPossibleAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);
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
