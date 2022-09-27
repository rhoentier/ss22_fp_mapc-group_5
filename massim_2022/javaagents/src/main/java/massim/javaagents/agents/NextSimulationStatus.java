package massim.javaagents.agents;

import java.util.HashSet;

import massim.javaagents.percept.NextNorm;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextTask;

/**
 * Part of Agent Belief System
 * 
 * Simulation related status values
 *
 * @author Alexander Lorenz
 */

public class NextSimulationStatus {

    /*
     * ########## region fields
     */
    
    private Boolean simulationIsFinished;   // true to state simulation round has finished
    private Boolean simulationIsStarted;    // true to state simulation round has started

    private int actionID;                   // the ID for the action to return to the server
    private int teamSize;                   // number of entities used in the simulation
    private int totalSteps;                 // total number of steps in this round
    private int currentStep;                // current step in the simulation
    private int ranking;                    // achieved rank after the simulation round
    private long score;                     // the current score of the team
    private long timestamp;                 // server time the percept message was created at
    private long deadline;                  // server time when the server expects the action

    private boolean mapDiscoveryStarted = false;                // status of map discovery. true if started

    private HashSet<String> violations;                         // set of norm IDs the agent has violated

    private HashSet<NextTask> tasksList = new HashSet<>();      // set of tasks provided by the server
    private HashSet<NextRole> rolesList = new HashSet<>();      // set of roles provided by the server
    private HashSet<NextNorm> normsList = new HashSet<>();      // set of norms provided by the server


    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     */
    
    public NextSimulationStatus() {
        simulationIsFinished = false;
        simulationIsStarted = false;
        actionID = -1;
    }

    /**
     * Sets the ID for the action to return to the server
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param actionID integer ID value 
     */
    public void SetActionID(Integer actionID) {
        this.actionID = actionID;
    }

    /**
     * Retrieve the current action ID
     *
     * @return integer ID value
     */
    public Integer GetActionID() {
        return actionID;
    }

    /**
     * Sets the size of team for current simulation round
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param teamSize int size of the team
     */
    public void SetTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    /**
     * Retrieve the team size for current simulation round
     * 
     * @return integer size of the team
     */
    public Integer GetTeamSize() {
        return teamSize;
    }

    /**
     * Sets the current score until current step
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param score long total points achieved by the team
     */
    public void SetScore(long score) {
        this.score = score;
    }

    /**
     * Retrieve the current points achieved by the team until current step
     * 
     * @return long total points achieved by the team
     */
    public long GetScore() {
        return score;
    }

    /**
     * Sets the server time when the server expects the next action
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param deadline long time in ms
     */
    public void SetDeadline(long deadline) {
        this.deadline = deadline;
    }

    /**
     * Retrieve the server time when the server expects the next action
     * 
     * @return long time in ms
     */
    public long GetDeadline() {
        return deadline;
    }

    /**
     * Sets the server time the latest percept message was created at.
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param timestamp long time in ms
     */
    public void SetTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Retrieve the server time the percept message was created at.
     * 
     * @return long time in ms
     */
    public long GetTimestamp() {
        return timestamp;
    }

    /**
     * Sets the total number of steps in this round
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param totalSteps Integer total steps in round 
     */
    public void SetTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    /**
     * Retrieve the total number of steps in this round
     * 
     * @return Integer total steps in round
     */
    public Integer GetTotalSteps() {
        return totalSteps;
    }

    /**
     * Sets the current step
     *
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param currentStep Integer value
     */
    public void SetCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Retrieves the current step in the simulation
     *
     * @return Integer value
     */
    public Integer GetCurrentStep() {
        return currentStep;
    }

    /**
     * Sets the achieved ranking after a simulation
     *
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param ranking Integer the final ranking of the agent's team
     */
    public void SetRanking(Integer ranking) {
        this.ranking = ranking;
    }

    /**
     * Retrieves the achieved ranking after a simulation
     *
     * @return Integer the final ranking of the agent's team
     */
    public Integer GetRanking() {
        return ranking;
    }

    /**
     * Retrieve the running status of the simulation 
     * 
     * @return Boolean true if simulation has started
     */
    public Boolean GetFlagSimulationIsStarted() {
        return simulationIsStarted;
    }

    /**
     * Set the status of the simulation to started.
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually.
     */
    public void SetFlagSimulationIsStarted() {
        simulationIsStarted = true;
    }

    /**
     * Check if simulation is finished
     * 
     * @return Boolean true if simulation is finished
     */
    public Boolean GetFlagSimulationIsFinished() {
        return simulationIsFinished;
    }

    /**
     * Set the status of the simulation to finished.
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     */
    public void SetFlagSimulationIsFinished() {
        simulationIsFinished = true;
    }

    /**
     * Get the IDs of the norms violated by the agent´s team
     * 
     * @return String HashSet containing the IDs
     */
    public HashSet<String> GetViolations() {
        return violations;
    }

    /**
     * Set the IDs of the norms violated by the agent´s team
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param violations String HashSet containing the IDs
     */
    public void SetViolations(HashSet<String> violations) {
        this.violations = violations;
    }

    /**
     * Retrieve the possible tasks, offered by the simulation
     * 
     * @return NextTask HashSet containing the possible tasks
     */
    public HashSet<NextTask> GetTasksList() {
        return tasksList;
    }

    /**
     * Sets the possible tasks, offered by the simulation
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param tasksList NextTask HashSet containing the possible tasks
     */
    public void SetTasksList(HashSet<NextTask> tasksList) {
        this.tasksList = tasksList;
    }

    /**
     * Retrieve the roles agents can adapt
     * 
     * @return NextRole HashSet containing the possible roles
     */
    public HashSet<NextRole> GetRolesList() {
        return rolesList;
    }

    /**
     * Sets the roles agents can adapt
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param rolesList NextRole HashSet containing the possible roles
     */
    public void SetRolesList(HashSet<NextRole> rolesList) {
        this.rolesList = rolesList;
    }

    /**
     * Retrieve the description of the norms valid in the round
     * 
     * @return NextNorm HashSet containing the norms
     */
    public HashSet<NextNorm> GetNormsList() {
        return normsList;
    }

    /**
     * Sets the norms valid in the round
     * 
     * value is provided by NextPerceptReader, not to be adjusted manually
     * @param normsList NextNorm HashSet containing the norms
     */
    public void SetNormsList(HashSet<NextNorm> normsList) {
        this.normsList = normsList;
    }

    /**
     * Check if size search for the map was started 
     * 
     * @return boolean true if size exploration has begun
     */
    public boolean HasMapSizeDiscoveryStarted() {
        return mapDiscoveryStarted;
    }

    /**
     * Starts the size search for the map 
     */
    public void ActivateMapSizeDiscovery() {
        this.mapDiscoveryStarted = true;
    }
    
    /**
     * Resets the size search for the map. 
     * The search is set to disabled
     */
    public void ResetMapSizeDiscovery()  {
        this.mapDiscoveryStarted = false;
    }

}
