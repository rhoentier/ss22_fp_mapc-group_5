package massim.javaagents.agents;

import java.util.HashSet;

/**
 *
 * @author AVL
 */
public class SimulationStatus {

    /*
    public Set<TaskInfo> tasksList = new HashSet<>();
    public Set<NormInfo> normsInfo = new HashSet<>();
    -- public long score;
    -- public String lastAction;
    -- public String lastActionResult;
    public List<String> lastActionParams = new ArrayList<>();
    -- public List<Position> attachedThings = new ArrayList<>();
 
    public JSONArray stepEvents;
    public List<Position> goalZones = new ArrayList<>();
    public List<Position> roleZones = new ArrayList<>();
     */
    private Boolean simulationIsFinished;
    private Boolean simulationIsStarted;

    private Integer actionID = -1;
    private Integer teamSize;
    private Integer totalSteps;
    private Integer actualStep;
    private Integer ranking;
    private long score;
    private long timestamp;
    private long deadline;

    private HashSet<String> violations;

    private HashSet<NextTask> tasksList = new HashSet<>();
    private HashSet<NextRole> rolesList = new HashSet<>();
    private HashSet<NextNorm> normsList = new HashSet<>();
    
    //---------------- Getter and Setter
    public SimulationStatus() {
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

    void SetTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public Integer GetTeamSize() {
        return teamSize;
    }

    void SetScore(long score) {
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

    public void SetActualStep(Integer actualStep) {
        this.actualStep = actualStep;
    }

    public Integer GetActualStep() {
        return actualStep;
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

    public HashSet<String> getViolations() {
        return violations;
    }

    public void setViolations(HashSet<String> violations) {
        this.violations = violations;
    }

    public HashSet<NextTask> getTasksList() {
        return tasksList;
    }

    public void setTasksList(HashSet<NextTask> tasksList) {
        this.tasksList = tasksList;
    }

    public HashSet<NextRole> getRolesList() {
        return rolesList;
    }

    public void setRolesList(HashSet<NextRole> rolesList) {
        this.rolesList = rolesList;
    }

    public HashSet<NextNorm> getNormsList() {
        return normsList;
    }

    public void setNormsList(HashSet<NextNorm> normsList) {
        this.normsList = normsList;
    }

    
    
}
