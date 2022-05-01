package massim.javaagents.agents;

import eis.iilang.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import massim.javaagents.MailService;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * First iteration of an experimental agent.
 *
 * Done: Handling of transition between simulations Basic action generation
 * based on random movement
 *
 * ToDo: registerAgent @ Mailserver -> anmeldung für Agentenkommunikation. //
 * kombination mit Gruppenbildung?
 *
 * @author Alexander Lorenz
 */
public class NextAgent extends Agent {

    private int lastID = -1;        // Is used to compare with actionID -> new Step Recognition
    private Boolean actionRequestActive = false; // Todo: implement reaction to True if needed. Is activated, before next Step.
    private Boolean disableAgentFlag = false; // True when all Simulations are finished 

    //Agent related attributes
    private AgentStatus status;
    //Simulation related attributes
    private SimulationStatus simStatus;

    //Compilation of finisched Simulations to be Processed after "deactivateAgentFlag == True"
    private List<SimulationStatus> finishedSimulations = new ArrayList<>();

    // --- Algorithms ---
    // Eismassim interpreter
    NextPerceptReader processor;
    // Pathfinding algorithm
    //PathFinding pathFinder; - ToDo

    //--- General Directions ----
    final static Point westPoint = new Point(-1, 0);
    final static Point eastPoint = new Point(1, 0);
    final static Point southPoint = new Point(0, 1);
    final static Point northPoint = new Point(0, -1);

    
    /**
     * Priotiy selection to be used by Agents
     */
    final static Map<String, Integer> priorityMap = new HashMap<String, Integer>() {
        {
            put("submit", 1);
            put("attach", 2);
            put("request", 3);
            put("move", 4);
            put("detach", 5);
            put("rotate", 6);
            put("connect", 7);
            put("survey", 8);
            put("adopt", 9);
            put("disconnect", 10);
            put("clear", 11);
            put("skip", 100);
        }
    };

    /**
     * Constructor.
     *
     * @param name the agent's name
     * @param mailbox the mail facility
     */
    public NextAgent(String name, MailService mailbox) {
        super(name, mailbox);

        this.status = new AgentStatus();
        this.simStatus = new SimulationStatus();

        this.processor = new NextPerceptReader(this);

    }

    /*
    //-----------------------
 
    
    //------------------------
    */
    
    //-----------------------------------------------------------
    
    // Original Method
    @Override
    public void handlePercept(Percept percept) {
    }

    // Original Method
    @Override
    public void handleMessage(Percept message, String sender) {
    }

    /**
     * Agent handling after the step call
     *
     * @return Action - Next action for Massim simulation for this agent.
     */
    @Override
    public Action step() {
        processor.evaluate(getPercepts());

        if (disableAgentFlag) {
            disableAgent();
        }

        // processing after one simulation is finished
        if (simStatus.getSimulationIsFinished()) {
            finishTheSimulation();
        }

        // Skips the ActionGeneration while simulation is idle
        if (!simStatus.getSimulationIsStarted()) {
            return null;
        }
        
        // Represents losing attached Blocks after beeing deactivated.
        if(status.getDeactivatedFlag()){
            status.dropAttachedElements();
        }
        
        // ActionGeneration is started on a new ActionID only
        if (simStatus.getActionID() > lastID) {
            lastID = simStatus.getActionID();

            ArrayList<Action> possibleActions = new ArrayList<>();
            generatePossibleActions(possibleActions);

            return selectNextAction(possibleActions);
        }

        return null;

    }

    private Action generateRandomMove() {
        Random rn = new Random();
        String[] directions = new String[]{"n", "s", "w", "e"};
        return new Action("move", new Identifier(directions[rn.nextInt(4)]));
    }

    
    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return boolean
     */
    private boolean nextTo(Point position) {
        if(position.equals(westPoint) && !this.status.getAttachedElements().contains(westPoint)){
            return true;
        }
        if(position.equals(northPoint) && !this.status.getAttachedElements().contains(northPoint)){
            return true;
        }
        if(position.equals(eastPoint) && !this.status.getAttachedElements().contains(eastPoint)){
            return true;
        }
        if(position.equals(southPoint) && !this.status.getAttachedElements().contains(southPoint)){
            return true;
        }        
        return false;
    }

    /**
     * Returns the direction for an action
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return Identifier for the direction value of an action.
     */
    private Identifier getDirection(Point direction) {
        if (direction.equals(westPoint)) {
            return new Identifier("w");
        }

        if (direction.equals(southPoint)) {
            return new Identifier("s");
        }

        if (direction.equals(eastPoint)) {
            return new Identifier("e");
        }

        if (direction.equals(northPoint)) {
            return new Identifier("n");
        }

        return null;
    }

    /**
     * Selects the next Action based on priorityMap
     *
     * @param possibleActions
     * @return Action
     */
    private Action selectNextAction(ArrayList<Action> possibleActions) {

        Action nextAction = new Action("skip");

        //Compares each action based on the value
        for (Action action : possibleActions) {
            if (priorityMap.get(action.getName()) < priorityMap.get(nextAction.getName())) {
                nextAction = action;
            }
        }

        this.say(nextAction.toProlog());
        return nextAction;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public SimulationStatus getSimulationStatus() {
        return simStatus;
    }

    /*
        Agent behavior after all simulations have finished
     */
    public void disableAgent() {
        this.say("All games finished!");

        //System.exit(1); // Kill the window
    }

    /*
        Agent behavior after current simulation has finished
     */
    public void finishTheSimulation() {
        this.say("Finishing this Simulation!");
        this.say("Result: #" + simStatus.getRanking());

        resetAgent();
    }

    public void setFlagActionRequest() {
        this.actionRequestActive = true;
    }

    private void generatePossibleActions(ArrayList<Action> possibleActions) {
        possibleActions.add(generateRandomMove());

        List<Percept> percepts = getPercepts();
        for (Percept percept : percepts) {
            // TODO - Convert to using AgentStatus

            //Implementation of a reactive Action, atttach - if standing next to a block
            if (percept.getName().equals("thing")) {
                int xValue = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
                int yValue = ((Numeral) percept.getParameters().get(1)).getValue().intValue();

                Point PositionOfThing = new Point(xValue, yValue);

                Parameter Ident = percept.getParameters().get(2);

                // BUG: The agent seem to share the attached status with other agents. 
                // If 1 agent is full, no further attach or request actions are tried. 
                // - 
                
                if (Ident instanceof Identifier) {
                    String wert = ((Identifier) Ident).getValue();

                    // if (this.status.getAttachedElements().size() < 2 && 
                    if (        wert.equals("block") && nextTo(PositionOfThing)) {
                        possibleActions.add(new Action("attach", getDirection(PositionOfThing)));
                    }

                    // if (this.status.getAttachedElements().size() < 2 && 
                    if (        wert.equals("dispenser") && nextTo(PositionOfThing)) {
                        possibleActions.add(new Action("request", getDirection(PositionOfThing)));
                    }
                }
                
                // Todo: Wandeln in If NextTo Thing, select action based on thing
            }
        }

    }

    void setFlagDisableAgent() {
        this.disableAgentFlag = true;
    }

    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new SimulationStatus();
        this.status = new AgentStatus();
        this.processor = new NextPerceptReader(this);

        this.setPercepts(new ArrayList<>(), this.getPercepts());
    }

}
