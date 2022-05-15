package massim.javaagents.agents;

import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import eis.iilang.*;
import java.awt.Point;
import java.util.ArrayList;
import massim.javaagents.MailService;
import massim.javaagents.general.NextConstants;
import massim.javaagents.timeMonitor.NextTimeMonitor;

import java.util.List;

/**
 * First iteration of an experimental agent.
 *
 * Done: 
 * Handling of transition between simulations 
 * Basic action generation based on random movement
 * Processing of all percepts and storing in dataVaults
 *
 * ToDo: 
 * registerAgent @ Mailserver -> anmeldung für Agentenkommunikation. //
 * kombination mit Gruppenbildung?
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

    // --- Algorithms ---
    NextPerceptReader processor; // Eismassim interpreter
    //Pathfinding algorithm
    //PathFinding pathFinder; - ToDo

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

        this.agentStatus = new NextAgentStatus();
        this.simStatus = new NextSimulationStatus();

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
        processor.evaluate(getPercepts(), this);

        //this.printAgentStatus();
        if (disableAgentFlag) {
            disableAgent();
        }

        // Skips the ActionGeneration while simulation is idle
        if (!simStatus.GetFlagSimulationIsStarted()) {
            return null;
        }

        // processing after the current simulation is finished
        if (simStatus.GetFlagSimulationIsFinished()) {
            finishTheSimulation();
        }

        // Represents losing attached Blocks after beeing deactivated.
        if (agentStatus.GetDeactivatedFlag()) {
            agentStatus.DropAttachedElements();
        }

        // ActionGeneration is started on a new ActionID only
        if (simStatus.GetActionID() > lastID) {
            lastID = simStatus.GetActionID();

            ArrayList<Action> possibleActions = new ArrayList<>();
            generatePossibleActions(possibleActions);

            return selectNextAction(possibleActions);
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
	 * ########## region private methods
     */
    /**
     * Selects the next Action based on the priorityMap
     *
     * @param possibleActions
     * @return Action
     */
    private Action selectNextAction(ArrayList<Action> possibleActions) {

        Action nextAction = new Action("skip");

        //Compares each action based on the value
        for (Action action : possibleActions) {
            if (NextConstants.PriorityMap.get(action.getName()) < NextConstants.PriorityMap.get(nextAction.getName())) {
                nextAction = action;
            }
        }

        this.say(nextAction.toProlog());
        return nextAction;
    }

    private void disableAgent() {
        this.say("All games finished!");

        //System.exit(1); // Kill the window
    }

    //Agent behavior after current simulation has finished
    private void finishTheSimulation() {
        this.say("Finishing this Simulation!");
        this.say("Result: #" + simStatus.GetRanking());

        resetAgent();
    }

    private void generatePossibleActions(ArrayList<Action> possibleActions) {
        possibleActions.add(NextAgentUtil.generateRandomMove());

        // Localises the distance to the next target:  "dispenser", "goal", "role"
        possibleActions.add(NextAgentUtil.GenerateSurveyThingAction("dispenser"));

        // Survey a specific field with an agent. Get Name, Role, Energy
        // Attributes x-Position, y-Position relative to the Agent
        possibleActions.add(NextAgentUtil.GenerateSurveyAgentAction(0, 0));

        //Special case: Interaction with an adjacent element.
        for (NextMapTile visibleThing : agentStatus.GetVision()) {

            Point position = visibleThing.getPoint();

            if (NextAgentUtil.NextTo(position, agentStatus)) {

                if (visibleThing.getThingType().contains("dispenser")) {

                    if (agentStatus.GetAttachedElementsAmount() < 2) {
                        possibleActions.add(new Action("request", NextAgentUtil.GetDirection(position)));
                    }
                }

                if (visibleThing.getThingType().contains("block")) {
                    if (agentStatus.GetAttachedElementsAmount() < 2) {
                        possibleActions.add(new Action("attach", NextAgentUtil.GetDirection(position)));
                    }
                }
            }

        }
    }

    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new NextSimulationStatus();
        this.agentStatus = new NextAgentStatus();
        this.processor = new NextPerceptReader(this);

        this.setPercepts(new ArrayList<>(), this.getPercepts());
    }

    private void printAgentStatus() {
        this.say(agentStatus.toString());
    }

    /*
	 * ##################### endregion private methods
     */
}
