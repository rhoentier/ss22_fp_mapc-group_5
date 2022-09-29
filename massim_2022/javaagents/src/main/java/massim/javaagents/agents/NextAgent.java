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
import massim.javaagents.pathfinding.NextPathfindingUtil;
import massim.javaagents.percept.NextRole;

/**
 * NextAgent - an agent implementation for the MASSim Scenario
 *  
 * @Author Alexander Lorenz, Miriam Wolf, Jan Steffen Jendrny
 */
public class NextAgent extends Agent {

    /*
     * ########## region fields
     */
    
    // Collection of all groups 
    public static HashMap<Integer, NextGroup> globalGroupMap = new HashMap<>();
    
    // Agents to be ignored in the group building process
    public static HashMap<String, HashSet<Vector2D>> GroupBuildingSkipMemory = new HashMap<>();
    
    // message collector for group finding process
    private final HashSet<String> messageStore;
    
    public static int lastGroupJoinAtStep = -1;             // Is used in group join process for status recognition
    private int lastID = -1;                                // Is used to compare with actionID -> new Step Recognition

    //Agent related attributes
    private NextAgentStatus agentStatus;                    // collection of agent related status values   
    private NextGroup agentGroup;                           // collection of agents in the group
    private List<Action> pathMemory = new ArrayList<>();    // storing of movement actions
    private Vector2D goToPosition = new Vector2D();         // current target point

    //Simulation related attributes
    private NextSimulationStatus simStatus;                 // collection of simulation related status values


    // --- Algorithms ---
    NextPerceptReader processor;                            // Eismassim interpreter
    // Pathfinding algorithms
    private final NextManhattanPath manhattanPath = new NextManhattanPath();    // Manhattan Distance based path generation
    private final NextAStarPath aStar = new NextAStarPath();                    // A*Star based path gemeration

    //BDI
    private NextIntention intention;
    private NextTaskHandler taskHandler;

    // Tasks
    private NextTask activeTask = null;
    private EAgentActivity agentActivity;
    private NextPlan agentPlan;

    // Counter for successful submits for a single task
    private int failStatus = 0;
    private int solvedTasks = 0;

    private boolean correctPosition = false;
    private boolean connectedToAgent = false;

    /*
     * ##################### endregion fields
     */
    
    /**
     * ########## region constructor.
     *
     * @param name      the agent's name
     * @param mailbox   the mail facility
     */
    public NextAgent(String name, MailService mailbox) {
        super(name, mailbox);
        this.messageStore = new HashSet<>();
        this.agentStatus = new NextAgentStatus(this);
        this.simStatus = new NextSimulationStatus();
        this.intention = new NextIntention(this);
        this.processor = new NextPerceptReader(this);

        taskHandler = new NextTaskHandler(this);
        this.SetGoToPosition(new Vector2D(0, 0));
    }

    /*
     * ##################### endregion constructor
     */

    /*
     * ########## region public methods
     */
    
    //Original Method implemented from Agent.java
    @Override
    public void handlePercept(Percept percept) { }

    // Original Method implemented from Agent.java
    @Override
    public void handleMessage(Percept message, String sender) {
        //this.say("Message: " + message.toProlog() + " from Sender: " + sender);

        String[] messageContainer = message.toString().split(",");

        //-- GroupBuilding start
        
        // Message Type: AgentObserved,Step,6,X,0,Y,3
        if (messageContainer[0].contains("AgentObserved")) {
            if (!(this.simStatus.GetCurrentStep() == null) && (Integer.parseInt(messageContainer[2]) > 2)) {
                if (this.simStatus.GetCurrentStep() == Integer.parseInt(messageContainer[2])) {     // prevents agents with future set of data to be involved 
                    // Check for agent on opposite position
                    int xToTest = -1 * Integer.parseInt(messageContainer[4]);
                    int yToTest = -1 * Integer.parseInt(messageContainer[6]);
                    for (NextMapTile feld : this.agentStatus.GetVisibleThings()) {
                        if (feld.GetPositionX() == xToTest && feld.GetPositionY() == yToTest) {
                            if (feld.GetThingType().contains(this.agentStatus.GetTeamName())
                                    && feld.GetThingType().contains(NextConstants.EVisibleThings.entity.toString())) {
                                // send a response message
                                this.sendMessage(new Percept("GroupFinding-ResponseMessage," + this.agentGroup.GetGroupID() + "," + xToTest + "," + yToTest + ", MapPosition," + this.GetPosition().x + "," + this.GetPosition().y), sender, this.getName());
                            }
                        }
                    }
                }
            }
        }

        // Message Type: AO-ResponseMessage,GroupID,x,y,MapPosition,x,y
        if (messageContainer[0].contains("GroupFinding-ResponseMessage")) {
            //calculate offset
            int mapOffsetX = Integer.parseInt(messageContainer[5]) - this.GetPosition().x;
            int mapOffsetY = Integer.parseInt(messageContainer[6]) - this.GetPosition().y;
            // send 2 Join Group Messages with sending agent and responding agent as main agent.
            messageStore.add(new Percept("JoinGroup-Execution," + this.agentGroup.GetGroupID()) + "," + sender + "," + this.getName() + "," + messageContainer[2] + "," + messageContainer[3] + "," + mapOffsetX + "," + mapOffsetY);
            messageStore.add(new Percept("JoinGroup-Execution," + messageContainer[1]) + "," + this.getName() + "," + this.getName() + "," + (-1 * Integer.parseInt(messageContainer[2])) + "," + (-1 * Integer.parseInt(messageContainer[3]) + "," + (-1 * mapOffsetX) + "," + (-1 * mapOffsetY)));
        }

        // Message Type: JoinGroup-Execution,GroupID,x,y,MapOffsetX,MapOffsetY
        if (messageContainer[0].contains("JoinGroup-Execution")) {
            // this.say("JoinGroup-EXECUTION " + messageContainer[1] + " X: " + messageContainer[2] + " Y: " + messageContainer[3] + " mapOffsetX: " + messageContainer[4] + " mapOffsetY: " + messageContainer[5]);
            NextGroup target = globalGroupMap.get(Integer.parseInt(messageContainer[1]));
            // execute group joining
            if (target != null) {
                joinGroup(target, new Vector2D(Integer.parseInt(messageContainer[2]), Integer.parseInt(messageContainer[3])), new Vector2D(Integer.parseInt(messageContainer[4]), Integer.parseInt(messageContainer[5])));
            }
        }
        //-- GroupBuilding end
        
        //-- MapSizeDicovery start
        // react to start of MapSizeDiscovery
        if (messageContainer[0].contains("MapSizeDiscoveryHasStarted")) {
            this.simStatus.ActivateMapSizeDiscovery();
        }
        // react to aborting of MapSizeDiscovery
        if (messageContainer[0].contains("MapSizeDiscoveryAborted")) {
            this.simStatus.ResetMapSizeDiscovery();
        }
        // react to found MapHeight
        if (messageContainer[0].contains("MapHeightFound")) {
            this.SetSimulationMapHeight(Integer.parseInt(messageContainer[1]));
        }
        // react to found MapWidth
        if (messageContainer[0].contains("MapWidthFound")) {
            this.SetSimulationMapWidth(Integer.parseInt(messageContainer[1]));
        }

        //-- MapSizeDicovery end
    }

    /**
     * Main agent logic - implemented from Agent.java
     *
     * @return Action - Next action for Massim simulation for this agent.
     */
    @Override
    public Action step() {

        long startTime = Instant.now().toEpochMilli();      // evaluate step lenght

        // Initialise a group if empty
        if (agentGroup == null) {
            createGroup();
        }

        // delayed GroupJoining process, to spread agents
        if (lastID > 3 && lastGroupJoinAtStep != simStatus.GetCurrentStep()) {
            // Check if friendly Agents are visible and join them to groups
            // Map Information of the last step is used to ensure consistency of data
            processFriendlyAgents();
            processGroupJoinMessages();
        }

        processServerData();

        // ActionGeneration is started on a new ActionID only
        if (simStatus.GetActionID() > lastID) {
            lastID = simStatus.GetActionID();

            updateInternalBeliefs();
            //printBeliefReport(); // live String output to console

            clearPossibleActions();

            // new path
            if (agentGroup != null) {
                if (solvedTasks >= 1 || agentGroup.IsDeadlineReached(GetActiveTask())
                        || agentActivity.equals(EAgentActivity.cleanMap)) {
                    solvedTasks = 0;
                    failStatus = 0;
                    NextAgentPlan groupPlan = agentGroup.GetPlan(this);
                    taskHandler.SetAgentPlan(groupPlan);
                }
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

            
            Action nextAction = NextAgentUtil.GenerateRandomMove();
            
            nextAction = generatePathMemory();
            if (nextAction == null) {
                // Weg generiert - aktuelle Action auswählen
                nextAction = selectNextAction2();
            }

            // Clears StepMemory if walking was interrupted.
            if (!nextAction.getName().contains("move")) {
                this.ClearAgentStepMemory();
            }

            if (this.agentStatus.GetLastActionResult().contains("fail")) {
                System.out.println("Letzte FailedAction: " + this.agentStatus.GetLastAction() + " " + this.agentStatus.GetLastActionResult());
            }

            //printActionsReport();         // live String output to console
            //printBlockedStepsReport();    // live String output to console
            //printFinalReport();           // live String output to console
            //System.out.println("Used time: " + (Instant.now().toEpochMilli() - startTime) + " ms"); // Calculation Time report

            return nextAction;

        }
        return null;

    }

    /**
     * Getter for local NextAgentStatus
     *
     * @return NextAgentStatus collection of agent related status values   
     */
    public NextAgentStatus GetAgentStatus() {
        return this.agentStatus;
    }

    /**
     * Getter for local NextSimulationStatus
     *
     * @return NextSimulationStatus collection of agents in the group
     */
    public NextSimulationStatus GetSimulationStatus() {
        return simStatus;
    }

    /**
     * Getter for currently active Task
     * 
     * @return NextTask currently active Task
     */
    public NextTask GetActiveTask() {
        return this.activeTask;
    }

    /**
     * Getter for agent´s current group
     * 
     * @return NextGroup agent joined to 
     */
    public NextGroup GetAgentGroup() {
        if(agentGroup!= null){
            return agentGroup;
        }
        return new NextGroup(this, -1);
    }

    /**
     * Specifies agent´s new group
     * 
     * @param agentGroup NextGroup for the agent to join
     */
    public void SetAgentGroup(NextGroup agentGroup) {
        this.agentGroup = agentGroup;
    }

    /**
     * Setter for active task
     * 
     * @param activeTask NextTask to set active
     */
    public void SetActiveTask(NextTask activeTask) {
        this.activeTask = activeTask;
    }

    /**
     * Getter for agent activity
     * 
     * @return EAgentActivity
     */
    public EAgentActivity GetAgentActivity() {
        return this.agentActivity;
    }

    /**
     * Setter for agent activity
     * 
     * @param agentTask EAgentActivity to specify task  
     */
    public void SetAgentActivity(EAgentActivity agentTask) {
        this.agentActivity = agentTask;
    }

    /**
     * Setter for agent plan
     * 
     * @param agentPlan 
     */
    public void SetAgentPlan(NextPlan agentPlan) {
        this.agentPlan = agentPlan;
    }

    /**
     * Retrieves a collection of actions created by patfinding to perform an action
     * 
     * @return Action List of tasks to be performed by the agent 
     */
    public List<Action> GetPathMemory() {
        return this.pathMemory;
    }

    /**
     * Specifies a collection of actions created by patfinding to perform an action
     * 
     * @param pathMemory Action List of tasks to be performed by the agent 
     */
    public void SetPathMemory(List<Action> pathMemory) {
        this.pathMemory = pathMemory;
    }

    /**
     *  Empty the path memory
     */
    public void ClearPathMemory() {
        this.pathMemory = new ArrayList<>();
    }

    /**
     * Setter for correctPosition
     * 
     * @param correctPosition boolean
     */
    public void SetCorrectPosition(boolean correctPosition) {
        this.correctPosition = correctPosition;
    }

    /**
     * Getter for correctPosition
     * 
     * @return boolean
     */
    public boolean GetCorrectPosition() {
        return correctPosition;
    }

    /**
     * Getter for connectedToAgent
     * 
     * @return boolean
     */
    public boolean GetConnectedToAgent() {
        return connectedToAgent;
    }
    
    /**
     * Retrieve agents position from the group as a copy
     * 
     * @return Vector2D with XY coordinates
     */
    public Vector2D GetPosition() {
        if (agentGroup == null) {
            return new Vector2D(0, 0);
        }
        return agentGroup.GetAgentPosition(this).clone();
    }

    /**
     * Retrieve agents position from the group as a reference
     * 
     * @return Vector2D with XY coordinates as a reference
     */
    public Vector2D GetPositionRef() {
        if (agentGroup == null) {
            return new Vector2D(0, 0);
        }
        return agentGroup.GetAgentPosition(this);
    }

    /**
     * Retrieve the map of agent´s group
     * 
     * @return NextMap with current group data
     */
    public NextMap GetMap() {
        if (this.agentGroup == null) {      // Workaround for deep linking
            return new NextMap(this);
        }
        return this.agentGroup.GetGroupMap();
    }

    /**
     * Move agents position
     * 
     * @param vector Vector2D XY offset to move by
     */
    public void MovePosition(Vector2D vector) {
        this.agentGroup.MoveSingleAgent(this, vector);
    }

    /**
     * Perform a mod operation on agents position
     */
    public void ModPosition() {
        this.agentGroup.ModSingleAgent(this);
    }

    /**
     * amount of blocks agent is able to carry
     * 
     * @return int amount of blocks
     */
    public int GetCarryableBlocks() {
        return (int) agentStatus.GetCurrentRole().GetSpeed().stream().filter(speed -> speed > 0).count();
    }

    /**
     * Getter for taskHandler
     * @return taskHandler
     */
    public NextTaskHandler GetTaskHandler() {
        return this.taskHandler;
    }

    /**
     * Getter for agentPlan
     * @return agentPlan
     */
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
        
        //NextMap map = GetMap();
        this.SetGoToPosition(target);                   // Specify target as helper for recalculation
        NextMap map = this.agentGroup.GetGroupMap();    // store map
        Boolean targetIsOnMap = map.IsOnMap(target);    // check if target is accessable
        try {
            if (targetIsOnMap && !map.GetMapArray()[target.x][target.y].GetThingType().equals("unknown")) {
                List<Action> pathMemoryA;
                pathMemoryA = aStar.CalculatePath(map.GetMapArray(), GetPosition(), target, this.simStatus.GetCurrentStep());
                //this.say("A* path:" + pathMemoryA);
                if (pathMemoryA.isEmpty()) {
                    // Fallback:
                    // In case the way is blocked and it is impossible to get there with A*
                    return calculateManhattanPath(target);
                }
                return pathMemoryA;

            } else {
                return calculateManhattanPath(target); // Fallback
            }
        } catch (Exception e) {
            this.say("Path generation failed: " + e);
        }
        return null;
    }

    /**
     * Calculate Path to the Target, ending on a free Tile next to it
     *
     * @param target Vector2D position to walk to ( dispenser )
     * @return List PathMemory to arrive at adjacent point
     */
    public List<Action> CalculatePathNextToTarget(Vector2D target) {

        NextMap map = this.agentGroup.GetGroupMap();
        //Select position next to the target
        try {
            if (map.GetMapArray()[target.x + 1][target.y].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x + 1, target.y));
            }
            if (map.GetMapArray()[target.x][target.y + 1].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x, target.y + 1));
            }
            if (map.GetMapArray()[target.x - 1][target.y].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x - 1, target.y));
            }
            if (map.GetMapArray()[target.x][target.y - 1].IsWalkableStrict()) {
                return CalculatePath(new Vector2D(target.x, target.y - 1));
            }
        } catch (Exception e) {
            this.say("CalculatePathNextToTarget:" + e);
        }
        // Calculate path to new target
        return CalculatePath(new Vector2D(target.x, target.y));
    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases.
     *
     * @param message - String based message
     */
    public void TellGroup(String message) {
        this.agentGroup.TellGroup(message, this);
    }

    /**
     * String-based communication with groupagents to be extended for further
     * usecases.
     *
     * @param message - String based message
     * @param targetAgent - Name of the targeted agent
     */
    public void TellGroupAgent(String message, String targetAgent) {
        this.agentGroup.TellGroupAgent(message, targetAgent, this);
    }

    /**
     * Handling of custom groupmessages
     *
     * @param message - String based message
     * @param sourceAgent - Name of the calling agent
     */
    public void HandleGroupMessage(String message, String sourceAgent) {

        if (message.equals("JUNIT TEST")) {
            this.GetAgentStatus().SetName("JUNIT TEST");
        }

        // definitive implementation needed
    }

    /**
     * Clears the occupied MapTiles in case of an error in movement
     */
    public void ClearAgentStepMemory() {
        Vector2D startPoint = this.GetPosition();
        char[] lastAction = this.agentStatus.GetLastActionParams().toCharArray();

        //find the supposed position after successful movement
        for (Character step : lastAction) {
            if (step.equals('n')) {
                startPoint.Add(0, -1);
            }
            if (step.equals('e')) {
                startPoint.Add(1, 0);
            }
            if (step.equals('w')) {
                startPoint.Add(-1, 0);
            }
            if (step.equals('s')) {
                startPoint.Add(0, 1);
            }
        }
        // clear MapTiles blocked by 
        clearMapTiles(startPoint, pathMemory);
    }

    /**
     * Calculate the distances to the dispensers
     * 
     * @param requiredBlocks NextMapTile HashSet with required blocks
     * @return NextMapTile to Integer Hashmap storing values with distance
     */
    public HashMap<NextMapTile, Integer> GetDispenserDistances(HashSet<NextMapTile> requiredBlocks) {
        HashMap<NextMapTile, Integer> distances = new HashMap<>();
        for (NextMapTile requiredBlock : requiredBlocks) {
            HashSet<NextMapTile> attachedElements = agentStatus.GetAttachedElementsNextMapTiles();
            boolean hasCorrectBlockAttached = false;
            for (NextMapTile attachedElement : attachedElements) {
                if (attachedElement.GetThingType().contains(requiredBlock.GetThingType())) {
                    hasCorrectBlockAttached = true;
                }
            }
            if (hasCorrectBlockAttached) {
                distances.put(requiredBlock, 0);
                continue;
            }
            NextMapTile nearestDispenser = NextAgentUtil.GetNearestDispenserFromType(GetMap().GetDispensers(), requiredBlock.GetThingType(), GetPosition());
            if (nearestDispenser == null) {
                distances.put(requiredBlock, 1000);
                continue;
            }
            distances.put(requiredBlock, aStar.CalculatePath(GetMap().GetMapArray(), GetPosition(), nearestDispenser.GetPosition(), simStatus.GetCurrentStep()).size());
        }
        return distances;
    }

    /**
     * Compare the agent activities
     * 
     * @param activity
     * @return 
     */
    public boolean IsAgentActivity(EAgentActivity activity) {
        return this.agentActivity.equals(activity);
    }

    /**
     * Getter for goToPosition
     * 
     * @return Vector2D goToPosition
     */
    public Vector2D GetGoToPosition() {
        return goToPosition;
    }

    /**
     * Setter for goToPosition
     * 
     * @param goToPosition Vector2D
     */
    public final void SetGoToPosition(Vector2D goToPosition) {
        this.goToPosition = goToPosition;
    }

    /**
     * Counts all Groups created by NextAgent
     *
     * @return int the amount of available groups
     */
    public int CountAllGroups() {
        return globalGroupMap.size();
    }

    /**
     * Removes the provided group from memory if empty
     *
     * @param groupToRemove - group to remove
     */
    public static void RemoveEmptyGroup(NextGroup groupToRemove) {
        if (groupToRemove.CountAgents() == 0) {
            globalGroupMap.remove(groupToRemove.GetGroupID());
        }
    }

    /*
     * ##################### endregion public methods
     */
    
    //--------------------------------------------------------------------------
    
    /*
     * ########## region private methods
     */
    
    /**
     * Calculate distance between two cells using Manhattan or A*JPS if 
     * applicable
     *
     * @param startPosition Vector2D Start of calculation
     * @param targetPosition Vector2D Targetof calculation
     * @return int distance between the points using Manhattan or A*
     */
    private int calculateDistance(Vector2D startPosition, Vector2D targetPosition) {
        return NextPathfindingUtil.CalculateDistance(this.GetMap(), startPosition, targetPosition);
    }

    /**
     *  Reset Agent
     */
    private void resetAfterInactiveTask() {
        this.SetActiveTask(null);
        this.clearPossibleActions();
        this.ClearPathMemory();
    }

    /**
     * Stops the Agent. Closes the agent´s window
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
        this.agentGroup = null;
        this.say("Finishing this Simulation!");
        this.say("Result: #" + simStatus.GetRanking());

    }

    /**
     * The path is optimised using local percepts. Reactive behavior.
     *
     * @param currentPathMemory recieve current pathMemory
     * @return adjustedPathMemory pathMemory with adjusted route
     * 
     * @author Alexander Lorenz
     */
    private List<Action> generateAlternativePathMemory(List<Action> currentPathMemory) {

        List<Action> localPathMemory = currentPathMemory;
        int vision = this.agentStatus.GetCurrentRole().GetVision();
        int reach = vision - 1;
        HashSet<NextMapTile> fullLocalView = this.agentStatus.GetFullLocalView();

        //Clear step memory
        clearMapTiles(this.GetPosition(), currentPathMemory);
        
        List<Action> localPath;
        List<Action> pathRest = new ArrayList<>();
        
        //Divide path memory
        if (localPathMemory.size() > reach) {
            localPath = localPathMemory.subList(0, reach);
            pathRest = localPathMemory.subList(reach, localPathMemory.size());
        } else {
            localPath = localPathMemory;
        }

        // Convert local Path to Target Cell and clear Path in between
        Vector2D target = clearMapTiles(this.GetPosition(), localPath);

        //Build local map for calculation
        NextMapTile[][] localMap = new NextMapTile[2 * vision + 1][2 * vision + 1];

        for (int x = 0; x < localMap.length; x++) {
            for (int y = 0; y < localMap[0].length; y++) {
                localMap[x][y] = new NextMapTile(x, y, 0, "unknown");
            }
        }
        // fill visible area
        HashSet<NextMapTile> emptyVision = new HashSet<>();

        for (int i = -1 * vision; i <= vision; i++) {
            for (int j = -1 * vision; j <= vision; j++) {
                if (Math.abs(i) + Math.abs(j) <= vision) {
                    emptyVision.add(new NextMapTile(i, j, this.simStatus.GetCurrentStep()));
                }
            }
        }

        for (NextMapTile element : emptyVision) {
            int newX = element.GetPositionX() + vision;
            int newY = element.GetPositionY() + vision;

            localMap[newX][newY] = element.Clone();
            localMap[newX][newY].SetPosition(new Vector2D(newX, newY));
        }

        // Fill local percepts
        for (NextMapTile element : fullLocalView) {
            int newX = element.GetPositionX() + vision;
            int newY = element.GetPositionY() + vision;

            localMap[newX][newY] = element.Clone();
            localMap[newX][newY].SetPosition(new Vector2D(newX, newY));
        }
        
        // Calculate Path
        List<Action> newPath = aStar.CalculatePath(localMap, new Vector2D(vision, vision), target.GetAdded(vision, vision), false, true, this.simStatus.GetCurrentStep());
        
        // Fallback with full recalculation using strict rules and full map in case lokal pathfinding failed
        if (newPath.isEmpty()) {
            //newPath = CalculatePath(goToPosition);
            newPath = aStar.CalculatePath(this.GetMap().GetMapArray(), GetPosition(), target, false, true, this.simStatus.GetCurrentStep());
            // random move as last fallback     
            if (newPath.isEmpty()) {
                newPath.add(NextAgentUtil.GenerateRandomMove());
            }
            return newPath;
        }

        // Join Path 
        newPath.addAll(pathRest); 
        return newPath;

    }
    
    /**
     * Clear stepMemory between start and endpoint
     * 
     * @param startPoint Vector2D startpoint of the path
     * @param actionList ActionList describing the path
     * @return last point of the path
     */

    private Vector2D clearMapTiles(Vector2D startPoint, List<Action> actionList) {
        Vector2D target = new Vector2D();                   // virtual point on the map
        int counter = 0;                                    // counter for step specification
        NextMap workMap = this.GetAgentGroup().GetGroupMap();    // Map to adjust
        for (Action step : actionList) {
            counter += 1;
            // Fix for different parameter values in tests and simulation
            String[] values = step.getParameters().get(0).toString().split("\"");
            String direction;
            if (values.length == 1) {
                direction = values[0];
            } else {
                direction = values[1];
            }

            //calculate the offset
            if (direction.equals("n")) {
                target.Add(0, -1);
            }
            if (direction.equals("e")) {
                target.Add(1, 0);
            }
            if (direction.equals("w")) {
                target.Add(-1, 0);
            }
            if (direction.equals("s")) {
                target.Add(0, 1);
            }

            // Free the MapTile under the virtual point
            int xPosition = this.GetPosition().GetAdded(target).x;
            int yPosition = this.GetPosition().GetAdded(target).y;
            if (xPosition > -1 && yPosition > -1 && xPosition < workMap.GetSizeOfMap().x && yPosition < workMap.GetSizeOfMap().y) {

                workMap.GetMapTile(new Vector2D(xPosition, yPosition)).ReleaseAtStep(this.simStatus.GetCurrentStep() + counter);
            }

        }
        return target;
    }

    /**
     * Selects the next Action with pathMemory - alternative Version
     *
     * @return Action
     */
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
                    return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + thing.GetPositionX()), new Identifier("" + thing.GetPositionY()));
                } else if (thing.IsEntity()) {
                    //pathMemory = generateAlternativePathMemory(pathMemory);
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
                        return NextActionWrapper.CreateAction(EActions.clear, new Identifier("" + thing.GetPositionX()), new Identifier("" + thing.GetPositionY()));
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

    /**
     * Calculate reaction in case of a block in the way
     * @param direction
     * @return Action to perform
     */
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
            } else if (this.agentStatus.GetLastActionParams().contains("clear") && !this.agentStatus.GetLastActionResult().contains("fail")) {
                Vector2D dir = NextAgentUtil.ConvertECardinalsToVector2D(ECardinals.valueOf(direction));
                return NextActionWrapper.CreateAction(EActions.clear,
                        new Identifier("" + dir.x),
                        new Identifier("" + dir.y));
            } else {
                // Randomstep
                return new NextRandomPath().GenerateNextMove();
            }
        }
    }

    /**
     * GeneratePathMemory
     * 
     * @return Action
     */
    private Action generatePathMemory() {
        return intention.GeneratePathMemory();
    }

    /**
     *  clearPossibleActions
     */
    private void clearPossibleActions() {
        intention.ClearPossibleActions();
    }

    /**
     * resets the agent between the Simulations, clears the belief elements
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

    /**
     * calculate path using ManhattanPath
     * @param target Vector2D 
     * @return Action List describing a path
     */
    private List<Action> calculateManhattanPath(Vector2D target) {
        List<Action> pathMemoryB;
        int targetX = target.x - GetPosition().x;
        int targetY = target.y - GetPosition().y;
        pathMemoryB = manhattanPath.CalculatePath(targetX, targetY);
        return pathMemoryB;
    }

    /**
     * Update the internal state after processing of percept messages
     */
    private void updateInternalBeliefs() {

        // update the selected Role
        updateCurrentRole();

        // Update the GroupMap
        if (this.agentGroup != null) {
            NextMap.UpdateMap(this);
        }

        // Update Tasks at taskPlanner
        updateTasks();
        taskHandler.SetInitialTask();

        // handle connect status
        if (this.agentStatus.GetLastAction().contains("connect") && this.agentStatus.GetLastActionResult().contains("success")) {
            this.connectedToAgent = true;
            //System.out.println("Letzte ConnectAction: " + this.agentStatus.GetLastAction() + " " + this.agentStatus.GetLastActionResult());
        } else {
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
                        processor.Evaluate(getPercepts());
                    }
                }
            }
            // Process the percepts after current simulation is finished
            if (percept.getName().equals("simEnd")) {
                System.out.println("simEnd triggered");
                ArrayList<Percept> container = new ArrayList<>();
                container.add(percept);
                this.setPercepts(new ArrayList<>(), container);

                processor.Evaluate(getPercepts());
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
        checkIfMaxAttemptsAreReached();
        taskHandler.UpdateTasks();
        if (agentGroup != null) {
            agentGroup.UpdateTasks(simStatus.GetTasksList(), simStatus.GetCurrentStep());
        }
    }

    /**
     * Checks whether task can still be solved in the time available
     */
    private void checkIfMaxAttemptsAreReached() {
        if (agentStatus.GetLastAction().contains("submit") && agentStatus.GetLastActionResult().contains("failed_target")) {
            failStatus += 1;
        } else if (agentStatus.GetLastAction().contains("submit") && agentStatus.GetLastActionResult().contains("success")) {
            failStatus = 0;
            solvedTasks += 1;
        }

        int failOffset = 2;
        if (failStatus == failOffset) {
            agentGroup.SetMaxAttemptsAreReached(activeTask);
        }
    }

    /**
     * Creation of a new group while agent initialisation
     */
    private void createGroup() {
        int groupId = CountAllGroups();
        this.agentGroup = new NextGroup(this, groupId);

        globalGroupMap.put(this.agentGroup.GetGroupID(), this.agentGroup);

    }

    /**
     * Joins the provided group and the group of the agent, if provided group
     * has a lower id. Has to be executed on both agents
     *
     * @param newGroup - new group to combine
     * @param offset - Vector2D manhattan Distance between agents
     * @param mapOffset - Vector2D manhattan Distance between maps zero points
     */
    private void joinGroup(NextGroup newGroup, Vector2D offset, Vector2D mapOffset) {
        offset.Add(mapOffset); // Position Agent A minus Position Agent B

        //Join groups only if basegroup ID is smaller 
        if (newGroup.GetGroupID() < this.agentGroup.GetGroupID()) {
            newGroup.AddGroup(this.agentGroup, offset);
        }
    }

    /**
     * Debugging helper - live report - current task and selected activities
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
     * Debugging helper - live report - Position, Groups, Last action
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
     * Debugging helper - live report -current beliefs
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

    /**
     * Debugging helper - live report - prints blocked steps for a tile
     */
    private void printBlockedStepsReport() {

        if (agentGroup != null) {
            this.say("Current tile was blocked: " + this.agentGroup.GetGroupMap().GetMapTile(this.GetPosition()).CheckAtStep(this.simStatus.GetCurrentStep()));
            this.say("Blocked Steps " + this.agentGroup.GetGroupMap().GetMapTile(this.GetPosition()).ReportBlockedSteps());
            this.say("Current Step " + this.simStatus.GetCurrentStep());
        }
    }

    /**
     * Check the local view for the presence of friendly agents
     * 
     * @return NextMapTile HashSet containing friendly agents
     */
    private HashSet<NextMapTile> findFriendlyAgentsInLocalView() {

        Iterator<NextMapTile> visibleElements = this.agentStatus.GetVisibleThings().iterator();
        HashSet<NextMapTile> visibleEntities = new HashSet<>();
        while (visibleElements.hasNext()) {
            NextMapTile next = visibleElements.next();
            if (next.GetThingType().contains(NextConstants.EVisibleThings.entity.toString())) {
                // agent is friendly and not "this" agent.    
                if (next.GetThingType().substring(7).contains(agentStatus.GetTeamName())
                        && !next.GetPosition().equals(new Vector2D(0, 0))) {
                    visibleEntities.add(next);
                }
            }
        }
        return visibleEntities;
    }

    /**
     * Selects the friendly agents in the local view. 
     * Drops the agents known to the group. 
     * 
     * Broadcasts 1st message for group building.
     */
    private void processFriendlyAgents() {

        //Collect visible friendly agents
        HashSet<NextMapTile> visibleEntities = findFriendlyAgentsInLocalView();

        // Visible agents found
        if (!visibleEntities.isEmpty()) {

            // Remove agents known to the group
            HashSet<NextMapTile> newFriendlyAgents = agentGroup.RemovePositionsOfKnownAgents(this.GetPosition(), visibleEntities);
            
            //For every new visible Agent
            for (NextMapTile newAgent : newFriendlyAgents) {

                // get agent´s name for communication
                String agentName = agentStatus.GetName().replace("agent", "");

                //Check if the position of the found agent was blocked in GroupBuildingSkipMemory
                if (GroupBuildingSkipMemory.containsKey(agentName) && GroupBuildingSkipMemory.get(agentName).contains(newAgent.GetPosition())) {
                    // Clear the position of the found agent in GroupBuildingSkipMemory
                    GroupBuildingSkipMemory.get(agentName).remove(newAgent.GetPosition());
                } else {
                    // Start the group joining process and broadcast the first message.
                    this.broadcast(new Percept("AgentObserved,Step," + simStatus.GetCurrentStep() + ",X," + newAgent.GetPositionX() + ",Y," + newAgent.GetPositionY()), this.getName());
                }
            }
        }
    }

    /**
     * Processing of Messages in the Mailstore, used for Groupbuilding
     * second step in the group joining process
     */
    private void processGroupJoinMessages() {
        
        // This case should never happen
        if (this.messageStore.size() == 1) {
            System.out.println("Unexpected error in GroupJoinMessages - single object in message store");
        }
        // successfull identification for 2 agents
        if (this.messageStore.size() == 2) {
            for (String message : messageStore) {
                // ("JoinGroup-Execution," + this.agentGroup.GetGroupID()), reciever, sender, deltaX, deltaY, mapOffsetX, mapOffsetY,)
                String[] messageContainer = message.split(",");
                lastGroupJoinAtStep = simStatus.GetCurrentStep();
                // Initialise joining
                this.sendMessage(new Percept(messageContainer[0] + "," + messageContainer[1] + "," + messageContainer[4] + "," + messageContainer[5] + "," + messageContainer[6] + "," + messageContainer[7]), messageContainer[2], messageContainer[3]);
            }
        } else {
            // to many possible fits. All candidates are blocked for the round.
            for (String message : messageStore) {
                // ("JoinGroup-Execution," + this.agentGroup.GetGroupID()), reciever, sender, deltaX, deltaY, mapOffsetX, mapOffsetY,)
                String[] messageContainer = message.split(",");
                if (!messageContainer[2].equals(messageContainer[3])) {
                    // Initialise the communication partner in the GroupBuildingSkipMemory
                    if (!GroupBuildingSkipMemory.containsKey(messageContainer[2])) {
                        GroupBuildingSkipMemory.put(messageContainer[2], new HashSet());
                    }
                    //block the position of the current agent for the communication partner
                    GroupBuildingSkipMemory.get(messageContainer[2]).add(new Vector2D(Integer.parseInt(messageContainer[4]), Integer.parseInt(messageContainer[5])));
                }
            }
        }
        this.messageStore.clear();
    }

    /**
     * Uses the MASSim broadcast communication to announce 
     * the start of the map size discovery to all agents
     */
    private void announceMapSizeDiscoveryStart() {
        this.broadcast(new Percept("MapSizeDiscoveryHasStarted"), this.getName());
        this.simStatus.ActivateMapSizeDiscovery();
    }
    
    /**
     * Uses the MASSim broadcast to communicate the map height to all agents
     *
     * @param foundMapHeight int dimension
     */
    private void announceMapHeight(int foundMapHeight) {
        this.broadcast(new Percept("MapHeightFound," + foundMapHeight), this.getName());
        SetSimulationMapHeight(foundMapHeight);
    }
    
    /**
     * Uses the MASSim broadcast to communicate the map width to all agents
     *
     * @param foundMapWidth int dimension
     */
    private void announceMapWidth(int foundMapWidth) {
        this.broadcast(new Percept("MapWidthFound," + foundMapWidth), this.getName());
        SetSimulationMapWidth(foundMapWidth);
    }

    /**
     * Set the width dimension to the provided value 
     * @param MapWidth int dimension
     */
    private void SetSimulationMapWidth(int MapWidth) {
        // Call the map size update only for agents playing the simulation
        if (this.agentGroup != null) {
            this.agentGroup.GetGroupMap().SetSimulationMapWidth(MapWidth);
        }
    }

    /**
     * Set the height dimension to the provided value 
     * @param MapHeight int dimension
     */
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
