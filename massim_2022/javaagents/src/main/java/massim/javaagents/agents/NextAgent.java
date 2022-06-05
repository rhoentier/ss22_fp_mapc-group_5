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
import massim.javaagents.timeMonitor.NextTimeMonitor;
import massim.javaagents.pathfinding.NextRandomPath;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.pathfinding.PathfindingConfig;

import java.util.List;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextAStarPath;

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

        
        //Experimental part for Pathfinder implementation - For testing only
        if (pathMemory.isEmpty()) {
            try {
                this.say(agentStatus.GetPosition().toString());
                ///*
                    try{ 
                    
                    //    this.say(" " + agentStatus.getMap().MapToStringBuilder());
                        
                    //this.say(" " + NextMap.MapToStringBuilder(agentStatus.getMap().GetMapArray()));
                    this.say(" " + NextMap.MapToStringBuilder(NextMap.CenterMapAroundPosition(agentStatus.getMap().GetMapArray(),agentStatus.GetPosition())));
                    
                    
                    }catch( Exception e){
                    this.say(" " + e);
                    } finally{}
                //*/
                this.say(" " + agentStatus.GetSizeOfMap());
                //pathMemory = aStar.calculatePath(agentStatus.GetMapArray(), agentStatus.GetPosition(), agentStatus.GetPosition().getAdded(NextAgentUtil.GenerateRandomNumber(21)-10,NextAgentUtil.GenerateRandomNumber(21)-10) );
                //pathMemory = manhattanPath.calculatePath(NextAgentUtil.GenerateRandomNumber(21)-10,NextAgentUtil.GenerateRandomNumber(21)-10);

                this.say(pathMemory.toString());
            } catch (Exception e) {
                this.say("Path generation failed: " + e);
            }
        }

        // ActionGeneration is started on a new ActionID only
        if (simStatus.GetActionID() > lastID) {
            lastID = simStatus.GetActionID();
            
            // Update internal map with new percept
            agentStatus.UpdateMap();


            generatePossibleActions();

            //return selectNextAction();
            return selectNextActionTest();
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
        
        if(!pathMemory.isEmpty()){
            nextAction = pathMemory.remove(0);
        }
        say(nextAction.toProlog());
        return nextAction;
        //return new Action("move", new Identifier("n"));
    }

    private void generatePossibleActions() {
        intention.GeneratePossibleActions();
    }

    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new NextSimulationStatus();
        this.simStatus.SetActionID(lastID);
        this.agentStatus = new NextAgentStatus(this);
        this.processor = new NextPerceptReader(this);

        this.setPercepts(new ArrayList<>(), this.getPercepts());
        //this.roleToChangeTo=null;
    }

    private void printAgentStatus() {
        this.say(agentStatus.toString());
    }

    /*
     * ##################### endregion private methods
     */
}
