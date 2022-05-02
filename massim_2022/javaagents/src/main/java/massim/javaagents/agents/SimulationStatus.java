package massim.javaagents.agents;

/**
 *
 * @author AVL
 */
public class SimulationStatus {
    
    
    /*
    public Set<Thing> things = new HashSet<>();
    public Set<TaskInfo> taskInfo = new HashSet<>();
    public Set<NormInfo> normsInfo = new HashSet<>();
    -- public long score;
    -- public String lastAction;
    -- public String lastActionResult;
    public List<String> lastActionParams = new ArrayList<>();
    -- public List<Position> attachedThings = new ArrayList<>();
 
    public JSONArray stepEvents;
    public List<String> violations;
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

    //---------------- Getter and Setter
    
    public SimulationStatus () {
        simulationIsFinished = false;
        simulationIsStarted = false;
        actionID = -1;
    }
    
    public void setActionID(Integer actionID) {
        this.actionID = actionID;
    }

    public Integer getActionID() {
        return actionID;
    }

    void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    void setScore(long score) {
        this.score = score;
    }

    public long getScore() {
        return score;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setActualStep(Integer actualStep) {
        this.actualStep = actualStep;
    }

    public Integer getActualStep() {
        return actualStep;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public Integer getRanking() {
        return ranking;
    }

    public Boolean getSimulationIsStarted() {
        return simulationIsStarted;
    }
    
    public void setFlagSimulationIsStarted() {
        simulationIsStarted=true;
    }

    public Boolean getSimulationIsFinished() {
        return simulationIsFinished;
    }

    public void setFlagSimulationIsFinished() {
        simulationIsFinished=true;
    }
}
