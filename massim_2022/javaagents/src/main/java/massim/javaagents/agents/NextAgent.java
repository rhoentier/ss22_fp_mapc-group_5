package massim.javaagents.agents;

import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.intention.NextIntention;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import eis.iilang.*;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;

import massim.javaagents.MailService;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.timeMonitor.NextTimeMonitor;
import massim.javaagents.pathfinding.NextRandomPath;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.PathfindingConfig;
import massim.javaagents.percept.NextTask;

import java.util.List;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextAStarPath;

import javax.lang.model.element.ModuleElement.DirectiveKind;

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
    private NextTimeMonitor timeMonitor;

    //Compilation of finished Simulations to be Processed after "deactivateAgentFlag == True"
    private List<NextSimulationStatus> finishedSimulations = new ArrayList<>();

    //BDI
    private NextIntention intention;

    // --- Algorithms ---
    NextPerceptReader processor; // Eismassim interpreter

    // Pathfinding algorithm
    //PathfindingConfig pathfindingConfig;
    private NextManhattanPath manhattanPath = new NextManhattanPath();
    private NextAStarPath aStar = new NextAStarPath();
    private List<Action> pathMemory = new ArrayList<>();

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
        
        this.agentTask = EAgentTask.exploreMap;
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
     * Agent handling after the step call
     *
     * @return Action - Next action for Massim simulation for this agent.
     */
    @Override
    public Action step() {

        //this.broadcast(new Percept(" Message"), this.getName());
        //this.sendMessage(new Percept(" Message"), "B2", this.getName());
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
                disableAgent();
            }
        }
        
        // ActionGeneration is started on a new ActionID only
        if (simStatus.GetActionID() > lastID) {
            lastID = simStatus.GetActionID();

            //Experimental part for Pathfinder implementation - For testing only
//            if (pathMemory.isEmpty()) {
//                Vector2D target = agentStatus.GetPosition().getAdded(NextAgentUtil.GenerateRandomNumber(11) - 5, NextAgentUtil.GenerateRandomNumber(11) - 5);
//                pathMemory = calculatePath(target);
//            }
            
            // Update internal map with new percept
            agentStatus.UpdateMap();

            clearPossibleActions();
            
            generatePathMemory();
            
            generatePossibleActions();

            //return selectNextAction();
            return selectNextActionTest();  // For Testing purposes only
        }

        return null;
    }

    public NextAgentStatus getStatus() {
        return this.agentStatus;
    }

    public NextSimulationStatus getSimulationStatus() {
        return simStatus;
    }

    public void setFlagDisableAgent() {
        this.disableAgentFlag = true;
    }

    //Agent behavior after all simulations have finished
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
    /*
     * ##################### endregion public methods
     */
 /*

    /**
     * Stops the Agent. 
     * Is disabled to keep the logdata visible. 
     */
    private void disableAgent() {
        this.say("All games finished!");
        try {
            Thread.sleep(10000000);
        } catch (Exception e) {
        }
        //System.exit(1); // Kill the window
    }

    //Agent behavior after current simulation has finished
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
        if(!nextAction.getName().contains("submit")) 
        {
	        if(!pathMemory.isEmpty()){
	        	// TODO miri Clear der Bloecke implementieren
	        	// Wenn ich meinen Schritt nicht gehen kann, dann will ich den Block zerstören
	        	//Action currentAction = pathMemory.get(0);
	        	//String direction = currentAction.getParameters().toString().replace("[","").replace("]", "");
	//        	if(this.getStatus().IsObstacleInNextStep(this.getStatus().GetPosition(), ECardinals.valueOf(direction)))
	//        	{        		
	//        		nextAction = NextActionWrapper.CreateAction(EActions.clear, new Identifier(direction));
	//        	} else {        		
	        		nextAction = pathMemory.remove(0);
	//        	}
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

    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new NextSimulationStatus();
        this.simStatus.SetActionID(lastID);
        this.agentStatus = new NextAgentStatus(this);
        this.processor = new NextPerceptReader(this);

        this.setPercepts(new ArrayList<>(), this.getPercepts());
        //this.roleToChangeTo=null;

        pathMemory = new ArrayList<>();

    }

    private void printAgentStatus() {
        this.say(agentStatus.toString());
    }

    public List<Action> calculatePath(Vector2D target) {
        // System.out.println("iNPUT" + agentStatus.GetPosition() + " " + target);
        
        Boolean targetIsOnMap = agentStatus.GetMap().containsPoint(target);
        try {
            if (targetIsOnMap && !agentStatus.GetMapArray()[(int)target.x][(int)target.y].getThingType().equals("unknown")) {
                List<Action> pathMemoryA;
                pathMemoryA = aStar.calculatePath(agentStatus.GetMapArray(), agentStatus.GetPosition(), target);
                // this.say("A* path:" + pathMemoryA);
                return pathMemoryA;
            
            } else {
                List<Action> pathMemoryB;
                int targetX = (int) (target.x - agentStatus.GetPosition().x);
                int targetY = (int) (target.y - agentStatus.GetPosition().y);
                // this.say("Values path: " + targetX +" "+ targetY);
                pathMemoryB = manhattanPath.calculatePath(targetX, targetY);
                // this.say("Direct path: " + pathMemoryB.size() +" "+ pathMemoryB);
                return pathMemoryB; 
            }
        } catch (Exception e) {
            this.say("Path generation failed: " + e);
        }
        return null;
    }

    /*
     * ##################### endregion private methods
     */
}
