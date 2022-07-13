package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlanDispenser;
import massim.javaagents.plans.NextPlanGoalZone;

import java.util.HashSet;
import java.util.stream.Collectors;


public class NextGroupPlanTask extends NextGroupPlan {

    private NextGroup group;
    private NextTask task;
    private int estimatedStepsToSolveTask = 0;

    private boolean isDeadlineFulfillable = true;
    private boolean isPreconditionFulfilled = false;

    private int failOffest = 2;
    private int failStatus = 0;


    public NextGroupPlanTask(NextGroup group, NextTask task) {
        this.group = group;
        this.task = task;
        this.agentTask = NextConstants.EAgentActivity.solveTask;
    }

    /**
     * Berechnet die Mindestanzahl an Schritten, damit ein Agent seinen Teiltask erfüllen kann
     */
    private void estimateStepsToSolveTask() {
        if (!isPreconditionFulfilled) return;

        int longestWay = 0;
        int shortestWayFromDispenserToGoalZone = 10000;

        HashSet<NextMapTile> goalZones = group.GetGroupMap().GetGoalZones();
        HashSet<NextMapTile> dispensers = group.GetGroupMap().GetDispensers();

        //calculate ways from dispensers to nearestGoalZone
        for (NextMapTile block : task.GetRequiredBlocks()) {
            for (NextMapTile dispenser : dispensers) {
                // TODO: Vielleicht liegt hier noch ein Fehler
                if (dispenser.getThingType() != block.getThingType()) continue;
                Vector2D nearestGoalZone = NextAgentUtil.GetNearestZone(dispenser.GetPosition(), goalZones);
                // TODO: Hier muss der Pfad verbessert werden
                int wayFromDispenserToGoalZone = NextManhattanPath.CalculatePath(dispenser.GetPosition(), nearestGoalZone).size();
                shortestWayFromDispenserToGoalZone = wayFromDispenserToGoalZone < shortestWayFromDispenserToGoalZone ? wayFromDispenserToGoalZone : shortestWayFromDispenserToGoalZone;
            }
            longestWay = shortestWayFromDispenserToGoalZone > longestWay ? shortestWayFromDispenserToGoalZone : longestWay;
        }
        estimatedStepsToSolveTask = longestWay;
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
        int stepsUntilTaskIsFinished = group.GetLastSimulationStep() + estimatedStepsToSolveTask;
        if (stepsUntilTaskIsFinished >= (int) task.GetDeadline()) isDeadlineFulfillable = false;
    }

    public NextTask GetTask() {
        return task;
    }

    /**
     * Erzeugt eine Liste mit subPlans - Hier werden zuerst alle Dispenser abgegangen und dann zur Zielzone
     */
    @Override
    public void CreateSubPlans() {
        HashSet<NextMapTile> requiredBlocks = task.GetRequiredBlocks();
        for (NextMapTile block : requiredBlocks) {
            subPlans.add(new NextGroupPlanDispenser(block));
        }
        subPlans.add(new NextGroupPlanGoalZone());
    }

    public void UpdateInternalBelief() {
        checkIfPreConditionIsFulfilled();
        checkIfDeadlineIsReached();
    }
}
