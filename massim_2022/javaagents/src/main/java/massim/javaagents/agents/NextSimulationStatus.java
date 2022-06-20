package massim.javaagents.agents;

import java.util.HashSet;

import massim.javaagents.percept.NextNorm;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextTask;

/**
 * Part of Agent Belief System
 * <p>
 * Simulation related status values
 * 
 * @author Alexander Lorenz
 */

public class NextSimulationStatus {

    private Boolean simulationIsFinished;
    private Boolean simulationIsStarted;

    private Integer actionID = -1;
    private Integer teamSize;
    private Integer totalSteps;
    private Integer currentStep;
    private Integer ranking;
    private long score;
    private long timestamp;
    private long deadline;

    private HashSet<String> violations;

    private HashSet<NextTask> tasksList = new HashSet<>();
    private HashSet<NextRole> rolesList = new HashSet<>();
    private HashSet<NextNorm> normsList = new HashSet<>();
    
    //---------------- Getter and Setter
    public NextSimulationStatus() {
        simulationIsFinished = false;
        simulationIsStarted = false;
        actionID = -1;
    }

    public void SetActionID(Integer actionID) {
        this.actionID = actionID;
    }

    public Integer GetActionID() {
        return actionID;
    }

    public void SetTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public Integer GetTeamSize() {
        return teamSize;
    }

    public void SetScore(long score) {
        this.score = score;
    }

    public long GetScore() {
        return score;
    }

    public void SetDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long GetDeadline() {
        return deadline;
    }

    public void SetTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long GetTimestamp() {
        return timestamp;
    }

    public void SetTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Integer GetTotalSteps() {
        return totalSteps;
    }

    public void SetCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Integer GetCurrentStep() {
        return currentStep;
    }

    public void SetRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public Integer GetRanking() {
        return ranking;
    }

    public Boolean GetFlagSimulationIsStarted() {
        return simulationIsStarted;
    }

    public void SetFlagSimulationIsStarted() {
        simulationIsStarted = true;
    }

    public Boolean GetFlagSimulationIsFinished() {
        return simulationIsFinished;
    }

    public void SetFlagSimulationIsFinished() {
        simulationIsFinished = true;
    }

    public HashSet<String> GetViolations() {
        return violations;
    }

    public void SetViolations(HashSet<String> violations) {
        this.violations = violations;
    }

    public HashSet<NextTask> GetTasksList() {
        return tasksList;
    }

    public void SetTasksList(HashSet<NextTask> tasksList) {
        this.tasksList = tasksList;
    }

    public HashSet<NextRole> GetRolesList() {
        return rolesList;
    }

    public void SetRolesList(HashSet<NextRole> rolesList) {
        this.rolesList = rolesList;
    }

    public HashSet<NextNorm> GetNormsList() {
        return normsList;
    }

    public void SetNormsList(HashSet<NextNorm> normsList) {
        this.normsList = normsList;
    }    
}
