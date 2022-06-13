package massim.javaagents.agents;

import massim.javaagents.intention.NextIntention;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.NextMapUtil;
import eis.iilang.*;

import java.util.ArrayList;

import massim.javaagents.MailService;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.timeMonitor.NextTimeMonitor;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.PathfindingConfig;
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
 * Done: Handling of transition between simulations Basic action generation
 * based on random movement Processing of all percepts and storing in dataVaults
 * <p>
 * ToDo: Gruppenbildung
 *
 * @author Alexander Lorenz
 */
public class NextAgent extends Agent {

    /*
     * ########## region fields
     */
    private int lastID = -1;        // Is used to compare with actionID -> new Step Recognition
    private Boolean actionRequestActive = false; // Todo: implement reaction to True if needed. Is activated, before next Step.
    private Boolean disableAgentFlag = false; // True when all Simulations are finished 

    //Agent related attributes
    private NextAgentStatus agentStatus;
    //Simulation related attributes
    private NextSimulationStatus simStatus;

    //Compilation of finished Simulations to be Processed after "deactivateAgentFlag == True"
    private List<NextSimulationStatus> finishedSimulations = new ArrayList<>();

    private NextTimeMonitor timeMonitor;

    //BDI
    private NextIntention intention;

    // --- Algorithms ---
    NextPerceptReader processor; // Eismassim interpreter

    // Pathfinding algorithm
    //PathfindingConfig pathfindingConfig;
    private NextManhattanPath manhattanPath = new NextManhattanPath(); // Manhattan distance based path generation
    private NextAStarPath aStar = new NextAStarPath();  // A*Star based path gemeration
    private List<Action> pathMemory = new ArrayList<>();    // storing 

    // Map
    private Vector2D position; // Position on the map relative to the starting point
    private NextMap map;

    // Tasks
    private NextTask activeTask = null;
    private EAgentTask agentTask;
    
    /*
     * ##################### endregion fields
     */
    /**
     * ########## region constructor.
     *
     * @param name the agent's name
     * @param mailbox the mail facility
     */
    public NextAgent(String name, MailService mailbox) {
        super(name, mailbox);

        this.agentStatus = new NextAgentStatus(this);
        this.simStatus = new NextSimulationStatus();
        PathfindingConfig.ParseConfig("conf/NextAgents");

        this.say("Algorithmus: " + PathfindingConfig.GetAlgorithm().toString());
        this.intention = new NextIntention(this);

        this.processor = new NextPerceptReader(this);

        this.position = new Vector2D(0, 0);
        this.map = new NextMap(this);
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
        this.say(sender + message.toProlog());
    }

    /**
     * Main agent logic
     * @return Action - Next action for Massim simulation for this agent.
     */
    @Override
    public Action step() {

        processServerData();

        //this.broadcast(new Percept(" Message"), this.getName());
        //this.sendMessage(new Percept(" Message"), "B2", this.getName());
        
        // ActionGeneration is started on a new ActionID only
        if (simStatus.GetActionID() > lastID) {
            lastID = simStatus.GetActionID();

            // ----- Experimental part for Pathfinder implementation - For testing only
            
            //System.out.println(NextMap.MapToStringBuilder(this.agentStatus.GetMapArray()));
            
//            if (pathMemory.isEmpty()) {
//                Vector2D target = GetPosition().getAdded(NextAgentUtil.GenerateRandomNumber(11) - 5, NextAgentUtil.GenerateRandomNumber(11) - 5);
//                pathMemory = calculatePath(target);
//            }

            updateInternalBeliefs();

            clearPossibleActions();
            
            // new path
            generatePathMemory();
            
            generatePossibleActions();

            //return selectNextAction(); // nextAactionSelection V1
            
            return selectNextActionTest();  // For Testing purposes only
        }

        return null;
    }

	/**
     * Getter for local NextAgentStatus
     * @return NextAgentStatus
     */
    public NextAgentStatus getAgentStatus() {
        return this.agentStatus;
    }

    /**
     * Getter for local NextSimulationStatus
     * @return NextSimulationStatus
     */
    public NextSimulationStatus getSimulationStatus() {
        return simStatus;
    }

    /**
     *  Set flag to disable agent
     *  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *  Check if needed or ok to remove.
    */
    public void setFlagDisableAgent() {
        this.disableAgentFlag = true;
    }

    /**
     *  Set flag - action request active
     *  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *  Check if needed or ok to remove.
    */
    public void setFlagActionRequest() {
        this.actionRequestActive = true;
    }
    
    public NextTask GetActiveTask()
    {
    	return this.activeTask;
    }
    
    public void SetActiveTask(NextTask activeTask)
    {
    	this.activeTask = activeTask;
    }
    
    public EAgentTask GetAgentTask() {
    	return this.agentTask;
    }
    
    public void SetAgentTask(EAgentTask agentTask)
    {
    	this.agentTask = agentTask;
    }
    
    public List<Action> GetPathMemory()
    {
    	return this.pathMemory;
    }
    
    public void SetPathMemory(List<Action> pathMemory)
    {
    	this.pathMemory = pathMemory;
    }
    
    public void ClearPathMemory()
    {
    	this.pathMemory = new ArrayList<Action>();
    }
    
    public Vector2D GetPosition() {
        return map.RelativeToAbsolute(position);
    }
    
    public NextMap GetMap() {
    	return this.map;
    }

    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */

    private void resetAfterInactiveTask() {
    	this.SetActiveTask(null);
    	this.clearPossibleActions();
    	this.ClearPathMemory();
    	this.SetAgentTask(EAgentTask.exploreMap);
    	
    	// TODO miri: Mehrere Blöcke fallen lassen
    	// Erst schauen, ob es gerade einen Task gibt, den ich sonst abgeben könnte
//    	if(nextAgentStatus.GetAttachedElementsAmount() > 0)
//    	{
//    		possibleActions.add(NextActionWrapper.CreateAction(EActions.detach, 
//    				NextAgentUtil.GetDirection(nextAgentStatus.GetAttachedElements().iterator().next().getLocation())));
//    	}
	}


    /**
     * Stops the Agent. 
     * Closes the agent window 
     */
    private void disableAgent() {
        this.say("All games finished!");
        System.exit(1); // Kill the window
    }

    /**
     *  Agent behavior after finishing of the current simulation
     */
    private void finishTheSimulation() {
        this.say("Finishing this Simulation!");
        this.say("Result: #" + simStatus.GetRanking());
        resetAgent();
    }

    /**
     * Selects the next Action based on the priorityMap
     *
     * @return Action
     */
    private Action selectNextAction() {
        Action nextAction = intention.SelectNextAction();

        say(nextAction.toProlog());
        return nextAction;
    }

    // PATHFINDING EVALUATION - NUR ZUM TESTEN
    private Action selectNextActionTest() {
        Action nextAction = intention.SelectNextAction();

        if(!pathMemory.isEmpty()){
        	Action currentAction = pathMemory.get(0);
        	String direction = currentAction.getParameters().toString().replace("[","").replace("]", "");
        	NextMapTile obstacle = this.getAgentStatus().IsObstacleInNextStep(ECardinals.valueOf(direction));
        	if(obstacle != null)
        	{
        		// Block rotieren, wenn er nicht hinter mir ist (sonst pass ich nicht durchs loch) 
        		//IsRotationPossible
        		
        		nextAction = new Action(EActions.clear.toString(), new Identifier("" + obstacle.getPositionX()),new Identifier("" + obstacle.getPositionY()));
        	} else {        		
        		nextAction = pathMemory.remove(0);
        	}
        }
        say(nextAction.toProlog());
        return nextAction;
    }
    
    private void generatePossibleActions() {
        intention.GeneratePossibleActions();
    }
    
    private void generatePathMemory() {
    	intention.GeneratePathMemory();
    }
    
    private void clearPossibleActions()
    {
    	intention.ClearPossibleActions();
    }

    /**
     * resets the agent between the Simulations, clears the Belief elements
     */
    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new NextSimulationStatus();
        this.simStatus.SetActionID(lastID);
        this.agentStatus = new NextAgentStatus(this);
        this.processor = new NextPerceptReader(this);

        this.setPercepts(new ArrayList<>(), this.getPercepts());
        this.pathMemory = new ArrayList<>();

        //this.roleToChangeTo=null;
    }

    /**
     * CalculatePath to the target cell. The function decides which algorithm to
     * select based on target attributes.
     *
     * @param target Vector2D - cell on the general map
     * @return List of actions to reach the target.
     */
    private List<Action> calculatePath(Vector2D target) {
        // System.out.println("iNPUT" + agentStatus.GetPosition() + " " + target);

        Boolean targetIsOnMap = map.ContainsPoint(target);
        try {
            if (targetIsOnMap && !map.GetMapArray()[target.x][target.y].getThingType().equals("unknown")) {
                List<Action> pathMemoryA;
                pathMemoryA = aStar.calculatePath(map.GetMapArray(), GetPosition(), target);
                this.say("A* path:" + pathMemoryA);
                return pathMemoryA;

            } else {
                List<Action> pathMemoryB;
                int targetX = target.x - GetPosition().x;
                int targetY = target.y - GetPosition().y;
                // this.say("Values path: " + targetX +" "+ targetY);
                pathMemoryB = manhattanPath.calculatePath(targetX, targetY);
                this.say("Direct path: " + pathMemoryB.size() + " " + pathMemoryB);
                return pathMemoryB;
            }
        } catch (Exception e) {
            this.say("Path generation failed: " + e);
        }
        return null;
    }

    /**
     * Transfer the recieved percept data to the general map
     */
    private void updateMap() {
        if (agentStatus.GetLastAction().equals("move") && agentStatus.GetLastActionResult().equals("success")) {
            Vector2D currentStep = new Vector2D(0, 0);

            switch (agentStatus.GetLastActionParams()) {
                case "[n]":
                    currentStep = new Vector2D(0, -1);
                    break;
                case "[e]":
                    currentStep = new Vector2D(1, 0);
                    break;
                case "[s]":
                    currentStep = new Vector2D(0, 1);
                    break;
                case "[w]":
                    currentStep = new Vector2D(-1, 0);
                    break;
            }

            position.add(currentStep);

            // Init all tiles within view
            HashSet<NextMapTile> view = new HashSet<>();

            int vision = 5; // ToDo: Get vision from agent
            //String roleString = agent.getAgentStatus().GetRole();
            //HashSet<NextRole> roles = agent.getSimulationStatus().GetRolesList();

            for (int i = -1 * vision; i <= vision; i++) {
                for (int j = -1 * vision; j <= vision; j++) {
                    if (Math.abs(i) + Math.abs(j) <= vision) {
                    	HashSet<NextMapTile> goalZone = agentStatus.GetGoalZones();
                    	Iterator<NextMapTile> goalZoneIt = agentStatus.GetGoalZones().iterator();
                    	while(goalZoneIt.hasNext()) {
                    		NextMapTile next = goalZoneIt.next();
                    		if(i == next.getPositionX() && j == next.getPositionY()) {
                                view.add(new NextMapTile(i, j, getSimulationStatus().GetActualStep(), "goalZone"));
                    		}
                    	}
                        view.add(new NextMapTile(i, j, getSimulationStatus().GetActualStep(), "free"));
                    }
                }
            }
            map.AddPercept(position, view);

            // Only add visible things which are not attached to the agent
            HashSet<NextMapTile> visibleNotAttachedThings = new HashSet<>();

            for (NextMapTile thing : agentStatus.GetVisibleThings()) {
                if (!agentStatus.GetAttachedElements().contains(thing.getPoint())) {
                    visibleNotAttachedThings.add(thing);
                }
            }
            map.AddPercept(position, visibleNotAttachedThings);
            map.AddPercept(position, agentStatus.GetObstacles());

            //map.WriteToFile("map.txt");
        }
    }


    private void updateInternalBeliefs() {

        // update the selected Role
        updateCurrentRole();

        // Update internal map with new percept
        updateMap();

    }

    /**
     * processes the percepts provided by the server
     */
    private void processServerData() {

        // Checks if a new ActionID is found and proceeds with processing of all percepts
        for (Percept percept : getPercepts()) {
            if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);
                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        processor.evaluate(getPercepts(), this);
                    }
                }
            }
            // Reset of Data Storage after the current simulation is finished
            if (percept.getName().equals("simEnd")) {
                processor.evaluate(getPercepts(), this);
                finishTheSimulation();
            }
            //Stop processing after last Simulation
            if (percept.getName().equals("bye")) {
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
    
    /*
     * ##################### endregion private methods
     */
}
