package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;


public class NextPlanSolveTask extends NextPlan {

    private final NextTask task;
    private int estimatedStepsToSolveTask = 0;
    private float utilization = 0;
    private boolean isPreconditionFulfilled = false;
    private boolean isDeadlineFulfillable = true;

    private int failStatus = 0;


    public NextPlanSolveTask(NextTask task, NextAgent agent) {
        this.agent = agent;
        this.task = task;
        this.agentTask = NextConstants.EAgentActivity.solveTask;
        CheckIfPreConditionIsFulfilled();
        CreateSubPlans();
        calculateProfit();
    }

    /**
     * Berechnet Nutzen/Kosten-Quotient und den maximal erreichbaren Profit
     */
    private void calculateProfit() {
        if (!isPreconditionFulfilled) return;
        int sumOfShortestWays = 0;
        int shortestWayFromDispenserToGoalZone = 0;
        HashSet<NextMapTile> goalZones = agent.GetMap().GetGoalZones();
        HashSet<NextMapTile> dispensers = agent.GetMap().GetDispensers();
        //calculate ways from dispensers to nearestGoalZone
        for (NextMapTile block : task.GetRequiredBlocks()) {
            for (NextMapTile dispenser : dispensers) {
                if (!dispenser.getThingType().equals(block.getThingType())) continue;
                Vector2D nearestGoalZone = NextAgentUtil.GetNearestZone(dispenser.GetPosition(), goalZones);
                // TODO: Hier muss der Pfad verbessert werden
                int wayFromDispenserToGoalZone = NextManhattanPath.CalculatePath(dispenser.GetPosition(), nearestGoalZone).size();
                if (shortestWayFromDispenserToGoalZone == 0 || wayFromDispenserToGoalZone < shortestWayFromDispenserToGoalZone)
                    shortestWayFromDispenserToGoalZone = wayFromDispenserToGoalZone;
            }
            sumOfShortestWays += shortestWayFromDispenserToGoalZone;
        }
        if (sumOfShortestWays > 0) {
            estimatedStepsToSolveTask = sumOfShortestWays;
            setUtilization(sumOfShortestWays);
        }
    }

    /**
     * Berechnet den Nutzen/Kosten-Quotienten, der durch diese Aufgabe erreicht wird
     *
     * @param sumOfShortestWays Summe der Wege von Dispensern zu den GoalZones
     */
    private void setUtilization(int sumOfShortestWays) {
        utilization = (float) task.GetReward() / (float) sumOfShortestWays;
    }

    /**
     * Versucht die Karte zu durchsuchen, um eine Aufgabe zu lösen
     */
    public void FulfillPrecondition() {
        subPlans.add(0, new NextPlanExploreMap(task.GetRequiredBlocks(), agent));
    }

    /**
     * Check if the precondition is Fulfilled
     *
     * @return true if the precondition is fulfilled
     */
    public boolean IsPreconditionFulfilled() {
        // Fix for Task with two or more blocks
        if (task.GetRequiredBlocks().size() > 1) return false;
        return isPreconditionFulfilled;
    }

    /**
     * prüft, ob die Vorbedingung erfüllt ist
     */
    public void CheckIfPreConditionIsFulfilled() {
        HashSet<String> requiredBlocks = task.GetRequiredBlocks().stream().map(NextMapTile::getThingType).collect(Collectors.toCollection(HashSet::new));
        isPreconditionFulfilled = agent.GetMap().IsTaskExecutable(requiredBlocks);
    }

    /**
     * prüft, ob Task noch in der zur Verfügung stehenden Zeit gelöst werden kann
     */
    public void CheckIfDeadlineIsReached() {
        if (agent.GetAgentStatus().GetLastAction().contains("submit") && agent.GetAgentStatus().GetLastActionResult().contains("failed_target") && agent.GetActiveTask().GetName().equals(task.GetName()))
            failStatus += 1;
        else if (agent.GetAgentStatus().GetLastAction().contains("submit") && agent.GetAgentStatus().GetLastActionResult().contains("success") && agent.GetActiveTask().GetName().equals(task.GetName()))
            failStatus = 0;

        int failOffset = 2;
        if (failStatus == failOffset) isDeadlineFulfillable = false;
        int stepsUntilTaskIsFinished = agent.GetSimulationStatus().GetCurrentStep() + estimatedStepsToSolveTask;
        if (stepsUntilTaskIsFinished >= (int) task.GetDeadline()) isDeadlineFulfillable = false;
    }

    /**
     * prüft, ob der Task vollständig erfüllt ist und setzt ggf. die subPlans zurück
     */
    public boolean CheckIfTaskIsFulfilled() {
        if (agent.GetAgentStatus().GetLastAction().equals("submit") && agent.GetAgentStatus().GetLastActionResult().equals("success")) {
            for (NextPlan subplan : subPlans) {
                subplan.SetPlanIsFulfilled(false);
            }
            return true;
        }
        return false;
    }


    /**
     * Erzeugt eine Liste mit subPlans - hier werden zuerst alle Dispenser abgegangen und dann zur Zielzone
     */
    @Override
    public void CreateSubPlans() {
        if (!agent.GetAgentStatus().GetCurrentRole().GetName().equals("worker"))
            subPlans.add(new NextPlanRoleZone(agent, "worker"));
        HashSet<NextMapTile> requiredBlocks = task.GetRequiredBlocks();
        for (NextMapTile block : requiredBlocks) {
            subPlans.add(new NextPlanDispenser(block));
        }
        subPlans.add(new NextPlanGoalZone());
    }

    /**
     * @return Nutzen/Kosten-Quotient
     */
    public float GetUtilization() {
        return utilization;
    }

    public String GetTaskName() {
        return task.GetName();
    }

    public NextTask GetTask() {
        return task;
    }

    /**
     * @return Kann Task noch in der zur Verfügung stehenden Zeit gelöst werden
     */
    public boolean IsDeadlineFulfillable() {
        return isDeadlineFulfillable;
    }

    public void UpdateInternalBelief() {
        CheckIfDeadlineIsReached();
        CheckIfPreConditionIsFulfilled();
        if (CheckIfTaskIsFulfilled()) return;
        for (Iterator<NextPlan> subPlanIterator = subPlans.iterator(); subPlanIterator.hasNext(); ) {
            NextPlan subPlan = subPlanIterator.next();
            if (subPlan instanceof NextPlanRoleZone) {
                if (agent.GetAgentStatus().GetCurrentRole().GetName().equals("worker")) subPlanIterator.remove();
                continue;
            }
            if (subPlan instanceof NextPlanExploreMap) {
                if (isPreconditionFulfilled) subPlanIterator.remove();
                else ((NextPlanExploreMap) subPlan).CheckPreconditionStatus();
                continue;
            }
            if (subPlan instanceof NextPlanDispenser) {
                // prüft, welche Blöcke momentan attached sind
                HashSet<NextMapTile> visibleThings = agent.GetAgentStatus().GetVisibleThings();
                HashSet<Vector2D> attachedElements = agent.GetAgentStatus().GetAttachedElementsVector2D();
                ArrayList<String> attachedBlockTypes = new ArrayList<>();
                for (Vector2D attachedElement : attachedElements) {
                    for (NextMapTile visibleThing : visibleThings) {
                        if (attachedElement.equals(visibleThing.GetPosition())) {
                            if (visibleThing.getThingType().contains("block")) {
                                attachedBlockTypes.add(visibleThing.getThingType().substring(visibleThing.getThingType().length() - 2));
                            }
                        }
                    }
                }
                //prüft, ob Blöcke momentan attached sind und stellt subPlans (goToDispenser) auf fertig
                subPlan.SetPlanIsFulfilled(attachedBlockTypes.contains(((NextPlanDispenser) subPlan).GetDispenser().getThingType()));
            }
        }
    }
}
