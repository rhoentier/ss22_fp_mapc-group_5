package massim.javaagents.agents;

import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.intention.NextIntention;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import eis.iilang.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import massim.javaagents.MailService;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentActivity;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextTaskHandler;
import massim.javaagents.timeMonitor.NextTimeMonitor;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.NextRandomPath;
import massim.javaagents.percept.NextTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextAStarPath;
import massim.javaagents.percept.NextRole;

/**
 * First iteration of an experimental agent.
 * <p>
 * Done:
 * - Handling of transition between simulations Basic action generation
 * based on random movement Processing of all percepts and storing in dataVaults
 * - Gruppenbildung
 * <p>
 *
 * @Author Alexander, Miriam
 */
public class NextAgent extends Agent {

    public static HashMap<Integer, NextGroup> globalGroupMap = new HashMap<>();
    public static HashMap<String, HashSet<Vector2D>> GroupBuildingSkipMemory = new HashMap<>();
    public static int lastGroupJoinAtStep = -1;

    /*
     * ########## region fields
     */
    private int lastID = -1;        // Is used to compare with actionID -> new Step Recognition
    private Boolean actionRequestActive = false; // Todo: implement reaction to True if needed. Is activated, before next Step.
    private Boolean disableAgentFlag = false; // True when all Simulations are finished 

    //Agent related attributes
    private NextAgentStatus agentStatus;
    private NextGroup agentGroup;
    private HashSet<String> messageStore = new HashSet<>(); // message collector for group finding process

    //Simulation related attributes
    private NextSimulationStatus simStatus;

    //Compilation of finished Simulations to be Processed after "deactivateAgentFlag == True"
    private List<NextSimulationStatus> finishedSimulations = new ArrayList<>();

    private NextTimeMonitor timeMonitor;

    //BDI
    private NextIntention intention;
    private NextTaskHandler taskHandler;

    // --- Algorithms ---
    NextPerceptReader processor; // Eismassim interpreter

    // Pathfinding algorithm
    //PathfindingConfig pathfindingConfig;
    private NextManhattanPath manhattanPath = new NextManhattanPath(); // Manhattan distance based path generation
    private NextAStarPath aStar = new NextAStarPath();  // A*Star based path gemeration
    private List<Action> pathMemory = new ArrayList<>();    // storing 

    // Tasks
    private NextTask activeTask = null;
    private EAgentActivity agentActivity;
    private NextPlan agentPlan;

    private int failOffest = 2;
    private int failStatus = 0;

    private boolean correctPosition = false;

    private Vector2D goToPosition = new Vector2D();
    private boolean connectedToAgent = false;

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public NextAgent(String name, MailService mailbox) {
        super(name, mailbox);

        this.agentStatus = new NextAgentStatus(this);
        this.simStatus = new NextSimulationStatus();

        //PathfindingConfig.ParseConfig("conf/NextAgents");
        //this.say("Algorithmus: " + PathfindingConfig.GetAlgorithm().toString());
        this.intention = new NextIntention(this);

        this.processor = new NextPerceptReader(this);

        taskHandler = new NextTaskHandler(this);
        this.setGoToPosition(new Vector2D(0, 0));
    }

    /*
     * ##################### endregion constructor
     */

    /*
     * ########## region public methods
     */
    // Original Method
    @Override
    public void handlePercept(Percept percept) {
    }

    // Original Method extended
    @Override
    public void handleMessage(Percept message, String sender) {
        //this.say("Message: " + message.toProlog() + " from Sender: " + sender);

        String[] messageContainer = message.toString().split(",");

        //-- GroupBuilding start
        // Message Type: AgentObserved,Step,6,X,0,Y,3
        if (messageContainer[0].contains("AgentObserved")) {
            if (!(this.simStatus.GetCurrentStep() == null) && (Integer.parseInt(messageContainer[2]) > 2)) {
                if (this.simStatus.GetCurrentStep() == Integer.parseInt(messageContainer[2])) {
                    int xToTest = -1 * Integer.parseInt(messageContainer[4]);
                    int yToTest = -1 * Integer.parseInt(messageContainer[6]);
                    for (NextMapTile feld : this.agentStatus.GetVisibleThings()) {
                        if (feld.getPositionX() == xToTest && feld.getPositionY() == yToTest) {
                            if (feld.getThingType().contains(this.agentStatus.GetTeamName())
                                    && feld.getThingType().contains(NextConstants.EVisibleThings.entity.toString())) {
                                this.sendMessage(new Percept("GroupFinding-ResponseMessage," + this.agentGroup.getGroupID() + "," + xToTest + "," + yToTest + ", MapPosition," + this.GetPosition().x + "," + this.GetPosition().y), sender, this.getName());
                            }
                        }
                    }
                }
            }
        }

        // Message Type: AO-ResponseMessage,GroupID,x,y,MapPosition,x,y
        if (messageContainer[0].contains("GroupFinding-ResponseMessage")) {
            int mapOffsetX = Integer.parseInt(messageContainer[5]) - this.GetPosition().x;
            int mapOffsetY = Integer.parseInt(messageContainer[6]) - this.GetPosition().y;
            messageStore.add(new Percept("JoinGroup-Execution," + this.agentGroup.getGroupID()) + "," + sender + "," + this.getName() + "," + messageContainer[2] + "," + messageContainer[3] + "," + mapOffsetX + "," + mapOffsetY);
            messageStore.add(new Percept("JoinGroup-Execution," + messageContainer[1]) + "," + this.getName() + "," + this.getName() + "," + (-1 * Integer.parseInt(messageContainer[2])) + "," + (-1 * Integer.parseInt(messageContainer[3]) + "," + (-1 * mapOffsetX) + "," + (-1 * mapOffsetY)));
        }

        // Message Type: JoinGroup-Execution,GroupID,x,y,MapOffsetX,MapOffsetY
        if (messageContainer[0].contains("JoinGroup-Execution")) {
            // this.say("JoinGroup-EXECUTION " + messageContainer[1] + " X: " + messageContainer[2] + " Y: " + messageContainer[3] + " mapOffsetX: " + messageContainer[4] + " mapOffsetY: " + messageContainer[5]);
            NextGroup target = globalGroupMap.get(Integer.parseInt(messageContainer[1]));
            if (target != null) {
                joinGroup(target, new Vector2D(Integer.parseInt(messageContainer[2]), Integer.parseInt(messageContainer[3])), new Vector2D(Integer.parseInt(messageContainer[4]), Integer.parseInt(messageContainer[5])));
            } else {
                this.say("Error in group join execution");
            }
        }

        //-- GroupBuilding end
        //-- MapSizeDicovery start
        // "MapSizeDiscoveryHasStarted"
        if (messageContainer[0].contains("MapSizeDiscoveryHasStarted")) {
            this.simStatus.ActivateMapSizeDiscovery();
        }
        // "MapHeightFound"
        if (messageContainer[0].contains("MapHeightFound")) {
            this.SetSimulationMapHeight(Integer.parseInt(messageContainer[1]));
        }
        // "MapWidthFound"
        if (messageContainer[0].contains("MapWidthFound")) {
            this.SetSimulationMapWidth(Integer.parseInt(messageContainer[1]));
        }

        //-- MapSizeDicovery end
    }

    /**
     * Main agent logic
     *
     * @return Action - Next action for Massim simulation for this agent.
     */
    @Override
    public Action step() {

        long startTime = Instant.now().toEpochMilli();

        // Initialise a group if empty
        if (agentGroup == null) {
            createGroup();
        }

        // GroupJoining delayed, to spread agents
        if (lastID > 3 && lastGroupJoinAtStep != simStatus.GetCurrentStep()) {
            // Check if friendly Agents are visible and join them to groups
            // Map Information of the last step is used to ensure consistency
            processFriendlyAgents();
            processGroupJoinMessages();
        }

        processServerData();

        // ActionGeneration is started on a new ActionID only
        if (simStatus.GetActionID() > lastID) {
            lastID = simStatus.GetActionID();

            updateInternalBeliefs();
            //printBeliefReport(); // live String output to console

            // -----------------------------------
            clearPossibleActions();

            // new path
            if (agentGroup != null) {
                NextAgentPlan groupPlan = agentGroup.GetPlan(this);
                taskHandler.SetAgentPlan(groupPlan);
                NextPlan nextPlan = taskHandler.GetDeepestEAgentTask();
                if (nextPlan != null) {
                    NextTask nextTask = taskHandler.GetCurrentTask();
                    // Neuen Task nur setzen, wenn sich der Task verändert hat.
                    if (nextTask != null) {
                        if (this.GetActiveTask() == null || !this.GetActiveTask().GetName()
                                .contains(nextTask.GetName())) {
                            if (!this.agentActivity.toString().contains("survey")) {
                                intention.ResetAfterTaskChange();
                            }
                            SetActiveTask(nextTask);
                        }
                    }
                    SetAgentPlan(nextPlan);
                }
            }


            //printActionsReport(); // live String output to console
            //printFinalReport(); // live String output to console

            Action nextAction = generatePathMemory();
            if (nextAction == null) {
                // Weg generiert - aktuelle Action auswählen
                nextAction = selectNextAction2();
            }
            
            System.out.println();
            System.out.println("NextPossibleAction .... " + nextAction.toString());
            System.out.println();

            /**
             if( agentGroup != null) {
             // this.say("Current tile was blocked: " + this.agentGroup.GetGroupMap().GetMapTile(this.GetPosition()).CheckAtStep(this.simStatus.GetCurrentStep()));
             // this.say("Blocked Steps " + this.agentGroup.GetGroupMap().GetMapTile(this.GetPosition()).ReportBlockedSteps());
             // this.say("Current Step " + this.simStatus.GetCurrentStep());
             }
             //**/

            if (this.agentStatus.GetLastActionResult().contains("fail")) {
                System.out.println("Letzte FailedAction: " + this.agentStatus.GetLastAction() + " " + this.agentStatus.GetLastActionResult());
                //this.connectedToAgent = false;
            }
            //System.out.println("Used time: " + (Instant.now().toEpochMilli() - startTime) + " ms"); // Calculation Time report
            return nextAction;

        }
        return null;

    }

    /**
     * Getter for local NextAgentStatus
     *
     * @return NextAgentStatus
     */
    public NextAgentStatus GetAgentStatus() {
        return this.agentStatus;
    }

    /**
     * Getter for local NextSimulationStatus
     *
     * @return NextSimulationStatus
     */
    public NextSimulationStatus GetSimulationStatus() {
        return simStatus;
    }

    /**
     * Set flag to disable agent !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Check if
     * needed or ok to remove.
     */
    public void setFlagDisableAgent() {
        this.disableAgentFlag = true;
    }

    /**
     * Set flag - action request active !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * Check if needed or ok to remove.
     */
    public void setFlagActionRequest() {
        this.actionRequestActive = true;
    }

    public NextTask GetActiveTask() {
        return this.activeTask;
    }

    public NextGroup GetAgentGroup() {
        return agentGroup;
    }

    public void SetAgentGroup(NextGroup agentGroup) {
        this.agentGroup = agentGroup;
    }

    public void SetActiveTask(NextTask activeTask) {
        this.activeTask = activeTask;
    }

    public EAgentActivity GetAgentTask() {
        return this.agentActivity;
    }

    public void SetAgentTask(EAgentActivity agentTask) {
        this.agentActivity = agentTask;
    }

    public void SetAgentPlan(NextPlan agentPlan) {
        this.agentPlan = agentPlan;
    }

    public List<Action> GetPathMemory() {
        return this.pathMemory;
    }

    public void SetPathMemory(List<Action> pathMemory) {
        this.pathMemory = pathMemory;
    }


    public void SetCorrectPosition(boolean correctPosition) {
        this.correctPosition = correctPosition;
    }

    public boolean GetCorrectPosition() {
        return correctPosition;
    }

    public boolean GetConnectedToAgent(){return connectedToAgent;}

    public void ClearPathMemory() {
        this.pathMemory = new ArrayList<Action>();
    }

    public Vector2D GetPosition() {
        if (agentGroup == null) {
            return new Vector2D(0, 0);
        }
        return agentGroup.GetAgentPosition(this).clone();
    }

    public Vector2D GetPositionRef() {
        if (agentGroup == null) {
            return new Vector2D(0, 0);
        }
        return agentGroup.GetAgentPosition(this);
    }

    public NextMap GetMap() {
        if (this.agentGroup == null) {      // Workaround for deep linking
            return new NextMap(this);
        }
        return this.agentGroup.GetGroupMap();
    }

    public void MovePosition(Vector2D vector) {
        this.agentGroup.MoveSingleAgent(this, vector);
    }

    public void ModPosition() {
        this.agentGroup.ModSingleAgent(this);
    }

    public int GetCarryableBlocks() {
        return (int) agentStatus.GetCurrentRole().GetSpeed().stream().filter(speed -> speed > 0).count();
    }

    public NextTaskHandler GetTaskHandler() {
        return this.taskHandler;
    }

    public NextPlan GetAgentPlan() {
        return agentPlan;
    }

    /**
     * CalculatePath to the target cell. The function decides which algorithm to
     * select based on target attributes.
     *
     * @param target Vector2D - cell on the general map
     * @return List of actions to reach the target.
     */
    public List<Action> CalculatePath(Vector2D target) {
        // System.out.println("iNPUT" + agentStatus.GetPosition() + " " + target);

        //NextMap map = GetMap();
    	this.setGoToPosition(target);
        NextMap map = this.agentGroup.GetGroupMap();
        Boolean targetIsOnMap = map.IsOnMap(target);
        try {
            if (targetIsOnMap && !map.GetMapArray()[target.x][target.y].getThingType().equals("unknown")) {
                List<Action> pathMemoryA;
                pathMemoryA = aStar.calculatePath(map.GetMapArray(), GetPosition(), target, this.simStatus.GetCurrentStep());
                //this.say("A* path:" + pathMemoryA);
                if (pathMemoryA.size() == 0) {
                    // Fuer den Fall, dass der Weg versperrt ist und es fuer den A* unmoeglich ist, hinzukommen
                    return calculateManhattanPath(target);
                }
                return pathMemoryA;

            } else {
                return calculateManhattanPath(target);
            }
        } catch (Exception e) {
            this.say("Path generation failed: " + e);
        }
        return null;
    }

    /**
     * Calculate Path to the Target, ending on a free Tile next to it
     *
     * @param target
     * @return
     */
    public List<Action> CalculatePathNextToTarget(Vector2D target) {

        //NextMap map = GetMap();
        NextMap map = this.agentGroup.GetGroupMap();
        //ToDo - Optimale Position je nach Ausgangslage auswählen
        try {
            if (map.GetMapArray()[target.x + 1][target.y].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x + 1, target.y));
            }
            if (map.GetMapArray()[target.x + 1][target.y].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x + 1, target.y));
            }
            if (map.GetMapArray()[target.x + 1][target.y].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x + 1, target.y));
            }
            if (map.GetMapArray()[target.x + 1][target.y].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x + 1, target.y));
            }
        } catch (Exception e) {
            this.say("CalculatePathNextToTarget:" + e);
        }
        return CalculatePath(new Vector2D(target.x, target.y));
    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases.
     *
     * @param message - String based message
     */
//    public void TellGroup(String message) {
//        this.agentGroup.TellGroup(message, this, "");
//    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases.
     *
     * @param message - String based message
     */
    public void TellGroupAgent(String message, NextAgent senderAgent, NextAgent targetAgent) {
        this.agentGroup.TellGroupAgent(message, senderAgent, targetAgent);
    }

    /**
     * Handling of custom groupmessages
     *
     * @param message - String based message
     */
    public void HandleGroupMessage(String message, String senderAgent, String targetAgent) {
        this.say("Message (" + message + ") from " + senderAgent + " to " + targetAgent);
        //this.nextMessage.newMessage(message, senderAgent, targetAgent);
        // definitive implementation needed
    }

    public HashMap<NextMapTile, Integer> GetDispenserDistances(HashSet<NextMapTile> requiredBlocks) {
        HashMap<NextMapTile, Integer> distances = new HashMap<>();
        for (NextMapTile requiredBlock : requiredBlocks) {
            HashSet<NextMapTile> attachedElements = agentStatus.GetAttachedElementsNextMapTiles();
            boolean hasCorrectBlockAttached = false;
            for (NextMapTile attachedElement : attachedElements) {
                if (attachedElement.getThingType().contains(requiredBlock.getThingType())) {
                    hasCorrectBlockAttached = true;
                }
            }
            if (hasCorrectBlockAttached) {
                distances.put(requiredBlock, 0);
                continue;
            }
            NextMapTile nearestDispenser = NextAgentUtil.GetNearestDispenserFromType(GetMap().GetDispensers(), requiredBlock.getThingType(), GetPosition());
            if (nearestDispenser == null) {
                distances.put(requiredBlock, 1000);
                continue;
            }
            distances.put(requiredBlock, aStar.calculatePath(GetMap().GetMapArray(), GetPosition(), nearestDispenser.GetPosition(), simStatus.GetCurrentStep()).size());
        }
        return distances;
    }

    public boolean IsAgentActivity(EAgentActivity activity) {
        return this.agentActivity.equals(activity);
    }

	public Vector2D getGoToPosition() {
		return goToPosition;
	}

	public void setGoToPosition(Vector2D goToPosition) {
		this.goToPosition = goToPosition;
	}


    /*
     * ##################### endregion public methods
     */
    //--------------------------------------------------------------------------
    /*
     * ########## region private methods
     */
    private void resetAfterInactiveTask() {
        this.SetActiveTask(null);
        this.clearPossibleActions();
        this.ClearPathMemory();
    }

    /**
     * Stops the Agent. Closes the agent window
     */
    private void disableAgent() {
        this.say("All games finished!");
        System.exit(1); // Kill the window
    }

    /**
     * Agent behavior at the start of a new simulation
     */
    private void startTheSimulation() {
        System.out.println("Starting the Simulation");
        resetAgent();
    }

    /**
     * Agent behavior after finishing of the current simulation
     */
    private void finishTheSimulation() {
        this.say("Finishing this Simulation!");
        this.say("Result: #" + simStatus.GetRanking());

    }

    /**
     * The path is optimised using local Percepts. Reactive behavior.
     *
     * @param currentPathMemory
     * @return adjustedPathMemory
     * @author Alexander Lorenz
     */
    private List<Action> generateAlternativePathMemory(List<Action> currentPathMemory) {

        int vision = this.agentStatus.GetCurrentRole().GetVision();
        int reach = vision - 1;
        HashSet<NextMapTile> fullLocalView = this.agentStatus.GetFullLocalView();

        List<Action> localPath = new ArrayList<>();
        List<Action> pathRest = new ArrayList<>();

        //Divide path memory
        if (pathMemory.size() > reach) {
            localPath = pathMemory.subList(0, reach);
            pathRest = pathMemory.subList(reach, pathMemory.size());
        } else {
            localPath = pathMemory;
        }

        // Report Element  
        /*
        System.out.println("Original " + pathMemory + "/n ---------------------------");
        System.out.println("Local " + localPath);
        System.out.println("Global " + pathRest);
        //*/
        // Convert local Path to Target Cell and clear Path in between
        Vector2D target = clearMapTiles(this.GetPosition(), localPath);

        //BuildMap
        NextMapTile[][] localMap = new NextMapTile[2 * vision + 1][2 * vision + 1];

        for (int x = 0; x < localMap.length; x++) {
            for (int y = 0; y < localMap[0].length; y++) {
                localMap[x][y] = new NextMapTile(x, y, 0, "unknown");
            }
        }
        // Fill Visible Area
        HashSet<NextMapTile> emptyVision = new HashSet<>();

        for (int i = -1 * vision; i <= vision; i++) {
            for (int j = -1 * vision; j <= vision; j++) {
                if (Math.abs(i) + Math.abs(j) <= vision) {
                    emptyVision.add(new NextMapTile(i, j, this.simStatus.GetCurrentStep()));
                }
            }
        }

        for (NextMapTile element : emptyVision) {
            int newX = element.getPositionX() + vision;
            int newY = element.getPositionY() + vision;

            localMap[newX][newY] = element.Clone();
            localMap[newX][newY].SetPosition(new Vector2D(newX, newY));
        }

        // Fill local percepts
        for (NextMapTile element : fullLocalView) {
            int newX = element.getPositionX() + vision;
            int newY = element.getPositionY() + vision;

            localMap[newX][newY] = element.Clone();
            localMap[newX][newY].SetPosition(new Vector2D(newX, newY));
        }

        //System.out.println(NextMap.MapToStringBuilder(localMap));
        // Calculate Path
        List<Action> newPath = new ArrayList<>();
        newPath = aStar.calculatePath(localMap, new Vector2D(vision, vision), target.getAdded(vision, vision), false, true, this.simStatus.GetCurrentStep());

        //System.out.println("path" + newPath);
        // Join Path
        //System.out.println("\n \n \n PATH ADAPTATION TRIGGERED \n \n \n ");

        if (newPath.isEmpty()) {
            newPath.add(NextAgentUtil.GenerateRandomMove());
            clearMapTiles(target, pathRest);
            return newPath;
        }

        newPath.addAll(pathRest);
        return newPath;

    }

    private Vector2D clearMapTiles(Vector2D startPoint, List<Action> actionList) {
        Vector2D target = new Vector2D();
        int counter = 0;
        for (Action step : actionList) {
            counter += 1;
            //System.out.println("step.getParameters()" + step.getParameters());
            if (step.getParameters().get(0).toString().contains("n")) {
                target.add(0, -1);
            }
            if (step.getParameters().get(0).toString().contains("e")) {
                target.add(1, 0);
            }
            if (step.getParameters().get(0).toString().contains("w")) {
                target.add(-1, 0);
            }
            if (step.getParameters().get(0).toString().contains("s")) {
                target.add(0, 1);
            }
            //System.out.println("Target: " + target);

            // Free MapTile
            NextMap workMap = this.agentGroup.GetGroupMap();
            int xPosition = this.GetPosition().getAdded(target).x;
            int yPosition = this.GetPosition().getAdded(target).y;

            if (xPosition > -1 && yPosition > -1 && xPosition < workMap.GetSizeOfMap().x && yPosition < workMap.GetSizeOfMap().y) {
                //System.out.print(" blocked :" + workMap.GetMapTile(this.GetPosition().getAdded(target)).CheckAtStep(this.simStatus.GetCurrentStep() + counter + 1));
                workMap.GetMapTile(this.GetPosition().getAdded(target)).ReleaseAtStep(this.simStatus.GetCurrentStep() + counter + 1);
            }

        }
        return target;
    }


    private Action selectNextAction2() {
        // -- Mögliche Action holen, um auf lokale sicht zu reagieren.
        // -- Wenn in der lokalen Sicht nichts ist, dann den normalen weg gehen
        Action possibleAction = intention.GeneratePossibleAction();
        if (possibleAction == null) {
            return selectNextAction();
        }
        return possibleAction;
    }


    /**
     * Selects the next Action with pathMemory
     *
     * @return Action
     */
    private Action selectNextAction() {
        Action nextAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);

        if (!pathMemory.isEmpty()) {
            Action currentAction = pathMemory.get(0);
            String direction = currentAction.getParameters().toString().replace("[", "").replace("]", "");

            NextMapTile thing = NextAgentUtil.IsThingInNextStep(ECardinals.valueOf(direction), agentStatus.GetFullLocalView());
            if (thing != null) // thing vor mir
            {
                if (thing.IsObstacle()) {
                    return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + thing.getPositionX()), new Identifier("" + thing.getPositionY()));
                } else if (thing.IsEntity()) {
                    Vector2D vector = NextAgentUtil.ConvertECardinalsToVector2D(ECardinals.valueOf(direction));
                    if (NextAgentUtil.IsObstacleInPosition(this.agentStatus.GetFullLocalView(), vector)) {
                        return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + vector.x), new Identifier("" + vector.y));
                    } else {
                        return NextActionWrapper.CreateAction(EActions.move, new Identifier(NextAgentUtil.NextDirection(ECardinals.valueOf(direction)).toString()));
                    }
                } else if (!thing.IsBlock()) {
                    // um Block herumlaufen
                    pathMemory = generateAlternativePathMemory(pathMemory);
                    return pathMemory.remove(0);
                } else if (thing.IsBlock()) {
                    if (!NextAgentUtil.IsBlockInPosition(thing.getPosition(), agentStatus.GetAttachedElementsVector2D())) {
                        // Block den ich seh, gehört nicht zu mir
                        return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + thing.getPositionX()), new Identifier("" + thing.getPositionY()));
                    } else {
                        return blockInFrontOfMeAction(direction);
                    }
                } else {
                    return pathMemory.remove(0);
                }
            } else {
                // Keinen Block oder 1 Block hinter mir oder naechster Schritt moeglich
                if (agentStatus.GetAttachedElementsAmount() == 0
                        || (agentStatus.GetAttachedElementsAmount() == 1
                        && NextAgentUtil.IsBlockBehindMe(ECardinals.valueOf(direction), agentStatus.GetAttachedElementsVector2D().iterator().next()))
                        || NextAgentUtil.IsNextStepPossible(ECardinals.valueOf(direction), agentStatus.GetAttachedElementsVector2D(), agentStatus.GetFullLocalView())) // no block or 1 element behind me or next Step is possible
                {
                    return pathMemory.remove(0);
                } else {
                    return blockInFrontOfMeAction(direction);
                }
            }
        }
        return nextAction;
    }

    private Action blockInFrontOfMeAction(String direction) {
        if (NextAgentUtil.IsBlockInFrontOfMe(ECardinals.valueOf(direction), agentStatus.GetAttachedElementsVector2D().iterator().next())) {
            if (!agentStatus.GetLastAction().contains("rotate")) {
                return NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw"));
            } else {
                Vector2D oppositeDirection = NextAgentUtil.GetOppositeDirectionInVector2D(ECardinals.valueOf(direction));
                pathMemory.remove(0);
                return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + oppositeDirection.x), new Identifier("" + oppositeDirection.y));
            }
        } else {
            if (NextAgentUtil.IsRotationPossible(this.agentStatus, "cw")) {
                return NextActionWrapper.CreateAction(EActions.rotate, new Identifier("cw"));
            } else if (NextAgentUtil.IsRotationPossible(this.agentStatus, "ccw")) {
                return NextActionWrapper.CreateAction(EActions.rotate, new Identifier("ccw"));
            } else {
                // Randomstep
                return new NextRandomPath().GenerateNextMove();
            }
        }
    }

    private Action generatePathMemory() {
        return intention.GeneratePathMemory();
    }

    private void clearPossibleActions() {
        intention.ClearPossibleActions();
    }

    /**
     * resets the agent between the Simulations, clears the Belief elements
     */
    private void resetAgent() {

        this.lastID = -1;
        //this.setPercepts(new ArrayList<>(), this.getPercepts());

        this.simStatus = new NextSimulationStatus();
        this.simStatus.SetActionID(lastID);
        this.agentStatus = new NextAgentStatus(this);

        this.processor = new NextPerceptReader(this);
        this.intention = new NextIntention(this);

        this.pathMemory = new ArrayList<>();
        this.activeTask = null;
        this.agentActivity = EAgentActivity.exploreMap;
        this.agentPlan = null;
        this.taskHandler = new NextTaskHandler(this);
        //this.roleToChangeTo=null;

        agentGroup = null;
        globalGroupMap = new HashMap<>();
    }

    private List<Action> calculateManhattanPath(Vector2D target) {
        List<Action> pathMemoryB;
        int targetX = target.x - GetPosition().x;
        int targetY = target.y - GetPosition().y;
        // this.say("Values path: " + targetX +" "+ targetY);
        pathMemoryB = manhattanPath.calculatePath(targetX, targetY);
        //this.say("Direct path: " + pathMemoryB.size() + " " + pathMemoryB);
        return pathMemoryB;
    }

    private void updateInternalBeliefs() {

        // update the selected Role
        updateCurrentRole();

        // Update the GroupMap
        NextMap.UpdateMap(this);

        // Update Tasks at taskPlanner
        updateTasks();
        taskHandler.SetInitialTask();
        
        // handle connect status
        if (this.agentStatus.GetLastAction().contains("connect") && this.agentStatus.GetLastActionResult().contains("success")) {
        	this.connectedToAgent = true;
            //System.out.println("Letzte ConnectAction: " + this.agentStatus.GetLastAction() + " " + this.agentStatus.GetLastActionResult());
        }
        else
        {
        	this.connectedToAgent = false;
        }
    }

    /**
     * processes the percepts provided by the server
     */
    private void processServerData() {

        for (Percept percept : getPercepts()) {

            // Process the percepts when a new simulation is started
            if (percept.getName().equals("simStart")) {
                System.out.println("simStart triggered");
                ArrayList<Percept> container = new ArrayList<>();
                container.add(percept);
                this.setPercepts(new ArrayList<>(), container);
                startTheSimulation();
            }
            // Checks if a new ActionID is found and proceeds with processing of all percepts
            if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);

                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        processor.evaluate(getPercepts(), this);
                    }
                }
            }
            // Process the percepts after current simulation is finished
            if (percept.getName().equals("simEnd")) {
                System.out.println("simEnd triggered");
                ArrayList<Percept> container = new ArrayList<>();
                container.add(percept);
                this.setPercepts(new ArrayList<>(), container);

                processor.evaluate(getPercepts(), this);
                finishTheSimulation();
            }
            //Stop processing after last Simulation
            if (percept.getName().contains("bye")) {
                //disableAgent(); //---- closing the window is disabled to keep the logdata visible.
            }            
        }
    }

    /**
     * Compares the role name stored in agentStatus and retrieves new NextRole
     * element if needed
     */
    private void updateCurrentRole() {
        if (!agentStatus.GetRole().equals(agentStatus.GetCurrentRole().GetName())) {
            for (NextRole rolle : simStatus.GetRolesList()) {
                if (rolle.GetName().equals(agentStatus.GetRole())) {
                    agentStatus.SetCurrentRole(rolle);
                    break;
                }
            }
        }
    }

    /**
     * Updates tasks in the taskPlanner and generate new plans if a new tasks
     * was generated
     */
    private void updateTasks() {
        CheckIfMaxAttemptsAreReached();
        taskHandler.UpdateTasks();
        if (agentGroup != null) {
            agentGroup.UpdateTasks(simStatus.GetTasksList(), simStatus.GetCurrentStep());
        }
    }

    /**
     * prüft, ob Task noch in der zur Verfügung stehenden Zeit gelöst werden kann
     */
    private void CheckIfMaxAttemptsAreReached() {
        if (agentStatus.GetLastAction().contains("submit") && agentStatus.GetLastActionResult().contains("failed_target"))
            failStatus += 1;
        else if (agentStatus.GetLastAction().contains("submit") && agentStatus.GetLastActionResult().contains("success"))
            failStatus = 0;

        if (failStatus == failOffest) agentGroup.SetMaxAttemptsAreReached(activeTask);

    }

    /**
     * Creation of a new group while agent initialisation
     */
    private void createGroup() {
        int groupId = globalGroupMap.size();
        this.agentGroup = new NextGroup(this, groupId);

        globalGroupMap.put(this.agentGroup.getGroupID(), this.agentGroup);

    }

    /**
     * Removes the provided group from memory
     *
     * @param groupToRemove - group to remove
     */
    public static void RemoveEmptyGroup(NextGroup groupToRemove) {
        if (groupToRemove.countAgents() == 0) {
            globalGroupMap.remove(groupToRemove.getGroupID());
        }
    }

    /**
     * Joins the provided group and the group of the agent, if provided group
     * has a lower id. Has to be executed on both agents
     *
     * @param newGroup  - new group to combine
     * @param offset    - Vector2D manhattan distance between agents
     * @param mapOffset - Vector2D manhattan distance between maps zero points
     */
    private void joinGroup(NextGroup newGroup, Vector2D offset, Vector2D mapOffset) {
        offset.add(mapOffset); // Position Agent A minus Position Agent B
        //offset.subtract(mapOffset);
        //offset.reverse();

        if (newGroup.getGroupID() < this.agentGroup.getGroupID()) {
            newGroup.AddGroup(this.agentGroup, offset);
        }
    }

    /**
     * Debugging helper - current task and selected activities
     */
    private void printActionsReport() {
        if (this.agentActivity != null) {
            System.out.println("AgentActivity: \n" + agentActivity.toString());
        }
        if (this.activeTask != null) {
            System.out.println("ActiveTask : \n" + this.GetActiveTask().GetName() + " | required Blocks: " + this.GetActiveTask().GetRequiredBlocks().size());
        }
    }

    /**
     * Debugging helper - Position, Groups, Last action
     */
    private void printFinalReport() {
        if (this.agentGroup != null) {
            this.say("Agents Group:" + agentGroup + "GroupCount " + globalGroupMap.size());
            this.say("Dispenser : " + this.GetAgentGroup().GetGroupMap().GetDispensers());
            this.say("LastAction: " + agentStatus.GetLastActionResult() + " " + agentStatus.GetLastAction() + " " + agentStatus.GetLastActionParams());
            //this.say(NextMap.MapToStringBuilder(this.agentGroup.GetGroupMap().GetMapArray(),this.agentGroup.GetAgentPositions(),this.agentGroup.GetGroupMap().GetDispenserPositions()));
            this.say("Aktuelle Position: " + this.GetPosition());
        }

    }

    /**
     * Debugging helper - current beliefs
     */
    private void printBeliefReport() {

        System.out.println("-------------------------------------------------------------");

        this.say("Local ------------------------- ");
        this.say("Goalzones: \n" + agentStatus.GetGoalZones());
        //this.say("RoleZones \n: " + agentStatus.GetRoleZones());
        this.say("Things: \n" + agentStatus.GetVisibleThings());

        this.say("Global ------------------------- ");
        this.say("Goalzones: \n" + GetMap().GetGoalZones());
        //this.say("RoleZones: \n" + map.GetRoleZones());
        this.say("Dispensers: \n" + GetMap().GetDispensers());
        System.out.println("-------------------------------------------------------------");

        if (agentGroup != null) {

            System.out.println("MAP ______________________________________ \n" + NextMap.MapToStringBuilder(agentGroup.GetGroupMap().GetMapArray()));
            this.say("Agent Position" + this.GetAgentGroup().GetAgentPosition(this).toString());

        }
    }

    private HashSet<NextMapTile> findFriendlyAgentsInLocalView() {

        Iterator<NextMapTile> visibleElements = this.agentStatus.GetVisibleThings().iterator();
        HashSet<NextMapTile> visibleEntities = new HashSet<>();
        while (visibleElements.hasNext()) {
            NextMapTile next = visibleElements.next();
            if (next.getThingType().contains(NextConstants.EVisibleThings.entity.toString())) {
                // agent is friendly and not "this" agent.    
                if (next.getThingType().substring(7).contains(agentStatus.GetTeamName())
                        && !next.GetPosition().equals(new Vector2D(0, 0))) {
                    visibleEntities.add(next);
                }
            }
        }
        return visibleEntities;
    }

    /**
     * Selects the friendly agents in the local view. Drops the agents known in
     * the group. Broadcasts 1st message for groupbuilding
     */
    private void processFriendlyAgents() {

        HashSet<NextMapTile> visibleEntities = findFriendlyAgentsInLocalView();

        if (!visibleEntities.isEmpty()) {

            HashSet<NextMapTile> newFriendlyAgents = agentGroup.removePositionsOfKnownAgents(this.GetPosition(), visibleEntities);
            for (NextMapTile newAgent : newFriendlyAgents) {

                String agentName = agentStatus.GetName().replace("agent", "");
                Vector2D foundPosition = newAgent.GetPosition();

                if (GroupBuildingSkipMemory.containsKey(agentName)) {
                    HashSet<Vector2D> skipVectorSet = GroupBuildingSkipMemory.get(agentName);
                    Iterator<Vector2D> skipVectorIt = skipVectorSet.iterator();
                    Vector2D storedVector = null;

                    while (skipVectorIt.hasNext()) {
                        Vector2D next = skipVectorIt.next();

                        if (next.x == foundPosition.x && next.y == foundPosition.y) {
                            storedVector = next;
                            break;
                        }

                    }

                    skipVectorSet.remove(storedVector);

                    GroupBuildingSkipMemory.put(agentName, skipVectorSet);

                }
                if (GroupBuildingSkipMemory.containsKey(agentName) && GroupBuildingSkipMemory.get(agentName).equals(newAgent.GetPosition())) {
                    GroupBuildingSkipMemory.get(agentName).remove(newAgent.GetPosition());
                } else {
                    this.broadcast(new Percept("AgentObserved,Step," + simStatus.GetCurrentStep() + ",X," + newAgent.getPositionX() + ",Y," + newAgent.getPositionY()), this.getName());
                }
            }

        }
    }

    /**
     * Processing of Messages in the Mailstore, used for Groupbuilding
     * initialises groupjoining process
     */
    private void processGroupJoinMessages() {
        if (this.messageStore.size() == 1) {
            System.out.println("Unexpected error in GroupJoinMessages - single object in message store");
        }

        if (this.messageStore.size() == 2) {
            for (String message : messageStore) {
                // ("JoinGroup-Execution," + this.agentGroup.getGroupID()), reciever, sender, deltaX, deltaY, mapOffsetX, mapOffsetY,)
                String[] messageContainer = message.split(",");
                lastGroupJoinAtStep = simStatus.GetCurrentStep();
                this.sendMessage(new Percept(messageContainer[0] + "," + messageContainer[1] + "," + messageContainer[4] + "," + messageContainer[5] + "," + messageContainer[6] + "," + messageContainer[7]), messageContainer[2], messageContainer[3]);
            }
        } else {
            for (String message : messageStore) {
                // ("JoinGroup-Execution," + this.agentGroup.getGroupID()), reciever, sender, deltaX, deltaY, mapOffsetX, mapOffsetY,)
                String[] messageContainer = message.split(",");
                if (!messageContainer[2].equals(messageContainer[3])) {
                    if (!GroupBuildingSkipMemory.containsKey(messageContainer[2])) {
                        GroupBuildingSkipMemory.put(messageContainer[2], new HashSet());
                    }
                    GroupBuildingSkipMemory.get(messageContainer[2]).add(new Vector2D(Integer.parseInt(messageContainer[4]), Integer.parseInt(messageContainer[5])));
                }
            }
        }
        this.messageStore.clear();
    }

    private void announceMapSizeDiscoveryStart() {
        this.broadcast(new Percept("MapSizeDiscoveryHasStarted"), this.getName());
        this.simStatus.ActivateMapSizeDiscovery();
    }

    private void announceMapHeight(int foundMapHeight) {
        this.broadcast(new Percept("MapHeightFound," + foundMapHeight), this.getName());
        SetSimulationMapHeight(foundMapHeight);
    }

    private void announceMapWidth(int foundMapWidth) {
        this.broadcast(new Percept("MapWidthFound," + foundMapWidth), this.getName());
        SetSimulationMapWidth(foundMapWidth);
    }

    private void SetSimulationMapWidth(int MapWidth) {
        // Call the map size update only for agents playing the simulation
        if (this.agentGroup != null) {
            this.agentGroup.GetGroupMap().SetSimulationMapWidth(MapWidth);
        }
    }

    private void SetSimulationMapHeight(int MapHeight) {
        // Call the map size update only for agents playing the simulation
        if (this.agentGroup != null) {
            this.agentGroup.GetGroupMap().SetSimulationMapHeight(MapHeight);
        }
    }

    /*
     * ##################### endregion private methods
     */
}
