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
 * Done: 
 * Handling of transition between simulations 
 * Basic action generation based on random movement
 * 
 * ToDo: registerAgent @ Mailserver -> anmeldung für Agentenkommunikation.
 *          // kombination mit Gruppenbildung?
 *
 * @author AVL
 */

public class NextAgent extends Agent {

    private int lastID = -1;
    private Boolean actionRequestActive = false; // Todo: implement reaction to True if needed. Is activated, before next Step.
    private Boolean deactivateAgentFlag = false;

    private AgentStatus status;
    private SimulationStatus simStatus;

    private List<SimulationStatus> finishedSimulations = new ArrayList<>();

    // --- Algorithms ---
    NextPerceptReader processor;
    //PathFinding pathFinder; - ToDo

    //--- General Directions ----
    final static Point westPoint = new Point(-1, 0);
    final static Point eastPoint = new Point(1, 0);
    final static Point southPoint = new Point(0, 1);
    final static Point northPoint = new Point(0, -1);

    private ArrayList<Point> cargoSlots = new ArrayList();
    private ArrayList<Point> attachedList = new ArrayList();

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

        this.cargoSlots.add(westPoint);
        this.cargoSlots.add(northPoint);
        this.cargoSlots.add(eastPoint);
        this.cargoSlots.add(southPoint);

        this.status = new AgentStatus();
        this.simStatus = new SimulationStatus();

        this.processor = new NextPerceptReader(this);

    }

    //-----------------------------------------------------------
    @Override
    public void handlePercept(Percept percept) {
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }

    /**
     * Agent handling after the step Call 
     * 
     * @return Action - Next action for Massim simulation for this agent.
     */
    @Override
    public Action step() {
        processor.evaluate(getPercepts());
        
        //clear the processed perceipts - used later at the moment, has to be moved here
        // this.setPercepts(new ArrayList<>(), this.getPercepts());
    
        
        if (deactivateAgentFlag) {
            deactivateAgent();
        }
        
        if (simStatus.getSimulationIsFinished()) {
            finishTheSimulation();
        }
        
        // Skips the ActionGeneration while simulation is idle
        if(!simStatus.getSimulationIsStarted()){
            return null;
        }
        // ActionGeneration is started on a new ActionID only
        if (simStatus.getActionID() > lastID) {
            lastID = simStatus.getActionID();

            ArrayList<Action> possibleActions = new ArrayList<>();
            generatePossibleActions(possibleActions);
            
            //clear the processed perceipts
            this.setPercepts(new ArrayList<>(), this.getPercepts());
    
            return selectNextAction(possibleActions);
        }

        
        return null;

    }

    private Action generateRandomMove() {
        Random rn = new Random();
        String[] directions = new String[]{"n", "s", "w", "e"};
        return new Action("move", new Identifier(directions[rn.nextInt(4)]));
    }

    private Action generateMoveNorth() {
        return new Action("move", new Identifier("n"));
    }

    private Action generateMoveSouth() {
        return new Action("move", new Identifier("s"));
    }

    private Action generateMoveEast() {
        return new Action("move", new Identifier("e"));
    }

    private Action generateMoveWest() {
        return new Action("move", new Identifier("w"));
    }

    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param xValue - xValue of Thing
     * @param yValue - yValue of Thing
     * @return boolean
     */
    private boolean nextTo(Point position) {
        Boolean feedback = false;
        for (Point freeSlot : cargoSlots) {

            if (position.equals(freeSlot)) {
                feedback = true;
            }

            for (Point attachedSlot : attachedList) {
                if (position.equals(attachedSlot)) {
                    feedback = false;
                }
            }
        }
        return feedback;
    }

    /**
     * Returns the direction for an action
     *
     * @param xValue - xValue of Thing
     * @param yValue - yValue of Thing
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
    public void deactivateAgent() {
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

        ArrayList<Point> attachedListNew = new ArrayList();

        List<Percept> percepts = getPercepts();
        for (Percept percept : percepts) {
            // TODO - Convert to using AgentStatus
            if (percept.getName().equals("attached")) {

                List<Parameter> parameters = percept.getParameters();
                Point newPoint = new Point(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                if (!attachedListNew.contains(newPoint)) {
                    attachedListNew.add(newPoint);
                }
            }

            if (percept.getName().equals("goalZones")) {
                this.say("Goal" + percept.getParameters().get(0));
                possibleActions.add(new Action("submit"));

            }

            //Implementation of a reactive Action, atttach - if standing next to a block
            if (percept.getName().equals("thing")) {
                int xValue = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
                int yValue = ((Numeral) percept.getParameters().get(1)).getValue().intValue();

                Point PositionOfThing = new Point(xValue, yValue);

                Parameter Ident = percept.getParameters().get(2);

                if (Ident instanceof Identifier) {
                    String wert = ((Identifier) Ident).getValue();

                    /*
                    TODO - if Block attached, ignore 
                    if (wert.equals("block")) {
                        this.say("Sehe Block");
                    }
                     */
                    if (wert.equals("block") && nextTo(PositionOfThing) && this.attachedList.size() < 2) {
                        // - TODO: evaluate if Cargo fetch sucessful

                        //this.say("attach " + getDirection(PositionOfThing));
                        //cargoSlots.remove(PositionOfThing);
                        possibleActions.add(new Action("attach", getDirection(PositionOfThing)));
                    }

                    if (wert.equals("dispenser") && nextTo(PositionOfThing) && this.attachedList.size() < 2) {

                        //this.say("request " + getDirection(PositionOfThing));
                        possibleActions.add(new Action("request", getDirection(PositionOfThing)));
                    }
                }
            }
        }

        //Can be removed after transition to Agent status
        attachedList = attachedListNew;
        // this.say(" Actionlist " + possibleActions.size());
        this.actionRequestActive = false;
    }

    void setFlagDeactivateAgent() {
        this.deactivateAgentFlag = true;
    }

    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new SimulationStatus();
        this.status = new AgentStatus();
        this.processor = new NextPerceptReader(this);

        this.setPercepts(new ArrayList<>(), this.getPercepts());
    }

}
