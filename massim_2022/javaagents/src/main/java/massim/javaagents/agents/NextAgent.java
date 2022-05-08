package massim.javaagents.agents;

import eis.iilang.*;
import java.awt.Point;
import java.util.ArrayList;
import massim.javaagents.MailService;

import java.util.List;

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

    /*
	 * ########## region fields
     */
    private int lastID = -1;        // Is used to compare with actionID -> new Step Recognition
    private Boolean actionRequestActive = false; // Todo: implement reaction to True if needed. Is activated, before next Step.
    private Boolean disableAgentFlag = false; // True when all Simulations are finished 

    //Agent related attributes
    private AgentStatus agentStatus;
    //Simulation related attributes
    private SimulationStatus simStatus;

    //Compilation of finisched Simulations to be Processed after "deactivateAgentFlag == True"
    private List<SimulationStatus> finishedSimulations = new ArrayList<>();

    // --- Algorithms ---
    
    NextPerceptReader processor; // Eismassim interpreter
    // Pathfinding algorithm
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

        this.agentStatus = new AgentStatus();
        this.simStatus = new SimulationStatus();

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
        processor.evaluate(getPercepts(),this);
        
        
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

    public AgentStatus getStatus() {
        return this.agentStatus;
    }

    public SimulationStatus getSimulationStatus() {
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
            if (Constants.PriorityMap.get(action.getName()) < Constants.PriorityMap.get(nextAction.getName())) {
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
        possibleActions.add(AgentUtil.generateRandomMove());
        
        // Localises the distance to the next target:  "dispenser", "goal", "role"
        possibleActions.add(AgentUtil.GenerateSurveyThingAction("dispenser"));
        
        // Survey a specific field with an agent. Get Name, Role, Energy
        // Attributes x-Position, y-Position relative to the Agent
        possibleActions.add(AgentUtil.GenerateSurveyAgentAction(0, 0));
        

        //Special case: Interaction with an adjacent element.
        for (MapTile visibleThing : agentStatus.GetVision()) {

            Point position = visibleThing.getPoint();

            if (AgentUtil.NextTo(position, agentStatus)) {
                
                if (visibleThing.getThingType().equals("dispenser")) {
                    if (agentStatus.GetAttachedElementsAmount() < 2) {
                        possibleActions.add(new Action("request", AgentUtil.GetDirection(position)));
                    }
                }

                if (visibleThing.getThingType().equals("block")) {
                    if (agentStatus.GetAttachedElementsAmount() < 2) {
                        possibleActions.add(new Action("attach", AgentUtil.GetDirection(position)));
                    }
                }
            }

        }
    }

    private void resetAgent() {

        this.lastID = -1;
        this.simStatus = new SimulationStatus();
        this.agentStatus = new AgentStatus();
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
