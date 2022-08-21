package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextPlanDispenser;
import massim.javaagents.plans.NextPlanGoalZone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;


public class NextGroupPlan {

    private final NextGroup group;
    private final NextTask task;
    ArrayList<NextPlan> subPlans = new ArrayList<>();

    private int estimatedStepsToSolveTask = 0;
    private float utilization = 0;

    private boolean isDeadlineFulfillable = true;
    private boolean isPreconditionFulfilled = false;

    public NextGroupPlan(NextGroup group, NextTask task) {
        this.group = group;
        this.task = task;
        createSubPlans();
    }

    /**
     * Berechnet die Mindestanzahl an Schritten, damit ein Agent seinen Teiltask erfüllen kann
     */
    private void estimateStepsToSolveTask() {
        if (!isPreconditionFulfilled) return;

        int longestWay = 0;
        int shortestWayFromDispenserToGoalZone = 10000;
        int offsetToConnect = 10;

        HashSet<NextMapTile> goalZones = group.GetGroupMap().GetGoalZones();
        HashSet<NextMapTile> dispensers = group.GetGroupMap().GetDispensers();

        //calculate ways from dispensers to nearestGoalZone
        for (NextMapTile block : task.GetRequiredBlocks()) {
            for (NextMapTile dispenser : dispensers) {
                // TODO: Noch Testen, ob dies wirklich den besten Task gibt
                if (!dispenser.getThingType().substring(dispenser.getThingType().length() - 2).equals(block.getThingType())) continue;
                Vector2D nearestGoalZone = NextAgentUtil.GetNearestZone(dispenser.GetPosition(), goalZones);
                // TODO: Hier muss der Pfad verbessert werden
                int wayFromDispenserToGoalZone = NextManhattanPath.CalculatePath(dispenser.GetPosition(), nearestGoalZone).size();
                shortestWayFromDispenserToGoalZone = Math.min(wayFromDispenserToGoalZone, shortestWayFromDispenserToGoalZone);
            }
            longestWay = Math.max(shortestWayFromDispenserToGoalZone, longestWay);
        }
        estimatedStepsToSolveTask = longestWay + offsetToConnect;
        setUtilization(estimatedStepsToSolveTask);
    }

    /**
     * Berechnet den Nutzen/Kosten-Quotienten, der durch diese Aufgabe erreicht wird
     *
     * @param estimatedStepsToSolveTask Summe der Wege von Dispensern zu den GoalZones
     */
    private void setUtilization(int estimatedStepsToSolveTask) {
        utilization = (float) task.GetReward() / (float) estimatedStepsToSolveTask / (float) task.GetRequiredBlocks().size();
    }

    /**
     * @return Nutzen/Kosten-Quotient
     */
    public float GetUtilization() {
        return utilization;
    }

    /**
     * Prüft, ob die Vorbedingung erfüllt ist
     *
     * @return true if the precondition is fulfilled
     */
    public boolean IsPreconditionFulfilled() {
        return isPreconditionFulfilled;
    }

    /**
     * prüft, ob die Vorbedingung erfüllt ist
     */
    private void checkIfPreConditionIsFulfilled() {
        HashSet<String> requiredBlocks = task.GetRequiredBlocks().stream().map(NextMapTile::getThingType).collect(Collectors.toCollection(HashSet::new));
        isPreconditionFulfilled = group.GetGroupMap().IsTaskExecutable(requiredBlocks);
    }

    /**
     * Check if the task can be submitted within the deadline
     */
    public boolean IsDeadlineFulfillable() {
        return isDeadlineFulfillable;
    }

    /**
     * prüft, ob Task noch in der zur Verfügung stehenden Zeit gelöst werden kann
     */
    public void checkIfDeadlineIsReached() {
        int stepsUntilTaskIsFinished = group.GetLastStep() + estimatedStepsToSolveTask;
        isDeadlineFulfillable = stepsUntilTaskIsFinished < (int) task.GetDeadline();
    }

    public NextTask GetTask() {
        return task;
    }

    /**
     * Erzeugt eine Liste mit subPlans - hier werden zuerst alle Dispenser abgegangen und dann zur Zielzone
     */
    private void createSubPlans() {
        HashSet<NextMapTile> requiredBlocks = task.GetRequiredBlocks();
        for (NextMapTile block : requiredBlocks) {
            subPlans.add(new NextPlanDispenser(block));
        }
        subPlans.add(new NextPlanGoalZone());
    }

    public void UpdateInternalBelief() {
        checkIfPreConditionIsFulfilled();
        checkIfDeadlineIsReached();
        estimateStepsToSolveTask();
    }

    public void SetMaxAttemptsAreReached() {
        isDeadlineFulfillable = false;
    }
}
